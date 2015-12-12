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
package org.vesalainen.nmea.router;

import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public abstract class AbstractNMEAFilter implements MessageFilter
{
    protected final JavaLogging log = new JavaLogging();
    protected enum Cond {Accept, Reject, GoOn};

    public AbstractNMEAFilter()
    {
        log.setLogger(this.getClass());
    }

    @Override
    public boolean accept(CharSequence cs)
    {
        int prev = 0;
        int count = 0;
        int length = cs.length();
        for (int ii=0;ii<length;ii++)
        {
            int cc = cs.charAt(ii);
            if (cc == ',' || cc == '*')
            {
                Cond cond = acceptField(cs, count++, prev, ii);
                switch (cond)
                {
                    case Accept:
                        return true;
                    case Reject:
                        return false;
                }
                prev = ii+1;
            }
        }
        return true;
    }

    protected abstract Cond acceptField(CharSequence cs, int index, int begin, int end);
    
}
