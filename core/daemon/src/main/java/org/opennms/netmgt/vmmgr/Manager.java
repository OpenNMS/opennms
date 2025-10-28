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

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.config.ServiceConfigFactory;
import org.opennms.netmgt.config.service.InvokeAtType;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.PingerFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The Manager is responsible for managing running services in the VM
 * that it is started for.
 * </p>
 * <p>
 * The Starter starts all services configured for its VM in the
 * service-configuration.xml file and the manager exposes an
 * MBean over JMX that is used to get service status and stop services.
 * </p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj</a>
 * @author <a href="http://www.opennms.org">OpenNMS.org</a>
 */
public class Manager implements ManagerMBean {

    private static final Logger LOG = LoggerFactory.getLogger(Manager.class);

    /**
     * The log4j category used to log debug messages and statements.
     */
    private static final String LOG4J_CATEGORY = "manager";
    private static final String m_osName = System.getProperty("os.name") == null? "" : System.getProperty("os.name").toLowerCase();
    private static final long startTime = System.currentTimeMillis();
    private static final AtomicBoolean stopInitiated = new AtomicBoolean(false);
    private static final AtomicInteger exitCode = new AtomicInteger(0);


    /**
     *  Register shutdown hook to handle SIGTERM signal for the process.
     */
    public void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { LOG.info("got sigterm, stopping"); stop(); }));
        doTestLoadLibraries();
    }

    /**
     * <p>stop</p>
     */
    @Override
    public synchronized void stop() {
        if (stopInitiated.get()) {
            return;
        }
        stopInitiated.set(true);

        Logging.withPrefix(LOG4J_CATEGORY, () -> {
            for (MBeanServer server : getMBeanServers()) {
                stop(server);
            }
        });
    }

    public synchronized void stop(int processExitCode) {
        exitCode.set(processExitCode);
        stop();
    }

    private void stop(MBeanServer server) {
        Logging.withPrefix(LOG4J_CATEGORY, () -> {
            LOG.debug("Beginning shutdown");

            Invoker invoker = new Invoker();
            invoker.setServer(server);
            invoker.setAtType(InvokeAtType.STOP);
            invoker.setReverse(true);
            invoker.setFailFast(false);

            List<InvokerService> services = InvokerService.createServiceList(new ServiceConfigFactory().getServices());
            invoker.setServices(services);
            invoker.getObjectInstances();
            invoker.invokeMethods();

            LOG.debug("Shutdown complete");
        });
    }

    /**
     * <p>status</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<String> status() {
        List<String> result = new ArrayList<>();

        Logging.withPrefix(LOG4J_CATEGORY, () -> {
            for (MBeanServer server : getMBeanServers()) {
                result.addAll(status(server));
            }
        });

        return result;
    }

    private List<String> status(final MBeanServer server) {
        final List<String> statusInfo = new ArrayList<>();

        Logging.withPrefix(LOG4J_CATEGORY, () -> {
            LOG.debug("Beginning status check");
            final Invoker invoker = new Invoker();
            invoker.setServer(server);
            invoker.setAtType(InvokeAtType.STATUS);
            invoker.setFailFast(false);

            final List<InvokerService> services = InvokerService.createServiceList(new ServiceConfigFactory().getServices());
            invoker.setServices(services);
            invoker.getObjectInstances();
            final List<InvokerResult> results = invoker.invokeMethods();

            for (final InvokerResult invokerResult : results) {
                if (invokerResult.getThrowable() == null) {
                    statusInfo.add("Status: " + invokerResult.getMbean().getObjectName() + " = " + invokerResult.getResult().toString());
                } else {
                    statusInfo.add("Status: " + invokerResult.getMbean().getObjectName() + " = STATUS_CHECK_ERROR");
                }
            }
            LOG.debug("Status check complete");
        });

        return statusInfo;
    }

    /**
     * Uncleanly shutdown OpenNMS.  This method calls
     * {@see java.lang.System.exit(int)}, which causes the JVM to
     * exit immediately.  This method is usually invoked via JMX from
     * another process as the last stage of shutting down OpenNMS.
     */
    @Override
    public void doSystemExit() {
        Logging.withPrefix(LOG4J_CATEGORY, () -> {
            LOG.debug("doSystemExit called");

            if (LOG.isDebugEnabled()) {
                dumpThreads();

                Runtime r = Runtime.getRuntime();
                LOG.debug("memory usage (free/used/total/max allowed): {}/{}/{}/{}", r.freeMemory(), (r.totalMemory() - r.freeMemory()), r.totalMemory(), (r.maxMemory() == Long.MAX_VALUE ? "infinite" : r.maxMemory()));
            }


            LOG.info("calling System.exit(" + exitCode.get() + ") very shortly");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    System.exit(exitCode.get());
                }
            }, 500);
        });
    }

    public void dumpThreads() {
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        int daemons = 0;
        for (Thread t : threads.keySet()) {
            if (t.isDaemon()) {
                daemons++;
            }
        }
        LOG.debug("Thread dump of {} threads ({} daemons):", threads.size(), daemons);
        Map<Thread, StackTraceElement[]> sortedThreads = new TreeMap<>(Comparator.comparing(Thread::getId));
        sortedThreads.putAll(threads);

        for (Entry<Thread, StackTraceElement[]> entry : sortedThreads.entrySet()) {
            Thread thread = entry.getKey();
            LOG.debug("Thread {}{}: {} (state: {})", thread.getId(), (thread.isDaemon() ? " (daemon)" : ""), thread, thread.getState() );
            for (StackTraceElement e : entry.getValue()) {
                LOG.debug("\t{}", e);
            }
        }
        LOG.debug("Thread dump completed.");
    }

    /**
     * <p>doTestLoadLibraries</p>
     */
    @Override
    public void doTestLoadLibraries() {
        Logging.withPrefix(LOG4J_CATEGORY, () -> {
            testPinger();
            testGetLocalHost();
        });
    }

    private void testGetLocalHost() {
        try {
            var localHost = InetAddress.getLocalHost();
            LOG.debug("local host: {}", localHost);
        } catch (UnknownHostException e) {
            throw new UndeclaredThrowableException(e, "Could not lookup the host name for the local host machine: " + e);
        }
    }

    private void testPinger() {
        final PingerFactoryImpl pingerFactory = new PingerFactoryImpl();
        final Pinger pinger = pingerFactory.getInstance();

        boolean hasV4 = pinger.isV4Available();
        boolean hasV6 = pinger.isV6Available();

        LOG.info("Using ICMP implementation: {}", pinger.getClass().getName());
        LOG.info("IPv4 ICMP available? {}", hasV4);
        LOG.info("IPv6 ICMP available? {}", hasV6);

        if (!hasV4) {
            try {
                pinger.initialize4();
                hasV4 = true;
            } catch (final Exception e) {
                LOG.warn("Failed to initialize IPv4 stack.", e);
            }
        }

        if (!hasV6) {
            try {
                pinger.initialize6();
                hasV6 = true;
            } catch (final Exception e) {
                LOG.warn("Failed to initialize IPv6 stack.", e);
            }

        }

        if (!hasV4 && !hasV6) {
            throwPingError("Neither IPv4 nor IPv6 are available.  Bailing.");
        }

        final String requireV4String = System.getProperty("org.opennms.netmgt.icmp.requireV4");
        final String requireV6String = System.getProperty("org.opennms.netmgt.icmp.requireV6");

        if ("true".equalsIgnoreCase(requireV4String) && !hasV4) {
            throwPingError("org.opennms.netmgt.icmp.requireV4 is true, but IPv4 ICMP could not be initialized.");
        }
        if ("true".equalsIgnoreCase(requireV6String) && !hasV6) {
            throwPingError("org.opennms.netmgt.icmp.requireV6 is true, but IPv6 ICMP could not be initialized.");
        }

        // at least one is initialized, and we haven't said otherwise, so barrel ahead
        // but first, reset the pinger factory, so we can let auto-detection happen
        pingerFactory.reset();
    }

    private void throwPingError(final String message) throws IllegalStateException {
        String errorMessage = message;
        if (m_osName.contains("win")) {
            errorMessage += " On Windows, you can see this error if you are not running OpenNMS in an Administrator shell.";
        }
        throw new IllegalStateException(errorMessage);
    }

    private List<MBeanServer> getMBeanServers() {
        return MBeanServerFactory.findMBeanServer(null);
    }

    public Long getUptime() {
        return (System.currentTimeMillis() - startTime);
    }
}
