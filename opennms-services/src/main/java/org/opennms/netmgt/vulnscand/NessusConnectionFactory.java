/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.vulnscand;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.opennms.core.utils.ThreadCategory;

class NessusConnectionFactory {
    /**
     * <p>getConnection</p>
     *
     * @param hostname a {@link java.net.InetAddress} object.
     * @param hostport a int.
     * @return a {@link java.net.Socket} object.
     */
    static public Socket getConnection(InetAddress hostname, int hostport) {
        ThreadCategory log = ThreadCategory.getInstance(NessusConnectionFactory.class);

        try {
            Socket retval = new Socket(hostname, hostport);
            return retval;
        } catch (UnknownHostException ex) {
            log.warn(ex.getMessage());
            return null;
        } catch (IOException ex) {
            log.warn(ex.getMessage());
            return null;
        }
    }

    /**
     * <p>releaseConnection</p>
     *
     * @param socket a {@link java.net.Socket} object.
     */
    static public void releaseConnection(Socket socket) {
        ThreadCategory log = ThreadCategory.getInstance(NessusConnectionFactory.class);

        try {
            socket.close();
        } catch (IOException ex) {
            log.error("Could not close socket", ex);
        }
    }
}
