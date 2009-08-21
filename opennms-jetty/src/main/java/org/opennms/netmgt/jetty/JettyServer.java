/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.jetty;

import java.io.File;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Properties;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.ajp.Ajp13SocketConnector;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.serviceregistration.ServiceRegistrationFactory;
import org.opennms.serviceregistration.ServiceRegistrationStrategy;

/**
 * Implements Web Application within OpenNMS as a Service Daemon.
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class JettyServer extends AbstractServiceDaemon implements SpringServiceDaemon {
    
    int m_port = 8080;

    private Server m_server;
    private Hashtable<String,ServiceRegistrationStrategy> services = new Hashtable<String,ServiceRegistrationStrategy>();
    
    protected JettyServer() {
        super("OpenNMS.JettyServer");
    }
    
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

        m_server.addConnector(connector);

        Integer ajp_port = Integer.getInteger("org.opennms.netmgt.jetty.ajp-port");
        if (ajp_port != null) {
            connector = new Ajp13SocketConnector();
            connector.setPort(ajp_port);
            // Apache AJP connector freaks out with anything larger
            connector.setHeaderBufferSize(8096);
            m_server.addConnector(connector);
        }
        
        Integer https_port = Integer.getInteger("org.opennms.netmgt.jetty.https-port");
        if (https_port != null) {
        	SslSocketConnector sslConnector = new SslSocketConnector();
        	sslConnector.setPort(https_port);
        	excludeCipherSuites(sslConnector);
    		sslConnector.setKeystore(System.getProperty("org.opennms.netmgt.jetty.https-keystore", homeDir+File.separator+"etc"+File.separator+"examples"+File.separator+"jetty.keystore"));
    		sslConnector.setPassword(System.getProperty("org.opennms.netmgt.jetty.https-keystorepassword", "changeit"));
    		sslConnector.setKeyPassword(System.getProperty("org.opennms.netmgt.jetty.https-keypassword", "changeit"));
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
                    registerService(port, contextPath);
                }
            }
            if (rootDir != null) {
                // If we deferred a ROOT context, handle that now
                addContext(handlers, rootDir, "/");
                registerService(port, "/");
            }
        }

        m_server.setHandler(handlers);
        m_server.setStopAtShutdown(true);
    }

    protected void addContext(HandlerCollection handlers, File name, String contextPath) {
        log().warn("adding context: " + contextPath + " -> " + name.getAbsolutePath());
        WebAppContext wac = new WebAppContext();
        wac.setWar(name.getAbsolutePath());
        wac.setContextPath(contextPath);
        handlers.addHandler(wac);
    }

    protected void registerService(Integer port, String contextPath) {
        String contextName = contextPath.replace("/", "");

        try {
            ServiceRegistrationStrategy srs = ServiceRegistrationFactory.getStrategy();
            String host = InetAddress.getLocalHost().getHostName().replace(".local", "").replace(".", "-");
            Hashtable<String, String> properties = new Hashtable<String, String>();
            properties.put("path", contextPath);
            
            srs.initialize("HTTP", contextName + "-" + host, port, properties);
            services.put(contextName, srs);
        } catch (Exception e) {
            log().warn("unable to get a DNS-SD object for context '" + contextPath + "'", e);
        }
    }

    protected void excludeCipherSuites(SslSocketConnector sslConnector) {
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
        
        sslConnector.setExcludeCipherSuites(exclSuites);
        for (String suite : exclSuites) {
            log().info("Excluded SSL/TLS cipher suite " + suite + " for connector on port " + sslConnector.getPort());
        }
    }

    @Override
    protected void onStart() {
        try {
            m_server.start();
        } catch (Exception e) {
            log().error("Error starting Jetty Server", e);
        }
        for (String key: services.keySet()) {
        	ServiceRegistrationStrategy srs = services.get(key);
        	if (srs != null) {
            	try {
            		srs.register();
            	} catch (Exception e) {
            		log().warn("unable to register a DNS-SD object for context '" + key + "'", e);
            	}
        	}
        }
    }

    @Override
    protected void onStop() {
        for (String key: services.keySet()) {
        	ServiceRegistrationStrategy srs = services.get(key);
        	if (srs != null) {
            	try {
            		srs.unregister();
            	} catch (Exception e) {
            		log().warn("unable to unregister a DNS-SD object for context '" + key + "'", e);
            	}
        	}
        }
        try {
            m_server.stop();
        } catch (Exception e) {
            log().error("Error stopping Jetty Server", e);
        }
    }
    
}
