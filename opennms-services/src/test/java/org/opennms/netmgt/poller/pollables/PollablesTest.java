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

package org.opennms.netmgt.poller.pollables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.Querier;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.PollOutagesConfig;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.eventd.mock.EventAnticipator;
import org.opennms.netmgt.eventd.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockElement;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockPollerConfig;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockVisitor;
import org.opennms.netmgt.mock.MockVisitorAdapter;
import org.opennms.netmgt.mock.OutageAnticipator;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.mock.MockPollContext;
import org.opennms.netmgt.poller.mock.MockScheduler;
import org.opennms.netmgt.poller.mock.MockTimer;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.scheduler.ScheduleTimer;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.test.mock.MockUtil;

/**
 * Represents a PollablesTest 
 *
 * @author brozow
 */
public class PollablesTest {

    private PollableNetwork m_network;
    private MockPollContext m_pollContext;
    private MockNetwork m_mockNetwork;
    private MockDatabase m_db;
    private EventAnticipator m_anticipator;
    private MockEventIpcManager m_eventMgr;
    private MockNode mNode1;
    private MockInterface mDot1;
    private MockInterface mDot2;
    private MockNode mNode2;
    private MockService mDot1Smtp;
    private MockService mDot1Icmp;
    private MockService mDot2Icmp;
    private MockInterface mDot3;
    private MockService mDot3Http;
    private MockNode mNode3;
    private MockInterface mDot4;
    private MockService mDot4Smtp;
    @SuppressWarnings("unused")
    private MockService mDot4Http;
    private PollableNode pNode1;
    private PollableInterface pDot1;
    private PollableService pDot1Smtp;
    private PollableService pDot1Icmp;
    private PollableInterface pDot2;
    private PollableService pDot2Smtp;
    private PollableService pDot2Icmp;
    private PollableNode pNode2;
    private PollableInterface pDot3;
    private PollableService pDot3Http;
    private PollableService pDot3Icmp;
    private PollableNode pNode3;
    private PollableInterface pDot4;
    private PollableService pDot4Smtp;
    private PollableService pDot4Http;
    private OutageAnticipator m_outageAnticipator;
    private MockPollerConfig m_pollerConfig;
    
    private MockScheduler m_scheduler;
    private MockTimer m_timer;
    
    private int m_lockCount = 0;
    

    @Before
    public void setUp() throws Exception {
        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();
        
        MockUtil.println("------------ Begin Test --------------------------");
        
        MockLogAppender.setupLogging();
        
        m_lockCount = 0;
        
        m_mockNetwork = new MockNetwork();
        m_mockNetwork.addNode(1, "Router");
        m_mockNetwork.addInterface("192.168.1.1");
        m_mockNetwork.addService("ICMP");
        m_mockNetwork.addService("SMTP");
        m_mockNetwork.addInterface("192.168.1.2");
        m_mockNetwork.addService("ICMP");
        m_mockNetwork.addService("SMTP");
        m_mockNetwork.addNode(2, "Server");
        m_mockNetwork.addInterface("192.168.1.3");
        m_mockNetwork.addService("ICMP");
        m_mockNetwork.addService("HTTP");
        m_mockNetwork.addNode(3, "Firewall");
        m_mockNetwork.addInterface("192.168.1.4");
        m_mockNetwork.addService("SMTP");
        m_mockNetwork.addService("HTTP");
        m_mockNetwork.addInterface("192.168.1.5");
        m_mockNetwork.addService("SMTP");
        m_mockNetwork.addService("SNMP");

        m_db = new MockDatabase();
        m_db.populate(m_mockNetwork);
        
        
        m_anticipator = new EventAnticipator();
        m_outageAnticipator = new OutageAnticipator(m_db);

        
        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventWriter(m_db);
        m_eventMgr.setEventAnticipator(m_anticipator);
        m_eventMgr.addEventListener(m_outageAnticipator);
        
        m_pollContext = new MockPollContext();
        m_pollContext.setDatabase(m_db);
        m_pollContext.setCriticalServiceName("ICMP");
        m_pollContext.setNodeProcessingEnabled(true);
        m_pollContext.setPollingAllIfCritServiceUndefined(true);
        m_pollContext.setServiceUnresponsiveEnabled(true);
        m_pollContext.setEventMgr(m_eventMgr);
        m_pollContext.setMockNetwork(m_mockNetwork);
        
        m_pollerConfig = new MockPollerConfig(m_mockNetwork);
        m_pollerConfig.setNodeOutageProcessingEnabled(true);
        m_pollerConfig.setCriticalService("ICMP");
        m_pollerConfig.addPackage("TestPackage");
        m_pollerConfig.addDowntime(100L, 0L, 500L, false);
        m_pollerConfig.addDowntime(200L, 500L, 1500L, false);
        m_pollerConfig.addDowntime(500L, 1500L, -1L, true);
        m_pollerConfig.setDefaultPollInterval(1000L);
        m_pollerConfig.populatePackage(m_mockNetwork);
        m_pollerConfig.addPackage("TestPkg2");
        m_pollerConfig.addDowntime(500L, 0L, 1000L, false);
        m_pollerConfig.addDowntime(500L, 1000L, -1L, true);
        m_pollerConfig.setDefaultPollInterval(2000L);
        m_pollerConfig.addService(m_mockNetwork.getService(2, "192.168.1.3", "HTTP"));
        
        m_timer = new MockTimer();
        m_scheduler = new MockScheduler(m_timer);
        m_network = createPollableNetwork(m_db, m_scheduler, m_pollerConfig, m_pollerConfig, m_pollContext);
        
        // set members to make the tests easier
        
        mNode1 = m_mockNetwork.getNode(1);
        mDot1 = mNode1.getInterface("192.168.1.1");
        mDot1Smtp = mDot1.getService("SMTP");
        mDot1Icmp = mDot1.getService("ICMP");
        mDot2 = mNode1.getInterface("192.168.1.2");
        mDot2Icmp = mDot2.getService("ICMP");
        
        mNode2 = m_mockNetwork.getNode(2);
        mDot3 = mNode2.getInterface("192.168.1.3");
        mDot3Http = mDot3.getService("HTTP");
        
        mNode3 = m_mockNetwork.getNode(3);
        mDot4 = mNode3.getInterface("192.168.1.4");
        mDot4Smtp = mDot4.getService("SMTP");
        mDot4Http = mDot4.getService("HTTP");
        
        assignPollableMembers(m_network);
        
    }

    private void assignPollableMembers(PollableNetwork pNetwork) throws UnknownHostException {
        pNode1 = pNetwork.getNode(1);
        pDot1 = pNode1.getInterface(InetAddressUtils.addr("192.168.1.1"));
        pDot1Smtp = pDot1.getService("SMTP");
        pDot1Icmp = pDot1.getService("ICMP");
        pDot2 = pNode1.getInterface(InetAddressUtils.addr("192.168.1.2"));
        pDot2Smtp = pDot2.getService("SMTP");
        pDot2Icmp = pDot2.getService("ICMP");
        
        pNode2 = pNetwork.getNode(2);
        pDot3 = pNode2.getInterface(InetAddressUtils.addr("192.168.1.3"));
        pDot3Http = pDot3.getService("HTTP");
        pDot3Icmp = pDot3.getService("ICMP");
        
        pNode3 = pNetwork.getNode(3);
        pDot4 = pNode3.getInterface(InetAddressUtils.addr("192.168.1.4"));
        pDot4Smtp = pDot4.getService("SMTP");
        pDot4Http = pDot4.getService("HTTP");
    }
    
    static class InitCause extends PollableVisitorAdaptor {
        private PollEvent m_cause;
        
        public InitCause(PollEvent cause) {
            m_cause = cause;
        }

        @Override
        public void visitElement(PollableElement element) {
            if (!element.hasOpenOutage())
                element.setCause(m_cause);
        }
    }

    private PollableNetwork createPollableNetwork(final DataSource db, final ScheduleTimer scheduler, final PollerConfig pollerConfig, final PollOutagesConfig pollOutageConfig, PollContext pollContext) throws UnknownHostException {
        
        final PollableNetwork pNetwork = new PollableNetwork(pollContext);
        
        String sql = "select ifServices.nodeId as nodeId, node.nodeLabel as nodeLabel, ifServices.ipAddr as ipAddr, ifServices.serviceId as serviceId, service.serviceName as serviceName, outages.svcLostEventId as svcLostEventId, events.eventUei as svcLostEventUei, outages.ifLostService as ifLostService, outages.ifRegainedService as ifRegainedService " +
                "from ifServices " +
                "join node on ifServices.nodeId = node.nodeId " +
                "join service on ifServices.serviceId = service.serviceId " +
                "left outer join outages on " +
                "ifServices.nodeId = outages.nodeId and " +
                "ifServices.ipAddr = outages.ipAddr and " +
                "ifServices.serviceId = outages.serviceId and " +
                "ifRegainedService is null " +
                "left outer join events on outages.svcLostEventId = events.eventid " +
                "where ifServices.status = 'A'";

        
        Querier querier = new Querier(db, sql) {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int nodeId = rs.getInt("nodeId");
                String nodeLabel = rs.getString("nodeLabel");
                String ipAddr = rs.getString("ipAddr");
                String serviceName = rs.getString("serviceName");
                Date date = rs.getTimestamp("ifLostService");
                Number svcLostEventId = (Number)rs.getObject("svcLostEventId");
                String svcLostUei = rs.getString("svcLostEventUei");
                
                addServiceToNetwork(pNetwork, nodeId, nodeLabel, ipAddr,
                                    serviceName, svcLostEventId, svcLostUei,
                                    date, scheduler, pollerConfig,
                                    pollOutageConfig);
                    
                // schedule.schedule();
                //MockUtil.println("Created Pollable Service "+svc+" with package "+pkg.getName());

            }

        };
        querier.execute();


        pNetwork.recalculateStatus();
        pNetwork.propagateInitialCause();
        pNetwork.resetStatusChanged();
        return pNetwork;
    }

    @After
    public void tearDown() throws Exception {

        m_eventMgr.finishProcessingEvents();
        MockLogAppender.assertNoWarningsOrGreater();
        m_db.drop();
    }

    @Test
    public void testCreateNode() {
        int nodeId = 99;
        PollableNode node = m_network.createNode(nodeId, "WebServer99");
        assertNotNull("node is null", node);

        assertEquals(0, node.getMemberCount());
        assertEquals(99, node.getNodeId());
        assertEquals("WebServer99", node.getNodeLabel());
        assertEquals(node, m_network.getNode(nodeId));
        assertEquals(m_network, node.getNetwork());
    }
    
    @Test
    public void testCreateInterface() throws UnknownHostException {
        int nodeId = 99;
        InetAddress addr = InetAddressUtils.addr("192.168.1.99");

        PollableInterface iface = m_network.createInterface(nodeId, "WebServer99", addr);
        assertNotNull("iface is null", iface);
        assertEquals(addr, iface.getAddress());
        assertEquals(nodeId, iface.getNodeId());
        assertEquals(iface, m_network.getInterface(nodeId, addr));

        PollableNode node = iface.getNode();
        assertNotNull("node is null", node);
        assertEquals(nodeId, node.getNodeId());
        assertEquals("WebServer99", node.getNodeLabel());
        assertEquals(node, m_network.getNode(nodeId));
        
        assertEquals(m_network, iface.getNetwork());
    }
    
    @Test
    public void testCreateService() throws Exception {
        int nodeId = 99;
        InetAddress addr = InetAddressUtils.addr("192.168.1.99");
        String svcName = "HTTP-99";
        
        PollableService svc = m_network.createService(nodeId, "WebServer99", addr, svcName);
        assertNotNull("svc is null", svc);
        assertEquals(svcName, svc.getSvcName());
        assertEquals(addr, svc.getAddress());
        assertEquals(nodeId, svc.getNodeId());
        assertEquals(svc, m_network.getService(nodeId, addr, svcName));
        
        PollableInterface iface = svc.getInterface();
        assertNotNull("iface is null", iface);
        assertEquals(addr, iface.getAddress());
        assertEquals(nodeId, iface.getNodeId());
        assertEquals(iface, m_network.getInterface(nodeId, addr));
        
        PollableNode node = svc.getNode();
        assertNotNull("node is null", node);
        assertEquals(nodeId, node.getNodeId());
        assertEquals("WebServer99", node.getNodeLabel());
        assertEquals(node, m_network.getNode(nodeId));
        
        assertEquals(m_network, svc.getNetwork());
        
    }
    
    @Test
    public void testVisit() {
        class Counter extends PollableVisitorAdaptor {
            public int svcCount;
            public int ifCount;
            public int nodeCount;
            public int elementCount;
            public int containerCount;
            public int networkCount;

            @Override
            public void visitService(PollableService s) {
                svcCount++;
            }

            @Override
            public void visitInterface(PollableInterface iface) {
                ifCount++;
            }

            @Override
            public void visitNode(PollableNode node) {
                nodeCount++;
            }

            @Override
            public void visitElement(PollableElement element) {
                elementCount++;
            }

            @Override
            public void visitContainer(PollableContainer container) {
                containerCount++;
            }

            @Override
            public void visitNetwork(PollableNetwork n) {
                networkCount++;
            }
        };
        Counter counter = new Counter();
        m_network.visit(counter);
        assertEquals(10, counter.svcCount);
        assertEquals(5, counter.ifCount);
        assertEquals(3, counter.nodeCount);
        assertEquals(1, counter.networkCount);
        assertEquals(19, counter.elementCount);
        assertEquals(9, counter.containerCount);
    }
    
    @Test
    public void testDeleteService() {

        pDot1Icmp.delete();
        
        assertDeleted(pDot1Icmp);
        assertNotDeleted(pDot1);
        assertNotDeleted(pNode1);
        
        pDot1Smtp.delete();
        
        assertDeleted(pDot1Smtp);
        assertDeleted(pDot1);
        assertNotDeleted(pNode1);
        
        pDot2Smtp.delete();
        
        assertDeleted(pDot2Smtp);
        assertNotDeleted(pDot2);
        assertNotDeleted(pNode1);
        
        pDot2Icmp.delete();
        
        assertDeleted(pDot2Icmp);
        assertDeleted(pDot2);
        assertDeleted(pNode1);
        
    }
    
    private void assertDeleted(PollableService svc) {
        assertTrue(svc.isDeleted());
        assertNull(m_network.getService(svc.getNodeId(), svc.getAddress(), svc.getSvcName()));
    }

    private void assertNotDeleted(PollableService svc) {
        assertFalse(svc.isDeleted());
        assertNotNull(m_network.getService(svc.getNodeId(), svc.getAddress(), svc.getSvcName()));
    }

    private void assertDeleted(PollableInterface iface) {
        assertTrue(iface.isDeleted());
        assertNull(m_network.getInterface(iface.getNodeId(), iface.getAddress()));
    }

    private void assertNotDeleted(PollableInterface iface) {
        assertFalse(iface.isDeleted());
        assertNotNull(m_network.getInterface(iface.getNodeId(), iface.getAddress()));
    }

    private void assertDeleted(PollableNode node) {
        assertTrue(node.isDeleted());
        assertNull(m_network.getNode(node.getNodeId()));
    }

    private void assertNotDeleted(PollableNode node) {
        assertFalse(node.isDeleted());
        assertNotNull(m_network.getNode(node.getNodeId()));
    }

    @Test
    public void testDeleteInterface() throws Exception {
        
        pDot1.delete();
        
        assertDeleted(pDot1Icmp);
        assertDeleted(pDot1Smtp);
        assertDeleted(pDot1);
        assertNotDeleted(pDot2);
        assertNotDeleted(pNode1);
        
        pDot2.delete();
        
        assertDeleted(pDot2Icmp);
        assertDeleted(pDot2Smtp);
        assertDeleted(pDot2);;
        assertDeleted(pNode1);
        
    }
    
    @Test
    public void testDeleteNode() throws Exception {
        pNode1.delete();
        
        assertDeleted(pDot1Icmp);
        assertDeleted(pDot1Smtp);
        assertDeleted(pDot1);
        assertDeleted(pDot2Icmp);
        assertDeleted(pDot2Smtp);
        assertDeleted(pDot2);
        assertDeleted(pNode1);
    }
    
    @Test
    public void testDeleteServiceStatus() {
        anticipateDown(mDot1);

        mDot1Icmp.bringDown();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        mDot1.removeService(mDot1Icmp);
        
        anticipateUp(mDot1);
        
        pDot1Icmp.delete();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        
        
    }
    
    @Test
    public void testDowntimeDelete() {
        pDot3Http.getSchedule().schedule();
        
        m_scheduler.next();
        
        assertTime(0);
        assertNotDeleted(pDot3Http);
        
        m_scheduler.next();
        
        assertTime(2000);
        assertNotDeleted(pDot3Http);
        
        anticipateDown(mDot3Http);
        
        mDot3Http.bringDown();
        
        m_scheduler.next();
        
        assertTime(4000);
        assertNotDeleted(pDot3Http);
        
        verifyAnticipated();
        
        m_scheduler.next();
        
        assertTime(4500);
        assertNotDeleted(pDot3Http);
        
        m_anticipator.anticipateEvent(MockEventUtil.createServiceEvent("Test", EventConstants.DELETE_SERVICE_EVENT_UEI, mDot3Http, null));
        
        m_scheduler.next();
        
        assertTime(5000);
        
        verifyAnticipated();
        
        
    }
    
    @Test
    public void testReparentInterface()  {
        InetAddress address = pDot1.getAddress();
        pDot1.reparentTo(pNode2);
        
        assertNull(m_network.getInterface(1, address));
        assertNotNull(m_network.getInterface(2, address));
        assertEquals(2, pDot1.getNodeId());
        assertSame(pNode2, pDot1.getNode());
    }
    
    @Test
    public void testReparentOutages() {
        // create some outages in the database
        mDot1.bringDown();
        
        pDot1Icmp.doPoll();
        m_network.processStatusChange(new Date());
        
        mDot1.bringUp();
        
        pDot1Icmp.doPoll();
        m_network.processStatusChange(new Date());
        
        final String ifOutageOnNode1 = "select * from outages where nodeId = 1 and ipAddr = '192.168.1.1'";
        final String ifOutageOnNode2 = "select * from outages where nodeId = 2 and ipAddr = '192.168.1.1'";
        
        m_eventMgr.finishProcessingEvents();

        assertEquals(2, m_db.countRows(ifOutageOnNode1));
        assertEquals(0, m_db.countRows(ifOutageOnNode2));
        
        m_db.reparentInterface(pDot1.getIpAddr(), pDot1.getNodeId(), pNode2.getNodeId());
        pDot1.reparentTo(pNode2);
        
        assertEquals(0, m_db.countRows(ifOutageOnNode1));
        assertEquals(2, m_db.countRows(ifOutageOnNode2));

    }
    
    @Test(timeout=30000)
    public void testReparentStatusChanges() {
        

        //
        // Plan to bring down both nodes except the reparented interface
        // the node owning the interface should be up while the other is down
        // after reparenting we should got the old owner go down while the other
        // comes up.
        //
        anticipateDown(mNode2);
        anticipateDown(mDot1);

        // bring down both nodes but bring iface back up
        mNode1.bringDown();
        mNode2.bringDown();
        mDot2.bringUp();

        pDot1Icmp.doPoll();
        m_network.processStatusChange(new Date());
        pDot2Icmp.doPoll();
        m_network.processStatusChange(new Date());
        pDot3Icmp.doPoll();
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();

        m_db.reparentInterface(mDot2.getIpAddr(), mDot2.getNodeId(), mNode2.getNodeId());
        mDot2.moveTo(mNode2);

        resetAnticipated();
        anticipateDown(mNode1);
        anticipateUp(mNode2);
        anticipateDown(mDot3);

        pDot2.reparentTo(pNode2);
        
        verifyAnticipated();

    }
    
    @Test
    public void testStatus() throws Exception {
        PollableVisitor updater = new PollableVisitorAdaptor() {
            @Override
            public void visitElement(PollableElement e) {
                e.updateStatus(PollStatus.down());
            }
        };
        m_network.visit(updater);
        PollableVisitor downChecker = new PollableVisitorAdaptor() {
            @Override
            public void visitElement(PollableElement e) {
                assertEquals(PollStatus.down(), e.getStatus());
                assertEquals(true, e.isStatusChanged());
            }
        };
        m_network.visit(downChecker);
        m_network.resetStatusChanged();
        PollableVisitor statusChangedChecker = new PollableVisitorAdaptor() {
            @Override
            public void visitElement(PollableElement e) {
                assertEquals(false, e.isStatusChanged());
            }
        };
        m_network.visit(statusChangedChecker);
        
        pDot1Icmp.updateStatus(PollStatus.up());
        m_network.recalculateStatus();
        
        PollableVisitor upChecker = new PollableVisitorAdaptor() {
            @Override
            public void visitNode(PollableNode node) {
                if (node == pDot1Icmp.getNode())
                    assertUp(node);
                else
                    assertDown(node);
            }
            @Override
            public void visitInterface(PollableInterface iface) {
                if (iface == pDot1Icmp.getInterface())
                    assertUp(iface);
                else
                    assertDown(iface);
            }
            @Override
            public void visitService(PollableService s) {
                if (s == pDot1Icmp)
                    assertUp(s);
                else
                    assertDown(s);
            }
        };
        m_network.visit(upChecker);
    }
    
    @Test
    public void testInterfaceStatus() throws Exception {
        
        
        pDot2Smtp.updateStatus(PollStatus.down());
        m_network.recalculateStatus();
        
        assertDown(pDot2Smtp);
        assertUp(pDot2Smtp.getInterface());
        
        pDot2Smtp.updateStatus(PollStatus.up());
        m_network.recalculateStatus();
        
        assertUp(pDot2Smtp);
        assertUp(pDot2Smtp.getInterface());
        
        pDot2Icmp.updateStatus(PollStatus.down());
        m_network.recalculateStatus();
        
        assertDown(pDot2Icmp);
        assertDown(pDot2Icmp.getInterface());
        
    }

    @Test
    public void testFindMemberWithDescendent() throws Exception {

        assertSame(pNode1, m_network.findMemberWithDescendent(pDot1Icmp));
        assertSame(pDot1, pNode1.findMemberWithDescendent(pDot1Icmp));
        assertSame(pDot1Icmp, pDot1.findMemberWithDescendent(pDot1Icmp));
        
        // pDot1Icmp is not a descendent of pNode2
        assertNull(pNode2.findMemberWithDescendent(pDot1Icmp));
        
        
    }
    

    @Test
    public void testPropagateUnresponsive() throws Exception {

        pDot1Smtp.updateStatus(PollStatus.unresponsive());
        pDot1Icmp.updateStatus(PollStatus.unresponsive());
        m_network.recalculateStatus();
        
        assertUp(pDot1);
        
    }
    
    @Test
    public void testPollUnresponsive() {
        m_pollContext.setServiceUnresponsiveEnabled(true);
        
        anticipateUnresponsive(mDot1);
        
        mDot1.bringUnresponsive();
            
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());

        assertUp(pDot1);
        verifyAnticipated();
        
        anticipateResponsive(mDot1);
        
        mDot1.bringUp();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());

        assertUp(pDot1);
        verifyAnticipated();
        
    }
    
    @Test
    public void testPollUnresponsiveWithOutage() {
        m_pollContext.setServiceUnresponsiveEnabled(true);
        
        anticipateUnresponsive(mDot1);
        
        mDot1.bringUnresponsive();
            
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());

        assertUp(pDot1);
        verifyAnticipated();
        
        anticipateDown(mDot1);
        
        mDot1.bringDown();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUp(mDot1);
        
        mDot1.bringUp();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUnresponsive(mDot1);
        
        mDot1.bringUnresponsive();
            
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());

        assertUp(pDot1);
        verifyAnticipated();
        
        
    }
    
    @Test
    public void testNoEventsOnNoOutages() throws Exception {
        
        // poll expecting no events
        pDot1Smtp.doPoll();
        m_network.processStatusChange(new Date());

        verifyAnticipated();

    }
    
    
    @Test
    public void testPollService() throws Exception {

        PollableService pSvc = pDot1Smtp;
        MockService mSvc = mDot1Smtp;
        
        pSvc.doPoll();
        assertUp(pSvc);
        assertUnchanged(pSvc);
        
        mSvc.bringDown();
        
        pSvc.doPoll();
        assertDown(pSvc);
        assertChanged(pSvc);
        pSvc.resetStatusChanged();
        
        mSvc.bringUp();
        
        pSvc.doPoll();
        assertUp(pSvc);
        assertChanged(pSvc);
        pSvc.recalculateStatus();
    }
    
    @Test
    public void testPollAllUp() throws Exception {

        pDot1Icmp.doPoll();
        assertUp(pDot1Icmp);
        assertUp(pDot1);
        assertUnchanged(pDot1Icmp);
        assertUnchanged(pDot1);

        assertPoll(mDot1Icmp);
        assertNoPoll(m_mockNetwork);
        
    }
    
    @Test
    public void testPollIfUpNonCritSvcDown() throws Exception {

        mDot1Smtp.bringDown();
        
        pDot1Smtp.doPoll();
        assertDown(pDot1Smtp);
        assertUp(pDot1);
        assertChanged(pDot1Smtp);
        assertUnchanged(pDot1);

        assertPoll(mDot1Smtp);
        assertPoll(mDot1Icmp);
        assertNoPoll(m_mockNetwork);
        
        
    }
    
    @Test
    public void testPollIfUpCritSvcDownPoll() throws Exception {

        mDot1Icmp.bringDown();
        
        pDot1Icmp.doPoll();
        
        assertDown(pDot1Icmp);
        assertDown(pDot1);
        assertChanged(pDot1Icmp);
        assertChanged(pDot1);

        assertPoll(mDot1Icmp);
        assertPoll(mDot2Icmp);
        assertNoPoll(m_mockNetwork);

    }
    
    
    @Test
    public void testPollIfDownNonCritSvcUp() throws Exception {

        mDot1.bringDown();

        pDot1.updateStatus(PollStatus.down());
        pDot1Icmp.updateStatus(PollStatus.down());
        
        m_network.recalculateStatus();
        m_network.resetStatusChanged();

        assertDown(pDot1Icmp);
        assertDown(pDot1);
        
        
        mDot1Smtp.bringUp();
        
        pDot1Smtp.doPoll();
        
        assertDown(pDot1Icmp);
        assertDown(pDot1);
        assertUnchanged(pDot1Icmp);
        assertUnchanged(pDot1);

        assertNoPoll(m_mockNetwork);

    }

    @Test
    public void testPollIfDownCritSvcUp() throws Exception {

        mDot1.bringDown();

        pDot1.updateStatus(PollStatus.down());
        pDot1Icmp.updateStatus(PollStatus.down());
        pDot1.setCause(new DbPollEvent(1, EventConstants.INTERFACE_DOWN_EVENT_UEI, new Date()));

        m_network.recalculateStatus();
        m_network.resetStatusChanged();

        assertDown(pDot1Icmp);
        assertDown(pDot1);
        
        mDot1Icmp.bringUp();
        
        pDot1Icmp.doPoll();
        
        assertDown(pDot1Smtp);
        assertUp(pDot1Icmp);
        assertUp(pDot1);
        
        assertChanged(pDot1Icmp);
        assertChanged(pDot1);
        
        assertPoll(mDot1Smtp);
        assertPoll(mDot1Icmp);
        assertNoPoll(m_mockNetwork);

    }

    @Test
    public void testPollIfUpCritSvcUndefSvcDown() throws Exception {
        m_pollContext.setCriticalServiceName(null);

        mDot1Smtp.bringDown();
        
        pDot1Smtp.doPoll();
        
        assertDown(pDot1Smtp);
        assertUp(pDot1);
        assertChanged(pDot1Smtp);
        assertUnchanged(pDot1);

        assertPoll(mDot1Smtp);
        assertPoll(mDot1Icmp);
        assertNoPoll(m_mockNetwork);

    }

    @Test
    public void testPollIfDownCritSvcUndefSvcDown() throws Exception {
        m_pollContext.setCriticalServiceName(null);

        mDot1.bringDown();

        pDot1.updateStatus(PollStatus.down());
        pDot1Icmp.updateStatus(PollStatus.down());
        pDot1Smtp.updateStatus(PollStatus.down());
        
        m_network.recalculateStatus();
        m_network.resetStatusChanged();

        assertDown(pDot1Smtp);
        assertDown(pDot1Icmp);
        assertDown(pDot1);
        
        mDot1Smtp.bringUp();
        
        pDot1Smtp.doPoll();
        
        assertUp(pDot1Smtp);
        assertDown(pDot1Icmp);
        assertUp(pDot1);
        assertChanged(pDot1Smtp);
        assertUnchanged(pDot1Icmp);
        assertChanged(pDot1);

        assertPoll(mDot1Smtp);
        assertPoll(mDot1Icmp);
        assertNoPoll(m_mockNetwork);

    }

    @Test
    public void testPollIfUpCritSvcUndefSvcDownNoPoll() throws Exception {
        m_pollContext.setCriticalServiceName(null);
        m_pollContext.setPollingAllIfCritServiceUndefined(false);

        mDot1Smtp.bringDown();
        
        pDot1Smtp.doPoll();
        
        assertDown(pDot1Smtp);
        assertUp(pDot1);
        assertChanged(pDot1Smtp);
        assertUnchanged(pDot1);

        assertPoll(mDot1Smtp);
        assertNoPoll(m_mockNetwork);

    }

    @Test
    public void testPollIfDownCritSvcUndefSvcDownNoPoll() throws Exception {
        m_pollContext.setCriticalServiceName(null);
        m_pollContext.setPollingAllIfCritServiceUndefined(false);

        mDot1.bringDown();

        pDot1.updateStatus(PollStatus.down());
        pDot1Icmp.updateStatus(PollStatus.down());
        pDot1Smtp.updateStatus(PollStatus.down());
        
        m_network.recalculateStatus();
        m_network.resetStatusChanged();

        assertDown(pDot1Smtp);
        assertDown(pDot1Icmp);
        assertDown(pDot1);
        
        mDot1Smtp.bringUp();
        
        pDot1Smtp.doPoll();
        
        assertUp(pDot1Smtp);
        assertDown(pDot1Icmp);
        assertUp(pDot1);
        assertChanged(pDot1Smtp);
        assertUnchanged(pDot1Icmp);
        assertChanged(pDot1);

        assertPoll(mDot1Smtp);
        assertNoPoll(m_mockNetwork);

    }
    
    @Test
    public void testPollNode() throws Exception {
        mNode1.bringDown();
        
        pDot1Smtp.doPoll();
        assertDown(pDot1Smtp);
        assertDown(pDot1Icmp);
        assertDown(pDot2Icmp);
        assertDown(pNode1);
        
        assertPoll(mDot1Smtp);
        assertPoll(mDot1Icmp);
        assertPoll(mDot2Icmp);
        assertNoPoll(m_mockNetwork);
        
    }
    
    @Test
    public void testNodeProcessingDisabled() {
        m_pollContext.setNodeProcessingEnabled(false);
        
        // anticipate nothing
        
        pDot1Smtp.run();
        
        verifyAnticipated();
        
        anticipateDown(mDot1Smtp);
        
        mDot1Smtp.bringDown();
        
        pDot1Smtp.run();
        
        verifyAnticipated();
        
        // anticipate nothing since its still down
        
        pDot1Smtp.run();
        
        verifyAnticipated();
        
        anticipateUp(mDot1Smtp);
        
        mDot1Smtp.bringUp();
        
        pDot1Smtp.run();
        
        verifyAnticipated();
        
    }
    
    @Test
    public void testServiceEvent() throws Exception {
        MockService mSvc = mDot1Smtp;
        PollableService pSvc = pDot1Smtp;
        
        anticipateDown(mSvc);

        mSvc.bringDown();
        
        pSvc.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        // anticipate nothin since service is still down
        
        pSvc.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUp(mSvc);
        
        mSvc.bringUp();
        
        pSvc.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
    }

    @Test
    public void testInterfaceEvent() throws Exception {
        anticipateDown(mDot1);

        mDot1.bringDown();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUp(mDot1);
        
        mDot1.bringUp();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
    }
    
    @Test
    public void testNodeEvent() throws Exception {
        anticipateDown(mNode1);

        mNode1.bringDown();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUp(mNode1);
        
        mNode1.bringUp();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
    }
    
    @Test
    public void testLingeringSvcDownOnIfUp() throws Exception {
        anticipateDown(mDot1);

        mDot1.bringDown();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUp(mDot1);
        anticipateDown(mDot1Smtp);
        
        mDot1.bringUp();
        mDot1Smtp.bringDown();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUp(mDot1Smtp);
        
        mDot1Smtp.bringUp();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();

    }

    @Test
    public void testLingeringSvcDownOnNodeUp() throws Exception {
        anticipateDown(mNode1);

        mNode1.bringDown();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUp(mNode1);
        anticipateDown(mDot1);
        
        mNode1.bringUp();
        mDot1Icmp.bringDown();
        
        pDot2Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUp(mDot1);
        anticipateDown(mDot1Smtp);
        
        mDot1.bringUp();
        mDot1Smtp.bringDown();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();

    }
    
    @Test
    public void testSvcOutage() {
        
        anticipateDown(mDot1Smtp);
        
        mDot1Smtp.bringDown();
        
        pDot1Smtp.doPoll();
        
        pDot1Smtp.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUp(mDot1Smtp);

        mDot1Smtp.bringUp();
        
        pDot1Smtp.doPoll();

        pDot1Smtp.processStatusChange(new Date());
        
        verifyAnticipated();
        
    }
    
    @Test
    public void testIfOutage() {
        anticipateDown(mDot1);
        
        mDot1.bringDown();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUp(mDot1);
        
        mDot1.bringUp();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
    }
    
    @Test
    public void testCause() {
        anticipateDown(mDot1Smtp);
        
        mDot1Smtp.bringDown();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();

        anticipateDown(mDot1);
        
        mDot1.bringDown();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        PollEvent cause = pDot1.getCause();
        assertNotNull(cause);
        
        assertEquals(cause, pDot1.getCause());
        assertEquals(cause, pDot1Icmp.getCause());
        assertFalse(cause.equals(pDot1Smtp.getCause()));
        
    }
    
    @Test
    public void testIndependentOutageEventsUpTogether() throws Exception {
        anticipateDown(mDot1Smtp);
        
        mDot1Smtp.bringDown();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());

        verifyAnticipated();
        
        anticipateDown(mNode1);

        mNode1.bringDown();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUp(mDot1Smtp);
        anticipateUp(mNode1);
        
        mNode1.bringUp();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();


    }
    
    @Test
    public void testIndependentOutageEventsUpSeparately() throws Exception {
        anticipateDown(mDot1Smtp);
        
        mDot1Smtp.bringDown();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());

        verifyAnticipated();
        
        anticipateDown(mNode1);

        mNode1.bringDown();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUp(mNode1);
        m_outageAnticipator.deanticipateOutageClosed(mDot1Smtp, mNode1.createUpEvent());
        
        mNode1.bringUp();
        mDot1Smtp.bringDown();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();

        anticipateUp(mDot1Smtp);

        mDot1Smtp.bringUp();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
    }
    
    @Test
    public void testDowntimeInterval() {
        // HERE ARE the calls to setup the downtime model
//        m_pollerConfig.addDowntime(100L, 0L, 500L, false);
//        m_pollerConfig.addDowntime(200L, 500L, 1500L, false);
//        m_pollerConfig.addDowntime(500L, 1500L, -1L, true);

        Package pkg = m_pollerConfig.getPackage("TestPackage");
        PollableServiceConfig pollConfig = new PollableServiceConfig(pDot1Smtp, m_pollerConfig, m_pollerConfig, pkg, m_timer);
        
        m_timer.setCurrentTime(1000L);
        pDot1Smtp.updateStatus(PollStatus.down());
        assertEquals(1000, pDot1Smtp.getStatusChangeTime());
        assertDown(pDot1Smtp);
        pDot1.resetStatusChanged();
        
        assertEquals(100L, pollConfig.getInterval());

        m_timer.setCurrentTime(1234L);
        
        assertEquals(100L, pollConfig.getInterval());
        
        m_timer.setCurrentTime(1500L);
        
        assertEquals(200L, pollConfig.getInterval());

        m_timer.setCurrentTime(1700L);
        
        assertEquals(200L, pollConfig.getInterval());
        
        m_timer.setCurrentTime(2500L);
        
        assertEquals(-1L, pollConfig.getInterval());
        //assertTrue(pDot1Smtp.isDeleted());
        

        
    }
    
    @Test
    public void testSchedule() {
        pDot1Smtp.getSchedule().schedule();
        
        m_scheduler.next();
        
        assertPoll(mDot1Smtp);
        assertTime(0);
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);
        
        mDot1Smtp.bringDown();

        m_scheduler.next();
        
        assertPoll(mDot1Smtp);
        assertTime(1000);
        assertDown(pDot1Smtp);
        assertChanged(pDot1Smtp);
        pDot1Smtp.resetStatusChanged();
        
        // test scheduling for downTime model
        
        for(int downFor = 100; downFor < 500; downFor += 100) {
            m_scheduler.next();
            assertPoll(mDot1Smtp);
            assertTime(1000+downFor);
        }
        
        for(int downFor = 500; downFor < 1500; downFor += 200) {
            m_scheduler.next();
            assertPoll(mDot1Smtp);
            assertTime(1000+downFor);
        }   
        
        
        mDot1Smtp.bringUp();
        
        m_scheduler.next();
        
        assertPoll(mDot1Smtp);
        assertUp(pDot1Smtp);
        assertChanged(pDot1Smtp);
        pDot1Smtp.recalculateStatus();

        
    }
    
    @Test
    public void testScheduleAdjust() {
        // change SMTP so it is only polled every 10 secs rather than 1 sec
        m_pollerConfig.setPollInterval(m_pollerConfig.getPackage("TestPackage"), "SMTP", 10000L);
        
        pDot1Icmp.getSchedule().schedule();
        pDot1Smtp.getSchedule().schedule();
        
        // get the immediate polls out of the way
        m_scheduler.next();
        m_scheduler.next();
        
        assertTime(0);
        assertPoll(mDot1Icmp);
        assertPoll(mDot1Smtp);
        assertUp(pDot1Smtp);
        assertUp(pDot1Icmp);
        assertUnchanged(pDot1Smtp);
        assertUnchanged(pDot1Icmp);
        
        // not we should come to the poll for icmp
        m_scheduler.next();
        
        // icmp should be polled but not smtp and they both should be up
        assertTime(1000);
        assertPoll(mDot1Icmp);
        assertNoPoll(mDot1Smtp);
        assertUp(pDot1Smtp);
        assertUp(pDot1Icmp);
        assertUnchanged(pDot1Smtp);
        assertUnchanged(pDot1Icmp);
        
        // now bring down both services
        mDot1.bringDown();
        
        // we come to the next icmp poll still not time to poll smtp
        m_scheduler.next();
        
        // no need to poll smtp because icmp reports itself down
        assertTime(2000);
        assertPoll(mDot1Icmp);
        assertNoPoll(mDot1Smtp);
        assertUp(pDot1Smtp);  // TODO:  i wonder if this matters... its really down (the outage does get created)
        assertDown(pDot1Icmp);
        assertUnchanged(pDot1Smtp);
        assertChanged(pDot1Icmp);
        
        // now we bring icmp back up but not smtp.  it is still not time for a scheduled smtp poll
        mDot1Icmp.bringUp();
        
        // we come to the next icmp poll in only 100ms according to the downtime model
        m_scheduler.next();
        
        // since icmp came up we do an unscheduled poll of smtp and find its down
        assertTime(2100);
        assertPoll(mDot1Icmp);
        assertPoll(mDot1Smtp);
        assertDown(pDot1Smtp);
        assertUp(pDot1Icmp);
        assertChanged(pDot1Smtp);
        assertChanged(pDot1Icmp);
        
        // since smtp is now down, the schedule for smtp should be adjusted according
        // to the downtime model so we expect the next poll for it in only 100ms
        m_scheduler.next();
        
        // this time we should poll only smtp and find it still down
        assertTime(2200);
        assertNoPoll(mDot1Icmp);
        assertPoll(mDot1Smtp);
        assertDown(pDot1Smtp);
        assertUp(pDot1Icmp);
        assertUnchanged(pDot1Smtp);
        assertUnchanged(pDot1Icmp);
        
        mDot1Smtp.bringUp();
        
        // another downtime model poll of smtp
        m_scheduler.next();

        assertTime(2300);
        assertNoPoll(mDot1Icmp);
        assertPoll(mDot1Smtp);
        assertUp(pDot1Smtp);
        assertUp(pDot1Icmp);
        assertChanged(pDot1Smtp);
        assertUnchanged(pDot1Icmp);
        
        // now the next one should be the next scheduled icmp poll
        m_scheduler.next();
        
        assertTime(3100);
        assertPoll(mDot1Icmp);
        assertNoPoll(mDot1Smtp);
        assertUp(pDot1Smtp);
        assertUp(pDot1Icmp);
        assertUnchanged(pDot1Smtp);
        assertUnchanged(pDot1Icmp);
        
        
    }
    
    @Test
    public void testComputeScheduledOutageTime() {
        Package pkg = m_pollerConfig.getPackage("TestPackage");
        m_pollerConfig.addScheduledOutage(pkg, "first", 3000, 5000, "192.168.1.1");
               PollableServiceConfig pollConfig = new PollableServiceConfig(pDot1Smtp, m_pollerConfig, m_pollerConfig, pkg, m_timer);
        
        m_timer.setCurrentTime(2000L);
        
        assertFalse(pollConfig.scheduledSuspension());
        
        m_timer.setCurrentTime(4000L);
        
        assertTrue(pollConfig.scheduledSuspension());
        
        
    }
    
    @Test
    public void testScheduledOutage() {
        m_pollerConfig.addScheduledOutage(m_pollerConfig.getPackage("TestPackage"), "first", 3000, 5000, "192.168.1.1");

        pDot1Smtp.getSchedule().schedule();
        
        m_scheduler.next();
        
        assertPoll(mDot1Smtp);
        assertTime(0);
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);
        
        m_scheduler.next();
        
        assertPoll(mDot1Smtp);
        assertTime(1000);
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);

        m_scheduler.next();
        
        assertPoll(mDot1Smtp);
        assertTime(2000);
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);

        m_scheduler.next();
        
        assertNoPoll(mDot1Smtp);
        assertTime(3000);
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);

        m_scheduler.next();
        
        assertNoPoll(mDot1Smtp);
        assertTime(4000);
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);

        m_scheduler.next();
        
        assertNoPoll(mDot1Smtp);
        assertTime(5000);
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);

        m_scheduler.next();
        
        assertPoll(mDot1Smtp);
        assertTime(6000);
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);
    }
    
    @Test
    public void testMidnightOutageBug1122() throws ParseException {
        m_pollerConfig.addScheduledOutage(m_pollerConfig.getPackage("TestPackage"), "first", "monday", "23:59:57", "23:59:59", "192.168.1.1");
        m_pollerConfig.addScheduledOutage(m_pollerConfig.getPackage("TestPackage"), "second", "tuesday", "00:00:00", "00:00:02", "192.168.1.1");

        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		Date start = dateFormat.parse("21-FEB-2005 23:59:56");
        long startTime = start.getTime();
        m_timer.setCurrentTime(startTime);
        
        pDot1Smtp.getSchedule().schedule();
        
        m_scheduler.next();
        
        assertPoll(mDot1Smtp);
        assertTime(startTime+0); // 23:59:56 should poll
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);
        
        m_scheduler.next();
        
        assertNoPoll(mDot1Smtp);
        assertTime(startTime+1000); // 23:59:57 should not poll
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);

        m_scheduler.next();
       
        assertNoPoll(mDot1Smtp);
        assertTime(startTime+2000); // 23:59:58 should not poll
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);

        m_scheduler.next();
        
        assertNoPoll(mDot1Smtp);
        assertTime(startTime+3000); // 23:59:59 should not poll
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);

        m_scheduler.next();
        
        assertNoPoll(mDot1Smtp);
        assertTime(startTime+4000); // 00:00:00 should not poll
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);

        m_scheduler.next();
        
        assertNoPoll(mDot1Smtp);
        assertTime(startTime+5000);  // 00:00:01 should not poll
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);

        m_scheduler.next();
        
        assertNoPoll(mDot1Smtp);
        assertTime(startTime+6000);  // 00:00:02 should not poll
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);

        m_scheduler.next();
        
        assertPoll(mDot1Smtp);
        assertTime(startTime+7000);  // 00:00:03 should poll
        assertUp(pDot1Smtp);
        assertUnchanged(pDot1Smtp);
    }
    
    @Test
    public void testLoadService() throws Exception {

        anticipateDown(mDot1Smtp);
        
        mDot1Smtp.bringDown();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        // recreate the pollable network from the database
        m_network = createPollableNetwork(m_db, m_scheduler, m_pollerConfig, m_pollerConfig, m_pollContext);
        assignPollableMembers(m_network);
        
        assertDown(pDot1Smtp);
        
        anticipateUp(mDot1Smtp);

        mDot1Smtp.bringUp();
        
        pDot1Smtp.doPoll();

        m_network.processStatusChange(new Date());
        
        verifyAnticipated();

        
    }
    
    @Test
    public void testAddUpServiceToUpNodeWithCritSvc() throws Exception {
        testAddUpSvcToUpNode(1, "Router", "192.168.1.1", "SMTP", "HTTP");
    }

    @Test
    public void testAddUpServiceToUpNodeHasNoCritSvc() throws Exception {
        testAddUpSvcToUpNode(3, "Firewall", "192.168.1.4", "SMTP", "SNMP");
    }

    private void testAddUpSvcToUpNode(int nodeId, String nodeLabel,
            String ipAddr, String existingSvcName, String newSvcName) {

        PollableService pExistingSvc = m_network.getService(nodeId, getInetAddress(ipAddr), existingSvcName);
        assertNotNull(pExistingSvc);

        PollableInterface pIface = pExistingSvc.getInterface();
        PollableNode pNode = pIface.getNode();

        pExistingSvc.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        MockService mSvc = m_mockNetwork.addService(nodeId, ipAddr, newSvcName);
        m_db.writeService(mSvc);
        PollableService pSvc = addServiceToNetwork(nodeId, nodeLabel, ipAddr, newSvcName);
        
        assertNotNull(pSvc);
        
        assertElementHasNullCause(pSvc);
        assertElementHasNullCause(pExistingSvc);
        assertElementHasNullCause(pIface);
        assertElementHasNullCause(pNode);
        
        assertUp(pSvc);
        assertUp(pExistingSvc);
        assertUp(pIface);
        assertUp(pNode);
        
        anticipateDown(mSvc);

        mSvc.bringDown();
        
        pSvc.doPoll();

        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
    }
    
    @Test
    public void testAddDownServiceToDownNodeWithCritSvc() throws Exception {
        addDownServiceToDownNode(1, "Router", "192.168.1.1", "SMTP", "HTTP");
    }

    @Test
    public void testAddDownServiceToDownNodeHasNoCritSvc() throws Exception {
        addDownServiceToDownNode(3, "Firewall", "192.168.1.4", "SMTP", "SNMP");
    }

    private void addDownServiceToDownNode(int nodeId, String nodeLabel,
            String ipAddr, String existingSvcName, String newSvcName) {
        MockNode mNode = m_mockNetwork.getNode(nodeId);
        
        PollableService pExistingSvc = m_network.getService(nodeId, getInetAddress(ipAddr), existingSvcName);
        PollableInterface pIface = pExistingSvc.getInterface();
        PollableNode pNode = pExistingSvc.getNode();
        
        
        // before we start make sure the node is down
        anticipateDown(mNode);
        
        mNode.bringDown();
        
        pExistingSvc.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        // ok the node is down.. here is the cause
        PollEvent nodeCause = pNode.getCause();
        
        // add a new mock service 
        MockService mSvc = m_mockNetwork.addService(nodeId, ipAddr, newSvcName);
        m_db.writeService(mSvc);
        
        // that is down
        mSvc.bringDown();

        // expect nothing since we already have node down event outstanding

        // simulate a nodeGainedService event
        PollableService pSvc = addServiceToNetwork(nodeId, nodeLabel, ipAddr, newSvcName);
        assertNotNull(pSvc);

        // before the first poll everthing should have the node down cause
        assertElementHasCause(pSvc, nodeCause);
        assertElementHasCause(pExistingSvc, nodeCause);
        assertElementHasCause(pIface, nodeCause);
        assertElementHasCause(pNode, nodeCause);
        
        // and should be mored down
        assertDown(pSvc);
        assertDown(pExistingSvc);
        assertDown(pIface);
        assertDown(pNode);
        
        // now to the first poll
        pSvc.doPoll();

        // everything should still be down
        assertDown(pSvc);
        assertDown(pExistingSvc);
        assertDown(pIface);
        assertDown(pNode);

        m_network.processStatusChange(new Date());

        // and should have the same node down cause
        assertElementHasCause(pSvc, nodeCause);
        assertElementHasCause(pExistingSvc, nodeCause);
        assertElementHasCause(pIface, nodeCause);
        assertElementHasCause(pNode, nodeCause);
        
        // verify we've received no events
        verifyAnticipated();
    }
    
    @Test
    public void testAddDownServiceToUpNodeWithCritSvc() throws Exception {
        testAddDownServiceToUpNode(1, "Router", "192.168.1.1", "SMTP", "HTTP");
    }

    @Test
    public void testAddDownServiceToUpNodeHasNoCritSvc() throws Exception {
        testAddDownServiceToUpNode(3, "Firewal", "192.168.1.4", "SMTP", "SNMP");
    }

    private void testAddDownServiceToUpNode(int nodeId, String nodeLabel,
            String ipAddr, String existingSvcName, String newSvcName) {
        PollableService pExistingSvc = m_network.getService(nodeId, getInetAddress(ipAddr), existingSvcName);
        PollableInterface pIface = pExistingSvc.getInterface();
        PollableNode pNode = pExistingSvc.getNode();
        
        // first we cause a poll for the up node just be make sure everythings working
        pExistingSvc.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();

        // no we add the mock serve
        MockService mSvc = m_mockNetwork.addService(nodeId, ipAddr, newSvcName);
        m_db.writeService(mSvc);
        
        // bring down the service
        mSvc.bringDown();

        // expect a nodeLostService event
        anticipateDown(mSvc);

        // add the service to the PollableNetowrk (simulates nodeGainedService event)
        PollableService pSvc = addServiceToNetwork(nodeId, nodeLabel, ipAddr, newSvcName);
        assertNotNull(pSvc);
        
        // before the first call nothing has a cause
        assertElementHasNullCause(pSvc);
        assertElementHasNullCause(pExistingSvc);
        assertElementHasNullCause(pIface);
        assertElementHasNullCause(pNode);
        
        // and everything is up
        assertUp(pSvc);
        assertUp(pExistingSvc);
        assertUp(pIface);
        assertUp(pNode);
        
        // now poll
        pSvc.doPoll();

        // expect the svc to be down and everythign else up
        assertDown(pSvc);
        assertUp(pExistingSvc);
        assertUp(pIface);
        assertUp(pNode);

        // no we send the events and update the causes
        m_network.processStatusChange(new Date());

        // only the svc has a cause
        assertNotNull(pSvc.getCause());
        assertElementHasNullCause(pExistingSvc);
        assertElementHasNullCause(pIface);
        assertElementHasNullCause(pNode);
        
        verifyAnticipated();
    }
    
    @Test
    public void testLoadInterface() throws Exception {
        anticipateDown(mDot1);
        
        mDot1.bringDown();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();

        PollEvent ifCause = pDot1.getCause();

        // recreate the pollable network from the database
        m_network = createPollableNetwork(m_db, m_scheduler, m_pollerConfig, m_pollerConfig, m_pollContext);
        assignPollableMembers(m_network);

        assertElementHasCause(pDot1Smtp, ifCause);
        assertElementHasCause(pDot1Icmp, ifCause);
        assertElementHasCause(pDot1, ifCause);
        assertElementHasNullCause(pNode1);

        assertDown(pDot1Smtp);
        assertDown(pDot1Icmp);

        anticipateUp(mDot1);

        mDot1.bringUp();

        pDot1Icmp.doPoll();

        m_network.processStatusChange(new Date());

        verifyAnticipated();


    }

    @Test
    public void testLoadInterfaceWithMissingOutage() throws Exception {

        // create an outage for only the SMTP svc but with an if down event
        mDot4.bringDown();

        Event ifDownEvent = mDot4.createDownEvent();

        m_db.writeEvent(ifDownEvent);
        m_db.createOutage(mDot4Smtp, ifDownEvent);


        // recreate the pollable network from the database
        m_network = createPollableNetwork(m_db, m_scheduler, m_pollerConfig, m_pollerConfig, m_pollContext);
        assignPollableMembers(m_network);

        resetAnticipated();

        assertElementHasCause(pDot4Smtp, ifDownEvent);
        assertElementHasCause(pDot4, ifDownEvent);
        // see if cause is pushed down to service without outage
        assertElementHasCause(pDot4Http, ifDownEvent);

        assertDown(pDot4Smtp);
        assertDown(pDot4Http);

        anticipateUp(mDot4);

        mDot4.bringUp();

        pDot4Http.doPoll();

        m_network.processStatusChange(new Date());

        verifyAnticipated();


    }

    private void assertElementHasCause(PollableElement element, Event causeEvent) {
        if (causeEvent == null) {
            assertElementHasNullCause(element);
        } else {
            if (element.getCause() == null || element.getCause().getEventId() != causeEvent.getDbid()) {
                failCause(element, causeEvent);
            }
        }
    }
    
    private void assertElementHasNullCause(PollableElement element) {
        assertNull("Unexpected cause for element "+element+": Expected cause to be null", element.getCause());
    }

    private void assertElementHasCause(PollableElement element, PollEvent causeEvent) {
        if (causeEvent == null) {
            assertElementHasNullCause(element);
        } else {
            if (element.getCause() == null || element.getCause().getEventId() != causeEvent.getEventId()) {
                failCause(element, causeEvent);
            }
        }
    }


    private void failCause(PollableElement element, Object expectedCause) {
        throw new AssertionError("Unexpected cause for element "+element+" expected cause matching "+expectedCause+" but cause was "+element.getCause());
    }

    @Test
    public void testLoadNode() throws Exception {
        anticipateDown(mNode1);
        
        mNode1.bringDown();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        System.out.println("Cause is "+pNode1.getCause());
        
        verifyAnticipated();
        
        // recreate the pollable network from the database
        m_network = createPollableNetwork(m_db, m_scheduler, m_pollerConfig, m_pollerConfig, m_pollContext);
        assignPollableMembers(m_network);
        
        assertDown(pDot1Smtp);
        assertDown(pDot1Icmp);
        assertDown(pDot2Smtp);
        assertDown(pDot2Icmp);
        
        anticipateUp(mNode1);

        mNode1.bringUp();
        
        pDot1Icmp.doPoll();

        m_network.processStatusChange(new Date());
        
        verifyAnticipated();

        
    }
    
    @Test
    public void testLoadIndependentOutageEventsUpTogether() throws Exception {
        anticipateDown(mDot1Smtp);
        
        mDot1Smtp.bringDown();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        PollEvent svcCause = pDot1Smtp.getCause();

        verifyAnticipated();
        
        anticipateDown(mNode1);

        mNode1.bringDown();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        PollEvent nodeCause = pNode1.getCause();

        assertElementHasCause(pDot1Smtp, svcCause);
        assertElementHasCause(pDot1Icmp, nodeCause);
        assertElementHasCause(pNode1, nodeCause);

        verifyAnticipated();

        // recreate the pollable network from the database
        m_network = createPollableNetwork(m_db, m_scheduler, m_pollerConfig, m_pollerConfig, m_pollContext);
        assignPollableMembers(m_network);

        assertElementHasCause(pDot1Smtp, svcCause);
        assertElementHasCause(pDot1Icmp, nodeCause);
        assertElementHasCause(pNode1, nodeCause);

        assertDown(pDot1Smtp);
        assertDown(pDot1Icmp);
        assertDown(pDot2Smtp);
        assertDown(pDot2Icmp);
        assertDown(pDot1);
        assertDown(pDot2);
        assertDown(pNode1);
        
        anticipateUp(mDot1Smtp);
        anticipateUp(mNode1);
        
        mNode1.bringUp();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();


    }

    @Test
    public void testLoadIndependentOutageEventsUpSeparately() throws Exception {
        anticipateDown(mDot1Smtp);
        
        mDot1Smtp.bringDown();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());

        verifyAnticipated();
        
        anticipateDown(mNode1);

        mNode1.bringDown();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        // recreate the pollable network from the database
        m_network = createPollableNetwork(m_db, m_scheduler, m_pollerConfig, m_pollerConfig, m_pollContext);
        assignPollableMembers(m_network);
        
        assertDown(pDot1Smtp);
        assertDown(pDot1Icmp);
        assertDown(pDot2Smtp);
        assertDown(pDot2Icmp);
        assertDown(pDot1);
        assertDown(pDot2);
        assertDown(pNode1);
        
        anticipateUp(mNode1);
        m_outageAnticipator.deanticipateOutageClosed(mDot1Smtp, mNode1.createUpEvent());
        
        mNode1.bringUp();
        mDot1Smtp.bringDown();
        
        pDot1Icmp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUp(mDot1Smtp);

        mDot1Smtp.bringUp();
        
        pDot1Smtp.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
    }
    
    
    @Test
    public void testLock() throws Exception {
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                m_lockCount++;
                assertEquals(1, m_lockCount);
                try { Thread.sleep(3000); } catch (InterruptedException e) {}
                m_lockCount--;
                assertEquals(0, m_lockCount);
            }
        };
        
        final Runnable locker = new Runnable() {
            @Override
            public void run() {
                pNode1.withTreeLock(r);
            }
        };
        
        Thread[] threads = new Thread[5];
        for(int i = 0; i < 5; i++) {
            threads[i] = new Thread(locker);
            threads[i].start();
        }
        
        for(int i = 0; i < 5; i++) {
            threads[i].join();
        }
        
    }
    
    @Test
    public void testLockTimeout() throws Exception {
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                m_lockCount++;
                assertEquals(1, m_lockCount);
                try { Thread.sleep(5000); } catch (InterruptedException e) {}
                m_lockCount--;
                assertEquals(0, m_lockCount);
            }
        };
        
        final Runnable locker = new Runnable() {
            @Override
            public void run() {
                pNode1.withTreeLock(r);
            }
        };
        
        final Runnable lockerWithTimeout = new Runnable() {
            @Override
            public void run() {
                try {
                    pNode1.withTreeLock(r, 500);
                    fail("Expected LockUnavailable");
                } catch (LockUnavailable e) {
                    MockUtil.println("Received expected exception "+e);
                }
            }
        };
        
        Thread[] threads = new Thread[5];
        threads[0] = new Thread(locker);
        threads[0].start();
        for(int i = 1; i < 5; i++) {
            threads[i] = new Thread(lockerWithTimeout);
            threads[i].start();
        }
        
        for(int i = 0; i < 5; i++) {
            threads[i].join();
        }
        
    }

    /**
     * @param i
     */
    private void assertTime(long time) {
        assertEquals("Unexpected time", time, m_scheduler.getCurrentTime());
    }

    /**
     * 
     */
    private void verifyAnticipated() {
        m_eventMgr.finishProcessingEvents();
        MockEventUtil.printEvents("Missing Anticipated Events: ", m_anticipator.getAnticipatedEvents());
        assertTrue("Expected events not forthcoming", m_anticipator.getAnticipatedEvents().isEmpty());
        MockEventUtil.printEvents("Unanticipated: ", m_anticipator.unanticipatedEvents());
        assertEquals("Received unexpected events", 0, m_anticipator.unanticipatedEvents().size());

        assertEquals("Wrong number of outages opened", m_outageAnticipator.getExpectedOpens(), m_outageAnticipator.getActualOpens());
        assertEquals("Wrong number of outages in outage table", m_outageAnticipator.getExpectedOutages(), m_outageAnticipator.getActualOutages());
        assertTrue("Created outages don't match the expected outages", m_outageAnticipator.checkAnticipated());
        
        resetAnticipated();
    }

    /**
     * 
     */
    private void resetAnticipated() {
        m_anticipator.reset();
        m_outageAnticipator.reset();
    }

    /**
     * @param svc
     */
    private void anticipateUp(MockElement element) {
        Event event = element.createUpEvent();
        m_anticipator.anticipateEvent(event);
        m_outageAnticipator.anticipateOutageClosed(element, event);
    }

    /**
     * @param svc
     */
    private void anticipateDown(MockElement element) {
        Event event = element.createDownEvent();
        m_anticipator.anticipateEvent(event);
        m_outageAnticipator.anticipateOutageOpened(element, event);
    }

    private void anticipateUnresponsive(MockElement element) {
        MockVisitor visitor = new MockVisitorAdapter() {
            @Override
            public void visitService(MockService svc) {
                m_anticipator.anticipateEvent(svc.createUnresponsiveEvent());
            }
        };
        element.visit(visitor);
    }

    private void anticipateResponsive(MockElement element) {
        MockVisitor visitor = new MockVisitorAdapter() {
            @Override
            public void visitService(MockService svc) {
                m_anticipator.anticipateEvent(svc.createResponsiveEvent());
            }
        };
        element.visit(visitor);
    }


    private void assertPoll(MockService svc) {
        assertEquals(1, svc.getPollCount());
        svc.resetPollCount();
    }


    /**
     * @param network
     */
    private void assertNoPoll(MockElement elem) {
        MockVisitor zeroAsserter = new MockVisitorAdapter() {
            @Override
            public void visitService(MockService svc) {
                assertEquals("Unexpected poll count for "+svc, 0, svc.getPollCount());
            }
        };
        elem.visit(zeroAsserter);
    }

    private void assertChanged(PollableElement elem) {
        assertEquals(true, elem.isStatusChanged());
    }

    private void assertUnchanged(PollableElement elem) {
        assertEquals(false, elem.isStatusChanged());
    }

    private void assertUp(PollableElement elem) {
        assertEquals(PollStatus.up(), elem.getStatus());
    }
    private void assertDown(PollableElement elem) {
        assertEquals(PollStatus.down(), elem.getStatus());
    }

    protected Package findPackageForService(PollerConfig pollerConfig, String ipAddr, String serviceName) {
        Enumeration<Package> en = pollerConfig.enumeratePackage();
        Package lastPkg = null;
        while (en.hasMoreElements()) {
            Package pkg = (Package)en.nextElement();
            if (pollableServiceInPackage(pollerConfig, ipAddr, serviceName, pkg))
                lastPkg = pkg;
        }
        return lastPkg;
    }

    private boolean pollableServiceInPackage(PollerConfig pollerConfig, String ipAddr, String serviceName, Package pkg) {
        return (pollerConfig.isServiceInPackageAndEnabled(serviceName, pkg)
                && pollerConfig.isInterfaceInPackage(ipAddr, pkg));
    }

    private InetAddress getInetAddress(String ipAddr) {
        InetAddress addr;
        addr = InetAddressUtils.addr(ipAddr);
        if (addr == null) {
            // in 'real life' I would just log this and contine with the others
            throw new RuntimeException("Error converting "+ipAddr+" to an InetAddress");
        }
        return addr;
    }
    
    private PollableService addServiceToNetwork(final int nodeId, final String nodeLabel, final String ipAddr, final String serviceName) {

        final PollableNode svcNode = m_network.createNodeIfNecessary(nodeId, nodeLabel);
        
        return svcNode.withTreeLock(new Callable<PollableService>() {

            @Override
            public PollableService call() throws Exception {
                PollableService svc = addServiceToNetwork(m_network, nodeId, nodeLabel, ipAddr, serviceName, null, null, null, m_scheduler, m_pollerConfig, m_pollerConfig);
                //svcNode.recalculateStatus();
                //svcNode.processStatusChange(new Date());
                return svc;
            }

        });
    }

    private PollableService addServiceToNetwork(final PollableNetwork pNetwork,
            int nodeId, String nodeLabel, String ipAddr, String serviceName,
            Number svcLostEventId, String svcLostUei,
            Date svcLostTime, final ScheduleTimer scheduler,
            final PollerConfig pollerConfig,
            final PollOutagesConfig pollOutageConfig) {
        InetAddress addr = getInetAddress(ipAddr); 

        Package pkg = findPackageForService(pollerConfig, ipAddr, serviceName);
        if (pkg == null) {
            MockUtil.println("No package for service "+serviceName+" with ipAddr "+ipAddr);
            return null;
        }
        

            
        PollableService svc = pNetwork.createService(nodeId, nodeLabel, addr, serviceName);
        PollableServiceConfig pollConfig = new PollableServiceConfig(svc, pollerConfig, pollOutageConfig, pkg, scheduler);
        svc.setPollConfig(pollConfig);
        synchronized (svc) {
            if (svc.getSchedule() == null) {
                Schedule schedule = new Schedule(svc, pollConfig, scheduler);
                svc.setSchedule(schedule);
            }
        }

        //MockUtil.println("svcLostEventId for "+svc+" is "+svcLostEventId);
        if (svcLostEventId == null) {
            if (svc.getParent().getStatus().isUnknown()) {
                svc.updateStatus(PollStatus.up());
            } else {
                svc.updateStatus(svc.getParent().getStatus());
                svc.setCause(svc.getParent().getCause());
            }
        }
        else {
            svc.updateStatus(PollStatus.down());
            
            PollEvent cause = new DbPollEvent(svcLostEventId.intValue(), svcLostUei, svcLostTime);
            svc.setCause(cause);
        }
        return svc;
    }


}
