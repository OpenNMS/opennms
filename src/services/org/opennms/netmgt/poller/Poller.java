//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
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
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DbConnectionFactory;
import org.opennms.netmgt.config.PollOutagesConfig;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

public final class Poller implements PausableFiber {
    final static String LOG4J_CATEGORY = "OpenNMS.Poller";

    private final static Poller m_singleton = new Poller();

    /**
     * Holds map of service names to service identifiers
     */
    private Map m_svcNameToId = new HashMap();

    private Map m_svcIdToName = new HashMap();

    private Scheduler m_scheduler;

    private int m_status;

    private PollerEventProcessor m_receiver;

    private PollableNetwork m_network = new PollableNetwork(this);

    /**
     * Map of all available 'ServiceMonitor' objects indexed by service name
     */
    private static Map m_svcMonitors;

    private QueryManager m_queryMgr = new DefaultQueryManager();

    private PollerConfig m_pollerConfig;

    private PollOutagesConfig m_pollOutagesConfig;

    private EventIpcManager m_eventMgr;

    private boolean m_initialized = false;

    private DbConnectionFactory m_dbConnectionFactory;

    public static final String EVENT_SOURCE = "OpenNMS.Poller";

    public Poller() {
        m_scheduler = null;
        m_status = START_PENDING;
    }

    public synchronized void init() {

        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        // get the category logger
        Category log = ThreadCategory.getInstance();
        
        // set the DbConnectionFactory in the QueryManager
        m_queryMgr.setDbConnectionFactory(m_dbConnectionFactory);

        // create service name to id maps
        createServiceMaps();

        // serviceUnresponsive behavior enabled/disabled?
        log.debug("start: serviceUnresponsive behavior: " + (getPollerConfig().serviceUnresponsiveEnabled() ? "enabled" : "disabled"));

        createScheduler();

        // Schedule the interfaces currently in the database
        //
        try {
            log.debug("start: Scheduling existing interfaces");

            scheduleExistingInterfaces();
        } catch (SQLException sqlE) {
            log.error("start: Failed to schedule existing interfaces", sqlE);
        }

        // Create an event receiver. The receiver will
        // receive events, process them, creates network
        // interfaces, and schedulers them.
        //
        try {
            log.debug("start: Creating event broadcast event processor");

            m_receiver = new PollerEventProcessor(this);
        } catch (Throwable t) {
            log.fatal("start: Failed to initialized the broadcast event receiver", t);

            throw new UndeclaredThrowableException(t);
        }

        m_initialized = true;

    }

    private void createScheduler() {

        Category log = ThreadCategory.getInstance();
        // Create a scheduler
        //
        try {
            log.debug("start: Creating poller scheduler");

            m_scheduler = new Scheduler("Poller", getPollerConfig().getThreads());
        } catch (RuntimeException e) {
            log.fatal("start: Failed to create poller scheduler", e);
            throw e;
        }
    }

    /**
     * 
     */
    private void createServiceMaps() {
        // load the serviceId to serviceName tables
        getQueryMgr().buildServiceNameToIdMaps(m_svcNameToId, m_svcIdToName);
    }

    public synchronized void start() {
        m_status = STARTING;

        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        // get the category logger
        Category log = ThreadCategory.getInstance();

        // start the scheduler
        //
        try {
            if (log.isDebugEnabled())
                log.debug("start: Starting poller scheduler");

            m_scheduler.start();
        } catch (RuntimeException e) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("start: Failed to start scheduler", e);
            throw e;
        }

        // Set the status of the service as running.
        //
        m_status = RUNNING;

        if (log.isDebugEnabled())
            log.debug("start: Poller running");
    }

    public synchronized void stop() {
        m_status = STOP_PENDING;
        m_scheduler.stop();
        m_receiver.close();

        Iterator iter = getServiceMonitors().values().iterator();
        while (iter.hasNext()) {
            ServiceMonitor sm = (ServiceMonitor) iter.next();
            sm.release();
        }
        m_scheduler = null;
        m_status = STOPPED;
        Category log = ThreadCategory.getInstance();
        if (log.isDebugEnabled())
            log.debug("stop: Poller stopped");
    }

    public synchronized void pause() {
        if (m_status != RUNNING)
            return;

        m_status = PAUSE_PENDING;
        m_scheduler.pause();
        m_status = PAUSED;

        Category log = ThreadCategory.getInstance();
        if (log.isDebugEnabled())
            log.debug("pause: Poller paused");
    }

    public synchronized void resume() {
        if (m_status != PAUSED)
            return;

        m_status = RESUME_PENDING;
        m_scheduler.resume();
        m_status = RUNNING;

        Category log = ThreadCategory.getInstance();
        if (log.isDebugEnabled())
            log.debug("resume: Poller resumed");
    }

    public synchronized int getStatus() {
        return m_status;
    }

    public String getName() {
        return "OpenNMS.Poller";
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

    public List getPollableServiceList() {
        return m_network.getPollableServices();
    }

    public PollableNode findNode(int nodeId) {
        return m_network.findNode(nodeId);
    }

    public void removeNode(int nodeId) {
        m_network.removeNode(nodeId);
    }
    
    public PollableNetwork getNetwork() {
        return m_network;
    }

    private void scheduleExistingInterfaces() throws SQLException {
        // get the category logger
        //
        Category log = ThreadCategory.getInstance();

        // Loop through loaded monitors and schedule for each one present
        //
        Set svcNames = getServiceMonitors().keySet();
        Iterator i = svcNames.iterator();
        while (i.hasNext()) {
            String svcName = (String) i.next();
            scheduleInterfacesWithService(svcName);
        }

        // Debug dump pollable network
        //
        if (log.isDebugEnabled()) {
            log.debug("scheduleExistingInterfaces: dumping content of pollable network: ");
            m_network.dumpNetwork();
        }
    }

    /**
     * @param svcName
     * @param log
     * @throws SQLException
     */
    private void scheduleInterfacesWithService(String svcName) throws SQLException {
        Category log = ThreadCategory.getInstance();

        if (log.isDebugEnabled())
            log.debug("scheduleInterfacesWithService: Scheduling existing interfaces for monitor: " + svcName);

        // Retrieve list of interfaces from the database which
        // support the service polled by this monitor
        //
        try {
            Iterator it = getQueryMgr().getInterfacesWithService(svcName).iterator();
            while (it.hasNext()) {
                IfKey key = (IfKey) it.next();
                scheduleService(key.getNodeId(), key.getIpAddr(), svcName);
            }
        } catch (SQLException sqle) {
            log.warn("scheduleInterfacesWithService: SQL exception while querying ipInterface table", sqle);
            throw sqle;
        }
    }

    public void scheduleService(int nodeId, String ipAddr, String svcName) {
        Category log = ThreadCategory.getInstance();

        /*
         * Find the last package configured for this service and schedule it
         */
        Package pkg = null;
        Enumeration epkgs = getPollerConfig().enumeratePackage();
        while (epkgs.hasMoreElements()) {
            Package spkg = (org.opennms.netmgt.config.poller.Package) epkgs.nextElement();

            // Make certain the the current service and ipaddress are in the
            // package and enabled!
            //
            if (packageIncludesIfAndSvc(spkg, ipAddr, svcName))
                pkg = spkg;
        }

        if (pkg == null) return;

        //
        // getServiceLostDate() method will return the date
        // a service was lost if the service was last known to be
        // unavailable or will return null if the service was last known to
        // be available...based on outage information on the 'outages'
        // table.
        Date svcLostDate = getQueryMgr().getServiceLostDate(nodeId, ipAddr, svcName, getServiceIdByName(svcName));
        PollStatus lastKnownStatus = (svcLostDate == null ? PollStatus.STATUS_UP : PollStatus.STATUS_DOWN);
        
        // Criteria checks have all been padded...update
        // Node Outage
        // Hierarchy and create new service for polling
        //
        try {
            ServiceConfig svcConfig = new ServiceConfig(this, pkg, svcName);
            PollableService pSvc = m_network.createPollableService(nodeId, ipAddr, svcConfig, lastKnownStatus, svcLostDate);
            
            // Initialize the service monitor with the pollable service
            //
            ServiceMonitor monitor = getServiceMonitor(svcName);
            pSvc.initializeMonitor(monitor);
            
            // Schedule the service
            //
            pSvc.schedule();
            
        } catch (UnknownHostException ex) {
            log.error("scheduleService: Failed to schedule interface " + ipAddr + " for service monitor " + svcName + ", illegal address", ex);
        } catch (InterruptedException ie) {
            log.error("scheduleService: Failed to schedule interface " + ipAddr + " for service monitor " + svcName + ", thread interrupted", ie);
        } catch (RuntimeException rE) {
            log.warn("scheduleService: Unable to schedule " + ipAddr + " for service monitor " + svcName + ", reason: " + rE.getMessage());
        } catch (Throwable t) {
            log.error("scheduleService: Uncaught exception, failed to schedule interface " + ipAddr + " for service monitor " + svcName, t);
        }
        
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
     * @return Returns the m_svcMonitors.
     */
    private Map getServiceMonitors() {
        return getPollerConfig().getServiceMonitors();
    }

    int getServiceIdByName(String svcName) {
        Integer id = (Integer) m_svcNameToId.get(svcName);
        return (id == null ? -1 : id.intValue());
    }

    String getServiceNameById(int svcId) {
        return (String) m_svcIdToName.get(new Integer(svcId));
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
    public void setDbConnectionFactory(DbConnectionFactory dbConnectionFactory) {
        m_dbConnectionFactory = dbConnectionFactory;
    }

    public Event createEvent(String uei, int nodeId, InetAddress address, String svcName, java.util.Date date) {
        Category log = ThreadCategory.getInstance(PollableNode.class);
    
        if (log.isDebugEnabled())
            log.debug("createEvent: uei = " + uei + " nodeid = " + nodeId);
    
        // create the event to be sent
        Event newEvent = new Event();
        newEvent.setUei(uei);
        newEvent.setSource(Poller.EVENT_SOURCE);
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
    
            // Add appropriate parms
            Parms eventParms = new Parms();
    
            // Add nodelabel parm
            Parm eventParm = new Parm();
            eventParm.setParmName(EventConstants.PARM_NODE_LABEL);
            Value parmValue = new Value();
            parmValue.setContent(nodeLabel);
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);
    
            // Add Parms to the event
            newEvent.setParms(eventParms);
        }
    
        return newEvent;
    }

}
