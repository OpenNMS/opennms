/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc. All rights
 * reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included
 * code and modified
 * code that was published under the GNU General Public License. Copyrights
 * for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2003 Jan 31: Cleaned up some unused imports.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
 * reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.vmmgr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.service.types.InvokeAtType;

/**
 * <p>
 * The Manager is reponsible for launching/starting all services in the VM
 * that it is started for. The Manager operates in two modes, normal and
 * server
 * </p>
 * <p>
 * normal mode: In the normal mode, the Manager starts all services configured
 * for its VM in the service-configuration.xml and starts listening for
 * control events on the 'control-broadcast' JMS topic for stop control
 * messages for itself
 * </p>
 * <p>
 * server mode: In the server mode, the Manager starts up and listens on the
 * 'control-broadcast' JMS topic for 'start' control messages for services in
 * its VM and a stop control messge for itself. When a start for a service is
 * received, it launches only that service and sends a successful 'running' or
 * an 'error' response to the Controller
 * </p>
 * <p>
 * <strong>Note: </strong>The Manager is NOT intelligent - if it receives a
 * stop control event, it will exit - does not check to see if the services
 * its started are all stopped
 * <p>
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj</a>
 * @author <a href="http://www.opennms.org">OpenNMS.org</a>
 */
public class Starter {
    /**
     * The log4j category used to log debug messsages and statements.
     */
    private static final String LOG4J_CATEGORY = "OpenNMS.Manager";

    private void setLogPrefix() {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    public void startDaemon() {
        configureLog4j();
        
        setLogPrefix();
        
        mx4j.log.Log.redirectTo(new mx4j.log.Log4JLogger());
        
        loadGlobalProperties();
        
        setupProperties();

        MBeanServer server = MBeanServerFactory.createMBeanServer("OpenNMS");
        start(server);
    }
    
    private void configureLog4j() {
        File homeDir = new File(System.getProperty("opennms.home"));
        File etcDir = new File(homeDir, "etc");
        File daemonProperties = new File(etcDir, "log4j.properties");
        PropertyConfigurator.configureAndWatch(daemonProperties.getAbsolutePath());
    }
    
    private void setupProperties() {
        if (System.getProperty("opennms.library.jicmp") == null) {
            URL url = Starter.class.getResource(System.mapLibraryName("jicmp"));
            if (url != null) {
                log().debug("Found jicmp library at " + url.getPath());
                System.setProperty("opennms.library.jicmp", url.getPath());
            }
        }

        if (System.getProperty("opennms.library.jrrd") == null) {
            URL url = Starter.class.getResource(System.mapLibraryName("jrrd"));
            if (url != null) {
                log().debug("Found jrrd library at " + url.getPath());
                System.setProperty("opennms.library.jrrd", url.getPath());
            }
        }

        if (System.getProperty("jcifs.properties") == null) {
            URL url = Starter.class.getResource("jcifs.properties");
            if (url != null) {
                System.setProperty("jcifs.properties", url.getPath());
            }
        }

    }

    private void loadGlobalProperties() {
        File propertiesFile = getPropertiesFile();
        if (!propertiesFile.exists()) {
            // don't require the file
            return;
        }
        
        Properties props = new Properties(System.getProperties());
        InputStream fin = null;
        try {
            fin = new FileInputStream(propertiesFile);
            props.load(fin);
        } catch (IOException e) {
            System.err.println("Error trying to read properties file '" + propertiesFile + "': " + e);
            System.exit(1);
        } finally {
            closeQuietly(fin);
        }
        
        System.setProperties(props);
    }

    private File getPropertiesFile() {
        String homeDir = System.getProperty("opennms.home");
        File etcDir = new File(homeDir, "etc");
        File propertiesFile = new File(etcDir, "opennms.properties");
        return propertiesFile;
    }
    
    private void closeQuietly(InputStream in) {
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            // ignore this
        }
    }

    private void start(MBeanServer server) {
        log().debug("Beginning startup");
        
        Invoker invoker = new Invoker();
        invoker.setServer(server);
        invoker.setAtType(InvokeAtType.START);
        List<InvokerService> services = InvokerService.createServiceList(Invoker.getDefaultServiceConfigFactory().getServices());
        invoker.setServices(services);
        invoker.instantiateClasses();

        List<InvokerResult> resultInfo = invoker.invokeMethods();

        for (InvokerResult result : resultInfo) {
            if (result != null && result.getThrowable() != null) {
                Service service = result.getService();
                String name = service.getName();
                String className = service.getClassName();

                String message =
                    "An error occurred while attempting to start the \"" +
                    name + "\" service (class " + className + ").  "
                    + "Shutting down and exiting.";
                log().fatal(message, result.getThrowable());
                System.err.println(message);
                result.getThrowable().printStackTrace();

                Manager manager = new Manager();
                manager.stop();
                manager.doSystemExit();

                // Shouldn't get here
                return;
            }
        }
        
        log().debug("Startup complete");
    }
}
