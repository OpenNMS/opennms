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

package org.opennms.netmgt.provision.support.jmx.connectors;

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
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.opennms.netmgt.provision.support.jmx.connectors.IsolatingClassLoader.InvalidContextClassLoaderException;
import org.opennms.netmgt.provision.support.protocol.jmx.MBeanServerProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JBossConnectionFactory class handles the creation of a connection to the 
 * remote JBoss server.  The connections can be either RMI or HTTP based.  RMI is
 * more efficient but doesn't work with firewalls.  The HTTP connection is suited 
 * for that.  Before attempting to use the HTTP connector, you need to make sure that
 * the invoker-suffix is properly set.  It must match the InvokerURLSuffix value in 
 * the jboss-service.xml found in the 
 * <jboss-home>/server/default/deploy/http-invoker/META-INF directory
 * 
 * TODO: Merge this code with {@link org.opennms.netmgt.jmx.impl.connection.connectors.JBossMBeanServerConnector}.
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 */
public abstract class JBossConnectionFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(JBossConnectionFactory.class);
    static String[] packages = {"org.jboss.naming.*", "org.jboss.interfaces.*"};

    /* (non-Javadoc)
     * @see org.opennms.netmgt.utils.jmx.connectors.ConnectionFactory#getMBeanServer()
     */
    /**
     * <p>getMBeanServerConnection</p>
     *
     * @param propertiesMap a {@link java.util.Map} object.
     * @param address a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.provision.support.jmx.connectors.JBossConnectionWrapper} object.
     */
    public static JmxServerConnectionWrapper getMBeanServerConnection(Map<String,Object> propertiesMap, InetAddress address) {
        
        JBossConnectionWrapper wrapper = null;
        //IsolatingClassLoader   icl     = null;
        ClassLoader icl = null;
        final ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
                
        String connectionType = ParameterMap.getKeyedString(propertiesMap, "factory", "RMI");
        String timeout        = ParameterMap.getKeyedString(propertiesMap, "timeout", "3000");
        String jbossVersion   = ParameterMap.getKeyedString(propertiesMap, "version", "4");
        String port           = ParameterMap.getKeyedString(propertiesMap, "port",    "1099");

        
        if (connectionType == null) {
            return null;
        }

        if (jbossVersion == null || jbossVersion.startsWith("4")) {
            icl = createIsolatingClassloader(originalLoader, new File(System.getProperty("opennms.home") + "/lib/jboss/jbossall-client.jar"));
        } else if (jbossVersion.startsWith("3")){
            icl = createIsolatingClassloader(originalLoader, new File(System.getProperty("opennms.home") + "/lib/jboss/jbossall-client32.jar"));
        }

        if (icl == null) {
            return null;
        }
        
        Thread.currentThread().setContextClassLoader(icl);
        
        try {
            if (connectionType.equals("RMI")) {
                InitialContext ctx = null;

                try {

                    Hashtable<String, String> props = new Hashtable<String, String>();

                    //"org.jboss.naming.NamingContextFactory"
                    props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
                    props.put(Context.PROVIDER_URL, "jnp://" + InetAddressUtils.toUrlIpAddress(address) + ":" + port);
                    props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
                    props.put("jnp.sotimeout", timeout);

                    try {
                        ctx = new InitialContext(props);
                        Object rmiAdaptor = ctx.lookup("jmx/rmi/RMIAdaptor");
                        wrapper = new JBossConnectionWrapper(MBeanServerProxy.buildServerProxy(rmiAdaptor));
                    } catch (final Throwable t) {
                        LOG.debug("Unable to connect to JBOSS", t);
                    }
                } finally {
                    try {
                        if (ctx != null) {
                            ctx.close();
                        }
                    } catch (Throwable e1) {
                        // ignore
                    }
                }
            } else if (connectionType.equals("HTTP")) {
                InitialContext ctx = null;
                // String invokerSuffix = null;

                try {

                    Hashtable<String, String> props = new Hashtable<String, String>();
                    props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.HttpNamingContextFactory");
                    props.put(Context.PROVIDER_URL, "http://" + InetAddressUtils.toUrlIpAddress(address) + ":" + port + "/invoker/JNDIFactory");
                    props.put("jnp.sotimeout", timeout);

                    ctx = new InitialContext(props);

                    Object rmiAdaptor = ctx.lookup("jmx/rmi/RMIAdaptor");
                    wrapper = new JBossConnectionWrapper(MBeanServerProxy.buildServerProxy(rmiAdaptor));

                } catch (Throwable e) {
                    //log.debug("JBossConnectionFactory - unable to get MBeanServer using HTTP on " + InetAddressUtils.str(address) + invokerSuffix);
                } finally {
                    try {
                        if (ctx != null) {
                            ctx.close();
                        }
                    } catch (NamingException e1) {
                        //log.debug("JBossConnectionFactory error closing initial context");
                    }
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(originalLoader);
        }
        return wrapper;
    }

    private static ClassLoader createIsolatingClassloader(
            final ClassLoader originalLoader, final File clientJar) {
        ClassLoader icl;

        final PrivilegedAction<ClassLoader> p = new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                try {
                    return new IsolatingClassLoader("jboss", new URL[] { clientJar.toURI().toURL() }, originalLoader, packages, true);
                } catch (MalformedURLException e) {
                } catch (InvalidContextClassLoaderException e) {
                }
                return null;
            }

        };
        icl = AccessController.doPrivileged(p);
        return icl;
    }
}
