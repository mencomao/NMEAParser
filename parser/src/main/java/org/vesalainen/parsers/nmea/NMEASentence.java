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
package org.vesalainen.parsers.nmea;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Locale;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import static org.vesalainen.parsers.nmea.Converter.*;
import static org.vesalainen.parsers.nmea.MessageType.*;
import static org.vesalainen.parsers.nmea.TalkerId.*;
import org.vesalainen.util.CharSequences;

/**
 * A utility for creating NMEA sentences
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEASentence
{
    private ByteBuffer buffer;
    private CharSequence seq;
    /**
     * Creates NMEASentence from string
     * @param sentence
     * @throws IOException 
     */
    public NMEASentence(CharSequence sentence) throws IOException
    {
        if (!NMEA.isNMEAOrAIS(sentence))
        {
            throw new IllegalArgumentException(sentence+" not valid");
        }
        buffer = ByteBuffer.wrap(sentence.toString().getBytes("NMEA"));
        seq = CharSequences.getAsciiCharSequence(buffer);
    }
    /**
     * Creates NMEASentence from array
     * @param buffer 
     */
    private NMEASentence(byte[] buffer, int offset, int length)
    {
        this.buffer = ByteBuffer.wrap(buffer, offset, length);
        seq = CharSequences.getAsciiCharSequence(buffer, offset, length);
        if (!NMEA.isNMEAOrAIS(seq))
        {
            throw new IllegalArgumentException(seq+" not valid");
        }
    }
    /**
     * Recommended Minimum Specific GNSS Data - only declination
     * @param declination
     * @return
     * @throws IOException 
     */
    public static NMEASentence rmc(double declination) throws IOException
    {
        String variation = String.format(Locale.US, "%.1f", Math.abs(declination));
        char ew = declination > 0 ? 'E' : 'W';
        return builder(IN, RMC)
                .add()      // utc
                .add('A')   // status
                .add()      // lat
                .add()      // N / S
                .add()      // lon
                .add()      // E / W
                .add()      // sog
                .add()      // tmg
                .add()      // ddmmyy
                .add(variation)    // Magnetic variation degrees
                .add(ew)    // E / W
                .add('A')   // FAA
                .build();
    }
    /**
     * Depth Below Transducer
     * @param depth
     * @return
     * @throws IOException 
     */
    public static NMEASentence dbt(float depth, UnitType unit) throws IOException
    {
        return builder(SD, DBT)
                .add(unit.convertTo(depth, Foot))
                .add(FT)
                .add(unit.convertTo(depth, Meter))
                .add(M)
                .add(unit.convertTo(depth, Fathom))
                .add(FATH)
                .build();
    }
    /**
     * Water Speed and Heading
     * @param speed
     * @return
     * @throws IOException 
     */
    public static NMEASentence vhw(double speed, UnitType unit) throws IOException
    {
        return builder(VW, VHW)
                .add().add().add().add()
                .add(unit.convertTo(speed, Knot))
                .add(KTS)
                .add(unit.convertTo(speed, UnitType.KMH))
                .add(Converter.KMH)
                .build();
    }
    /**
     * Wind speed and Angle
     * @param windAngle
     * @param windSpeed
     * @param trueWind
     * @return
     * @throws IOException 
     */
    public static NMEASentence mwv(int windAngle, double windSpeed, UnitType unit, boolean trueWind) throws IOException
    {
        return builder(UP, MWV)
                .add(windAngle)
                .add(trueWind ? 'T' : 'R')
                .add(unit.convertTo(windSpeed, Knot))
                .add(KTS)
                .add('A')
                .build();
    }
    /**
     * Water Temperature
     * @param temperature
     * @param unit
     * @return
     * @throws IOException 
     */
    public static NMEASentence mtw(float temperature, UnitType unit) throws IOException
    {
        return builder(YC, MTW)
                .add(unit.convertTo(temperature, Celsius))
                .add(CELCIUS)
                .build();
    }
    /**
     * Text Transmission
     * @param msg
     * @return
     * @throws IOException 
     */
    public static NMEASentence txt(String msg) throws IOException
    {
        if (msg.indexOf(',') != -1 || msg.indexOf('*') != -1)
        {
            throw new IllegalArgumentException(msg+" contains (,) or (*)");
        }
        return builder(U0, TXT)
                .add(1)
                .add(1)
                .add()
                .add(msg)
                .build();
    }
    /**
     * Returns byte buffer containing the sentence.
     * @return 
     */
    public ByteBuffer getByteBuffer()
    {
        return buffer.duplicate();
    }
    /**
     * Returns true if NMEA sentence
     * @return 
     */
    public boolean isNMEA()
    {
        return NMEA.isNMEA(seq);
    }
    /**
     * Returns true if AIS sentence
     * @return 
     */
    public boolean isAIS()
    {
        return NMEA.isAIS(seq);
    }
    /**
     * Returns true if proprietary sentence
     * @return 
     */
    public boolean isProprietary()
    {
        return NMEA.isProprietory(seq);
    }
    /**
     * Returns talker-id if NMEA/AIS sentence or null
     * @return 
     */
    public TalkerId getTalkerId()
    {
        return NMEA.getTalkerId(seq);
    }
    /**
     * Returns message-id if NMEA/AIS sentence or null
     * @return 
     */
    public MessageType getMessageType()
    {
        return NMEA.getMessageType(seq);
    }
    /**
     * Returns sentence prefix. (before first ',')
     * @return 
     */
    public CharSequence getPrefix()
    {
        return NMEA.getPrefix(seq);
    }
    /**
     * Write sentence to bb
     * @param bb 
     */
    public void writeTo(ByteBuffer bb)
    {
        bb.put(buffer);
        buffer.flip();
    }
    /**
     * Write sentence to out
     * @param out
     * @throws IOException 
     */
    public void writeTo(OutputStream out) throws IOException
    {
        out.write(buffer.array(), 0, buffer.limit());
    }
    /**
     * Write sentence to out
     * @param out
     * @throws IOException 
     */
    public void writeTo(Appendable out) throws IOException
    {
        out.append(seq);
    }
    public void writeTo(ByteChannel channel) throws IOException
    {
        channel.write(buffer);
        buffer.flip();
    }
    /**
     * Returns sentence as string
     * @return 
     */
    @Override
    public String toString()
    {
        return seq.toString();
    }
    /**
     * Creates NMEASentence builder. Builder is initialized with '$'/'!', talker-id
     * and message-id
     * @param talkerId
     * @param messageType
     * @return 
     */
    public static Builder builder(TalkerId talkerId, MessageType messageType)
    {
        return new Builder(talkerId, messageType);
    }
    /**
     * Creates NMEASentence builder. Builder is initialized with prefix which
     * can contain the whole sentence until '*' including commas. Each field
     * if present are concatenated and separated with commas
     * @param prefix
     * @param fields
     * @return 
     */
    public static Builder builder(CharSequence prefix, CharSequence... fields)
    {
        return new Builder(prefix, fields);
    }
    public static final class Builder
    {
        private byte[] buffer = new byte[128];
        private int index;

        private Builder(TalkerId talkerId, MessageType messageType)
        {
            switch (messageType)
            {
                case VDM:
                case VDO:
                    write('!');
                    break;
                default:
                    write('$');
                    break;
            }
            write(talkerId.name());
            write(messageType.name());
        }

        private Builder(CharSequence prefix, CharSequence... fields)
        {
            write(prefix);
            for (CharSequence field : fields)
            {
                add(field);
            }
        }
        /**
         * Add empty field
         * @return 
         */
        public Builder add()
        {
            write(',');
            return this;
        }
        /**
         * Add string field
         * @param fld
         * @return 
         */
        public Builder add(CharSequence... fld)
        {
            for (CharSequence f : fld)
            {
                add();
                try
                {
                    write(f.toString().getBytes("NMEA"));
                }
                catch (UnsupportedEncodingException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
            return this;
        }
        /**
         * Add double field
         * @param fld
         * @return 
         */
        public Builder add(double fld)
        {
            add();
            return write(String.format(Locale.US, "%.1f", fld));
        }
        /**
         * Add int field
         * @param fld
         * @return 
         */
        public Builder add(int fld)
        {
            add();
            return write(String.valueOf(fld));
        }
        /**
         * Add char field
         * @param fld
         * @return 
         */
        public Builder add(char fld)
        {
            add();
            return write(fld);
        }
        /**
         * Adds '*' starting suffix with checksum and returns NMEASentence.
         * @return 
         */
        public NMEASentence build()
        {
            NMEAChecksum checksum = new NMEAChecksum();
            checksum.update(buffer, 0, index);
            checksum.fillSuffix(buffer, index);
            index += 5;
            return new NMEASentence(buffer, 0, index);
        }

        private Builder write(int c)
        {
            buffer[index++] = (byte) c;
            return this;
        }
        private Builder write(byte[] b)
        {
            System.arraycopy(b, 0, buffer, index, b.length);
            index += b.length;
            return this;
        }

        private Builder write(CharSequence seq)
        {
            seq.chars().forEach(this::write);
            return this;
        }
    }
}
