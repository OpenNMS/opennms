/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.vmmgr;

import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.config.ServiceConfigFactory;
import org.opennms.netmgt.config.service.Argument;
import org.opennms.netmgt.config.service.Invoke;
import org.opennms.netmgt.config.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The Manager is responsible for launching/starting all services in the VM
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
 * its VM and a stop control message for itself. When a start for a service is
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
 */
public class Controller {
	
	private static final Logger LOG = LoggerFactory.getLogger(Controller.class);
	
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
    private static final String LOG4J_CATEGORY = "manager";
    
    /**
     * Default read timeout for HTTP requests in milliseconds.
     * The default is zero which means wait forever.
     */
    private static final int DEFAULT_HTTP_REQUEST_READ_TIMEOUT = 0;
    
    private boolean m_verbose = false;
    private String m_invokeUrl = DEFAULT_INVOKER_URL;
    private Authenticator m_authenticator;
    private int m_httpRequestReadTimeout = DEFAULT_HTTP_REQUEST_READ_TIMEOUT;
    
    /**
     * <p>Constructor for Controller.</p>
     */
    public Controller() {
        
    }

    /**
     * <p>main</p>
     *
     * @param argv an array of {@link java.lang.String} objects.
     */
    public static void main(String[] argv) {
        
        Logging.putPrefix(LOG4J_CATEGORY);
        
        Controller c = new Controller();

        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equals("-h")) {
                System.out.println("Usage: java org.opennms.netmgt.vmmgr.Controller "
                                   + "[<options>] <command>");
                System.out.println("Accepted options:");
                System.out.println("        -t <timeout>    HTTP connection timeout in seconds.  Defaults to 30.");
                System.out.println("        -u <URL>        Alternate invoker URL.");
                System.out.println("        -v              Verbose mode.");
                System.out.println("");
                System.out.println("Accepted commands: start, stop, status");
                System.out.println("");
                System.out.println("The default invoker URL is: " + DEFAULT_INVOKER_URL);
                System.exit(0);
            } else if (argv[i].equals("-t")) {
                c.setHttpRequestReadTimeout(Integer.parseInt(argv[i + 1]) * 1000);
                i++;
            } else if (argv[i].equals("-v")) {
                c.setVerbose(true);
            } else if (argv[i].equals("-u")) {
                c.setInvokeUrl(argv[i + 1]);
                i++;
            } else if (i != (argv.length - 1)) {
                System.err.println("Invalid command-line option: \"" + argv[i] + "\".  Use \"-h\" option for help.");
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
        
        c.setAuthenticator(c.createAuthenticatorUsingConfigCredentials());

        String command = argv[argv.length - 1];

        if ("start".equals(command)) {
            c.start();
        } else if ("stop".equals(command)) {
            System.exit(c.stop());
        } else if ("status".equals(command)) {
            System.exit(c.status());
        } else if ("check".equals(command)) {
            System.exit(c.check());
        } else if ("exit".equals(command)) {
            System.exit(c.exit());
        } else {
            System.err.println("Invalid command \"" + command + "\".");
            System.err.println("Use \"-h\" option for help.");
            System.exit(1);
        }
    }

    /**
     * Start the OpenNMS daemon.  Never returns.
     */
    public void start() {
        Starter starter = new Starter();
        starter.startDaemon();
    }

    /**
     * <p>stop</p>
     *
     * @return a int.
     */
    public int stop() {
        return invokeOperation("stop");
    }
    
    /**
     * <p>status</p>
     *
     * @return a int.
     */
    public int status() {
        Authenticator.setDefault(getAuthenticator());

        StatusGetter statusGetter = new StatusGetter();
        statusGetter.setVerbose(isVerbose());
        
        String url = getInvokeUrl() + "&operation=status";
        try {
            statusGetter.setInvokeURL(new URL(url));
        } catch (MalformedURLException e) {
            String message = "Error creating URL object for invoke URL: '" + url + "'";
            System.err.println(message);
            LOG.error(message, e);
        }

        try {
            statusGetter.queryStatus();
        } catch (Throwable t) {
            String message =  "Error invoking status command";
            System.err.println(message);
            LOG.error(message, t);
            return 1;
        }

        switch (statusGetter.getStatus()) {
        case NOT_RUNNING:
        case CONNECTION_REFUSED:
            return 3;  // According to LSB: 3 - service not running

        case PARTIALLY_RUNNING:
            /*
             * According to LSB: reserved for application So, I say
             * 160 - partially running
             */
            return 160;

        case RUNNING:
            return 0; // everything should be good and running

        default:
        	LOG.error("Unknown status returned from statusGetter.getStatus(): {}", statusGetter.getStatus());
            return 1;
        }
    }

    /**
     * <p>check</p>
     *
     * @return a int.
     */
    public int check() {
        try {
            new DatabaseChecker().check();
        } catch (final Throwable t) {
        	LOG.error("error invoking check command", t);
            return 1;
        }
        return 0;
    }

    /**
     * <p>exit</p>
     *
     * @return a int.
     */
    public int exit() {
        return invokeOperation("doSystemExit");
    }
    
    int invokeOperation(String operation) {
        Authenticator.setDefault(getAuthenticator());

        String urlString = getInvokeUrl() + "&operation=" + operation;
        try {
            URL invoke = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) invoke.openConnection();
            connection.setReadTimeout(getHttpRequestReadTimeout());
            InputStream in = connection.getInputStream();

            int ch;
            while ((ch = in.read()) != -1) {
                System.out.write((char) ch);
            }
            in.close();
            System.out.println("");
            System.out.flush();
        } catch (final ConnectException e) {
        	LOG.error("error when attempting to fetch URL \"{}\"", urlString, e);
            if (isVerbose()) {
                System.out.println(e.getMessage() + " when attempting to fetch URL \"" + urlString + "\"");
            }
            return 1;
        } catch (final Throwable t) {
        	LOG.error("error invoking {} operation", operation, t);
            System.out.println("error invoking " + operation + " operation");
            return 1;
        }

        return 0;
    }

    /*
     * Create an Authenticator so that we can provide authentication, if
     * needed, when go to connect to the URL
     */
    Authenticator createAuthenticatorUsingConfigCredentials() {
        Service service = getConfiguredService(JMX_HTTP_ADAPTER_NAME);
        if (service == null) {
            // Didn't find the service we were looking for
        	LOG.warn("Could not find configured service for '{}'", JMX_HTTP_ADAPTER_NAME);
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
                	LOG.error("AuthenticationMethod is \"{}\", but only \"basic\" is supported", attrib.getValue());
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
            @Override
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

    /**
     * <p>isVerbose</p>
     *
     * @return a boolean.
     */
    public boolean isVerbose() {
        return m_verbose;
    }

    /**
     * <p>setVerbose</p>
     *
     * @param verbose a boolean.
     */
    public void setVerbose(boolean verbose) {
        m_verbose = verbose;
    }

    /**
     * <p>getInvokeUrl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInvokeUrl() {
        return m_invokeUrl;
    }

    /**
     * <p>setInvokeUrl</p>
     *
     * @param invokerUrl a {@link java.lang.String} object.
     */
    public void setInvokeUrl(String invokerUrl) {
        m_invokeUrl = invokerUrl;
    }

    /**
     * <p>getAuthenticator</p>
     *
     * @return a {@link java.net.Authenticator} object.
     */
    public Authenticator getAuthenticator() {
        return m_authenticator;
    }

    /**
     * <p>setAuthenticator</p>
     *
     * @param authenticator a {@link java.net.Authenticator} object.
     */
    public void setAuthenticator(Authenticator authenticator) {
        m_authenticator = authenticator;
    }

    /**
     * <p>getHttpRequestReadTimeout</p>
     *
     * @return a int.
     */
    public int getHttpRequestReadTimeout() {
        return m_httpRequestReadTimeout;
    }

    /**
     * <p>setHttpRequestReadTimeout</p>
     *
     * @param httpRequestReadTimeout a int.
     */
    public void setHttpRequestReadTimeout(int httpRequestReadTimeout) {
        m_httpRequestReadTimeout = httpRequestReadTimeout;
    }
}
