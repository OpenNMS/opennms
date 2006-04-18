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
// 2004 Dec 27: Changed SQL_RETRIEVE_INTERFACES to omit interfaces that have been
//              marked as deleted.
// 2004 Feb 12: Rebuild the package to ip list mapping while a new discoveried interface
//              to be scheduled.
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

package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.capsd.InsufficientInformationException;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.daemon.ServiceDaemon;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.jdbc.IpInterfaceDaoJdbc;
import org.opennms.netmgt.dao.jdbc.MonitoredServiceDaoJdbc;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.protocols.ip.IPv4Address;

public final class Collectd extends ServiceDaemon implements EventListener {
    /**
     * Log4j category
     */
    private final static String LOG4J_CATEGORY = "OpenNMS.Collectd";

    /**
     * Singleton instance of the Collectd class
     */
    private final static Collectd m_singleton = new Collectd();

    /**
     * List of all CollectableService objects.
     */
    private List m_collectableServices;

    /**
     * Reference to the collection scheduler
     */
    private Scheduler m_scheduler;

    /**
     * Indicates if scheduling of existing interfaces has been completed
     */
    private boolean m_schedulingCompleted = false;

	private CollectorConfigDaoImpl m_collectorConfigDao;

	private MonitoredServiceDao m_monSvcDao;

	private IpInterfaceDao m_ifSvcDao;

    /**
     * Constructor.
     */
    private Collectd() {
        m_scheduler = null;
        setStatus(START_PENDING);
        
        m_collectableServices = Collections.synchronizedList(new LinkedList());
    }

    /**
     * Responsible for starting the collection daemon.
     */
    public synchronized void init() {
        // Set the category prefix
        log().debug("init: Initializing collection daemon");

        m_collectorConfigDao = new CollectorConfigDaoImpl();
        m_monSvcDao = new MonitoredServiceDaoJdbc(DataSourceFactory.getInstance());
        m_ifSvcDao = new IpInterfaceDaoJdbc(DataSourceFactory.getInstance());
        
        createScheduler();
        m_scheduler.schedule(ifScheduler(), 0);
        
        
        installMessageSelectors();

    }

	Category log() {
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		return ThreadCategory.getInstance();
	}

	private void installMessageSelectors() {
		// Create the JMS selector for the ueis this service is interested in
		//
		List ueiList = new ArrayList();

		// nodeGainedService
		ueiList.add(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI);

		// primarySnmpInterfaceChanged
		ueiList.add(EventConstants.PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI);

		// reinitializePrimarySnmpInterface
		ueiList.add(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI);

		// interfaceReparented
		ueiList.add(EventConstants.INTERFACE_REPARENTED_EVENT_UEI);

		// nodeDeleted
		ueiList.add(EventConstants.NODE_DELETED_EVENT_UEI);

		// duplicateNodeDeleted
		ueiList.add(EventConstants.DUP_NODE_DELETED_EVENT_UEI);

		// interfaceDeleted
		ueiList.add(EventConstants.INTERFACE_DELETED_EVENT_UEI);

		// serviceDeleted
		ueiList.add(EventConstants.SERVICE_DELETED_EVENT_UEI);

		// outageConfigurationChanged
		ueiList.add(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI);

		// configureSNMP
		ueiList.add(EventConstants.CONFIGURE_SNMP_EVENT_UEI);

		EventIpcManagerFactory.getIpcManager().addEventListener(this, ueiList);
	}

    private ReadyRunnable ifScheduler() {
        // Schedule existing interfaces for data collection

        ReadyRunnable interfaceScheduler = new ReadyRunnable() {

            public boolean isReady() {
                return true;
            }

            public void run() {
                try {
                    scheduleExistingInterfaces();
                } catch (SQLException e) {
                    log().error("start: Failed to schedule existing interfaces", e);
                } finally {
                    setSchedulingCompleted(true);
                }

            }
        };
        return interfaceScheduler;
    }

    private void createScheduler() {
        // Create a scheduler
        try {
            log().debug("init: Creating collectd scheduler");

            m_scheduler = new Scheduler("Collectd", m_collectorConfigDao.getSchedulerThreads());
        } catch (RuntimeException e) {
            log().fatal("init: Failed to create collectd scheduler", e);
            throw e;
        }
    }

	/**
     * Responsible for starting the collection daemon.
     */
    public synchronized void start() {
        setStatus(STARTING);

        log().debug("start: Initializing collection daemon");

        // start the scheduler
        try {
            log().debug("start: Starting collectd scheduler");

            m_scheduler.start();
        } catch (RuntimeException e) {
            log().fatal("start: Failed to start scheduler", e);
            throw e;
        }

        // Set the status of the service as running.
        //
        setStatus(RUNNING);

        log().debug("start: Collectd running");
    }

    /**
     * Responsible for stopping the collection daemon.
     */
    public synchronized void stop() {
        setStatus(STOP_PENDING);
        m_scheduler.stop();
        deinstallMessageSelectors();

        m_scheduler = null;
        setStatus(STOPPED);
        log().debug("stop: Collectd stopped");
    }

    /**
     * Responsible for pausing the collection daemon.
     */
    public synchronized void pause() {
        if (!isRunning()) {
            return;
        }

        setStatus(PAUSE_PENDING);
        m_scheduler.pause();
        setStatus(PAUSED);

        log().debug("pause: Collectd paused");
    }

    /**
     * Responsible for resuming the collection daemon.
     */
    public synchronized void resume() {
        if (!isPaused()) {
            return;
        }

        setStatus(RESUME_PENDING);
        m_scheduler.resume();
        setStatus(RUNNING);

        log().debug("resume: Collectd resumed");
    }

    /**
     * Return sthe name of the collection daemon.
     */
    public String getName() {
        return "OpenNMS.Collectd";
    }

    /**
     * Returns singleton instance of the collection daemon.
     */
    public static Collectd getInstance() {
        return m_singleton;
    }

    /**
     * Returns reference to the scheduler
     */
    public Scheduler getScheduler() {
        return m_scheduler;
    }

    /**
     * Returns the loaded ServiceCollector for the specified service name.
     * 
     * @param svcName
     *            Service name to lookup.
     * 
     * @return ServiceCollector responsible for performing data collection on
     *         the specified service.
     */
    public ServiceCollector getServiceCollector(String svcName) {
    		return m_collectorConfigDao.getServiceCollector(svcName);
    }

    /**
     * Schedule existing interfaces for data collection.
     * 
     * @throws SQLException
     *             if database errors encountered.
     */
    private void scheduleExistingInterfaces() throws SQLException {
    	// Loop through collectors and schedule for each one present
    	for (Iterator it = getCollectorNames().iterator(); it.hasNext();) {
    		scheduleInterfacesWithService((String) it.next());
		}
    }

	private void scheduleInterfacesWithService(String svcName) {
		log().debug("scheduleExistingInterfaces: svcName = " + svcName);

		Collection ifsWithServices = m_ifSvcDao.findByServiceType(svcName);
		for (Iterator it = ifsWithServices.iterator(); it.hasNext();) {
			OnmsIpInterface iface = (OnmsIpInterface) it.next();
			scheduleInterface(iface, svcName, true);
		}
	}

	private Set getCollectorNames() {
		return m_collectorConfigDao.getCollectorNames();
	}

    /**
     * This method is responsible for scheduling the specified
     * node/address/svcname tuple for data collection.
     * 
     * @param nodeId
     *            Node id
     * @param ipAddress
     *            IP address
     * @param svcName
     *            Service name
     * @param existing
     *            True if called by scheduleExistingInterfaces(), false
     *            otheriwse
     */
    void scheduleInterface(int nodeId, String ipAddress, String svcName, boolean existing) {
    	OnmsMonitoredService svc = m_monSvcDao.get(nodeId, ipAddress, svcName);
    	scheduleInterface(svc.getIpInterface(), svc.getServiceType().getName(), existing);
    }
    	
    void scheduleInterface(OnmsIpInterface iface, String svcName, OnmsMonitoredService x, boolean existing) {
		scheduleInterface(iface, svcName, existing);
	}

	void scheduleInterface(OnmsIpInterface iface, String svcName, boolean existing) {
        Collection matchingPkgs = m_collectorConfigDao.getSpecificationsForInterface(iface, svcName);
        
        for (Iterator it = matchingPkgs.iterator(); it.hasNext();) {
			CollectionSpecification spec = (CollectionSpecification) it.next();
			
            if (existing == false) {
                /*
                 * It is possible that both a nodeGainedService and a
                 * primarySnmpInterfaceChanged event are generated for an
                 * interface during a rescan. To handle this scenario we must
                 * verify that the ipAddress/pkg pair identified by this event
                 * does not already exist in the collectable services list.
                 */
                if (alreadyScheduled(iface, spec)) {
                    if (log().isDebugEnabled()) {
                        log().debug("scheduleInterface: svc/pkgName "
                                  + iface + '/' + svcName + "/" + spec.getPackageName()
                                  + " already in collectable service list, "
                                  + "skipping.");
                    }
                    continue;
                }
            }

            try {
                /*
                 * Criteria checks have all passed. The interface/service pair
                 * can be scheduled.
                 */
                CollectableService cSvc = null;

                /*
                 * Create a new SnmpCollector object representing this node,
                 * interface,
                 * service and package pairing
                 */
                cSvc = new CollectableService(iface, spec);

                // Add new collectable service to the colleable service list.
                m_collectableServices.add(cSvc);

                // Schedule the collectable service for immediate collection
                m_scheduler.schedule(cSvc, 0);

                if (log().isDebugEnabled()) {
                    log().debug("scheduleInterface: " + iface +'/' + svcName + " collection");
                }
            } catch (RuntimeException rE) {
                log().warn("scheduleInterface: Unable to schedule " + iface +'/' + svcName + ", reason: "
                         + rE.getMessage());
            } catch (Throwable t) {
                log().error("scheduleInterface: Uncaught exception, failed to "
                          + "schedule interface " + iface +'/' + svcName + ".", t);
            }
        } // end while more packages exist
    }

	/**
     * Returns true if specified address/pkg pair is already represented in the
     * collectable services list. False otherwise.
	 * @param iface TODO
	 * @param spec TODO
	 * @param svcName TODO
     */
    private boolean alreadyScheduled(OnmsIpInterface iface, CollectionSpecification spec) {
    	String ipAddress = iface.getIpAddress();
    	String svcName = spec.getServiceName();
    	String pkgName = spec.getPackageName();
        synchronized (m_collectableServices) {
            CollectableService cSvc = null;
            Iterator iter = m_collectableServices.iterator();
            while (iter.hasNext()) {
                cSvc = (CollectableService) iter.next();

                InetAddress addr = (InetAddress) cSvc.getAddress();
                if (addr.getHostAddress().equals(ipAddress) 
                    && cSvc.getPackageName().equals(pkgName)
                    && cSvc.getServiceName().equals(svcName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @return Returns the schedulingCompleted.
     */
    public boolean isSchedulingCompleted() {
        return m_schedulingCompleted;
    }

    /**
     * @param schedulingCompleted
     *            The schedulingCompleted to set.
     */
    public void setSchedulingCompleted(boolean schedulingCompleted) {
        m_schedulingCompleted = schedulingCompleted;
    }


	public void refreshServicePackages() {
	    Iterator serviceIterator=m_collectableServices.iterator();
	    while (serviceIterator.hasNext()) {
	        CollectableService thisService =
                (CollectableService) serviceIterator.next();
	        thisService.refreshPackage();
	    }
	}
	
	public List getCollectableServices() {
		return m_collectableServices;
	}

	/**
	 * This method is invoked by the JMS topic session when a new event is
	 * available for processing. Currently only text based messages are
	 * processed by this callback. Each message is examined for its Universal
	 * Event Identifier and the appropriate action is taking based on each UEI.
	 * @param event
	 *            The event message.
	 * @param processor TODO
	 * 
	 */
	public void onEvent(Event event) {
		// print out the uei
		//
		log().debug("received event, uei = " + event.getUei());
		
		try {
		if (event.getUei().equals(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI)) {
			handleScheduledOutagesChanged(event);
		} else if (event.getUei().equals(EventConstants.CONFIGURE_SNMP_EVENT_UEI)) {
			handleConfigureSNMP(event);
		} else if (event.getUei().equals(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)) {
			handleNodeGainedService(event);
		} else if (event.getUei().equals(EventConstants.PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI)) {
			handlePrimarySnmpInterfaceChanged(event);
		} else if (event.getUei().equals(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI)) {
			handleReinitializePrimarySnmpInterface(event);
		} else if (event.getUei().equals(EventConstants.INTERFACE_REPARENTED_EVENT_UEI)) {
			handleInterfaceReparented(event);
		} else if (event.getUei().equals(EventConstants.NODE_DELETED_EVENT_UEI)) {
			handleNodeDeleted(event);
		} else if (event.getUei().equals(EventConstants.DUP_NODE_DELETED_EVENT_UEI)) { 
			handleDupNodeDeleted(event);
		} else if (event.getUei().equals(EventConstants.INTERFACE_DELETED_EVENT_UEI)) {
			handleInterfaceDeleted(event);
		} else if (event.getUei().equals(EventConstants.SERVICE_DELETED_EVENT_UEI)) {
			handleServiceDeleted(event);
		}
		} catch (InsufficientInformationException e) {
			log().info(e.getMessage());
		}

	}

	private void handleDupNodeDeleted(Event event) throws InsufficientInformationException {
		handleNodeDeleted(event);
	}

	private void handleScheduledOutagesChanged(Event event) {
		try {
			log().info("Reloading Collectd config factory");
			CollectdConfigFactory.reload();
			refreshServicePackages();
		} catch (Exception e) {
			log().error("Failed to reload CollectdConfigFactory because "+e.getMessage(), e);
		}
	}

	/**
	 * </p>
	 * Closes the current connections to the Java Message Queue if they are
	 * still active. This call may be invoked more than once safely and may be
	 * invoked during object finalization.
	 * </p>
	 * 
	 */
	void deinstallMessageSelectors() {
	    EventIpcManagerFactory.getIpcManager().removeEventListener(this);
	}

	/**
	 * This method is responsible for handling configureSNMP events.
	 *
	 * @param event The event to process.
	 */
	void handleConfigureSNMP(Event event) {
	    if (log().isDebugEnabled())
	        log().debug("configureSNMPHandler: processing configure SNMP event...");
	
	    // Extract the IP adddress range and SNMP community string from the
	    // event parms.
	    //
	    String firstIPAddress = null;
	    String lastIPAddress = null;
	    String communityString = null;
	    Parms parms = event.getParms();
	    if (parms != null) {
	        String parmName = null;
	        Value parmValue = null;
	        String parmContent = null;
	
	        Enumeration parmEnum = parms.enumerateParm();
	        while (parmEnum.hasMoreElements()) {
	            Parm parm = (Parm) parmEnum.nextElement();
	            parmName = parm.getParmName();
	            parmValue = parm.getValue();
	            if (parmValue == null)
	                continue;
	            else
	                parmContent = parmValue.getContent();
	
	            // First IP Address
	            if (parmName.equals(EventConstants.PARM_FIRST_IP_ADDRESS)) {
	                firstIPAddress = parmContent;
	            }
	
	            // Last IP Address (optional parameter)
	            else if (parmName.equals(EventConstants.PARM_LAST_IP_ADDRESS)) {
	                lastIPAddress = parmContent;
	            }
	
	            // SNMP community string
	            else if (parmName.equals(EventConstants.PARM_COMMUNITY_STRING)) {
	                communityString = parmContent;
	            }
	        }
	    }
	
	    if (firstIPAddress != null && !firstIPAddress.equals("")) {
	        int begin = new IPv4Address(firstIPAddress).getAddress();
	        int end = begin;
	        if (lastIPAddress != null && !lastIPAddress.equals("")) {
	            end = new IPv4Address(lastIPAddress).getAddress();
	            if (end < begin)
	                end = begin;
	        }
	
	        SnmpPeerFactory factory = SnmpPeerFactory.getInstance();
	
	        for (int address = begin; address <= end; address++) {
	            try {
	                InetAddress ip =
	                    InetAddress.getByAddress(new IPv4Address(address).getAddressBytes());
	
	                factory.define(ip, communityString);
	            }
	            catch (Exception e) {
	                log().warn("configureSNMPHandler: Failed to process IP address "
	                         + IPv4Address.addressToString(address)
	                         + ": " + e.getMessage(), e);
	            }
	        }
	
	        try {
	            SnmpPeerFactory.saveCurrent();
	        }
	        catch (Exception e) {
	            log().warn("configureSNMPHandler: Failed to store SNMP configuration"
	                     + ": " + e.getMessage(), e);
	        }
	    }
	
	    if (log().isDebugEnabled())
	        log().debug("configureSNMPHandler: processing configure SNMP event for IP "
	                  + firstIPAddress + "-" + lastIPAddress + " completed.");
	}

	/**
	 * This method is responsible for handling interfaceDeleted events.
	 * 
	 * @param event
	 *            The event to process.
	 * @throws InsufficientInformationException 
	 * 
	 */
	private void handleInterfaceDeleted(Event event) throws InsufficientInformationException {
		EventUtils.checkNodeId(event);
		EventUtils.checkInterface(event);

	    Category log = log();
	
	    int nodeId = (int) event.getNodeid();
	    String ipAddr = event.getInterface();
	
	    // Iterate over the collectable services list and mark any entries
	    // which match the deleted nodeId/IP address pair for deletion
	    synchronized (getCollectableServices()) {
	        CollectableService cSvc = null;
	        ListIterator liter = getCollectableServices().listIterator();
	        while (liter.hasNext()) {
	            cSvc = (CollectableService) liter.next();
	
	            // Only interested in entries with matching nodeId and IP
	            // address
	            InetAddress addr = (InetAddress) cSvc.getAddress();
	            if (!(cSvc.getNodeId() == nodeId && addr.getHostName().equals(ipAddr)))
	                continue;
	
	            synchronized (cSvc) {
	                // Retrieve the CollectorUpdates object associated with
	                // this CollectableService if one exists.
	                CollectorUpdates updates = cSvc.getCollectorUpdates();
	
	                // Now set the update's deletion flag so the next
	                // time it is selected for execution by the scheduler
	                // the collection will be skipped and the service will not
	                // be rescheduled.
	                updates.markForDeletion();
	            }
	
	            // Now safe to remove the collectable service from
	            // the collectable services list
	            liter.remove();
	        }
	    }
	
	    if (log.isDebugEnabled())
	        log.debug("interfaceDeletedHandler: processing of interfaceDeleted event for " + nodeId + "/" + ipAddr + " completed.");
	}

	/**
	 * This method is responsible for processing 'interfacReparented' events. An
	 * 'interfaceReparented' event will have old and new nodeId parms associated
	 * with it. All CollectableService objects in the service updates map which
	 * match the event's interface address and the SNMP service have a
	 * reparenting update associated with them. When the scheduler next pops one
	 * of these services from an interval queue for collection all of the RRDs
	 * associated with the old nodeId are moved under the new nodeId and the
	 * nodeId of the collectable service is updated to reflect the interface's
	 * new parent nodeId.
	 * 
	 * @param event
	 *            The event to process.
	 * @throws InsufficientInformationException 
	 * 
	 */
	void handleInterfaceReparented(Event event) throws InsufficientInformationException {
		EventUtils.checkNodeId(event);
		EventUtils.checkInterface(event);

	    Category log = log();
	    if (log.isDebugEnabled())
	        log.debug("interfaceReparentedHandler:  processing interfaceReparented event for " + event.getInterface());
	
	    // Verify that the event has an interface associated with it
	    if (event.getInterface() == null)
	        return;
	
	    // Extract the old and new nodeId's from the event parms
	    String oldNodeIdStr = null;
	    String newNodeIdStr = null;
	    Parms parms = event.getParms();
	    if (parms != null) {
	        String parmName = null;
	        Value parmValue = null;
	        String parmContent = null;
	
	        Enumeration parmEnum = parms.enumerateParm();
	        while (parmEnum.hasMoreElements()) {
	            Parm parm = (Parm) parmEnum.nextElement();
	            parmName = parm.getParmName();
	            parmValue = parm.getValue();
	            if (parmValue == null)
	                continue;
	            else
	                parmContent = parmValue.getContent();
	
	            // old nodeid
	            if (parmName.equals(EventConstants.PARM_OLD_NODEID)) {
	                oldNodeIdStr = parmContent;
	            }
	
	            // new nodeid
	            else if (parmName.equals(EventConstants.PARM_NEW_NODEID)) {
	                newNodeIdStr = parmContent;
	            }
	        }
	    }
	
	    // Only proceed provided we have both an old and a new nodeId
	    //
	    if (oldNodeIdStr == null || newNodeIdStr == null) {
	        log.warn("interfaceReparentedHandler: old and new nodeId parms are required, unable to process.");
	        return;
	    }
	
	    // Iterate over the CollectableService objects in the services
	    // list looking for entries which share the same interface
	    // address as the reparented interface. Mark any matching objects
	    // for reparenting.
	    //
	    // The next time the service is scheduled for execution it
	    // will move all of the RRDs associated
	    // with the old nodeId under the new nodeId and update the service's
	    // SnmpMonitor.NodeInfo attribute to reflect the new nodeId. All
	    // subsequent collections will then be updating the appropriate RRDs.
	    //
	    synchronized (getCollectableServices()) {
	        CollectableService cSvc = null;
	        Iterator iter = getCollectableServices().iterator();
	        while (iter.hasNext()) {
	            cSvc = (CollectableService) iter.next();
	
	            InetAddress addr = (InetAddress) cSvc.getAddress();
	            if (addr.getHostAddress().equals(event.getInterface())) {
	                synchronized (cSvc) {
	                    // Got a match!
	                    if (log.isDebugEnabled())
	                        log.debug("interfaceReparentedHandler: got a CollectableService match for " + event.getInterface());
	
	                    // Retrieve the CollectorUpdates object associated with
	                    // this CollectableService.
	                    CollectorUpdates updates = cSvc.getCollectorUpdates();
	
	                    // Now set the reparenting flag
	                    updates.markForReparenting(oldNodeIdStr, newNodeIdStr);
	                    if (log.isDebugEnabled())
	                        log.debug("interfaceReparentedHandler: marking " + event.getInterface() + " for reparenting for service SNMP.");
	                }
	            }
	        }
	    }
	
	    if (log.isDebugEnabled())
	        log.debug("interfaceReparentedHandler: processing of interfaceReparented event for interface " + event.getInterface() + " completed.");
	}

	/**
	 * This method is responsible for handling nodeDeleted events.
	 * 
	 * @param event
	 *            The event to process.
	 * @throws InsufficientInformationException 
	 * 
	 */
	void handleNodeDeleted(Event event) throws InsufficientInformationException {
		EventUtils.checkNodeId(event);
		EventUtils.checkInterface(event);

	    Category log = log();
	
	    int nodeId = (int) event.getNodeid();
	
	    // Iterate over the collectable service list and mark any entries
	    // which match the deleted nodeId for deletion.
	    synchronized (getCollectableServices()) {
	        CollectableService cSvc = null;
	        ListIterator liter = getCollectableServices().listIterator();
	        while (liter.hasNext()) {
	            cSvc = (CollectableService) liter.next();
	
	            // Only interested in entries with matching nodeId
	            if (!(cSvc.getNodeId() == nodeId))
	                continue;
	
	            synchronized (cSvc) {
	                // Retrieve the CollectorUpdates object associated
	                // with this CollectableService.
	                CollectorUpdates updates = cSvc.getCollectorUpdates();
	
	                // Now set the update's deletion flag so the next
	                // time it is selected for execution by the scheduler
	                // the collection will be skipped and the service will not
	                // be rescheduled.
	                updates.markForDeletion();
	            }
	
	            // Now safe to remove the collectable service from
	            // the collectable services list
	            liter.remove();
	        }
	    }
	
	    if (log.isDebugEnabled())
	        log.debug("nodeDeletedHandler: processing of nodeDeleted event for nodeid " + nodeId + " completed.");
	}

	/**
	 * Process the event, construct a new CollectableService object representing
	 * the node/interface combination, and schedule the interface for
	 * collection.
	 * 
	 * If any errors occur scheduling the interface no error is returned.
	 * 
	 * @param event
	 *            The event to process.
	 * @throws InsufficientInformationException 
	 * 
	 */
	void handleNodeGainedService(Event event) throws InsufficientInformationException {
		EventUtils.checkNodeId(event);
		EventUtils.checkInterface(event);
		EventUtils.checkService(event);
	    // Schedule the interface
	    //
	    scheduleForCollection(event);
	}

	void scheduleForCollection(Event event) {
	    //This moved to here from the scheduleInterface() for better behavior during initialization
	    CollectdConfigFactory cCfgFactory = CollectdConfigFactory.getInstance();
	    cCfgFactory.rebuildPackageIpListMap();
	
	    scheduleInterface((int) event.getNodeid(), event.getInterface(), event.getService(), false);
	}

	/**
	 * Process the 'primarySnmpInterfaceChanged' event.
	 * 
	 * Extract the old and new primary SNMP interface addresses from the event
	 * parms. Any CollectableService objects located in the collectable services
	 * list which match the IP address of the old primary interface and have a
	 * service name of "SNMP" are flagged for deletion. This will ensure that
	 * the old primary interface is no longer collected against.
	 * 
	 * Finally the new primary SNMP interface is scheduled. The packages are
	 * examined and new CollectableService objects are created, initialized and
	 * scheduled for collection.
	 * 
	 * @param event
	 *            The event to process.
	 * @throws InsufficientInformationException 
	 * 
	 */
	void handlePrimarySnmpInterfaceChanged(Event event) throws InsufficientInformationException {
		EventUtils.checkNodeId(event);
		EventUtils.checkInterface(event);

	    Category log = log();
	
	    if (log.isDebugEnabled())
	        log.debug("primarySnmpInterfaceChangedHandler:  processing primary SNMP interface changed event...");
	
	    // Currently only support SNMP data collection.
	    //
	    if (!event.getService().equals("SNMP"))
	        return;
	
	    // Extract the old and new primary SNMP interface adddresses from the
	    // event parms.
	    //
	    String oldPrimaryIfAddr = null;
	    String newPrimaryIfAddr = null;
	    Parms parms = event.getParms();
	    if (parms != null) {
	        String parmName = null;
	        Value parmValue = null;
	        String parmContent = null;
	
	        Enumeration parmEnum = parms.enumerateParm();
	        while (parmEnum.hasMoreElements()) {
	            Parm parm = (Parm) parmEnum.nextElement();
	            parmName = parm.getParmName();
	            parmValue = parm.getValue();
	            if (parmValue == null)
	                continue;
	            else
	                parmContent = parmValue.getContent();
	
	            // old primary SNMP interface (optional parameter)
	            if (parmName.equals(EventConstants.PARM_OLD_PRIMARY_SNMP_ADDRESS)) {
	                oldPrimaryIfAddr = parmContent;
	            }
	
	            // old primary SNMP interface (optional parameter)
	            else if (parmName.equals(EventConstants.PARM_NEW_PRIMARY_SNMP_ADDRESS)) {
	                newPrimaryIfAddr = parmContent;
	            }
	        }
	    }
	
	    if (oldPrimaryIfAddr != null) {
	        // Mark the service for deletion so that it will not be rescheduled
	        // for
	        // collection.
	        //
	        // Iterate over the CollectableService objects in the service
	        // updates map
	        // and mark any which have the same interface address as the old
	        // primary SNMP interface and a service name of "SNMP" for deletion.
	        //
	        synchronized (getCollectableServices()) {
	            CollectableService cSvc = null;
	            ListIterator liter = getCollectableServices().listIterator();
	            while (liter.hasNext()) {
	                cSvc = (CollectableService) liter.next();
	
	                InetAddress addr = (InetAddress) cSvc.getAddress();
	                if (addr.getHostAddress().equals(oldPrimaryIfAddr)) {
	                    synchronized (cSvc) {
	                        // Got a match! Retrieve the CollectorUpdates object
	                        // associated
	                        // with this CollectableService.
	                        CollectorUpdates updates = cSvc.getCollectorUpdates();
	
	                        // Now set the deleted flag
	                        updates.markForDeletion();
	                        if (log.isDebugEnabled())
	                            log.debug("primarySnmpInterfaceChangedHandler: marking " + oldPrimaryIfAddr + " as deleted for service SNMP.");
	                    }
	
	                    // Now safe to remove the collectable service from
	                    // the collectable services list
	                    liter.remove();
	                }
	            }
	        }
	    }
	
	    // Now we can schedule the new service...
	    //
	    scheduleForCollection(event);
	
	    if (log.isDebugEnabled())
	        log.debug("primarySnmpInterfaceChangedHandler: processing of primarySnmpInterfaceChanged event for nodeid " + event.getNodeid() + " completed.");
	}

	/**
	 * Process the event.
	 * 
	 * This event is generated when a managed node which supports SNMP gains a
	 * new interface. In this situation the CollectableService object
	 * representing the primary SNMP interface of the node must be
	 * reinitialized.
	 * 
	 * The CollectableService object associated with the primary SNMP interface
	 * for the node will be marked for reinitialization. Reinitializing the
	 * CollectableService object consists of calling the
	 * ServiceCollector.release() method followed by the
	 * ServiceCollector.initialize() method which will refresh attributes such
	 * as the interface key list and number of interfaces (both of which most
	 * likely have changed).
	 * 
	 * Reinitialization will take place the next time the CollectableService is
	 * popped from an interval queue for collection.
	 * 
	 * If any errors occur scheduling the service no error is returned.
	 * 
	 * @param event
	 *            The event to process.
	 * @throws InsufficientInformationException 
	 */
	void handleReinitializePrimarySnmpInterface(Event event) throws InsufficientInformationException {
		EventUtils.checkNodeId(event);
		EventUtils.checkInterface(event);

	    Category log = log();
	
	    if (event.getInterface() == null) {
	        log.error("reinitializePrimarySnmpInterface event is missing an interface.");
	        return;
	    }
	
	    // Mark the primary SNMP interface for reinitialization in
	    // order to update any modified attributes associated with
	    // the collectable service..
	    //
	    // Iterate over the CollectableService objects in the
	    // updates map and mark any which have the same interface
	    // address for reinitialization
	    //
	    synchronized (getCollectableServices()) {
	        Iterator iter = getCollectableServices().iterator();
	        while (iter.hasNext()) {
	            CollectableService cSvc = (CollectableService) iter.next();
	
	            InetAddress addr = (InetAddress) cSvc.getAddress();
	            if (log.isDebugEnabled())
	                log.debug("Comparing CollectableService ip address = " + addr.getHostAddress() + " and event ip interface = " + event.getInterface());
	            if (addr.getHostAddress().equals(event.getInterface())) {
	                synchronized (cSvc) {
	                    // Got a match! Retrieve the CollectorUpdates object
	                    // associated
	                    // with this CollectableService.
	                    CollectorUpdates updates = cSvc.getCollectorUpdates();
	
	                    // Now set the reinitialization flag
	                    updates.markForReinitialization();
	                    if (log.isDebugEnabled())
	                        log.debug("reinitializePrimarySnmpInterfaceHandler: marking " + event.getInterface() + " for reinitialization for service SNMP.");
	                }
	            }
	        }
	    }
	}

	/**
	 * This method is responsible for handling serviceDeleted events.
	 * 
	 * @param event
	 *            The event to process.
	 * @throws InsufficientInformationException 
	 * 
	 */
	void handleServiceDeleted(Event event) throws InsufficientInformationException {
		EventUtils.checkNodeId(event);
		EventUtils.checkInterface(event);
		EventUtils.checkService(event);

	    Category log = log();
	
	    // Currently only support SNMP data collection.
	    //
	    if (!event.getService().equals("SNMP"))
	        return;
	
	    int nodeId = (int) event.getNodeid();
	    String ipAddr = event.getInterface();
	    String svcName = event.getService();
	
	    // Iterate over the collectable services list and mark any entries
	    // which match the nodeId/ipAddr of the deleted service
	    // for deletion.
	    synchronized (getCollectableServices()) {
	        CollectableService cSvc = null;
	        ListIterator liter = getCollectableServices().listIterator();
	        while (liter.hasNext()) {
	            cSvc = (CollectableService) liter.next();
	
	            // Only interested in entries with matching nodeId, IP address
	            // and service
	            InetAddress addr = (InetAddress) cSvc.getAddress();
	            if (!(cSvc.getNodeId() == nodeId && addr.getHostName().equals(ipAddr)) && cSvc.getServiceName().equals(svcName))
	                continue;
	
	            synchronized (cSvc) {
	                // Retrieve the CollectorUpdates object associated with
	                // this CollectableService if one exists.
	                CollectorUpdates updates = cSvc.getCollectorUpdates();
	
	                // Now set the update's deletion flag so the next
	                // time it is selected for execution by the scheduler
	                // the collection will be skipped and the service will not
	                // be rescheduled.
	                updates.markForDeletion();
	            }
	
	            // Now safe to remove the collectable service from
	            // the collectable services list
	            liter.remove();
	        }
	    }
	
	    if (log.isDebugEnabled())
	        log.debug("serviceDeletedHandler: processing of serviceDeleted event for " + nodeId + "/" + ipAddr + "/" + svcName + " completed.");
	}
}
