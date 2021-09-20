/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.opennms.netmgt.config.wsman.WsmanConfig;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.WSManVersion;
import org.opennms.netmgt.config.wsman.Definition;
import org.opennms.netmgt.config.wsman.WsmanAgentConfig;

public interface WSManConfigDao {
    static final String DEFAULT_PROTOCOL = "http";
    static final String DEFAULT_PATH = "/wsman";

    WsmanConfig getConfig();
    Definition getAgentConfig(InetAddress agentInetAddress);
    WSManEndpoint getEndpoint(InetAddress agentInetAddress);

    static WSManEndpoint getEndpoint(WsmanAgentConfig agentConfig, InetAddress agentInetAddress) {
        Objects.requireNonNull(agentConfig, "agentConfig argument");
        Objects.requireNonNull(agentInetAddress, "agentInetAddress argument");
        URL url;
        try {
            String protocol = DEFAULT_PROTOCOL;
            if (agentConfig.isSsl()!= null) {
                protocol = agentConfig.isSsl() ? "https" : "http";
            }

            String port = "";
            if (agentConfig.getPort() != null) {
                port = String.format(":%d", agentConfig.getPort());
            }

            String path = DEFAULT_PATH;
            if (agentConfig.getPath() != null) {
                path = agentConfig.getPath();
            }
            // Prepend a forward slash if missing
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            String host = agentInetAddress.getHostAddress();
            if (agentConfig.isGssAuth()!=null && agentConfig.isGssAuth()) {
                // Always use the canonical host name when using GSS authentication
                host = agentInetAddress.getCanonicalHostName();
            }

            url = new URL(String.format("%s://%s%s%s", protocol, host, port, path));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid endpoint URL: " + e.getMessage());
        }

        final WSManEndpoint.Builder builder = new WSManEndpoint.Builder(url)
                .withServerVersion(WSManVersion.WSMAN_1_0);
        if (agentConfig.getUsername() != null && agentConfig.getPassword() != null) {
            builder.withBasicAuth(agentConfig.getUsername(), agentConfig.getPassword());
        }
        if (agentConfig.isGssAuth() != null && agentConfig.isGssAuth()) {
            builder.withGSSAuth();
        }
        if (agentConfig.getMaxElements() != null) {
            builder.withMaxElements(agentConfig.getMaxElements());
        }
        if (agentConfig.isStrictSsl() != null) {
            builder.withStrictSSL(false);
        }
        if (agentConfig.getTimeout() != null) {
            builder.withConnectionTimeout(agentConfig.getTimeout())
                   .withReceiveTimeout(agentConfig.getTimeout());
        }
        return builder.build();
    }
}
