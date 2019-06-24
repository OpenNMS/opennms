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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.MockLogger;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.model.outage.CurrentOutageDetails;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
public class MinionStatusTrackerTest {
    private static final String FOREIGN_SOURCE = "Test";
    private static final String MINION_HEARTBEAT = MinionStatusTracker.MINION_HEARTBEAT;
    private static final String MINION_RPC = MinionStatusTracker.MINION_RPC;

    private MinionStatusTracker m_tracker;
    private NodeDao m_nodeDao;
    private MinionDao m_minionDao;
    private ServiceTypeDao m_serviceTypeDao;
    private OutageDao m_outageDao;

    private Integer m_globalId = 0;

    @Before
    public void setUp() throws Exception {
        final Properties props = new Properties();
        props.setProperty(MockLogger.LOG_KEY_PREFIX + "org.opennms.minion.status", "TRACE");
        MockLogAppender.setupLogging(props);

        m_tracker = new MinionStatusTracker();

        m_nodeDao = mock(NodeDao.class /*, withSettings().verboseLogging() */);
        m_minionDao = mock(MinionDao.class /*, withSettings().verboseLogging() */);
        m_serviceTypeDao = mock(ServiceTypeDao.class /*, withSettings().verboseLogging() */);
        m_outageDao = mock(OutageDao.class /*, withSettings().verboseLogging() */);

        m_tracker.m_nodeDao = m_nodeDao;
        m_tracker.m_minionDao = m_minionDao;
        m_tracker.m_serviceTypeDao = m_serviceTypeDao;
        m_tracker.m_outageDao = m_outageDao;

        m_tracker.m_transactionOperations = new TransactionOperations() {
            @Override
            public <T> T execute(final TransactionCallback<T> action) throws TransactionException {
                return action.doInTransaction(null);
            }
        };

        // we don't call afterPropertiesSet() here because
        // we don't want to start the executor
    }

    @Test(expected=IllegalStateException.class)
    public void testEventMissingNodeId() throws Exception {
        final Event e = EventUtils.createNodeGainedServiceEvent(FOREIGN_SOURCE, 1, InetAddressUtils.addr("192.168.0.1"), MINION_HEARTBEAT, null, NodeLabelSource.UNKNOWN, null, null);
        e.setNodeid(null);
        m_tracker.onNodeGainedService(e);
    }

    @Test(expected=IllegalStateException.class)
    public void testEventMissingNode() throws Exception {
        final Event e = EventUtils.createNodeGainedServiceEvent(FOREIGN_SOURCE, 1, InetAddressUtils.addr("192.168.0.1"), MINION_HEARTBEAT, null, NodeLabelSource.UNKNOWN, null, null);
        when(m_nodeDao.get(anyInt())).thenReturn(null);
        m_tracker.onNodeGainedService(e);
    }

    @Test
    public void testNodeGainedNonMinionService() throws Exception {
        final String foreignId = UUID.randomUUID().toString();

        final Event e = EventUtils.createNodeGainedServiceEvent(FOREIGN_SOURCE, 1, InetAddressUtils.addr("192.168.0.1"), "Imaginary", "one", NodeLabelSource.HOSTNAME, null, null);
        m_tracker.onNodeGainedService(e);

        verifyNoMoreInteractions(m_nodeDao);
        verifyNoMoreInteractions(m_minionDao);

        assertEquals("there should not be a minion", 0, m_tracker.getMinions().size());
        final MinionStatus status = m_tracker.getStatus(foreignId);
        assertNull("there should not be a status for the given node foreign ID", status);
    }

    @Test
    public void testNodeGainedMinionHeartbeat() throws Exception {
        final String foreignId = UUID.randomUUID().toString();
        final OnmsNode node = getNode(1, FOREIGN_SOURCE, foreignId, "MinionLocA");
        final OnmsMinion minion = getMinion(node);

        when(m_nodeDao.get(Integer.valueOf(1))).thenReturn(node);
        when(m_minionDao.findById(foreignId)).thenReturn(minion);

        final Event e = EventUtils.createNodeGainedServiceEvent(FOREIGN_SOURCE, 1, InetAddressUtils.addr("192.168.0.1"), MINION_HEARTBEAT, "one", NodeLabelSource.HOSTNAME, null, null);
        m_tracker.onNodeGainedService(e);

        assertEquals("there should be one minion", 1, m_tracker.getMinions().size());
        assertEquals("it should match our minion", foreignId, m_tracker.getMinions().iterator().next().getId());
        final MinionStatus status = m_tracker.getStatus(foreignId);
        assertNotNull("we should get a default status for the minion", status);
        assertTrue("the default status for a minion with no status in the database should be up", status.isUp());
        assertEquals("the status in the minion object should be up", "up", minion.getStatus());
        verify(m_minionDao, times(1)).findById(foreignId);
    }

    public void testNodeGainedDownMinionHeartbeat() throws Exception {
        final String foreignId = UUID.randomUUID().toString();
        final OnmsNode node = getNode(1, FOREIGN_SOURCE, foreignId, "MinionLocA");
        final OnmsMinion minion = getMinion(node);
        minion.setStatus("down");

        when(m_nodeDao.get(Integer.valueOf(1))).thenReturn(node);
        when(m_minionDao.findById(foreignId)).thenReturn(minion);

        final Event e = EventUtils.createNodeGainedServiceEvent(FOREIGN_SOURCE, 1, InetAddressUtils.addr("192.168.0.1"), MINION_HEARTBEAT, "one", NodeLabelSource.HOSTNAME, null, null);
        m_tracker.onNodeGainedService(e);

        assertEquals("there should be one minion", 1, m_tracker.getMinions().size());
        assertEquals("it should match our minion", foreignId, m_tracker.getMinions().iterator().next().getId());
        final MinionStatus status = m_tracker.getStatus(foreignId);
        assertNotNull("we should get a default status for the minion", status);
        assertFalse("the status for a newly indexed minion with 'down' in the database is down", status.isUp());
        verify(m_minionDao, times(1)).saveOrUpdate(minion);
    }

    @Test
    public void testNodeGainedMinionRPC() throws Exception {
        final String foreignId = UUID.randomUUID().toString();
        final OnmsNode node = getNode(1, FOREIGN_SOURCE, foreignId, "MinionLocA");
        final OnmsMinion minion = getMinion(node);

        when(m_nodeDao.get(Integer.valueOf(1))).thenReturn(node);
        when(m_minionDao.findById(foreignId)).thenReturn(minion);

        final Event e = EventUtils.createNodeGainedServiceEvent(FOREIGN_SOURCE, 1, InetAddressUtils.addr("192.168.0.1"), MINION_RPC, "one", NodeLabelSource.HOSTNAME, null, null);
        m_tracker.onNodeGainedService(e);

        assertEquals("there should be one minion", 1, m_tracker.getMinions().size());
        assertEquals("it should match our minion", foreignId, m_tracker.getMinions().iterator().next().getId());
        final MinionStatus status = m_tracker.getStatus(foreignId);
        assertNotNull("we should get a default status for the minion", status);
        verify(m_minionDao, times(1)).findById(foreignId);
    }

    @Test
    public void testNodeGainedBoth() throws Exception {
        final String foreignId = UUID.randomUUID().toString();
        final OnmsNode node = getNode(1, FOREIGN_SOURCE, foreignId, "MinionLocA");
        final OnmsMinion minion = getMinion(node);

        when(m_nodeDao.get(Integer.valueOf(1))).thenReturn(node);
        when(m_minionDao.findById(foreignId)).thenReturn(minion);

        Event e = EventUtils.createNodeGainedServiceEvent(FOREIGN_SOURCE, 1, InetAddressUtils.addr("192.168.0.1"), MINION_HEARTBEAT, "one", NodeLabelSource.HOSTNAME, null, null);
        m_tracker.onNodeGainedService(e);
        e = EventUtils.createNodeGainedServiceEvent(FOREIGN_SOURCE, 1, InetAddressUtils.addr("192.168.0.1"), MINION_RPC, "one", NodeLabelSource.HOSTNAME, null, null);
        m_tracker.onNodeGainedService(e);

        assertEquals("there should be one minion", 1, m_tracker.getMinions().size());
        assertEquals("it should match our minion", foreignId, m_tracker.getMinions().iterator().next().getId());
        final MinionStatus status = m_tracker.getStatus(foreignId);
        assertNotNull("we should get a status for the minion", status);
        assertTrue("the status should be up", status.isUp());
        verify(m_minionDao, times(1)).findById(foreignId);
    }

    @Test
    public void testNodeLostRPC() throws Exception {
        final String foreignId = UUID.randomUUID().toString();
        final OnmsNode node = getNode(1, FOREIGN_SOURCE, foreignId, "MinionLocA");
        final OnmsMinion minion = getMinion(node);

        m_tracker.m_state.put(foreignId, AggregateMinionStatus.create(MinionServiceStatus.up(), MinionServiceStatus.up()));

        when(m_nodeDao.get(Integer.valueOf(1))).thenReturn(node);
        when(m_minionDao.findById(foreignId)).thenReturn(minion);

        generateOutage(EventConstants.OUTAGE_CREATED_EVENT_UEI, node, MINION_RPC, new Date());

        assertEquals("there should be one minion", 1, m_tracker.getMinions().size());
        assertEquals("it should match our minion", foreignId, m_tracker.getMinions().iterator().next().getId());
        final MinionStatus status = m_tracker.getStatus(foreignId);
        assertNotNull("we should get a status for the minion", status);
        assertFalse("the status should be down", status.isUp());
        verify(m_minionDao, times(1)).saveOrUpdate(minion);
    }

    @Test
    public void testNodeDeleted() throws Exception {
        final String foreignId = UUID.randomUUID().toString();
        final OnmsNode node = getNode(1, FOREIGN_SOURCE, foreignId, "MinionLocA");
        final OnmsMinion minion = getMinion(node);

        m_tracker.m_state.put(foreignId, AggregateMinionStatus.create(MinionServiceStatus.up(), MinionServiceStatus.up()));

        when(m_nodeDao.get(Integer.valueOf(1))).thenReturn(node);
        when(m_minionDao.findById(foreignId)).thenReturn(minion);

        Event e = EventUtils.createNodeDeletedEvent(FOREIGN_SOURCE, 1, "one", "one");
        m_tracker.onNodeDeleted(e);

        assertEquals("there should still be a minion", 1, m_tracker.getMinions().size());
        final MinionStatus status = m_tracker.getStatus(minion);
        assertNull("we should not get a status for the minion", status);
        verify(m_minionDao, times(1)).saveOrUpdate(minion);
    }

    @Test
    public void testUpMinion() throws Exception {
        final String foreignId = UUID.randomUUID().toString();
        final OnmsNode node = getNode(1, FOREIGN_SOURCE, foreignId, "MinionLocA");
        final OnmsMinion minion = getMinion(node);

        m_tracker.m_minions.put(foreignId, minion);
        m_tracker.m_minionNodes.put(1, minion);
        m_tracker.m_state.put(foreignId, AggregateMinionStatus.create(MinionServiceStatus.up(), MinionServiceStatus.up()));

        assertEquals("there should be one minion", 1, m_tracker.getMinions().size());
        final MinionStatus status = m_tracker.getStatus(minion);
        assertTrue("we should get an up status for the minion", status.isUp());
        verify(m_minionDao, times(0)).saveOrUpdate(minion);
    }


    @Test
    public void testOtherServiceFails() throws Exception {
        final String foreignId = UUID.randomUUID().toString();
        final OnmsNode node = getNode(1, FOREIGN_SOURCE, foreignId, "MinionLocA");
        final OnmsMinion minion = getMinion(node);

        m_tracker.m_minions.put(foreignId, minion);
        m_tracker.m_minionNodes.put(1, minion);
        m_tracker.m_state.put(foreignId, AggregateMinionStatus.create(MinionServiceStatus.up(), MinionServiceStatus.up()));

        assertEquals("there should be one minion", 1, m_tracker.getMinions().size());
        assertTrue("we should get an up status for the minion", m_tracker.getStatus(minion).isUp());

        generateOutage(EventConstants.OUTAGE_CREATED_EVENT_UEI, node, "WontYouTakeMeToFunkyTown", new Date());

        assertTrue("we should get an up status for the minion", m_tracker.getStatus(minion).isUp());
    }

    @Test
    public void testMinionHeartbeatFails() throws Exception {
        final String foreignId = UUID.randomUUID().toString();
        final OnmsNode node = getNode(1, FOREIGN_SOURCE, foreignId, "MinionLocA");
        final OnmsMinion minion = getMinion(node);

        m_tracker.m_minions.put(foreignId, minion);
        m_tracker.m_minionNodes.put(1, minion);
        m_tracker.m_state.put(foreignId, AggregateMinionStatus.create(MinionServiceStatus.up(), MinionServiceStatus.up()));

        assertEquals("there should be one minion", 1, m_tracker.getMinions().size());
        assertTrue("we should get an up status for the minion", m_tracker.getStatus(minion).isUp());

        generateOutage(EventConstants.OUTAGE_CREATED_EVENT_UEI, node, MINION_HEARTBEAT, new Date());

        assertFalse("we should get a down status for the minion", m_tracker.getStatus(minion).isUp());
        verify(m_minionDao, times(1)).saveOrUpdate(minion);
    }

    @Test
    public void testMinionRPCFails() throws Exception {
        final String foreignId = UUID.randomUUID().toString();
        final OnmsNode node = getNode(1, FOREIGN_SOURCE, foreignId, "MinionLocA");
        final OnmsMinion minion = getMinion(node);

        m_tracker.m_minions.put(foreignId, minion);
        m_tracker.m_minionNodes.put(1, minion);
        m_tracker.m_state.put(foreignId, AggregateMinionStatus.create(MinionServiceStatus.up(), MinionServiceStatus.up()));

        assertEquals("there should be one minion", 1, m_tracker.getMinions().size());
        assertTrue("we should get an up status for the minion", m_tracker.getStatus(minion).isUp());

        generateOutage(EventConstants.OUTAGE_CREATED_EVENT_UEI, node, MINION_RPC, new Date());

        assertFalse("we should get a down status for the minion", m_tracker.getStatus(minion).isUp());
        verify(m_minionDao, times(1)).saveOrUpdate(minion);
    }

    @Test
    public void testMinionResponsive() throws Exception {
        final String foreignId = UUID.randomUUID().toString();
        final OnmsNode node = getNode(1, FOREIGN_SOURCE, foreignId, "MinionLocA");
        final OnmsMinion minion = getMinion(node);

        m_tracker.m_minions.put(foreignId, minion);
        m_tracker.m_minionNodes.put(1, minion);
        m_tracker.m_state.put(foreignId, AggregateMinionStatus.create(MinionServiceStatus.down(), MinionServiceStatus.down()));

        assertEquals("there should be one minion", 1, m_tracker.getMinions().size());
        assertFalse("we should get a down status for the minion", m_tracker.getStatus(minion).isUp());

        generateOutage(EventConstants.OUTAGE_RESOLVED_EVENT_UEI, node, MINION_HEARTBEAT, new Date());
        assertFalse("we should still be down", m_tracker.getStatus(minion).isUp());

        generateOutage(EventConstants.OUTAGE_RESOLVED_EVENT_UEI, node, MINION_RPC, new Date());
        assertTrue("we should get an up status for the minion", m_tracker.getStatus(minion).isUp());

        verify(m_minionDao, times(2)).saveOrUpdate(minion);
    }

    @Test
    public void testRefresh() throws Exception {
        assertEquals("there should be no minions in the node:minion cache", 0, m_tracker.m_minionNodes.size());
        assertEquals("there should be no minions in the minion cache", 0, m_tracker.m_minionNodes.size());
        assertEquals("there should be no minions in the state cache", 0, m_tracker.m_minionNodes.size());

        final OnmsServiceType heartbeatServiceType = new OnmsServiceType(1, MINION_HEARTBEAT);
        final OnmsServiceType rpcServiceType = new OnmsServiceType(2, MINION_RPC);
        when(m_serviceTypeDao.findByName(MINION_HEARTBEAT)).thenReturn(heartbeatServiceType);
        when(m_serviceTypeDao.findByName(MINION_RPC)).thenReturn(rpcServiceType);

        final String foreignIdA = "00000000-0000-0000-0000-00000000042A";
        final OnmsNode nodeA = getNode(1, FOREIGN_SOURCE, foreignIdA, "MinionLocA");
        final OnmsMinion minionA = getMinion(nodeA);

        final String foreignIdB = "00000000-0000-0000-0000-00000000042B";
        final OnmsNode nodeB = getNode(2, FOREIGN_SOURCE, foreignIdB, "MinionLocB");
        final OnmsMinion minionB = getMinion(nodeB);

        final String foreignIdC = "00000000-0000-0000-0000-00000000042C";
        final OnmsNode nodeC = getNode(3, FOREIGN_SOURCE, foreignIdC, "MinionLocC");
        final OnmsMinion minionC = getMinion(nodeC);

        final String foreignIdD = "00000000-0000-0000-0000-00000000042D";
        final OnmsNode nodeD = getNode(4, FOREIGN_SOURCE, foreignIdD, "MinionLocD");
        final OnmsMinion minionD = getMinion(nodeD);

        final Date now = new Date(System.currentTimeMillis());
        final Date old = new Date(1);
        final List<CurrentOutageDetails> outages = Arrays.asList(
                                                       createOutage(now, null, nodeA, MINION_RPC), // nodeA RPC down
                                                       createOutage(now, null, nodeA, MINION_HEARTBEAT) // nodeA heartbeat down
                );

        when(m_minionDao.findAll()).thenReturn(Arrays.asList(minionA, minionB, minionC, minionD));
        when(m_nodeDao.findMatching(any(Criteria.class))).thenReturn(Arrays.asList(nodeA, nodeB, nodeC, nodeD));
        when(m_outageDao.newestCurrentOutages(anyListOf(String.class))).thenReturn(outages);

        System.err.println("old=" + old);
        System.err.println("now=" + now);

        m_tracker.refresh();

        assertEquals("there should be 4 minions", 4, m_tracker.getMinions().size());
        assertFalse("we should get a down status for minion A (both services down)", m_tracker.getStatus(foreignIdA).isUp());
        assertTrue("we should get an up status for minion B", m_tracker.getStatus(foreignIdB).isUp());
        assertTrue("we should get an up status for minion C", m_tracker.getStatus(foreignIdC).isUp());
        assertTrue("we should get an up status for minion D", m_tracker.getStatus(foreignIdD).isUp());

        final AggregateMinionStatus statusA = (AggregateMinionStatus)m_tracker.getStatus(foreignIdA);
        assertFalse("node A heartbeat status should be down", statusA.getHeartbeatStatus().isUp());
        assertFalse("node A RPC status should be down", statusA.getRpcStatus().isUp());

        AggregateMinionStatus statusB = (AggregateMinionStatus)m_tracker.getStatus(foreignIdB);
        assertTrue("node B heartbeat status should be up", statusB.getHeartbeatStatus().isUp());
        assertTrue("node B RPC status should be up", statusB.getRpcStatus().isUp());

        AggregateMinionStatus statusC = (AggregateMinionStatus)m_tracker.getStatus(foreignIdC);
        assertTrue("node C heartbeat status should be up", statusC.getHeartbeatStatus().isUp());
        assertTrue("node C RPC status should be up", statusC.getRpcStatus().isUp());

        AggregateMinionStatus statusD = (AggregateMinionStatus)m_tracker.getStatus(foreignIdD);
        assertTrue("node D heartbeat status should be up", statusD.getHeartbeatStatus().isUp());
        assertTrue("node D RPC status should be up", statusD.getRpcStatus().isUp());

        generateOutage(EventConstants.OUTAGE_RESOLVED_EVENT_UEI, nodeA, MINION_HEARTBEAT, now);
        assertFalse("node A should still be down", m_tracker.getStatus(foreignIdA).isUp());
        generateOutage(EventConstants.OUTAGE_RESOLVED_EVENT_UEI, nodeA, MINION_RPC, now);
        assertTrue("node A should now be up", m_tracker.getStatus(foreignIdA).isUp());

        verify(m_minionDao, times(2)).saveOrUpdate(minionA);
    }

    @Test
    public void testRefreshNoOutages() throws Exception {
        final OnmsServiceType heartbeatServiceType = new OnmsServiceType(1, MINION_HEARTBEAT);
        final OnmsServiceType rpcServiceType = new OnmsServiceType(2, MINION_RPC);
        when(m_serviceTypeDao.findByName(MINION_HEARTBEAT)).thenReturn(heartbeatServiceType);
        when(m_serviceTypeDao.findByName(MINION_RPC)).thenReturn(rpcServiceType);

        final String foreignIdA = UUID.randomUUID().toString();
        final OnmsNode nodeA = getNode(1, FOREIGN_SOURCE, foreignIdA, "MinionLocA");
        final OnmsMinion minionA = getMinion(nodeA);

        // initial query based on the outage resolved event(s)
        when(m_nodeDao.get(Integer.valueOf(1))).thenReturn(nodeA);
        when(m_minionDao.findById(foreignIdA)).thenReturn(minionA);

        assertEquals("there should be no minions", 0, m_tracker.getMinions().size());

        final Date heartbeatUpDate = new Date(System.currentTimeMillis() - 100);
        generateOutage(EventConstants.OUTAGE_RESOLVED_EVENT_UEI, nodeA, MINION_HEARTBEAT, heartbeatUpDate);

        assertEquals("there should be 1 minion", 1, m_tracker.getMinions().size());
        assertFalse("it should be down", m_tracker.getStatus(foreignIdA).isUp());

        final Date rpcUpDate = new Date(System.currentTimeMillis() - 50);
        generateOutage(EventConstants.OUTAGE_RESOLVED_EVENT_UEI, nodeA, MINION_RPC, rpcUpDate);

        assertEquals("there should still be 1 minion", 1, m_tracker.getMinions().size());
        assertTrue("it should be up", m_tracker.getStatus(foreignIdA).isUp());

        // refresh() query
        when(m_minionDao.findAll()).thenReturn(Arrays.asList(minionA));
        when(m_nodeDao.findMatching(any(Criteria.class))).thenReturn(Arrays.asList(nodeA));
        when(m_outageDao.newestCurrentOutages(anyListOf(String.class))).thenReturn(Collections.emptyList());

        m_tracker.refresh();

        assertEquals("there should still be 1 minion", 1, m_tracker.getMinions().size());
        assertTrue("it should still be up", m_tracker.getStatus(foreignIdA).isUp());

        final AggregateMinionStatus statusA = (AggregateMinionStatus)m_tracker.getStatus(foreignIdA);
        assertTrue("node A heartbeat status should be up", statusA.getHeartbeatStatus().isUp());
        assertTrue("node A RPC status should be up", statusA.getRpcStatus().isUp());

        verify(m_minionDao, times(2)).saveOrUpdate(any(OnmsMinion.class));
    }

    @Test
    public void testStartup() throws Exception {
        final OnmsServiceType heartbeatServiceType = new OnmsServiceType(1, MINION_HEARTBEAT);
        final OnmsServiceType rpcServiceType = new OnmsServiceType(2, MINION_RPC);
        when(m_serviceTypeDao.findByName(MINION_HEARTBEAT)).thenReturn(heartbeatServiceType);
        when(m_serviceTypeDao.findByName(MINION_RPC)).thenReturn(rpcServiceType);

        final String foreignIdA = UUID.randomUUID().toString();
        final OnmsNode nodeA = getNode(1, FOREIGN_SOURCE, foreignIdA, "MinionLocA");
        final OnmsMinion minionA = getMinion(nodeA);
        minionA.setLastUpdated(new Date(1));

        when(m_minionDao.findAll()).thenReturn(Arrays.asList(minionA));
        when(m_nodeDao.findMatching(any(Criteria.class))).thenReturn(Arrays.asList(nodeA));
        when(m_outageDao.newestCurrentOutages(anyListOf(String.class))).thenReturn(Collections.emptyList());

        m_tracker.refresh();

        assertEquals("there should be 1 minion restored from the database", 1, m_tracker.getMinions().size());
        assertTrue("it should be up because refresh always checks for outages", m_tracker.getStatus(foreignIdA).isUp());

        verify(m_minionDao, times(1)).findAll();
    }

    @Test
    public void testMinionWithoutNode() throws Exception {
        final OnmsServiceType heartbeatServiceType = new OnmsServiceType(1, MINION_HEARTBEAT);
        final OnmsServiceType rpcServiceType = new OnmsServiceType(2, MINION_RPC);
        when(m_serviceTypeDao.findByName(MINION_HEARTBEAT)).thenReturn(heartbeatServiceType);
        when(m_serviceTypeDao.findByName(MINION_RPC)).thenReturn(rpcServiceType);

        final OnmsMinion minion = new OnmsMinion(UUID.randomUUID().toString(), "MinionLocation", "up", new Date());
        when(m_minionDao.findAll()).thenReturn(Arrays.asList(minion));

        m_tracker.refresh();

        assertEquals("there should be 1 minion restored from the database", 1, m_tracker.getMinions().size());
        assertNull("it should not have a status", m_tracker.getStatus(minion.getId()));
    }

    private Map<Integer,OnmsNode> m_nodes = new HashMap<>();
    private Map<String,OnmsMonitoringLocation> m_locations = new HashMap<>();
    private Map<Integer,OnmsMinion> m_minions = new HashMap<>();
    private Map<String,OnmsServiceType> m_serviceTypes = new HashMap<>();

    private Integer lastLastOctet = 0;

    private CurrentOutageDetails generateOutage(final String uei, final OnmsNode node, final String service, final Date time) {
        final CurrentOutageDetails outage = createOutage(time, null, node, service);
        final Event e = new EventBuilder(uei, "MinionStatusTrackerTest")
                .setNodeid(node.getId())
                .setService(service)
                .setTime(time)
                .getEvent();
        m_tracker.onOutageEvent(e);
        return outage;
    }

    private CurrentOutageDetails createOutage(final Date lostService, final Date regainedService, final OnmsNode node, final String serviceType) {
        final OnmsServiceType svcType = getServiceType(serviceType);
        return new CurrentOutageDetails(++m_globalId, svcType.getId(), serviceType, lostService, node.getId(), node.getForeignSource(), node.getForeignId(), node.getLocation().getLocationName());
    }

    private OnmsMinion getMinion(final OnmsNode node) {
        final Integer nodeId = node.getId();
        final String foreignId = node.getForeignId();
        final String location = node.getLocation().getLocationName();
        final OnmsMinion minion = m_minions.getOrDefault(nodeId, new OnmsMinion(foreignId, location, "up", new Date()));
        minion.setLabel(location);
        m_minions.put(nodeId, minion);
        return minion;
    }

    private OnmsNode getNode(final Integer nodeId, final String foreignSource, final String foreignId, final String location) {
        final OnmsNode node = m_nodes.getOrDefault(nodeId, new OnmsNode());
        m_nodes.put(nodeId, node);

        final OnmsMonitoringLocation loc = m_locations.getOrDefault(location, new OnmsMonitoringLocation(location, ""));
        m_locations.put(foreignSource, loc);

        node.setId(nodeId);
        node.setLabel(foreignId);
        node.setLabelSource(NodeLabelSource.USER);
        node.setLocation(loc);
        node.setForeignSource(foreignSource);
        node.setForeignId(foreignId);

        final OnmsIpInterface iface = new OnmsIpInterface(InetAddressUtils.addr("192.168.0." + ++lastLastOctet), node);
        iface.setIsSnmpPrimary(PrimaryType.PRIMARY);
        iface.addMonitoredService(getManagedService(iface, MINION_HEARTBEAT));
        iface.addMonitoredService(getManagedService(iface, MINION_RPC));

        return node;
    }

    private OnmsMonitoredService getManagedService(final OnmsIpInterface iface, final String serviceType) {
        return new OnmsMonitoredService(iface, getServiceType(serviceType));
    }

    private OnmsServiceType getServiceType(final String serviceName) {
        if (!m_serviceTypes.containsKey(serviceName)) {
            m_serviceTypes.put(serviceName, new OnmsServiceType(++m_globalId, serviceName));
        }
        return m_serviceTypes.get(serviceName);
    }
}
