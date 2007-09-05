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
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
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
        m_server.addConnector(connector);

        HandlerCollection handlers = new HandlerCollection();
        
        for (File entry: webappsDir.listFiles()) {
        	if (entry.isDirectory()) {
        		log().warn("name = " + entry.getName());
        		WebAppContext wac = new WebAppContext();
        		wac.setWar(entry.getAbsolutePath());
        		wac.setContextPath("/" + entry.getName());
        		handlers.addHandler(wac);
        		
        		try {
        			ServiceRegistrationStrategy srs = ServiceRegistrationFactory.getStrategy();
                	String host = InetAddress.getLocalHost().getHostName().replace(".local", "").replace(".", "-");
                	Hashtable<String, String> properties = new Hashtable<String, String>();
                	properties.put("path", "/" + entry.getName());
                	srs.initialize("HTTP", entry.getName() + "-" + host, port, properties);
                	services.put(entry.getName(), srs);
        		} catch (Exception e) {
        			log().warn("unable to get a DNS-SD object for context '" + entry.getName() + "'", e);
        		}
        	}
        }

        m_server.setHandler(handlers);
        m_server.setStopAtShutdown(true);
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
