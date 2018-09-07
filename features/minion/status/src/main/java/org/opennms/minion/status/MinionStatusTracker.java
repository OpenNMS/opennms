/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.minion.status;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.logging.Logging;
import org.opennms.core.logging.Logging.MDCCloseable;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.netmgt.model.outage.CurrentOutageDetails;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.util.Assert;

@EventListener(name="minionStatusTracker", logPrefix=MinionStatusTracker.LOG_PREFIX)
public class MinionStatusTracker implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(MinionStatusTracker.class);

    ScheduledExecutorService m_executor = Executors.newSingleThreadScheduledExecutor();

    public static final String LOG_PREFIX = "minion";

    private static final String OUTAGE_CREATED_EVENT_UEI = EventConstants.OUTAGE_CREATED_EVENT_UEI;
    private static final String OUTAGE_RESOLVED_EVENT_UEI = EventConstants.OUTAGE_RESOLVED_EVENT_UEI;

    static final String MINION_HEARTBEAT = "Minion-Heartbeat";
    static final String MINION_RPC = "Minion-RPC";

    @Autowired
    NodeDao m_nodeDao;

    @Autowired
    MinionDao m_minionDao;

    @Autowired
    ServiceTypeDao m_serviceTypeDao;

    @Autowired
    OutageDao m_outageDao;

    @Autowired
    TransactionOperations m_transactionOperations;

    private long m_refresh = TimeUnit.MINUTES.toMillis(5);

    Map<Integer,OnmsMinion> m_minionNodes = new ConcurrentHashMap<>();
    Map<String,OnmsMinion> m_minions = new ConcurrentHashMap<>();
    Map<String,AggregateMinionStatus> m_state = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(LOG_PREFIX)) {
            LOG.info("Starting minion status tracker.");
            Assert.notNull(m_nodeDao);
            Assert.notNull(m_minionDao);
            Assert.notNull(m_serviceTypeDao);
            Assert.notNull(m_outageDao);
            Assert.notNull(m_transactionOperations);
            final Runnable command = new Runnable() {
                @Override public void run() {
                    try {
                        refresh();
                    } catch (final Throwable t) {
                        LOG.warn("Failed to refresh minion status from the database.", t);
                    }
                }
            };
            // sanity check every 5 minutes by default
            m_executor.scheduleAtFixedRate(command, 0, m_refresh, TimeUnit.MILLISECONDS);
        }
    }

    public long getRefresh() {
        return m_refresh;
    }

    public void setRefresh(final long refresh) {
        m_refresh = refresh;
    }

    public Collection<OnmsMinion> getMinions() {
        return m_minions.values();
    }

    public MinionStatus getStatus(final String foreignId) {
        return m_state.get(foreignId);
    }

    public MinionStatus getStatus(final OnmsMinion minion) {
        return m_state.get(minion.getId());
    }

    @EventHandler(uei=EventConstants.MONITORING_SYSTEM_ADDED_UEI)
    public void onMonitoringSystemAdded(final Event e) {
        runInLoggingTransaction(() -> {
            final String id = e.getParm(EventConstants.PARAM_MONITORING_SYSTEM_ID).toString();
            LOG.debug("Monitoring system added: {}", id);
            if (id != null) {
                m_state.put(id, AggregateMinionStatus.up());
            }
        });
    }

    @EventHandler(uei=EventConstants.MONITORING_SYSTEM_DELETED_UEI)
    public void onMonitoringSystemDeleted(final Event e) {
        runInLoggingTransaction(() -> {
            final String id = e.getParm(EventConstants.PARAM_MONITORING_SYSTEM_ID).toString();
            if (id != null) {
                LOG.debug("Monitoring system removed: {}", id);
                final OnmsMinion minion = m_minions.get(id);
                m_minions.remove(id);
                m_state.remove(id);
                if (minion != null) {
                    final Iterator<Entry<Integer,OnmsMinion>> it = m_minionNodes.entrySet().iterator();
                    while (it.hasNext()) {
                        final Entry<Integer,OnmsMinion> entry = it.next();
                        if (entry.getValue().getId().equals(minion.getId())) {
                            it.remove();
                            break;
                        };
                    }
                }
            } else {
                LOG.warn("Monitoring system removed event received, but unable to determine ID: {}", e);
            }
        });
    }

    @EventHandler(uei=EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)
    public void onNodeGainedService(final Event e) {
        if (!MINION_HEARTBEAT.equals(e.getService()) && !MINION_RPC.equals(e.getService())) {
            return;
        }

        runInLoggingTransaction(() -> {
            assertHasNodeId(e);

            final Integer nodeId = e.getNodeid().intValue();
            final OnmsMinion minion = getMinionForNodeId(nodeId);
            if (minion == null) {
                LOG.debug("No minion found for node ID {}", nodeId);
                return;
            }

            final String minionId = minion.getId();

            LOG.debug("Node {}/{} gained a Minion service: {}", nodeId, minionId, e.getService());

            AggregateMinionStatus state = m_state.get(minionId);
            if (state == null) {
                LOG.info("Found new Minion node: {}/{}", nodeId, minionId);
                state = "down".equals(minion.getStatus())? AggregateMinionStatus.down() : AggregateMinionStatus.up();
            }

            if (MINION_HEARTBEAT.equals(e.getService())) {
                state = state.heartbeatUp();
            } else if (MINION_RPC.equals(e.getService())) {
                state = state.rpcUp();
            }
            updateStateIfChanged(minion, state, m_state.get(minionId));
        });
    }

    @EventHandler(uei=EventConstants.NODE_DELETED_EVENT_UEI)
    public void onNodeDeleted(final Event e) {
        runInLoggingTransaction(() -> {
            assertHasNodeId(e);
            final Integer nodeId = e.getNodeid().intValue();
            OnmsMinion minion = getMinionForNodeId(nodeId);
            m_minionNodes.remove(nodeId);
            if (minion != null) {
                final String minionId = minion.getId();
                LOG.debug("Minion node {}({}) deleted.", nodeId, minionId);
                updateStateIfChanged(minion, null, m_state.get(minionId));
                m_state.remove(minionId);
            }
        });
    }

    @EventHandler(ueis= {
            OUTAGE_CREATED_EVENT_UEI,
            OUTAGE_RESOLVED_EVENT_UEI
    })
    public void onOutageEvent(final Event e) {
        final boolean isHeartbeat = MINION_HEARTBEAT.equals(e.getService());
        final boolean isRpc = MINION_RPC.equals(e.getService());

        if (!isHeartbeat && !isRpc) {
            return;
        }

        runInLoggingTransaction(() -> {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Minion {} service event received for node {}: {}", isHeartbeat? "heartbeat":"rpc", e.getNodeid(), e);
            }

            assertHasNodeId(e);

            final OnmsMinion minion = getMinionForNodeId(e.getNodeid().intValue());
            final String minionId = minion.getId();

            AggregateMinionStatus status = m_state.get(minionId);
            if (status == null) {
                status = AggregateMinionStatus.down();
            }

            final String uei = e.getUei();
            if (MINION_HEARTBEAT.equalsIgnoreCase(e.getService())) {
                if (OUTAGE_CREATED_EVENT_UEI.equals(uei)) {
                    status = status.heartbeatDown();
                } else if (OUTAGE_RESOLVED_EVENT_UEI.equals(uei)) {
                    status = status.heartbeatUp();
                }
            } else if (MINION_RPC.equalsIgnoreCase(e.getService())) {
                if (OUTAGE_CREATED_EVENT_UEI.equals(uei)) {
                    status = status.rpcDown();
                } else if (OUTAGE_RESOLVED_EVENT_UEI.equals(uei)) {
                    status = status.rpcUp();
                }
            }

            updateStateIfChanged(minion, status, m_state.get(minionId));
        });
    }

    public void refresh() {
        runInLoggingTransaction(() -> {
            LOG.info("Refreshing minion status from the outages database.");

            final Map<String,OnmsMinion> minions = new ConcurrentHashMap<>();
            final Map<Integer,OnmsMinion> minionNodes = new ConcurrentHashMap<>();
            final Map<String,AggregateMinionStatus> state = new ConcurrentHashMap<>();

            final List<OnmsMinion> dbMinions = m_minionDao.findAll();

            if (dbMinions.size() == 0) {
                LOG.info("No minions found in the database.  Skipping processing.  Next refresh in {} milliseconds.", m_refresh);
                return;
            }

            // populate the foreignId -> minion map
            LOG.debug("Populating minion state from the database.  Found {} minions.", dbMinions.size());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing minions: {}", dbMinions.stream().map(OnmsMinion::getId).collect(Collectors.toList()));
            }

            final AggregateMinionStatus upStatus = AggregateMinionStatus.up();
            dbMinions.forEach(minion -> {
                final String minionId = minion.getId();
                minions.put(minionId, minion);
            });

            // populate the nodeId -> minion map
            final Criteria c = new CriteriaBuilder(OnmsNode.class)
                    .in("foreignId", minions.keySet())
                    .distinct()
                    .toCriteria();
            final List<OnmsNode> nodes = m_nodeDao.findMatching(c);
            for (final OnmsNode node : nodes) {
                m_nodeDao.initialize(node.getLocation());
            }
            LOG.debug("Mapping {} node IDs to minions.", nodes.size());
            if (LOG.isTraceEnabled()) {
                LOG.trace("Processing nodes: {}", nodes.stream().map(OnmsNode::getId).collect(Collectors.toList()));
            }
            nodes.forEach(node -> {
                final OnmsMinion m = minions.get(node.getForeignId());
                if (m.getLocation().equals(node.getLocation().getLocationName())) {
                    minionNodes.put(node.getId(), m);
                    // if the minion has an associated node, give it a default "up" status
                    // this will get marked "down" if there are associated outages
                    state.put(node.getForeignId(), upStatus);
                }
            });

            final Collection<CurrentOutageDetails> outages = m_outageDao.newestCurrentOutages(Arrays.asList(MINION_HEARTBEAT, MINION_RPC));

            if (outages != null && outages.size() > 0) {
                LOG.debug("Processing {} outage records.", outages.size());
                outages.stream().sorted(Comparator.comparing(CurrentOutageDetails::getOutageId).reversed()).forEach(outage -> {
                    final String foreignId = outage.getForeignId();

                    final AggregateMinionStatus currentStatus = state.get(foreignId);
                    final AggregateMinionStatus newStatus = transformStatus(currentStatus, outage.getServiceName(), null, outage.getIfLostService());

                    // If this is a refresh, and the "in-memory" tracking is more up-to-date than the outage records, keep it. Otherwise update with outage records.
                    final AggregateMinionStatus existingStatus = m_state.get(foreignId);
                    if (newStatus.equals(existingStatus)) {
                        LOG.trace("{} status {} is unchanged.", foreignId, newStatus);
                    } else {
                        // if there is no existing status, or the new status is changed, update the state
                        LOG.trace("{} status {} is different than {}, using it instead.", foreignId, newStatus, existingStatus);
                        state.put(foreignId, newStatus);
                    }
                });
            } else {
                LOG.debug("No minion-related outages were found.");
            }

            LOG.debug("Persisting states to the database.");
            minions.values().forEach(minion -> {
                final AggregateMinionStatus oldState = m_state.get(minion.getId());
                final AggregateMinionStatus newState = state.get(minion.getId());
                updateStateIfChanged(minion, newState, oldState);
            });

            m_state = state;
            m_minions = minions;
            m_minionNodes = minionNodes;

            LOG.info("Minion status updated from the outages database.  Next refresh in {} milliseconds.", m_refresh);
        });
    }

    private AggregateMinionStatus transformStatus(final AggregateMinionStatus currentStatus, final String serviceName, final Date ifRegainedService, final Date ifLostService) {
        final AggregateMinionStatus newStatus;
        if (ifRegainedService != null) {
            if (MINION_HEARTBEAT.equals(serviceName)) {
                newStatus = currentStatus.heartbeatUp();
            } else if (MINION_RPC.equals(serviceName)) {
                newStatus = currentStatus.rpcUp();
            } else {
                LOG.warn("Unhandled 'up' outage record: service={}, lost={}, regained={}", serviceName, ifLostService, ifRegainedService);
                newStatus = currentStatus;
            }
        } else {
            if (MINION_HEARTBEAT.equals(serviceName)) {
                newStatus = currentStatus.heartbeatDown();
            } else if (MINION_RPC.equals(serviceName)) {
                newStatus = currentStatus.rpcDown();
            } else {
                LOG.warn("Unhandled 'down' outage record: service={}, lost={}, regained={}", serviceName, ifLostService, ifRegainedService);
                newStatus = currentStatus;
            }
        }
        return newStatus;
    }

    private void updateStateIfChanged(final OnmsMinion minion, final AggregateMinionStatus current, final AggregateMinionStatus previous) {
        final String minionId = minion.getId();
        final String currentMinionStatus = minion.getStatus();

        if (current == null) {
            LOG.debug("Minion {} does not have a state. This is likely because it does not have a monitored node in the 'Minions' requisition.", minionId);
            if (!"unknown".equals(currentMinionStatus)) {
                minion.setStatus("unknown");
                LOG.info("Minion {} status changed: {} -> {}", minionId, currentMinionStatus, minion.getStatus());
                m_minionDao.saveOrUpdate(minion);
            }
            m_state.remove(minionId);
            return;
        }

        m_state.put(minionId, current);
        final String newMinionStatus = current.isUp()? "up":"down";

        if (newMinionStatus.equals(currentMinionStatus)) {
            LOG.trace("Minion {} status did not change: {} = {}", minionId, currentMinionStatus, newMinionStatus);
            return;
        }

        minion.setStatus(newMinionStatus);
        m_minionDao.saveOrUpdate(minion);
        LOG.info("Minion {} status changed: {} -> {}", minionId, (previous == null? "Unknown":previous.getState()), current.getState());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Minion {} status processed: Heartbeat: {} -> {}, RPC: {} -> {}", minionId, (previous == null? "Unknown" : previous.getHeartbeatStatus()), current.getHeartbeatStatus(), (previous == null? "Unknown" : previous.getRpcStatus()), current.getRpcStatus());
        }
    }

    private OnmsMinion getMinionForNodeId(final Integer nodeId) {
        if (m_minionNodes.containsKey(nodeId)) {
            return m_minionNodes.get(nodeId);
        }
        final OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null) {
            final IllegalStateException ex = new IllegalStateException("Unable to retrieve minion. The node (ID: " + nodeId + ") does not exist!");
            LOG.warn(ex.getMessage());
            throw ex;
        }
        m_nodeDao.initialize(node.getLocation());
        final String minionId = node.getForeignId();
        final OnmsMinion minion = m_minionDao.findById(minionId);
        m_minionNodes.put(nodeId, minion);
        m_minions.put(minionId, minion);
        return minion;
    }

    private void assertHasNodeId(final Event e) {
        if (e.getNodeid() == null || e.getNodeid() == 0) {
            final IllegalStateException ex = new IllegalStateException("Received a nodeGainedService event, but there is no node ID!");
            LOG.warn(ex.getMessage() + " {}", e, ex);
            throw ex;
        }
    }

    private void runInLoggingTransaction(final Runnable runnable) {
        m_transactionOperations.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {
                try (MDCCloseable mdc = Logging.withPrefixCloseable(LOG_PREFIX)) {
                    runnable.run();
                    m_minionDao.flush();
                }
            }
        });
    }
}
