/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
