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

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.opennms.core.utils.InetAddressUtils;
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
/**
 * <p>JBossConnectionFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JBossConnectionFactory {
    
    static ThreadCategory log = ThreadCategory.getInstance(JBossConnectionFactory.class);
    static String[] packages = {"org.jboss.naming.*", "org.jboss.interfaces.*"};

    /* (non-Javadoc)
     * @see org.opennms.netmgt.utils.jmx.connectors.ConnectionFactory#getMBeanServer()
     */
    /**
     * <p>getMBeanServerConnection</p>
     *
     * @param propertiesMap a {@link java.util.Map} object.
     * @param address a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.protocols.jmx.connectors.JBossConnectionWrapper} object.
     */
    public static JBossConnectionWrapper getMBeanServerConnection(Map<String,Object> propertiesMap, InetAddress address) {
        
        JBossConnectionWrapper wrapper = null;
        //IsolatingClassLoader   icl     = null;
        ClassLoader icl = null;
        final ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();

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
                        new URL[] {new File(System.getProperty("opennms.home") + "/lib/jboss/jbossall-client.jar").toURI().toURL()},
                        originalLoader,
                        packages,
                        true);
                       
            } catch (MalformedURLException e) {
                log.error("JBossConnectionWrapper MalformedURLException" ,e);
            } catch (InvalidContextClassLoaderException e) {
                log.error("JBossConnectionWrapper InvalidContextClassLoaderException" ,e);
            }
        } else if (jbossVersion.startsWith("3")){
                PrivilegedAction<IsolatingClassLoader> action = new PrivilegedAction<IsolatingClassLoader>() {

                    @Override
                    public IsolatingClassLoader run() {
                        try {
                            return new IsolatingClassLoader(
                                "jboss", 
                                new URL[] {new File(System.getProperty("opennms.home") + "/lib/jboss/jbossall-client32.jar").toURI().toURL()},
                                originalLoader,
                                packages,
                                true
                            );
                        } catch (MalformedURLException e) {
                            log.error("JBossConnectionWrapper MalformedURLException" ,e);
                        } catch (InvalidContextClassLoaderException e) {
                            log.error("JBossConnectionWrapper InvalidContextClassLoaderException" ,e);
                        }
                        return null;
                    }
                    
                };
                AccessController.doPrivileged(action);
        }
        
        if (icl == null) {
            return null;
        }
        
        Thread.currentThread().setContextClassLoader(icl);
        
        if (connectionType.equals("RMI")) {
            InitialContext  ctx  = null;

            final String hostAddress = InetAddressUtils.str(address);
			try {
                
                Hashtable<String, String> props = new Hashtable<String, String>();
                props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.NamingContextFactory");
                props.put(Context.PROVIDER_URL,            "jnp://" + hostAddress + ":" + port);
                props.put(Context.URL_PKG_PREFIXES,        "org.jboss.naming:org.jnp.interfaces" );
                props.put("jnp.sotimeout",                 timeout );
                
                ctx = new InitialContext(props);
                
                Object rmiAdaptor = ctx.lookup("jmx/rmi/RMIAdaptor");
                wrapper = new JBossConnectionWrapper(MBeanServerProxy.buildServerProxy(rmiAdaptor));
     
            } catch (Throwable e) {
                 log.debug("JBossConnectionFactory - unable to get MBeanServer using RMI on " + hostAddress + ":" + port);
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

            final String hostAddress = InetAddressUtils.str(address);
			try {
                
                Hashtable<String, String> props = new Hashtable<String, String>();
                props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.HttpNamingContextFactory");
                props.put(Context.PROVIDER_URL,            "http://" + hostAddress + ":" + port + "/invoker/JNDIFactory");
                props.put("jnp.sotimeout",                 timeout );
                
                ctx = new InitialContext(props);
                
                Object rmiAdaptor = ctx.lookup("jmx/rmi/RMIAdaptor");
                wrapper = new JBossConnectionWrapper(MBeanServerProxy.buildServerProxy(rmiAdaptor));
                
            } catch (Throwable e) {
                log.debug("JBossConnectionFactory - unable to get MBeanServer using HTTP on " + hostAddress + invokerSuffix);
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
