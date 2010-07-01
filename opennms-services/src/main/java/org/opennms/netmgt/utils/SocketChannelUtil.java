//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 21 Jul 2003: Changed code to explicitly close sockets.
// 31 Jan 2003: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

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
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * @version $Id: $
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
     * @return SocketChannel object already connected to the remote host/port
     *         pair.
     * @throws java.io.IOException if any.
     * @throws java.lang.InterruptedException if any.
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
