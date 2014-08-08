/*
 * Copyright (C) 2014 Timo Vesalainen
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

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.TimeZone;
import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.util.navi.Knots;

/**
 * TODO
 * AAM
   ALM
   APA
   APB
   BOD
   BWC
   BWR
   BWW
   DBK
   DBS
   DBT
   DPT
   GGA
   GLL
   HDG
   HDM
   HDT
   MTW
   MWV
   R00
   RMA
   RMB
   RMC
   RMM
   ROT
   RPM
   RSA
   RTE
   TXT
   VHW
   VWR
   WCV
   WNC
   WPL
   XTE
   XTR
   ZDA

 * 
 * @author Timo Vesalainen
 */
public class NMEAParserTest
{
    private final NMEAParser parser;
    private final double Epsilon = 0.00001;
    
    public NMEAParserTest()
    {
        parser = NMEAParser.newInstance();
    }

    @Test
    public void aam()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPAAM,A,A,0.10,N,WPTNME*32\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals('G', ss.getProperty("talkerId1"));
                assertEquals('P', ss.getProperty("talkerId2"));
                assertEquals(nch.getChar(1), ss.getProperty("arrivalStatus"));
                assertEquals(nch.getChar(2), ss.getProperty("waypointStatus"));
                assertEquals(nch.getFloat(3), ss.getFloat("arrivalCircleRadius"), Epsilon);
                assertEquals(nch.getString(5), ss.getProperty("waypoint"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void alm()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPALM,1,1,15,1159,00,441d,4e,16be,fd5e,a10c9f,4a2da4,686e81,58cbe1,0a4,001*77\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals('G', ss.getProperty("talkerId1"));
                assertEquals('P', ss.getProperty("talkerId2"));
                assertEquals(nch.getInt(1), ss.getProperty("totalNumberOfMessages"));
                assertEquals(nch.getInt(2), ss.getProperty("messageNumber"));
                assertEquals(nch.getInt(3), ss.getProperty("satellitePRNNumber"));
                assertEquals(nch.getInt(4), ss.getProperty("gpsWeekNumber"));
                assertEquals(nch.getHex(5), ss.getProperty("svHealth"));
                assertEquals(nch.getHex(6), ss.getProperty("eccentricity"));
                assertEquals(nch.getHex(7), ss.getProperty("almanacReferenceTime"));
                assertEquals(nch.getHex(8), ss.getProperty("inclinationAngle"));
                assertEquals(nch.getHex(9), ss.getProperty("rateOfRightAscension"));
                assertEquals(nch.getHex(10), ss.getProperty("rootOfSemiMajorAxis"));
                assertEquals(nch.getHex(11), ss.getProperty("argumentOfPerigee"));
                assertEquals(nch.getHex(12), ss.getProperty("longitudeOfAscensionNode"));
                assertEquals(nch.getHex(13), ss.getProperty("meanAnomaly"));
                assertEquals(nch.getHex(14), ss.getProperty("f0ClockParameter"));
                assertEquals(nch.getHex(15), ss.getProperty("f1ClockParameter"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void apa()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPAPA,A,A,0.10,R,N,V,V,011,M,DEST*3f\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals('G', ss.getProperty("talkerId1"));
                assertEquals('P', ss.getProperty("talkerId2"));
                assertEquals(nch.getChar(1), ss.getProperty("status"));
                assertEquals(nch.getChar(2), ss.getProperty("status2"));
                assertEquals(nch.getFloat(3), ss.getProperty("crossTrackError"));
                assertEquals(nch.getChar(6), ss.getProperty("arrivalStatus"));
                assertEquals(nch.getChar(7), ss.getProperty("waypointStatus"));
                assertEquals(nch.getFloat(8), ss.getProperty(nch.getPrefix(9)+"BearingOriginToDestination"));
                assertEquals(nch.getString(10), ss.getProperty("waypoint"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void apb()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPAPB,A,A,0.10,R,N,V,V,011,M,DEST,011,M,011,M*3c\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals('G', ss.getProperty("talkerId1"));
                assertEquals('P', ss.getProperty("talkerId2"));
                assertEquals(nch.getChar(1), ss.getProperty("status"));
                assertEquals(nch.getChar(2), ss.getProperty("status2"));
                assertEquals(nch.getFloat(3), ss.getProperty("crossTrackError"));
                assertEquals(nch.getChar(6), ss.getProperty("arrivalStatus"));
                assertEquals(nch.getChar(7), ss.getProperty("waypointStatus"));
                assertEquals(nch.getFloat(8), ss.getProperty(nch.getPrefix(9)+"BearingOriginToDestination"));
                assertEquals(nch.getString(10), ss.getProperty("waypoint"));
                assertEquals(nch.getFloat(11), ss.getProperty(nch.getPrefix(12)+"BearingPresentPositionToDestination"));
                assertEquals(nch.getFloat(13), ss.getProperty(nch.getPrefix(14)+"HeadingToSteerToDestination"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void bod()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPBOD,099.3,T,105.6,M,POINTB,*48\r\n",
                "$GPBOD,097.0,T,103.2,M,POINTB,POINTA*4a\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals('G', ss.getProperty("talkerId1"));
                assertEquals('P', ss.getProperty("talkerId2"));
                assertEquals(nch.getFloat(1), ss.getProperty(nch.getPrefix(2)+"Bearing"));
                assertEquals(nch.getFloat(3), ss.getProperty(nch.getPrefix(4)+"Bearing"));
                assertEquals(nch.getString(5), ss.getProperty("toWaypoint"));
                assertEquals(nch.getString(6), ss.getProperty("fromWaypoint"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void bwc()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPBWC,081837,,,,,,T,,M,,N,*13\r\n",
                "$GPBWC,220516,5130.02,N,00046.34,W,213.8,T,218.0,M,0004.6,N,EGLM*11\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals('G', ss.getProperty("talkerId1"));
                assertEquals('P', ss.getProperty("talkerId2"));
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                Clock clock = (Clock) ss.getProperty("clock");
                cal.setTimeInMillis(clock.getTime());
                String hhmmss = nch.getString(1);
                assertEquals(Integer.parseInt(hhmmss.substring(0, 2)), cal.get(Calendar.HOUR_OF_DAY));
                assertEquals(Integer.parseInt(hhmmss.substring(2, 4)), cal.get(Calendar.MINUTE));
                assertEquals(Integer.parseInt(hhmmss.substring(4, 6)), cal.get(Calendar.SECOND));
                assertEquals(nch.getDegree(4), ss.getFloat("latitude"), Epsilon);
                assertEquals(nch.getDegree(6), ss.getFloat("longitude"), Epsilon);
                assertEquals(nch.getFloat(6), ss.getProperty(nch.getPrefix(7)+"Bearing"));
                assertEquals(nch.getFloat(8), ss.getProperty(nch.getPrefix(9)+"Bearing"));
                assertEquals(nch.getFloat(10), ss.getProperty("distanceToWaypoint"));
                assertEquals(nch.getString(12), ss.getProperty("waypoint"));
                assertEquals(nch.getChar(13), ss.getProperty("faaModeIndicator"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void dpt()
    {
        try
        {
            String[] nmeas = new String[] {
                "$IIDPT,007.4,+0.3,*47\r\n",
                "$IIDPT,025.6,+0.3,*45\r\n",
                "$IIDPT,016.5,+0.3,*46\r\n",
                "$IIDPT,014.9,+0.3,*48\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals('I', ss.getProperty("talkerId1"));
                assertEquals('I', ss.getProperty("talkerId2"));
                assertEquals(nch.getFloat(1), ss.getFloat("depthOfWater"), Epsilon);
                assertEquals(nch.getFloat(2), ss.getFloat("depthOffsetOfWater"), Epsilon);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void hdg()
    {
        try
        {
            String[] nmeas = new String[] {
                "$IIHDG,171,,,06,E*13\r\n",
                "$IIHDG,177,,,06,E*15\r\n",
                "$IIHDG,175,,,06,E*17\r\n",
                "$IIHDG,174,,,06,E*16\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals('I', ss.getProperty("talkerId1"));
                assertEquals('I', ss.getProperty("talkerId2"));
                assertEquals(nch.getFloat(1), ss.getFloat("magneticSensorHeading"), Epsilon);
                assertEquals(nch.getFloat(2), ss.getFloat("magneticDeviation"), Epsilon);
                assertEquals(nch.getFloat(4), ss.getFloat("magneticVariation"), Epsilon);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void mwv()
    {
        try
        {
            String[] nmeas = new String[] {
                "$IIMWV,282,R,11.6,N,A*1D\r\n",
                "$IIMWV,284,T,12.3,N,A*1B\r\n",
                "$IIMWV,280,R,11.6,N,A*1F\r\n",
                "$IIMWV,282,T,11.6,N,A*1B\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals('I', ss.getProperty("talkerId1"));
                assertEquals('I', ss.getProperty("talkerId2"));
                String rt = nch.getPrefix(2);
                assertEquals(nch.getFloat(1), ss.getFloat(rt+"WindAngle"), Epsilon);
                assertEquals(Knots.toMetersPerSecond(nch.getFloat(3)), ss.getFloat("windSpeed"), Epsilon);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void rmc()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPRMC,062455,A,6009.2054,N,02453.6493,E,000.0,001.3,171009,,,A*78\r\n",
                "$GPRMC,062456,A,6009.2054,N,02453.6493,E,000.0,001.3,171009,,,A*7B\r\n",
                "$GPRMC,062457,A,6009.2053,N,02453.6493,E,000.0,001.3,171009,,,A*7D\r\n",
                "$GPRMC,062458,A,6009.2053,N,02453.6493,E,000.0,001.3,171009,,,A*72\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals('G', ss.getProperty("talkerId1"));
                assertEquals('P', ss.getProperty("talkerId2"));
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                Clock clock = (Clock) ss.getProperty("clock");
                cal.setTimeInMillis(clock.getTime());
                String hhmmss = nch.getString(1);
                assertEquals(Integer.parseInt(hhmmss.substring(0, 2)), cal.get(Calendar.HOUR_OF_DAY));
                assertEquals(Integer.parseInt(hhmmss.substring(2, 4)), cal.get(Calendar.MINUTE));
                assertEquals(Integer.parseInt(hhmmss.substring(4, 6)), cal.get(Calendar.SECOND));
                assertEquals(nch.getChar(2), ss.getProperty("status"));
                assertEquals(nch.getDegree(3), ss.getFloat("latitude"), Epsilon);
                assertEquals(nch.getDegree(5), ss.getFloat("longitude"), Epsilon);
                assertEquals(nch.getFloat(7), ss.getFloat("speedOverGround"), Epsilon);
                assertEquals(nch.getFloat(8), ss.getFloat("trackMadeGood"), Epsilon);
                String ddmmyy = nch.getString(9);
                assertEquals(Integer.parseInt(ddmmyy.substring(0, 2)), cal.get(Calendar.DAY_OF_MONTH));
                assertEquals(Integer.parseInt(ddmmyy.substring(2, 4)), cal.get(Calendar.MONTH)+1);
                assertEquals(2000+Integer.parseInt(ddmmyy.substring(4, 6)), cal.get(Calendar.YEAR));
                assertEquals(nch.getFloat(10), ss.getFloat("magneticVariation"), Epsilon);
                assertEquals(nch.getChar(12), ss.getProperty("faaModeIndicator"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

}