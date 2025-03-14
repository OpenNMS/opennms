/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.poller;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.InRestriction;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.dao.outages.api.ReadablePollOutagesDao;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OutageDao;
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
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.annotations.VisibleForTesting;

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
    private ThresholdingService m_thresholdingService;

    @Autowired
    private LocationAwarePollerClient m_locationAwarePollerClient;
    
    @Autowired
    private ReadablePollOutagesDao m_pollOutagesDao;

    @Autowired()
    @Qualifier("deviceConfigMonitorAdaptor")
    private ServiceMonitorAdaptor serviceMonitorAdaptor;

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
     * @param eventIpcManager a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventMgr = eventIpcManager;
    }

    /**
     * <p>getEventIpcManager</p>
     *
     * @return a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
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
     * <p>getPollOutagesDao</p>
     */
    ReadablePollOutagesDao getPollOutagesDao() {
        return m_pollOutagesDao;
    }
    
    @VisibleForTesting
    void setPollOutagesDao(ReadablePollOutagesDao pollOutagesDao) {
        m_pollOutagesDao = Objects.requireNonNull(pollOutagesDao);
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

    public void setLocationAwarePollerClient(LocationAwarePollerClient locationAwarePollerClient) {
        m_locationAwarePollerClient = locationAwarePollerClient;
    }

    public void setServiceMonitorAdaptor(ServiceMonitorAdaptor serviceMonitorAdaptor) {
        this.serviceMonitorAdaptor = serviceMonitorAdaptor;
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

        setScheduler(null);
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
     * @param nodeId a int.
     * @param nodeLabel a {@link String} object.
     * @param nodeLocation a {@link String} object.
     * @param ipAddr a {@link String} object.
     * @param svcName a {@link String} object.
     * @param pollableNode a {@link PollableNode} object
     */
    public void scheduleService(final int nodeId, final String nodeLabel, final String nodeLocation, final String ipAddr, final String svcName, PollableNode pollableNode) {
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
                    node = getNetwork().createNode(nodeId, nodeLabel, nodeLocation);
                    // In case of module reload, all existing PollableNodes gets deleted and re-created.
                    // It is necessary to retrieve the previous state of node and reset the change of status.
                    // Otherwise this may produce duplicate node down events, see NMS-12681
                    if(pollableNode != null) {
                        node.updateStatus(pollableNode.getStatus());
                        node.setCause(pollableNode.getCause());
                        node.resetStatusChanged();
                    }
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
                            if (scheduleService(service, service.getCurrentOutages())) {
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
            LOG.error("Unable to schedule service {}/{}/{}", nodeId, normalizedAddress, svcName, e);
        }
    }

    private int scheduleServices() {
        final Criteria criteria = new Criteria(OnmsMonitoredService.class);
        criteria.addRestriction(new InRestriction("status", Arrays.asList("A", "N")));

        return m_transactionTemplate.execute(new TransactionCallback<Integer>() {
            @Override
            public Integer doInTransaction(TransactionStatus arg0) {
                final List<OnmsMonitoredService> services =  m_monitoredServiceDao.findMatching(criteria);
                final Map<Integer, Set<OnmsOutage>> outagesByServiceId = m_outageDao.currentOutagesByServiceId();
                for (OnmsMonitoredService service : services) {
                    scheduleService(service, outagesByServiceId.getOrDefault(service.getId(), Collections.emptySet()));
                }
                return services.size();
            }
        });
    }

    private boolean scheduleService(OnmsMonitoredService service, Set<OnmsOutage> outages) {
        final OnmsIpInterface iface = service.getIpInterface();
        final OnmsOutage outage = (outages == null || outages.size() < 1 ? null : outages.iterator().next());
        final OnmsEvent event = (outage == null ? null : outage.getServiceLostEvent());
        final String ipAddr = InetAddressUtils.str(iface.getIpAddress());
        final String serviceName = service.getServiceName();
        boolean active = "A".equals(service.getStatus());
        final Number svcLostEventId = event == null ? null : event.getId();
        final Date ifLostService = outage == null ? null : outage.getIfLostService();
        final String svcLostUei = event == null ? null : event.getEventUei();

        closeOutageIfSvcLostEventIsMissing(outage);

        final Package pkg = this.findPackageForService(ipAddr, serviceName);
        if (pkg == null) {
            if(active){
                LOG.warn("Active service {} on {} not configured for any package. Marking as Not Polled.", serviceName, ipAddr);
                updateServiceStatus(service, "N");
            }
            return false;
        } else if (!active) {
            LOG.info("Active service {} on {} is now configured for a package. Marking as active.", serviceName, ipAddr);
            updateServiceStatus(service, "A");
        }

        InetAddress addr;
        addr = InetAddressUtils.addr(ipAddr);
        if (addr == null) {
            LOG.error("Could not convert {} as an InetAddress {}", ipAddr, ipAddr);
            return false;
        }

        PollableService svc = getNetwork().createService(service.getNodeId(), iface.getNode().getLabel(), iface.getNode().getLocation().getLocationName(), addr, serviceName);
        PollableServiceConfig pollConfig = new PollableServiceConfig(svc, m_pollerConfig, pkg,
                                                                     getScheduler(), m_persisterFactory, m_thresholdingService,
                                                                     m_locationAwarePollerClient, m_pollOutagesDao, serviceMonitorAdaptor);
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
            svc.updateStatus(PollStatus.down("Service has lost event : " + svcLostEventId));

            PollEvent cause = new DbPollEvent(svcLostEventId.intValue(), svcLostUei, ifLostService);

            svc.setCause(cause);

        }

        svc.schedule();

        return true;

    }

    private Package findPackageForService(String ipAddr, String serviceName) {
        if (m_initialized) {
            // Only rebuild the map when services are scheduled after the initial initialization
            m_pollerConfig.rebuildPackageIpListMap();
        }
        return this.m_pollerConfig.findPackageForService(ipAddr, serviceName);
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
