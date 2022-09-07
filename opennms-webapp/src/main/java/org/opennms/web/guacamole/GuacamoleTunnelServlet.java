/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.guacamole;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;

import javax.servlet.http.HttpServletRequest;

public class GuacamoleTunnelServlet extends GuacamoleHTTPTunnelServlet {

    private static final String HEADER_CONNECTION_PROTOCOL = "X-Connection-Protocol";
    private static final String HEADER_CONNECTION_HOSTNAME = "X-Connection-Hostname";

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException {
        // VNC connection information
        // parameters found here: https://guacamole.apache.org/doc/0.8.3/gug/configuring-guacamole.html
        GuacamoleConfiguration config = new GuacamoleConfiguration();
        config.setProtocol(request.getHeader(HEADER_CONNECTION_PROTOCOL));
        config.setParameter("hostname", request.getHeader(HEADER_CONNECTION_HOSTNAME));
        config.setParameter("port", "5900");
        config.setParameter("username", "");
        config.setParameter("password", "password");

        // Connect to guacd, proxying a connection to the VNC server above
        InetGuacamoleSocket inetGuacamoleSocket = new InetGuacamoleSocket("127.0.0.1", 4822);
        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(inetGuacamoleSocket, config);
        return new SimpleGuacamoleTunnel(socket);
    }
}
