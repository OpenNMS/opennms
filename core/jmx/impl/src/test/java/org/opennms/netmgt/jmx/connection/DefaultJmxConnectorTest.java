/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
