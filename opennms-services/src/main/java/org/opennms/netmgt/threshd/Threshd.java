/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.config.ThreshdConfigManager;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.Thresholder;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.model.capsd.DbIfServiceEntry;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * <p>Threshd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public final class Threshd extends AbstractServiceDaemon {
    
    private static final Logger LOG = LoggerFactory.getLogger(Threshd.class);
    
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
    private static volatile Map<String, ServiceThresholder> m_svcThresholders = new ConcurrentSkipListMap<String, ServiceThresholder>();

    private ThreshdConfigManager m_threshdConfig;

    /**
     * Constructor.
     */
    Threshd() {
    	super("threshd");
        m_scheduler = null;
        m_thresholdableServices = Collections.synchronizedList(new LinkedList<ThresholdableService>());
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {

        LOG.debug("start: Initializing thresholding daemon");

        LOG.debug("start: Loading thresholders");

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

        LOG.debug("start: Scheduling existing interfaces");

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
            LOG.debug("start: Creating event broadcast event processor");

            m_receiver = new BroadcastEventProcessor(this, m_thresholdableServices);
        } catch (Throwable t) {
            LOG.error("start: Failed to initialized the broadcast event receiver", t);
            throw new UndeclaredThrowableException(t);
        }
    }

    private void scheduleBackgroundInitTask() {
        ReadyRunnable interfaceScheduler = new ReadyRunnable() {

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void run() {
                //
                try {
                    scheduleExistingInterfaces();
                } catch (DataRetrievalFailureException sqlE) {
                    LOG.error("start: Failed to schedule existing interfaces", sqlE);
                } finally {
                    setSchedulingCompleted(true);
                }

            }
        };

        m_scheduler.schedule(interfaceScheduler, 0);
    }

    private void initializeScheduler() {
        try {
            LOG.debug("start: Creating threshd scheduler");

            m_scheduler = new LegacyScheduler("Threshd", m_threshdConfig.getConfiguration().getThreads());
        } catch (RuntimeException e) {
            LOG.error("start: Failed to create threshd scheduler", e);
            throw e;
        }
    }

    private void initializeThresholders() {
        Enumeration<Thresholder> eiter = m_threshdConfig.getConfiguration().enumerateThresholder();
        while (eiter.hasMoreElements()) {
            Thresholder thresholder = eiter.nextElement();
            try {
                LOG.debug("start: Loading thresholder {}, classname {}", thresholder.getService(), thresholder.getClassName());
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
                LOG.warn("start: Failed to load thresholder {} for service {}", thresholder.getClassName(), thresholder.getService(), t);
            }
        }
    }

    /**
     * <p>reinitializeThresholders</p>
     */
    public void reinitializeThresholders() {
        for(String key: m_svcThresholders.keySet()) {
            ServiceThresholder thresholder=m_svcThresholders.get(key);

            LOG.debug("reinitializeThresholders: About to reinitialize thresholder {}", key);
            thresholder.reinitialize();
        }
    }
    
    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {

        LOG.debug("start: Initializing thresholding daemon");

        // start the scheduler
        //
        try {
            LOG.debug("start: Starting threshd scheduler");

            m_scheduler.start();
        } catch (RuntimeException e) {
            LOG.error("start: Failed to start scheduler", e);
            throw e;
        }


        LOG.debug("start: Threshd running");
	}

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
		m_scheduler.stop();
        m_receiver.close();

        m_scheduler = null;
	}

    /**
     * <p>onPause</p>
     */
    @Override
    protected void onPause() {
		m_scheduler.pause();
	}

    /**
     * <p>onResume</p>
     */
    @Override
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
    public static ServiceThresholder getServiceThresholder(String svcName) {
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
                    
                    LOG.debug("Scheduling service nodeId/ipAddress/svcName {}/{}/{}",nodeId,ipAddress,svcName);
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
                LOG.debug("scheduleService: address/service: {}/{} not scheduled, service is not enabled or does not exist in package: {}", ipAddress, svcName, pkg.getName());
                continue;
            }

            // Is the interface in the package?
            //
            LOG.debug("scheduleService: checking ipaddress {} for inclusion in pkg {}", ipAddress, pkg.getName());
            boolean foundInPkg = m_threshdConfig.interfaceInPackage(ipAddress, pkg);
            if (!foundInPkg && existing == false) {
                // The interface might be a newly added one, rebuild the package
                // to ipList mapping and again to verify if the interface is in
                // the package.
                m_threshdConfig.rebuildPackageIpListMap();
                foundInPkg = m_threshdConfig.interfaceInPackage(ipAddress, pkg);
            }
            if (!foundInPkg) {
                LOG.debug("scheduleInterface: address/service: {}/{} not scheduled, interface does not belong to package: {}", ipAddress, svcName, pkg.getName());
                continue;
            }

            LOG.debug("scheduleService: ipaddress {} IS in pkg {}", ipAddress, pkg.getName());

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
                    LOG.debug("scheduleService: ipAddr/pkgName {}/{} already in thresholdable service list, skipping.", ipAddress, pkg.getName());
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
                ServiceThresholder thresholder = Threshd.getServiceThresholder(svcName);
                if (thresholder == null) {
                    // no thresholder exists for this service so go on to the next one
                    LOG.warn("Unable to find a Thresholder for service {}! But it is configured for Thresholding!", svcName);
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

                LOG.debug("scheduleService: {}/{} scheduled for {} threshold checking", nodeId, ipAddress, svcName);
            } catch (RuntimeException rE) {
                LOG.warn("scheduleService: Unable to schedule {} for service {}", ipAddress, svcName, rE);
            } catch (Throwable t) {
                LOG.error("scheduleService: Uncaught exception, failed to schedule interface {} for service {}", ipAddress, svcName, t);
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
