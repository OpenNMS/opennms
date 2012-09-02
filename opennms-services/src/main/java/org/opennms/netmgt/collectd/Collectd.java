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

package org.opennms.netmgt.collectd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.capsd.InsufficientInformationException;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.CollectorConfigDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * <p>Collectd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Collectd extends AbstractServiceDaemon implements
        EventListener {
    
    private static CollectdInstrumentation s_instrumentation = null;
    
    /**
     * <p>instrumentation</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.CollectdInstrumentation} object.
     */
    public static CollectdInstrumentation instrumentation() {
        if (s_instrumentation == null) {
            String className = System.getProperty("org.opennms.collectd.instrumentationClass", DefaultCollectdInstrumentation.class.getName());
            try { 
                s_instrumentation = (CollectdInstrumentation) ClassUtils.forName(className, Thread.currentThread().getContextClassLoader()).newInstance();
            } catch (Throwable e) {
                s_instrumentation = new DefaultCollectdInstrumentation();
            }
        }

        return s_instrumentation;
    }
    
    /**
     * Log4j category
     */
    private final static String LOG4J_CATEGORY = "OpenNMS.Collectd";
    
    /**
     * Instantiated service collectors specified in config file
     */
    private final Map<String,ServiceCollector> m_collectors = new HashMap<String,ServiceCollector>(4);

    /**
     * List of all CollectableService objects.
     */
    private final List<CollectableService> m_collectableServices;

    /**
     * Reference to the collection scheduler
     */
    private volatile Scheduler m_scheduler;

    /**
     * Indicates if scheduling of existing interfaces has been completed
     */
    private volatile CollectorConfigDao m_collectorConfigDao;

    private volatile IpInterfaceDao m_ifaceDao;

    static class SchedulingCompletedFlag {
        volatile boolean m_schedulingCompleted = false;

        public synchronized void setSchedulingCompleted(
                boolean schedulingCompleted) {
            m_schedulingCompleted = schedulingCompleted;
        }

        public synchronized boolean isSchedulingCompleted() {
            return m_schedulingCompleted;
        }

    }

    private final SchedulingCompletedFlag m_schedulingCompletedFlag = new SchedulingCompletedFlag();

    private volatile EventIpcManager m_eventIpcManager;

    private volatile TransactionTemplate m_transTemplate;

    private volatile NodeDao m_nodeDao;

    /**
     * Constructor.
     */
    public Collectd() {
        super(LOG4J_CATEGORY);

        m_collectableServices = Collections.synchronizedList(new LinkedList<CollectableService>());
    }

    /**
     * <p>onInit</p>
     */
    protected void onInit() {
        Assert.notNull(m_collectorConfigDao, "collectorConfigDao must not be null");
        Assert.notNull(m_eventIpcManager, "eventIpcManager must not be null");
        Assert.notNull(m_transTemplate, "transTemplate must not be null");
        Assert.notNull(m_ifaceDao, "ifaceDao must not be null");
        Assert.notNull(m_nodeDao, "nodeDao must not be null");
        
        
        log().debug("init: Initializing collection daemon");
        
        // make sure the instrumentation gets initialized
        instrumentation();
        
        instantiateCollectors();

        getScheduler().schedule(0, ifScheduler());

        installMessageSelectors();
    }

    private void installMessageSelectors() {
        // Add the EventListeners for the UEIs in which this service is
        // interested
        List<String> ueiList = new ArrayList<String>();

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
        
        // thresholds configuration change
        ueiList.add(EventConstants.THRESHOLDCONFIG_CHANGED_EVENT_UEI);

        // daemon configuration change
        ueiList.add(EventConstants.RELOAD_DAEMON_CONFIG_UEI);
        
        // node category membership changes
        ueiList.add(EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI);
        
        getEventIpcManager().addEventListener(this, ueiList);
    }

    /**
     * <p>setEventIpcManager</p>
     *
     * @param eventIpcManager a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

    /**
     * <p>getEventIpcManager</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public EventIpcManager getEventIpcManager() {
        return m_eventIpcManager;
    }

    private ReadyRunnable ifScheduler() {
        // Schedule existing interfaces for data collection

        ReadyRunnable interfaceScheduler = new ReadyRunnable() {

            public boolean isReady() {
                return true;
            }

            public void run() {
                final String prefix = ThreadCategory.getPrefix();
                try {
                    ThreadCategory.setPrefix(LOG4J_CATEGORY);
                    scheduleExistingInterfaces();
                } catch (SQLException e) {
                    log().error(
                                "start: Failed to schedule existing interfaces",
                                e);
                } finally {
                    setSchedulingCompleted(true);
                    ThreadCategory.setPrefix(prefix);
                }

            }
        };
        return interfaceScheduler;
    }

    private void createScheduler() {

        // Create a scheduler
        try {
            log().debug("init: Creating collectd scheduler");

            setScheduler(new LegacyScheduler(
                                             "Collectd",
                                             getCollectorConfigDao().getSchedulerThreads()));
        } catch (RuntimeException e) {
            log().fatal("init: Failed to create collectd scheduler", e);
            throw e;
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onStart() {
        // start the scheduler
        try {
            log().debug("start: Starting collectd scheduler");

            getScheduler().start();
        } catch (RuntimeException e) {
            log().fatal("start: Failed to start scheduler", e);
            throw e;
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onStop() {
        getScheduler().stop();
        deinstallMessageSelectors();

        setScheduler(null);
    }

    /** {@inheritDoc} */
    @Override
    protected void onPause() {
        getScheduler().pause();
    }

    /** {@inheritDoc} */
    @Override
    protected void onResume() {
        getScheduler().resume();
    }

    /**
     * Schedule existing interfaces for data collection.
     * 
     * @throws SQLException
     *             if database errors encountered.
     */
    private void scheduleExistingInterfaces() throws SQLException {
        
        instrumentation().beginScheduleExistingInterfaces();
        try {

            m_transTemplate.execute(new TransactionCallback<Object>() {

                public Object doInTransaction(TransactionStatus status) {
                    
                    // Loop through collectors and schedule for each one present
                    for(String name : getCollectorNames()) {
                        scheduleInterfacesWithService(name);
                    }
                    return null;
                }

            });
        
        } finally {
            instrumentation().endScheduleExistingInterfaces();
        }
    }

    private void scheduleInterfacesWithService(String svcName) {
        instrumentation().beginScheduleInterfacesWithService(svcName);
        try {
        log().info("scheduleInterfacesWithService: svcName = " + svcName);

        Collection<OnmsIpInterface> ifsWithServices = findInterfacesWithService(svcName);
        for (OnmsIpInterface iface : ifsWithServices) {
            scheduleInterface(iface, svcName, true);
        }
        } finally {
            instrumentation().endScheduleInterfacesWithService(svcName);
        }
    }

    private Collection<OnmsIpInterface> findInterfacesWithService(String svcName) {
        instrumentation().beginFindInterfacesWithService(svcName);
        int count = -1;
        try {
           Collection<OnmsIpInterface> ifaces = getIpInterfaceDao().findByServiceType(svcName);
           count = ifaces.size();
           return ifaces;
        } finally {
            instrumentation().endFindInterfacesWithService(svcName, count);
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
    private void scheduleInterface(int nodeId, String ipAddress,
            String svcName, boolean existing) {
        
        OnmsIpInterface iface = getIpInterface(nodeId, ipAddress);
        if (iface == null) {
            log().error("Unable to find interface with address "+ipAddress+" on node "+nodeId);
            return;
        }
        
        OnmsMonitoredService svc = iface.getMonitoredServiceByServiceType(svcName);
        if (svc == null) {
            log().error("Unable to find service "+svcName+" on interface with address "+ipAddress+" on node "+nodeId);
            return;
        }
        
        scheduleInterface(iface, svc.getServiceType().getName(),
                          existing);
    }
    
	private void scheduleNode(final int nodeId, final boolean existing) {
		
        getCollectorConfigDao().rebuildPackageIpListMap();
		
		OnmsNode node = m_nodeDao.getHierarchy(nodeId);
		node.visit(new AbstractEntityVisitor() {

			@Override
			public void visitMonitoredService(OnmsMonitoredService monSvc) {
				scheduleInterface(monSvc.getIpInterface(), monSvc.getServiceName(), existing);
			}
			
		});
	}

	private OnmsIpInterface getIpInterface(int nodeId, String ipAddress) {
		OnmsNode node = m_nodeDao.load(nodeId);
        OnmsIpInterface iface = node.getIpInterfaceByIpAddress(ipAddress);
		return iface;
	}

    private void scheduleInterface(OnmsIpInterface iface, String svcName, boolean existing) {
        
        final String ipAddress = str(iface.getIpAddress());
        if (ipAddress == null) {
        	LogUtils.warnf(this, "Unable to schedule interface %s, could not determine IP address.", iface);
        	return;
        }

		instrumentation().beginScheduleInterface(iface.getNode().getId(), ipAddress, svcName);
        try {
        
        Collection<CollectionSpecification> matchingSpecs = getSpecificationsForInterface(iface, svcName);
        StringBuffer sb;
        
        if (log().isDebugEnabled()) {
            sb = new StringBuffer();
            sb.append("scheduleInterface: found ");
            sb.append(Integer.toString(matchingSpecs.size()));
            sb.append(" matching specs for interface: ");
            sb.append(iface);
            log().debug(sb.toString());
        }

        for (CollectionSpecification spec : matchingSpecs) {

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
                        sb = new StringBuffer();
                        sb.append("scheduleInterface: svc/pkgName ");
                        sb.append(iface);
                        sb.append('/');
                        sb.append(spec);
                        sb.append(" already in collectable service list, skipping.");
                        log().debug(sb.toString());
                    }
                    continue;
                }
            }

            try {
                /*
                 * Criteria checks have all passed. The interface/service pair
                 * can be scheduled.
                 */
                if (log().isDebugEnabled()) {
                    sb = new StringBuffer();
                    sb.append("scheduleInterface: now scheduling interface: ");
                    sb.append(iface);
                    sb.append('/');
                    sb.append(svcName);
                    log().debug(sb.toString());
                }
                CollectableService cSvc = null;

                /*
                 * Create a new SnmpCollector object representing this node,
                 * interface, service and package pairing
                 */

                cSvc = new CollectableService(iface, m_ifaceDao, spec, getScheduler(),
                                              m_schedulingCompletedFlag,
                                              m_transTemplate.getTransactionManager());

                // Add new collectable service to the collectable service list.
                m_collectableServices.add(cSvc);

                // Schedule the collectable service for immediate collection
                getScheduler().schedule(0, cSvc.getReadyRunnable());

                if (log().isDebugEnabled()) {
                    sb = new StringBuffer();
                    sb.append("scheduleInterface: ");
                    sb.append(iface);
                    sb.append('/');
                    sb.append(svcName);
                    sb.append(" collection, scheduled");
                    log().debug(sb.toString());
                }
            } catch (CollectionInitializationException e) {
                sb = new StringBuffer();
                sb.append("scheduleInterface: Unable to schedule ");
                sb.append(iface);
                sb.append('/');
                sb.append(svcName);
                sb.append(", reason: ");
                sb.append(e.getMessage());

                // Only log the stack trace if TRACE level logging is enabled.
                // Fixes bug NMS-3324.
                // http://issues.opennms.org/browse/NMS-3324
                if (log().isTraceEnabled()) {
                    log().trace(sb.toString(), e);
                } else {
                    log().info(sb.toString());
                }
            } catch (Throwable t) {
                sb = new StringBuffer();
                sb.append("scheduleInterface: Uncaught exception, failed to schedule interface ");
                sb.append(iface);
                sb.append('/');
                sb.append(svcName);
                sb.append(". ");
                sb.append(t);
                log().error(sb.toString(), t);
            }
        } // end while more specifications exist
        
        } finally {
            instrumentation().endScheduleInterface(iface.getNode().getId(), ipAddress, svcName);
        }
    }

    /**
     * <p>getSpecificationsForInterface</p>
     *
     * @param iface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    public Collection<CollectionSpecification> getSpecificationsForInterface(OnmsIpInterface iface, String svcName) {
        Collection<CollectionSpecification> matchingPkgs = new LinkedList<CollectionSpecification>();


        /*
         * Compare interface/service pair against each collectd package
         * For each match, create new SnmpCollector object and
         * schedule it for collection
         */
        for(CollectdPackage wpkg : getCollectorConfigDao().getPackages()) {
            /*
             * Make certain the the current service is in the package
             * and enabled!
             */
            if (!wpkg.serviceInPackageAndEnabled(svcName)) {
                if (log().isDebugEnabled()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("getSpecificationsForInterface: address/service: ");
                    sb.append(iface);
                    sb.append("/");
                    sb.append(svcName);
                    sb.append(" not scheduled, service is not enabled or does not exist in package: ");
                    sb.append(wpkg.getName());
                    log().debug(sb.toString());
                }
                continue;
            }

            // Is the interface in the package?
            final String ipAddress = str(iface.getIpAddress());
			if (!wpkg.interfaceInPackage(ipAddress)) {
                if (log().isDebugEnabled()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("getSpecificationsForInterface: address/service: ");
                    sb.append(iface);
                    sb.append("/");
                    sb.append(svcName);
                    sb.append(" not scheduled, interface does not belong to package: ");
                    sb.append(wpkg.getName());
                    log().debug(sb.toString());
                }
                continue;
            }

            if (log().isDebugEnabled()) {
                StringBuffer sb = new StringBuffer();
                sb.append("getSpecificationsForInterface: address/service: ");
                sb.append(iface);
                sb.append("/");
                sb.append(svcName);
                sb.append(" scheduled, interface does belong to package: ");
                sb.append(wpkg.getName());
                log().debug(sb.toString());
            }
            
            matchingPkgs.add(new CollectionSpecification(wpkg, svcName, getServiceCollector(svcName)));
        }
        return matchingPkgs;
    }

    /**
     * Returns true if specified address/pkg pair is already represented in
     * the collectable services list. False otherwise.
     * 
     * @param iface
     *            TODO
     * @param spec
     *            TODO
     * @param svcName
     *            TODO
     */
    private boolean alreadyScheduled(OnmsIpInterface iface, CollectionSpecification spec) {
        String ipAddress = str(iface.getIpAddress());
        
        if (ipAddress == null) {
        	LogUtils.warnf(this, "Cannot determine if interface %s is already scheduled.  Unable to look up IP address.", iface);
        	return false;
        }

        String svcName = spec.getServiceName();
        String pkgName = spec.getPackageName();
        StringBuffer sb;
        boolean isScheduled = false;
        
        if (log().isDebugEnabled()) {
            sb = new StringBuffer();
            sb.append("alreadyScheduled: determining if interface: ");
            sb.append(iface);
            sb.append(" is already scheduled.");
        }
        
        synchronized (m_collectableServices) {
        	for (CollectableService cSvc : m_collectableServices) {
                InetAddress addr = (InetAddress) cSvc.getAddress();
                if (str(addr).equals(ipAddress)
                        && cSvc.getPackageName().equals(pkgName)
                        && cSvc.getServiceName().equals(svcName)) {
                    isScheduled = true;
                    break;
                }
            }
        }

        if (log().isDebugEnabled()) {
            sb = new StringBuffer();
            sb.append("alreadyScheduled: interface ");
            sb.append(iface);
            sb.append("already scheduled check: ");
            sb.append(isScheduled);
        }
        return isScheduled;
    }

    /**
     * @param schedulingCompleted
     *            The schedulingCompleted to set.
     */
    private void setSchedulingCompleted(boolean schedulingCompleted) {
        m_schedulingCompletedFlag.setSchedulingCompleted(schedulingCompleted);
    }

    private void refreshServicePackages() {
    	for (CollectableService thisService : m_collectableServices) {
            thisService.refreshPackage(getCollectorConfigDao());
        }
    }

    private List<CollectableService> getCollectableServices() {
        return m_collectableServices;
    }

    /**
     * {@inheritDoc}
     *
     * This method is invoked by the JMS topic session when a new event is
     * available for processing. Currently only text based messages are
     * processed by this callback. Each message is examined for its Universal
     * Event Identifier and the appropriate action is taking based on each
     * UEI.
     */
    public void onEvent(final Event event) {
    	
    	String prefix = ThreadCategory.getPrefix();
    	try {
    		ThreadCategory.setPrefix(getName());
    		m_transTemplate.execute(new TransactionCallback<Object>() {

    			public Object doInTransaction(TransactionStatus status) {
    				onEventInTransaction(event);
    				return null;
    			}

    		});
    	} finally {
    		ThreadCategory.setPrefix(prefix);
    	}

    }

    private void onEventInTransaction(Event event) {
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
            } else if (event.getUei().equals(EventConstants.RELOAD_DAEMON_CONFIG_UEI)) {
                handleReloadDaemonConfig(event);
            } else if (event.getUei().equals(EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI)) {
                handleNodeCategoryMembershipChanged(event);
            }
        } catch (InsufficientInformationException e) {
            handleInsufficientInfo(e);
        }
    }

    /**
     * <p>handleInsufficientInfo</p>
     *
     * @param e a {@link org.opennms.netmgt.capsd.InsufficientInformationException} object.
     */
    protected void handleInsufficientInfo(InsufficientInformationException e) {
        log().info(e.getMessage());
    }

    private void handleDupNodeDeleted(Event event)
            throws InsufficientInformationException {
        handleNodeDeleted(event);
    }

    private void handleScheduledOutagesChanged(Event event) {
        try {
            log().info("Reloading Collectd config factory");
            CollectdConfigFactory.reload();
            refreshServicePackages();
        } catch (Throwable e) {
            log().error(
                        "Failed to reload CollectdConfigFactory because "
                                + e.getMessage(), e);
        }
    }

    /**
     * </p>
     * Closes the current connections to the Java Message Queue if they are
     * still active. This call may be invoked more than once safely and may be
     * invoked during object finalization.
     * </p>
     */
    private void deinstallMessageSelectors() {
        getEventIpcManager().removeEventListener(this);
    }

    /**
     * This method is responsible for handling configureSNMP events.
     * 
     * @param event
     *            The event to process.
     */
    private void handleConfigureSNMP(final Event event) {
        if (log().isDebugEnabled()) {
            log().debug("configureSNMPHandler: processing configure SNMP event..."+event);
        }
        
        SnmpEventInfo info = null;
        try {
            info = new SnmpEventInfo(event);
            
            if (StringUtils.isBlank(info.getFirstIPAddress())) {				
                log().error("configureSNMPHandler: event contained invalid firstIpAddress.  "+event);
                return;
            }
            
            log().debug("configureSNMPHandler: processing configure SNMP event: "+info);
            SnmpPeerFactory.getInstance().define(info);
            SnmpPeerFactory.saveCurrent();
            log().debug("configureSNMPHandler: process complete. "+info);
            
        } catch (Throwable e) {
            log().error("configureSNMPHandler: ",e);
        }
    }

    /**
     * This method is responsible for handling interfaceDeleted events.
     * 
     * @param event
     *            The event to process.
     * @throws InsufficientInformationException
     */
    private void handleInterfaceDeleted(Event event)
            throws InsufficientInformationException {
        EventUtils.checkNodeId(event);

        ThreadCategory log = log();
        
        String ipAddr = event.getInterface();
        if(EventUtils.isNonIpInterface(ipAddr) ) {
            log().debug("handleInterfaceDeleted: the deleted interface was a non-ip interface. Nothing to do here.");
            return;
        }

        Long nodeId = event.getNodeid();

        // Iterate over the collectable services list and mark any entries
        // which match the deleted nodeId/IP address pair for deletion
        synchronized (getCollectableServices()) {
            CollectableService cSvc = null;
            ListIterator<CollectableService> liter = getCollectableServices().listIterator();
            while (liter.hasNext()) {
                cSvc = liter.next();

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
                    log().debug("Marking CollectableService for deletion because an interface was deleted:  Service nodeid="+cSvc.getNodeId()+
                                ", deleted node:"+nodeId+
                                "service address:"+addr.getHostName()+
                                "deleted interface:"+ipAddr);

                    updates.markForDeletion();
                }

                // Now safe to remove the collectable service from
                // the collectable services list
                liter.remove();
            }
        }

        if (log.isDebugEnabled())
            log.debug("interfaceDeletedHandler: processing of interfaceDeleted event for "
                    + nodeId + "/" + ipAddr + " completed.");
    }

    /**
     * This method is responsible for processing 'interfacReparented' events.
     * An 'interfaceReparented' event will have old and new nodeId parms
     * associated with it. All CollectableService objects in the service
     * updates map which match the event's interface address and the SNMP
     * service have a reparenting update associated with them. When the
     * scheduler next pops one of these services from an interval queue for
     * collection all of the RRDs associated with the old nodeId are moved
     * under the new nodeId and the nodeId of the collectable service is
     * updated to reflect the interface's new parent nodeId.
     * 
     * @param event
     *            The event to process.
     * @throws InsufficientInformationException
     */
    private void handleInterfaceReparented(Event event)
            throws InsufficientInformationException {
        EventUtils.checkNodeId(event);
        EventUtils.checkInterface(event);

        ThreadCategory log = log();
        if (log.isDebugEnabled())
            log.debug("interfaceReparentedHandler:  processing interfaceReparented event for "
                    + event.getInterface());

        // Verify that the event has an interface associated with it
        if (event.getInterface() == null)
            return;

        // Extract the old and new nodeId's from the event parms
        String oldNodeIdStr = null;
        String newNodeIdStr = null;
        String parmName = null;
        Value parmValue = null;
        String parmContent = null;

        for (Parm parm : event.getParmCollection()) {
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
        OnmsIpInterface iface = null;
        synchronized (getCollectableServices()) {
            CollectableService cSvc = null;
            Iterator<CollectableService> iter = getCollectableServices().iterator();
            while (iter.hasNext()) {
                cSvc = iter.next();

                InetAddress addr = (InetAddress) cSvc.getAddress();
				if (addr.equals(event.getInterfaceAddress())) {
                    synchronized (cSvc) {
                        // Got a match!
                        if (log.isDebugEnabled())
                            log.debug("interfaceReparentedHandler: got a CollectableService match for "
                                    + event.getInterface());

                        // Retrieve the CollectorUpdates object associated
                        // with
                        // this CollectableService.
                        CollectorUpdates updates = cSvc.getCollectorUpdates();
                        if (iface == null) {
                        	iface = getIpInterface(event.getNodeid().intValue(), event.getInterface());
                        }

                        // Now set the reparenting flag
                        updates.markForReparenting(oldNodeIdStr, newNodeIdStr, iface);
                        if (log.isDebugEnabled())
                            log.debug("interfaceReparentedHandler: marking "
                                    + event.getInterface()
                                    + " for reparenting for service SNMP.");
                    }
                }
            }
        }

        if (log.isDebugEnabled())
            log.debug("interfaceReparentedHandler: processing of interfaceReparented event for interface "
                    + event.getInterface() + " completed.");
    }

    /**
     * This method is responsible for handling nodeDeleted events.
     * 
     * @param event
     *            The event to process.
     * @throws InsufficientInformationException
     */
    private void handleNodeDeleted(Event event)
            throws InsufficientInformationException {
        EventUtils.checkNodeId(event);
        EventUtils.checkInterface(event);

        ThreadCategory log = log();

        Long nodeId = event.getNodeid();

        unscheduleNodeAndMarkForDeletion(nodeId);

        if (log.isDebugEnabled())
            log.debug("nodeDeletedHandler: processing of nodeDeleted event for nodeid "
                    + nodeId + " completed.");
    }

    /**
     * This method is responsible for handling nodeDeleted events.
     * 
     * @param event
     *            The event to process.
     * @throws InsufficientInformationException
     */
    private void handleNodeCategoryMembershipChanged(Event event) throws InsufficientInformationException {
        EventUtils.checkNodeId(event);
        
        ThreadCategory log = log();

        Long nodeId = event.getNodeid();

        unscheduleNodeAndMarkForDeletion(nodeId);

        if (log.isDebugEnabled()) {
            log.debug("nodeCategoryMembershipChanged: unscheduling nodeid " + nodeId + " completed.");
        }
        
        scheduleNode(nodeId.intValue(), true);
        
    }
    
	private void unscheduleNodeAndMarkForDeletion(Long nodeId) {
		// Iterate over the collectable service list and mark any entries
        // which match the deleted nodeId for deletion.
        synchronized (getCollectableServices()) {
            CollectableService cSvc = null;
            final ListIterator<CollectableService> liter = getCollectableServices().listIterator();
            while (liter.hasNext()) {
                cSvc = liter.next();

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
                    log().debug("Marking CollectableService for deletion because a node was deleted:  Service nodeid="+cSvc.getNodeId()+
                                ", deleted node:"+nodeId);
                    updates.markForDeletion();
                }

                // Now safe to remove the collectable service from
                // the collectable services list
                liter.remove();
            }
        }
	}

    /**
     * Process the event, construct a new CollectableService object
     * representing the node/interface combination, and schedule the interface
     * for collection. If any errors occur scheduling the interface no error
     * is returned.
     * 
     * @param event
     *            The event to process.
     * @throws InsufficientInformationException
     */
    private void handleNodeGainedService(Event event)
            throws InsufficientInformationException {
        EventUtils.checkNodeId(event);
        EventUtils.checkInterface(event);
        EventUtils.checkService(event);
        // Schedule the interface
        //
        scheduleForCollection(event);
    }
    
    private void handleReloadDaemonConfig(Event event) {
        final String daemonName = "Threshd";
        boolean isTarget = false;
        for (Parm parm : event.getParmCollection()) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && daemonName.equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }
        if (isTarget) {
            String thresholdsFile = ConfigFileConstants.getFileName(ConfigFileConstants.THRESHOLDING_CONF_FILE_NAME);
            String threshdFile = ConfigFileConstants.getFileName(ConfigFileConstants.THRESHD_CONFIG_FILE_NAME);
            String targetFile = thresholdsFile; // Default
            for (Parm parm : event.getParmCollection()) {
                if (EventConstants.PARM_CONFIG_FILE_NAME.equals(parm.getParmName()) && threshdFile.equalsIgnoreCase(parm.getValue().getContent())) {
                    targetFile = threshdFile;
                }
            }
            EventBuilder ebldr = null;
            try {
                // Reloading Factories
                if (targetFile.equals(thresholdsFile)) {
                    ThresholdingConfigFactory.reload();
                }
                if (targetFile.equals(threshdFile)) {
                    ThreshdConfigFactory.reload();
                    ThresholdingConfigFactory.reload(); // This is required if the threshold packages has been changed.
                }
                // Sending the threshold configuration change event
                ebldr = new EventBuilder(EventConstants.THRESHOLDCONFIG_CHANGED_EVENT_UEI, "Collectd");
                getEventIpcManager().sendNow(ebldr.getEvent());
                // Updating thresholding visitors to use the new configuration
                log().debug("handleReloadDaemonConfig: Reloading thresholding configuration in collectd");
                synchronized (m_collectableServices) {
                    for(CollectableService service: m_collectableServices) {
                        service.reinitializeThresholding();
                    }
                }
                // Preparing successful event
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, "Collectd");
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, daemonName);
                ebldr.addParam(EventConstants.PARM_CONFIG_FILE_NAME, targetFile);
            } catch (Throwable e) {
                // Preparing failed event
                log().error("handleReloadDaemonConfig: Error reloading/processing thresholds configuration: " + e.getMessage(), e);
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, "Collectd");
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, daemonName);
                ebldr.addParam(EventConstants.PARM_CONFIG_FILE_NAME, targetFile);
                ebldr.addParam(EventConstants.PARM_REASON, e.getMessage());
            }
            finally {
                if (ebldr != null) {
                    getEventIpcManager().sendNow(ebldr.getEvent());
                }
            }
        }
    }
    
    private void scheduleForCollection(Event event) {
        // This moved to here from the scheduleInterface() for better behavior
        // during initialization
        
        getCollectorConfigDao().rebuildPackageIpListMap();

        scheduleInterface(event.getNodeid().intValue(), event.getInterface(),
                          event.getService(), false);
    }

    /**
     * Process the 'primarySnmpInterfaceChanged' event. Extract the old and
     * new primary SNMP interface addresses from the event parms. Any
     * CollectableService objects located in the collectable services list
     * which match the IP address of the old primary interface and have a
     * service name of "SNMP" are flagged for deletion. This will ensure that
     * the old primary interface is no longer collected against. Finally the
     * new primary SNMP interface is scheduled. The packages are examined and
     * new CollectableService objects are created, initialized and scheduled
     * for collection.
     * 
     * @param event
     *            The event to process.
     * @throws InsufficientInformationException
     */
    private void handlePrimarySnmpInterfaceChanged(Event event)
            throws InsufficientInformationException {
        EventUtils.checkNodeId(event);
        EventUtils.checkInterface(event);

        ThreadCategory log = log();

        if (log.isDebugEnabled())
            log.debug("primarySnmpInterfaceChangedHandler:  processing primary SNMP interface changed event...");

        // Currently only support SNMP data collection.
        //
        if (!event.getService().equals("SNMP"))
            return;

        // Extract the old and new primary SNMP interface addresses from the
        // event parms.
        //
        String oldPrimaryIfAddr = null;
        String parmName = null;
        Value parmValue = null;
        String parmContent = null;

        for (Parm parm : event.getParmCollection()) {
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
        }

        if (oldPrimaryIfAddr != null) {
            // Mark the service for deletion so that it will not be
            // rescheduled
            // for
            // collection.
            //
            // Iterate over the CollectableService objects in the service
            // updates map
            // and mark any which have the same interface address as the old
            // primary SNMP interface and a service name of "SNMP" for
            // deletion.
            //
            synchronized (getCollectableServices()) {
                CollectableService cSvc = null;
                ListIterator<CollectableService> liter = getCollectableServices().listIterator();
                while (liter.hasNext()) {
                    cSvc = liter.next();

                    final InetAddress addr = (InetAddress) cSvc.getAddress();
                    final String addrString = str(addr);
					if (addrString != null && addrString.equals(oldPrimaryIfAddr)) {
                        synchronized (cSvc) {
                            // Got a match! Retrieve the CollectorUpdates
                            // object
                            // associated
                            // with this CollectableService.
                            CollectorUpdates updates = cSvc.getCollectorUpdates();

                            // Now set the deleted flag
                            updates.markForDeletion();
                            if (log.isDebugEnabled())
                                log.debug("primarySnmpInterfaceChangedHandler: marking "
                                        + oldPrimaryIfAddr
                                        + " as deleted for service SNMP.");
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
            log.debug("primarySnmpInterfaceChangedHandler: processing of primarySnmpInterfaceChanged event for nodeid "
                    + event.getNodeid() + " completed.");
    }

    /**
     * Process the event. This event is generated when a managed node which
     * supports SNMP gains a new interface. In this situation the
     * CollectableService object representing the primary SNMP interface of
     * the node must be reinitialized. The CollectableService object
     * associated with the primary SNMP interface for the node will be marked
     * for reinitialization. Reinitializing the CollectableService object
     * consists of calling the ServiceCollector.release() method followed by
     * the ServiceCollector.initialize() method which will refresh attributes
     * such as the interface key list and number of interfaces (both of which
     * most likely have changed). Reinitialization will take place the next
     * time the CollectableService is popped from an interval queue for
     * collection. If any errors occur scheduling the service no error is
     * returned.
     * 
     * @param event
     *            The event to process.
     * @throws InsufficientInformationException
     */
    private void handleReinitializePrimarySnmpInterface(Event event)
            throws InsufficientInformationException {
        EventUtils.checkNodeId(event);
        EventUtils.checkInterface(event);

        Long nodeid = event.getNodeid();
        String ipAddress = event.getInterface();

        // Mark the primary SNMP interface for reinitialization in
        // order to update any modified attributes associated with
        // the collectable service..
        //
        // Iterate over the CollectableService objects in the
        // updates map and mark any which have the same interface
        // address for reinitialization
        //
        ThreadCategory log = log();
        
        OnmsIpInterface iface = null;
        synchronized (getCollectableServices()) {
            Iterator<CollectableService> iter = getCollectableServices().iterator();
            while (iter.hasNext()) {
                CollectableService cSvc = iter.next();
        
                final InetAddress addr = (InetAddress) cSvc.getAddress();
                final String addrString = str(addr);
				if (log.isDebugEnabled())
                    log.debug("Comparing CollectableService ip address = "
                            + addrString
                            + " and event ip interface = "
                            + ipAddress);
                if (addrString != null && addrString.equals(ipAddress)) {
                    synchronized (cSvc) {
                    	if (iface == null) {
                            iface = getIpInterface(nodeid.intValue(), ipAddress);
                    	}
                        // Got a match! Retrieve the CollectorUpdates object
                        // associated
                        // with this CollectableService.
                        CollectorUpdates updates = cSvc.getCollectorUpdates();
        
                        // Now set the reinitialization flag
                        updates.markForReinitialization(iface);
                        if (log.isDebugEnabled())
                            log.debug("reinitializePrimarySnmpInterfaceHandler: marking "
                                    + ipAddress
                                    + " for reinitialization for service SNMP.");
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
    private void handleServiceDeleted(Event event)
            throws InsufficientInformationException {
        EventUtils.checkNodeId(event);
        EventUtils.checkInterface(event);
        EventUtils.checkService(event);

        ThreadCategory log = log();

        //INCORRECT; we now support all *sorts* of data collection.  This is *way* out of date
        // Currently only support SNMP data collection.
        //
        //if (!event.getService().equals("SNMP"))
        //    return;

        Long nodeId = event.getNodeid();
        String ipAddr = event.getInterface();
        String svcName = event.getService();

        // Iterate over the collectable services list and mark any entries
        // which match the nodeId/ipAddr of the deleted service
        // for deletion.
        synchronized (getCollectableServices()) {
            CollectableService cSvc = null;
            ListIterator<CollectableService> liter = getCollectableServices().listIterator();
            while (liter.hasNext()) {
                cSvc = liter.next();

                // Only interested in entries with matching nodeId, IP address
                // and service
                InetAddress addr = (InetAddress) cSvc.getAddress();
                
                //WATCH the brackets; there used to be an extra close bracket after the ipAddr comparison which borked this whole expression
                if (!(cSvc.getNodeId() == nodeId && 
                        addr.getHostName().equals(ipAddr) && 
                        cSvc.getServiceName().equals(svcName))) 
                    continue;

                synchronized (cSvc) {
                    // Retrieve the CollectorUpdates object associated with
                    // this CollectableService if one exists.
                    CollectorUpdates updates = cSvc.getCollectorUpdates();

                    // Now set the update's deletion flag so the next
                    // time it is selected for execution by the scheduler
                    // the collection will be skipped and the service will not
                    // be rescheduled.
                    log().debug("Marking CollectableService for deletion because a service was deleted:  Service nodeid="+cSvc.getNodeId()+
                                ", deleted node:"+nodeId+
                                ", service address:"+addr.getHostName()+
                                ", deleted interface:"+ipAddr+
                                ", service servicename:"+cSvc.getServiceName()+
                                ", deleted service name:"+svcName+
                                ", event source "+event.getSource());
                    updates.markForDeletion();
                }

                // Now safe to remove the collectable service from
                // the collectable services list
                liter.remove();
            }
        }

        if (log.isDebugEnabled())
            log.debug("serviceDeletedHandler: processing of serviceDeleted event for "
                    + nodeId + "/" + ipAddr + "/" + svcName + " completed.");
    }

    /**
     * <p>setScheduler</p>
     *
     * @param scheduler a {@link org.opennms.netmgt.scheduler.Scheduler} object.
     */
    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

    private Scheduler getScheduler() {
        if (m_scheduler == null) {
            createScheduler();
        }
        return m_scheduler;
    }

    /**
     * <p>setCollectorConfigDao</p>
     *
     * @param collectorConfigDao a {@link org.opennms.netmgt.dao.CollectorConfigDao} object.
     */
    public void setCollectorConfigDao(CollectorConfigDao collectorConfigDao) {
        m_collectorConfigDao = collectorConfigDao;
    }

    private CollectorConfigDao getCollectorConfigDao() {
        return m_collectorConfigDao;
    }

    /**
     * <p>setIpInterfaceDao</p>
     *
     * @param ifSvcDao a {@link org.opennms.netmgt.dao.IpInterfaceDao} object.
     */
    public void setIpInterfaceDao(IpInterfaceDao ifSvcDao) {
        m_ifaceDao = ifSvcDao;
    }

    private IpInterfaceDao getIpInterfaceDao() {
        return m_ifaceDao;
    }

    /**
     * <p>setTransactionTemplate</p>
     *
     * @param transTemplate a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public void setTransactionTemplate(TransactionTemplate transTemplate) {
        m_transTemplate = transTemplate;
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    

    /**
     * <p>setServiceCollector</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @param collector a {@link org.opennms.netmgt.collectd.ServiceCollector} object.
     */
    public void setServiceCollector(String svcName, ServiceCollector collector) {
        m_collectors.put(svcName, collector);
    }

    /**
     * <p>getServiceCollector</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.collectd.ServiceCollector} object.
     */
    public ServiceCollector getServiceCollector(String svcName) {
        return m_collectors.get(svcName);
    }

    /**
     * <p>getCollectorNames</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getCollectorNames() {
        return m_collectors.keySet();
    }

    private void instantiateCollectors() {
        log().debug("instantiateCollectors: Loading collectors");

        /*
         * Load up an instance of each collector from the config
         * so that the event processor will have them for
         * new incoming events to create collectable service objects.
         */
        Collection<Collector> collectors = getCollectorConfigDao().getCollectors();
        for (Collector collector : collectors) {
            String svcName = collector.getService();
            try {
                if (log().isDebugEnabled()) {
                    log().debug("instantiateCollectors: Loading collector " 
                                + svcName + ", classname "
                                + collector.getClassName());
                }
                Class<?> cc = Class.forName(collector.getClassName());
                ServiceCollector sc = (ServiceCollector) cc.newInstance();

                sc.initialize(Collections.<String, String>emptyMap());

                setServiceCollector(svcName, sc);
            } catch (Throwable t) {
                log().warn("instantiateCollectors: Failed to load collector "
                           + collector.getClassName() + " for service "
                           + svcName + ": " + t, t);
            }
        }
    }

}
