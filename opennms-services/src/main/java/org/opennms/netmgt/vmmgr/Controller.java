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
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.ServiceConfigFactory;
import org.opennms.netmgt.config.service.Argument;
import org.opennms.netmgt.config.service.Invoke;
import org.opennms.netmgt.config.service.Service;

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
public class Controller {
    private static final String JMX_HTTP_ADAPTER_NAME = ":Name=HttpAdaptorMgmt";

    /**
     * Default invoker URL. This is used for getting status information from a
     * running OpenNMS instance.
     */
    public static final String DEFAULT_INVOKER_URL =
        "http://127.0.0.1:8181/invoke?objectname=OpenNMS%3AName=Manager";

    /**
     * The log4j category used to log debug messsages and statements.
     */
    private static final String LOG4J_CATEGORY = "OpenNMS.Manager";
    
    private boolean m_verbose = false;
    private String m_invokeUrl = DEFAULT_INVOKER_URL;

    public static void main(String[] argv) {
        configureLog4j();
        
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        
        Controller c = new Controller();

        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equals("-h")) {
                System.out.println("Usage: java org.opennms.netmgt.vmmgr.Controller "
                                   + "[<options>] <command>");
                System.out.println("Accepted options:");
                System.out.println("        -v              Verbose mode.");
                System.out.println("        -u <URL>        Alternate invoker URL.");
                System.out.println("");
                System.out.println("Accepted commands: start, stop, status");
                System.out.println("");
                System.out.println("The default invoker URL is: "
                        + DEFAULT_INVOKER_URL);
                System.exit(0);
            } else if (argv[i].equals("-v")) {
                c.setVerbose(true);
            } else if (argv[i].equals("-u")) {
                c.setInvokeUrl(argv[i + 1]);
                i++;
            } else if (i != (argv.length - 1)) {
                System.err.println("Invalid command-line option: \""
                        + argv[i] + "\".  Use \"-h\" option for help.");
                System.exit(1);
            } else {
                break;
            }
        }

        if (argv.length == 0) {
            System.err.println("You must specify a command.  Use \"-h\""
                               + " option for help");
            System.exit(1);
        }

        String command = argv[argv.length - 1];

        if ("start".equals(command)) {
            c.start();
        } else if ("stop".equals(command)) {
            c.stop();
        } else if ("status".equals(command)) {
            c.status();
        } else if ("check".equals(command)) {
            c.check();
        } else if ("exit".equals(command)) {
            c.exit();
        } else {
            System.err.println("Invalid command \"" + command + "\".");
            System.err.println("Use \"-h\" option for help.");
            System.exit(1);
        }
    }

    private static void configureLog4j() {
        File homeDir = new File(System.getProperty("opennms.home"));
        File etcDir = new File(homeDir, "etc");
        File controllerProperties = new File(etcDir, "log4j-controller.properties");
        PropertyConfigurator.configure(controllerProperties.getAbsolutePath());
    }

    /**
     * Start the OpenNMS daemon.  Never returns.
     */
    public void start() {
        Starter starter = new Starter();
        starter.startDaemon();
    }

    public void stop() {
        invokeOperation("stop");
    }
    
    public void status() {
        Authenticator.setDefault(getAuthenticator());

        StatusGetter statusGetter = new StatusGetter();
        statusGetter.setVerbose(isVerbose());
        
        String url = getInvokeUrl() + "&operation=status";
        try {
            statusGetter.setInvokeURL(new URL(url));
        } catch (MalformedURLException e) {
            String message = "Error creating URL object for invoke URL: '"
                + url + "': " + e;
            System.err.println(message);
            log().error(message, e);
        }

        try {
            statusGetter.queryStatus();
        } catch (Throwable t) {
            String message =  "Error invoking status command: " + t;
            System.err.println(message);
            log().error(message, t);
            System.exit(1);
        }

        int exitValue;
        switch (statusGetter.getStatus()) {
        case NOT_RUNNING:
        case CONNECTION_REFUSED:
            exitValue = 3;  // According to LSB: 3 - service not running
            break;

        case PARTIALLY_RUNNING:
            /*
             * According to LSB: reserved for application So, I say
             * 160 - partially running
             */
            exitValue = 160;
            break;

        case RUNNING:
            exitValue = 0; // everything should be good and running
            break;

        default:
            String message = "Unknown status returned from "
                + "statusGetter.getStatus(): "
                + statusGetter.getStatus();
            System.err.println(message);
            log().error(message);
            exitValue = 1;
            break;
        }

        System.exit(exitValue);
    }

    public void check() {
        try {
            DatabaseChecker checker = new DatabaseChecker();
            checker.check();
        } catch (Throwable t) {
            log().error("error invoking check command", t);
            System.err.println(t);
            System.exit(1);
        }
        System.exit(0);
    }

    public void exit() {
        invokeOperation("doSystemExit");
    }
    
    private void invokeOperation(String operation) {
        Authenticator.setDefault(getAuthenticator());

        String urlString = getInvokeUrl() + "&operation=" + operation;
        try {
            URL invoke = new URL(urlString);
            InputStream in = invoke.openStream();
            int ch;
            while ((ch = in.read()) != -1) {
                System.out.write((char) ch);
            }
            in.close();
            System.out.println("");
            System.out.flush();
            System.exit(0);
        } catch (ConnectException e) {
            log().error(e.getMessage() + " when attempting to fetch URL \""
                      + urlString + "\"");
            if (isVerbose()) {
                System.out.println(e.getMessage()
                                   + " when attempting to fetch URL \""
                                   + urlString + "\"");
            }
            System.exit(1);
        } catch (Throwable t) {
            log().error("error invoking " + operation + " operation", t);
            System.out.println("error invoking " + operation + " operation");
            t.printStackTrace();
            System.exit(1);
        }
    }

    /*
     * Create an Authenticator so that we can provide authentication, if
     * needed, when go to connect to the URL
     */
    private Authenticator getAuthenticator() {
        Service service = getConfiguredService(JMX_HTTP_ADAPTER_NAME);
        if (service == null) {
            // Didn't find the service we were looking for
            log().warn("Could not find configured service for '" + JMX_HTTP_ADAPTER_NAME + "'");
            return null;
        }

        org.opennms.netmgt.config.service.Attribute[] attribs = service.getAttribute();

        if (attribs == null) {
            // the AuthenticationMethod is not set, so no authentication
            return null;
        }

        boolean usingBasic = false;
        for (org.opennms.netmgt.config.service.Attribute attrib : attribs) {
            if (attrib.getName().equals("AuthenticationMethod")) {
                if (!attrib.getValue().getContent().equals("basic")) {
                    log().error("AuthenticationMethod is \""
                              + attrib.getValue()
                              + "\", but only \"basic\" is supported");
                    return null;
                }
                usingBasic = true;
                break;
            }
        }
            
        if (!usingBasic) {
            // AuthenticationMethod is not set to basic, so no authentication
            return null;
        }

        Invoke[] invokes = service.getInvoke();
        if (invokes == null) {
            // No username or password could be set
            return null;
        }
        
        String username = null;
        String password = null;
        for (Invoke invoke : invokes) {
            if (invoke.getMethod().equals("addAuthorization")) {
                Argument[] args = invoke.getArgument();
                if (args != null && args.length == 2
                        && args[0].getContent().equals("manager")) {
                    username = args[0].getContent();
                    password = args[1].getContent();
                    break;
                }
            }
        }
            
        if (username == null || password == null) {
            // Didn't find a username or password
            return null;
        }
            
        final String username_f = username;
        final String password_f = password;
        
        return new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username_f,
                                                  password_f.toCharArray());
            }
        };
    }

    private ServiceConfigFactory getServiceConfigFactory() {
        try {
            ServiceConfigFactory.init();
            return ServiceConfigFactory.getInstance();
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
    }

    private Service getConfiguredService(String serviceName) {
        ServiceConfigFactory sfact = getServiceConfigFactory();

        Service[] services = sfact.getServices();

        for (Service service : services) {
            if (service.getName().equals(serviceName)) {
                return service;
            }
        }
        
        return null;
    }

    private static Category log() {
        return ThreadCategory.getInstance(Category.class);
    }

    public boolean isVerbose() {
        return m_verbose;
    }

    public void setVerbose(boolean verbose) {
        m_verbose = verbose;
    }

    public String getInvokeUrl() {
        return m_invokeUrl;
    }

    public void setInvokeUrl(String invokerUrl) {
        m_invokeUrl = invokerUrl;
    }

}
