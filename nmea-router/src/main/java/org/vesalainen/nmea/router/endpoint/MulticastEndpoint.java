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
package org.vesalainen.nmea.router.endpoint;

import java.io.IOException;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.nmea.jaxb.router.MulticastType;
import org.vesalainen.nmea.router.Router;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
class MulticastEndpoint extends Endpoint<MulticastType,UnconnectedDatagramChannel>
{

    public MulticastEndpoint(MulticastType multicastType, Router router)
    {
        super(multicastType, router);
    }

    @Override
    public UnconnectedDatagramChannel createChannel() throws IOException
    {
        String address = endpointType.getAddress();
        int port = endpointType.getPort();
        return UnconnectedDatagramChannel.open(address, port, bufferSize, true, false);
    }
    
}
