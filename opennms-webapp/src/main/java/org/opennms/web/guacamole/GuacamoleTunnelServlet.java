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

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotSupportedException;
import java.text.MessageFormat;

public class GuacamoleTunnelServlet extends GuacamoleHTTPTunnelServlet {
    private static final Logger LOG = LoggerFactory.getLogger(GuacamoleTunnelServlet.class.getSimpleName());
    private static final String HEADER_GUACD_HOSTNAME = "X-Guacd-Hostname";
    private static final String HEADER_GUACD_PORT = "X-Guacd-Port";
    private static final String HEADER_CONNECTION_PROTOCOL = "X-Connection-Protocol";
    private static final String HEADER_CONNECTION_HOSTNAME = "X-Connection-Hostname";
    private static final String HEADER_VNC_PORT = "X-Vnc-Port";
    private static final String HEADER_VNC_USERNAME = "X-Vnc-Username";
    private static final String HEADER_VNC_PASSWORD = "X-Vnc-Password";
    private static final String HEADER_RDP_PORT = "X-Rdp-Port";
    private static final String HEADER_RDP_USERNAME = "X-Rdp-Username";
    private static final String HEADER_RDP_PASSWORD = "X-Rdp-Password";
    private static final String HEADER_SSH_PORT = "X-Ssh-Port";
    private static final String HEADER_SSH_USERNAME = "X-Ssh-Username";
    private static final String HEADER_SSH_PASSWORD = "X-Ssh-Password";
    private static final String DEFAULT_GUACD_HOSTNAME = "127.0.0.1";
    private static final int DEFAULT_GUACD_PORT = 4822;
    private static final String KEY_CONFIG_PORT = "port";
    private static final String DEFAULT_SSH_VALUE = "22";
    private static final String DEFAULT_RDP_VALUE = "5900";
    private static final String DEFAULT_VNC_PORT = "5900";
    private static final String KEY_CONFIG_USERNAME = "username";
    private static final String KEY_CONFIG_PASSWORD = "password";
    private static final String KEY_CONFIG_HOSTNAME = "hostname";

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException {
        GuacamoleConfiguration config = buildGuacamoleConfiguration(request);
        InetGuacamoleSocket inetGuacamoleSocket = buildInetGuacamoleSocket(request);

        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(inetGuacamoleSocket, config);
        return new SimpleGuacamoleTunnel(socket);
    }

    private GuacamoleConfiguration buildGuacamoleConfiguration(HttpServletRequest request) {
        GuacamoleConfiguration config = new GuacamoleConfiguration();

        String protocol = request.getHeader(HEADER_CONNECTION_PROTOCOL);
        config.setProtocol(protocol);
        config.setParameter(KEY_CONFIG_HOSTNAME, request.getHeader(HEADER_CONNECTION_HOSTNAME));

        switch (protocol.toLowerCase()) {
            case "vnc": {
                config.setParameter(KEY_CONFIG_PORT, getHeader(request, HEADER_VNC_PORT, DEFAULT_VNC_PORT));
                config.setParameter(KEY_CONFIG_USERNAME, getHeader(request, HEADER_VNC_USERNAME, ""));
                config.setParameter(KEY_CONFIG_PASSWORD, getHeader(request, HEADER_VNC_PASSWORD, ""));
                break;
            }
            case "rdp": {
                config.setParameter(KEY_CONFIG_PORT, getHeader(request, HEADER_RDP_PORT, DEFAULT_RDP_VALUE));
                config.setParameter(KEY_CONFIG_USERNAME, getHeader(request, HEADER_RDP_USERNAME, ""));
                config.setParameter(KEY_CONFIG_PASSWORD, getHeader(request, HEADER_RDP_PASSWORD, ""));
                break;
            }
            case "ssh": {
                config.setParameter(KEY_CONFIG_PORT, getHeader(request, HEADER_SSH_PORT, DEFAULT_SSH_VALUE));
                config.setParameter(KEY_CONFIG_USERNAME, getHeader(request, HEADER_SSH_USERNAME, ""));
                config.setParameter(KEY_CONFIG_PASSWORD, getHeader(request, HEADER_SSH_PASSWORD, ""));
                break;
            }
            case "telnet": {
                throw new NotImplementedException("Connection Telnet");
            }
            case "kubernetes": {
                throw new NotImplementedException("Connection Kubernetes");
            }
            default: {
                throw new NotSupportedException("Protocol " + protocol + " not supported");
            }
        }
        return config;
    }

    private InetGuacamoleSocket buildInetGuacamoleSocket(HttpServletRequest request) throws GuacamoleException {
        String guacdHostname = getHeader(request, HEADER_GUACD_HOSTNAME, DEFAULT_GUACD_HOSTNAME);
        int guacdPort = getHeader(request, HEADER_GUACD_PORT, DEFAULT_GUACD_PORT);

        return new InetGuacamoleSocket(guacdHostname, guacdPort);
    }

    private String getHeader(HttpServletRequest request, String header, String defaultValue) {
        String value = request.getHeader(header);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

    private int getHeader(HttpServletRequest request, String header, int defaultValue) {
        String value = getHeader(request, header, null);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            String message = MessageFormat.format("Falling back to " + "default value {} " +
                    "for header value: {}:{}", defaultValue, header, value);
            LOG.warn(message, e);
            return defaultValue;
        }
    }
}
