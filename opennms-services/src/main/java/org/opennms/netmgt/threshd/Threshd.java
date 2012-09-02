/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.Querier;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.ThreshdConfigManager;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.Thresholder;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.model.capsd.DbIfServiceEntry;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * <p>Threshd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public final class Threshd extends AbstractServiceDaemon {
    /**
     * SQL used to retrieve all the interfaces which support a particular
     * service.
     * 
     * @see DbIfServiceEntry#STATUS_ACTIVE
     */
    private final static String SQL_RETRIEVE_INTERFACES = "SELECT nodeid,ipaddr FROM ifServices, service WHERE ifServices.status = 'A' AND ifServices.serviceid = service.serviceid AND service.servicename = ?";

    /**
     * Singleton instance of the Threshd class
     */
    private final static Threshd m_singleton = new Threshd();

    /**
     * List of all ThresholdableService objects.
     */
    private final List<ThresholdableService> m_thresholdableServices;

    /**
     * Reference to the threshd scheduler
     */
    private volatile LegacyScheduler m_scheduler;

    /**
     * Indicates if all the existing interfaces have been scheduled
     */
    private volatile boolean m_schedulingCompleted = false;

    /**
     * Reference to the event processor
     */
    private volatile BroadcastEventProcessor m_receiver;

    /**
     * Map of all available ServiceThresholder objects indexed by service name
     */
    private static volatile Map<String, ServiceThresholder> m_svcThresholders;

    private ThreshdConfigManager m_threshdConfig;

    /**
     * Constructor.
     */
    Threshd() {
    	super("OpenNMS.Threshd");
        m_scheduler = null;
        m_svcThresholders = new ConcurrentSkipListMap<String, ServiceThresholder>();
        m_thresholdableServices = Collections.synchronizedList(new LinkedList<ThresholdableService>());
    }

    /**
     * <p>onInit</p>
     */
    protected void onInit() {

        log().debug("start: Initializing thresholding daemon");

        log().debug("start: Loading thresholders");

        // Threshd configuration
        //
        // Load up an instance of each thresholder from the config
        // so that the event processor will have them for
        // new incoming events to create collectable service objects.
        //
        initializeThresholders();

        // Create a scheduler
        //
        initializeScheduler();

        if (log().isDebugEnabled())
            log().debug("start: Scheduling existing interfaces");

        // Schedule existing interfaces for thresholding
        //

        scheduleBackgroundInitTask();

        // Create an event receiver. The receiver will
        // receive events, process them, creates network
        // interfaces, and schedulers them.
        //
        createBroadcastEventProcessor();

    }

    private void createBroadcastEventProcessor() {
        try {
            if (log().isDebugEnabled())
                log().debug("start: Creating event broadcast event processor");

            m_receiver = new BroadcastEventProcessor(this, m_thresholdableServices);
        } catch (Throwable t) {
            if (log().isEnabledFor(ThreadCategory.Level.FATAL))
                log().fatal("start: Failed to initialized the broadcast event receiver", t);

            throw new UndeclaredThrowableException(t);
        }
    }

    private void scheduleBackgroundInitTask() {
        ReadyRunnable interfaceScheduler = new ReadyRunnable() {

            public boolean isReady() {
                return true;
            }

            public void run() {
                //
                try {
                    scheduleExistingInterfaces();
                } catch (DataRetrievalFailureException sqlE) {
                    log().error("start: Failed to schedule existing interfaces", sqlE);
                } finally {
                    setSchedulingCompleted(true);
                }

            }
        };

        m_scheduler.schedule(interfaceScheduler, 0);
    }

    private void initializeScheduler() {
        try {
            log().debug("start: Creating threshd scheduler");

            m_scheduler = new LegacyScheduler("Threshd", m_threshdConfig.getConfiguration().getThreads());
        } catch (RuntimeException e) {
            log().fatal("start: Failed to create threshd scheduler", e);
            throw e;
        }
    }

    private void initializeThresholders() {
        Enumeration<Thresholder> eiter = m_threshdConfig.getConfiguration().enumerateThresholder();
        while (eiter.hasMoreElements()) {
            Thresholder thresholder = eiter.nextElement();
            try {
                if (log().isDebugEnabled()) {
                    log().debug("start: Loading thresholder " + thresholder.getService() + ", classname " + thresholder.getClassName());
                }
                Class<?> tc = Class.forName(thresholder.getClassName());
                ServiceThresholder st = (ServiceThresholder) tc.newInstance();

                // Attempt to initialize the service thresholder
                //

                // Store service name in map keyed by "svcName"
                Map<String, String> properties = new HashMap<String, String>();
                properties.put("svcName", thresholder.getService());

                st.initialize(properties);

                m_svcThresholders.put(thresholder.getService(), st);
            } catch (Throwable t) {
                log().warn("start: Failed to load thresholder " + thresholder.getClassName() + " for service " + thresholder.getService(), t);
            }
        }
    }

    /**
     * <p>reinitializeThresholders</p>
     */
    public void reinitializeThresholders() {
        for(String key: m_svcThresholders.keySet()) {
            ServiceThresholder thresholder=m_svcThresholders.get(key);

            if(log().isDebugEnabled()) {
                log().debug("reinitializeThresholders: About to reinitialize thresholder "+key);
            }
            thresholder.reinitialize();
        }
    }
    
    /**
     * <p>onStart</p>
     */
    protected void onStart() {

        log().debug("start: Initializing thresholding daemon");

        // start the scheduler
        //
        try {
            log().debug("start: Starting threshd scheduler");

            m_scheduler.start();
        } catch (RuntimeException e) {
            log().fatal("start: Failed to start scheduler", e);
            throw e;
        }


        log().debug("start: Threshd running");
	}

    /**
     * <p>onStop</p>
     */
    protected void onStop() {
		m_scheduler.stop();
        m_receiver.close();

        m_scheduler = null;
	}

    /**
     * <p>onPause</p>
     */
    protected void onPause() {
		m_scheduler.pause();
	}

    /**
     * <p>onResume</p>
     */
    protected void onResume() {
		m_scheduler.resume();
	}

    /**
     * Returns singleton instance of the thresholding daemon.
     *
     * @return a {@link org.opennms.netmgt.threshd.Threshd} object.
     */
    public static Threshd getInstance() {
        return m_singleton;
    }

    /**
     * Returns reference to the scheduler
     *
     * @return a {@link org.opennms.netmgt.scheduler.LegacyScheduler} object.
     */
    public LegacyScheduler getScheduler() {
        return m_scheduler;
    }

    /**
     * Returns the loaded ServiceThresholder for the specified service name.
     *
     * @param svcName
     *            Service name to lookup.
     * @return ServiceThresholder responsible for performing thresholding on the
     *         specified service.
     */
    public ServiceThresholder getServiceThresholder(String svcName) {
        return m_svcThresholders.get(svcName);
    }

    /**
     * Schedule existing interfaces for thresholding.
     * 
     * @throws SQLException
     *             if database errors encountered.
     */
    private void scheduleExistingInterfaces() {

        // Loop through loaded thresholders and schedule for each one
        // present
        //
        for(final String svcName : m_svcThresholders.keySet()) {

            // find the monitored services for each thresholder and schedule them
            Querier querier = new Querier(DataSourceFactory.getDataSource(), SQL_RETRIEVE_INTERFACES) {

                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    int nodeId = rs.getInt(1);
                    String ipAddress = rs.getString(2);
                    
                    log().debug("Scheduling service nodeId/ipAddress/svcName "+nodeId+'/'+ipAddress+'/'+svcName);
                    scheduleService(nodeId, ipAddress, svcName, true);
                }

            };
            querier.execute(svcName);

        } 
    }

    /**
     * This method is responsible for scheduling the specified
     * node/address/svcname tuple for thresholding.
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
    void scheduleService(int nodeId, String ipAddress, String svcName, boolean existing) {
        Enumeration<org.opennms.netmgt.config.threshd.Package> epkgs = m_threshdConfig.getConfiguration().enumeratePackage();
        
        // Compare interface/service pair against each threshd package
        // For each match, create new ThresholdableService object and
        // schedule it for collection
        //
        while (epkgs.hasMoreElements()) {
            org.opennms.netmgt.config.threshd.Package pkg = epkgs.nextElement();

            // Make certain the the current service is in the package
            // and enabled!
            //
            if (!m_threshdConfig.serviceInPackageAndEnabled(svcName, pkg)) {
                if (log().isDebugEnabled())
                    log().debug("scheduleService: address/service: " + ipAddress + "/" + svcName + " not scheduled, service is not enabled or does not exist in package: " + pkg.getName());
                continue;
            }

            // Is the interface in the package?
            //
            log().debug("scheduleService: checking ipaddress " + ipAddress + " for inclusion in pkg " + pkg.getName());
            boolean foundInPkg = m_threshdConfig.interfaceInPackage(ipAddress, pkg);
            if (!foundInPkg && existing == false) {
                // The interface might be a newly added one, rebuild the package
                // to ipList mapping and again to verify if the interface is in
                // the package.
                m_threshdConfig.rebuildPackageIpListMap();
                foundInPkg = m_threshdConfig.interfaceInPackage(ipAddress, pkg);
            }
            if (!foundInPkg) {
                if (log().isDebugEnabled())
                    log().debug("scheduleInterface: address/service: " + ipAddress + "/" + svcName + " not scheduled, interface does not belong to package: " + pkg.getName());
                continue;
            }

            log().debug("scheduleService: ipaddress " + ipAddress + " IS in pkg " + pkg.getName());

            if (existing == false) {
                // It is possible that both a nodeGainedService and a
                // primarySnmpInterfaceChanged
                // event are generated for an interface during a rescan. To
                // handle
                // this scenario we must verify that the ipAddress/pkg pair
                // identified by
                // this event does not already exist in the thresholdable
                // services list.
                //
                if (alreadyScheduled(ipAddress, pkg.getName())) {
                    if (log().isDebugEnabled()) {
                        log().debug("scheduleService: ipAddr/pkgName " + ipAddress + "/" + pkg.getName() + " already in thresholdable service list, skipping.");
                    }
                    continue;
                }
            }

            try {
                // Criteria checks have all passed. The interface/service pair
                // can be scheduled.
                //
                ThresholdableService tSvc = null;

                // Create a new SnmpThresholder object representing this node,
                // interface,
                // service and package pairing
                //
                tSvc = new ThresholdableService(this, nodeId, InetAddressUtils.addr(ipAddress), svcName, pkg);

                // Initialize the thresholder with the service.
                //
                ServiceThresholder thresholder = this.getServiceThresholder(svcName);
                if (thresholder == null) {
                    // no thresholder exists for this service so go on to the next one
                    log().warn("Unable to find a Thresholder for service "+svcName+"! But it is configured for Thresholding!");
                    continue;
                }
                thresholder.initialize(tSvc, tSvc.getPropertyMap());

                // Add new service to the thresholdable service list.
                //
                m_thresholdableServices.add(tSvc);

                // Schedule the service for threshold checking
                //
                // NOTE: Service will be scheduled at the configured
                // interval (default is 5 minutes). This should give
                // the collector a chance to update the RRD file so
                // there is data available to be fetched.
                m_scheduler.schedule(tSvc, tSvc.getInterval());

                if (log().isDebugEnabled())
                    log().debug("scheduleService: " + nodeId + "/" + ipAddress + " scheduled for " + svcName + " threshold checking");
            } catch (RuntimeException rE) {
                log().warn("scheduleService: Unable to schedule " + ipAddress + " for service " + svcName + ", reason: " + rE.getMessage(), rE);
            } catch (Throwable t) {
                log().error("scheduleService: Uncaught exception, failed to schedule interface " + ipAddress + " for service " + svcName, t);
            }
        } // end while more packages exist
    }

    /**
     * Returns true if specified address/pkg pair is already represented in the
     * thresholdable services list. False otherwise.
     */
    private boolean alreadyScheduled(String ipAddress, String pkgName) {
        synchronized (m_thresholdableServices) {
            for  (ThresholdableService tSvc : m_thresholdableServices) {
                InetAddress addr = (InetAddress) tSvc.getAddress();
                if (InetAddressUtils.str(addr).equals(InetAddressUtils.normalize(ipAddress)) && tSvc.getPackageName().equals(pkgName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * <p>isSchedulingCompleted</p>
     *
     * @return Returns the schedulingCompleted.
     */
    public boolean isSchedulingCompleted() {
        return m_schedulingCompleted;
    }

    /**
     * <p>setSchedulingCompleted</p>
     *
     * @param schedulingCompleted
     *            The schedulingCompleted to set.
     */
    public void setSchedulingCompleted(boolean schedulingCompleted) {
        m_schedulingCompleted = schedulingCompleted;
    }

    /**
     * <p>refreshServicePackages</p>
     */
    public void refreshServicePackages() {
	for (ThresholdableService thisService : m_thresholdableServices) {
		thisService.refreshPackage();
	}
    }

    /**
     * <p>setThreshdConfig</p>
     *
     * @param threshdConfig a {@link org.opennms.netmgt.config.ThreshdConfigManager} object.
     */
    public void setThreshdConfig(ThreshdConfigManager threshdConfig) {
        m_threshdConfig = threshdConfig;
    }

    /**
     * <p>getPackage</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.threshd.Package} object.
     */
    public Package getPackage(String name) {
        return m_threshdConfig.getPackage(name);
    }
}
