/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.vmmgr;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.google.common.annotations.VisibleForTesting;
import org.opennms.core.logging.Logging;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.mate.api.SecureCredentialsVaultScope;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.utils.ScvUtils;
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
    static final String LOG4J_CATEGORY = "manager";

    /**
     * This is the name of the JVM that we try to connect to when we are invoking
     * operations or checking status. If the name of the OpenNMS process changes then
     * this value might change. Run jconsole to see the list of available JVM display 
     * names on the system.
     */
    private static final String OPENNMS_JVM_DISPLAY_NAME_SUBSTRING = "opennms_bootstrap.jar start";

    public static final String DEFAULT_JMX_RMI_URL = "service:jmx:rmi:///jndi/rmi://127.0.0.1:1099/jmxrmi";

    private boolean m_verbose = false;

    private String m_jmxUrl = DEFAULT_JMX_RMI_URL;

    private String m_pid = null;

    static SecureCredentialsVault secureCredentialsVault;

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
                System.out.println("        -p <pid>        PID of the OpenNMS process. Used when the process cannot be autodetected.");
                System.out.println("        -t <timeout>    HTTP connection timeout in seconds.  Defaults to 30.");
                System.out.println("        -u <URL>        JMX RMI URL. Used when we cannot automatically attach to the OpenNMS JVM.");
                System.out.println("        -v              Verbose mode.");
                System.out.println("");
                System.out.println("Accepted commands: start, stop, status, check, dumpThreads, exit");
                System.out.println("");
                System.out.println("The default JMX RMI URL is: " + DEFAULT_JMX_RMI_URL);
                System.exit(0);
            } else if (argv[i].equals("-p")) {
                c.setPid(argv[i + 1]);
                i++;
            } else if (argv[i].equals("-t")) {
                c.setRmiHandshakeTimeout(Integer.parseInt(argv[i + 1]) * 1000);
                i++;
            } else if (argv[i].equals("-u")) {
                c.setJmxRmiUrl(argv[i + 1]);
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

        interpolateSystemProperties();

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

    static void interpolateSystemProperties() {
        if (secureCredentialsVault == null) {
            try {

                String opennmsHome = System.getProperty("opennms.home");
                if (opennmsHome != null && !opennmsHome.isEmpty()) {
                    Properties scvProps = ScvUtils.loadScvProperties(opennmsHome);
                    final Class clazz = Class.forName("org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault");
                    final Constructor constructor = clazz.getConstructor(String.class, String.class, String.class);
                    final String keyStoreKeyProperty = (String) clazz.getField("KEYSTORE_KEY_PROPERTY").get(null);
                    final String defaultKeyStoreKey = (String) clazz.getField("DEFAULT_KEYSTORE_KEY").get(null);
                    secureCredentialsVault = (SecureCredentialsVault) constructor.newInstance(
                            Paths.get(opennmsHome, "etc", "scv.jce").toString(),
                            System.getProperty(keyStoreKeyProperty, defaultKeyStoreKey), scvProps.getProperty(ScvUtils.SCV_KEYSTORE_TYPE_PROPERTY)
                    );
                }
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException | NoSuchFieldException e) {
                LOG.warn("Error instantiating JCEKSSecureCredentialsVault: {}", e.getMessage());
                return;
            }
        }

        final Scope scope = new SecureCredentialsVaultScope(secureCredentialsVault);

        for(final Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || !(entry.getValue() instanceof String)) {
                continue;
            }
            // Since we are only interpolating scv related properties, we restrict this to interpolating only scv related properties.
            if (((String) entry.getValue()).contains("${scv:")) {
                System.setProperty(entry.getKey().toString(), Interpolator.interpolate(entry.getValue().toString(), scope).output);
            }
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

        StatusGetter statusGetter = new StatusGetter(this);

        try {
            statusGetter.queryStatus();
        } catch (Throwable t) {
            String message = "error invoking \"status\" operation: " + t.getMessage();
            LOG.error(message, t);
            System.err.println(message);
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
            String message = "error invoking \"check\" operation: " + t.getMessage();
            LOG.error(message, t);
            System.err.println(message);
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
            String message = "error invoking \"" + operation + "\" operation: " + t.getMessage();
            LOG.error(message, t);
            System.err.println(message);
            return 1;
        }

        return 0;
    }

    public Object doInvokeOperation(String operation) throws MalformedURLException, IOException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException, NullPointerException {
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
    public String getJmxUrl() {
        try {
            // Check to see if the com.sun.tools.attach classes are loadable in
            // this JVM
            Class.forName("com.sun.tools.attach.VirtualMachine");
            Class.forName("com.sun.tools.attach.VirtualMachineDescriptor");
            Class.forName("com.sun.tools.attach.AttachNotSupportedException");
        } catch (ClassNotFoundException e) {
            LOG.info("The Attach API is not available in this JVM, falling back to JMX over RMI");
            return m_jmxUrl;
        }

        VirtualMachine vm = null;

        final StringBuilder vmNames = new StringBuilder();
        boolean first = true;

        // Use the Attach API to enumerate all of the JVMs that are running as the same
        // user on this machine
        VirtualMachineDescriptor foundVm = null;
        for (VirtualMachineDescriptor vmDescr : VirtualMachine.list()) {
            if (!first) {
                vmNames.append(", ");
            }
            vmNames.append("\"" + vmDescr.displayName() + "\"");
            first = false;

            if (vmDescr.displayName().contains(OPENNMS_JVM_DISPLAY_NAME_SUBSTRING)) {
                foundVm = vmDescr;
            }
        }

        if (foundVm == null) {
            LOG.debug("Could not find OpenNMS JVM (\"" + OPENNMS_JVM_DISPLAY_NAME_SUBSTRING + "\") among JVMs (" + vmNames + ")");
        } else {
            try {
                vm = VirtualMachine.attach(foundVm);
                LOG.debug("Attached to OpenNMS JVM: " + foundVm.id() + " (" + foundVm.displayName() + ")");
            } catch (AttachNotSupportedException e) {
                // This exception is unexpected so log a warning
                LOG.warn("Cannot attach to OpenNMS JVM", e);
            } catch (IOException e) {
                // This exception is unexpected so log a warning
                LOG.warn("IOException when attaching to OpenNMS JVM", e);
            }
        }

        if (vm == null) {
            if (m_pid == null) {
                LOG.debug("No PID specified for OpenNMS JVM");
            } else {
                try {
                    vm = VirtualMachine.attach(m_pid);
                    LOG.debug("Attached to OpenNMS JVM with PID: " + m_pid);
                } catch (AttachNotSupportedException e) {
                    // This exception is unexpected so log a warning
                    LOG.warn("Cannot attach to OpenNMS JVM at PID: " + m_pid, e);
                } catch (IOException e) {
                    // This exception will occur if the PID cannot be found
                    // because the process has been terminated
                    LOG.debug("IOException when attaching to OpenNMS JVM at PID: " + m_pid + ": " + e.getMessage());
                }
            }
        }

        if (vm == null) {
            LOG.debug("Could not attach to JVM, falling back to JMX over RMI");
            return m_jmxUrl;
        } else {
            return getJmxUriFromVirtualMachine(vm);
        }
    }

    /**
     * <p>setJmxRmiUrl</p>
     *
     * @param jmxUrl a {@link java.lang.String} object.
     */
    public void setJmxRmiUrl(String jmxUrl) {
        m_jmxUrl = jmxUrl;
    }
 
    private static String getJmxUriFromVirtualMachine(VirtualMachine vm) {
        String connectorAddress = null;
        try {
            // Get the local JMX connector URI
            connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
        } catch (IOException e) {
            throw new IllegalStateException("IOException when fetching JMX URI from JVM with ID: " + vm.id(), e);
        }

        // If there is no local JMX connector URI, we need to launch the
        // JMX agent via this VirtualMachine attachment.
        if (connectorAddress == null) {
            LOG.info("Starting local management agent in JVM with ID: " + vm.id());

            try {
                vm.startLocalManagementAgent();
            } catch (IOException e) {
                throw new IllegalStateException("IOException when starting local JMX management agent in JVM with ID: " + vm.id(), e);
            }

            // Agent is started, get the connector address
            try {
                connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
            } catch (IOException e) {
                throw new IllegalStateException("IOException when fetching JMX URI from JVM with ID: " + vm.id(), e);
            }
        }

        return connectorAddress;
    }

    public String getPid() {
        return m_pid;
    }

    public void setPid(String pid) {
        m_pid = pid;
    }

    public int getRmiHandshakeTimeout() {
        // This default value is from:
        // http://docs.oracle.com/javase/7/docs/technotes/guides/rmi/sunrmiproperties.html
        return Integer.valueOf(System.getProperty("sun.rmi.transport.handshakeTimeout", "60000"));
    }

    public void setRmiHandshakeTimeout(int httpRequestReadTimeout) {
        System.setProperty("sun.rmi.transport.tcp.handshakeTimeout", String.valueOf(httpRequestReadTimeout));
    }

    @VisibleForTesting
    static void setSecureCredentialsVault(SecureCredentialsVault secureCredentialsVault) {
        Controller.secureCredentialsVault = secureCredentialsVault;
    }
}
