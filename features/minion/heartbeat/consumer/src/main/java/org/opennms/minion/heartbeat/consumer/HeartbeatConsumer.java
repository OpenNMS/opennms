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
package org.opennms.minion.heartbeat.consumer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.minion.heartbeat.common.HeartbeatModule;
import org.opennms.minion.heartbeat.common.MinionIdentityDTO;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class HeartbeatConsumer implements MessageConsumer<MinionIdentityDTO, MinionIdentityDTO>, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(HeartbeatConsumer.class);

    private static final boolean PROVISIONING = Boolean.valueOf(System.getProperty("opennms.minion.provisioning", "true"));
    private static final String PROVISIONING_FOREIGN_SOURCE_PATTERN = System.getProperty("opennms.minion.provisioning.foreignSourcePattern", "Minions");
    // Default queue size is chosen as tests indicated that provisioning can import 500 nodes in 30 secs.
    private static final Integer DEFAULT_QUEUE_SIZE = 500;
    private static final Integer queueSize = SystemProperties.getInteger("opennms.minion.provisioning.queueSize", DEFAULT_QUEUE_SIZE);

    /**
     * Services on the Minion nodes must be associated to *some* interface, so we use the following constant:
     */
    private static final String MINION_INTERFACE = "127.0.0.1";

    private static String DEFAULT_SNMP_POLICY = "Minion-SNMP-Policy";

    private static String DEFAULT_SNMP_DETECTOR = "SNMP";
    private static String DEFAULT_JMX_DETECTOR = "JMX-Minion";

    private static final HeartbeatModule heartbeatModule = new HeartbeatModule();

    private final AtomicInteger numofRejected = new AtomicInteger(0);

    @Autowired
    private MinionDao minionDao;

    @Autowired
    private MessageConsumerManager messageConsumerManager;

    @Autowired
    @Qualifier("deployed")
    private ForeignSourceRepository deployedForeignSourceRepository;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy eventProxy;

    @Autowired
    @Qualifier("eventSubscriptionService")
    private EventSubscriptionService eventSubscriptionService;

    @Autowired
    private NodeDao nodeDao;

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("minion-provision-handler")
            .build();

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(queueSize), threadFactory, new RejectedExecutionHandlerImpl());

    @Override
    @Transactional
    public void handleMessage(MinionIdentityDTO minionHandle) {
        LOG.info("Received heartbeat for Minion with id: {} at location: {}",
                minionHandle.getId(), minionHandle.getLocation());

        OnmsMinion minion = minionDao.findById(minionHandle.getId());
        if (minion == null) {
            minion = new OnmsMinion();
            minion.setId(minionHandle.getId());

            // The real location is filled in below, but we set this to null
            // for now to detect requisition changes
            minion.setLocation(null);
        }

        if (!Objects.isNull(minionHandle.getVersion()) &&
                (Objects.isNull(minion.getVersion()) || !minion.getVersion().equals(minionHandle.getVersion()))) {
            minion.setVersion(minionHandle.getVersion());
        }

        final String prevLocation = minion.getLocation();
        final String nextLocation = minionHandle.getLocation();

        minion.setLocation(minionHandle.getLocation());

        if (minionHandle.getTimestamp() == null) {
            // The heartbeat does not contain a timestamp - use the current time
            minion.setLastUpdated(new Date());
            LOG.info("Received heartbeat without a timestamp: {}", minionHandle);
        } else if (minion.getLastUpdated() == null) {
            // The heartbeat does contain a timestamp, and we don't have
            // one set yet, so use whatever we've been given
            minion.setLastUpdated(minionHandle.getTimestamp());
        } else if (minionHandle.getTimestamp().after(minion.getLastUpdated())) {
            // The timestamp in the heartbeat is more recent than the one we
            // have stored, so update it
            minion.setLastUpdated(minionHandle.getTimestamp());
        } else {
            // The timestamp in the heartbeat is earlier than the
            // timestamp we have stored, so ignore it
            LOG.info("Ignoring stale timestamp from heartbeat: {}", minionHandle);
        }

        minionDao.saveOrUpdate(minion);

        // Provision the minions node in a separate thread.
        final OnmsMinion onmsMinion = minion;

        executor.execute(() -> {

            this.provision(onmsMinion,
                    prevLocation,
                    nextLocation);

            if (prevLocation == null) {
                final EventBuilder eventBuilder = new EventBuilder(EventConstants.MONITORING_SYSTEM_ADDED_UEI,
                        "OpenNMS.Minion.Heartbeat");
                eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_TYPE, OnmsMonitoringSystem.TYPE_MINION);
                eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_ID, minionHandle.getId());
                eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_LOCATION, nextLocation);
                try {
                    eventProxy.send(eventBuilder.getEvent());
                } catch (final EventProxyException e) {
                    throw new DataAccessResourceFailureException("Unable to send event", e);
                }
            } else if (!prevLocation.equals(nextLocation)) {

                final EventBuilder eventBuilder = new EventBuilder(EventConstants.MONITORING_SYSTEM_LOCATION_CHANGED_UEI,
                        "OpenNMS.Minion.Heartbeat");
                eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_TYPE, OnmsMonitoringSystem.TYPE_MINION);
                eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_ID, minionHandle.getId());
                eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_PREV_LOCATION, prevLocation);
                eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_LOCATION, nextLocation);
                try {
                    eventProxy.send(eventBuilder.getEvent());
                } catch (final EventProxyException e) {
                    throw new DataAccessResourceFailureException("Unable to send event", e);
                }
            }
        });



    }

    private void provision(final OnmsMinion minion,
                           final String prevLocation,
                           final String nextLocation) {
        // Return fast if automatic provisioning is disabled
        if (!PROVISIONING) {
            return;
        }

        // Return fast until the provisioner is running to pick up the events sent below
        if (!this.eventSubscriptionService.hasEventListener(EventConstants.RELOAD_IMPORT_UEI)) {
            return;
        }

        final String prevForeignSource = String.format(PROVISIONING_FOREIGN_SOURCE_PATTERN, prevLocation);
        final String nextForeignSource = String.format(PROVISIONING_FOREIGN_SOURCE_PATTERN, nextLocation);

        PluginConfig policy = new PluginConfig(DEFAULT_SNMP_POLICY, "org.opennms.netmgt.provision.persist.policies.MatchingSnmpInterfacePolicy");
        policy.addParameter("ifDescr", "~^docker.*$");
        policy.addParameter("action", "DO_NOT_PERSIST");
        policy.addParameter("matchBehavior", "ALL_PARAMETERS");

        PluginConfig snmpDetector = new PluginConfig(DEFAULT_SNMP_DETECTOR, "org.opennms.netmgt.provision.detector.snmp.SnmpDetector");
        PluginConfig jmxDetector = new PluginConfig(DEFAULT_JMX_DETECTOR, "org.opennms.netmgt.provision.detector.jmx.Jsr160Detector");
        jmxDetector.addParameter("port", "1299");
        jmxDetector.addParameter("factory", "PASSWORD_CLEAR");
        jmxDetector.addParameter("username", "admin");
        jmxDetector.addParameter("password", "admin");
        jmxDetector.addParameter("protocol", "rmi");
        jmxDetector.addParameter("urlPath", "/karaf-minion");
        jmxDetector.addParameter("timeout", "3000");
        jmxDetector.addParameter("retries", "2");
        jmxDetector.addParameter("type", "default");

        // Return if minion with this foreignId and location already exists.
        String foreignId = minion.getLabel() != null ? minion.getLabel() : minion.getId();
        List<OnmsNode> nodes = nodeDao.findByForeignIdForLocation(foreignId, nextLocation);
        if (!nodes.isEmpty()) {
            //check for existing requisitions the policy and detectors are in place
            final ForeignSource foreignSource = deployedForeignSourceRepository.getForeignSource(prevForeignSource);
            if (foreignSource.getPolicy(DEFAULT_SNMP_POLICY) == null || foreignSource.getDetector(DEFAULT_SNMP_DETECTOR) == null || foreignSource.getDetector(DEFAULT_JMX_DETECTOR) == null ) {
                foreignSource.addPolicy(policy);
                foreignSource.addDetector(snmpDetector);
                foreignSource.addDetector(jmxDetector);
                deployedForeignSourceRepository.save(foreignSource);
            }
            return;
        }

        final Set<String> alteredForeignSources = Sets.newHashSet();

        // Remove the node from the previous requisition, if location has changed
        if (!Objects.equals(prevForeignSource, nextForeignSource)) {
            final Requisition prevRequisition = this.deployedForeignSourceRepository.getRequisition(prevForeignSource);
            if (prevRequisition != null && prevRequisition.getNode(minion.getId()) != null) {
                prevRequisition.deleteNode(minion.getId());
                prevRequisition.updateDateStamp();

                deployedForeignSourceRepository.save(prevRequisition);
                deployedForeignSourceRepository.flush();

                alteredForeignSources.add(prevForeignSource);
            }
        }

        Requisition nextRequisition = deployedForeignSourceRepository.getRequisition(nextForeignSource);
        // check that existing foreignId requisition have detectors and policies in place
        if (nextRequisition != null) {
            final ForeignSource foreignSource = deployedForeignSourceRepository.getForeignSource(nextForeignSource);
            if (foreignSource.getPolicy(DEFAULT_SNMP_POLICY) == null || foreignSource.getDetector(DEFAULT_SNMP_DETECTOR) == null || foreignSource.getDetector(DEFAULT_JMX_DETECTOR) == null ) {
                foreignSource.addPolicy(policy);
                foreignSource.addDetector(snmpDetector);
                foreignSource.addDetector(jmxDetector);
                deployedForeignSourceRepository.save(foreignSource);

                alteredForeignSources.add(nextForeignSource);
            }
        }
        if (nextRequisition == null) {
            nextRequisition = new Requisition(nextForeignSource);
            nextRequisition.updateDateStamp();

            // We have to save the requisition before we can alter the according foreign source definition
            deployedForeignSourceRepository.save(nextRequisition);

            // Remove and replace all policies and detectors from the foreign source with defaults
            // Default Snmp detector and policy for appliances
            final ForeignSource foreignSource = deployedForeignSourceRepository.getForeignSource(nextForeignSource);
            foreignSource.setDetectors(List.of(snmpDetector,jmxDetector));
            foreignSource.setPolicies(List.of(policy));

            deployedForeignSourceRepository.save(foreignSource);

            alteredForeignSources.add(nextForeignSource);
        }

        RequisitionNode requisitionNode = nextRequisition.getNode(minion.getId());
        if (requisitionNode == null) {
            final RequisitionInterface requisitionInterface = new RequisitionInterface();
            requisitionInterface.setIpAddr(MINION_INTERFACE);
            requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
            ensureServicesAreOnInterface(requisitionInterface);

            requisitionNode = new RequisitionNode();
            requisitionNode.setNodeLabel(minion.getId());
            requisitionNode.setForeignId(foreignId);
            requisitionNode.setLocation(minion.getLocation());
            requisitionNode.putInterface(requisitionInterface);

            nextRequisition.putNode(requisitionNode);
            nextRequisition.setDate(new Date());
            deployedForeignSourceRepository.save(nextRequisition);
            deployedForeignSourceRepository.flush();

            alteredForeignSources.add(nextForeignSource);
        } else {
            // Change location in requisition.
            if (!prevLocation.equals(nextLocation)) {
                requisitionNode.setLocation(nextLocation);
            }
            // The node already exists in the requisition
            RequisitionInterface requisitionInterface = requisitionNode.getInterface(MINION_INTERFACE);
            if (requisitionInterface == null) {
                // The interface was deleted, add it again
                requisitionInterface = new RequisitionInterface();
                requisitionInterface.setIpAddr(MINION_INTERFACE);
                requisitionNode.putInterface(requisitionInterface);
            }

            if (ensureServicesAreOnInterface(requisitionInterface)) {
                // We've altered the set of services on the interface
                nextRequisition.setDate(new Date());
                deployedForeignSourceRepository.save(nextRequisition);
                deployedForeignSourceRepository.flush();

                alteredForeignSources.add(nextForeignSource);
            }
        }

        for (final String alteredForeignSource : alteredForeignSources) {
            final EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, "Web");
            eventBuilder.addParam(EventConstants.PARM_URL, String.valueOf(deployedForeignSourceRepository.getRequisitionURL(alteredForeignSource)));

            try {
                eventProxy.send(eventBuilder.getEvent());
            } catch (final EventProxyException e) {
                throw new DataAccessResourceFailureException("Unable to send event to import group " + alteredForeignSource, e);
            }
        }
    }

    private static boolean ensureServicesAreOnInterface(RequisitionInterface requisitionInterface) {
        final List<RequisitionMonitoredService> minionServices = new ArrayList<>();

        final RequisitionMonitoredService heartbeatService = new RequisitionMonitoredService();
        heartbeatService.setServiceName("Minion-Heartbeat");
        minionServices.add(heartbeatService);

        final RequisitionMonitoredService rpcService = new RequisitionMonitoredService();
        rpcService.setServiceName("Minion-RPC");
        minionServices.add(rpcService);

        final RequisitionMonitoredService jmxService = new RequisitionMonitoredService();
        jmxService.setServiceName("JMX-Minion");
        minionServices.add(jmxService);

        // Add missing services
        boolean didAlterInterface = false;
        for (RequisitionMonitoredService svc : minionServices) {
            if (requisitionInterface.getMonitoredService(svc.getServiceName()) == null) {
                requisitionInterface.putMonitoredService(svc);
                didAlterInterface = true;
            }
        }

        return didAlterInterface;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Automatically register the consumer on initialization
        messageConsumerManager.registerConsumer(this);
    }

    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public SinkModule<MinionIdentityDTO, MinionIdentityDTO> getModule() {
        return heartbeatModule;
    }

    @VisibleForTesting
    void setMinionDao(MinionDao minionDao) {
        this.minionDao = minionDao;
    }

    @VisibleForTesting
    void setEventProxy(EventProxy eventProxy) {
        this.eventProxy = eventProxy;
    }

    @VisibleForTesting
    void setDeployedForeignSourceRepository(ForeignSourceRepository deployedForeignSourceRepository) {
        this.deployedForeignSourceRepository = deployedForeignSourceRepository;
    }

    @VisibleForTesting
    ForeignSourceRepository getDeployedForeignSourceRepository() {
        return deployedForeignSourceRepository;
    }

    @VisibleForTesting
    public void setEventSubscriptionService(EventSubscriptionService eventSubscriptionService) {
        this.eventSubscriptionService = eventSubscriptionService;
    }

    @VisibleForTesting
    void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }


    private class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
            // Ignore.
            LOG.debug("Provisioning queue for Minions with size {} is full , dropping heartbeat message ", threadPoolExecutor.getQueue().size());
            numofRejected.incrementAndGet();
        }
    }

    @VisibleForTesting
    AtomicInteger getNumofRejected() {
        return numofRejected;
    }
}
