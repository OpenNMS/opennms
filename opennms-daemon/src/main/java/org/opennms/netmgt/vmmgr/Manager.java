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
 * 2008 Jan 16: Test that we can call InetAddress.getLocalHost(). - dj@opennms.org
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

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.service.types.InvokeAtType;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.protocols.icmp.IcmpSocket;

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
    /**
     * The log4j category used to log debug messages and statements.
     */
    private static final String LOG4J_CATEGORY = "OpenNMS.Manager";

    public void stop() {
        setLogPrefix();

        for (MBeanServer server : getMBeanServers()) {
            stop(server);
        }
    }
    
    private void stop(MBeanServer server) {
        log().debug("Beginning shutdown");
        Invoker invoker = new Invoker();
        invoker.setServer(server);
        invoker.setAtType(InvokeAtType.STOP);
        invoker.setReverse(true);
        invoker.setFailFast(false);
        
        List<InvokerService> services = InvokerService.createServiceList(Invoker.getDefaultServiceConfigFactory().getServices());
        invoker.setServices(services);
        invoker.getObjectInstances();
        invoker.invokeMethods();

        log().debug("Shutdown complete");
    }
    
    public List<String> status() {
        setLogPrefix();

        List<String> result = new ArrayList<String>();
        for (MBeanServer server : getMBeanServers()) {
            result.addAll(status(server));
        }
        return result;
    }
    
    private List<String> status(MBeanServer server) {
        log().debug("Beginning status check");
        Invoker invoker = new Invoker();
        invoker.setServer(server);
        invoker.setAtType(InvokeAtType.STATUS);
        invoker.setFailFast(false);
        
        List<InvokerService> services = InvokerService.createServiceList(Invoker.getDefaultServiceConfigFactory().getServices());
        invoker.setServices(services);
        invoker.getObjectInstances();
        List<InvokerResult> results = invoker.invokeMethods();
        
        List<String> statusInfo = new ArrayList<String>(results.size());
        for (InvokerResult invokerResult : results) {
            if (invokerResult.getThrowable() == null) {
                statusInfo.add("Status: "
                               + invokerResult.getMbean().getObjectName()
                               + " = " + invokerResult.getResult().toString());
            } else {
                statusInfo.add("Status: "
                               + invokerResult.getMbean().getObjectName()
                               + " = STATUS_CHECK_ERROR");
            }
        }
        log().debug("Status check complete");
        
        return statusInfo;
    }

    /**
     * Uncleanly shutdown OpenNMS.  This method calls
     * {@see java.lang.System.exit(int)}, which causes the JVM to
     * exit immediately.  This method is usually invoked via JMX from
     * another process as the last stage of shutting down OpenNMS.
     * 
     * @return does not return
     */
    public void doSystemExit() {
        setLogPrefix();

        log().debug("doSystemExit called");
        
        if (log().isDebugEnabled()) {
            dumpThreads();
            
            Runtime r = Runtime.getRuntime();
            log().debug("memory usage (free/used/total/max allowed): " + r.freeMemory() + "/" + (r.totalMemory() - r.freeMemory()) + "/" + r.totalMemory() + "/" + (r.maxMemory() == Long.MAX_VALUE ? "infinite" : r.maxMemory()));
        }
        
        log().info("calling System.exit(1)");
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
        log().debug("Thread dump of " + threads.size() + " threads (" + daemons + " daemons):");
        Map<Thread, StackTraceElement[]> sortedThreads = new TreeMap<Thread, StackTraceElement[]>(new Comparator<Thread>() {
            public int compare(Thread t1, Thread t2) {
                return new Long(t1.getId()).compareTo(new Long(t2.getId()));
            }
        });
        sortedThreads.putAll(threads);

        for (Entry<Thread, StackTraceElement[]> entry : sortedThreads.entrySet()) {
            Thread thread = entry.getKey();
            log().debug("Thread " + thread.getId() + (thread.isDaemon() ? " (daemon)" : "") + ": " + thread + " (state: " + thread.getState() + ")");
            for (StackTraceElement e : entry.getValue()) {
                log().debug("\t" + e);
            }
        }
        log().debug("Thread dump completed.");
    }

    @SuppressWarnings("deprecation")
    private void shutdownLogging() {
        Logger.shutdown();
    }
    
    public void doTestLoadLibraries() {
        setLogPrefix();

        testIcmpSocket();
        testGetLocalHost();
    }

    private void testGetLocalHost() {
        try {
            InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new UndeclaredThrowableException(e, "Could not lookup the host name for the local host machine: " + e);
        }
    }

    private void testIcmpSocket() {
        IcmpSocket s = null;
        try {
            s = new IcmpSocket();
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t, ("Could not initialize ICMP socket: " + t.getMessage()));
        }
        s.close();
    }

    private void setLogPrefix() {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    private List<MBeanServer> getMBeanServers() {
        return MBeanServerFactory.findMBeanServer(null);
    }
}
