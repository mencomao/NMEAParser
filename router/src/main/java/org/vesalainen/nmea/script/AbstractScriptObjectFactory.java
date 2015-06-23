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
package org.vesalainen.nmea.script;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author tkv
 */
public abstract class AbstractScriptObjectFactory<R> implements ScriptObjectFactory<R>
{

    @Override
    public ScriptStatement<R> createSleeper(long millis)
    {
        return new Sleeper<>(millis);
    }

    @Override
    public ScriptStatement<R> createLooper(int times, List<ScriptStatement<R>> sList)
    {
        return new Looper<>(times, sList);
    }

    private static class Sleeper<R> implements ScriptStatement<R>
    {
        private final long millis;
        public Sleeper(long millis)
        {
            this.millis = millis;
        }

        @Override
        public synchronized R exec() throws IOException
        {
            try
            {
                wait(millis);
                return null;
            }
            catch (InterruptedException ex)
            {
                throw new IOException(ex);
            }
        }
    }

    private static class Looper<R> implements ScriptStatement<R>
    {
        private final int times;
        private final List<ScriptStatement<R>> statements;

        public Looper(int times, List<ScriptStatement<R>> statements)
        {
            this.times = times;
            this.statements = statements;
        }
        @Override
        public R exec() throws IOException
        {
            for (int ii=0;ii<times;ii++)
            {
                for (ScriptStatement ss : statements)
                {
                    ss.exec();
                }
            }
            return null;
        }
    }
    
}
