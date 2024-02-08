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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.jmx.connection.test.ClientProvider;
import org.opennms.netmgt.jmx.impl.connection.connectors.DefaultConnectionManager;

public class DefaultJmxConnectorTest {

    private InetAddress localhost = InetAddressUtils.getLocalHostAddress();

    /**
     * Verify that the connector is closed when an exception is throws on connect.
     *
     * @throws IOException test
     */
    @Test
    public void canCloseConnectorOnConnectException() throws IOException {
        // Mock the connector and register it with our client provider
        JMXConnector connector = mock(JMXConnector.class);
        ClientProvider.setConnector(connector);

        // This property is set so that the connector factory can find our ClientProvider class
        System.setProperty(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "org.opennms.netmgt.jmx.connection");
        try {
            // Throw an exception on connect
            when(connector.getMBeanServerConnection()).thenThrow(new IOException("Cannot connect"));

            // Try to connect
            try {
                connect();
                fail("Connect should throw an exception.");
            } catch (JmxServerConnectionException e) {
                // pass
            }

            // Verify that the connector was closed
            verify(connector, times(1)).close();
        } finally {
            System.clearProperty(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES);
        }
    }

    private JmxServerConnectionWrapper connect() throws JmxServerConnectionException {
        final Map<String,String> properties = new HashMap<>();
        properties.put("url", "service:jmx:test://oops");
        DefaultConnectionManager defaultConnectionManager = new DefaultConnectionManager();
        return defaultConnectionManager.connect(JmxConnectors.jsr160, localhost, properties, null);
    }
}
