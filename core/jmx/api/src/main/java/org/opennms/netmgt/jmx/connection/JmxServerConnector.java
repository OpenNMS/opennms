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
package org.opennms.netmgt.jmx.connection;

import java.net.InetAddress;
import java.util.Map;

/**
 * The JmxServerConnector implements the logic on how to connect to a certain JMX Server (MBeanServer).
 */
public interface JmxServerConnector {

    public static String DEFAULT_OPENNMS_JMX_PORT = "18980";

    public static String JMX_PORT_SYSTEM_PROPERTY = "com.sun.management.jmxremote.port";

    public static enum Parameters {
        factory,
        port,
        protocol,
        sunCacao,
        timeout,
        urlPath,
        version
    }

    /**
     * <p>
     * Establishes a JMX connection ({@link javax.management.MBeanServerConnection}) to the given <code>ipAddress</code>
     * using required properties from the given <code>propertiesMap</code>.
     * <p/>
     * The created {@link javax.management.MBeanServerConnection} is wrapped by the {@link org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper}.
     *
     * @param ipAddress     The IP address to connect to.
     * @param propertiesMap Properties to use to establish the connection (e.g. timeout, user, password, etc.)
     * @return The wrapped {@link javax.management.MBeanServerConnection}. May return null, but should throw a {@link JmxServerConnectionException} instead.
     * @throws JmxServerConnectionException If a JMX connection to the given <code>ipAddress</code> could not be established.
     */
    JmxServerConnectionWrapper createConnection(final InetAddress ipAddress, final Map<String, String> propertiesMap) throws JmxServerConnectionException;
}
