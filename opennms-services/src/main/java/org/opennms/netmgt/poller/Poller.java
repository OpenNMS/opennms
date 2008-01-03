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

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.plugins.IcmpPlugin;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.PollOutagesConfig;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.pollables.DbPollEvent;
import org.opennms.netmgt.poller.pollables.PollEvent;
import org.opennms.netmgt.poller.pollables.PollableElement;
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
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

public class Poller extends AbstractServiceDaemon {

    private final static Poller m_singleton = new Poller();

    private boolean m_initialized = false;

    private LegacyScheduler m_scheduler = null;

    private PollerEventProcessor m_receiver;

    private PollableNetwork m_network = new PollableNetwork(new DefaultPollContext(this));

    private QueryManager m_queryMgr = new DefaultQueryManager();

    private PollerConfig m_pollerConfig;

    private PollOutagesConfig m_pollOutagesConfig;

    private EventIpcManager m_eventMgr;

    private DataSource m_dbConnectionFactory;

    public Poller() {
    	super("OpenNMS.Poller");
    }

    protected void onInit() {

        // get the category logger
        // set the DbConnectionFactory in the QueryManager
        m_queryMgr.setDbConnectionFactory(m_dbConnectionFactory);

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

            m_receiver = new PollerEventProcessor(this);
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
        Updater svcUpdater = new Updater(m_dbConnectionFactory, DB_CLOSE_OUTAGES_FOR_UNMANAGED_SERVICES);
        svcUpdater.execute(closeTime);
        
        final String DB_CLOSE_OUTAGES_FOR_UNMANAGED_INTERFACES = "UPDATE outages set ifregainedservice = ? where outageid in (select outages.outageid from outages, ipinterface where ((outages.nodeid = ipinterface.nodeid) AND (outages.ipaddr = ipinterface.ipaddr) AND ((ipinterface.ismanaged = 'F') OR (ipinterface.ismanaged = 'U')) AND (outages.ifregainedservice IS NULL)))";
        Updater ifUpdater = new Updater(m_dbConnectionFactory, DB_CLOSE_OUTAGES_FOR_UNMANAGED_INTERFACES);
        ifUpdater.execute(closeTime);
        


    }
    
    public void closeOutagesForNode(Date closeDate, int eventId, int nodeId) {
        Timestamp closeTime = new Timestamp(closeDate.getTime());
        final String DB_CLOSE_OUTAGES_FOR_NODE = "UPDATE outages set ifregainedservice = ?, svcRegainedEventId = ? where outages.nodeId = ? AND (outages.ifregainedservice IS NULL)";
        Updater svcUpdater = new Updater(m_dbConnectionFactory, DB_CLOSE_OUTAGES_FOR_NODE);
        svcUpdater.execute(closeTime, new Integer(eventId), new Integer(nodeId));
    }
    
    public void closeOutagesForInterface(Date closeDate, int eventId, int nodeId, String ipAddr) {
        Timestamp closeTime = new Timestamp(closeDate.getTime());
        final String DB_CLOSE_OUTAGES_FOR_IFACE = "UPDATE outages set ifregainedservice = ?, svcRegainedEventId = ? where outages.nodeId = ? AND outages.ipAddr = ? AND (outages.ifregainedservice IS NULL)";
        Updater svcUpdater = new Updater(m_dbConnectionFactory, DB_CLOSE_OUTAGES_FOR_IFACE);
        svcUpdater.execute(closeTime, new Integer(eventId), new Integer(nodeId), ipAddr);
    }
    
    public void closeOutagesForService(Date closeDate, int eventId, int nodeId, String ipAddr, String serviceName) {
        Timestamp closeTime = new Timestamp(closeDate.getTime());
        final String DB_CLOSE_OUTAGES_FOR_SERVICE = "UPDATE outages set ifregainedservice = ?, svcRegainedEventId = ? where outageid in (select outages.outageid from outages, service where outages.nodeid = ? AND outages.ipaddr = ? AND outages.serviceid = service.serviceId AND service.servicename = ? AND outages.ifregainedservice IS NULL)";
        Updater svcUpdater = new Updater(m_dbConnectionFactory, DB_CLOSE_OUTAGES_FOR_SERVICE);
        svcUpdater.execute(closeTime, new Integer(eventId), new Integer(nodeId), ipAddr, serviceName);
    }

    private void createScheduler() {

        Category log = ThreadCategory.getInstance(getClass());
        // Create a scheduler
        //
        try {
            log.debug("init: Creating poller scheduler");

            m_scheduler = new LegacyScheduler("Poller", getPollerConfig().getThreads());
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

            m_scheduler.start();
        } catch (RuntimeException e) {
            if (log().isEnabledFor(Level.FATAL))
                log().fatal("start: Failed to start scheduler", e);
            throw e;
        }
	}

    protected void onStop() {
        if(m_scheduler!=null) {
            m_scheduler.stop();
        }
        if(m_receiver!=null) {
            m_receiver.close();
        }

        releaseServiceMonitors();
        m_scheduler = null;
	}

	private void releaseServiceMonitors() {
		getPollerConfig().releaseAllServiceMonitors();
	}

	protected void onPause() {
		m_scheduler.pause();
	}

    protected void onResume() {
		m_scheduler.resume();
	}

    public static Poller getInstance() {
        return m_singleton;
    }

    public Scheduler getScheduler() {
        return m_scheduler;
    }

    public ServiceMonitor getServiceMonitor(String svcName) {
        return getPollerConfig().getServiceMonitor(svcName);
    }

    public PollableNetwork getNetwork() {
        return m_network;
    }

    static private class InitCause extends PollableVisitorAdaptor {
        private PollEvent m_cause;

        public void setCause(PollEvent cause) {
            m_cause = cause;
        }
        
        public void visitElement(PollableElement element) {
            if (!element.hasOpenOutage())
                element.setCause(m_cause);
        }
    }

    private void scheduleExistingServices() throws Exception {
        Category log = ThreadCategory.getInstance(getClass());
        
        scheduleMatchingServices(null);
        
        m_network.recalculateStatus();
        m_network.resetStatusChanged();
        
        // Debug dump pollable network
        //
        if (log.isDebugEnabled()) {
            log.debug("scheduleExistingServices: dumping content of pollable network: ");
            m_network.dump();
        }
        

    }
    
    public void scheduleService(final int nodeId, final String nodeLabel, final String ipAddr, final String svcName) {
        final Category log = ThreadCategory.getInstance(getClass());
        try {
            /*
             * Do this here so that we can use the treeLock for this node as we
             * add its service and schedule it
             */
            PollableNode node;
            synchronized (m_network) {
                node = m_network.getNode(nodeId);
                if (node == null) {
                    node = m_network.createNode(nodeId, nodeLabel);
                }
            }

            final PollableNode svcNode = node;
            Runnable r = new Runnable() {
                public void run() {
                    int matchCount = scheduleMatchingServices("ifServices.nodeId = "+nodeId+" AND ifServices.ipAddr = '"+ipAddr+"' AND service.serviceName = '"+svcName+"'");
                    if (matchCount > 0) {
                        svcNode.recalculateStatus();
                        svcNode.resetStatusChanged();
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
                "ifServices.serviceId AS serviceId, service.serviceName AS serviceName, " +
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
        "WHERE ifServices.status = 'A'" +
        (criteria == null ? "" : " AND "+criteria);
       
        
        
        Querier querier = new Querier(m_dbConnectionFactory, sql) {
            public void processRow(ResultSet rs) throws SQLException {
                scheduleService(rs.getInt("nodeId"), rs.getString("nodeLabel"), rs.getString("ipAddr"), rs.getString("serviceName"), 
                                (Number)rs.getObject("svcLostEventId"), rs.getTimestamp("ifLostService"), 
                                rs.getString("svcLostEventUei"));
            }
        };
        querier.execute();
        
        
        return querier.getCount();

    }

    private void scheduleService(int nodeId, String nodeLabel, String ipAddr, String serviceName, Number svcLostEventId, Date date, String svcLostUei) {
        Category log = ThreadCategory.getInstance();

        Package pkg = findPackageForService(ipAddr, serviceName);
        if (pkg == null) {
            log.warn("Active service "+serviceName+" on "+ipAddr+" not configured for any package");
            return;
        }
        
        ServiceMonitor monitor = m_pollerConfig.getServiceMonitor(serviceName);
        if (monitor == null) {
            log.info("Could not find service monitor associated with service "+serviceName);
            return;
        }
        
        InetAddress addr;
        try {
            addr = InetAddress.getByName(ipAddr);
        } catch (UnknownHostException e) {
            log.error("Could not convert "+ipAddr+" as an InetAddress "+ipAddr);
            return;
        }
        
        PollableService svc = m_network.createService(nodeId, nodeLabel, addr, serviceName);
        PollableServiceConfig pollConfig = new PollableServiceConfig(svc, m_pollerConfig, m_pollOutagesConfig, pkg, m_scheduler);
        svc.setPollConfig(pollConfig);
        synchronized(svc) {
            if (svc.getSchedule() == null) {
                Schedule schedule = new Schedule(svc, pollConfig, m_scheduler);
                svc.setSchedule(schedule);
            }
        }
        
        
        if (svcLostEventId == null) 
            svc.updateStatus(PollStatus.up());
        else {
            svc.updateStatus(PollStatus.down());
            
            InitCause causeSetter = new InitCause();
            PollEvent cause = new DbPollEvent(svcLostEventId.intValue(), date);
            causeSetter.setCause(cause);
            
            if (EventConstants.NODE_LOST_SERVICE_EVENT_UEI.equals(svcLostUei)) {
                svc.visit(causeSetter);
            } else if (EventConstants.INTERFACE_DOWN_EVENT_UEI.equals(svcLostUei)) {
                svc.getInterface().visit(causeSetter);
            } else if (EventConstants.NODE_DOWN_EVENT_UEI.equals(svcLostUei)) {
                svc.getNode().visit(causeSetter);
            }
        }
        
        svc.schedule();

    }

    private Package findPackageForService(String ipAddr, String serviceName) {
        Enumeration en = m_pollerConfig.enumeratePackage();
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
        Category log = ThreadCategory.getInstance();

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

    /**
     * @return
     */
    PollerConfig getPollerConfig() {
        return m_pollerConfig;
    }

    /**
     * @param instance
     */
    public void setPollerConfig(PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

    PollOutagesConfig getPollOutagesConfig() {
        return m_pollOutagesConfig;
    }

    public void setPollOutagesConfig(PollOutagesConfig pollOutagesConfig) {
        m_pollOutagesConfig = pollOutagesConfig;
    }

    public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    public void setEventManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }

    /**
     * @param queryMgr
     *            The queryMgr to set.
     */
    void setQueryMgr(QueryManager queryMgr) {
        m_queryMgr = queryMgr;
    }

    /**
     * @return Returns the queryMgr.
     */
    QueryManager getQueryMgr() {
        return m_queryMgr;
    }

    /**
     * @param instance
     */
    public void setDbConnectionFactory(DataSource dbConnectionFactory) {
        m_dbConnectionFactory = dbConnectionFactory;
    }

    public Event createEvent(String uei, int nodeId, InetAddress address, String svcName, java.util.Date date, String reason) {
        Category log = ThreadCategory.getInstance(getClass());
    
        if (log.isDebugEnabled())
            log.debug("createEvent: uei = " + uei + " nodeid = " + nodeId);
    
        // create the event to be sent
        Event newEvent = new Event();
        newEvent.setUei(uei);
        newEvent.setSource(getName());
        newEvent.setNodeid((long) nodeId);
        if (address != null)
            newEvent.setInterface(address.getHostAddress());
    
        if (svcName != null)
            newEvent.setService(svcName);
    
        try {
            newEvent.setHost(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException uhE) {
            newEvent.setHost("unresolved.host");
            log.warn("Failed to resolve local hostname", uhE);
        }
    
        // Set event time
        newEvent.setTime(EventConstants.formatToString(date));
    
        // For service lost events (nodeLostService) retrieve the 
        // reason parameter
        Parms eventParms = new Parms();
        Parm eventParm = new Parm();
        Value parmValue = new Value();

        if (uei.equals(EventConstants.NODE_DOWN_EVENT_UEI) && getPollerConfig().pathOutageEnabled()) {
            String[] criticalPath = getCriticalPath(nodeId);
            if(criticalPath[0] != null && !criticalPath[0].equals("")) {
                if(!testCriticalPath(criticalPath)) {
                    log.debug("Critical path test failed for node " + nodeId);
                    // add eventReason, criticalPathIp, criticalPathService parms
                    eventParm = new Parm();
                    eventParm.setParmName(EventConstants.PARM_LOSTSERVICE_REASON);
                    parmValue = new Value();
                    parmValue.setContent(EventConstants.PARM_VALUE_PATHOUTAGE);
                    eventParm.setValue(parmValue);
                    eventParms.addParm(eventParm);

                    eventParm = new Parm();
                    eventParm.setParmName(EventConstants.PARM_CRITICAL_PATH_IP);
                    parmValue = new Value();
                    parmValue.setContent(criticalPath[0]);
                    eventParm.setValue(parmValue);
                    eventParms.addParm(eventParm);

                    eventParm = new Parm();
                    eventParm.setParmName(EventConstants.PARM_CRITICAL_PATH_SVC);
                    parmValue = new Value();
                    parmValue.setContent(criticalPath[1]);
                    eventParm.setValue(parmValue);
                    eventParms.addParm(eventParm);
                } else {
                    log.debug("Critical path test passed for node " + nodeId);
                }
            } else {
                log.debug("No Critical path to test for node " + nodeId);
            }
        }

        
        if (uei.equals(EventConstants.NODE_LOST_SERVICE_EVENT_UEI)) {
            eventParm = new Parm();
            eventParm.setParmName(EventConstants.PARM_LOSTSERVICE_REASON);
            parmValue = new Value();
            parmValue.setContent(reason == null ? "Unknown" : reason);
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);
        }
        
        // For node level events (nodeUp/nodeDown) retrieve the
        // node's nodeLabel value and add it as a parm
        if (uei.equals(EventConstants.NODE_UP_EVENT_UEI) || uei.equals(EventConstants.NODE_DOWN_EVENT_UEI)) {
            String nodeLabel = null;
            try {
                nodeLabel = getQueryMgr().getNodeLabel(nodeId);
            } catch (SQLException sqlE) {
                // Log a warning
                log.warn("Failed to retrieve node label for nodeid " + nodeId, sqlE);
            }
    
            if (nodeLabel == null) {
                // This should never happen but if it does just
                // use nodeId for the nodeLabel so that the
                // event description has something to display.
                nodeLabel = String.valueOf(nodeId);
            }
    
            // Add nodelabel parm
            eventParm = new Parm();
            eventParm.setParmName(EventConstants.PARM_NODE_LABEL);
            parmValue = new Value();
            parmValue.setContent(nodeLabel);
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);    
        }
        // Add Parms to the event
        if (eventParms.getParmCount() > 0)
            newEvent.setParms(eventParms);
    
        return newEvent;
    }

    public void refreshServicePackages() {
	PollableVisitor visitor = new PollableVisitorAdaptor() {
		public void visitService(PollableService service) {
			service.refreshConfig();
		}
	};
	m_network.visit(visitor);
    }

    private String[] getCriticalPath(int nodeId) {
        Category log = ThreadCategory.getInstance(getClass());
        String SQL_DB_RETRIEVE_PATHOUTAGE = "SELECT criticalpathip, criticalpathservicename FROM pathoutage where nodeid=?";
        String[] cpath = new String[2];
	Connection dbc = null;
        try {
            dbc = DataSourceFactory.getInstance().getConnection();
            PreparedStatement stmt = dbc.prepareStatement(SQL_DB_RETRIEVE_PATHOUTAGE);
            stmt.setLong(1, nodeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cpath[0] = rs.getString(1);
                cpath[1] = rs.getString(2);
            }
            rs.close();
            stmt.close();
        } catch (SQLException sqlE) {
            log.error("getCriticalPath: SQLException " + sqlE);
        } finally {
            try {
                dbc.close();
            } catch (Exception e) {
                log.error("getCriticalPath: Exception on close" + e);
            }
        }

        if (cpath[0] == null || cpath[0].equals("")) {
            cpath[0] = OpennmsServerConfigFactory.getInstance().getDefaultCriticalPathIp();
            cpath[1] = "ICMP";
        }
        if (cpath[1] == null || cpath[1].equals("")) {
            cpath[1] = "ICMP";
        }
        return cpath;
    }

    private boolean testCriticalPath(String[] criticalPath) {
        //TODO: Generalize the service
        InetAddress addr = null;
        boolean result = true;
        log().debug("Test critical path IP " + criticalPath[0]);
        try {
            addr = InetAddress.getByName(criticalPath[0]);
        } catch (UnknownHostException e ) {
            log().error("failed to convert string address to InetAddress " + criticalPath[0]);
            return true;
        }
        try {
            IcmpPlugin p = new IcmpPlugin();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("retry", new Long(OpennmsServerConfigFactory.getInstance().getDefaultCriticalPathRetries()));
            map.put("timeout", new Long(OpennmsServerConfigFactory.getInstance().getDefaultCriticalPathTimeout()));

            result = p.isProtocolSupported(addr, map);
        } catch (IOException e) {
            log().error("IOException when testing critical path " + e);
        }
        return result;
    }

}
