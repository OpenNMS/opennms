/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jmx.connection;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;

public class JmxConnectionConfigBuilder {

    public static JmxConnectionConfigBuilder buildFrom(String ipAddress, Map<String, String> properties) {
        InetAddress inetAddress = InetAddressUtils.getInetAddress(ipAddress);
        return buildFrom(inetAddress, properties);
    }

    public static JmxConnectionConfigBuilder buildFrom(InetAddress ipAddress, Map<String, String> properties) {

        final JmxConnectionConfigBuilder builder = new JmxConnectionConfigBuilder();
        builder.withIpAddress(ipAddress);
        builder.withSunCacao(ParameterMap.getKeyedBoolean(properties, "sunCacao", false));
        builder.withUsername(properties.get("username"));
        builder.withPassword(properties.get("password"));
        builder.withFactory(properties.getOrDefault("factory", "STANDARD"));

        // Legacy
        if (properties.get("url") == null) {
            String url = createLegacyUrl(ipAddress, properties);
            builder.withUrl(url);
            builder.withPort(properties.getOrDefault("port", "1099"));
        } else {
            builder.withUrl(properties.get("url"));
        }
        return builder;
    }

    private static String createLegacyUrl(InetAddress ipAddress, Map<String, String> properties) {
        final String ipAddrString = InetAddressUtils.toUrlIpAddress(ipAddress);
        final String port = properties.getOrDefault("port", "1099");
        final String protocol = properties.getOrDefault("protocol", "rmi");
        final String urlPath = properties.getOrDefault("urlPath",  "/jmxrmi");
        final String rmiServerPort = properties.getOrDefault("rmiServerport",  "45444");
        final boolean remote = ParameterMap.getKeyedBoolean(properties, "remoteJMX",  false);

        if(remote) {
            final String url = String.format("service:jmx:%s:%s:%s://jndi/%s://%s:%s%s",
                    protocol,
                    ipAddrString,
                    rmiServerPort,
                    protocol,
                    ipAddrString,
                    port,
                    urlPath);
            return url;
        } else {
            final String url = String.format("service:jmx:%s:///jndi/%s://%s:%s%s",
                    protocol,
                    protocol,
                    ipAddrString,
                    port,
                    urlPath);
            return url;
        }
    }

    private JmxConnectionConfig config = new JmxConnectionConfig();

    public JmxConnectionConfigBuilder withIpAddress(InetAddress ipAddress) {
        config.setIpAddress(ipAddress);
        return this;
    }

    public JmxConnectionConfigBuilder withPassword(String password) {
        config.setPassword(password);
        return this;
    }

    public JmxConnectionConfigBuilder withUsername(String username) {
        config.setUsername(username);
        return this;
    }

    private JmxConnectionConfigBuilder withSunCacao(boolean sunCacao) {
        config.setSunCacao(sunCacao);
        return this;
    }

    public JmxConnectionConfigBuilder withUrl(String url) {
        config.setUrl(url);
        return this;
    }

    public JmxConnectionConfigBuilder withFactory(String factory) {
        config.setFactory(factory);
        return this;
    }

    private JmxConnectionConfigBuilder withPort(String port) {
        config.setPort(port);
        return this;
    }

    public JmxConnectionConfig build() {
        return config;
    }
}
