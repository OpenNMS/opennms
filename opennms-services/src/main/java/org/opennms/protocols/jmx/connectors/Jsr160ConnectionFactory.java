/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.jmx.connectors;

import java.net.InetAddress;
import java.util.*;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import mx4j.tools.remote.*;


/*
 * This class creates a connection to the remote server. There are many options to using this
 * class.  BUT THEY ARE NOT WORKING YET....
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class Jsr160ConnectionFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(Jsr160ConnectionFactory.class);
    
    public static Jsr160ConnectionWrapper getMBeanServerConnection(Map<String, ?> propertiesMap, InetAddress address) {
        Jsr160ConnectionWrapper connectionWrapper = null;
        JMXServiceURL url = null;
        
        
        String factory =  ParameterMap.getKeyedString( propertiesMap, "factory", "STANDARD");
        int    port =     ParameterMap.getKeyedInteger(propertiesMap, "port",     1099);
        String protocol = ParameterMap.getKeyedString( propertiesMap, "protocol", "rmi");
        String urlPath =  ParameterMap.getKeyedString( propertiesMap, "urlPath",  "/jmxrmi");
        
        LOG.debug("JMX: {} - service:{}//{}:{}{}", factory, protocol, InetAddressUtils.str(address), port, urlPath);

        if (factory == null || factory.equals("STANDARD")) {
            try {
                
                url = new JMXServiceURL("service:jmx:" + protocol + ":///jndi/"+protocol+"://" + InetAddressUtils.str(address) + ":" + port + urlPath);
                
                // Connect a JSR 160 JMXConnector to the server side
                JMXConnector connector = JMXConnectorFactory.connect(url);
                MBeanServerConnection connection = connector.getMBeanServerConnection();
                
                connectionWrapper = new Jsr160ConnectionWrapper(connector, connection);
            } catch(Throwable e) {
            	LOG.warn("Unable to get MBeanServerConnection: {}", url);
            }
        }
        else if (factory.equals("PASSWORD-CLEAR")) {
            try {
                
                String username   = ParameterMap.getKeyedString(propertiesMap, "username", null);
                String password   = ParameterMap.getKeyedString(propertiesMap, "password", null);
                
                Map<String,String[]> env = new HashMap<String,String[]>();
                
                // Provide the credentials required by the server to successfully
                // perform user authentication
                //
                String[] credentials = new String[] { username , password };
                env.put("jmx.remote.credentials", credentials);
                
                // Create an RMI connector client and
                // connect it to the RMI connector server
                //
                url = new JMXServiceURL("service:jmx:" + protocol + ":///jndi/"+protocol+"://" + InetAddressUtils.str(address) + ":" + port + urlPath);
                
                // Connect a JSR 160 JMXConnector to the server side
                JMXConnector connector = JMXConnectorFactory.newJMXConnector(url, null);
                
                // Connect and invoke an operation on the remote MBeanServer
                try
                {
                    connector.connect(env);
                }
                catch (SecurityException x)
                {
                    // Uh-oh ! Bad credentials 
                    LOG.error("Security exception: bad credentials");
                    throw x;
                }

                MBeanServerConnection connection = connector.getMBeanServerConnection();

                connectionWrapper = new Jsr160ConnectionWrapper(connector, connection);
                
            } catch(Throwable e) {
                LOG.error("Unable to get MBeanServerConnection: {}", url, e);
            }
        }
        /*
        else if (factory.equals("PASSWORD-OBFUSCATED")) {
            try {
                
                String username   = ParameterMap.getKeyedString(propertiesMap, "username", null);
                String password   = ParameterMap.getKeyedString(propertiesMap, "password", null);
                
                HashMap env = new HashMap();
                
                // Provide the credentials required by the server to successfully
                // perform user authentication
                //
                String[] credentials = new String[] { username , PasswordAuthenticator.obfuscatePassword(password) };
                env.put("jmx.remote.credentials", credentials);
                
                // Create an RMI connector client and
                // connect it to the RMI connector server
                //
                JMXServiceURL url = new JMXServiceURL(protocol, InetAddressUtils.str(address), port, urlPath);
                
                // Connect a JSR 160 JMXConnector to the server side
                JMXConnector connector = JMXConnectorFactory.newJMXConnector(url, null);
                
                // Connect and invoke an operation on the remote MBeanServer
                try
                {
                    connector.connect(env);
                }
                catch (SecurityException x)
                {
                    // Uh-oh ! Bad credentials 
                    log.error("Security exception: bad credentials");
                    throw x;
                }

                MBeanServerConnection connection = connector.getMBeanServerConnection();

                connectionWrapper = new Jsr160ConnectionWrapper(connector, connection);
                
            } catch(Throwable e) {
                e.fillInStackTrace();
                log.error("Unable to get MBeanServerConnection", e);
            }
        }
        
        else if (factory.equals("SSL")) {
            try {
                
                String username   = ParameterMap.getKeyedString(propertiesMap, "username", null);
                String password   = ParameterMap.getKeyedString(propertiesMap, "password", null);
                
                HashMap env = new HashMap();
                
                // Provide the credentials required by the server to successfully
                // perform user authentication
                //
                String[] credentials = new String[] { username , PasswordAuthenticator.obfuscatePassword(password) };
                env.put("jmx.remote.credentials", credentials);
                
                // Create an RMI connector client and
                // connect it to the RMI connector server
                //
                JMXServiceURL url = new JMXServiceURL(protocol, InetAddressUtils.str(address), port, urlPath);
                
                // Connect a JSR 160 JMXConnector to the server side
                JMXConnector connector = JMXConnectorFactory.newJMXConnector(url, null);
                
                // Connect and invoke an operation on the remote MBeanServer
                try
                {
                    connector.connect(env);
                }
                catch (SecurityException x)
                {
                    // Uh-oh ! Bad credentials 
                    log.error("Security exception: bad credentials");
                    throw x;
                }

                MBeanServerConnection connection = connector.getMBeanServerConnection();

                connectionWrapper = new Jsr160ConnectionWrapper(connector, connection);
                
            } catch(Throwable e) {
                e.fillInStackTrace();
                log.error("Unable to get MBeanServerConnection", e);
            }
        }
        */
        return connectionWrapper;
    }    
}
