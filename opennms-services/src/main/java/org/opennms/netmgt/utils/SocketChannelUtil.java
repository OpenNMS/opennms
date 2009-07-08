/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2002-2004, 2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Class to obtain a connected SocketChannel object.
 * 
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 */
public class SocketChannelUtil extends Object {

    /**
     * This will attempt to connect to the passed host and port. The connection
     * will be made in non-blocking mode, so if there is no route to the host,
     * then it won't hold up capsd or the poller.
     * 
     * Once a connection is made, the channel is returned to blocking mode.
     * 
     * @param host
     *            remote host
     * @param port
     *            port
     * @param timeout
     *            timeout (ms)
     * 
     * @return SocketChannel object already connected to the remote host/port
     *         pair.
     */
    public static SocketChannel getConnectedSocketChannel(InetAddress host, int port, int timeout) throws IOException, InterruptedException {
        SocketChannel sChannel = null;

        try {
            // try to connect first as non-blocking
            sChannel = SocketChannel.open();
            sChannel.configureBlocking(false);
            sChannel.connect(new InetSocketAddress(host, port));
            long startConnectTime = System.currentTimeMillis();

            // see if connected
            do {
                if (!sChannel.finishConnect()) {
                    Thread.sleep(100);
                }
            } while (!sChannel.isConnected() && (System.currentTimeMillis() - startConnectTime) <= timeout);

            // check timeout
            if (!sChannel.isConnected()) {
                if (sChannel.socket() != null)
                    sChannel.socket().close();

                sChannel.close();
                sChannel = null;
            } else {
                // we're connected, so return channel to blocking mode. Avoids
                // No Route to Host errors.
                sChannel.configureBlocking(true);
                sChannel.socket().setSoTimeout(timeout);
            }
        } catch (IOException e) {
            if (sChannel != null) {
                if (sChannel.socket() != null)
                    sChannel.socket().close();
                sChannel.close();
                sChannel = null;
            }
            throw e;
        } catch (InterruptedException e) {
            if (sChannel != null) {
                if (sChannel.socket() != null)
                    sChannel.socket().close();
                sChannel.close();
                sChannel = null;
            }
            throw e;
        }

        return sChannel;
    }
}
