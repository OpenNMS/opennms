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

package org.opennms.netmgt.mock;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.PollOutagesConfig;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.eventd.mock.EventAnticipator;
import org.opennms.netmgt.eventd.mock.MockEventIpcManager;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.poller.IfKey;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.QueryManager;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.netmgt.xml.event.Event;

/**
 * Test the MockNetwork and related classes
 * 
 * @author brozow
 */
public class MockNetworkTest extends TestCase {

    static class ElementCounter implements MockVisitor {

        private int containerCount = 0;

        private int elementCount = 0;

        private int interfaceCount = 0;

        private int networkCount = 0;

        private int nodeCount = 0;

        private int serviceCount = 0;

        public int getContainerCount() {
            return containerCount;
        }

        public int getElementCount() {
            return elementCount;
        }

        public int getInterfaceCount() {
            return interfaceCount;
        }

        public int getNetworkCount() {
            return networkCount;
        }

        public int getNodeCount() {
            return nodeCount;
        }

        public int getServiceCount() {
            return serviceCount;
        }

        public void visitContainer(MockContainer<?,?> c) {
            containerCount++;
        }

        public void visitElement(MockElement e) {
            elementCount++;
        }

        public void visitInterface(MockInterface i) {
            interfaceCount++;
        }

        public void visitNetwork(MockNetwork n) {
            networkCount++;
        }

        public void visitNode(MockNode n) {
            nodeCount++;
        }

        public void visitService(MockService s) {
            serviceCount++;
        }
    }

    class StatusChecker extends MockVisitorAdapter {
        PollStatus m_expectedStatus;

        int m_serviceCount = 0;

        public StatusChecker(PollStatus status) {
            m_expectedStatus = status;
        }

        public int getServiceCount() {
            return m_serviceCount;
        }

        public void resetServiceCount() {
            m_serviceCount = 0;
        }

        public void setExpectedStatus(PollStatus status) {
            m_expectedStatus = status;
        }

        @Override
        public void visitService(MockService service) {
            m_serviceCount++;
            ServiceMonitor monitor = m_pollerConfig.getServiceMonitor(service.getSvcName());
            PollStatus pollResult = monitor.poll(service, new HashMap<String, Object>());
            assertEquals(m_expectedStatus, pollResult);
        }
    }

    private StatusChecker m_downChecker;

    private MockNetwork m_network;

    private StatusChecker m_upChecker;

    private MockPollerConfig m_pollerConfig;

    private MockEventIpcManager m_eventMgr;

    private void anticipateServiceEvents(final EventAnticipator anticipator, MockElement element, final String uei) {
        MockVisitor eventSetter = new MockVisitorAdapter() {
            public void visitService(MockService svc) {
                Event event = MockEventUtil.createServiceEvent("Test", uei, svc, null);
                anticipator.anticipateEvent(event);
            }
        };
        element.visit(eventSetter);
    }

    protected void setUp() throws Exception {
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
        m_network.addInterface("192.168.1.2");
        m_network.addNode(3, "IPv6Server");
        m_network.addInterface("fe80:0000:0000:0000:0000:0000:0000:00ff");
        m_network.addService("ICMP");
        m_network.addService("HTTP");
        m_network.addService("DNS");
        
        m_eventMgr = new MockEventIpcManager();

        m_pollerConfig = new MockPollerConfig(m_network);
        m_pollerConfig.addPackage("TestPackage");
        m_pollerConfig.addDowntime(1000L, 0L, -1L, false);
        m_pollerConfig.setDefaultPollInterval(1000L);
        m_pollerConfig.populatePackage(m_network);
        m_pollerConfig.setPollInterval("ICMP", 500L);
        
        m_upChecker = new StatusChecker(PollStatus.up());
        m_downChecker = new StatusChecker(PollStatus.down());

    }

    protected void tearDown() throws Exception {
    }

    public void testCreateInterfaces() {

        MockNode router = m_network.getNode(1);
        MockInterface iface = m_network.getInterface(1, "192.168.1.1");
        assertEquals("192.168.1.1", iface.getIpAddr());
        assertEquals(router, iface.getNode());

        MockInterface iface2 = m_network.getInterface(1, "192.168.1.2");
        assertEquals("192.168.1.2", iface2.getIpAddr());
        assertEquals(router, iface2.getNode());

        MockNode server = m_network.getNode(2);
        MockInterface iface3 = m_network.getInterface(2, "192.168.1.3");
        assertEquals(server.getNodeId(), iface3.getNode().getNodeId());
        assertEquals(server, iface3.getNode());

        MockNode ipv6 = m_network.getNode(3);
        MockInterface iface4 = m_network.getInterface(3, "fe80:0000:0000:0000:0000:0000:0000:00ff");
        assertEquals(ipv6.getNodeId(), iface4.getNode().getNodeId());
        assertEquals(ipv6, iface4.getNode());
    }

    public void testCreateNodes() {

        MockNode router = m_network.getNode(1);
        assertEquals(1, router.getNodeId());
        assertEquals("Router", router.getLabel());
        assertEquals(2, router.getMembers().size());

        MockNode server = m_network.getNode(2);
        assertEquals(2, server.getNodeId());
        assertEquals("Server", server.getLabel());
        assertEquals(2, server.getMembers().size());

        MockNode ipv6 = m_network.getNode(3);
        assertEquals(3, ipv6.getNodeId());
        assertEquals("IPv6Server", ipv6.getLabel());
        assertEquals(1, ipv6.getMembers().size());
    }

    public void testCreateServices() {

        MockInterface rtrIface = m_network.getInterface(1, "192.168.1.2");
        MockInterface svrIface = m_network.getInterface(2, "192.168.1.3");
        MockService icmpSvc = m_network.getService(1, "192.168.1.2", "ICMP");
        MockService icmpSvc2 = m_network.getService(1, "192.168.1.1", "ICMP");
        MockService httpSvc = m_network.getService(2, "192.168.1.3", "HTTP");

        assertEquals("ICMP", icmpSvc.getSvcName());
        assertEquals(rtrIface, icmpSvc.getInterface());
        assertEquals("HTTP", httpSvc.getSvcName());
        assertEquals(svrIface, httpSvc.getInterface());

        assertTrue(icmpSvc.getId() == icmpSvc2.getId());
        assertFalse(icmpSvc.getId() == httpSvc.getId());
    }

    public void testEventListeners() {
        Event sentEvent = MockEventUtil.createEvent("Test", EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, 1, "192.168.1.1", "NEW", null);
        Event sentEvent2 = MockEventUtil.createEvent("Test", EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI, 1, "192.168.1.1", "NEW", null);

        class MockListener implements EventListener {
            private Event receivedEvent;

            public String getName() {
                return "MockListener";
            }

            public Event getReceivedEvent() {
                return receivedEvent;
            }

            public void onEvent(Event event) {
                System.err.println("onEvent: " + event.getUei());
                receivedEvent = event;
            }

            public void reset() {
                receivedEvent = null;
            }
        }
        ;
        MockListener listener = new MockListener();

        m_eventMgr.addEventListener(listener, EventConstants.NODE_GAINED_SERVICE_EVENT_UEI);
        m_eventMgr.sendEventToListeners(sentEvent);
        assertTrue(MockEventUtil.eventsMatch(sentEvent, listener.getReceivedEvent()));

        listener.reset();
        m_eventMgr.sendEventToListeners(sentEvent2);
        assertFalse(MockEventUtil.eventsMatch(sentEvent2, listener.getReceivedEvent()));

    }

    public void testEventMgr() {
        assertNotNull(m_eventMgr);
    }

    public void testEventProcessing() {
        testEventProcessing(m_network.getService(2, "192.168.1.3", "ICMP"));
        testEventProcessing(m_network.getNode(2));
        testEventProcessing(m_network.getInterface(1, "192.168.1.2"));

    }

    /**
     * @param element
     */
    private void testEventProcessing(MockElement element) {
        m_pollerConfig.setNodeOutageProcessingEnabled(false);

        EventAnticipator anticipator = m_eventMgr.getEventAnticipator();

        String nlsUei = EventConstants.NODE_LOST_SERVICE_EVENT_UEI;
        String nrsUei = EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI;
        anticipateServiceEvents(anticipator, element, nlsUei);
        element.bringDown();

        assertFalse(0 == anticipator.waitForAnticipated(0).size());

        anticipator.reset();
        anticipateServiceEvents(anticipator, element, nrsUei);
        element.bringUp();

        assertFalse(0 == anticipator.waitForAnticipated(0).size());

        anticipator.reset();
        anticipateServiceEvents(anticipator, element, nlsUei);
        element.bringDown();

        MockVisitor lostSvcSender = new MockVisitorAdapter() {
            public void visitService(MockService svc) {
                Event event = MockEventUtil.createEvent("Test", EventConstants.NODE_LOST_SERVICE_EVENT_UEI, svc.getNodeId(), svc.getIpAddr(), svc.getSvcName(), String.valueOf(PollStatus.SERVICE_UNAVAILABLE));
                m_eventMgr.sendNow(event);
            }
        };
        element.visit(lostSvcSender);

        assertEquals(0, anticipator.waitForAnticipated(0).size());

        anticipator.reset();
        anticipateServiceEvents(anticipator, element, nrsUei);
        element.bringUp();

        MockVisitor gainedSvcSender = new MockVisitorAdapter() {
            public void visitService(MockService svc) {
                Event event = MockEventUtil.createEvent("Test", EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI, svc.getNodeId(), svc.getIpAddr(), svc.getSvcName(), null);
                m_eventMgr.sendNow(event);
            }
        };
        element.visit(gainedSvcSender);

        m_eventMgr.finishProcessingEvents();
        assertEquals(0, anticipator.waitForAnticipated(0).size());
        assertEquals(0, anticipator.unanticipatedEvents().size());

        MockNode node = m_network.getNode(1);
        Event nodeEvent = MockEventUtil.createNodeDownEvent("Test", node);

        anticipator.reset();
        m_eventMgr.sendNow(nodeEvent);
        m_eventMgr.finishProcessingEvents();
        assertEquals(0, anticipator.waitForAnticipated(0).size());
        assertEquals(1, anticipator.unanticipatedEvents().size());

    }
    
    public void testInvalidPoll() throws UnknownHostException {
        m_network.resetInvalidPollCount();
        MonitoredService svc = new MockMonitoredService(99, "InvalidNode", InetAddressUtils.addr("1.1.1.1"), "ICMP");
        ServiceMonitor monitor = m_pollerConfig.getServiceMonitor("ICMP");
        monitor.poll(svc, new HashMap<String, Object>());
        assertEquals(1, m_network.getInvalidPollCount());

    }

    public void testLookupNotThere() {
        assertNotNull(m_network.getService(1, "192.168.1.1", "ICMP"));
        assertNotNull(m_network.getService(3, "fe80:0000:0000:0000:0000:0000:0000:00ff", "ICMP"));
        assertNotNull(m_network.getService(3, "fe80:0000:0000:0000:0000:0000:0000:00ff", "HTTP"));
        assertNotNull(m_network.getService(3, "fe80:0000:0000:0000:0000:0000:0000:00ff", "DNS"));
        assertNull(m_network.getService(7, "192.168.1.1", "ICMP"));
        assertNull(m_network.getService(1, "192.168.1.175", "ICMP"));
        assertNull(m_network.getService(1, "192.168.1.1", "ICMG"));
        assertNull(m_network.getService(3, "fe80:0000:0000:0000:0000:0000:0000:00ff", "DHCP"));
    }

    public void testPollerConfig() {
        m_pollerConfig.setNodeOutageProcessingEnabled(true);
        m_pollerConfig.setPollInterval("HTTP", 750L);
        m_pollerConfig.setPollerThreads(5);
        m_pollerConfig.setCriticalService("YAHOO");
        PollerConfig pollerConfig = m_pollerConfig;

        // test the nodeOutageProcessing setting works
        assertTrue(pollerConfig.isNodeOutageProcessingEnabled());

        // test to ensure that the poller has packages
        Enumeration<Package> pkgs = pollerConfig.enumeratePackage();
        assertNotNull(pkgs);
        int pkgCount = 0;
        Package pkg = null;

        while (pkgs.hasMoreElements()) {
            pkg = (Package) pkgs.nextElement();
            pkgCount++;
        }
        assertTrue(pkgCount > 0);

        // ensure a sample interface is in the package
        assertTrue(pollerConfig.isInterfaceInPackage("192.168.1.1", pkg));

        Enumeration<Service> svcs = pkg.enumerateService();
        assertNotNull(svcs);
        while (svcs.hasMoreElements()) {
            Service svc = (Service) svcs.nextElement();
            if ("ICMP".equals(svc.getName()))
                assertEquals(500L, svc.getInterval());
            else if ("HTTP".equals(svc.getName()))
                assertEquals(750L, svc.getInterval());
            else
                assertEquals(1000L, svc.getInterval());
        }

        // ensure that setting the thread worked
        assertEquals(5, pollerConfig.getThreads());

        // ensure that setting the critical service worked
        assertEquals("YAHOO", pollerConfig.getCriticalService());

        // ensure that we have service monitors to the sevices
        assertNotNull(pollerConfig.getServiceMonitor("SMTP"));

    }

    public void testPollOutageConfig() {
        PollOutagesConfig pollOutagesConfig = m_pollerConfig;
        assertNotNull(pollOutagesConfig);
    }

    public void testPollStatus() {
        MockNode node = m_network.getNode(1);
        MockInterface iface = m_network.getInterface(1, "192.168.1.2");
        assertEquals(PollStatus.up(), node.getPollStatus());
        assertEquals(PollStatus.up(), iface.getPollStatus());
        node.bringDown();
        assertEquals(PollStatus.down(), node.getPollStatus());
        assertEquals(PollStatus.down(), iface.getPollStatus());
        iface.bringUp();
        assertEquals(PollStatus.up(), node.getPollStatus());
        assertEquals(PollStatus.up(), iface.getPollStatus());
        node.bringUp();
        assertEquals(PollStatus.up(), node.getPollStatus());
        assertEquals(PollStatus.up(), iface.getPollStatus());
    }

    public void testQueryManager() throws Exception {
        QueryManager queryManager = new MockQueryManager(m_network);
        assertNotNull(queryManager);

        assertTrue(queryManager.activeServiceExists("Test", 1, "192.168.1.1", "ICMP"));
        assertFalse(queryManager.activeServiceExists("Test", 1, "192.168.1.17", "ICMP"));

        MockInterface iface = m_network.getInterface(1, "192.168.1.2");
        Collection<MockService> expectedSvcs = getServicesForInterface(iface);

        List<Integer> svcs = queryManager.getActiveServiceIdsForInterface("192.168.1.2");

        for (MockService svc : expectedSvcs) {
            assertTrue(svcs.contains(Integer.valueOf(svc.getId())));
        }

        List<IfKey> ifKeys = queryManager.getInterfacesWithService("HTTP");
        MockInterface httpIf = m_network.getInterface(2, "192.168.1.3");
        assertEquals(2, ifKeys.size());
        IfKey key = ifKeys.get(0);
        assertEquals(httpIf.getNode().getNodeId(), key.getNodeId());
        assertEquals(httpIf.getIpAddr(), key.getIpAddr());

        int findNodeId = queryManager.getNodeIDForInterface("192.168.1.3");
        assertEquals(httpIf.getNode().getNodeId(), findNodeId);

        assertEquals("Router", queryManager.getNodeLabel(1));

        assertEquals(2, queryManager.getServiceCountForInterface("192.168.1.1"));

    }

    private Collection<MockService> getServicesForInterface(MockInterface iface) {
        return iface.getServices();
    }

    public void testRemove() {
        assertNotNull(m_network.getService(1, "192.168.1.1", "SMTP"));
        m_network.removeService(m_network.getService(1, "192.168.1.1", "SMTP"));
        assertNull(m_network.getService(1, "192.168.1.1", "SMTP"));

        assertNotNull(m_network.getInterface(1, "192.168.1.1"));
        m_network.removeInterface(m_network.getInterface(1, "192.168.1.1"));
        assertNull(m_network.getInterface(1, "192.168.1.1"));
        assertNull(m_network.getService(1, "192.168.1.1", "ICMP"));

        assertNotNull(m_network.getNode(1));
        m_network.removeNode(m_network.getNode(1));
        assertNull(m_network.getNode(1));
        assertNull(m_network.getInterface(1, "192.168.1.2"));

        MockInterface iface = m_network.getInterface(2, "192.168.1.3");
        assertNotNull(iface);
        m_network.removeElement(iface);
        assertNull(m_network.getInterface(2, "192.168.1.3"));
    }

    public void testScheduledOutages() {
        long now = System.currentTimeMillis();
        long tenMinutes = 600000L;

        m_pollerConfig.addScheduledOutage("outage1", now - tenMinutes, now + tenMinutes, "192.168.1.1");
        m_pollerConfig.addScheduledOutage("outage2", now - tenMinutes, now, "192.168.1.2");
        m_pollerConfig.addScheduledOutage("outage3", now - tenMinutes, now, 1);
        m_pollerConfig.addScheduledOutage("outage4", now - tenMinutes, now, 2);
        
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        Package pkg = m_pollerConfig.getPackage("TestPackage");
        assertNotNull(pkg);

        Collection<String> outages = pkg.getOutageCalendarCollection();
        assertTrue(outages.contains("outage1"));
        assertTrue(outages.contains("outage2"));

        // test isInterfaceInOutage
        assertTrue(m_pollerConfig.isInterfaceInOutage("192.168.1.1", "outage1"));
        assertFalse(m_pollerConfig.isInterfaceInOutage("192.168.1.2", "outage1"));
        assertTrue(m_pollerConfig.isInterfaceInOutage("192.168.1.2", "outage2"));
        assertFalse(m_pollerConfig.isInterfaceInOutage("192.168.1.1", "outage2"));

        // test isCurTimeInOutage
        assertTrue(m_pollerConfig.isCurTimeInOutage("outage1"));
        assertFalse(m_pollerConfig.isCurTimeInOutage("outage2"));

        // test isNodeIdInOutage
        assertFalse(m_pollerConfig.isNodeIdInOutage(1, "outage1"));
        assertTrue(m_pollerConfig.isNodeIdInOutage(1, "outage3"));
        assertFalse(m_pollerConfig.isNodeIdInOutage(1, "outage4"));
    }

    private void testServicePoll(MockElement element) throws UnknownHostException {

        element.resetPollCount();
        m_downChecker.resetServiceCount();
        element.bringDown();
        element.visit(m_downChecker);
        assertEquals(m_downChecker.getServiceCount(), element.getPollCount());

        element.resetPollCount();
        m_upChecker.resetServiceCount();
        element.bringUp();
        element.visit(m_upChecker);
        assertEquals(m_upChecker.getServiceCount(), element.getPollCount());

    }

    public void testSetPollStatus() throws Exception {

        // service poll status
        testServicePoll(m_network.getService(1, "192.168.1.1", "SMTP"));
        testServicePoll(m_network.getService(1, "192.168.1.1", "ICMP"));
        testServicePoll(m_network.getService(1, "192.168.1.2", "SMTP"));
        testServicePoll(m_network.getService(2, "192.168.1.3", "HTTP"));

        // interface poll status
        testServicePoll(m_network.getInterface(1, "192.168.1.1"));

        // node poll status
        testServicePoll(m_network.getNode(2));

    }

    public void testVisitor() {
        ElementCounter counter = new ElementCounter();
        m_network.visit(counter);
        assertEquals(1, counter.getNetworkCount());
        assertEquals(3, counter.getNodeCount());
        assertEquals(5, counter.getInterfaceCount());
        assertEquals(9, counter.getServiceCount());
        assertEquals(9, counter.getContainerCount());
        assertEquals(18, counter.getElementCount());
    }

    public void testWaitForEvent() throws Throwable {
        MockNode node = m_network.getNode(1);
        final Event event1 = MockEventUtil.createNodeDownEvent("Test", node);
        final Event event2 = MockEventUtil.createNodeDownEvent("Test", node);
        final Event event3 = MockEventUtil.createNodeDownEvent("Test", m_network.getNode(2));

        EventAnticipator anticipator = m_eventMgr.getEventAnticipator();

        anticipator.anticipateEvent(event1);
        anticipator.anticipateEvent(event3);

        class EventSender extends Thread {
            Throwable m_t = null;

            public void assertSuccess() throws Throwable {
                if (m_t != null)
                    throw m_t;
            }

            public void run() {
                try {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    m_eventMgr.sendNow(event2);
                    m_eventMgr.sendNow(event2);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    m_eventMgr.sendNow(event3);
                } catch (Throwable t) {
                    m_t = t;
                }
            }
        }
        ;
        EventSender eventSender = new EventSender();
        eventSender.start();
        eventSender.assertSuccess();

        assertEquals(1, anticipator.waitForAnticipated(1500).size());
        assertEquals(0, anticipator.waitForAnticipated(1000).size());
        assertEquals(1, anticipator.unanticipatedEvents().size());

    }

    public void testWaitForPoll() throws Throwable {

        final PollAnticipator anticipator = new PollAnticipator();

        // locate the elements and register the pollAnticipator with them
        final MockService smtpService = m_network.getService(1, "192.168.1.1", "SMTP");
        smtpService.resetPollCount();
        smtpService.addAnticipator(anticipator);

        final MockService icmpService = m_network.getService(2, "192.168.1.3", "ICMP");
        icmpService.resetPollCount();
        icmpService.addAnticipator(anticipator);

        final MockInterface iface = m_network.getInterface(1, "192.168.1.2");
        iface.resetPollCount();
        iface.addAnticipator(anticipator);

        anticipator.anticipateAllServices(smtpService);
        anticipator.anticipateAllServices(icmpService);
        anticipator.anticipateAllServices(iface);

        // poll the services
        m_upChecker.resetServiceCount();
        smtpService.visit(m_upChecker);
        icmpService.visit(m_upChecker);
        iface.visit(m_upChecker);

        // poll the icmpService an extra time so we get one unanticipated poll
        icmpService.visit(m_upChecker);

        // assert the the polls all occurred
        assertEquals(0, anticipator.waitForAnticipated(0L).size());
        assertEquals(1, anticipator.unanticipatedPolls().size());

        // reset the anticipator so we can use it again
        anticipator.reset();

        // visit the elements and ensure that we anticpate polls on them
        anticipator.anticipateAllServices(smtpService);
        anticipator.anticipateAllServices(icmpService);
        anticipator.anticipateAllServices(iface);

        // anticipate icmp twice
        anticipator.anticipateAllServices(icmpService);

        smtpService.bringDown();
        icmpService.bringDown();
        iface.bringDown();

        m_downChecker.resetServiceCount();
        class PollerThread extends Thread {
            Throwable m_t = null;

            public void assertSuccess() throws Throwable {
                if (m_t != null)
                    throw m_t;
            }

            public void run() {
                try {
                    smtpService.visit(m_downChecker);
                    icmpService.visit(m_downChecker);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    iface.visit(m_downChecker);
                    icmpService.visit(m_downChecker);
                } catch (Throwable t) {
                    m_t = t;
                }
            }
        }
        ;
        PollerThread pollerThread = new PollerThread();
        pollerThread.start();

        assertEquals(0, anticipator.waitForAnticipated(3000L).size());
        assertEquals(0, anticipator.unanticipatedPolls().size());

        pollerThread.assertSuccess();

        // add one because we polled icmp an extra time
        assertEquals(m_upChecker.getServiceCount(), m_downChecker.getServiceCount());

    }
    

}
