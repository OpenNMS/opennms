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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import junit.framework.TestCase;

import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockElement;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockOutageConfig;
import org.opennms.netmgt.mock.MockPollerConfig;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.mock.MockVisitor;
import org.opennms.netmgt.mock.MockVisitorAdapter;
import org.opennms.netmgt.mock.OutageAnticipator;
import org.opennms.netmgt.mock.PollAnticipator;
import org.opennms.netmgt.mock.Querier;
import org.opennms.netmgt.outage.OutageManager;
import org.opennms.netmgt.xml.event.Event;

public class PollerTest extends TestCase {

    private Poller m_poller;

    private OutageManager m_outageMgr;

    private MockNetwork m_network;
    
    private MockDatabase m_db;

    private MockPollerConfig m_pollerConfig;

    private MockEventIpcManager m_eventMgr;

    private boolean m_daemonsStarted = false;

    private EventAnticipator m_anticipator;

    private OutageAnticipator m_outageAnticipator;
    
    //
    // SetUp and TearDown
    //

    public void setUp() {
        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
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
        
        m_anticipator = new EventAnticipator();
        m_outageAnticipator = new OutageAnticipator(m_db);
        
        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventWriter(m_db);
        m_eventMgr.setEventAnticipator(m_anticipator);
        m_eventMgr.addEventListener(m_outageAnticipator);
        
        m_poller = new Poller();
        m_poller.setEventManager(m_eventMgr);
        m_poller.setDbConnectionFactory(m_db);
        m_poller.setPollerConfig(m_pollerConfig);
        m_poller.setPollOutagesConfig(m_pollerConfig);
        
        MockOutageConfig config = new MockOutageConfig();
        config.setGetNextOutageID(m_db.getNextOutageIdStatement());
        
        m_outageMgr = new OutageManager();
        m_outageMgr.setEventMgr(m_eventMgr);
        m_outageMgr.setOutageMgrConfig(config);
        m_outageMgr.setDbConnectionFactory(m_db);

    }

    public void tearDown() {
        stopDaemons();
        sleep(200);
        assertTrue(MockUtil.noWarningsOrHigherLogged());
        m_db.drop();
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
    }

    //
    // Tests
    //
    
    public void testBug709() {

        m_pollerConfig.setNodeOutageProcessingEnabled(true);

        MockNode node = m_network.getNode(2);
        MockService icmpService = m_network.getService(2, "192.168.1.3", "ICMP");
        MockService httpService = m_network.getService(2, "192.168.1.3", "HTTP");

        // start the poller
        startDaemons();

        //
        // Bring Down the HTTP service and expect nodeLostService Event
        //

        resetAnticipated();
        anticipateDown(httpService);

        // bring down the HTTP service
        httpService.bringDown();

        verifyAnticipated(10000);

        //
        // Bring Down the ICMP (on the only interface on the node) now expect nodeDown
        // only.
        //

        resetAnticipated();
        anticipateDown(node);

        // bring down the ICMP service
        icmpService.bringDown();

        // make sure the down events are received
        verifyAnticipated(10000);

        //
        // Bring up both the node and the httpService at the same time. Expect
        // both a nodeUp and a nodeRegainedService
        //

        resetAnticipated();
        anticipateUp(node);

        // FIXME: Bug 709: The following event never occurs. We sent a
        // nodeLostService earlier we should send a nodeGainedService
        // anticipateSvcUp(httpService);

        // bring up all the services on the node
        node.bringUp();

        // make sure the down events are received
        verifyAnticipated(10000);

    }

    private void resetAnticipated() {
        // expect node lost service for HTTP
        m_anticipator.reset();
        m_outageAnticipator.reset();
    }

    public void testCritSvcStatusPropagation() {
        m_pollerConfig.setNodeOutageProcessingEnabled(true);

        MockNode node = m_network.getNode(1);

        anticipateDown(node);

        startDaemons();

        bringDownCritSvcs(node);

        verifyAnticipated(2000);


    }
    
    // what about scheduled outages?
    public void testDontPollDuringScheduledOutages() {
        long start = System.currentTimeMillis();

        MockInterface iface = m_network.getInterface(1, "192.168.1.2");
        m_pollerConfig.addScheduledOutage("TestOutage", start, start + 5000, iface.getIpAddr());

        startDaemons();

        long now = System.currentTimeMillis();
        sleep(3000 - (now - start));

        assertEquals(0, iface.getPollCount());

        sleep(5000);

        assertTrue(0 < iface.getPollCount());


    }

    // Test harness that tests any type of node, interface or element.
    private void testElementDeleted(MockElement element, Event deleteEvent) {
        m_pollerConfig.setNodeOutageProcessingEnabled(false);

        PollAnticipator poll = new PollAnticipator();
        element.addAnticipator(poll);

        poll.anticipateAllServices(element);

        startDaemons();

        // wait til after the first poll of the services
        poll.waitForAnticipated(1000L);

        // now delete the node and send a nodeDeleted event
        m_network.resetInvalidPollCount();
        m_network.removeElement(element);
        m_eventMgr.sendEventToListeners(deleteEvent);

        // now ensure that no invalid polls have occurred
        sleep(3000);

        assertEquals(0, m_network.getInvalidPollCount());

    }

    // serviceDeleted: EventConstants.SERVICE_DELETED_EVENT_UEI
    public void testServiceDeleted() {
        MockService svc = m_network.getService(1, "192.168.1.1", "SMTP");
        Event deleteEvent = MockUtil.createServiceDeletedEvent("Test", svc);

        testElementDeleted(svc, deleteEvent);
    }

    // interfaceDeleted: EventConstants.INTERFACE_DELETED_EVENT_UEI
    public void testInterfaceDeleted() {
        MockInterface iface = m_network.getInterface(1, "192.168.1.1");
        Event deleteEvent = MockUtil.createInterfaceDeletedEvent("Test", iface);
        testElementDeleted(iface, deleteEvent);
    }

    // nodeDeleted: EventConstants.NODE_DELETED_EVENT_UEI
    public void testNodeDeleted() {
        MockNode node = m_network.getNode(1);
        Event deleteEvent = MockUtil.createNodeDeletedEvent("Test", node);

        testElementDeleted(node, deleteEvent);
        
        
    }

    // interfaceReparented: EventConstants.INTERFACE_REPARENTED_EVENT_UEI
    public void testInterfaceReparented() {

        m_pollerConfig.setNodeOutageProcessingEnabled(true);

        MockNode node1 = m_network.getNode(1);
        MockNode node2 = m_network.getNode(2);

        MockInterface dotOne = m_network.getInterface(1, "192.168.1.1");
        MockInterface dotTwo = m_network.getInterface(1, "192.168.1.2");
        MockInterface dotThree = m_network.getInterface(2, "192.168.1.3");
        

        Event reparentEvent = MockUtil.createReparentEvent("Test", "192.168.1.2", 1, 2);

        // we are going to reparent to node 2 so when we bring down its only
        // current interface we expect an interface down not the whole node.
        anticipateDown(dotThree);

        startDaemons();

        // move the reparted interface and send a reparented event
        dotTwo.moveTo(node2);
        m_db.reparentInterface(dotTwo.getIpAddr(), node1.getNodeId(), node2.getNodeId());

        // send the reparent event to the daemons
        m_eventMgr.sendEventToListeners(reparentEvent);

        // now bring down the other interface on the new node
        // System.err.println("Bring Down:"+node2Iface);
        dotThree.bringDown();

        verifyAnticipated(2000);

        resetAnticipated();
        anticipateDown(node2);

        // System.err.println("Bring Down:"+reparentedIface);
        dotTwo.bringDown();

        //sleep(5000);

        verifyAnticipated(6000);

    }


    // test to see that node lost/regained service events come in
    public void testNodeOutageProcessingDisabled() throws Exception {

        m_pollerConfig.setNodeOutageProcessingEnabled(false);

        MockNode node = m_network.getNode(1);

        startDaemons();

        resetAnticipated();
        anticipateServicesDown(node);

        node.bringDown();

        verifyAnticipated(10000);

        resetAnticipated();
        anticipateServicesUp(node);

        node.bringUp();

        verifyAnticipated(10000);


    }

    // test whole node down
    public void testNodeOutageProcessingEnabled() throws Exception {

        m_pollerConfig.setNodeOutageProcessingEnabled(true);

        MockNode node = m_network.getNode(1);

        // start the poller
        startDaemons();

        resetAnticipated();
        anticipateDown(node);

        // brind down the node (duh)
        node.bringDown();

        // make sure the correct events are recieved
        verifyAnticipated(10000);

        resetAnticipated();
        anticipateUp(node);

        // bring the node back up
        node.bringUp();

        // make sure the up events are received
        verifyAnticipated(10000);


    }
    
    public void testNodeLostRegainedService() throws Exception {

        testElementDownUp(m_network.getService(1, "192.168.1.1", "SMTP"));

    }
    
    
    public void testInterfaceDownUp() {

        testElementDownUp(m_network.getInterface(1, "192.168.1.1"));
    }

    public void testNodeDownUp() {
        testElementDownUp(m_network.getNode(1));
    }


    private void testElementDownUp(MockElement element) {
        startDaemons();

        resetAnticipated();
        anticipateDown(element);

        MockUtil.println("Bringing down element: "+element);
        element.bringDown();
        MockUtil.println("Finished bringing down element: "+element);
        
        verifyAnticipated(2000);
        
        sleep(5000);
        
        resetAnticipated();
        anticipateUp(element);
        
        MockUtil.println("Bringing up element: "+element);
        element.bringUp();
        MockUtil.println("Finished bringing up element: "+element);
        
        verifyAnticipated(2000);
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
        startDaemons();

        // wait for the polls to occur while its up... 1 poll per second plus
        // overhead
        assertEquals(0, anticipator.waitForAnticipated(4000L).size());


    }

    public void testReparentCausesStatusChange() {

        m_pollerConfig.setNodeOutageProcessingEnabled(true);

        MockNode node1 = m_network.getNode(1);
        MockNode node2 = m_network.getNode(2);

        MockInterface dotOne = m_network.getInterface(1, "192.168.1.1");
        MockInterface dotTwo = m_network.getInterface(1, "192.168.1.2");
        MockInterface dotThree = m_network.getInterface(2, "192.168.1.3");

        //
        // Plan to bring down both nodes except the reparented interface
        // the node owning the interface should be up while the other is down
        // after reparenting we should got the old owner go down while the other
        // comes up.
        //
        anticipateDown(node2);
        anticipateDown(dotOne);

        // bring down both nodes but bring iface back up
        node1.bringDown();
        node2.bringDown();
        dotTwo.bringUp();

        Event reparentEvent = MockUtil.createReparentEvent("Test", "192.168.1.2", 1, 2);

        startDaemons();

        verifyAnticipated(2000);



        m_db.reparentInterface(dotTwo.getIpAddr(), dotTwo.getNodeId(), node2.getNodeId());
        dotTwo.moveTo(node2);

        resetAnticipated();
        anticipateDown(node1, true);
        anticipateUp(node2, true);
        anticipateDown(dotThree, true);

        m_eventMgr.sendEventToListeners(reparentEvent);

        verifyAnticipated(20000);

    }

    // send a nodeGainedService event:
    // EventConstants.NODE_GAINED_SERVICE_EVENT_UEI
    public void testSendNodeGainedService() {

        m_pollerConfig.setNodeOutageProcessingEnabled(false);

        startDaemons();

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

        anticipateDown(element);

        element.bringDown();

        verifyAnticipated(10000);

    }

    public void testSuspendPollingResumeService() {
        long start = System.currentTimeMillis();

        MockService svc = m_network.getService(1, "192.168.1.2", "SMTP");

        startDaemons();

        sleep(2000);
        assertTrue(0 < svc.getPollCount());

        m_eventMgr.sendEventToListeners(MockUtil.createSuspendPollingServiceEvent("Test", svc));
        svc.resetPollCount();

        sleep(5000);
        assertEquals(0, svc.getPollCount());

        m_eventMgr.sendEventToListeners(MockUtil.createResumePollingServiceEvent("Test", svc));

        sleep(2000);
        assertTrue(0 < svc.getPollCount());

    }
    
    //
    // Utility methods
    //

    private void startDaemons() {
        m_outageMgr.init();
        m_poller.init();
        m_outageMgr.start();
        m_poller.start();
        m_daemonsStarted = true;
    }


    private void stopDaemons() {
        if (m_daemonsStarted) {
            m_poller.stop();
            m_outageMgr.stop();
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    private void verifyAnticipated(long millis) {
        // make sure the down events are received
        assertEquals("Expected events not forthcoming", 0, m_anticipator.waitForAnticipated(millis).size());
        sleep(2000);
        MockUtil.printEvents("Unanticipated: ", m_anticipator.unanticipatedEvents());
        assertEquals("Received unexpected events", 0, m_anticipator.unanticipatedEvents().size());
        sleep(500);
        assertEquals("Wrong number of outages opened", m_outageAnticipator.getExpectedOpens(), m_outageAnticipator.getActualOpens());
        assertEquals("Wrong number of outages in outage table", m_outageAnticipator.getExpectedOutages(), m_outageAnticipator.getActualOutages());
        assertTrue("Created outages don't match the expected outages", m_outageAnticipator.checkAnticipated());
    }

    
    private void anticipateUp(MockElement element) {
        anticipateUp(element, false);
    }
    
    private void anticipateUp(MockElement element, boolean force) {
        if (force || element.getPollStatus() != ServiceMonitor.SERVICE_AVAILABLE) {
            Event event = element.createUpEvent();
            m_anticipator.anticipateEvent(event);
            m_outageAnticipator.anticipateOutageClosed(element, event);
        }
    }

    private void anticipateDown(MockElement element) {
        anticipateDown(element, false);
    }
    
    private void anticipateDown(MockElement element, boolean force) {
        if (force || element.getPollStatus() != ServiceMonitor.SERVICE_UNAVAILABLE) {
            Event event = element.createDownEvent();
            m_anticipator.anticipateEvent(event);
            m_outageAnticipator.anticipateOutageOpened(element, event);
        }
    }
    
    private void anticipateDownEvent(MockElement element) {
        m_anticipator.anticipateEvent(element.createDownEvent());
    }
    
    
    
    

    private void anticipateServicesUp(MockElement node) {
        MockVisitor eventCreator = new MockVisitorAdapter() {
            public void visitService(MockService svc) {
                anticipateUp(svc);
            }
        };
        node.visit(eventCreator);
    }

    private void anticipateServicesDown(MockElement node) {
        MockVisitor eventCreator = new MockVisitorAdapter() {
            public void visitService(MockService svc) {
                anticipateDown(svc);
            }
        };
        node.visit(eventCreator);
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
    
    
    class OutageChecker extends Querier { 
        private Event m_lostSvcEvent;
        private Timestamp m_lostSvcTime;
        private MockService m_svc;
        private Event m_regainedSvcEvent;
        private Timestamp m_regainedSvcTime;
        OutageChecker(MockService svc, Event lostSvcEvent) throws Exception {
            this(svc, lostSvcEvent, null);
        }
        OutageChecker(MockService svc, Event lostSvcEvent, Event regainedSvcEvent) {
            super(m_db, "select * from outages where nodeid = ? and ipAddr = ? and serviceId = ?");
            
            m_svc = svc;
            m_lostSvcEvent = lostSvcEvent;
            m_lostSvcTime = m_db.convertEventTimeToTimeStamp(m_lostSvcEvent.getTime());
            m_regainedSvcEvent = regainedSvcEvent;
            if (m_regainedSvcEvent != null)
                m_regainedSvcTime = m_db.convertEventTimeToTimeStamp(m_regainedSvcEvent.getTime());
        }
       public void processRow(ResultSet rs) throws SQLException {
            assertEquals(m_svc.getNodeId(), rs.getInt("nodeId"));
            assertEquals(m_svc.getIpAddr(), rs.getString("ipAddr"));
            assertEquals(m_svc.getId(), rs.getInt("serviceId"));
            assertEquals(m_lostSvcEvent.getDbid(), rs.getInt("svcLostEventId"));
            assertEquals(m_lostSvcTime, rs.getTimestamp("ifLostService"));
            assertEquals(getRegainedEventId(), rs.getObject("svcRegainedEventId"));
            assertEquals(m_regainedSvcTime, rs.getTimestamp("ifRegainedService"));
        }
       private Integer getRegainedEventId() {
           if (m_regainedSvcEvent == null)
               return null;
           return new Integer(m_regainedSvcEvent.getDbid());
       }
    };
    


    // TODO: test multiple polling packages

    // TODO: test overlapping polling packages

    // TODO: test two packages both with the crit service and status propagation

    // TODO: how does unmanaging a node/iface/service work with the poller
    
    // TODO: test over lapping poll outages

}
