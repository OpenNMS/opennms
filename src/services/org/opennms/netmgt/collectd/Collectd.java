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

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.config.collectd.Package;

public final class Collectd implements PausableFiber {
    /**
     * Log4j category
     */
    private final static String LOG4J_CATEGORY = "OpenNMS.Collectd";

    /**
     * SQL used to retrieve all the interfaces which support a particular
     * service.
     */
    private final static String SQL_RETRIEVE_INTERFACES = "SELECT DISTINCT nodeid,ipaddr FROM ifServices, service WHERE ifServices.serviceid = service.serviceid AND service.servicename = ?";

    /**
     * SQL used to retrieve all the service id's and names from the database.
     */
    private final static String SQL_RETRIEVE_SERVICE_IDS = "SELECT serviceid,servicename FROM service";

    /**
     * Singleton instance of the Collectd class
     */
    private final static Collectd m_singleton = new Collectd();

    /**
     * Holds map of service names to service identifiers
     */
    private final static Map m_serviceIds = new HashMap();

    /**
     * List of all CollectableService objects.
     */
    private List m_collectableServices;

    /**
     * Reference to the collection scheduler
     */
    private Scheduler m_scheduler;

    /**
     * Status of the Collectd instance.
     */
    private int m_status;

    /**
     * Reference to the event processor
     */
    private BroadcastEventProcessor m_receiver;

    /**
     * Map of all available ServiceCollector objects indexed by service name
     */
    private static Map m_svcCollectors;

    /**
     * Indicates if scheduling of existing interfaces has been completed
     * 
     */
    private boolean m_schedulingCompleted = false;

    /**
     * Constructor.
     */
    private Collectd() {
        m_scheduler = null;
        m_status = START_PENDING;
        m_svcCollectors = Collections.synchronizedMap(new TreeMap());
        m_collectableServices = Collections.synchronizedList(new LinkedList());
    }

    /**
     * Responsible for starting the collection daemon.
     */
    public synchronized void init() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        // get the category logger
        final Category log = ThreadCategory.getInstance();

        if (log.isDebugEnabled())
            log.debug("init: Initializing collection daemon");

        loadConfigFactory(log);
        loadscheduledOutagesConfigFactory(log);

        if (log.isDebugEnabled())
            log.debug("init: Loading collectors");

        // Collectd configuration
        //
        CollectdConfigFactory cCfgFactory = CollectdConfigFactory.getInstance();
        CollectdConfiguration config = cCfgFactory.getConfiguration();

        instantiateCollectors(log, config);
        buildServiceIdMap(log);
        createScheduler(log, config);
        ReadyRunnable interfaceScheduler = buildSchedule(log);
        m_scheduler.schedule(interfaceScheduler, 0);
        createEventProcessor(log);

    }

    private void createEventProcessor(final Category log) {
        // Create an event receiver. The receiver will
        // receive events, process them, creates network
        // interfaces, and schedulers them.
        //
        try {
            if (log.isDebugEnabled())
                log.debug("init: Creating event broadcast event processor");

            m_receiver = new BroadcastEventProcessor(m_collectableServices);
        } catch (Throwable t) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("init: Failed to initialized the broadcast event receiver", t);

            throw new UndeclaredThrowableException(t);
        }
    }

    private ReadyRunnable buildSchedule(final Category log) {
        // Schedule existing interfaces for data collection

        ReadyRunnable interfaceScheduler = new ReadyRunnable() {

            public boolean isReady() {
                return true;
            }

            public void run() {
                //
                try {
                    scheduleExistingInterfaces();
                } catch (SQLException sqlE) {
                    if (log.isEnabledFor(Priority.ERROR))
                        log.error("start: Failed to schedule existing interfaces", sqlE);
                } finally {
                    setSchedulingCompleted(true);
                }

            }
        };
        return interfaceScheduler;
    }

    private void createScheduler(final Category log, CollectdConfiguration config) {
        // Create a scheduler
        //
        try {
            if (log.isDebugEnabled())
                log.debug("init: Creating collectd scheduler");

            m_scheduler = new Scheduler("Collectd", config.getThreads());
        } catch (RuntimeException e) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("init: Failed to create collectd scheduler", e);
            throw e;
        }
    }

    private void buildServiceIdMap(final Category log) {
        // Make sure we can connect to the database and load
        // the services table so we can easily convert from
        // service name to service id
        //
        if (log.isDebugEnabled())
            log.debug("start: Testing database connection");

        java.sql.Connection ctest = null;
        ResultSet rs = null;
        try {
            DatabaseConnectionFactory.init();
            ctest = DatabaseConnectionFactory.getInstance().getConnection();

            PreparedStatement loadStmt = ctest.prepareStatement(SQL_RETRIEVE_SERVICE_IDS);

            // go ahead and load the service table
            //
            rs = loadStmt.executeQuery();
            while (rs.next()) {
                Integer id = new Integer(rs.getInt(1));
                String name = rs.getString(2);

                m_serviceIds.put(name, id);
            }
        } catch (IOException iE) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("start: IOException getting database connection", iE);
            throw new UndeclaredThrowableException(iE);
        } catch (MarshalException mE) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("start: Marshall Exception getting database connection", mE);
            throw new UndeclaredThrowableException(mE);
        } catch (ValidationException vE) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("start: Validation Exception getting database connection", vE);
            throw new UndeclaredThrowableException(vE);
        } catch (SQLException sqlE) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("start: Error accessing database.", sqlE);
            throw new UndeclaredThrowableException(sqlE);
        } catch (ClassNotFoundException cnfE) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("start: Error accessing database.", cnfE);
            throw new UndeclaredThrowableException(cnfE);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    if (log.isInfoEnabled())
                        log.info("start: an error occured closing the result set", e);
                }
            }
            if (ctest != null) {
                try {
                    ctest.close();
                } catch (Exception e) {
                    if (log.isInfoEnabled())
                        log.info("start: an error occured closing the SQL connection", e);
                }
            }
        }
    }

    private void instantiateCollectors(final Category log, CollectdConfiguration config) {
        // Load up an instance of each collector from the config
        // so that the event processor will have them for
        // new incomming events to create collectable service objects.
        //
        Enumeration eiter = config.enumerateCollector();
        while (eiter.hasMoreElements()) {
            Collector collector = (Collector) eiter.nextElement();
            try {
                if (log.isDebugEnabled()) {
                    log.debug("init: Loading collector " + collector.getService() + ", classname " + collector.getClassName());
                }
                Class cc = Class.forName(collector.getClassName());
                ServiceCollector sc = (ServiceCollector) cc.newInstance();

                // Attempt to initialize the service collector
                //
                Map properties = null; // properties not currently used
                sc.initialize(properties);

                m_svcCollectors.put(collector.getService(), sc);
            } catch (Throwable t) {
                if (log.isEnabledFor(Priority.WARN)) {
                    log.warn("init: Failed to load collector " + collector.getClassName() + " for service " + collector.getService(), t);
                }
            }
        }
    }

    private void loadscheduledOutagesConfigFactory(final Category log) {
        // Load up the configuration for the scheduled outages.
        //
        try {
            PollOutagesConfigFactory.reload();
        } catch (MarshalException ex) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("init: Failed to load poll-outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("init: Failed to load poll-outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("init: Failed to load poll-outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }
    }

    private void loadConfigFactory(final Category log) {
        // Load collectd configuration file
        //
        try {
            CollectdConfigFactory.reload();
        } catch (MarshalException ex) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("init: Failed to load collectd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("init: Failed to load collectd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("init: Failed to load collectd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }
    }

    /**
     * Responsible for starting the collection daemon.
     */
    public synchronized void start() {
        m_status = STARTING;

        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        // get the category logger
        Category log = ThreadCategory.getInstance();

        if (log.isDebugEnabled())
            log.debug("start: Initializing collection daemon");

        // start the scheduler
        //
        try {
            if (log.isDebugEnabled())
                log.debug("start: Starting collectd scheduler");

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
            log.debug("start: Collectd running");
    }

    /**
     * Responsible for stopping the collection daemon.
     */
    public synchronized void stop() {
        m_status = STOP_PENDING;
        m_scheduler.stop();
        m_receiver.close();

        m_scheduler = null;
        m_status = STOPPED;
        Category log = ThreadCategory.getInstance();
        if (log.isDebugEnabled())
            log.debug("stop: Collectd stopped");
    }

    /**
     * Responsible for pausing the collection daemon.
     */
    public synchronized void pause() {
        if (m_status != RUNNING)
            return;

        m_status = PAUSE_PENDING;
        m_scheduler.pause();
        m_status = PAUSED;

        Category log = ThreadCategory.getInstance();
        if (log.isDebugEnabled())
            log.debug("pause: Collectd paused");
    }

    /**
     * Responsible for resuming the collection daemon.
     */
    public synchronized void resume() {
        if (m_status != PAUSED)
            return;

        m_status = RESUME_PENDING;
        m_scheduler.resume();
        m_status = RUNNING;

        Category log = ThreadCategory.getInstance();
        if (log.isDebugEnabled())
            log.debug("resume: Collectd resumed");
    }

    /**
     * Returns current status of the collection daemon.
     */
    public synchronized int getStatus() {
        return m_status;
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
        return (ServiceCollector) m_svcCollectors.get(svcName);
    }

    /**
     * Schedule existing interfaces for data collection.
     * 
     * @throws SQLException
     *             if database errors encountered.
     */
    private void scheduleExistingInterfaces() throws SQLException {
        // get the category logger
        //
        Category log = ThreadCategory.getInstance();

        // Database connection
        java.sql.Connection dbConn = null;

        PreparedStatement stmt = null;
        try {
            dbConn = DatabaseConnectionFactory.getInstance().getConnection();
            stmt = dbConn.prepareStatement(SQL_RETRIEVE_INTERFACES);

            // Loop through loaded collectors and schedule for each one present
            //
            Set svcNames = m_svcCollectors.keySet();
            Iterator i = svcNames.iterator();
            while (i.hasNext()) {
                String svcName = (String) i.next();
                ServiceCollector collector = (ServiceCollector) m_svcCollectors.get(svcName);

                if (log.isDebugEnabled())
                    log.debug("scheduleExistingInterfaces: dbConn = " + dbConn + ", svcName = " + svcName);

                // Retrieve list of interfaces from the database which
                // support the service collected by this collector
                //
                try {
                    stmt.setString(1, svcName); // Service name
                    ResultSet rs = stmt.executeQuery();

                    // Iterate over result set and schedule each
                    // interface/service
                    // pair which passes the criteria
                    //
                    while (rs.next()) {
                        int nodeId = rs.getInt(1);
                        String ipAddress = rs.getString(2);

                        scheduleInterface(nodeId, ipAddress, svcName, true);
                    } // end while more interfaces in result set

                    rs.close();
                } catch (SQLException sqle) {
                    log.warn("scheduleExistingInterfaces: SQL exception while querying ipInterface table", sqle);
                    throw sqle;
                }
            } // end while more service collectors
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    if (log.isDebugEnabled())
                        log.debug("scheduleExistingInterfaces: an exception occured closing the SQL statement", e);
                }
            }

            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (Throwable t) {
                    if (log.isDebugEnabled())
                        log.debug("scheduleExistingInterfaces: an exception occured closing the SQL connection", t);
                }
            }
        }
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
        Category log = ThreadCategory.getInstance(getClass());

        CollectdConfigFactory cCfgFactory = CollectdConfigFactory.getInstance();
        CollectdConfiguration cConfig = cCfgFactory.getConfiguration();
        Enumeration epkgs = cConfig.enumeratePackage();

        // Compare interface/service pair against each collectd package
        // For each match, create new SnmpCollector object and
        // schedule it for collection
        //
        while (epkgs.hasMoreElements()) {
            Package pkg = (Package) epkgs.nextElement();

            // Make certain the the current service is in the package
            // and enabled!
            //
            if (!cCfgFactory.serviceInPackageAndEnabled(svcName, pkg)) {
                if (log.isDebugEnabled())
                    log.debug("scheduleInterface: address/service: " + ipAddress + "/" + svcName + " not scheduled, service is not enabled or does not exist in package: " + pkg.getName());
                continue;
            }

            // Is the interface in the package?
            //
            if (!cCfgFactory.interfaceInPackage(ipAddress, pkg)) {
                if (log.isDebugEnabled())
                    log.debug("scheduleInterface: address/service: " + ipAddress + "/" + svcName + " not scheduled, interface does not belong to package: " + pkg.getName());
                continue;
            }

            if (existing == false) {
                // It is possible that both a nodeGainedService and a
                // primarySnmpInterfaceChanged
                // event are generated for an interface during a rescan. To
                // handle
                // this scenario we must verify that the ipAddress/pkg pair
                // identified by
                // this event does not already exist in the collectable services
                // list.
                //
                if (alreadyScheduled(ipAddress, pkg.getName())) {
                    if (log.isDebugEnabled()) {
                        log.debug("scheduleInterface: ipAddr/pkgName " + ipAddress + "/" + pkg.getName() + " already in collectable service list, skipping.");
                    }
                    continue;
                }
            }

            try {
                // Criteria checks have all passed. The interface/service pair
                // can be scheduled.
                //
                CollectableService cSvc = null;

                // Create a new SnmpCollector object representing this node,
                // interface,
                // service and package pairing
                //
                cSvc = new CollectableService(nodeId, InetAddress.getByName(ipAddress), svcName, pkg);

                // Initialize the collector with the collectable service.
                //
                ServiceCollector collector = this.getServiceCollector(svcName);
                collector.initialize(cSvc, cSvc.getPropertyMap());

                // Add new collectable service to the colleable service list.
                //
                m_collectableServices.add(cSvc);

                // Schedule the collectable service for immediate collection
                //
                m_scheduler.schedule(cSvc, 0);

                if (log.isDebugEnabled())
                    log.debug("scheduleInterface: " + nodeId + "/" + ipAddress + " scheduled for " + svcName + " collection");
            } catch (UnknownHostException ex) {
                log.error("scheduleInterface: Failed to schedule interface " + ipAddress + " for service " + svcName + ", illegal address", ex);
            } catch (RuntimeException rE) {
                log.warn("scheduleInterface: Unable to schedule " + ipAddress + " for service " + svcName + ", reason: " + rE.getMessage());
            } catch (Throwable t) {
                log.error("scheduleInterface: Uncaught exception, failed to schedule interface " + ipAddress + " for service " + svcName, t);
            }
        } // end while more packages exist
    }

    /**
     * Returns true if specified address/pkg pair is already represented in the
     * collectable services list. False otherwise.
     */
    private boolean alreadyScheduled(String ipAddress, String pkgName) {
        synchronized (m_collectableServices) {
            CollectableService cSvc = null;
            Iterator iter = m_collectableServices.iterator();
            while (iter.hasNext()) {
                cSvc = (CollectableService) iter.next();

                InetAddress addr = (InetAddress) cSvc.getAddress();
                if (addr.getHostAddress().equals(ipAddress) && cSvc.getPackageName().equals(pkgName)) {
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
}
