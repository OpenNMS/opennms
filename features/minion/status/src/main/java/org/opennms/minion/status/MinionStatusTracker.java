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
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@EventListener(name="minionStatusTracker", logPrefix=MinionStatusTracker.LOG_PREFIX)
public class MinionStatusTracker implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(MinionStatusTracker.class);

    ScheduledExecutorService m_executor = Executors.newSingleThreadScheduledExecutor();

    public static final String LOG_PREFIX = "minion";

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

    private boolean m_initialized = false;
    private Integer m_heartbeatServiceId = null;
    private Integer m_rpcServiceId = null;

    // by default, minions are updated every 30 seconds
    private long m_period = TimeUnit.SECONDS.toMillis(30);

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
            final Runnable command = new Runnable() {
                @Override public void run() {
                    try {
                        refresh();
                    } catch (final Throwable t) {
                        LOG.warn("Failed to refresh minion status from the database.", t);
                    }
                }
            };
            // sanity check every 10xPERIOD (5 minutes on the default period of 30 seconds)
            m_executor.scheduleAtFixedRate(command, 0, 10 * m_period, TimeUnit.MILLISECONDS);
        }
    }

    public long getPeriod() {
        return m_period;
    }

    public void setPeriod(final long period) {
        m_period = period;
    }

    @EventHandler(uei=EventConstants.MONITORING_SYSTEM_ADDED_UEI)
    @Transactional
    public void onMonitoringSystemAdded(final Event e) {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(LOG_PREFIX)) {
            final String id = e.getParm(EventConstants.PARAM_MONITORING_SYSTEM_ID).toString();
            LOG.debug("Monitoring system added: {}", id);
            if (id != null) {
                m_state.put(id, AggregateMinionStatus.up());
            }
        }
    }

    @EventHandler(uei=EventConstants.MONITORING_SYSTEM_DELETED_UEI)
    @Transactional
    public void onMonitoringSystemDeleted(final Event e) {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(LOG_PREFIX)) {
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
        }
    }

    @EventHandler(uei=EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)
    @Transactional
    public void onNodeGainedService(final Event e) {
        if (!MINION_HEARTBEAT.equals(e.getService()) && !MINION_RPC.equals(e.getService())) {
            return;
        }

        try (MDCCloseable mdc = Logging.withPrefixCloseable(LOG_PREFIX)) {
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
                state = state.heartbeatUp(e.getTime());
            } else if (MINION_RPC.equals(e.getService())) {
                state = state.rpcUp(e.getTime());
            }
            updateStateIfChanged(minion, state, m_state.get(minionId));
        }
    }

    @EventHandler(uei=EventConstants.NODE_LOST_SERVICE_EVENT_UEI)
    @Transactional
    public void onNodeLostService(final Event e) {
        if (!MINION_HEARTBEAT.equals(e.getService()) && !MINION_RPC.equals(e.getService())) {
            return;
        }

        try (MDCCloseable mdc = Logging.withPrefixCloseable(LOG_PREFIX)) {
            assertHasNodeId(e);

            final Integer nodeId = e.getNodeid().intValue();
            final OnmsMinion minion = getMinionForNodeId(nodeId);
            if (minion == null) {
                LOG.debug("No minion found for node ID {}", nodeId);
                return;
            }

            LOG.debug("Node {}({}) lost a Minion service: {}", nodeId, minion.getId(), e.getService());

            final String minionId = minion.getId();
            AggregateMinionStatus state = m_state.get(minionId);
            if (state == null) {
                LOG.debug("Found new Minion node: {}", minionId);
                state = "down".equals(minion.getStatus())? AggregateMinionStatus.down() : AggregateMinionStatus.up();
            }

            if (MINION_HEARTBEAT.equals(e.getService())) {
                state = state.heartbeatDown(e.getTime());
            } else if (MINION_RPC.equals(e.getService())) {
                state = state.rpcDown(e.getTime());
            }
            updateStateIfChanged(minion, state, m_state.get(minionId));
        }
    }

    @EventHandler(uei=EventConstants.NODE_DELETED_EVENT_UEI)
    @Transactional
    public void onNodeDeleted(final Event e) {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(LOG_PREFIX)) {
            assertHasNodeId(e);
            final Integer nodeId = e.getNodeid().intValue();
            OnmsMinion minion = getMinionForNodeId(nodeId);
            m_minionNodes.remove(nodeId);
            if (minion != null) {
                final String minionId = minion.getId();
                LOG.debug("Minion node {}({}) deleted.", nodeId, minionId);
                updateStateIfChanged(minion, AggregateMinionStatus.down(), m_state.get(minionId));
                m_minions.remove(minionId);
                m_state.remove(minionId);
            }
        }
    }

    @EventHandler(ueis= {
            EventConstants.OUTAGE_CREATED_EVENT_UEI,
            EventConstants.OUTAGE_RESOLVED_EVENT_UEI
    })
    @Transactional
    public void onOutageEvent(final Event e) {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(LOG_PREFIX)) {
            final boolean isHeartbeat = MINION_HEARTBEAT.equals(e.getService());
            final boolean isRpc = MINION_RPC.equals(e.getService());

            if (!isHeartbeat && !isRpc) {
                return;
            }

            assertHasNodeId(e);

            LOG.trace("Minion {} outage event received for node {}: {}", isHeartbeat? "heartbeat":"rpc", e.getNodeid(), e);

            final OnmsMinion minion = getMinionForNodeId(e.getNodeid().intValue());
            final String minionId = minion.getId();

            AggregateMinionStatus status = m_state.get(minionId);
            if (status == null) {
                status = AggregateMinionStatus.down();
            }

            final String uei = e.getUei();
            if (MINION_HEARTBEAT.equalsIgnoreCase(e.getService())) {
                if (EventConstants.OUTAGE_CREATED_EVENT_UEI.equals(uei)) {
                    status = status.heartbeatDown(e.getTime());
                } else if (EventConstants.OUTAGE_RESOLVED_EVENT_UEI.equals(uei)) {
                    status = status.heartbeatUp(e.getTime());
                }
                final MinionServiceStatus heartbeatStatus = status.getHeartbeatStatus();
                LOG.debug("{} heartbeat is {} as of {}", minionId, heartbeatStatus.getState(), heartbeatStatus.lastSeen());
            } else if (MINION_RPC.equalsIgnoreCase(e.getService())) {
                if (EventConstants.OUTAGE_CREATED_EVENT_UEI.equals(uei)) {
                    status = status.rpcDown(e.getTime());
                } else if (EventConstants.OUTAGE_RESOLVED_EVENT_UEI.equals(uei)) {
                    status = status.rpcUp(e.getTime());
                }
                final MinionServiceStatus rpcStatus = status.getRpcStatus();
                LOG.debug("{} RPC is {} as of {}", minionId, rpcStatus.getState(), rpcStatus.lastSeen());
            }

            updateStateIfChanged(minion, status, m_state.get(minionId));
        }
    }

    @Transactional
    public void refresh() {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(LOG_PREFIX)) {
            LOG.info("Refreshing minion status from the outages database.");

            final Map<String,OnmsMinion> minions = new ConcurrentHashMap<>();
            final Map<Integer,OnmsMinion> minionNodes = new ConcurrentHashMap<>();
            final Map<String,AggregateMinionStatus> state = new ConcurrentHashMap<>();

            final List<OnmsMinion> dbMinions = m_minionDao.findAll();

            if (dbMinions.size() == 0) {
                LOG.info("No minions found in the database.  Skipping processing.");
                return;
            }

            // populate the foreignId -> minion map
            LOG.debug("Populating minion state from the database.  Found {} minions.", dbMinions.size());
            if (LOG.isTraceEnabled()) {
                LOG.debug("Processing minions: {}", dbMinions.stream().map(OnmsMinion::getId).collect(Collectors.toList()));
            }

            dbMinions.forEach(minion -> {
                final String minionId = minion.getId();
                minions.put(minionId, minion);
                final AggregateMinionStatus status;
                if ("down".equals(minion.getStatus())) {
                    status = AggregateMinionStatus.down();
                } else {
                    status = AggregateMinionStatus.up();
                }
                state.put(minionId, status);
            });

            // populate the nodeId -> minion map
            final Criteria c = new CriteriaBuilder(OnmsNode.class)
                    .in("foreignId", minions.keySet())
                    .distinct()
                    .toCriteria();
            final List<OnmsNode> nodes = m_nodeDao.findMatching(c);
            LOG.debug("Mapping {} node IDs to minions.", nodes.size());
            if (LOG.isTraceEnabled()) {
                LOG.trace("Processing nodes: {}", nodes.stream().map(OnmsNode::getId).collect(Collectors.toList()));
            }
            nodes.forEach(node -> {
                final OnmsMinion m = minions.get(node.getForeignId());
                if (m.getLocation().equals(node.getLocation().getLocationName())) {
                    minionNodes.put(node.getId(), m);
                }
            });

            final Integer heartbeatServiceId = getHeartbeatServiceId();
            final Integer rpcServiceId = getRpcServiceId();

            final ServiceSelector selector = new ServiceSelector("IPADDR != '0.0.0.0'", Arrays.asList(MINION_HEARTBEAT, MINION_RPC));
            final Collection<OnmsOutage> outages = m_outageDao.matchingLatestOutages(selector);

            if (outages != null && outages.size() > 0) {
                LOG.debug("Processing {} outage records.", outages.size());
                outages.stream().sorted(Comparator.comparing(OnmsOutage::getId).reversed()).forEach(outage -> {
                    final String foreignId = outage.getForeignId();

                    final AggregateMinionStatus currentStatus = state.get(foreignId);
                    final AggregateMinionStatus newStatus = transformStatus(currentStatus, outage.getServiceId(), outage.getIfRegainedService(), outage.getIfLostService());

                    if (!m_initialized) {
                        if (outage.getServiceId().equals(heartbeatServiceId)) {
                            final MinionServiceStatus heartbeatStatus = newStatus.getHeartbeatStatus();
                            LOG.debug("{} heartbeat is {} as of {}", foreignId, heartbeatStatus.getState(), heartbeatStatus.lastSeen());
                        } else if (outage.getServiceId().equals(rpcServiceId)) {
                            final MinionServiceStatus rpcStatus = newStatus.getRpcStatus();
                            LOG.debug("{} RPC is {} as of {}", foreignId, rpcStatus.getState(), rpcStatus.lastSeen());
                        }
                    }

                    // if the "in-memory" tracking is more up-to-date than the outage records, keep it, otherwise update with outage records
                    final AggregateMinionStatus existingStatus = m_state.get(foreignId);
                    if (existingStatus == null || !newStatus.getState().equals(existingStatus.getState()) || newStatus.lastSeen().after(existingStatus.lastSeen())) {
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
            m_initialized = true;

            LOG.info("Minion status updated from the outages database.  Next refresh in {} milliseconds.", m_period);
        }
    }

    private AggregateMinionStatus transformStatus(final AggregateMinionStatus currentStatus, final Integer outageServiceId, final Date ifRegainedService, final Date ifLostService) {
        final AggregateMinionStatus newStatus;
        final Integer heartbeatId = getHeartbeatServiceId();
        final Integer rpcId = getRpcServiceId();
        if (ifRegainedService != null) {
            if (heartbeatId == outageServiceId) {
                newStatus = currentStatus.heartbeatUp(ifRegainedService);
            } else if (rpcId == outageServiceId) {
                newStatus = currentStatus.rpcUp(ifRegainedService);
            } else {
                LOG.warn("Unhandled 'up' outage record: ifservice={}, lost={}, regained={}", outageServiceId, ifLostService, ifRegainedService);
                newStatus = currentStatus;
            }
        } else {
            if (heartbeatId == outageServiceId) {
                newStatus = currentStatus.heartbeatDown(ifLostService);
            } else if (rpcId == outageServiceId) {
                newStatus = currentStatus.rpcDown(ifLostService);
            } else {
                LOG.warn("Unhandled 'down' outage record: ifservice={}, lost={}, regained={}", outageServiceId, ifLostService, ifRegainedService);
                newStatus = currentStatus;
            }
        }
        return newStatus;
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

    private Integer getHeartbeatServiceId() {
        if (m_heartbeatServiceId == null) {
            final OnmsServiceType heartbeatService = m_serviceTypeDao.findByName(MINION_HEARTBEAT);
            if (heartbeatService != null) {
                m_heartbeatServiceId = heartbeatService.getId();
            } else {
                LOG.warn("No " + MINION_HEARTBEAT + " service found.");
            }
        }
        return m_heartbeatServiceId;
    }

    private Integer getRpcServiceId() {
        if (m_rpcServiceId == null) {
            final OnmsServiceType rpcService = m_serviceTypeDao.findByName(MINION_RPC);
            if (rpcService != null) {
                m_rpcServiceId = rpcService.getId();
            } else {
                LOG.warn("No " + MINION_RPC + " service found.");
            }
        }
        return m_rpcServiceId;
    }

    private void updateStateIfChanged(final OnmsMinion minion, final AggregateMinionStatus current, final AggregateMinionStatus previous) {
        final String minionId = minion.getId();
        m_state.put(minionId, current);

        final String currentMinionStatus = minion.getStatus();
        final String newMinionStatus = current.isUp(2 * m_period)? "up":"down";

        if (newMinionStatus.equals(currentMinionStatus)) {
            LOG.trace("Minion {} status did not change: {}", minionId, currentMinionStatus);
            return;
        }

        minion.setStatus(newMinionStatus);
        m_minionDao.saveOrUpdate(minion);
        LOG.debug("Minion {} status has changed: Heartbeat: {} -> {}, RPC: {} -> {}", minionId, (previous == null? "Unknown" : previous.getHeartbeatStatus()), current.getHeartbeatStatus(), (previous == null? "Unknown" : previous.getRpcStatus()), current.getRpcStatus());
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
}
