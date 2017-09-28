/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates a connection to the remote server. There are many options to using this
 * class.  BUT THEY ARE NOT WORKING YET....
 * 
 * TODO: Merge this code with {@link org.opennms.netmgt.jmx.impl.connection.connectors.DefaultJmxConnector}.
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 */
public abstract class Jsr160ConnectionFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(Jsr160ConnectionFactory.class);

    /**
     * <p>getMBeanServerConnection</p>
     *
     * @param propertiesMap a {@link java.util.Map} object.
     * @param address a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.provision.support.jmx.connectors.Jsr160ConnectionWrapper} object.
     * @throws MalformedURLException, IOException 
     */
    public static JmxServerConnectionWrapper getMBeanServerConnection(Map<String,?> propertiesMap, InetAddress address) throws MalformedURLException, IOException {
        String factory  = ParameterMap.getKeyedString( propertiesMap, "factory", "STANDARD");
        int    port     = ParameterMap.getKeyedInteger(propertiesMap, "port",     1099);
        String protocol = ParameterMap.getKeyedString( propertiesMap, "protocol", "rmi");
        String urlPath  = ParameterMap.getKeyedString( propertiesMap, "urlPath",  "/jmxrmi");
        String username = ParameterMap.getKeyedString(propertiesMap, "username", null);
        String password = ParameterMap.getKeyedString(propertiesMap, "password", null);
        
        Jsr160ConnectionWrapper connectionWrapper = null;
        JMXServiceURL url = null;
        
        final String hostAddress = InetAddressUtils.toUrlIpAddress(address);
        LOG.debug("JMX: {} - service:{}//{}:{}{}", factory, protocol, hostAddress, port, urlPath);

        if (factory == null || factory.equals("STANDARD")) {
                url = getUrl(address, port, protocol, urlPath);
                
                // Connect a JSR 160 JMXConnector to the server side
                JMXConnector connector = JMXConnectorFactory.connect(url);
                MBeanServerConnection connection = connector.getMBeanServerConnection();
                
                return new Jsr160ConnectionWrapper(connector, connection);
        }
        else if (factory.equals("PASSWORD-CLEAR")) {
                HashMap<String, String[]> env = new HashMap<String, String[]>();
                
                // Provide the credentials required by the server to successfully
                // perform user authentication
                //
                String[] credentials = new String[] { username , password };
                env.put("jmx.remote.credentials", credentials);
                
                // Create an RMI connector client and
                // connect it to the RMI connector server
                //
                url = getUrl(address, port, protocol, urlPath);
                
                // Connect a JSR 160 JMXConnector to the server side
                JMXConnector connector = JMXConnectorFactory.newJMXConnector(url, null);
                
                // Connect and invoke an operation on the remote MBeanServer
                connector.connect(env);

                MBeanServerConnection connection = connector.getMBeanServerConnection();

                return new Jsr160ConnectionWrapper(connector, connection);
        }
        /*
        else if (factory.equals("PASSWORD-OBFUSCATED")) {
            Map<String,String[]> env = new HashMap<String,String[]>();
            
            // Provide the credentials required by the server to successfully
            // perform user authentication
            //
            String[] credentials = new String[] { username , PasswordAuthenticator.obfuscatePassword(password) };
            env.put("jmx.remote.credentials", credentials);
            
            // Create an RMI connector client and
            // connect it to the RMI connector server
            //
            url = new JMXServiceURL(protocol, hostAddress, port, urlPath);
            
            // Connect a JSR 160 JMXConnector to the server side
            JMXConnector connector = JMXConnectorFactory.newJMXConnector(url, null);
            
            // Connect and invoke an operation on the remote MBeanServer
            connector.connect(env);

            MBeanServerConnection connection = connector.getMBeanServerConnection();

            connectionWrapper = new Jsr160ConnectionWrapper(connector, connection);
        }
        
        else if (factory.equals("SSL")) {
            Map<String,String[]> env = new HashMap<String,String[]>();
            
            // Provide the credentials required by the server to successfully
            // perform user authentication
            //
            String[] credentials = new String[] { username , PasswordAuthenticator.obfuscatePassword(password) };
            env.put("jmx.remote.credentials", credentials);
            
            // Create an RMI connector client and
            // connect it to the RMI connector server
            //
            url = new JMXServiceURL(protocol, hostAddress, port, urlPath);
            
            // Connect a JSR 160 JMXConnector to the server side
            JMXConnector connector = JMXConnectorFactory.newJMXConnector(url, null);
            
            // Connect and invoke an operation on the remote MBeanServer
            connector.connect(env);

            MBeanServerConnection connection = connector.getMBeanServerConnection();

            connectionWrapper = new Jsr160ConnectionWrapper(connector, connection);
        }
        */
        else {
            throw new IOException("Unsupported connection factory: " + factory);
        }
    }

    public static JMXServiceURL getUrl(InetAddress address, int port, String protocol, String urlPath) throws MalformedURLException {
        if (protocol.equalsIgnoreCase("jmxmp") || protocol.equalsIgnoreCase("remoting-jmx")) {

            // Create an JMXMP connector client and
            // connect it to the JMXMP connector server
            //
            return new JMXServiceURL(protocol, InetAddressUtils.toUrlIpAddress(address), port, urlPath);
        } else {
            // Fallback, building a URL for RMI
            return new JMXServiceURL("service:jmx:" + protocol + ":///jndi/" + protocol + "://" + InetAddressUtils.toUrlIpAddress(address) + ":" + port + urlPath);
        }
    }

}
