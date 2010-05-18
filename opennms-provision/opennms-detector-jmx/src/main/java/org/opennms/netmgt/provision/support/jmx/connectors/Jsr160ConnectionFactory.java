//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.                                                            
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//  
//For more information contact: 
// OpenNMS Licensing       <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.netmgt.provision.support.jmx.connectors;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;

//import mx4j.tools.remote.*;


/*
 * This class creates a connection to the remote server. There are many options to using this
 * class.  BUT THEY ARE NOT WORKING YET....
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class Jsr160ConnectionFactory {
    
    static ThreadCategory log = ThreadCategory.getInstance(Jsr160ConnectionFactory.class);

    @SuppressWarnings("unchecked")
    public static Jsr160ConnectionWrapper getMBeanServerConnection(Map propertiesMap, InetAddress address) {
        String factory  = ParameterMap.getKeyedString( propertiesMap, "factory", "STANDARD");
        int    port     = ParameterMap.getKeyedInteger(propertiesMap, "port",     1099);
        String protocol = ParameterMap.getKeyedString( propertiesMap, "protocol", "rmi");
        String urlPath  = ParameterMap.getKeyedString( propertiesMap, "urlPath",  "/jmxrmi");
        String username = ParameterMap.getKeyedString(propertiesMap, "username", null);
        String password = ParameterMap.getKeyedString(propertiesMap, "password", null);
        
        return getWrapper(address, factory, port, protocol, urlPath, username, password);
    }

    private static Jsr160ConnectionWrapper getWrapper(InetAddress address, String factory, int port,
            String protocol, String urlPath, String username, String password) {
        Jsr160ConnectionWrapper connectionWrapper = null;
        JMXServiceURL url = null;
        
        log.debug("JMX: " + factory + " - service:" + protocol + "//" + address.getHostAddress() + ":" + port + urlPath);

        if (factory == null || factory.equals("STANDARD")) {
            try {
                
                url = new JMXServiceURL("service:jmx:" + protocol + ":///jndi/"+protocol+"://" + address.getHostAddress() + ":" + port + urlPath);
                
                // Connect a JSR 160 JMXConnector to the server side
                JMXConnector connector = JMXConnectorFactory.connect(url);
                MBeanServerConnection connection = connector.getMBeanServerConnection();
                
                connectionWrapper = new Jsr160ConnectionWrapper(connector, connection);
            } catch(MalformedURLException e) {
                log.warn("URL was malformed: " + url, e);
            } catch (IOException e) {
                log.warn("An I/O exception occurred: " +url, e);
            }
        }
        else if (factory.equals("PASSWORD-CLEAR")) {
            try {
                
                HashMap<String, String[]> env = new HashMap<String, String[]>();
                
                // Provide the credentials required by the server to successfully
                // perform user authentication
                //
                String[] credentials = new String[] { username , password };
                env.put("jmx.remote.credentials", credentials);
                
                // Create an RMI connector client and
                // connect it to the RMI connector server
                //
                url = new JMXServiceURL("service:jmx:" + protocol + ":///jndi/"+protocol+"://" + address.getHostAddress() + ":" + port + urlPath);
                
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
                
            } catch(Exception e) {
                log.error("Unable to get MBeanServerConnection: " + url, e);
            }
        }
        /*
        else if (factory.equals("PASSWORD-OBFUSCATED")) {
            try {
                
                HashMap env = new HashMap();
                
                // Provide the credentials required by the server to successfully
                // perform user authentication
                //
                String[] credentials = new String[] { username , PasswordAuthenticator.obfuscatePassword(password) };
                env.put("jmx.remote.credentials", credentials);
                
                // Create an RMI connector client and
                // connect it to the RMI connector server
                //
                JMXServiceURL url = new JMXServiceURL(protocol, address.getHostAddress(), port, urlPath);
                
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
                
            } catch(Exception e) {
                e.fillInStackTrace();
                log.error("Unable to get MBeanServerConnection", e);
            }
        }
        
        else if (factory.equals("SSL")) {
            try {
                
                HashMap env = new HashMap();
                
                // Provide the credentials required by the server to successfully
                // perform user authentication
                //
                String[] credentials = new String[] { username , PasswordAuthenticator.obfuscatePassword(password) };
                env.put("jmx.remote.credentials", credentials);
                
                // Create an RMI connector client and
                // connect it to the RMI connector server
                //
                JMXServiceURL url = new JMXServiceURL(protocol, address.getHostAddress(), port, urlPath);
                
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
                
            } catch(Exception e) {
                e.fillInStackTrace();
                log.error("Unable to get MBeanServerConnection", e);
            }
        }
        */
        return connectionWrapper;
    }    
}
