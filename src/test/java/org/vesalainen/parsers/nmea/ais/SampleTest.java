/*
 * Copyright (C) 2012 Timo Vesalainen
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
package org.vesalainen.parsers.nmea.ais;

import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vesalainen.parsers.nmea.AbstractNMEAObserver;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.util.navi.Average;

/**
 *
 * @author Timo Vesalainen
 */
public class SampleTest
{
    
    public SampleTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     */
    @Test
    public void testSample() throws Exception
    {
        System.out.println("sample");
        InputStream in = SampleTest.class.getClassLoader().getResourceAsStream("nmea-sample");
        IS is = new IS(in);
        NMEAParser parser = NMEAParser.newInstance();
        try
        {
            AD ad = new AD();
            parser.parse(is, new AbstractNMEAObserver(), ad);
            int rows = ad.getRollbacks()+ad.getCommits();
            System.err.println("rows="+rows);
            System.err.println("Latitude "+ad.getAverageLatitude().getMin()+" - "+ad.getAverageLatitude().getMax());
            System.err.println("Longitude "+ad.getAverageLongitude().getMin()+" - "+ad.getAverageLongitude().getMax());
            Average expCourse = new Average(-180, 180, rows, 123456789);
            assertEquals(expCourse.getAverage(), ad.getAverageCourse().getAverage(), 0.0001);
            assertTrue(ad.getRollbacks()/ad.getCommits() < 0.001);
        }
        catch (IOException | IllegalArgumentException ex)
        {
            fail(ex.getMessage());
        }
    }

    public class AD extends AISTracer
    {
        private int commits;
        private int rollbacks;
        private Average averageLongitude = new Average();
        private Average averageLatitude = new Average();
        private Average averageCourse = new Average();

        @Override
        public void setCourse(float course)
        {
            super.setCourse(course);
            averageCourse.add(course);
        }

        @Override
        public void setLatitude(float latitude)
        {
            super.setLatitude(latitude);
            averageLatitude.add(latitude);
        }

        @Override
        public void setLongitude(double longitude)
        {
            super.setLongitude(longitude);
            averageLongitude.add(longitude);
        }

        public Average getAverageLatitude()
        {
            return averageLatitude;
        }

        public Average getAverageCourse()
        {
            return averageCourse;
        }

        public Average getAverageLongitude()
        {
            return averageLongitude;
        }

        @Override
        public void commit(String reason)
        {
            super.commit(reason);
            commits++;
        }
        
        @Override
        public void rollback(String reason)
        {
            super.rollback(reason);
            rollbacks++;
        }

        public int getCommits()
        {
            return commits;
        }

        public int getRollbacks()
        {
            return rollbacks;
        }
        
    }
    public class IS extends InputStream
    {
        private final InputStream in;
        private final StringBuilder sb = new StringBuilder();

        public IS(InputStream in)
        {
            this.in = in;
        }
        
        @Override
        public int read() throws IOException
        {
            int rc = in.read();
            if (rc == '\n')
            {
                sb.append((char)rc);
                System.err.println(sb.toString());
                sb.setLength(0);
            }
            else
            {
                sb.append((char)rc);
            }
            return rc;
        }

        @Override
        public String toString()
        {
            return sb.toString();
        }

    }
}
