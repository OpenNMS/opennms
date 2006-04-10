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

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.daemon.ServiceDaemon;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.jdbc.MonitoredServiceDaoJdbc;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;

public final class Collectd extends ServiceDaemon {
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
     * Reference to the event processor
     */
    private BroadcastEventProcessor m_receiver;

    /**
     * Indicates if scheduling of existing interfaces has been completed
     * 
     */
    private boolean m_schedulingCompleted = false;

	private CollectorConfigDaoImpl m_collectorConfigDao;

	private ScheduledOutagesDaoImpl m_scheduledOutagesDao;

	private MonitoredServiceDao m_monSvcDao;

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
        m_scheduledOutagesDao = new ScheduledOutagesDaoImpl();
        m_monSvcDao = new MonitoredServiceDaoJdbc(DatabaseConnectionFactory.getInstance());
        
        log().debug("init: Loading collectors");

        createScheduler();
        ReadyRunnable interfaceScheduler = buildSchedule();
        m_scheduler.schedule(interfaceScheduler, 0);
        createEventProcessor();

    }

	Category log() {
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		return ThreadCategory.getInstance();
	}

    private void createEventProcessor() {
        /*
         * Create an event receiver. The receiver will
         * receive events, process them, creates network
         * interfaces, and schedulers them.
         */
        try {
            log().debug("init: Creating event broadcast event processor");

            m_receiver = new BroadcastEventProcessor(m_collectableServices);
        } catch (Throwable t) {
            log().fatal("init: Failed to initialized the broadcast event receiver", t);
            throw new UndeclaredThrowableException(t);
        }
    }

    private ReadyRunnable buildSchedule() {
        // Schedule existing interfaces for data collection

        ReadyRunnable interfaceScheduler = new ReadyRunnable() {

            public boolean isReady() {
                return true;
            }

            public void run() {
                try {
                    scheduleExistingInterfaces();
                } catch (SQLException sqlE) {
                    log().error("start: Failed to schedule existing interfaces",
                              sqlE);
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
        m_receiver.close();

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
    	Set svcNames = getCollectorNames();
    	Iterator i = svcNames.iterator();
    	while (i.hasNext()) {
    		String svcName = (String) i.next();

    		log().debug("scheduleExistingInterfaces: svcName = " + svcName);

    		Collection services = m_monSvcDao.findByType(svcName);
    		for (Iterator it = services.iterator(); it.hasNext();) {
    			OnmsMonitoredService svc = (OnmsMonitoredService) it.next();
    			boolean existing = true;
				scheduleInterface(svc, existing);
    		}

    	} // end while more service collectors
    }

	private Set getCollectorNames() {
		return m_collectorConfigDao.getCollectorName();
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
    	scheduleInterface(svc, existing);
    }
    	
    void scheduleInterface(OnmsMonitoredService svc, boolean existing) {
        Collection matchingPkgs = m_collectorConfigDao.getPackagesForService(svc);
        
        for (Iterator it = matchingPkgs.iterator(); it.hasNext();) {
			Package pkg = (Package) it.next();
			
            if (existing == false) {
                /*
                 * It is possible that both a nodeGainedService and a
                 * primarySnmpInterfaceChanged event are generated for an
                 * interface during a rescan. To handle this scenario we must
                 * verify that the ipAddress/pkg pair identified by this event
                 * does not already exist in the collectable services list.
                 */
                if (alreadyScheduled(svc, pkg.getName())) {
                    if (log().isDebugEnabled()) {
                        log().debug("scheduleInterface: svc/pkgName "
                                  + svc + "/" + pkg.getName()
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
                cSvc = new CollectableService(svc.getNodeId().intValue(),
                                              InetAddress.getByName(svc.getIpAddress()),
                                              svc.getServiceType().getName(), pkg);

                // Initialize the collector with the collectable service.
                ServiceCollector collector = this.getServiceCollector(svc.getServiceType().getName());
                collector.initialize(cSvc, cSvc.getPropertyMap());

                // Add new collectable service to the colleable service list.
                m_collectableServices.add(cSvc);

                // Schedule the collectable service for immediate collection
                m_scheduler.schedule(cSvc, 0);

                if (log().isDebugEnabled()) {
                    log().debug("scheduleInterface: " + svc + " collection");
                }
            } catch (UnknownHostException ex) {
                log().error("scheduleInterface: Failed to schedule interface "
                          + svc + ", illegal address", ex);
            } catch (RuntimeException rE) {
                log().warn("scheduleInterface: Unable to schedule " + svc + ", reason: "
                         + rE.getMessage());
            } catch (Throwable t) {
                log().error("scheduleInterface: Uncaught exception, failed to "
                          + "schedule interface " + svc + ".", t);
            }
        } // end while more packages exist
    }

	/**
     * Returns true if specified address/pkg pair is already represented in the
     * collectable services list. False otherwise.
     * @param svc TODO
     */
    private boolean alreadyScheduled(OnmsMonitoredService svc, String pkgName) {
    	String ipAddress = svc.getIpAddress();
    	String svcName = svc.getServiceType().getName();
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
}
