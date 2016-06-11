/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.nmea.processor;

import java.util.stream.Stream;
import org.vesalainen.nmea.util.NMEASample;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public abstract class AbstractProcess extends JavaLogging implements Runnable
{
    protected Stream<NMEASample> stream;

    public AbstractProcess(Class<? extends AbstractProcess> cls)
    {
        super(cls);
    }

    public abstract void init(Stream<NMEASample> stream);
    
    public abstract String[] getPrefixes();
    protected abstract void process(NMEASample sample);
    
    @Override
    public void run()
    {
        stream.forEach(this::process);
    }
    
}
