//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.poller.pollables;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;

import junit.framework.TestCase;

import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockElement;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockMonitor;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.mock.MockVisitor;
import org.opennms.netmgt.mock.MockVisitorAdapter;
import org.opennms.netmgt.mock.OutageAnticipator;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollablesTest 
 *
 * @author brozow
 */
public class PollablesTest extends TestCase {

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
    private MockService mDot2Smtp;
    private MockService mDot2Icmp;
    private MockInterface mDot3;
    private MockService mDot3Http;
    private MockService mDot3Icmp;
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
    private OutageAnticipator m_outageAnticipator;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PollablesTest.class);
    }

    private class MockPollConfig implements PollConfig {
        String m_svcName;
        MockMonitor m_monitor;
        Properties m_properties = new Properties();
        Package m_package;
        
        public MockPollConfig(MockNetwork network, String svcName) {
            m_svcName = svcName;
            m_monitor = new MockMonitor(network, svcName);
            m_package = new Package();
            m_package.setName("Fake");
        }

        public PollStatus poll(PollableService svc) {
            assertEquals(m_svcName, svc.getSvcName());
            return PollStatus.getPollStatus(m_monitor.poll(svc.getNetInterface(), m_properties, m_package));
        }
    }
    private class MockPollContext implements PollContext {
        private String m_critSvcName;
        private boolean m_nodeProcessingEnabled;
        private boolean m_pollingAllIfCritServiceUndefined;
        private boolean m_serviceUnresponsiveEnabled;

        public String getCriticalServiceName() {
            return m_critSvcName;
        }
        
        public void setCriticalServiceName(String svcName) {
            m_critSvcName = svcName;
        }
        
        public boolean isNodeProcessingEnabled() {
            return m_nodeProcessingEnabled;
        }
        public void setNodeProcessingEnabled(boolean nodeProcessingEnabled) {
            m_nodeProcessingEnabled = nodeProcessingEnabled;
        }
        public boolean isPollingAllIfCritServiceUndefined() {
            return m_pollingAllIfCritServiceUndefined;
        }
        public void setPollingAllIfCritServiceUndefined(boolean pollingAllIfCritServiceUndefined) {
            m_pollingAllIfCritServiceUndefined = pollingAllIfCritServiceUndefined;
        }
        public Event sendEvent(Event event) {
            m_eventMgr.sendNow(event);
            return event;
        }

        public Event createEvent(String uei, int nodeId, InetAddress address, String svcName, Date date) {
            return MockUtil.createEvent("Test", uei, nodeId, (address == null ? null : address.getHostAddress()), svcName);
        }
        public void openOutage(PollableService pSvc, Event svcLostEvent) {
            MockService mSvc = m_mockNetwork.getService(pSvc.getNodeId(), pSvc.getIpAddr(), pSvc.getSvcName());
            MockUtil.println("Opening Outage for "+mSvc);
            m_db.createOutage(mSvc, svcLostEvent);

        }
        public void resolveOutage(PollableService pSvc, Event svcRegainEvent) {
            MockService mSvc = m_mockNetwork.getService(pSvc.getNodeId(), pSvc.getIpAddr(), pSvc.getSvcName());
            MockUtil.println("Resolving Outage for "+mSvc);
            m_db.resolveOutage(mSvc, svcRegainEvent);
        }
        public boolean isServiceUnresponsiveEnabled() {
            return m_serviceUnresponsiveEnabled;
        }
        public void setServiceUnresponsiveEnabled(boolean serviceUnresponsiveEnabled) {
            m_serviceUnresponsiveEnabled = serviceUnresponsiveEnabled;
        }
    }
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        m_pollContext = new MockPollContext();
        m_pollContext.setCriticalServiceName("ICMP");
        m_pollContext.setNodeProcessingEnabled(true);
        m_pollContext.setPollingAllIfCritServiceUndefined(true);
        m_pollContext.setServiceUnresponsiveEnabled(true);

        
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
        m_mockNetwork.addService("HTTP");

        m_db = new MockDatabase();
        m_db.populate(m_mockNetwork);
        
        m_anticipator = new EventAnticipator();
        m_outageAnticipator = new OutageAnticipator(m_db);

        
        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventWriter(m_db);
        m_eventMgr.setEventAnticipator(m_anticipator);
        m_eventMgr.addEventListener(m_outageAnticipator);

        m_network = new PollableNetwork(m_pollContext);
        m_network.createService(1, InetAddress.getByName("192.168.1.1"), "ICMP");
        m_network.createService(1, InetAddress.getByName("192.168.1.1"), "SMTP");
        m_network.createService(1, InetAddress.getByName("192.168.1.2"), "ICMP");
        m_network.createService(1, InetAddress.getByName("192.168.1.2"), "SMTP");
        m_network.createService(2, InetAddress.getByName("192.168.1.3"), "ICMP");
        m_network.createService(2, InetAddress.getByName("192.168.1.3"), "HTTP");
        m_network.createService(3, InetAddress.getByName("192.168.1.4"), "SMTP");
        m_network.createService(3, InetAddress.getByName("192.168.1.4"), "HTTP");
        m_network.createService(3, InetAddress.getByName("192.168.1.5"), "SMTP");
        m_network.createService(3, InetAddress.getByName("192.168.1.5"), "HTTP");
        
        PollableVisitor upper = new PollableVisitorAdaptor() {
            public void visitElement(PollableElement e) {
                e.updateStatus(PollStatus.STATUS_UP);  
            }
            public void visitService(PollableService svc) {
                MockPollConfig pollConfig = new MockPollConfig(m_mockNetwork, svc.getSvcName());
                svc.setPollConfig(pollConfig);
            }
        };
        m_network.visit(upper);
        m_network.resetStatusChanged();
        
        mNode1 = m_mockNetwork.getNode(1);
        mDot1 = mNode1.getInterface("192.168.1.1");
        mDot1Smtp = mDot1.getService("SMTP");
        mDot1Icmp = mDot1.getService("ICMP");
        mDot2 = mNode1.getInterface("192.168.1.2");
        mDot2Smtp = mDot2.getService("SMTP");
        mDot2Icmp = mDot2.getService("ICMP");
        
        mNode2 = m_mockNetwork.getNode(2);
        mDot3 = mNode2.getInterface("192.168.1.3");
        mDot3Http = mDot3.getService("HTTP");
        mDot3Icmp = mDot3.getService("ICMP");
        
        pNode1 = m_network.getNode(1);
        pDot1 = pNode1.getInterface(InetAddress.getByName("192.168.1.1"));
        pDot1Smtp = pDot1.getService("SMTP");
        pDot1Icmp = pDot1.getService("ICMP");
        pDot2 = pNode1.getInterface(InetAddress.getByName("192.168.1.2"));
        pDot2Smtp = pDot2.getService("SMTP");
        pDot2Icmp = pDot2.getService("ICMP");
        
        pNode2 = m_network.getNode(2);
        pDot3 = pNode2.getInterface(InetAddress.getByName("192.168.1.3"));
        pDot3Http = pDot3.getService("HTTP");
        pDot3Icmp = pDot3.getService("ICMP");
        
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        m_db.drop();
    }
    
    public void testCreateNode() {
        int nodeId = 99;
        PollableNode node = m_network.createNode(nodeId);
        assertNotNull("node is null", node);

        assertEquals(99, node.getNodeId());
        assertEquals(node, m_network.getNode(nodeId));
        
        assertEquals(m_network, node.getNetwork());
    }
    
    public void testCreateInterface() throws UnknownHostException {
        int nodeId = 99;
        InetAddress addr = InetAddress.getByName("192.168.1.99");

        PollableInterface iface = m_network.createInterface(nodeId, addr);
        assertNotNull("iface is null", iface);
        assertEquals(addr, iface.getAddress());
        assertEquals(nodeId, iface.getNodeId());
        assertEquals(iface, m_network.getInterface(nodeId, addr));

        PollableNode node = iface.getNode();
        assertNotNull("node is null", node);
        assertEquals(nodeId, node.getNodeId());
        assertEquals(node, m_network.getNode(nodeId));
        
        assertEquals(m_network, iface.getNetwork());
    }
    
    public void testCreateService() throws Exception {
        int nodeId = 99;
        InetAddress addr = InetAddress.getByName("192.168.1.99");
        String svcName = "HTTP-99";
        
        PollableService svc = m_network.createService(nodeId, addr, svcName);
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
        assertEquals(node, m_network.getNode(nodeId));
        
        assertEquals(m_network, svc.getNetwork());
        
    }
    
    public void testVisit() {
        class Counter extends PollableVisitorAdaptor {
            public int svcCount;
            public int ifCount;
            public int nodeCount;
            public int elementCount;
            public int containerCount;
            public int networkCount;
            public void visitService(PollableService s) {
                svcCount++;
            }
            public void visitInterface(PollableInterface iface) {
                ifCount++;
            }
            public void visitNode(PollableNode node) {
                nodeCount++;
            }
            public void visitElement(PollableElement element) {
                elementCount++;
            }
            public void visitContainer(PollableContainer container) {
                containerCount++;
            }
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
    
    public void testStatus() throws Exception {
        PollableVisitor updater = new PollableVisitorAdaptor() {
            public void visitElement(PollableElement e) {
                e.updateStatus(PollStatus.STATUS_DOWN);
            }
        };
        m_network.visit(updater);
        PollableVisitor downChecker = new PollableVisitorAdaptor() {
            public void visitElement(PollableElement e) {
                assertEquals(PollStatus.STATUS_DOWN, e.getStatus());
                assertEquals(true, e.isStatusChanged());
                assertFalse(0L == e.getStatusChangeTime());
            }
        };
        m_network.visit(downChecker);
        m_network.resetStatusChanged();
        PollableVisitor statusChangedChecker = new PollableVisitorAdaptor() {
            public void visitElement(PollableElement e) {
                assertEquals(false, e.isStatusChanged());
            }
        };
        m_network.visit(statusChangedChecker);
        
        pDot1Icmp.updateStatus(PollStatus.STATUS_UP);
        m_network.recalculateStatus();
        
        PollableVisitor upChecker = new PollableVisitorAdaptor() {
            public void visitNetwork(PollableNetwork network) {
                assertUp(network);
            }
            public void visitNode(PollableNode node) {
                if (node == pDot1Icmp.getNode())
                    assertUp(node);
                else
                    assertDown(node);
            }
            public void visitInterface(PollableInterface iface) {
                if (iface == pDot1Icmp.getInterface())
                    assertUp(iface);
                else
                    assertDown(iface);
            }
            public void visitService(PollableService s) {
                if (s == pDot1Icmp)
                    assertUp(s);
                else
                    assertDown(s);
            }
        };
        m_network.visit(upChecker);
    }
    
    public void testInterfaceStatus() throws Exception {
        
        
        pDot2Smtp.updateStatus(PollStatus.STATUS_DOWN);
        m_network.recalculateStatus();
        
        assertDown(pDot2Smtp);
        assertUp(pDot2Smtp.getInterface());
        
        pDot2Smtp.updateStatus(PollStatus.STATUS_UP);
        m_network.recalculateStatus();
        
        assertUp(pDot2Smtp);
        assertUp(pDot2Smtp.getInterface());
        
        pDot2Icmp.updateStatus(PollStatus.STATUS_DOWN);
        m_network.recalculateStatus();
        
        assertDown(pDot2Icmp);
        assertDown(pDot2Icmp.getInterface());
        
    }

    public void testFindMemberWithDescendent() throws Exception {

        assertSame(pNode1, m_network.findMemberWithDescendent(pDot1Icmp));
        assertSame(pDot1, pNode1.findMemberWithDescendent(pDot1Icmp));
        assertSame(pDot1Icmp, pDot1.findMemberWithDescendent(pDot1Icmp));
        
        // pDot1Icmp is not a descendent of pNode2
        assertNull(pNode2.findMemberWithDescendent(pDot1Icmp));
        
        
    }
    

    public void testPropagateUnresponsive() throws Exception {

        pDot1Smtp.updateStatus(PollStatus.STATUS_UNRESPONSIVE);
        pDot1Icmp.updateStatus(PollStatus.STATUS_UNRESPONSIVE);
        m_network.recalculateStatus();
        
        assertUp(pDot1);
        
    }
    
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
    
    public void testPollAllUp() throws Exception {

        pDot1Icmp.doPoll();
        assertUp(pDot1Icmp);
        assertUp(pDot1);
        assertUnchanged(pDot1Icmp);
        assertUnchanged(pDot1);

        assertPoll(mDot1Icmp);
        assertNoPoll(m_mockNetwork);
        
    }
    
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
    
    
    public void testPollIfDownNonCritSvcUp() throws Exception {

        mDot1.bringDown();

        pDot1.updateStatus(PollStatus.STATUS_DOWN);
        pDot1Icmp.updateStatus(PollStatus.STATUS_DOWN);
        
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

    public void testPollIfDownCritSvcUp() throws Exception {

        mDot1.bringDown();

        pDot1.updateStatus(PollStatus.STATUS_DOWN);
        pDot1Icmp.updateStatus(PollStatus.STATUS_DOWN);
        
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

    public void testPollIfDownCritSvcUndefSvcDown() throws Exception {
        m_pollContext.setCriticalServiceName(null);

        mDot1.bringDown();

        pDot1.updateStatus(PollStatus.STATUS_DOWN);
        pDot1Icmp.updateStatus(PollStatus.STATUS_DOWN);
        pDot1Smtp.updateStatus(PollStatus.STATUS_DOWN);
        
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

    public void testPollIfDownCritSvcUndefSvcDownNoPoll() throws Exception {
        m_pollContext.setCriticalServiceName(null);
        m_pollContext.setPollingAllIfCritServiceUndefined(false);

        mDot1.bringDown();

        pDot1.updateStatus(PollStatus.STATUS_DOWN);
        pDot1Icmp.updateStatus(PollStatus.STATUS_DOWN);
        pDot1Smtp.updateStatus(PollStatus.STATUS_DOWN);
        
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
    
    public void testServiceEvent() throws Exception {
        MockService mSvc = mDot1Smtp;
        PollableService pSvc = pDot1Smtp;
        
        anticipateDown(mSvc);

        mSvc.bringDown();
        
        pSvc.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUp(mSvc);
        
        mSvc.bringUp();
        
        pSvc.doPoll();
        
        m_network.processStatusChange(new Date());
        
        verifyAnticipated();
        
    }

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
    
    public void testIfOutage() {
        anticipateDown(mDot1);
        
        mDot1.bringDown();
        
        pDot1Smtp.doPoll();
        
        pDot1.processStatusChange(new Date());
        
        verifyAnticipated();
        
        anticipateUp(mDot1);
        
        mDot1.bringUp();
        
        pDot1Icmp.doPoll();
        
        pDot1.processStatusChange(new Date());
        
        verifyAnticipated();
    }
    
    public void testCause() {
        anticipateDown(mDot1Smtp);
        
        mDot1Smtp.bringDown();
        
        pDot1Smtp.doPoll();
        
        pDot1.processStatusChange(new Date());

        anticipateDown(mDot1);
        
        mDot1.bringDown();
        
        pDot1Icmp.doPoll();
        
        pDot1.processStatusChange(new Date());
        
        verifyAnticipated();
        
        PollEvent cause = pDot1.getCause();
        assertNotNull(cause);
        
        assertEquals(cause, pDot1.getCause());
        assertEquals(cause, pDot1Icmp.getCause());
        assertFalse(cause.equals(pDot1Smtp.getCause()));
        
    }
    
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
    
    

    /**
     * 
     */
    private void verifyAnticipated() {
        MockUtil.printEvents("Missing Anticipated Events: ", m_anticipator.getAnticipatedEvents());
        assertTrue("Expected events not forthcoming", m_anticipator.getAnticipatedEvents().isEmpty());
        MockUtil.printEvents("Unanticipated: ", m_anticipator.unanticipatedEvents());
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
            public void visitService(MockService svc) {
                m_anticipator.anticipateEvent(svc.createUnresponsiveEvent());
            }
        };
        element.visit(visitor);
    }

    private void anticipateResponsive(MockElement element) {
        MockVisitor visitor = new MockVisitorAdapter() {
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
        assertEquals(PollStatus.STATUS_UP, elem.getStatus());
    }
    private void assertDown(PollableElement elem) {
        assertEquals(PollStatus.STATUS_DOWN, elem.getStatus());
    }

}
