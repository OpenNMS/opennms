/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.vmmgr;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * <p>This {@link Controller} class is used to interact with a Manager
 * MBean running inside an OpenNMS JMX service. This class can invoke operations
 * on that MBean and request status information from it. It is used to execute
 * shell operations to control the lifecycle of the OpenNMS process from init.d
 * or systemd scripts.
 *
 * @author Seth
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj</a>
 */
public class Controller {

    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

    /**
     * The system property used to determine the JMX management agent URI for the
     * JVM that we are attaching to. This is used for getting status information from a
     * running OpenNMS instance.
     * 
     * @see https://docs.oracle.com/javase/8/docs/technotes/guides/management/agent.html
     */
    public static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

    /**
     * The log4j category used to log debug messages and statements.
     */
    private static final String LOG4J_CATEGORY = "manager";

    /**
     * This is the name of the JVM that we try to connect to when we are invoking
     * operations or checking status. If the name of the OpenNMS process changes then
     * this value might change. Run jconsole to see the list of available JVM display 
     * names on the system.
     */
    private static final String OPENNMS_JVM_DISPLAY_NAME_SUBSTRING = "opennms_bootstrap";

    private boolean m_verbose = false;

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
                System.out.println("        -v              Verbose mode.");
                System.out.println("");
                System.out.println("Accepted commands: start, stop, status, check, dumpThreads, exit");
                System.exit(0);
            } else if (argv[i].equals("-t")) {
                c.setRmiHandshakeTimeout(Integer.parseInt(argv[i + 1]) * 1000);
                i++;
            } else if (argv[i].equals("-v")) {
                c.setVerbose(true);
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

        String command = argv[argv.length - 1];

        if ("start".equals(command)) {
            c.start();
        } else if ("stop".equals(command)) {
            System.exit(c.stop());
        } else if ("status".equals(command)) {
            System.exit(c.status());
        } else if ("check".equals(command)) {
            System.exit(c.check());
        } else if ("dumpThreads".equals(command)) {
            System.exit(c.dumpThreads());
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

        StatusGetter statusGetter = new StatusGetter();

        try {
            statusGetter.setVerbose(isVerbose());
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
     * <p>dumpThreads</p>
     *
     * @return a int.
     */
    public int dumpThreads() {
        return invokeOperation("dumpThreads");
    }

    /**
     * <p>exit</p>
     *
     * @return a int.
     */
    public int exit() {
        return invokeOperation("doSystemExit");
    }

    public int invokeOperation(String operation) {
        try {
            doInvokeOperation(operation); // Ignore the returned object
        } catch (final Throwable t) {
            LOG.error("error invoking \"{}\" operation", operation, t);
            System.err.println("error invoking \"" + operation + "\" operation");
            return 1;
        }

        return 0;
    }

    public static Object doInvokeOperation(String operation) throws MalformedURLException, IOException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException, NullPointerException {
        try (JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(getJmxUrl()))) {
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            return connection.invoke(ObjectName.getInstance("OpenNMS:Name=Manager"), operation, new Object[0], new String[0]);
        }
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
     * This method uses the Java Attach API to connect to a running OpenNMS JVM
     * and fetch the dynamically assigned local JMX agent URL.
     * 
     * @see https://docs.oracle.com/javase/8/docs/jdk/api/attach/spec/index.html
     * @see https://docs.oracle.com/javase/8/docs/technotes/guides/management/agent.html
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getJmxUrl() {
        for (VirtualMachineDescriptor vmDescr : VirtualMachine.list()) {
            if (vmDescr.displayName().contains(OPENNMS_JVM_DISPLAY_NAME_SUBSTRING)) {
                // Attach to the OpenNMS application
                VirtualMachine vm = null;
                try {
                    vm = VirtualMachine.attach(vmDescr);
                } catch (AttachNotSupportedException e) {
                    throw new IllegalStateException("Cannot attach to OpenNMS JVM", e);
                } catch (IOException e) {
                    throw new IllegalStateException("IOException when attaching to OpenNMS JVM", e);
                }

                String connectorAddress = null;
                try {
                    // Get the local JMX connector URI
                    vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
                } catch (IOException e) {
                    throw new IllegalStateException("IOException when fetching JMX URI", e);
                }

                // If there is no local JMX connector URI, we need to launch the
                // JMX agent via this VirtualMachine attachment.
                if (connectorAddress == null) {
                    LOG.info("Starting local management agent in JVM with ID: " + vm.id());

                    try {
                        vm.startLocalManagementAgent();
                    } catch (IOException e) {
                        throw new IllegalStateException("IOException when starting local JMX management agent", e);
                    }

                    // agent is started, get the connector address
                    try {
                        connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
                    } catch (IOException e) {
                        throw new IllegalStateException("IOException when fetching JMX URI", e);
                    }
                }

                return connectorAddress;
            }
        }
        throw new IllegalStateException("Could not find OpenNMS JVM (" + OPENNMS_JVM_DISPLAY_NAME_SUBSTRING + ")");
    }

    public int getRmiHandshakeTimeout() {
        // This default value is from:
        // http://docs.oracle.com/javase/7/docs/technotes/guides/rmi/sunrmiproperties.html
        return Integer.valueOf(System.getProperty("sun.rmi.transport.handshakeTimeout", "60000"));
    }

    public void setRmiHandshakeTimeout(int httpRequestReadTimeout) {
        System.setProperty("sun.rmi.transport.tcp.handshakeTimeout", String.valueOf(httpRequestReadTimeout));
    }
}
