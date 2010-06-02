//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 May 14: added threshold config change handler for in-line thresholds processing
// 2006 Apr 27: added support for pathOutageEnabled
// 2006 Apr 17: added path outage processing for nodeDown event
// 2003 Nov 11: Merged changes from Rackspace project
// 2003 Oct 08: Implemented the poller release function.
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.poller;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.PollOutagesConfig;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.pollables.DbPollEvent;
import org.opennms.netmgt.poller.pollables.PollEvent;
import org.opennms.netmgt.poller.pollables.PollableNetwork;
import org.opennms.netmgt.poller.pollables.PollableNode;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.poller.pollables.PollableServiceConfig;
import org.opennms.netmgt.poller.pollables.PollableVisitor;
import org.opennms.netmgt.poller.pollables.PollableVisitorAdaptor;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.utils.Querier;
import org.opennms.netmgt.utils.Updater;

public class Poller extends AbstractServiceDaemon {

    private final static Poller m_singleton = new Poller();

    private boolean m_initialized = false;

    private LegacyScheduler m_scheduler = null;

    private PollerEventProcessor m_eventProcessor;

    private PollableNetwork m_network;

    private QueryManager m_queryManager;

    private PollerConfig m_pollerConfig;

    private PollOutagesConfig m_pollOutagesConfig;

    private EventIpcManager m_eventMgr;

    private DataSource m_dataSource;

    public Poller() {
    	super("OpenNMS.Poller");
    }

    /* Getters/Setters used for dependency injection */
    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    public void setEventManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }

    public PollerEventProcessor getEventProcessor() {
        return m_eventProcessor;
    }

    public void setEventProcessor(PollerEventProcessor eventProcessor) {
        m_eventProcessor = eventProcessor;
    }

    public PollableNetwork getNetwork() {
        return m_network;
    }

    public void setNetwork(PollableNetwork network) {
        m_network = network;
    }
    
    public void setQueryManager(QueryManager queryManager) {
        m_queryManager = queryManager;
    }

    public QueryManager getQueryManager() {
        return m_queryManager;
    }
    
    public PollerConfig getPollerConfig() {
        return m_pollerConfig;
    }

    public void setPollerConfig(PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

    public PollOutagesConfig getPollOutagesConfig() {
        return m_pollOutagesConfig;
    }

    public void setPollOutagesConfig(PollOutagesConfig pollOutagesConfig) {
        m_pollOutagesConfig = pollOutagesConfig;
    }

    public Scheduler getScheduler() {
        return m_scheduler;
    }

    public void setScheduler(LegacyScheduler scheduler) {
        m_scheduler = scheduler;
    }

    protected void onInit() {
        
        // serviceUnresponsive behavior enabled/disabled?
        log().debug("init: serviceUnresponsive behavior: " + (getPollerConfig().serviceUnresponsiveEnabled() ? "enabled" : "disabled"));

        createScheduler();
        
        try {
            log().debug("init: Closing outages for unmanaged services");
            
            closeOutagesForUnmanagedServices();
        } catch (Exception e) {
            log().error("init: Failed to close ouates for unmanage services", e);
        }
        

        // Schedule the interfaces currently in the database
        //
        try {
            log().debug("start: Scheduling existing interfaces");

            scheduleExistingServices();
        } catch (Exception sqlE) {
            log().error("start: Failed to schedule existing interfaces", sqlE);
        }

        // Create an event receiver. The receiver will
        // receive events, process them, creates network
        // interfaces, and schedulers them.
        //
        try {
            log().debug("start: Creating event broadcast event processor");

            setEventProcessor(new PollerEventProcessor(this));
        } catch (Throwable t) {
            log().fatal("start: Failed to initialized the broadcast event receiver", t);

            throw new UndeclaredThrowableException(t);
        }

        m_initialized = true;

    }

    /**
     * 
     */
    private void closeOutagesForUnmanagedServices() {
        Timestamp closeTime = new Timestamp((new java.util.Date()).getTime());

        final String DB_CLOSE_OUTAGES_FOR_UNMANAGED_SERVICES = "UPDATE outages set ifregainedservice = ? where outageid in (select outages.outageid from outages, ifservices where ((outages.nodeid = ifservices.nodeid) AND (outages.ipaddr = ifservices.ipaddr) AND (outages.serviceid = ifservices.serviceid) AND ((ifservices.status = 'D') OR (ifservices.status = 'F') OR (ifservices.status = 'U')) AND (outages.ifregainedservice IS NULL)))";
        Updater svcUpdater = new Updater(m_dataSource, DB_CLOSE_OUTAGES_FOR_UNMANAGED_SERVICES);
        svcUpdater.execute(closeTime);
        
        final String DB_CLOSE_OUTAGES_FOR_UNMANAGED_INTERFACES = "UPDATE outages set ifregainedservice = ? where outageid in (select outages.outageid from outages, ipinterface where ((outages.nodeid = ipinterface.nodeid) AND (outages.ipaddr = ipinterface.ipaddr) AND ((ipinterface.ismanaged = 'F') OR (ipinterface.ismanaged = 'U')) AND (outages.ifregainedservice IS NULL)))";
        Updater ifUpdater = new Updater(m_dataSource, DB_CLOSE_OUTAGES_FOR_UNMANAGED_INTERFACES);
        ifUpdater.execute(closeTime);
        


    }
    
    public void closeOutagesForNode(Date closeDate, int eventId, int nodeId) {
        Timestamp closeTime = new Timestamp(closeDate.getTime());
        final String DB_CLOSE_OUTAGES_FOR_NODE = "UPDATE outages set ifregainedservice = ?, svcRegainedEventId = ? where outages.nodeId = ? AND (outages.ifregainedservice IS NULL)";
        Updater svcUpdater = new Updater(m_dataSource, DB_CLOSE_OUTAGES_FOR_NODE);
        svcUpdater.execute(closeTime, new Integer(eventId), new Integer(nodeId));
    }
    
    public void closeOutagesForInterface(Date closeDate, int eventId, int nodeId, String ipAddr) {
        Timestamp closeTime = new Timestamp(closeDate.getTime());
        final String DB_CLOSE_OUTAGES_FOR_IFACE = "UPDATE outages set ifregainedservice = ?, svcRegainedEventId = ? where outages.nodeId = ? AND outages.ipAddr = ? AND (outages.ifregainedservice IS NULL)";
        Updater svcUpdater = new Updater(m_dataSource, DB_CLOSE_OUTAGES_FOR_IFACE);
        svcUpdater.execute(closeTime, new Integer(eventId), new Integer(nodeId), ipAddr);
    }
    
    public void closeOutagesForService(Date closeDate, int eventId, int nodeId, String ipAddr, String serviceName) {
        Timestamp closeTime = new Timestamp(closeDate.getTime());
        final String DB_CLOSE_OUTAGES_FOR_SERVICE = "UPDATE outages set ifregainedservice = ?, svcRegainedEventId = ? where outageid in (select outages.outageid from outages, service where outages.nodeid = ? AND outages.ipaddr = ? AND outages.serviceid = service.serviceId AND service.servicename = ? AND outages.ifregainedservice IS NULL)";
        Updater svcUpdater = new Updater(m_dataSource, DB_CLOSE_OUTAGES_FOR_SERVICE);
        svcUpdater.execute(closeTime, new Integer(eventId), new Integer(nodeId), ipAddr, serviceName);
    }

    private void createScheduler() {

        ThreadCategory log = ThreadCategory.getInstance(getClass());
        // Create a scheduler
        //
        try {
            log.debug("init: Creating poller scheduler");

            setScheduler(new LegacyScheduler("Poller", getPollerConfig().getThreads()));
        } catch (RuntimeException e) {
            log.fatal("init: Failed to create poller scheduler", e);
            throw e;
        }
    }

    protected void onStart() {
		// get the category logger
        // start the scheduler
        //
        try {
            if (log().isDebugEnabled())
                log().debug("start: Starting poller scheduler");

            getScheduler().start();
        } catch (RuntimeException e) {
            if (log().isEnabledFor(ThreadCategory.Level.FATAL))
                log().fatal("start: Failed to start scheduler", e);
            throw e;
        }
	}

    protected void onStop() {
        if(getScheduler()!=null) {
            getScheduler().stop();
        }
        if(getEventProcessor()!=null) {
            getEventProcessor().close();
        }

        releaseServiceMonitors();
        setScheduler(null);
	}

	private void releaseServiceMonitors() {
		getPollerConfig().releaseAllServiceMonitors();
	}

	protected void onPause() {
		getScheduler().pause();
	}

    protected void onResume() {
		getScheduler().resume();
	}

    public static Poller getInstance() {
        return m_singleton;
    }

    public ServiceMonitor getServiceMonitor(String svcName) {
        return getPollerConfig().getServiceMonitor(svcName);
    }

    private void scheduleExistingServices() throws Exception {
        ThreadCategory log = ThreadCategory.getInstance(getClass());
        
        scheduleMatchingServices(null);
        
        getNetwork().recalculateStatus();
        getNetwork().propagateInitialCause();
        getNetwork().resetStatusChanged();
        
        
        // Debug dump pollable network
        //
        if (log.isDebugEnabled()) {
            log.debug("scheduleExistingServices: dumping content of pollable network: ");
            getNetwork().dump();
        }
        

    }
    
    public void scheduleService(final int nodeId, final String nodeLabel, final String ipAddr, final String svcName) {
        final ThreadCategory log = ThreadCategory.getInstance(getClass());
        try {
            /*
             * Do this here so that we can use the treeLock for this node as we
             * add its service and schedule it
             */
            PollableNode node;
            synchronized (getNetwork()) {
                node = getNetwork().getNode(nodeId);
                if (node == null) {
                    node = getNetwork().createNode(nodeId, nodeLabel);
                }
            }

            final PollableNode svcNode = node;
            Runnable r = new Runnable() {
                public void run() {
                    int matchCount = scheduleMatchingServices("ifServices.nodeId = "+nodeId+" AND ifServices.ipAddr = '"+ipAddr+"' AND service.serviceName = '"+svcName+"'");
                    if (matchCount > 0) {
                        svcNode.recalculateStatus();
                        svcNode.processStatusChange(new Date());
                    } else {
                        log.warn("Attempt to schedule service "+nodeId+"/"+ipAddr+"/"+svcName+" found no active service");
                    }
                }
            };
            node.withTreeLock(r);

        } catch (Exception e) {
            log.error("Unable to schedule service "+nodeId+"/"+ipAddr+"/"+svcName, e);
        }
    }
    
    private int scheduleMatchingServices(String criteria) {
        String sql = "SELECT ifServices.nodeId AS nodeId, node.nodeLabel AS nodeLabel, ifServices.ipAddr AS ipAddr, " +
                "ifServices.serviceId AS serviceId, service.serviceName AS serviceName, ifServices.status as status, " +
                "outages.svcLostEventId AS svcLostEventId, events.eventUei AS svcLostEventUei, " +
                "outages.ifLostService AS ifLostService, outages.ifRegainedService AS ifRegainedService " +
        "FROM ifServices " +
        "JOIN node ON ifServices.nodeId = node.nodeId " +
        "JOIN service ON ifServices.serviceId = service.serviceId " +
        "LEFT OUTER JOIN outages ON " +
        "ifServices.nodeId = outages.nodeId AND " +
        "ifServices.ipAddr = outages.ipAddr AND " +
        "ifServices.serviceId = outages.serviceId AND " +
        "ifRegainedService IS NULL " +
        "LEFT OUTER JOIN events ON outages.svcLostEventId = events.eventid " +
        "WHERE ifServices.status in ('A','N')" +
        (criteria == null ? "" : " AND "+criteria);
       
        
        final AtomicInteger count = new AtomicInteger(0);
        
        Querier querier = new Querier(m_dataSource, sql) {
            public void processRow(ResultSet rs) throws SQLException {
                if (scheduleService(rs.getInt("nodeId"), rs.getString("nodeLabel"), rs.getString("ipAddr"), rs.getString("serviceName"), 
                                "A".equals(rs.getString("status")), (Number)rs.getObject("svcLostEventId"), rs.getTimestamp("ifLostService"), 
                                rs.getString("svcLostEventUei"))) {
                    count.incrementAndGet();
                }
            }
        };
        querier.execute();
        
        
        return count.get();

    }
    
    private void updateServiceStatus(int nodeId, String ipAddr, String serviceName, String status) {
        final String sql = "UPDATE ifservices SET status = ? WHERE id " +
        		" IN (SELECT ifs.id FROM ifservices AS ifs JOIN service AS svc ON ifs.serviceid = svc.serviceid " +
        		" WHERE ifs.nodeId = ? AND ifs.ipAddr = ? AND svc.servicename = ?)"; 

        Updater updater = new Updater(m_dataSource, sql);
        updater.execute(status, nodeId, ipAddr, serviceName);
        
    }

    private boolean scheduleService(int nodeId, String nodeLabel, String ipAddr, String serviceName, boolean active, Number svcLostEventId, Date date, String svcLostUei) {
        ThreadCategory log = ThreadCategory.getInstance(getClass());

        Package pkg = findPackageForService(ipAddr, serviceName);
        if (pkg == null) {
            if(active){
                log.warn("Active service "+serviceName+" on "+ipAddr+" not configured for any package. Marking as Not Polled.");
                updateServiceStatus(nodeId, ipAddr, serviceName, "N");
            }
            return false;
        } else if (!active) {
            log.info("Active service "+serviceName+" on "+ipAddr+" is now configured for any package. Marking as active.");
            updateServiceStatus(nodeId, ipAddr, serviceName, "A");
        }
        
        ServiceMonitor monitor = m_pollerConfig.getServiceMonitor(serviceName);
        if (monitor == null) {
            log.info("Could not find service monitor associated with service "+serviceName);
            return false;
        }
        
        InetAddress addr;
        try {
            addr = InetAddress.getByName(ipAddr);
        } catch (UnknownHostException e) {
            log.error("Could not convert "+ipAddr+" as an InetAddress "+ipAddr);
            return false;
        }
        
        PollableService svc = getNetwork().createService(nodeId, nodeLabel, addr, serviceName);
        PollableServiceConfig pollConfig = new PollableServiceConfig(svc, m_pollerConfig, m_pollOutagesConfig, pkg, getScheduler());
        svc.setPollConfig(pollConfig);
        synchronized(svc) {
            if (svc.getSchedule() == null) {
                Schedule schedule = new Schedule(svc, pollConfig, getScheduler());
                svc.setSchedule(schedule);
            }
        }
        
        
        if (svcLostEventId == null) 
            if (svc.getParent().getStatus().isUnknown()) {
                svc.updateStatus(PollStatus.up());
            } else {
                svc.updateStatus(svc.getParent().getStatus());
            }
        else {
            svc.updateStatus(PollStatus.down());
            
            PollEvent cause = new DbPollEvent(svcLostEventId.intValue(), svcLostUei, date);

            svc.setCause(cause);

        }
        
        svc.schedule();
        
        return true;

    }

    private Package findPackageForService(String ipAddr, String serviceName) {
        Enumeration<Package> en = m_pollerConfig.enumeratePackage();
        Package lastPkg = null;
        
        while (en.hasMoreElements()) {
            Package pkg = (Package)en.nextElement();
            if (pollableServiceInPackage(ipAddr, serviceName, pkg))
                lastPkg = pkg;
        }
        return lastPkg;
    }
    
    protected boolean pollableServiceInPackage(String ipAddr, String serviceName, Package pkg) {
        
        if (pkg.getRemote()) {
            log().debug("pollableServiceInPackage: this package: "+pkg.getName()+", is a remote monitor package.");
            return false;
        }
        
        if (!m_pollerConfig.serviceInPackageAndEnabled(serviceName, pkg)) return false;
        
        boolean inPkg = m_pollerConfig.interfaceInPackage(ipAddr, pkg);
        
        if (inPkg) return true;
        
        if (m_initialized) {
            m_pollerConfig.rebuildPackageIpListMap();
            return m_pollerConfig.interfaceInPackage(ipAddr, pkg);
        }
        
        return false;
    }
    
    public boolean packageIncludesIfAndSvc(Package pkg, String ipAddr, String svcName) {
        ThreadCategory log = ThreadCategory.getInstance(getClass());

        if (!getPollerConfig().serviceInPackageAndEnabled(svcName, pkg)) {
            if (log.isDebugEnabled())
                log.debug("packageIncludesIfAndSvc: address/service: " + ipAddr + "/" + svcName + " not scheduled, service is not enabled or does not exist in package: " + pkg.getName());
            return false;
        }

        // Is the interface in the package?
        //
        if (!getPollerConfig().interfaceInPackage(ipAddr, pkg)) {

            if (m_initialized) {
                getPollerConfig().rebuildPackageIpListMap();
                if (!getPollerConfig().interfaceInPackage(ipAddr, pkg)) {
                    if (log.isDebugEnabled())
                        log.debug("packageIncludesIfAndSvc: interface " + ipAddr + " gained service " + svcName + ", but the interface was not in package: " + pkg.getName());
                    return false;
                }
            } else {
                if (log.isDebugEnabled())
                    log.debug("packageIncludesIfAndSvc: address/service: " + ipAddr + "/" + svcName + " not scheduled, interface does not belong to package: " + pkg.getName());
                return false;
            }
        }
        return true;
    }

    public void refreshServicePackages() {
        PollableVisitor visitor = new PollableVisitorAdaptor() {
            public void visitService(PollableService service) {
                service.refreshConfig();
            }
        };
        getNetwork().visit(visitor);
    }

    public void refreshServiceThresholds() {
        PollableVisitor visitor = new PollableVisitorAdaptor() {
            public void visitService(PollableService service) {
                service.refreshThresholds();
            }
        };
        getNetwork().visit(visitor);
    }

}    