/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.bootstrap;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;

public class HostRMIServerSocketFactory extends RMISocketFactory implements RMIServerSocketFactory, RMIClientSocketFactory, Serializable {

    private static final long serialVersionUID = 2980115395787642955L;

    private String m_host;

    public HostRMIServerSocketFactory() {
    }

    public HostRMIServerSocketFactory(final String host) {
        m_host = host;
    }

    @Override
    public ServerSocket createServerSocket(final int port) throws IOException {
        final InetAddress address;
        if ("localhost".equals(m_host)) {
            address = InetAddress.getLoopbackAddress();
        } else {
            address = InetAddress.getByName(m_host);
        }
        return new ServerSocket(port, -1, address);
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        return RMISocketFactory.getDefaultSocketFactory().createSocket(host, port);
    }

    public String getHost() {
        return m_host;
    }
    public void setHost(final String host) {
        m_host = host;
    }
}
