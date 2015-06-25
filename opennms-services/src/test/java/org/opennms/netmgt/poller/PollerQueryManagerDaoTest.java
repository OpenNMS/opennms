/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.Querier;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.eventd.AbstractEventUtil;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.mock.MockElement;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockOutageConfig;
import org.opennms.netmgt.mock.MockPollerConfig;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockService.SvcMgmtStatus;
import org.opennms.netmgt.mock.MockVisitor;
import org.opennms.netmgt.mock.MockVisitorAdapter;
import org.opennms.netmgt.mock.OutageAnticipator;
import org.opennms.netmgt.mock.PollAnticipator;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.poller.pollables.PollableNetwork;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",

        // Override the default QueryManager with the DAO version
        "classpath:/META-INF/opennms/applicationContext-pollerdTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class,reuseDatabase=false)
public class PollerQueryManagerDaoTest implements TemporaryDatabaseAware<MockDatabase> {

	private Poller m_poller;

	private MockNetwork m_network;

	private MockDatabase m_db;

	private MockPollerConfig m_pollerConfig;

	@Autowired
	private MockEventIpcManager m_eventMgr;

	private boolean m_daemonsStarted = false;

	private EventAnticipator m_anticipator;

	private OutageAnticipator m_outageAnticipator;

	@Autowired
	private QueryManager m_queryManager;
	
	@Autowired
	private MonitoredServiceDao m_monitoredServiceDao;

	@Autowired
	private TransactionTemplate m_transactionTemplate;

	//private DemandPollDao m_demandPollDao;

	@Override
	public void setTemporaryDatabase(MockDatabase database) {
		m_db = database;
	}

	//
	// SetUp and TearDown
	//

	@Before
	public void setUp() throws Exception {

		// System.setProperty("mock.logLevel", "DEBUG");
		// System.setProperty("mock.debug", "true");
		MockUtil.println("------------ Begin Test  --------------------------");
		MockLogAppender.setupLogging();

		m_network = new MockNetwork();
		m_network.setCriticalService("ICMP");
		m_network.addNode(1, "Router");
		m_network.addInterface("192.168.1.1");
		m_network.addService("ICMP");
		m_network.addService("SMTP");
		m_network.addService("SNMP");
		m_network.addInterface("192.168.1.2");
		m_network.addService("ICMP");
		m_network.addService("SMTP");
		m_network.addNode(2, "Server");
		m_network.addInterface("192.168.1.3");
		m_network.addService("ICMP");
		m_network.addService("HTTP");
		m_network.addService("SMTP");
		m_network.addService("SNMP");
		m_network.addNode(3, "Firewall");
		m_network.addInterface("192.168.1.4");
		m_network.addService("SMTP");
		m_network.addService("HTTP");
		m_network.addInterface("192.168.1.5");
		m_network.addService("SMTP");
		m_network.addService("HTTP");
		m_network.addNode(4, "DownNode");
		m_network.addInterface("192.168.1.6");
		m_network.addService("SNMP");
//		m_network.addInterface("fe80:0000:0000:0000:0231:f982:0123:4567");
//		m_network.addService("SNMP");

		m_db.populate(m_network);
		DataSourceFactory.setInstance(m_db);

//		DemandPollDao demandPollDao = new DemandPollDaoHibernate(m_db);
//		demandPollDao.setAllocateIdStmt(m_db
//				.getNextSequenceValStatement("demandPollNxtId"));
//		m_demandPollDao = demandPollDao;

		m_pollerConfig = new MockPollerConfig(m_network);
		m_pollerConfig.setNextOutageIdSql(m_db.getNextOutageIdStatement());
		m_pollerConfig.setNodeOutageProcessingEnabled(true);
		m_pollerConfig.setCriticalService("ICMP");
		m_pollerConfig.addPackage("TestPackage");
		m_pollerConfig.addDowntime(1000L, 0L, -1L, false);
		m_pollerConfig.setDefaultPollInterval(1000L);
		m_pollerConfig.populatePackage(m_network);
		m_pollerConfig.addPackage("TestPkg2");
		m_pollerConfig.addDowntime(1000L, 0L, -1L, false);
		m_pollerConfig.setDefaultPollInterval(2000L);
		m_pollerConfig.addService(m_network.getService(2, "192.168.1.3", "HTTP"));

		m_anticipator = new EventAnticipator();
		m_outageAnticipator = new OutageAnticipator(m_db);

		m_eventMgr = new MockEventIpcManager();
		m_eventMgr.setEventWriter(m_db);
		m_eventMgr.setEventAnticipator(m_anticipator);
		m_eventMgr.addEventListener(m_outageAnticipator);
		m_eventMgr.setSynchronous(false);
		
		DefaultPollContext pollContext = new DefaultPollContext();
		pollContext.setEventManager(m_eventMgr);
		pollContext.setLocalHostName("localhost");
		pollContext.setName("Test.DefaultPollContext");
		pollContext.setPollerConfig(m_pollerConfig);
		pollContext.setQueryManager(m_queryManager);
		
		PollableNetwork network = new PollableNetwork(pollContext);

		m_poller = new Poller();
        m_poller.setDataSource(m_db);
        m_poller.setMonitoredServiceDao(m_monitoredServiceDao);
        m_poller.setTransactionTemplate(m_transactionTemplate);
		m_poller.setEventIpcManager(m_eventMgr);
		m_poller.setNetwork(network);
		m_poller.setQueryManager(m_queryManager);
		m_poller.setPollerConfig(m_pollerConfig);
		m_poller.setPollOutagesConfig(m_pollerConfig);

		MockOutageConfig config = new MockOutageConfig();
		config.setGetNextOutageID(m_db.getNextOutageIdStatement());

		// m_outageMgr = new OutageManager();
		// m_outageMgr.setEventMgr(m_eventMgr);
		// m_outageMgr.setOutageMgrConfig(config);
		// m_outageMgr.setDbConnectionFactory(m_db);

	}

	@After
	public void tearDown() throws Exception {
		m_eventMgr.finishProcessingEvents();
		stopDaemons();
		sleep(200);
		m_db.drop();
		MockUtil.println("------------ End Test  --------------------------");
	}

	//
	// Tests
	//
    @Test
    public void testIsRemotePackage() {
    	Properties p = new Properties();
        p.setProperty("org.opennms.netmgt.ConfigFileConstants", "ERROR");
    	MockLogAppender.setupLogging(p);
        Package pkg = new Package();
        pkg.setName("SFO");
        pkg.setRemote(true);
        Poller poller = new Poller();
        assertFalse(poller.pollableServiceInPackage(null, null, pkg));
        poller = null;
    }

//	public void testDemandPollService() {
//		DemandPoll demandPoll = new DemandPoll();
//		demandPoll.setDescription("Test Poll");
//		demandPoll.setRequestTime(new Date());
//		demandPoll.setUserName("admin");
//
//		m_demandPollDao.save(demandPoll);
//
//		assertNotNull(demandPoll.getId());
//
//		MockService httpService = m_network
//				.getService(2, "192.168.1.3", "HTTP");
//		Event demandPollEvent = httpService.createDemandPollEvent(demandPoll.getId());
//
//	}

    @Test
    public void testNullInterfaceOnNodeDown() {
        // NODE processing = true;
        m_pollerConfig.setNodeOutageProcessingEnabled(true);
        MockNode node = m_network.getNode(2);
        MockService icmpService = m_network.getService(2, "192.168.1.3", "ICMP");
        MockService smtpService = m_network.getService(2, "192.168.1.3", "SMTP");
        MockService snmpService = m_network.getService(2, "192.168.1.3", "SNMP");

        // start the poller
        startDaemons();

        anticipateDown(node);

        icmpService.bringDown();
        smtpService.bringDown();
        snmpService.bringDown();

        verifyAnticipated(10000);

        // node is down at this point
        boolean foundNodeDown = false;
        for (final Event event : m_anticipator.getAnticipatedEventsRecieved()) {
            if (EventConstants.NODE_DOWN_EVENT_UEI.equals(event.getUei())) {
                foundNodeDown = true;
                assertNull(event.getInterfaceAddress());
            }
        }
        assertTrue(foundNodeDown);
    }

    @Test
    @Ignore
	public void testBug1564() {
		// NODE processing = true;
		m_pollerConfig.setNodeOutageProcessingEnabled(true);
		MockNode node = m_network.getNode(2);
		MockService icmpService = m_network.getService(2, "192.168.1.3", "ICMP");
		MockService smtpService = m_network.getService(2, "192.168.1.3", "SMTP");
		MockService snmpService = m_network.getService(2, "192.168.1.3", "SNMP");

		// start the poller
		startDaemons();

		//
		// Bring Down the HTTP service and expect nodeLostService Event
		//

		resetAnticipated();
		anticipateDown(snmpService);
		// One service works fine
		snmpService.bringDown();

		verifyAnticipated(10000);

		// Now we simulate the restart, the node
		// looses all three at the same time

		resetAnticipated();
		anticipateDown(node);

		icmpService.bringDown();
		smtpService.bringDown();
		snmpService.bringDown();

		verifyAnticipated(10000);
		
		anticipateDown(smtpService);
		verifyAnticipated(10000);
		anticipateDown(snmpService);
		verifyAnticipated(10000);

		// This is to simulate a restart,
		// where I turn off the node behaviour

		m_pollerConfig.setNodeOutageProcessingEnabled(false);

		anticipateUp(snmpService);
		snmpService.bringUp();

		verifyAnticipated(10000);

		anticipateUp(smtpService);
		smtpService.bringUp();

		verifyAnticipated(10000);

		// Another restart - let's see if this will work?

		m_pollerConfig.setNodeOutageProcessingEnabled(true);
		// So everything is down, now
		// SNMP will regain and SMTP will regain
		// will the node come up?

		
		smtpService.bringDown();
		
		anticipateUp(smtpService);
		smtpService.bringUp();

		verifyAnticipated(10000,true);

		anticipateUp(snmpService);
		snmpService.bringUp();

		verifyAnticipated(10000);

	}

    @Test
	public void testBug709() {

		m_pollerConfig.setNodeOutageProcessingEnabled(true);

		MockNode node = m_network.getNode(2);
		MockService icmpService = m_network
				.getService(2, "192.168.1.3", "ICMP");
		MockService httpService = m_network
				.getService(2, "192.168.1.3", "HTTP");

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
		// Bring Down the ICMP (on the only interface on the node) now expect
		// nodeDown
		// only.
		//

		resetAnticipated();
		anticipateDown(node);

		// bring down the ICMP service
		icmpService.bringDown();

		// make sure the down events are received
		// verifyAnticipated(10000);
		sleep(5000);
		//
		// Bring up both the node and the httpService at the same time. Expect
		// both a nodeUp and a nodeRegainedService
		//

		resetAnticipated();
		// the order matters here
		anticipateUp(httpService);
		anticipateUp(node);

		// bring up all the services on the node
		node.bringUp();

		// make sure the down events are received
		verifyAnticipated(10000);

	}

	private void resetAnticipated() {
		m_anticipator.reset();
		m_outageAnticipator.reset();
	}

    @Test
	public void testNodeLostServiceWithReason() {
		m_pollerConfig.setNodeOutageProcessingEnabled(true);

		MockService svc = m_network.getService(1, "192.168.1.1", "ICMP");
		Event e = svc.createDownEvent();
		String reasonParm = "eventReason";
		String val = (String) AbstractEventUtil.getInstance().getNamedParmValue("parm[" + reasonParm + "]", e);
		assertEquals("Service Not Responding.", val);
	}

    @Test
	public void testCritSvcStatusPropagation() {
		m_pollerConfig.setNodeOutageProcessingEnabled(true);

		MockNode node = m_network.getNode(1);

		anticipateDown(node);

		startDaemons();

		bringDownCritSvcs(node);

		verifyAnticipated(8000);
	}

    @Test
	public void testInterfaceWithNoCriticalService() {
		m_pollerConfig.setNodeOutageProcessingEnabled(true);

		MockInterface iface = m_network.getInterface(3, "192.168.1.4");
		MockService svc = iface.getService("SMTP");
		MockService otherService = iface.getService("HTTP");

		startDaemons();

		anticipateDown(iface);

		iface.bringDown();

		verifyAnticipated(8000);

		anticipateUp(iface);
		anticipateDown(otherService, true);

		svc.bringUp();

		verifyAnticipated(8000);

	}

	// what about scheduled outages?
    @Test
	public void testDontPollDuringScheduledOutages() {
		long start = System.currentTimeMillis();

		MockInterface iface = m_network.getInterface(1, "192.168.1.2");
		m_pollerConfig.addScheduledOutage(m_pollerConfig.getPackage("TestPackage"), "TestOutage", start, start + 5000, iface.getIpAddr());
		MockUtil.println("Begin Outage");
		startDaemons();

		long now = System.currentTimeMillis();
		sleep(3000 - (now - start));

		MockUtil.println("End Outage");
		assertEquals(0, iface.getPollCount());

		sleep(5000);

		assertTrue(0 < iface.getPollCount());

	}

	// Test harness that tests any type of node, interface or element.
	private void testElementDeleted(MockElement element) {
		Event deleteEvent = element.createDeleteEvent();
		m_pollerConfig.setNodeOutageProcessingEnabled(false);

		PollAnticipator poll = new PollAnticipator();
		element.addAnticipator(poll);

		poll.anticipateAllServices(element);

		startDaemons();

		// wait til after the first poll of the services
		poll.waitForAnticipated(1000L);

		// now delete the node and send a nodeDeleted event
		m_network.removeElement(element);
		m_eventMgr.sendEventToListeners(deleteEvent);

		// reset the poll count and wait to see if any polls on the removed
		// element happened
		m_network.resetInvalidPollCount();

		// now ensure that no invalid polls have occurred
		sleep(3000);

		assertEquals("Received a poll for an element that doesn't exist", 0, m_network.getInvalidPollCount());

	}

	// serviceDeleted: EventConstants.SERVICE_DELETED_EVENT_UEI
    @Test
	public void testServiceDeleted() {
		MockService svc = m_network.getService(1, "192.168.1.1", "SMTP");
		testElementDeleted(svc);
	}

	// interfaceDeleted: EventConstants.INTERFACE_DELETED_EVENT_UEI
    @Test
	public void testInterfaceDeleted() {
		MockInterface iface = m_network.getInterface(1, "192.168.1.1");
		testElementDeleted(iface);
	}

	// nodeDeleted: EventConstants.NODE_DELETED_EVENT_UEI
    @Test
	public void testNodeDeleted() {
		MockNode node = m_network.getNode(1);
		testElementDeleted(node);
	}

    // nodeLabelChanged: EventConstants.NODE_LABEL_CHANGED_EVENT_UEI
    @Test
    public void testNodeLabelChanged() {
        MockNode element = m_network.getNode(1);
        String newLabel = "NEW LABEL";
        Event event = element.createNodeLabelChangedEvent(newLabel);
        m_pollerConfig.setNodeOutageProcessingEnabled(false);

        PollAnticipator poll = new PollAnticipator();
        element.addAnticipator(poll);

        poll.anticipateAllServices(element);

        startDaemons();

        // wait until after the first poll of the services
        poll.waitForAnticipated(1000L);

        assertEquals("Router", m_poller.getNetwork().getNode(1).getNodeLabel());

        // now delete the node and send a nodeDeleted event
        element.setLabel(newLabel);
        m_eventMgr.sendEventToListeners(event);

        assertEquals(newLabel, m_poller.getNetwork().getNode(1).getNodeLabel());
    }

	public void testOutagesClosedOnDelete(MockElement element) {

		startDaemons();

		Event deleteEvent = element.createDeleteEvent();

		// bring down so we create an outage in the outages table
		anticipateDown(element);
		element.bringDown();
		verifyAnticipated(5000, false);

		m_outageAnticipator.anticipateOutageClosed(element, deleteEvent);

		// now delete the service
		m_eventMgr.sendEventToListeners(deleteEvent);
		m_network.removeElement(element);
		

		verifyAnticipated(5000);

	}

    @Test
	public void testServiceOutagesClosedOnDelete() {
		MockService element = m_network.getService(1, "192.168.1.1", "SMTP");
		testOutagesClosedOnDelete(element);

	}

    @Test
	public void testInterfaceOutagesClosedOnDelete() {
		MockInterface element = m_network.getInterface(1, "192.168.1.1");
		testOutagesClosedOnDelete(element);
	}

    @Test
	public void testNodeOutagesClosedOnDelete() {
		MockNode element = m_network.getNode(1);
		testOutagesClosedOnDelete(element);
	}

	// interfaceReparented: EventConstants.INTERFACE_REPARENTED_EVENT_UEI
    @Test
	public void testInterfaceReparented() throws Exception {

		m_pollerConfig.setNodeOutageProcessingEnabled(true);

		MockNode node1 = m_network.getNode(1);
		MockNode node2 = m_network.getNode(2);

		assertNotNull("Node 1 should have 192.168.1.1", node1.getInterface("192.168.1.1"));
		assertNotNull("Node 1 should have 192.168.1.2", node1.getInterface("192.168.1.2"));
		
		assertNull("Node 2 should not yet have 192.168.1.2", node2.getInterface("192.168.1.2"));
		assertNotNull("Node 2 should have 192.168.1.3", node2.getInterface("192.168.1.3"));

		MockInterface dotTwo = m_network.getInterface(1, "192.168.1.2");
		MockInterface dotThree = m_network.getInterface(2, "192.168.1.3");

		Event reparentEvent = MockEventUtil.createReparentEvent("Test", "192.168.1.2", 1, 2);

		// we are going to reparent to node 2 so when we bring down its only
		// current interface we expect an interface down not the whole node.
		anticipateDown(dotThree);

		startDaemons();

		final int waitTime = 2000;
		final int verifyTime = 2000;

		sleep(waitTime);

		// move the reparented interface and send a reparented event
		dotTwo.moveTo(node2);
		m_db.reparentInterface(dotTwo.getIpAddr(), node1.getNodeId(), node2.getNodeId());

		// send the reparent event to the daemons
		m_eventMgr.sendEventToListeners(reparentEvent);

		sleep(waitTime);

		// now bring down the other interface on the new node
		// System.err.println("Bring Down:"+node2Iface);
		dotThree.bringDown();

		verifyAnticipated(verifyTime);

		resetAnticipated();
		anticipateDown(node2);

		// System.err.println("Bring Down:"+reparentedIface);
		dotTwo.bringDown();

		sleep(waitTime);

		verifyAnticipated(verifyTime);

		node1 = m_network.getNode(1);
		node2 = m_network.getNode(2);

		assertNotNull("Node 1 should still have 192.168.1.1", node1.getInterface("192.168.1.1"));
		assertNull("Node 1 should no longer have 192.168.1.2", node1.getInterface("192.168.1.2"));
		
		assertNotNull("Node 2 should now have 192.168.1.2", node2.getInterface("192.168.1.2"));
		assertNotNull("Node 2 should still have 192.168.1.3", node2.getInterface("192.168.1.3"));
	}

	// test to see that node lost/regained service events come in
    @Test
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
    @Test
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
    
    @Test
    public void testNodeLostServiceIncludesReason() throws Exception {
        MockService element = m_network.getService(1, "192.168.1.1", "SMTP");
        String expectedReason = "Oh No!! An Outage!!";
        startDaemons();
        
        resetAnticipated();
        anticipateDown(element);
        
        MockUtil.println("Bringing down element: " + element);
        element.bringDown(expectedReason);
        MockUtil.println("Finished bringing down element: " + element);
        
        verifyAnticipated(8000);

        Collection<Event> receivedEvents = m_anticipator.getAnticipatedEventsRecieved();

        assertEquals(2, receivedEvents.size());
        
        Iterator<Event> receivedEventsIter= receivedEvents.iterator();
        Event event1 = receivedEventsIter.next();
        
        assertEquals(expectedReason, EventUtils.getParm(event1, EventConstants.PARM_LOSTSERVICE_REASON));
        
        Event event2 = receivedEventsIter.next();
        
        assertNotNull(event2);
        assertEquals(EventConstants.OUTAGE_CREATED_EVENT_UEI,event2.getUei());
        assertEquals("SMTP",event2.getService());
        assertEquals("192.168.1.1",event2.getInterface());
        
    }

    @Test
	public void testNodeLostRegainedService() throws Exception {

        testElementDownUp(m_network.getService(1, "192.168.1.1", "SMTP"));

	}

    @Test
	public void testInterfaceDownUp() {

		testElementDownUp(m_network.getInterface(1, "192.168.1.1"));
	}

    @Test
	public void testNodeDownUp() {
		testElementDownUp(m_network.getNode(1));
	}

	private void testElementDownUp(MockElement element) {
		startDaemons();

		resetAnticipated();
		anticipateDown(element);

		MockUtil.println("Bringing down element: " + element);
		element.bringDown();
		MockUtil.println("Finished bringing down element: " + element);

		verifyAnticipated(5000);

		sleep(2000);

		resetAnticipated();
		anticipateUp(element);

		MockUtil.println("Bringing up element: " + element);
		element.bringUp();
		MockUtil.println("Finished bringing up element: " + element);

		verifyAnticipated(8000);
	}

    @Test
	public void testNoEventsOnNoOutages() throws Exception {

		testElementDownUp(m_network.getService(1, "192.168.1.1", "SMTP"));

		resetAnticipated();
		verifyAnticipated(8000, true);

	}

    @Test
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
		assertEquals(0, anticipator.waitForAnticipated(4500L).size());


	}

    // test open outages for unmanaged services
    @Test
    public void testUnmangedWithOpenOutageAtStartup() throws InterruptedException {
        // before we start we need to initialize the database

        // create an outage for the service
        MockService svc = m_network.getService(1, "192.168.1.1", "SMTP");
        MockInterface iface = m_network.getInterface(1, "192.168.1.2");

        Event svcLostEvent = MockEventUtil.createNodeLostServiceEvent("Test", svc);
        m_db.writeEvent(svcLostEvent);
        createOutages(svc, svcLostEvent);

        Event ifaceDownEvent = MockEventUtil.createInterfaceDownEvent("Test", iface);
        m_db.writeEvent(ifaceDownEvent);
        createOutages(iface, ifaceDownEvent);

        // mark the service as unmanaged
        m_db.setServiceStatus(svc, 'U');
        m_db.setInterfaceStatus(iface, 'U');

        // assert that we have an open outage
        assertEquals(1, m_db.countOpenOutagesForService(svc));
        assertEquals(1, m_db.countOutagesForService(svc));

        assertEquals(iface.getServices().size(), m_db.countOutagesForInterface(iface));
        assertEquals(iface.getServices().size(), m_db.countOpenOutagesForInterface(iface));

        startDaemons();
        
        // assert that we have no open outages
        assertEquals(0, m_db.countOpenOutagesForService(svc));
        assertEquals(1, m_db.countOutagesForService(svc));

        assertEquals(0, m_db.countOpenOutagesForInterface(iface));
        assertEquals(iface.getServices().size(), m_db
                .countOutagesForInterface(iface));

    }
    
    @Test
    public void testNodeGainedServiceWhileNodeDownAndServiceUp() {
        
        startDaemons();
        
        MockNode node = m_network.getNode(4);
        MockService svc = m_network.getService(4, "192.168.1.6", "SNMP");
        
        anticipateDown(node);
        
        node.bringDown();
        
        verifyAnticipated(5000);
        
        resetAnticipated();
        
        anticipateUp(node);
        anticipateDown(svc, true);
        
        MockService newSvc = m_network.addService(4, "192.168.1.6", "SMTP");
        
        m_db.writeService(newSvc);
        
        Event e = MockEventUtil.createNodeGainedServiceEvent("Test", newSvc);
        m_eventMgr.sendEventToListeners(e);
        
        sleep(5000);
        System.err.println(m_db.getOutages());
        
        verifyAnticipated(8000);
        
        
    }

    @Test
    public void testNodeGainedServiceWhileNodeDownAndServiceDown() {
        
        startDaemons();
        
        MockNode node = m_network.getNode(4);
        MockService svc = m_network.getService(4, "192.168.1.6", "SNMP");
        
        anticipateDown(node);
        
        node.bringDown();
        
        verifyAnticipated(5000);
        
        resetAnticipated();
        
        MockService newSvc = m_network.addService(4, "192.168.1.6", "SMTP");
        
        m_db.writeService(newSvc);
        
        newSvc.bringDown();
        
        Event e = MockEventUtil.createNodeGainedServiceEvent("Test", newSvc);
        m_eventMgr.sendEventToListeners(e);
        
        sleep(5000);
        System.err.println(m_db.getOutages());
        
        verifyAnticipated(8000);
        
        anticipateUp(node);
        anticipateDown(svc, true);
        
        newSvc.bringUp();
        
        verifyAnticipated(5000);
        
        
    }

    // test open outages for unmanaged services
    @Test
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

		Event reparentEvent = MockEventUtil.createReparentEvent("Test",
				"192.168.1.2", 1, 2);

		startDaemons();

		verifyAnticipated(2000);

		m_db.reparentInterface(dotTwo.getIpAddr(), dotTwo.getNodeId(), node2
				.getNodeId());
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
    @Test
	public void testSendNodeGainedService() {
		m_pollerConfig.setNodeOutageProcessingEnabled(false);

		startDaemons();

		testSendNodeGainedService("SMTP", "HTTP");
	}

    @Test
    public void testSendNodeGainedServiceNodeOutages() {
        m_pollerConfig.setNodeOutageProcessingEnabled(true);

        startDaemons();

        testSendNodeGainedService("SMTP", "HTTP");
    }

    @Test
	public void testSendIPv6NodeGainedService() {
		m_pollerConfig.setNodeOutageProcessingEnabled(false);

		startDaemons();

        testSendNodeGainedServices(99, "TestNode", "fe80:0000:0000:0000:0231:f982:0123:4567", new String[] { "SMTP", "HTTP" });
	}

    @Test
    public void testSendIPv6NodeGainedServiceNodeOutages() {
        m_pollerConfig.setNodeOutageProcessingEnabled(true);

        startDaemons();
        testSendNodeGainedServices(99, "TestNode", "fe80:0000:0000:0000:0231:f982:0123:4567", new String[] { "SMTP", "HTTP" });
    }

	public void testSendNodeGainedService(String... svcNames) {
        testSendNodeGainedServices(99, "TestNode", "10.1.1.1", svcNames);
	}

    private void testSendNodeGainedServices(int nodeid, String nodeLabel, String ipAddr, String... svcNames) {
        assertNotNull(svcNames);
	    assertTrue(svcNames.length > 0);

        MockNode node = m_network.addNode(nodeid, nodeLabel);
		m_db.writeNode(node);
        MockInterface iface = m_network.addInterface(nodeid, ipAddr);
		m_db.writeInterface(iface);
		
		List<MockService> services = new ArrayList<MockService>();
		for(String svcName : svcNames) {
		    MockService svc = m_network.addService(nodeid, ipAddr, svcName);
		    m_db.writeService(svc);
		    m_pollerConfig.addService(svc);
		    services.add(svc);
		}
		
		MockVisitor gainSvcSender = new MockVisitorAdapter() {
                        @Override
			public void visitService(MockService svc) {
				Event event = MockEventUtil
						.createNodeGainedServiceEvent("Test", svc);
				m_eventMgr.sendEventToListeners(event);
			}
		};
		node.visit(gainSvcSender);
		
		MockService svc1 = services.get(0);

		PollAnticipator anticipator = new PollAnticipator();
		svc1.addAnticipator(anticipator);

		anticipator.anticipateAllServices(svc1);

        StringBuffer didNotOccur = new StringBuffer();
		for (MockService service : anticipator.waitForAnticipated(10000)) {
		    didNotOccur.append(service.toString());
		}
        StringBuffer unanticipatedStuff = new StringBuffer();
        for (MockService service : anticipator.unanticipatedPolls()) {
            unanticipatedStuff.append(service.toString());
        }
		
		assertEquals(unanticipatedStuff.toString(), "", didNotOccur.toString());

		anticipateDown(svc1);

		svc1.bringDown();

		verifyAnticipated(10000);
	}

	@Test
	public void testSuspendPollingResumeService() {

		MockService svc = m_network.getService(1, "192.168.1.2", "SMTP");

		startDaemons();

		sleep(2000);
		assertTrue(0 < svc.getPollCount());

		m_eventMgr.sendEventToListeners(MockEventUtil
				.createSuspendPollingServiceEvent("Test", svc));
		svc.resetPollCount();

		sleep(5000);
		assertEquals(0, svc.getPollCount());

		m_eventMgr.sendEventToListeners(MockEventUtil
				.createResumePollingServiceEvent("Test", svc));

		sleep(2000);
		assertTrue(0 < svc.getPollCount());

	}

	//
	// Utility methods
	//

	private void startDaemons() {
		// m_outageMgr.init();
		m_poller.init();
		// m_outageMgr.start();
		m_poller.start();
		m_daemonsStarted = true;
	}

	private void stopDaemons() {
		if (m_daemonsStarted) {
			m_poller.stop();
			// m_outageMgr.stop();
			m_daemonsStarted = false;
		}
	}

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (final InterruptedException e) {
		}
	}

	private void verifyAnticipated(long millis) {
		verifyAnticipated(millis, true);
	}

	private void verifyAnticipated(long millis, boolean checkUnanticipated) {
		// make sure the down events are received
		MockEventUtil.printEvents("Events we're still waiting for: ", m_anticipator.waitForAnticipated(millis));
		assertTrue("Expected events not forthcoming", m_anticipator.waitForAnticipated(0).isEmpty());
		if (checkUnanticipated) {
			sleep(2000);
			MockEventUtil.printEvents("Unanticipated: ", m_anticipator.unanticipatedEvents());
			assertEquals("Received unexpected events", 0, m_anticipator.unanticipatedEvents().size());
		}
		sleep(1000);
		m_eventMgr.finishProcessingEvents();
		assertEquals("Wrong number of outages opened", m_outageAnticipator.getExpectedOpens(), m_outageAnticipator.getActualOpens());
		assertEquals("Wrong number of outages in outage table", m_outageAnticipator.getExpectedOutages(), m_outageAnticipator.getActualOutages());
		assertTrue("Created outages don't match the expected outages", m_outageAnticipator.checkAnticipated());
	}

	private void anticipateUp(MockElement element) {
		anticipateUp(element, false);
	}

	private void anticipateUp(MockElement element, boolean force) {
		if (force || !element.getPollStatus().equals(PollStatus.up())) {
			Event event = element.createUpEvent();
			m_anticipator.anticipateEvent(event);
			for (Event outageResolvedEvent: m_outageAnticipator.anticipateOutageClosed(element, event)){
			    m_anticipator.anticipateEvent(outageResolvedEvent);
			}
		}
	}

	private void anticipateDown(MockElement element) {
		anticipateDown(element, false);
	}

	private void anticipateDown(MockElement element, boolean force) {
		if (force || !element.getPollStatus().equals(PollStatus.down())) {
			Event event = element.createDownEvent();
			m_anticipator.anticipateEvent(event);
			for (Event outageCreatedEvent: m_outageAnticipator.anticipateOutageOpened(element, event)) {
			    m_anticipator.anticipateEvent(outageCreatedEvent);
			}
		}
	}

	private void anticipateServicesUp(MockElement node) {
		MockVisitor eventCreator = new MockVisitorAdapter() {
                        @Override
			public void visitService(MockService svc) {
				anticipateUp(svc);
			}
		};
		node.visit(eventCreator);
	}

	private void anticipateServicesDown(MockElement node) {
		MockVisitor eventCreator = new MockVisitorAdapter() {
                        @Override
			public void visitService(MockService svc) {
				anticipateDown(svc);
			}
		};
		node.visit(eventCreator);
	}

	private void createOutages(MockElement element, final Event event) {
		MockVisitor outageCreater = new MockVisitorAdapter() {
                        @Override
			public void visitService(MockService svc) {
			    if (svc.getMgmtStatus().equals(SvcMgmtStatus.ACTIVE)) {
			        m_db.createOutage(svc, event);
			    }
			}
		};
		element.visit(outageCreater);
	}

	private void bringDownCritSvcs(MockElement element) {
		MockVisitor markCritSvcDown = new MockVisitorAdapter() {
                        @Override
			public void visitService(MockService svc) {
				if ("ICMP".equals(svc.getSvcName())) {
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

		OutageChecker(MockService svc, Event lostSvcEvent,
				Event regainedSvcEvent) {
			super(m_db,
					"select * from outages where nodeid = ? and ipAddr = ? and serviceId = ?");

			m_svc = svc;
			m_lostSvcEvent = lostSvcEvent;
			m_lostSvcTime = new Timestamp(m_lostSvcEvent.getTime().getTime());
			m_regainedSvcEvent = regainedSvcEvent;
			if (m_regainedSvcEvent != null) {
				m_regainedSvcTime = new Timestamp(m_regainedSvcEvent.getTime().getTime());
			}
		}

                @Override
		public void processRow(ResultSet rs) throws SQLException {
			assertEquals(m_svc.getNodeId(), rs.getInt("nodeId"));
			assertEquals(m_svc.getIpAddr(), rs.getString("ipAddr"));
			assertEquals(m_svc.getSvcId(), rs.getInt("serviceId"));
			assertEquals(m_lostSvcEvent.getDbid(), Integer.valueOf(rs.getInt("svcLostEventId")));
			assertEquals(m_lostSvcTime, rs.getTimestamp("ifLostService"));
			assertEquals(getRegainedEventId(), rs
					.getObject("svcRegainedEventId"));
			assertEquals(m_regainedSvcTime, rs
					.getTimestamp("ifRegainedService"));
		}

		private Integer getRegainedEventId() {
			if (m_regainedSvcEvent == null)
				return null;
			return Integer.valueOf(m_regainedSvcEvent.getDbid());
		}
	}

	// TODO: test multiple polling packages

	// TODO: test overlapping polling packages

	// TODO: test two packages both with the crit service and status propagation

	// TODO: how does unmanaging a node/iface/service work with the poller

	// TODO: test over lapping poll outages


}
