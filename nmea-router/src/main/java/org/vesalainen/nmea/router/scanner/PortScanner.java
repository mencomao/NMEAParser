/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.nmea.router.scanner;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.ScatteringByteChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import static java.util.logging.Level.*;
import org.vesalainen.nio.RingBuffer;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nmea.router.NMEAMatcher;
import org.vesalainen.nmea.router.PortType;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.Matcher;
import org.vesalainen.util.RepeatingIterator;
import org.vesalainen.util.logging.JavaLogging;
import static org.vesalainen.nmea.router.ThreadPool.*;
import org.vesalainen.util.AbstractProvisioner.Setting;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PortScanner extends JavaLogging
{
    private static final int BUF_SIZE = 128;
    private Set<PortType> portTypes;
    private long checkDelay = 5000;
    private long closeDelay = 1000;
    private long fingerPrintDelay = 10000;
    private Set<String> ports = new HashSet<>();
    private Map<String,Iterator<PortType>> channelIterators = new HashMap<>();
    private Map<String,Future<Throwable>> futures = new HashMap<>();
    private Map<String,Scanner> scanners = new HashMap<>();
    private ScheduledFuture<?> scanFuture;
    private Consumer<ScanResult> consumer;
    private Set<String> distinguishSet;

    public PortScanner()
    {
        super(PortScanner.class);
    }

    public PortScanner setPorts(Collection<String> ports)
    {
        this.ports.addAll(ports);
        return this;
    }
    /**
     * Add new port for scan. If scan has been started but already stopped it is restarted.
     * @param port
     * @throws IOException 
     */
    public PortScanner addPort(String port) throws IOException
    {
        ports.add(port);
        if (!isScanning() && consumer != null && distinguishSet != null)
        {
            scan(consumer, distinguishSet);
        }
        return this;
    }
    public void stop()
    {
        if (scanFuture != null )
        {
            scanFuture.cancel(false);
        }
    }
    public boolean isScanning()
    {
        return scanFuture != null && !scanFuture.isDone();
    }
    public void waitScanner() throws IOException
    {
        waitScanner(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    public void waitScanner(long time, TimeUnit unit) throws IOException
    {
        if (scanFuture == null)
        {
            return;
        }
        try
        {
            scanFuture.get(time, unit);
        }
        catch (CancellationException ex)
        {
        }
        catch (InterruptedException | ExecutionException | TimeoutException ex)
        {
            throw new IOException(ex);
        }
    }
    public PortScanner setChannelSuppliers(Set<PortType> portTypes)
    {
        this.portTypes = portTypes;
        return this;
    }
    @Setting
    public PortScanner setCheckDelay(long checkDelay)
    {
        this.checkDelay = checkDelay;
        return this;
    }
    @Setting
    public PortScanner setCloseDelay(long closeDelay)
    {
        this.closeDelay = closeDelay;
        return this;
    }
    @Setting
    public PortScanner setFingerPrintDelay(long fingerPrintDelay)
    {
        this.fingerPrintDelay = fingerPrintDelay;
        return this;
    }
    private void checkDelays()
    {
        if (!(closeDelay < checkDelay && checkDelay < fingerPrintDelay))
        {
            throw new IllegalArgumentException("closeDelay < checkPeriod < fingerPrintPeriod");
        }
    }
    public void scan(Consumer<ScanResult> consumer) throws IOException
    {
        scan(consumer, Collections.EMPTY_SET);
    }
    public void scan(Consumer<ScanResult> consumer, Set<String> distinguishSet) throws IOException
    {
        if (isScanning())
        {
            throw new IllegalStateException("scan is already running");
        }
        checkDelays();
        if (ports.isEmpty())
        {
            warning("no ports");
            return;
        }
        if (portTypes == null || portTypes.isEmpty())
        {
            warning("no channel suppliers");
            return;
        }
        Objects.requireNonNull(consumer, "consumer");
        Objects.requireNonNull(distinguishSet, "distinguishSet");
        this.consumer = consumer;
        this.distinguishSet = distinguishSet;
        for (String port : ports)
        {
            RepeatingIterator<PortType> it = new RepeatingIterator<>(portTypes);
            channelIterators.put(port, it);
            startScanner(port, 0);
        }
        Monitor monitor = new Monitor();
        scanFuture = POOL.scheduleWithFixedDelay(monitor, checkDelay, checkDelay, TimeUnit.MILLISECONDS);
    }
    private void startScanner(String port, long delayMillis) throws IOException
    {
        config("starting scanner for %s after %d millis", port, delayMillis);
        Iterator<PortType> it = channelIterators.get(port);
        if (it.hasNext())
        {
            PortType portType = it.next();
            Scanner scanner = new Scanner(port, portType);
            scanners.put(port, scanner);
            Future<Throwable> future = POOL.schedule(scanner, delayMillis, TimeUnit.MILLISECONDS);
            futures.put(port, future);
        }
        else
        {
            throw new IllegalArgumentException("should not happen");
        }
    }
    private class Monitor implements Runnable
    {

        @Override
        public void run()
        {
            Iterator<String> iterator = ports.iterator();
            while (iterator.hasNext())
            {
                String port = iterator.next();
                boolean rescan = false;
                Scanner scanner = scanners.get(port);
                Future<Throwable> future = futures.get(port);
                if (future.isDone())
                {
                    if (!future.isCancelled())
                    {
                        fine("finger print for %s because distinguish hit", port, fingerPrintDelay, scanner.getFingerPrint());
                        ScanResult sr = new ScanResult(scanner);
                        consumer.accept(sr);
                        iterator.remove();
                    }
                    else
                    {
                        try
                        {
                            log(SEVERE, future.get(), "rescanning %s because stopped", port);
                        }
                        catch (InterruptedException | ExecutionException ex)
                        {
                            log(SEVERE, ex, "%s", ex.getMessage());
                        }
                        rescan = true;
                    }
                }
                else
                {
                    if (scanner.getElapsedTime() >= checkDelay && scanner.getFingerPrint().isEmpty())
                    {
                        fine("rescanning %s because no finger print after %d millis", port, checkDelay);
                        rescan = true;
                    }
                    else
                    {
                        if (scanner.getElapsedTime() >= fingerPrintDelay)
                        {
                            fine("finger print for %s after %d millis %s", port, fingerPrintDelay, scanner.getFingerPrint());
                            ScanResult sr = new ScanResult(scanner);
                            consumer.accept(sr);
                            iterator.remove();
                            future.cancel(true);
                        }
                    }
                }
                if (rescan)
                {
                    future.cancel(true);
                    try
                    {
                        future.get();
                    }
                    catch (CancellationException | InterruptedException | ExecutionException ex)
                    {
                        fine("cancelled %s", scanner);
                    }
                    try
                    {
                        startScanner(port, closeDelay);
                    }
                    catch (IOException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                }
            }
            if (ports.isEmpty())
            {
                scanFuture.cancel(false);
            }
        }
        
    }
    private class Scanner implements Callable<Throwable>
    {
        private String port;
        private PortType portType;
        private RingByteBuffer ring = new RingByteBuffer(BUF_SIZE, true);
        private NMEAMatcher<Boolean> matcher;
        private Set<String> fingerPrint = new HashSet<>();
        private int count;
        private int matched;
        private boolean mark = true;
        private Matcher.Status match;
        private long time;

        public Scanner(String port, PortType portType) throws IOException
        {
            this.port = port;
            this.portType = portType;
            matcher = new NMEAMatcher<>();
            matcher.addExpression("$", true);
            matcher.addExpression("!", true);
            matcher.compile();
            time = System.currentTimeMillis();
        }

        @Override
        public Throwable call() throws Exception
        {
            try (ScatteringByteChannel channel = portType.getChannelFactory().apply(port))
            {
                config("started scanner for %s %s", port, channel);
                while (true)
                {
                    int cnt = ring.read(channel);
                    if (cnt == -1)
                    {
                        return new EOFException(channel.toString());
                    }
                    count += cnt;
                    while (ring.hasRemaining())
                    {
                        byte b = ring.get(mark);
                        match = matcher.match(b);
                        switch (match)
                        {
                            case Error:
                                finest("drop: '%1$c' %1$d 0x%1$02X %2$s", b & 0xff, (RingBuffer)ring);
                                mark = true;
                                break;
                            case Ok:
                            case WillMatch:
                                mark = false;
                                break;
                            case Match:
                                finer("read: %s", ring);
                                int idx = CharSequences.indexOf(ring, ',');
                                if (idx != -1)
                                {
                                    String prefix = ring.subSequence(0, idx).toString();
                                    fingerPrint.add(prefix);
                                    if (distinguishSet.contains(prefix))
                                    {
                                        return null;
                                    }
                                }
                                matched += ring.length();
                                mark = true;
                                break;
                        }
                    }
                }
            }
            catch (Throwable ex)
            {
                return ex;
            }
            finally
            {
                fine("scanner %s exit", port);
            }
        }

        public long getElapsedTime()
        {
            return System.currentTimeMillis() - time;
        }
        
        public Set<String> getFingerPrint()
        {
            return fingerPrint;
        }

        public int getCount()
        {
            return count;
        }

        public int getMatched()
        {
            return matched;
        }
        
        public int getErrors()
        {
            return count - matched;
        }

        @Override
        public String toString()
        {
            return "Scanner{" + "port=" + port + ", fingerPrint=" + fingerPrint + ", count=" + count + ", matched=" + matched + '}';
        }
        
    }
    public static class ScanResult
    {
        private String port;
        private PortType portType;
        private Set<String> fingerPrint;

        public ScanResult(Scanner scanner)
        {
            this.port = scanner.port;
            this.portType = scanner.portType;
            this.fingerPrint = scanner.fingerPrint;
        }

        public String getPort()
        {
            return port;
        }

        public PortType getPortType()
        {
            return portType;
        }

        public Set<String> getFingerPrint()
        {
            return fingerPrint;
        }

        @Override
        public String toString()
        {
            return "ScanResult{" + "port=" + port + ", portType=" + portType + ", fingerPrint=" + fingerPrint + '}';
        }

    }
}