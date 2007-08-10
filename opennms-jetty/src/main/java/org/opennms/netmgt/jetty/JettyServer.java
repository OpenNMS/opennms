package org.opennms.netmgt.jetty;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.daemon.SpringServiceDaemon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import org.opennms.serviceregistration.ServiceRegistrationStrategy;
import org.opennms.serviceregistration.ServiceRegistrationFactory;

public class JettyServer extends AbstractServiceDaemon implements SpringServiceDaemon {
    
	int m_port = 8080;
    private Server m_server;
    private Hashtable<String,ServiceRegistrationStrategy> services = new Hashtable<String,ServiceRegistrationStrategy>();
    
    protected JettyServer() {
        super("JettyServer");
    }
    
    public void setPort(int port) {
        m_port = port;
    }

    @Override
    protected void onInit() {
        m_server = new Server();
        Connector connector = new SelectChannelConnector();
        connector.setPort(m_port);
        m_server.addConnector(connector);

        File homeDir = new File(System.getProperty("opennms.home"));    
        File webappsDir = new File(homeDir, "jetty-webapps");

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
                	String host = InetAddress.getLocalHost().getCanonicalHostName().replace(".local.", "").replace(".", "-");
                	Hashtable<String, String> properties = new Hashtable<String, String>();
                	properties.put("path", "/" + entry.getName());
                	srs.initialize("HTTP", entry.getName() + "-" + host, m_port, properties);
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
