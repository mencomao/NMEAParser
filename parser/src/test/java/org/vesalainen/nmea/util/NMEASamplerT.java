/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.util.stream.Streams;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEASamplerT
{
    
    public NMEASamplerT()
    {
    }

    @Test
    public void test1()
    {
        try
        {
            NMEAService service = new NMEAService("224.0.0.3", 10110);
            //service.setLiveClock(false);
            service.start();
            Stream<NMEASample> stream = service.stream("latitude", "longitude");
            stream.forEach((s)->System.err.println(s));
        }
        catch (IOException ex)
        {
            Logger.getLogger(NMEASamplerT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
