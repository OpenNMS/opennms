/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jmx.impl.connection.connectors;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.opennms.netmgt.jmx.connection.JmxConnectionConfig;
import org.opennms.netmgt.jmx.connection.JmxConnectionConfigBuilder;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionException;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.opennms.netmgt.jmx.connection.JmxServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the default "jsr160" connection logic. If attempting to connect to a localhost
 * address on the default OpenNMS JMX port, it will also bypass using a socket connection
 * and connect directly to the JVM's MBeanServer.
 */
public class DefaultJmxConnector implements JmxServerConnector {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultJmxConnector.class);

    @Override
    public JmxServerConnectionWrapper createConnection(final InetAddress ipAddress, final Map<String, String> propertiesMap) throws JmxServerConnectionException {
        JmxConnectionConfig config = JmxConnectionConfigBuilder.buildFrom(ipAddress, propertiesMap).build();
        return createConnection(config);
    }

    public JmxServerConnectionWrapper createConnection(JmxConnectionConfig config) throws JmxServerConnectionException {
        try {
            // If we're trying to create a connection to a localhost address...
            if (config.isLocalConnection()) {
                // ...then use the {@link PlatformMBeanServerConnector} to connect to
                // this JVM's MBeanServer directly.
                return new PlatformMBeanServerConnector().createConnection();
            }

            // Create URL
            final String urlString = config.getUrl();
            final JMXServiceURL url = new JMXServiceURL(urlString);
            LOG.debug("JMX: {} - {}", config.getFactory(), url);

            // Apply password strategy
            final Map<String,Object> env = new HashMap<>();
            config.getPasswordStategy().apply(env, config);

            // Create connection and connect
            final JMXConnector connector = JMXConnectorFactory.newJMXConnector(url, env);
            try {
                connector.connect(env);
            } catch (SecurityException x) {
                throw new JmxServerConnectionException("Security exception: bad credentials", x);
            }

            // Wrap Connection
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            JmxServerConnectionWrapper connectionWrapper = new DefaultConnectionWrapper(connector, connection);
            return connectionWrapper;
        } catch (MalformedURLException e) {
            throw new JmxServerConnectionException(e);
        } catch (IOException e) {
            throw new JmxServerConnectionException(e);
        }
    }
}
