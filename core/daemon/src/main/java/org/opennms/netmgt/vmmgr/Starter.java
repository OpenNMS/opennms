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

import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.List;

import javax.management.MBeanServer;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.config.ServiceConfigFactory;
import org.opennms.netmgt.config.service.InvokeAtType;
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
 */
public class Starter {
    private static final Logger LOG = LoggerFactory.getLogger(Starter.class);
    private boolean emitStandardFileNotFoundWarnings = Boolean.parseBoolean(System.getProperty("org.opennms.netmgt.vmmgr.emitStandardFileNotFoundWarnings", "true"));

    /**
     * The log4j category used to log debug messsages and statements.
     */
    private static final String LOG4J_CATEGORY = "manager";

    private void setLogPrefix() {
        Logging.putPrefix(LOG4J_CATEGORY);
    }

    /**
     * <p>startDaemon</p>
     */
    public void startDaemon() {
        try {

            setLogPrefix();

            setDefaultProperties();

            start();
        } catch(Exception e) {
            die("Exception during startup: " + e.getMessage(), e);
        }
    }

    private void setDefaultProperties() {
        setupFileResourceProperty("opennms.library.jicmp", System.mapLibraryName("jicmp"), "Initialization of ICMP socket will likely fail.");
        try {
            setupFileResourceProperty("opennms.library.jicmp6", System.mapLibraryName("jicmp6"), "Initialization of ICMPv6 socket will likely fail.");
        } catch (Throwable e) {
            LOG.warn("Could not resolve library path for jicmp6: " + e.getMessage(), e);
        }
        setupFileResourceProperty("opennms.library.jrrd", System.mapLibraryName("jrrd"), "Initialization of RRD code will likely fail if the JniRrdStrategy is used.");
        setupFileResourceProperty("jcifs.properties", "jcifs.properties", "Initialization of JCIFS will likely fail or may be improperly configured.");
    }

    private void setupFileResourceProperty(String propertyName, String file, String notFoundWarning) {
        if (System.getProperty(propertyName) == null) {
            LOG.debug("System property '{}' not set.  Searching for file '{}' in the class path.", propertyName, file);
            URL url = getClass().getClassLoader().getResource(file);
            if (url != null) {
                LOG.info("Found file '{}' at '{}'.  Setting '{}' to this path.", file, url.getPath(), propertyName);
                System.setProperty(propertyName, url.getPath());
            } else {
                if(emitStandardFileNotFoundWarnings) {
                    LOG.warn("Did not find file '{}' in the class path. {} Set the property '{}' to the location of the file.", file, notFoundWarning, propertyName);
                }
            }
        } else {
            LOG.debug("System property '{}' already set to '{}'.", propertyName, System.getProperty(propertyName));
        }
    }

    /**
     * Print out a message and stack trace and then exit.
     * This method does not return.
     * 
     * @param message message to print to System.err
     * @param t Throwable for which to print a stack trace
     */
    private void die(String message, Throwable  t) {
        LOG.error(message, t);
        System.exit(1);
    }

    public void die(String message) {
        die(message, null);
    }

    private void start() {
        LOG.debug("Beginning startup");

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        Invoker invoker = new Invoker();
        invoker.setServer(server);
        invoker.setAtType(InvokeAtType.START);
        List<InvokerService> services = InvokerService.createServiceList(new ServiceConfigFactory().getServices());
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

                LOG.error(message, result.getThrowable());

                System.err.println(message);
                result.getThrowable().printStackTrace();

                Manager manager = new Manager();
                manager.stop(1);
                manager.doSystemExit();

                // Shouldn't get here
                return;
            }
        }

        LOG.debug("Startup complete");
    }
}
