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

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;

import org.opennms.netmgt.jmx.connection.JmxServerConnectionException;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.opennms.netmgt.jmx.connection.JmxServerConnector;

/**
 * This {@link JmxServerConnector} does not connect to a JMX server,
 * it uses the PlatformMbeanServer of the current running JVM instead.
 *
 * @see java.lang.management.ManagementFactory#getPlatformMBeanServer()
 */
public class PlatformMBeanServerConnector implements JmxServerConnector {
    @Override
    public JmxServerConnectionWrapper createConnection(final InetAddress ipAddress, final Map<String, String> propertiesMap) throws JmxServerConnectionException {
        return createConnection();
    }

    public JmxServerConnectionWrapper createConnection() {
        final MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        final JmxServerConnectionWrapper jmxConnectionWrapper = new JmxServerConnectionWrapper() {
            @Override
            public MBeanServerConnection getMBeanServerConnection() {
                return platformMBeanServer;
            }

            @Override
            public void close() {

            }
        };

        return jmxConnectionWrapper;
    }
}
