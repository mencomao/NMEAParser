/*
 * Copyright (C) 2013 Timo Vesalainen
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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This clock is based on system clock in utc time corrected by NMEA sentences.
 * @author Timo Vesalainen
 */
public class GPSClock implements Clock
{
    private final GregorianCalendar wc = (GregorianCalendar) Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
    private final GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
    private long offset;
    private boolean committed;
    @Override
    public long getTime()
    {
        return System.currentTimeMillis() + offset;
    }

    @Override
    public void setTime(int hour, int minute, float second)
    {
        wc.set(Calendar.HOUR_OF_DAY, hour);
        wc.set(Calendar.MINUTE, minute);
        int s = (int) second;
        wc.set(Calendar.SECOND, s);
        wc.set(Calendar.MILLISECOND, (int)((second - (float)s)*1000));
    }

    @Override
    public void setHour(int hour)
    {
        wc.set(Calendar.HOUR_OF_DAY, hour);
    }

    @Override
    public void setMinute(int minute)
    {
        wc.set(Calendar.MINUTE, minute);
    }

    @Override
    public void setSecond(float second)
    {
        int s = (int) second;
        wc.set(Calendar.SECOND, s);
        wc.set(Calendar.MILLISECOND, (int)((second - (float)s)*1000));
    }
    
    private static int getYear(int year)
    {
        if (year < 70)
        {
            return 2000 + year;
        }
        else
        {
            if (year < 100)
            {
                return 1900 + year;
            }
            else
            {
                return year;
            }
        }
    }
    
    @Override
    public void rollback()
    {
        wc.clear();
    }

    private static final int[] Fields = new int[] {
        Calendar.YEAR,
        Calendar.MONTH,
        Calendar.DAY_OF_MONTH,
        Calendar.HOUR_OF_DAY,
        Calendar.MINUTE,
        Calendar.SECOND,
        Calendar.MILLISECOND,
        Calendar.ZONE_OFFSET,
    };

    @Override
    public void commit()
    {
        boolean set = false;
        for (int field : Fields)
        {
            if (wc.isSet(field))
            {
                calendar.set(field, wc.get(field));
                set = true;
            }
        }
        if (set)
        {
            offset = calendar.getTimeInMillis() - System.currentTimeMillis();
            wc.clear();
            committed = true;
        }
    }

    @Override
    public void setDate(int year, int month, int day)
    {
        wc.set(Calendar.DAY_OF_MONTH, day);
        wc.set(Calendar.MONTH, month - 1);
        wc.set(Calendar.YEAR, getYear(year));
    }

    @Override
    public void setDay(int day)
    {
        wc.set(Calendar.DAY_OF_MONTH, day);
    }

    @Override
    public void setMonth(int month)
    {
        wc.set(Calendar.MONTH, month - 1);
    }

    @Override
    public void setYear(int year)
    {
        wc.set(Calendar.YEAR, getYear(year));
    }

    private static final int HourAsMillis = 60*60*1000;
    private static final int MinutesAsMillis = 60*1000;
    @Override
    public void setZoneHours(int localZoneHours)
    {
        wc.set(Calendar.ZONE_OFFSET, localZoneHours*HourAsMillis);
    }

    @Override
    public void setZoneMinutes(int localZoneMinutes)
    {
        wc.set(Calendar.ZONE_OFFSET, wc.get(Calendar.ZONE_OFFSET)+localZoneMinutes*MinutesAsMillis);
    }

    @Override
    public boolean isCommitted()
    {
        return committed;
    }

    @Override
    public GregorianCalendar getCalendar()
    {
        return calendar;
    }

}
