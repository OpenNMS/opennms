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

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.io.IOUtils;
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
        JMXConnector connector = null;
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
            connector = JMXConnectorFactory.newJMXConnector(url, env);
            try {
                connector.connect(env);
            } catch (SecurityException x) {
                IOUtils.closeQuietly(connector);
                throw new JmxServerConnectionException("Security exception: bad credentials", x);
            }

            // Wrap Connection
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            JmxServerConnectionWrapper connectionWrapper = new DefaultConnectionWrapper(connector, connection);
            return connectionWrapper;
        } catch (IOException e) {
            IOUtils.closeQuietly(connector);
            throw new JmxServerConnectionException(e);
        }
    }
}
