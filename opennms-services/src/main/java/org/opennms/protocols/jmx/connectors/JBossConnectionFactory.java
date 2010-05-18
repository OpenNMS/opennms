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

package org.opennms.protocols.jmx.connectors;

import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.jmx.MBeanServerProxy;
import org.opennms.protocols.jmx.connectors.IsolatingClassLoader.InvalidContextClassLoaderException;

/*
 * The JBossConnectionFactory class handles the creation of a connection to the 
 * remote JBoss server.  The connections can be either RMI or HTTP based.  RMI is
 * more efficient but doesn't work with firewalls.  The HTTP connection is suited 
 * for that.  Before attempting to use the HTTP connector, you need to make sure that
 * the invoker-suffix is properly set.  It must match the InvokerURLSuffix value in 
 * the jboss-service.xml found in the 
 * <jboss-home>/server/default/deploy/http-invoker/META-INF directory
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class JBossConnectionFactory {
    
    static ThreadCategory log = ThreadCategory.getInstance(JBossConnectionFactory.class);
    static String[] packages = {"org.jboss.naming.*", "org.jboss.interfaces.*"};

    /* (non-Javadoc)
     * @see org.opennms.netmgt.utils.jmx.connectors.ConnectionFactory#getMBeanServer()
     */
    public static JBossConnectionWrapper getMBeanServerConnection(Map propertiesMap, InetAddress address) {
        
        JBossConnectionWrapper wrapper = null;
        //IsolatingClassLoader   icl     = null;
        ClassLoader icl = null;
        ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
                
        String connectionType = ParameterMap.getKeyedString(propertiesMap, "factory", "RMI");
        String timeout        = ParameterMap.getKeyedString(propertiesMap, "timeout", "3000");
        String jbossVersion   = ParameterMap.getKeyedString(propertiesMap, "version", "4");
        String port           = ParameterMap.getKeyedString(propertiesMap, "port",    "1099");

        
        if (connectionType == null) {
            log.error("factory property is not set, check the configuration files.");
            return null;
        }
        
        if (jbossVersion == null || jbossVersion.startsWith("4")) {
            try {
                icl = new IsolatingClassLoader("jboss", 
                        new URL[] {new File(System.getProperty("opennms.home") + "/lib/jboss/jbossall-client.jar").toURL()},
                        originalLoader,
                        packages,
                        true);
                       
            } catch (MalformedURLException e) {
                log.error("JBossConnectionWrapper MalformedURLException" ,e);
            } catch (InvalidContextClassLoaderException e) {
                log.error("JBossConnectionWrapper InvalidContextClassLoaderException" ,e);
            }
        } else if (jbossVersion.startsWith("3")){
            try {
                icl = new IsolatingClassLoader("jboss", 
                        new URL[] {new File(System.getProperty("opennms.home") + "/lib/jboss/jbossall-client32.jar").toURL()},
                        originalLoader,
                        packages,
                        true);
            } catch (MalformedURLException e) {
                log.error("JBossConnectionWrapper MalformedURLException" ,e);
            } catch (InvalidContextClassLoaderException e) {
                log.error("JBossConnectionWrapper InvalidContextClassLoaderException" ,e);
            }
        }
        
        if (icl == null) {
            return null;
        }
        
        Thread.currentThread().setContextClassLoader(icl);
        
        if (connectionType.equals("RMI")) {
            InitialContext  ctx  = null;

            try {
                
                Hashtable<String, String> props = new Hashtable<String, String>();
                props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.NamingContextFactory");
                props.put(Context.PROVIDER_URL,            "jnp://" + address.getHostAddress() + ":" + port);
                props.put(Context.URL_PKG_PREFIXES,        "org.jboss.naming:org.jnp.interfaces" );
                props.put("jnp.sotimeout",                 timeout );
                
                ctx = new InitialContext(props);
                
                Object rmiAdaptor = ctx.lookup("jmx/rmi/RMIAdaptor");
                wrapper = new JBossConnectionWrapper(MBeanServerProxy.buildServerProxy(rmiAdaptor));
     
            } catch (Throwable e) {
                 log.debug("JBossConnectionFactory - unable to get MBeanServer using RMI on " + address.getHostAddress() + ":" + port);
            } finally {
                try {
                    if (ctx != null) {
                       ctx.close();
                    }
                } catch (Throwable e1) {
                    log.debug("JBossConnectionFactory error closing initial context");
                }
            }
        }
        else if (connectionType.equals("HTTP")) {
            InitialContext ctx  = null;
            String invokerSuffix = null;

            try {
                
                Hashtable<String, String> props = new Hashtable<String, String>();
                props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.HttpNamingContextFactory");
                props.put(Context.PROVIDER_URL,            "http://" + address.getHostAddress() + ":" + port + "/invoker/JNDIFactory");
                props.put("jnp.sotimeout",                 timeout );
                
                ctx = new InitialContext(props);
                
                Object rmiAdaptor = ctx.lookup("jmx/rmi/RMIAdaptor");
                wrapper = new JBossConnectionWrapper(MBeanServerProxy.buildServerProxy(rmiAdaptor));
                
            } catch (Exception e) {
                log.debug("JBossConnectionFactory - unable to get MBeanServer using HTTP on " + address.getHostAddress() + invokerSuffix);
           } finally {
                try {
                    if (ctx != null)
                       ctx.close();
                } catch (NamingException e1) {
                    log.debug("JBossConnectionFactory error closing initial context");
                }
            }
        }
        Thread.currentThread().setContextClassLoader(originalLoader);
        return wrapper;
    }
}
