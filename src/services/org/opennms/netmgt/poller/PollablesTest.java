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
package org.opennms.netmgt.poller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import junit.framework.TestCase;

import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockElement;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockMonitor;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.mock.MockVisitor;
import org.opennms.netmgt.mock.MockVisitorAdapter;

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
    }
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        m_pollContext = new MockPollContext();
        m_pollContext.setCriticalServiceName("ICMP");
        
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
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
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
        
        final PollableService svc = m_network.getService(1, InetAddress.getByName("192.168.1.1"), "ICMP");
        svc.updateStatus(PollStatus.STATUS_UP);
        m_network.recalculateStatus();
        
        PollableVisitor upChecker = new PollableVisitorAdaptor() {
            public void visitNetwork(PollableNetwork network) {
                assertUp(network);
            }
            public void visitNode(PollableNode node) {
                if (node == svc.getNode())
                    assertUp(node);
                else
                    assertDown(node);
            }
            public void visitInterface(PollableInterface iface) {
                if (iface == svc.getInterface())
                    assertUp(iface);
                else
                    assertDown(iface);
            }
            public void visitService(PollableService s) {
                if (s == svc)
                    assertUp(s);
                else
                    assertDown(s);
            }
        };
        m_network.visit(upChecker);
    }
    
    public void testInterfaceStatus() throws Exception {
        
        
        PollableService svc = m_network.getService(1, InetAddress.getByName("192.168.1.2"), "SMTP");
        svc.updateStatus(PollStatus.STATUS_DOWN);
        m_network.recalculateStatus();
        
        assertDown(svc);
        assertUp(svc.getInterface());
        
        svc.updateStatus(PollStatus.STATUS_UP);
        m_network.recalculateStatus();
        
        assertUp(svc);
        assertUp(svc.getInterface());
        
        PollableService icmp = m_network.getService(1, InetAddress.getByName("192.168.1.2"), "ICMP");
        icmp.updateStatus(PollStatus.STATUS_DOWN);
        m_network.recalculateStatus();
        
        assertDown(icmp);
        assertDown(icmp.getInterface());
        
    }
    
    public void testPollUnresponsive() throws Exception {
        PollableInterface iface = m_network.getInterface(1, InetAddress.getByName("192.168.1.1"));
        PollableService smtpSvc = iface.getService("SMTP");
        PollableService icmpSvc = iface.getService("ICMP");
        smtpSvc.updateStatus(PollStatus.STATUS_UNRESPONSIVE);
        icmpSvc.updateStatus(PollStatus.STATUS_UNRESPONSIVE);
        m_network.recalculateStatus();
        
        assertUp(iface);
        
    }
    
    public void testPollService() throws Exception {
        int nodeId = 1;
        String ipAddr = "192.168.1.1";
        String svcName = "SMTP";
        PollableService pSvc = m_network.getService(nodeId, InetAddress.getByName(ipAddr), svcName);
        MockService mSvc = m_mockNetwork.getService(nodeId, ipAddr, svcName);
        
        pSvc.doPoll();
        assertEquals(PollStatus.STATUS_UP, pSvc.getStatus());
        assertEquals(false, pSvc.isStatusChanged());
        
        mSvc.bringDown();
        
        pSvc.doPoll();
        assertEquals(PollStatus.STATUS_DOWN, pSvc.getStatus());
        assertEquals(true, pSvc.isStatusChanged());
        pSvc.resetStatusChanged();
        
        mSvc.bringUp();
        
        pSvc.doPoll();
        assertEquals(PollStatus.STATUS_UP, pSvc.getStatus());
        assertEquals(true, pSvc.isStatusChanged());
        pSvc.recalculateStatus();
    }
    
    public void testPollAllUp() throws Exception {
        m_pollContext.setNodeProcessingEnabled(true);
        m_pollContext.setPollingAllIfCritServiceUndefined(true);

        int nodeId = 1;
        String ipAddr = "192.168.1.1";
        String svcName = "ICMP";
        
        MockService mSvc = m_mockNetwork.getService(nodeId, ipAddr, svcName);
        PollableInterface iface = m_network.getInterface(nodeId, InetAddress.getByName(ipAddr));
        PollableService pSvc = iface.getService(svcName);
        
        
        pSvc.doPoll();
        assertUp(pSvc);
        assertUp(iface);
        assertEquals(false, pSvc.isStatusChanged());
        assertEquals(false, iface.isStatusChanged());

        assertEquals(1, mSvc.getPollCount());
        mSvc.resetPollCount();
        
        assertPollCountsZero(m_mockNetwork);
        
    }
    
    public void testPollIfUpNonCritSvcDown() throws Exception {
        m_pollContext.setNodeProcessingEnabled(true);
        m_pollContext.setPollingAllIfCritServiceUndefined(true);

        int nodeId = 1;
        String ipAddr = "192.168.1.1";
        String svcName = "SMTP";
        MockService mSvc = m_mockNetwork.getService(nodeId, ipAddr, svcName);
        MockService mCritSvc = m_mockNetwork.getService(nodeId, ipAddr, "ICMP");
        PollableInterface iface = m_network.getInterface(nodeId, InetAddress.getByName(ipAddr));
        PollableService pSvc = iface.getService(svcName);

        mSvc.bringDown();
        
        pSvc.doPoll();
        assertDown(pSvc);
        assertUp(iface);
        assertEquals(true, pSvc.isStatusChanged());
        assertEquals(false, iface.isStatusChanged());

        assertEquals(1, mSvc.getPollCount());
        mSvc.resetPollCount();
        
        assertEquals(1, mCritSvc.getPollCount());
        mCritSvc.resetPollCount();
        
        assertPollCountsZero(m_mockNetwork);
        
        
    }
    
    public void testPollIfUpCritSvcDownPoll() throws Exception {
        m_pollContext.setNodeProcessingEnabled(true);
        m_pollContext.setPollingAllIfCritServiceUndefined(true);

        int nodeId = 1;
        String ipAddr = "192.168.1.1";
        MockService mSvc = m_mockNetwork.getService(nodeId, ipAddr, "SMTP");
        MockService mCritSvc = m_mockNetwork.getService(nodeId, ipAddr, "ICMP");
        PollableInterface iface = m_network.getInterface(nodeId, InetAddress.getByName(ipAddr));
        PollableService pSvc = iface.getService("SMTP");
        PollableService pCritSvc = iface.getService("ICMP");
        
        mCritSvc.bringDown();
        
        pCritSvc.doPoll();
        
        assertDown(pCritSvc);
        assertDown(iface);
        assertEquals(true, pCritSvc.isStatusChanged());
        assertEquals(true, iface.isStatusChanged());

        assertEquals(1, mCritSvc.getPollCount());
        mCritSvc.resetPollCount();
        
        MockService otherIfCritSvc = m_mockNetwork.getService(1, "192.168.1.2", "ICMP");
        assertEquals(1, otherIfCritSvc.getPollCount());
        otherIfCritSvc.resetPollCount();
        
        assertPollCountsZero(m_mockNetwork);

    }
    
    
    public void testPollIfDownNonCritSvcUp() throws Exception {
        m_pollContext.setNodeProcessingEnabled(true);
        m_pollContext.setPollingAllIfCritServiceUndefined(true);

        int nodeId = 1;
        String ipAddr = "192.168.1.1";
        MockInterface mockIface = m_mockNetwork.getInterface(nodeId, ipAddr);
        MockService mSvc = m_mockNetwork.getService(nodeId, ipAddr, "SMTP");
        MockService mCritSvc = m_mockNetwork.getService(nodeId, ipAddr, "ICMP");
        PollableInterface iface = m_network.getInterface(nodeId, InetAddress.getByName(ipAddr));
        PollableService pSvc = iface.getService("SMTP");
        PollableService pCritSvc = iface.getService("ICMP");
        
        mockIface.bringDown();

        iface.updateStatus(PollStatus.STATUS_DOWN);
        pCritSvc.updateStatus(PollStatus.STATUS_DOWN);
        
        m_network.recalculateStatus();
        m_network.resetStatusChanged();

        assertDown(pCritSvc);
        assertDown(iface);
        
        
        mSvc.bringUp();
        
        pSvc.doPoll();
        
        assertDown(pCritSvc);
        assertDown(iface);
        assertEquals(false, pCritSvc.isStatusChanged());
        assertEquals(false, iface.isStatusChanged());

        // this is not the critical service so I expect nothing to be polled
        assertPollCountsZero(m_mockNetwork);

    }

    public void testPollIfDownCritSvcUp() throws Exception {
        m_pollContext.setNodeProcessingEnabled(true);
        m_pollContext.setPollingAllIfCritServiceUndefined(true);

        int nodeId = 1;
        String ipAddr = "192.168.1.1";
        MockInterface mockIface = m_mockNetwork.getInterface(nodeId, ipAddr);
        MockService mSvc = m_mockNetwork.getService(nodeId, ipAddr, "SMTP");
        MockService mCritSvc = m_mockNetwork.getService(nodeId, ipAddr, "ICMP");
        PollableInterface iface = m_network.getInterface(nodeId, InetAddress.getByName(ipAddr));
        PollableService pSvc = iface.getService("SMTP");
        PollableService pCritSvc = iface.getService("ICMP");
        
        mockIface.bringDown();

        iface.updateStatus(PollStatus.STATUS_DOWN);
        pCritSvc.updateStatus(PollStatus.STATUS_DOWN);
        
        m_network.recalculateStatus();
        m_network.resetStatusChanged();

        assertDown(pCritSvc);
        assertDown(iface);
        
        
        mCritSvc.bringUp();
        
        pCritSvc.doPoll();
        
        assertDown(pSvc);
        assertUp(pCritSvc);
        assertUp(iface);
        
        assertEquals(true, pCritSvc.isStatusChanged());
        assertEquals(true, iface.isStatusChanged());
        
        assertEquals(1, mSvc.getPollCount());
        mSvc.resetPollCount();
        
        assertEquals(1, mCritSvc.getPollCount());
        mCritSvc.resetPollCount();

        // this is not the critical service so I expect nothing to be polled
        assertPollCountsZero(m_mockNetwork);

    }

    public void testPollIfUpCritSvcUndefSvcDown() throws Exception {
        m_pollContext.setCriticalServiceName(null);
        m_pollContext.setNodeProcessingEnabled(true);
        m_pollContext.setPollingAllIfCritServiceUndefined(true);

        int nodeId = 1;
        String ipAddr = "192.168.1.1";
        MockInterface mockIface = m_mockNetwork.getInterface(nodeId, ipAddr);
        MockService mSvc = m_mockNetwork.getService(nodeId, ipAddr, "SMTP");
        MockService mCritSvc = m_mockNetwork.getService(nodeId, ipAddr, "ICMP");
        PollableInterface iface = m_network.getInterface(nodeId, InetAddress.getByName(ipAddr));
        PollableService pSvc = iface.getService("SMTP");
        PollableService pCritSvc = iface.getService("ICMP");
        
        
        mSvc.bringDown();
        
        pSvc.doPoll();
        
        assertDown(pSvc);
        assertUp(iface);
        assertEquals(true, pSvc.isStatusChanged());
        assertEquals(false, iface.isStatusChanged());

        assertEquals(1, mSvc.getPollCount());
        mSvc.resetPollCount();
        
        assertEquals(1, mCritSvc.getPollCount());
        mCritSvc.resetPollCount();
        
        // this is not the critical service so I expect nothing to be polled
        assertPollCountsZero(m_mockNetwork);

    }

    public void testPollIfDownCritSvcUndefSvcDown() throws Exception {
        m_pollContext.setCriticalServiceName(null);
        m_pollContext.setNodeProcessingEnabled(true);
        m_pollContext.setPollingAllIfCritServiceUndefined(true);

        int nodeId = 1;
        String ipAddr = "192.168.1.1";
        MockInterface mockIface = m_mockNetwork.getInterface(nodeId, ipAddr);
        MockService mSvc = m_mockNetwork.getService(nodeId, ipAddr, "SMTP");
        MockService mCritSvc = m_mockNetwork.getService(nodeId, ipAddr, "ICMP");
        PollableInterface iface = m_network.getInterface(nodeId, InetAddress.getByName(ipAddr));
        PollableService pSvc = iface.getService("SMTP");
        PollableService pCritSvc = iface.getService("ICMP");
        
        mockIface.bringDown();

        iface.updateStatus(PollStatus.STATUS_DOWN);
        pCritSvc.updateStatus(PollStatus.STATUS_DOWN);
        pSvc.updateStatus(PollStatus.STATUS_DOWN);
        
        m_network.recalculateStatus();
        m_network.resetStatusChanged();

        assertDown(pSvc);
        assertDown(pCritSvc);
        assertDown(iface);
        
        mSvc.bringUp();
        
        pSvc.doPoll();
        
        assertUp(pSvc);
        assertDown(pCritSvc);
        assertUp(iface);
        assertEquals(true, pSvc.isStatusChanged());
        assertEquals(false, pCritSvc.isStatusChanged());
        assertEquals(true, iface.isStatusChanged());

        assertEquals(1, mSvc.getPollCount());
        mSvc.resetPollCount();
        
        assertEquals(1, mCritSvc.getPollCount());
        mCritSvc.resetPollCount();
        
        // this is not the critical service so I expect nothing to be polled
        assertPollCountsZero(m_mockNetwork);

    }

    public void testPollIfUpCritSvcUndefSvcDownNoPoll() throws Exception {
        m_pollContext.setCriticalServiceName(null);
        m_pollContext.setNodeProcessingEnabled(true);
        m_pollContext.setPollingAllIfCritServiceUndefined(false);

        int nodeId = 1;
        String ipAddr = "192.168.1.1";
        MockInterface mockIface = m_mockNetwork.getInterface(nodeId, ipAddr);
        MockService mSvc = m_mockNetwork.getService(nodeId, ipAddr, "SMTP");
        MockService mCritSvc = m_mockNetwork.getService(nodeId, ipAddr, "ICMP");
        PollableInterface iface = m_network.getInterface(nodeId, InetAddress.getByName(ipAddr));
        PollableService pSvc = iface.getService("SMTP");
        PollableService pCritSvc = iface.getService("ICMP");
        
        
        mSvc.bringDown();
        
        pSvc.doPoll();
        
        assertDown(pSvc);
        assertUp(iface);
        assertEquals(true, pSvc.isStatusChanged());
        assertEquals(false, iface.isStatusChanged());

        assertEquals(1, mSvc.getPollCount());
        mSvc.resetPollCount();
        
        // this is not the critical service so I expect nothing to be polled
        assertPollCountsZero(m_mockNetwork);

    }

    public void testPollIfDownCritSvcUndefSvcDownNoPoll() throws Exception {
        m_pollContext.setCriticalServiceName(null);
        m_pollContext.setNodeProcessingEnabled(true);
        m_pollContext.setPollingAllIfCritServiceUndefined(false);

        int nodeId = 1;
        String ipAddr = "192.168.1.1";
        MockInterface mockIface = m_mockNetwork.getInterface(nodeId, ipAddr);
        MockService mSvc = m_mockNetwork.getService(nodeId, ipAddr, "SMTP");
        MockService mCritSvc = m_mockNetwork.getService(nodeId, ipAddr, "ICMP");
        PollableInterface iface = m_network.getInterface(nodeId, InetAddress.getByName(ipAddr));
        PollableService pSvc = iface.getService("SMTP");
        PollableService pCritSvc = iface.getService("ICMP");
        
        mockIface.bringDown();

        iface.updateStatus(PollStatus.STATUS_DOWN);
        pCritSvc.updateStatus(PollStatus.STATUS_DOWN);
        pSvc.updateStatus(PollStatus.STATUS_DOWN);
        
        m_network.recalculateStatus();
        m_network.resetStatusChanged();

        assertDown(pSvc);
        assertDown(pCritSvc);
        assertDown(iface);
        
        mSvc.bringUp();
        
        pSvc.doPoll();
        
        assertUp(pSvc);
        assertDown(pCritSvc);
        assertUp(iface);
        assertEquals(true, pSvc.isStatusChanged());
        assertEquals(false, pCritSvc.isStatusChanged());
        assertEquals(true, iface.isStatusChanged());

        assertEquals(1, mSvc.getPollCount());
        mSvc.resetPollCount();
        
        // this is not the critical service so I expect nothing to be polled
        assertPollCountsZero(m_mockNetwork);

    }

    /**
     * @param network
     */
    private void assertPollCountsZero(MockElement elem) {
        MockVisitor zeroAsserter = new MockVisitorAdapter() {
            public void visitService(MockService svc) {
                assertEquals("Unexpected poll count for "+svc, 0, svc.getPollCount());
                svc.resetPollCount();
            }
        };
        elem.visit(zeroAsserter);
    }

    public void testPollInterface() throws Exception {
        m_pollContext.setNodeProcessingEnabled(true);
        m_pollContext.setPollingAllIfCritServiceUndefined(true);

        int nodeId = 1;
        String ipAddr = "192.168.1.1";
        String svcName = "ICMP";
        PollableService pSvc = m_network.getService(nodeId, InetAddress.getByName(ipAddr), svcName);
        PollableInterface iface = pSvc.getInterface();
        
        MockService mSvc = m_mockNetwork.getService(nodeId, ipAddr, svcName);
        MockService mSmtpSvc = m_mockNetwork.getService(nodeId, ipAddr, "SMTP");
        
//        MockUtil.println("--- Bring SMTP Down ----");
//        mSmtpSvc.bringDown();
        
        MockUtil.println("---- Poll ICMP Up ----");
        pSvc.doPoll();
        assertUp(pSvc);
        assertUp(iface);
        assertEquals(false, pSvc.isStatusChanged());
        assertEquals(false, iface.isStatusChanged());
        
        mSvc.bringDown();

        MockUtil.println("---- Poll ICMP Down ----");
        pSvc.doPoll();
        assertDown(pSvc);
        assertDown(iface);
        assertEquals(true, pSvc.isStatusChanged());
        assertEquals(true, iface.isStatusChanged());
        m_network.resetStatusChanged();
        
        mSvc.bringUp();
        
        MockUtil.println("---- Poll ICMP Up ----");
        pSvc.doPoll();
        assertUp(pSvc);
        assertUp(iface);
        assertEquals(true, pSvc.isStatusChanged());
        assertEquals(true, iface.isStatusChanged());
        m_network.resetStatusChanged();

    }
    
    public void testFindMemberWithDescendent() throws Exception {
        int nodeId = 1;
        String ipAddr = "192.168.1.1";
        String svcName = "ICMP";
        PollableService pSvc = m_network.getService(nodeId, InetAddress.getByName(ipAddr), svcName);
        PollableInterface iface = pSvc.getInterface();
        PollableNode node = pSvc.getNode();
        
        assertSame(node, m_network.findMemberWithDescendent(pSvc));
        assertSame(iface, node.findMemberWithDescendent(pSvc));
        assertSame(pSvc, iface.findMemberWithDescendent(pSvc));
        
        assertNull(m_network.getNode(2).findMemberWithDescendent(pSvc));
        
        
    }
    
    private void assertUp(PollableElement elem) {
        assertEquals(PollStatus.STATUS_UP, elem.getStatus());
    }
    private void assertDown(PollableElement elem) {
        assertEquals(PollStatus.STATUS_DOWN, elem.getStatus());
    }

}
