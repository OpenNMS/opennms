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
//    OpenNMS Licensing       <license@opennms.org>
//    http://www.opennms.org/
//    http://www.opennms.com/
//
package org.opennms.netmgt.poller;

import junit.framework.TestCase;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockElement;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockPollerConfig;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.mock.MockVisitor;
import org.opennms.netmgt.mock.MockVisitorAdapter;
import org.opennms.netmgt.mock.PollAnticipator;
import org.opennms.netmgt.xml.event.Event;

public class PollerTest extends TestCase {

    static boolean logSetup = false;

    MockNetwork m_network;
    
    MockDatabase m_db;

    Poller m_poller;

    private MockPollerConfig m_pollerConfig;

    private MockEventIpcManager m_eventMgr;

    private void anticipateInterfaceStatusChanged(MockElement element, final EventAnticipator anticipator, final int newStatus) {
        final String uei = (newStatus == ServiceMonitor.SERVICE_AVAILABLE ? EventConstants.INTERFACE_UP_EVENT_UEI : EventConstants.INTERFACE_DOWN_EVENT_UEI);
        MockVisitor eventCreator = new MockVisitorAdapter() {
            public void visitInterface(MockInterface iface) {
                if (iface.getPollStatus() != newStatus) {
                    Event event = MockUtil.createInterfaceEvent("Test", uei, iface);
                    anticipator.anticipateEvent(event);
                }
            }
        };
        element.visit(eventCreator);
    }

    private void anticipateNodeStatusChanged(MockElement element, final EventAnticipator anticipator, final int newStatus) {
        final String uei = (newStatus == ServiceMonitor.SERVICE_AVAILABLE ? EventConstants.NODE_UP_EVENT_UEI : EventConstants.NODE_DOWN_EVENT_UEI);
        MockVisitor eventCreator = new MockVisitorAdapter() {
            public void visitNode(MockNode node) {
                if (node.getPollStatus() != newStatus) {
                    Event event = MockUtil.createNodeEvent("Test", uei, node);
                    anticipator.anticipateEvent(event);
                }
            }
        };
        element.visit(eventCreator);

    }

    private void anticipateSvcStatusChanged(MockElement element, final EventAnticipator anticipator, final int newStatus) {
        final String uei = (newStatus == ServiceMonitor.SERVICE_AVAILABLE ? EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI : EventConstants.NODE_LOST_SERVICE_EVENT_UEI);
        MockVisitor eventCreator = new MockVisitorAdapter() {
            public void visitService(MockService svc) {
                if (svc.getPollStatus() != newStatus) {
                    Event event = MockUtil.createServiceEvent("Test", uei, svc);
                    anticipator.anticipateEvent(event);
                }
            }
        };
        element.visit(eventCreator);
    }

    private void bringDownCritSvcs(MockElement element) {
        MockVisitor markCritSvcDown = new MockVisitorAdapter() {
            public void visitService(MockService svc) {
                if ("ICMP".equals(svc.getName())) {
                    svc.bringDown();
                }
            }
        };
        element.visit(markCritSvcDown);

    }

    public void setUp() {
        MockUtil.setupLogging();
        MockUtil.resetLogLevel();

        m_network = new MockNetwork();
        m_network.setCriticalService("ICMP");
        m_network.addNode(1, "Router");
        m_network.addInterface("192.168.1.1");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addInterface("192.168.1.2");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addNode(2, "Server");
        m_network.addInterface("192.168.1.3");
        m_network.addService("ICMP");
        m_network.addService("HTTP");
        
        m_db = new MockDatabase();
        m_db.populate(m_network);
        
        m_pollerConfig = new MockPollerConfig();
        m_pollerConfig.setNodeOutageProcessingEnabled(true);
        m_pollerConfig.setCriticalService("ICMP");
        m_pollerConfig.addPackage("TestPackage");
        m_pollerConfig.addDowntime(1000L, 0L, -1L, false);
        m_pollerConfig.setDefaultPollInterval(1000L);
        m_pollerConfig.populatePackage(m_network);
        
        m_eventMgr = new MockEventIpcManager();
        

        m_poller = new Poller();
        m_poller.setEventManager(m_eventMgr);
        m_poller.setDbConnectionFactory(m_db);
        m_poller.setPollerConfig(m_pollerConfig);
        m_poller.setPollOutagesConfig(m_pollerConfig);

    }

    public void tearDown() {
        assertTrue(MockUtil.noWarningsOrHigherLogged());
        m_db.drop();
    }

    public void testBug709() {

        m_pollerConfig.setNodeOutageProcessingEnabled(true);

        EventAnticipator anticipator = m_eventMgr.getEventAnticipator();
        MockNode node = m_network.getNode(2);
        MockService icmpService = m_network.getService(2, "192.168.1.3", "ICMP");
        MockService httpService = m_network.getService(2, "192.168.1.3", "HTTP");

        // start the poller
        m_poller.init();
        m_poller.start();

        //
        // Bring Down the HTTP service and expect nodeLostService Event
        //

        // expect node lost service for HTTP
        anticipator.reset();
        anticipateSvcStatusChanged(httpService, anticipator, ServiceMonitor.SERVICE_UNAVAILABLE);

        // bring down the HTTP service
        httpService.bringDown();

        // make sure the down events are received
        assertEquals(0, anticipator.waitForAnticipated(10000).size());
        assertEquals(0, anticipator.unanticipatedEvents().size());

        //
        // Bring Down the ICMP (on the only if on the node) now expect nodeDown
        // only.
        //

        // expect node down event
        anticipator.reset();
        anticipateNodeStatusChanged(node, anticipator, ServiceMonitor.SERVICE_UNAVAILABLE);

        // bring down the ICMP service
        icmpService.bringDown();

        // make sure the down events are received
        assertEquals(0, anticipator.waitForAnticipated(10000).size());
        assertEquals(0, anticipator.unanticipatedEvents().size());

        //
        // Bring up both the node and the httpService at the same time. Expect
        // both a nodeUp and a nodeRegainedService
        //

        // expect node up event and node regained service
        anticipator.reset();
        anticipateNodeStatusChanged(node, anticipator, ServiceMonitor.SERVICE_AVAILABLE);

        // FIXME: Bug 709: The following event never occurs. We sent a
        // nodeLostService earlier we should send a nodeGainedService
        // anticipateSvcStatusChanged(httpService, anticipator,
        // ServiceMonitor.SERVICE_AVAILABLE);

        // bring up all the services on the node
        node.bringUp();

        // make sure the down events are received
        assertEquals(0, anticipator.waitForAnticipated(10000).size());
        assertEquals(0, anticipator.unanticipatedEvents().size());

    }

    public void testCritSvcStatusPropagation() {
        m_pollerConfig.setNodeOutageProcessingEnabled(true);

        MockNode node = m_network.getNode(1);

        //
        // Set critical svc for all interfaces to down and see if we get Node
        // down event
        //

        final EventAnticipator anticipator = m_eventMgr.getEventAnticipator();
        anticipateNodeStatusChanged(node, anticipator, ServiceMonitor.SERVICE_UNAVAILABLE);

        m_poller.init();
        m_poller.start();

        bringDownCritSvcs(node);

        assertEquals(0, anticipator.waitForAnticipated(2000L).size());
        assertEquals(0, anticipator.unanticipatedEvents().size());

        m_poller.stop();

    }

    // what about scheduled outages?
    public void testDontPollDuringScheduledOutages() {
        long start = System.currentTimeMillis();

        MockInterface iface = m_network.getInterface(1, "192.168.1.2");
        m_pollerConfig.addOutage("TestOutage", start, start + 5000, iface.getIpAddr());

        m_poller.init();
        m_poller.start();

        long now = System.currentTimeMillis();
        sleep(3000 - (now - start));

        assertEquals(0, iface.getPollCount());

        sleep(3000);

        assertTrue(0 < iface.getPollCount());

        m_poller.stop();

    }

    private void testElementDeleted(MockElement element, Event deleteEvent) {
        m_pollerConfig.setNodeOutageProcessingEnabled(false);

        PollAnticipator poll = new PollAnticipator();
        element.addAnticipator(poll);

        poll.anticipateAllServices(element);

        m_poller.init();
        m_poller.start();

        // wait til after the first poll of the services
        poll.waitForAnticipated(1000L);

        // now delete the node and send a nodeDeleted event
        m_network.resetInvalidPollCount();
        m_network.removeElement(element);
        m_eventMgr.sendEventToListeners(deleteEvent);

        // now ensure that no invalid polls have occurred
        sleep(3000);

        assertEquals(0, m_network.getInvalidPollCount());

        m_poller.stop();
    }

    // interfaceDeleted: EventConstants.INTERFACE_DELETED_EVENT_UEI
    public void testInterfaceDeleted() {
        MockInterface iface = m_network.getInterface(1, "192.168.1.1");
        Event deleteEvent = MockUtil.createInterfaceDeletedEvent("Test", iface);
        testElementDeleted(iface, deleteEvent);
    }

    // interfaceReparented: EventConstants.INTERFACE_REPARENTED_EVENT_UEI
    public void testInterfaceReparented() {
        m_pollerConfig.setNodeOutageProcessingEnabled(true);

        MockNode node1 = m_network.getNode(1);
        MockNode node2 = m_network.getNode(2);

        MockInterface node1Iface = m_network.getInterface(1, "192.168.1.1");
        MockInterface reparentedIface = m_network.getInterface(1, "192.168.1.2");
        MockInterface node2Iface = m_network.getInterface(2, "192.168.1.3");

        Event reparentEvent = MockUtil.createReparentEvent("Test", "192.168.1.2", 1, 2);

        // we are going to repart to node 2 so when we bring down its only
        // current interface
        // we expect an interface down not the whole node.
        EventAnticipator anticipator = m_eventMgr.getEventAnticipator();
        anticipateInterfaceStatusChanged(node2Iface, anticipator, ServiceMonitor.SERVICE_UNAVAILABLE);

        m_poller.init();
        m_poller.start();

        // move the reparted interface and send a reparted event
        reparentedIface.moveTo(node2);
        m_eventMgr.sendEventToListeners(reparentEvent);

        // now bring down the other interface on the new node
        // System.err.println("Bring Down:"+node2Iface);
        node2Iface.bringDown();

        assertEquals(0, anticipator.waitForAnticipated(2000).size());
        assertEquals(0, anticipator.unanticipatedEvents().size());

        // FIXME: the event below is the CORRECT answer but the Poller isn't
        // doing
        // that. I'm going to test for the INCORRECT answer so I can tell if I
        // change the behavior during refactoring.
        // we now bring down the reparented interface and we should get node2
        // down
        // anticipator.reset();
        // anticipateNodeStatusChanged(node2, anticipator,
        // ServiceMonitor.SERVICE_UNAVAILABLE);

        // FIXME: BEGIN INCORRECT BEHAVIOR HERE
        anticipator.reset();
        anticipator.anticipateEvent(MockUtil.createNodeDownEvent("Test", node1));
        anticipator.anticipateEvent(MockUtil.createNodeLostServiceEvent("Test", m_network.getService(2, "192.168.1.2", "ICMP")));
        // FIXME: END INCORRECT BEHAVIOR HERE

        // System.err.println("Bring Down:"+reparentedIface);
        reparentedIface.bringDown();

        sleep(5000);

        // MockUtil.printEvents("Anticipated",
        // anticipator.waitForAnticipated(2000));
        assertEquals(0, anticipator.waitForAnticipated(3000).size());
        assertEquals(0, anticipator.unanticipatedEvents().size());

    }

    // nodeDeleted: EventConstants.NODE_DELETED_EVENT_UEI
    public void testNodeDeleted() {
        MockNode node = m_network.getNode(1);
        Event deleteEvent = MockUtil.createNodeDeletedEvent("Test", node);

        testElementDeleted(node, deleteEvent);
    }

    // test to see that node lost/regained service events come in
    public void testNodeOutageProcessingDisabled() throws Exception {

        m_pollerConfig.setNodeOutageProcessingEnabled(false);

        EventAnticipator anticipator = m_eventMgr.getEventAnticipator();
        MockNode node = m_network.getNode(1);

        m_poller.init();
        m_poller.start();

        anticipator.reset();
        anticipateSvcStatusChanged(node, anticipator, ServiceMonitor.SERVICE_UNAVAILABLE);

        node.bringDown();

        assertEquals(0, anticipator.waitForAnticipated(10000).size());
        assertEquals(0, anticipator.unanticipatedEvents().size());

        anticipator.reset();
        anticipateSvcStatusChanged(node, anticipator, ServiceMonitor.SERVICE_AVAILABLE);

        node.bringUp();

        assertEquals(0, anticipator.waitForAnticipated(10000).size());
        assertEquals(0, anticipator.unanticipatedEvents().size());

        m_poller.stop();

    }

    // test whole node down
    public void testNodeOutageProcessingEnabled() throws Exception {

        m_pollerConfig.setNodeOutageProcessingEnabled(true);

        EventAnticipator anticipator = m_eventMgr.getEventAnticipator();
        MockNode node = m_network.getNode(1);

        // start the poller
        m_poller.init();
        m_poller.start();

        // setup expected events
        anticipator.reset();
        anticipateNodeStatusChanged(node, anticipator, ServiceMonitor.SERVICE_UNAVAILABLE);

        // brind down the node (duh)
        node.bringDown();

        // make sure the correct events are recieved
        assertEquals(0, anticipator.waitForAnticipated(10000).size());
        assertEquals(0, anticipator.unanticipatedEvents().size());

        // setup excpeted node up events
        anticipator.reset();
        anticipateNodeStatusChanged(node, anticipator, ServiceMonitor.SERVICE_UNAVAILABLE);

        // bring the node back up
        node.bringUp();

        // make sure the up events are received
        assertEquals(0, anticipator.waitForAnticipated(10000).size());
        assertEquals(0, anticipator.unanticipatedEvents().size());

        // now stop the poller
        m_poller.stop();

    }

    public void testPolling() throws Exception {

        m_pollerConfig.setNodeOutageProcessingEnabled(false);

        // create a poll anticipator
        PollAnticipator anticipator = new PollAnticipator();

        // register it with the interfaces services
        MockInterface iface = m_network.getInterface(1, "192.168.1.2");
        iface.addAnticipator(anticipator);

        //
        // first ensure that polls are working while it is up
        //

        // anticipate three polls on all the interfaces services
        anticipator.anticipateAllServices(iface);
        anticipator.anticipateAllServices(iface);
        anticipator.anticipateAllServices(iface);

        // start the poller
        m_poller.init();
        m_poller.start();

        // wait for the polls to occur while its up... 1 poll per second plus
        // overhead
        assertEquals(0, anticipator.waitForAnticipated(4000L).size());

        // stop to poller
        m_poller.stop();

    }

    public void testReparentCausesStatusChange() {

        m_pollerConfig.setNodeOutageProcessingEnabled(true);

        MockNode node1 = m_network.getNode(1);
        MockNode node2 = m_network.getNode(2);

        MockInterface node1Iface = m_network.getInterface(1, "192.168.1.1");
        MockInterface reparentedIface = m_network.getInterface(1, "192.168.1.2");
        MockInterface node2Iface = m_network.getInterface(2, "192.168.1.3");

        //
        // Plan to bring down both nodes except the reparented interface
        // the node owning the interface should be up while the other is down
        // after reparenting we should got the old owner go down while the other
        // comes up.
        //
        EventAnticipator anticipator = m_eventMgr.getEventAnticipator();
        anticipateNodeStatusChanged(node2, anticipator, ServiceMonitor.SERVICE_UNAVAILABLE);
        anticipateInterfaceStatusChanged(node1Iface, anticipator, ServiceMonitor.SERVICE_UNAVAILABLE);

        // bring down both nodes but bring iface back up
        node1.bringDown();
        node2.bringDown();
        reparentedIface.bringUp();

        Event reparentEvent = MockUtil.createReparentEvent("Test", "192.168.1.2", 1, 2);

        m_poller.init();
        m_poller.start();

        assertEquals(0, anticipator.waitForAnticipated(2000).size());
        assertEquals(0, anticipator.unanticipatedEvents().size());

        anticipator.reset();

        // FIXME: should I expect this to send new events saying that the old
        // node is now down
        // and the new node is now up? YES I SHOULD
        // after moving the interface we expect node down on node1 and node up
        // on node2;
        // anticipateNodeStatusChanged(node1, anticipator,
        // ServiceMonitor.SERVICE_UNAVAILABLE);
        // anticipateNodeStatusChanged(node2, anticipator,
        // ServiceMonitor.SERVICE_AVAILABLE);

        reparentedIface.moveTo(node2);
        m_eventMgr.sendEventToListeners(reparentEvent);

        assertEquals(0, anticipator.waitForAnticipated(2000).size());
        assertEquals(0, anticipator.unanticipatedEvents().size());

        m_poller.stop();

    }

    // send a nodeGainedService event:
    // EventConstants.NODE_GAINED_SERVICE_EVENT_UEI
    public void testSendNodeGainedService() {

        m_pollerConfig.setNodeOutageProcessingEnabled(false);

        m_poller.init();
        m_poller.start();

        MockNode node = m_network.addNode(3, "TestNode");
        m_db.writeNode(node);
        MockInterface iface = m_network.addInterface(3, "10.1.1.1");
        m_db.writeInterface(iface);
        MockService element = m_network.addService(3, "10.1.1.1", "HTTP");
        m_db.writeService(element);
        m_pollerConfig.addService(element);

        MockVisitor gainSvcSender = new MockVisitorAdapter() {
            public void visitService(MockService svc) {
                Event event = MockUtil.createNodeGainedServiceEvent("Test", svc);
                m_eventMgr.sendEventToListeners(event);
            }
        };
        element.visit(gainSvcSender);

        PollAnticipator anticipator = new PollAnticipator();
        element.addAnticipator(anticipator);

        anticipator.anticipateAllServices(element);

        assertEquals(0, anticipator.waitForAnticipated(10000).size());

        EventAnticipator eventAnticipator = m_eventMgr.getEventAnticipator();
        anticipateSvcStatusChanged(element, eventAnticipator, ServiceMonitor.SERVICE_UNAVAILABLE);

        element.bringDown();

        assertEquals(0, eventAnticipator.waitForAnticipated(10000).size());
        assertEquals(0, eventAnticipator.unanticipatedEvents().size());

    }

    // serviceDeleted: EventConstants.SERVICE_DELETED_EVENT_UEI
    public void testServiceDeleted() {
        MockService svc = m_network.getService(1, "192.168.1.1", "SMTP");
        Event deleteEvent = MockUtil.createServiceDeletedEvent("Test", svc);

        testElementDeleted(svc, deleteEvent);
    }

    public void testSuspendPollingService() {
        long start = System.currentTimeMillis();

        MockService svc = m_network.getService(1, "192.168.1.2", "SMTP");

        m_poller.init();
        m_poller.start();

        sleep(2000);
        assertTrue(0 < svc.getPollCount());

        m_eventMgr.sendEventToListeners(MockUtil.createSuspendPollingServiceEvent("Test", svc));
        svc.resetPollCount();

        sleep(5000);
        assertEquals(0, svc.getPollCount());

        m_eventMgr.sendEventToListeners(MockUtil.createResumePollingServiceEvent("Test", svc));

        sleep(2000);
        assertTrue(0 < svc.getPollCount());

        m_poller.stop();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    // TODO: test multiple polling packages

    // TODO: test overlapping polling packages

    // TODO: test two packages both with the crit service and status propagation

    // TODO: how does unmanaging a node/iface/service work with the poller

}
