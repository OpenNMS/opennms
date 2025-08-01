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
