/*
 * Copyright (C) 2015 tkv
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
package org.vesalainen.parsers.seatalk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserConstants;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.RecoverMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.InputReader;
import static org.vesalainen.parsers.nmea.Converter.*;
import org.vesalainen.parsers.nmea.LocalNMEAChecksum;
import org.vesalainen.parsers.nmea.NMEAChecksum;
import org.vesalainen.parsers.nmea.NMEAGen;
import org.vesalainen.util.navi.Knots;
import org.vesalainen.util.navi.Meters;
import org.vesalainen.util.navi.Velocity;

/**
 *
 * @author tkv
 * @see <a href="http://www.thomasknauf.de/seatalk.htm">SeaTalk Technical Reference</a>
 */
@GenClassname("org.vesalainen.parsers.seatalk.SeaTalk2NMEAImpl")
@GrammarDef()
@Rules(
{
    @Rule(left = "statements", value = "statement*"),
    @Rule(left = "statement", value = "m00"),
    @Rule(left = "statement", value = "m20"),
    @Rule(left = "statement", value = "m23"),
    @Rule(left = "statement", value = "m26"),
    @Rule(left = "statement", value = "m27"),
    @Rule(left = "statement", value = "m65")
})
public abstract class SeaTalk2NMEA
{
    private static final LocalNMEAChecksum localChecksum = new LocalNMEAChecksum();
    private static final String talkerId = "ST";
    
    @Rule("'\\x00' '\\x02' b integer")
    protected void m00(
            char yz, 
            int xx, 
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        int y = yz >> 4;
        int z = yz & 0xf;
        boolean anchorAlarm = (y & 8) == 8;
        boolean metric = (y & 4) == 4;
        boolean defect = (z & 4) == 4;
        boolean deepAlarm = (z & 2) == 2;
        boolean shallowAlarm = (z & 1) == 1;
        bb.flip();
        target.write(bb);
        bb.clear();
        NMEAGen.dbt(talkerId, bb, (float)xx/10F);
    }
    @Rule("'\\x20' '\\x01' integer")
    protected void m20(
            int xx, 
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        float knots = (float)xx/10;
        bb.flip();
        target.write(bb);
        bb.clear();
        NMEAGen.vhw(talkerId, bb, knots);
    }
    @Rule("'\\x23' '\\x01' b b")
    protected void m23(
            char c, 
            char f, 
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        bb.flip();
        target.write(bb);
        bb.clear();
        NMEAGen.mtw(talkerId, bb, c);
    }
    @Rule("'\\x26' '\\x04' integer integer b")
    protected void m26(
            int xx, 
            int yy,
            char de,
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        float knots = (float)xx/100;
        bb.flip();
        target.write(bb);
        bb.clear();
        NMEAGen.vhw(talkerId, bb, knots);
    }
    @Rule("'\\x27' '\\x01' integer")
    protected void m27(
            int xx, 
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        bb.flip();
        target.write(bb);
        bb.clear();
        float temp = (float)(xx-100)/10;
        NMEAGen.mtw(talkerId, bb, temp);
    }
    @Rule("'\\x65' '\\x00' '\\x02'")
    protected void m65(
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
    }
    @Rule("b b")
    protected int integer(char x2, char x1)
    {
        return (x1<<8)+x2;
    }
    @Terminal(expression="[\\x00-\\xff]")
    protected abstract char b(char b);
    
    public static SeaTalk2NMEA newInstance()
    {
        return (SeaTalk2NMEA) GenClassFactory.loadGenInstance(SeaTalk2NMEA.class);
    }
    public void parse(
            ScatteringByteChannel channel,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(80);
        parse(channel, bb, target);
    }
    @ParseMethod(start = "statements", size = 256, charSet = "US-ASCII" )
    protected abstract void parse(
            ScatteringByteChannel channel,
            @ParserContext("bb") ByteBuffer bb,
            @ParserContext("target") WritableByteChannel target
    ) throws IOException;
    @RecoverMethod
    public void recover(
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader,
            @ParserContext(ParserConstants.THROWABLE) Throwable thr,
            @ParserContext("bb") ByteBuffer bb) throws IOException
    {
        int columnNumber = reader.getColumnNumber();
        int length = reader.getLength();
        reader.clear();
        bb.clear();
    }
}