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

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
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
class DefaultJmxConnector implements JmxServerConnector {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultJmxConnector.class);

    @Override
    public JmxServerConnectionWrapper createConnection(final InetAddress ipAddress, final Map<String, String> propertiesMap) throws JmxServerConnectionException {
        try {
            final String factory = ParameterMap.getKeyedString(propertiesMap, "factory", "STANDARD");
            final String port = ParameterMap.getKeyedString(propertiesMap, "port", "1099");
            final String protocol = ParameterMap.getKeyedString(propertiesMap, "protocol", "rmi");
            final String urlPath = ParameterMap.getKeyedString(propertiesMap, "urlPath",  "/jmxrmi");
            final String rmiServerPort = ParameterMap.getKeyedString(propertiesMap, "rmiServerport",  "45444");
            final String remoteJMX = ParameterMap.getKeyedString(propertiesMap, "remoteJMX",  "false");
            

            // If remote JMX access is enabled, this will return a non-null value
            String jmxPort = System.getProperty(JMX_PORT_SYSTEM_PROPERTY);

            if (
                ipAddress != null && 
                // If we're trying to create a connection to a localhost address...
                ipAddress.isLoopbackAddress() &&
                // port should never be null but let's check anyway 
                port != null && 
                (
                    // If the port matches the port of the current JVM...
                    port.equals(jmxPort) ||
                    // Or if remote JMX RMI is disabled and we're attempting to connect
                    // to the default OpenNMS JMX port...
                    (jmxPort == null && DEFAULT_OPENNMS_JMX_PORT.equals(port))
                )
            ) {
                // ...then use the {@link PlatformMBeanServerConnector} to connect to 
                // this JVM's MBeanServer directly.
                return new PlatformMBeanServerConnector().createConnection(ipAddress, propertiesMap);
            }
            JMXServiceURL url = null;
            
            if(remoteJMX.equalsIgnoreCase("true")){
            	url = new JMXServiceURL("service:jmx:" + protocol + ":" + InetAddressUtils.toUrlIpAddress(ipAddress) + ":" + rmiServerPort + "://jndi/"+ protocol +"://" + InetAddressUtils.toUrlIpAddress(ipAddress) + ":" + port + urlPath);
            }
            else{
            	url = new JMXServiceURL("service:jmx:" + protocol + ":///jndi/"+protocol+"://" + InetAddressUtils.toUrlIpAddress(ipAddress) + ":" + port + urlPath);
            }
             	
            LOG.debug("JMX: {} - {}", factory, url);

            final Map<String,String[]> env = new HashMap<>();

            // use credentials?
            if ("PASSWORD-CLEAR".equals(factory)) {
                final String username = propertiesMap.get("username");
                final String password = propertiesMap.get("password");

                // Provide the credentials required by the server to successfully
                // perform user authentication
                final String[] credentials = new String[]{username, password};
                env.put("jmx.remote.credentials", credentials);
            }

            // Connect a JSR 160 JMXConnector to the server side
            final JMXConnector connector = JMXConnectorFactory.connect(url, env);

            try {
                connector.connect(env);
            } catch (SecurityException x) {
                throw new JmxServerConnectionException("Security exception: bad credentials", x);
            }

            // Connect a JSR 160 JMXConnector to the server side
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
