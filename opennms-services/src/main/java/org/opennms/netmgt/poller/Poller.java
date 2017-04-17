/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.InRestriction;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.PollOutagesConfig;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * <p>Poller class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Poller extends AbstractServiceDaemon {

    private static final Logger LOG = LoggerFactory.getLogger(Poller.class);

    private static final String LOG4J_CATEGORY = "poller";

    private boolean m_initialized = false;

    private LegacyScheduler m_scheduler = null;

    private PollerEventProcessor m_eventProcessor;

    private PollableNetwork m_network;

    @Autowired
    private QueryManager m_queryManager;

    private PollerConfig m_pollerConfig;

    private PollOutagesConfig m_pollOutagesConfig;

    private EventIpcManager m_eventMgr;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    @Autowired
    private OutageDao m_outageDao;

    @Autowired
    private TransactionTemplate m_transactionTemplate;

    @Autowired
    private PersisterFactory m_persisterFactory;

    @Autowired
    private ResourceStorageDao m_resourceStorageDao;

    public void setPersisterFactory(PersisterFactory persisterFactory) {
        m_persisterFactory = persisterFactory;
    }

    public void setOutageDao(OutageDao outageDao) {
        this.m_outageDao = outageDao;
    }

    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        this.m_monitoredServiceDao = monitoredServiceDao;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        m_transactionTemplate = transactionTemplate;
    }

    /**
     * <p>setEventIpcManager</p>
     *
     * @param eventIpcManager a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventMgr = eventIpcManager;
    }

    /**
     * <p>getEventIpcManager</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public EventIpcManager getEventIpcManager() {
        return m_eventMgr;
    }

    /**
     * <p>Constructor for Poller.</p>
     */
    public Poller() {
        super(LOG4J_CATEGORY);
    }

    /* Getters/Setters used for dependency injection */

    /**
     * <p>getEventManager</p>
     *
     * @return a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    /**
     * <p>getEventProcessor</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollerEventProcessor} object.
     */
    public PollerEventProcessor getEventProcessor() {
        return m_eventProcessor;
    }

    /**
     * <p>setEventProcessor</p>
     *
     * @param eventProcessor a {@link org.opennms.netmgt.poller.PollerEventProcessor} object.
     */
    public void setEventProcessor(PollerEventProcessor eventProcessor) {
        m_eventProcessor = eventProcessor;
    }

    /**
     * <p>getNetwork</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableNetwork} object.
     */
    public PollableNetwork getNetwork() {
        return m_network;
    }

    /**
     * <p>setNetwork</p>
     *
     * @param network a {@link org.opennms.netmgt.poller.pollables.PollableNetwork} object.
     */
    public void setNetwork(PollableNetwork network) {
        m_network = network;
    }

    /**
     * <p>setQueryManager</p>
     *
     * @param queryManager a {@link org.opennms.netmgt.poller.QueryManager} object.
     */
    void setQueryManager(QueryManager queryManager) {
        m_queryManager = queryManager;
    }

    /**
     * <p>getQueryManager</p>
     *
     * @return a {@link org.opennms.netmgt.poller.QueryManager} object.
     */
    public QueryManager getQueryManager() {
        return m_queryManager;
    }

    /**
     * <p>getPollerConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.PollerConfig} object.
     */
    public PollerConfig getPollerConfig() {
        return m_pollerConfig;
    }

    /**
     * <p>setPollerConfig</p>
     *
     * @param pollerConfig a {@link org.opennms.netmgt.config.PollerConfig} object.
     */
    public void setPollerConfig(PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

    /**
     * <p>getPollOutagesConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.PollOutagesConfig} object.
     */
    public PollOutagesConfig getPollOutagesConfig() {
        return m_pollOutagesConfig;
    }

    /**
     * <p>setPollOutagesConfig</p>
     *
     * @param pollOutagesConfig a {@link org.opennms.netmgt.config.PollOutagesConfig} object.
     */
    public void setPollOutagesConfig(PollOutagesConfig pollOutagesConfig) {
        m_pollOutagesConfig = pollOutagesConfig;
    }

    /**
     * <p>getScheduler</p>
     *
     * @return a {@link org.opennms.netmgt.scheduler.Scheduler} object.
     */
    public Scheduler getScheduler() {
        return m_scheduler;
    }

    /**
     * <p>setScheduler</p>
     *
     * @param scheduler a {@link org.opennms.netmgt.scheduler.LegacyScheduler} object.
     */
    public void setScheduler(LegacyScheduler scheduler) {
        m_scheduler = scheduler;
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {

        // serviceUnresponsive behavior enabled/disabled?
        LOG.debug("init: serviceUnresponsive behavior: {}", (getPollerConfig().isServiceUnresponsiveEnabled() ? "enabled" : "disabled"));

        createScheduler();

        try {
            LOG.debug("init: Closing outages for unmanaged services");

            m_queryManager.closeOutagesForUnmanagedServices();
        } catch (Throwable e) {
            LOG.error("init: Failed to close ouates for unmanage services", e);
        }


        // Schedule the interfaces currently in the database
        //
        try {
            LOG.debug("start: Scheduling existing interfaces");

            scheduleExistingServices();
        } catch (Throwable sqlE) {
            LOG.error("start: Failed to schedule existing interfaces", sqlE);
        }

        // Create an event receiver. The receiver will
        // receive events, process them, creates network
        // interfaces, and schedulers them.
        //
        try {
            LOG.debug("start: Creating event broadcast event processor");

            setEventProcessor(new PollerEventProcessor(this));
        } catch (Throwable t) {
            LOG.error("start: Failed to initialized the broadcast event receiver", t);

            throw new UndeclaredThrowableException(t);
        }

        m_initialized = true;

    }

    private void createScheduler() {

        // Create a scheduler
        //
        try {
            LOG.debug("init: Creating poller scheduler");

            setScheduler(new LegacyScheduler("Poller", getPollerConfig().getThreads()));
        } catch (RuntimeException e) {
            LOG.error("init: Failed to create poller scheduler", e);
            throw e;
        }
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        // get the category logger
        // start the scheduler
        //
        try {
            if (LOG.isDebugEnabled())
                LOG.debug("start: Starting poller scheduler");

            getScheduler().start();
        } catch (RuntimeException e) {
            LOG.error("start: Failed to start scheduler", e);
            throw e;
        }
    }

    /**
     * <p>onStop</p>
     */
    @Override
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

    /**
     * <p>onPause</p>
     */
    @Override
    protected void onPause() {
        getScheduler().pause();
    }

    /**
     * <p>onResume</p>
     */
    @Override
    protected void onResume() {
        getScheduler().resume();
    }

    /**
     * <p>getServiceMonitor</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.poller.ServiceMonitor} object.
     */
    public ServiceMonitor getServiceMonitor(String svcName) {
        return getPollerConfig().getServiceMonitor(svcName);
    }

    private void scheduleExistingServices() throws Exception {
        scheduleServices();

        getNetwork().recalculateStatus();
        getNetwork().propagateInitialCause();
        getNetwork().resetStatusChanged();


        // Debug dump pollable network
        //
        LOG.debug("scheduleExistingServices: dumping content of pollable network: ");
        getNetwork().dump();
    }

    /**
     * <p>scheduleService</p>
     *
     * @param nodeId a int.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    public void scheduleService(final int nodeId, final String nodeLabel, final String ipAddr, final String svcName) {
        final String normalizedAddress = InetAddressUtils.normalize(ipAddr);
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
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    m_transactionTemplate.execute(new TransactionCallbackWithoutResult() {

                        @Override
                        protected void doInTransactionWithoutResult(TransactionStatus arg0) {
                            final OnmsMonitoredService service = m_monitoredServiceDao.get(nodeId, InetAddressUtils.addr(ipAddr), svcName);
                            if (scheduleService(service)) {
                                svcNode.recalculateStatus();
                                svcNode.processStatusChange(new Date());
                            } else {
                                LOG.warn("Attempt to schedule service {}/{}/{} found no active service", nodeId, normalizedAddress, svcName);
                            }
                        }
                    });
                }
            };
            node.withTreeLock(r);

        } catch (final Throwable e) {
            LOG.error("Unable to schedule service {}/{}/{}", nodeId, normalizedAddress, svcName);
        }
    }

    private int scheduleServices() {
        final Criteria criteria = new Criteria(OnmsMonitoredService.class);
        criteria.addRestriction(new InRestriction("status", Arrays.asList("A", "N")));

        return m_transactionTemplate.execute(new TransactionCallback<Integer>() {
            @Override
            public Integer doInTransaction(TransactionStatus arg0) {
                final List<OnmsMonitoredService> services =  m_monitoredServiceDao.findMatching(criteria);
                for (OnmsMonitoredService service : services) {
                    scheduleService(service);
                }
                return services.size();
            }
        });
    }

    private boolean scheduleService(OnmsMonitoredService service) {
        final OnmsIpInterface iface = service.getIpInterface();
        final Set<OnmsOutage> outages = service.getCurrentOutages();
        final OnmsOutage outage = (outages == null || outages.size() < 1 ? null : outages.iterator().next());
        final OnmsEvent event = (outage == null ? null : outage.getServiceLostEvent());
        final String ipAddr = InetAddressUtils.str(iface.getIpAddress());
        final String serviceName = service.getServiceName();
        boolean active = "A".equals(service.getStatus());
        final Number svcLostEventId = event == null ? null : event.getId();
        final Date ifLostService = outage == null ? null : outage.getIfLostService();
        final String svcLostUei = event == null ? null : event.getEventUei();

        closeOutageIfSvcLostEventIsMissing(outage);

        // We don't want to adjust the management state of the service if we're
        // on a machine that uses multiple servers with access to the same database
        // so check the value of OpennmsServerConfigFactory.getInstance().verifyServer()
        // before doing any updates.
        final Package pkg = findPackageForService(ipAddr, serviceName);
        final boolean verifyServer = OpennmsServerConfigFactory.getInstance().verifyServer();
        if (pkg == null) {
            if(active && !verifyServer){
                LOG.warn("Active service {} on {} not configured for any package. Marking as Not Polled.", serviceName, ipAddr);
                updateServiceStatus(service, "N");
            }
            return false;
        } else if (!active && !verifyServer) {
            LOG.info("Active service {} on {} is now configured for a package. Marking as active.", serviceName, ipAddr);
            updateServiceStatus(service, "A");
        }

        ServiceMonitor monitor = m_pollerConfig.getServiceMonitor(serviceName);
        if (monitor == null) {
            LOG.info("Could not find service monitor associated with service {}", serviceName);
            return false;
        }

        InetAddress addr;
        addr = InetAddressUtils.addr(ipAddr);
        if (addr == null) {
            LOG.error("Could not convert {} as an InetAddress {}", ipAddr, ipAddr);
            return false;
        }

        PollableService svc = getNetwork().createService(service.getNodeId(), iface.getNode().getLabel(), addr, serviceName);
        PollableServiceConfig pollConfig = new PollableServiceConfig(svc, m_pollerConfig, m_pollOutagesConfig, pkg,
                getScheduler(), m_persisterFactory, m_resourceStorageDao);
        svc.setPollConfig(pollConfig);
        synchronized(svc) {
            if (svc.getSchedule() == null) {
                Schedule schedule = new Schedule(svc, pollConfig, getScheduler());
                svc.setSchedule(schedule);
            }
        }

        if (svcLostEventId == null) {
            if (svc.getParent().getStatus().isUnknown()) {
                svc.updateStatus(PollStatus.up());
            } else {
                svc.updateStatus(svc.getParent().getStatus());
            }
        } else {
            svc.updateStatus(PollStatus.down());

            PollEvent cause = new DbPollEvent(svcLostEventId.intValue(), svcLostUei, ifLostService);

            svc.setCause(cause);

        }

        svc.schedule();

        return true;

    }

    private void updateServiceStatus(OnmsMonitoredService service, String status) {
        service.setStatus(status);
        m_monitoredServiceDao.saveOrUpdate(service);
    }

    /**
     * This method should be called before scheduling services with outstanding
     * outages for the first time.
     *
     * If an outage is open, but has no lost service event, we will mark it as closed
     * with the current timestamp. This can happen if the poller daemon is stopped after
     * creating the outage record, but before the event was received back from the event bus.
     *
     * We close the outage immediately, as opposed to marking the service's initial state
     * as down since we do not know the cause, and determining the cause from the current
     * state of the database is error prone.
     *
     * Closing the outage immediately also prevents the daemon from creating
     * duplicate outstanding outage records.
     */
    private void closeOutageIfSvcLostEventIsMissing(final OnmsOutage outage) {
        if (outage == null || outage.getServiceLostEvent() != null || outage.getIfRegainedService() != null) {
            // Nothing to do
            return;
        }

        LOG.warn("Outage {} was left open without a lost service event. "
                + "The outage will be closed.", outage);
        final Date now = new Date();
        outage.setIfRegainedService(now);
        m_outageDao.update(outage);
    }

    Package findPackageForService(String ipAddr, String serviceName) {
        Enumeration<Package> en = m_pollerConfig.enumeratePackage();
        Package lastPkg = null;

        while (en.hasMoreElements()) {
            Package pkg = (Package)en.nextElement();
            if (pollableServiceInPackage(ipAddr, serviceName, pkg))
                lastPkg = pkg;
        }
        return lastPkg;
    }

    /**
     * <p>pollableServiceInPackage</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     * @return a boolean.
     */
    protected boolean pollableServiceInPackage(String ipAddr, String serviceName, Package pkg) {

        if (pkg.getRemote()) {
            LOG.debug("pollableServiceInPackage: this package: {}, is a remote monitor package.", pkg.getName());
            return false;
        }

        if (!m_pollerConfig.isServiceInPackageAndEnabled(serviceName, pkg)) return false;

        boolean inPkg = m_pollerConfig.isInterfaceInPackage(ipAddr, pkg);

        if (inPkg) return true;

        if (m_initialized) {
            m_pollerConfig.rebuildPackageIpListMap();
            return m_pollerConfig.isInterfaceInPackage(ipAddr, pkg);
        }

        return false;
    }

    /**
     * <p>packageIncludesIfAndSvc</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean packageIncludesIfAndSvc(Package pkg, String ipAddr, String svcName) {
        if (!getPollerConfig().isServiceInPackageAndEnabled(svcName, pkg)) {
            LOG.debug("packageIncludesIfAndSvc: address/service: {}/{} not scheduled, service is not enabled or does not exist in package: {}", ipAddr, svcName, pkg.getName());
            return false;
        }

        // Is the interface in the package?
        //
        if (!getPollerConfig().isInterfaceInPackage(ipAddr, pkg)) {

            if (m_initialized) {
                getPollerConfig().rebuildPackageIpListMap();
                if (!getPollerConfig().isInterfaceInPackage(ipAddr, pkg)) {
                    LOG.debug("packageIncludesIfAndSvc: interface {} gained service {}, but the interface was not in package: {}", ipAddr, svcName, pkg.getName());
                    return false;
                }
            } else {
                LOG.debug("packageIncludesIfAndSvc: address/service: {}/{} not scheduled, interface does not belong to package: {}", ipAddr, svcName, pkg.getName());
                return false;
            }
        }
        return true;
    }

    /**
     * <p>refreshServicePackages</p>
     */
    public void refreshServicePackages() {
        PollableVisitor visitor = new PollableVisitorAdaptor() {
            @Override
            public void visitService(PollableService service) {
                service.refreshConfig();
            }
        };
        getNetwork().visit(visitor);
    }

    /**
     * <p>refreshServiceThresholds</p>
     */
    public void refreshServiceThresholds() {
        PollableVisitor visitor = new PollableVisitorAdaptor() {
            @Override
            public void visitService(PollableService service) {
                service.refreshThresholds();
            }
        };
        getNetwork().visit(visitor);
    }

    /**
     * Returns the number of polls that have been executed so far.
     *
     * @return the number of polls that have been executed
     */
    public long getNumPolls() {
        if (m_scheduler != null) {
            return m_scheduler.getNumTasksExecuted();
        } else {
            return 0L;
        }
    }

    public static String getLoggingCategory() {
        return LOG4J_CATEGORY;
    }
}    
