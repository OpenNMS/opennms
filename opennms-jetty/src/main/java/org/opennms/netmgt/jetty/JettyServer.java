/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jetty;

import java.io.File;
import java.net.InetAddress;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.ajp.Ajp13SocketConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;

/**
 * Implements Web Application within OpenNMS as a Service Daemon.
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class JettyServer extends AbstractServiceDaemon {
    
    int m_port = 8080;

    private Server m_server;
    
    /**
     * <p>Constructor for JettyServer.</p>
     */
    protected JettyServer() {
        super("OpenNMS.JettyServer");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        Properties p = System.getProperties();
        
        File homeDir = new File(p.getProperty("opennms.home"));
        File webappsDir = new File(homeDir, "jetty-webapps");

        m_server = new Server();
        Connector connector = new SelectChannelConnector();
        Integer port = Integer.getInteger("org.opennms.netmgt.jetty.port", m_port);
        connector.setPort(port);

        String host = System.getProperty("org.opennms.netmgt.jetty.host");
        if (host != null) {
            connector.setHost(host);
        }
        
        Integer requestHeaderSize = Integer.getInteger("org.opennms.netmgt.jetty.requestHeaderSize");
        if(requestHeaderSize != null) {
            connector.setRequestHeaderSize(requestHeaderSize);
        }

        m_server.addConnector(connector);

        Integer ajp_port = Integer.getInteger("org.opennms.netmgt.jetty.ajp-port");
        if (ajp_port != null) {
            Ajp13SocketConnector ajpConnector = new Ajp13SocketConnector();
            ajpConnector.setPort(ajp_port);
            // Apache AJP connector freaks out with anything larger
            ajpConnector.setRequestHeaderSize(8096);
            m_server.addConnector(ajpConnector);
        }
        
        Integer https_port = Integer.getInteger("org.opennms.netmgt.jetty.https-port");
        if (https_port != null) {
            
            String keyStorePath = System.getProperty("org.opennms.netmgt.jetty.https-keystore", homeDir+File.separator+"etc"+File.separator+"examples"+File.separator+"jetty.keystore");
            String keyStorePassword = System.getProperty("org.opennms.netmgt.jetty.https-keystorepassword", "changeit");
            String keyManagerPassword = System.getProperty("org.opennms.netmgt.jetty.https-keypassword", "changeit");
            String certificateAlias = System.getProperty("org.opennms.netmgt.jetty.https-cert-alias", null);

            SslContextFactory contextFactory = new SslContextFactory(keyStorePath);
            contextFactory.setKeyStorePassword(keyStorePassword);
            contextFactory.setKeyManagerPassword(keyManagerPassword);
            if (certificateAlias != null && !"".equals(certificateAlias.trim())) {
                contextFactory.setCertAlias(certificateAlias);
            }

        	excludeCipherSuites(contextFactory, https_port);

            SslSocketConnector sslConnector = new SslSocketConnector(contextFactory);
            sslConnector.setPort(https_port);

        	String httpsHost = System.getProperty("org.opennms.netmgt.jetty.https-host");
    		if (httpsHost != null) {
    			sslConnector.setHost(httpsHost);
    		}
    		m_server.addConnector(sslConnector);
        }
        
        HandlerCollection handlers = new HandlerCollection();

        if (webappsDir.exists()) {
            File rootDir = null;
            for (File file: webappsDir.listFiles()) {
                if (file.isDirectory()) {
                    String contextPath;
                    if ("ROOT".equals(file.getName())) {
                        // Defer this to last to avoid nested context order problems
                        rootDir = file;
                        continue;
                    } else {
                        contextPath = "/" + file.getName();
                    }
                    addContext(handlers, file, contextPath);
                }
            }
            if (rootDir != null) {
                // If we deferred a ROOT context, handle that now
                addContext(handlers, rootDir, "/");
            }
        }

        m_server.setHandler(handlers);
        m_server.setStopAtShutdown(true);
    }

    /**
     * <p>addContext</p>
     *
     * @param handlers a {@link org.eclipse.jetty.server.handler.HandlerCollection} object.
     * @param name a {@link java.io.File} object.
     * @param contextPath a {@link java.lang.String} object.
     */
    protected void addContext(HandlerCollection handlers, File name, String contextPath) {
        log().warn("adding context: " + contextPath + " -> " + name.getAbsolutePath());
        WebAppContext wac = new WebAppContext();
	/*
	 * Tell jetty to scan all of the jar files in the classpath for taglibs and other resources since
         * most of our jars are installed in ${opennms.home}/lib.  This is only required for jetty7
         * See: http://wiki.eclipse.org/Jetty/Howto/Configure_JSP
         */
	wac.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",".*/[^/]*\\.jar$");
        wac.setWar(name.getAbsolutePath());
        wac.setContextPath(contextPath);
        handlers.addHandler(wac);
    }

    /**
     * <p>excludeCipherSuites</p>
     * @param contextFactory 
     * @param https_port 
     * @param sslConnector a {@link org.eclipse.jetty.server.security.SslSocketConnector} object.
     */
    protected void excludeCipherSuites(SslContextFactory contextFactory, Integer port) {
        String[] defaultExclSuites = {
                "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_RSA_WITH_DES_CBC_SHA",
                "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
                "SSL_RSA_WITH_DES_CBC_SHA",
                "TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "TLS_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "TLS_RSA_WITH_DES_CBC_SHA"
        };
        
        String[] exclSuites;
        String exclSuitesString = System.getProperty("org.opennms.netmgt.jetty.https-exclude-cipher-suites");
        if (exclSuitesString == null) {
            log().warn("No excluded SSL/TLS cipher suites specified, using hard-coded defaults");
            exclSuites = defaultExclSuites;
        } else {
            exclSuites = exclSuitesString.split("\\s*:\\s*");
            log().warn("Excluding " + exclSuites.length + " user-specified SSL/TLS cipher suites");
        }
        
        contextFactory.setExcludeCipherSuites(exclSuites);
        
        for (String suite : exclSuites) {
            log().info("Excluded SSL/TLS cipher suite " + suite + " for connector on port " + port);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onStart() {
        try {
            m_server.start();
        } catch (Throwable e) {
            log().error("Error starting Jetty Server", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onStop() {
        try {
            m_server.stop();
        } catch (Throwable e) {
            log().error("Error stopping Jetty Server", e);
        }
    }
    
}
