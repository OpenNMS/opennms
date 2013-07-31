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

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.opennms.netmgt.config.service.types.InvokeAtType;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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
public class Manager implements ManagerMBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(Manager.class);
	
    /**
     * The log4j category used to log debug messages and statements.
     */
    private static final String LOG4J_CATEGORY = "manager";
    private static final String m_osName = System.getProperty("os.name") == null? "" : System.getProperty("os.name").toLowerCase();

    /**
     * <p>stop</p>
     */
    @Override
    public void stop() {
        setLogPrefix();

        for (MBeanServer server : getMBeanServers()) {
            stop(server);
        }
    }
    
    private void stop(MBeanServer server) {
        LOG.debug("Beginning shutdown");
        Invoker invoker = new Invoker();
        invoker.setServer(server);
        invoker.setAtType(InvokeAtType.STOP);
        invoker.setReverse(true);
        invoker.setFailFast(false);
        
        List<InvokerService> services = InvokerService.createServiceList(Invoker.getDefaultServiceConfigFactory().getServices());
        invoker.setServices(services);
        invoker.getObjectInstances();
        invoker.invokeMethods();

        LOG.debug("Shutdown complete");
    }
    
    /**
     * <p>status</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<String> status() {
        setLogPrefix();

        List<String> result = new ArrayList<String>();
        for (MBeanServer server : getMBeanServers()) {
            result.addAll(status(server));
        }
        return result;
    }
    
    private List<String> status(final MBeanServer server) {
        LOG.debug("Beginning status check");
        final Invoker invoker = new Invoker();
        invoker.setServer(server);
        invoker.setAtType(InvokeAtType.STATUS);
        invoker.setFailFast(false);

        final List<InvokerService> services = InvokerService.createServiceList(Invoker.getDefaultServiceConfigFactory().getServices());
        invoker.setServices(services);
        invoker.getObjectInstances();
        final List<InvokerResult> results = invoker.invokeMethods();
        
        final List<String> statusInfo = new ArrayList<String>(results.size());
        for (final InvokerResult invokerResult : results) {
            if (invokerResult.getThrowable() == null) {
                statusInfo.add("Status: " + invokerResult.getMbean().getObjectName() + " = " + invokerResult.getResult().toString());
            } else {
                statusInfo.add("Status: " + invokerResult.getMbean().getObjectName() + " = STATUS_CHECK_ERROR");
            }
        }
        LOG.debug("Status check complete");
        
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
        setLogPrefix();

        LOG.debug("doSystemExit called");
        
        if (LOG.isDebugEnabled()) {
            dumpThreads();
            
            Runtime r = Runtime.getRuntime();
            LOG.debug("memory usage (free/used/total/max allowed): {}/{}/{}/{}", r.freeMemory(), (r.totalMemory() - r.freeMemory()), r.totalMemory(), (r.maxMemory() == Long.MAX_VALUE ? "infinite" : r.maxMemory()));
        }
        
        LOG.info("calling System.exit(1)");
        shutdownLogging();
        System.exit(1);
    }

    private void dumpThreads() {
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        int daemons = 0;
        for (Thread t : threads.keySet()) {
            if (t.isDaemon()) {
                daemons++;
            }
        }
        LOG.debug("Thread dump of {} threads ({} daemons):", threads.size(), daemons);
        Map<Thread, StackTraceElement[]> sortedThreads = new TreeMap<Thread, StackTraceElement[]>(new Comparator<Thread>() {
            @Override
            public int compare(Thread t1, Thread t2) {
                return new Long(t1.getId()).compareTo(new Long(t2.getId()));
            }
        });
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

    private void shutdownLogging() {
    }
    
    /**
     * <p>doTestLoadLibraries</p>
     */
    @Override
    public void doTestLoadLibraries() {
        setLogPrefix();
        testPinger();
        testGetLocalHost();
    }

    private void testGetLocalHost() {
        try {
            InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new UndeclaredThrowableException(e, "Could not lookup the host name for the local host machine: " + e);
        }
    }

    private void testPinger() {
        final Pinger pinger = PingerFactory.getInstance();

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
            throwPingError("Neither IPv4 nor IPv6 are avaialable.  Bailing.");
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
    }

    private void throwPingError(final String message) throws IllegalStateException {
        String errorMessage = message;
        if (m_osName.contains("win")) {
            errorMessage += " On Windows, you can see this error if you are not running OpenNMS in an Administrator shell.";
        }
        throw new IllegalStateException(errorMessage);
    }

    private void setLogPrefix() {
        Logging.putPrefix(LOG4J_CATEGORY);
    }

    private List<MBeanServer> getMBeanServers() {
        return MBeanServerFactory.findMBeanServer(null);
    }
}
