//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 May 06: Make tests work with plugin management and database
//              synchronization pulled out of CapsdConfigManager.
//              Move TestCapsdConfigManager inner class to its own
//              class in org.opennms.netmgt.mock. - dj@opennms.org
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

import org.opennms.netmgt.capsd.JdbcCapsdDbSyncer;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.dao.support.RrdTestUtils;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockElement;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockOutageConfig;
import org.opennms.netmgt.mock.MockPollerConfig;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockVisitor;
import org.opennms.netmgt.mock.MockVisitorAdapter;
import org.opennms.netmgt.mock.OutageAnticipator;
import org.opennms.netmgt.mock.PollAnticipator;
import org.opennms.netmgt.mock.TestCapsdConfigManager;
import org.opennms.netmgt.mock.MockService.SvcMgmtStatus;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.utils.Querier;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xmlrpcd.OpenNMSProvisioner;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.jdbc.core.JdbcTemplate;

public class PollerTest extends TestCase {
    private static final String CAPSD_CONFIG = "\n"
            + "<capsd-configuration max-suspect-thread-pool-size=\"2\" max-rescan-thread-pool-size=\"3\"\n"
            + "   delete-propagation-enabled=\"true\">\n"
            + "   <protocol-plugin protocol=\"ICMP\" class-name=\"org.opennms.netmgt.capsd.plugins.LdapPlugin\"/>\n"
            + "   <protocol-plugin protocol=\"SMTP\" class-name=\"org.opennms.netmgt.capsd.plugins.LdapPlugin\"/>\n"
            + "   <protocol-plugin protocol=\"HTTP\" class-name=\"org.opennms.netmgt.capsd.plugins.LdapPlugin\"/>\n"
            + "</capsd-configuration>\n";

	private Poller m_poller;

	private MockNetwork m_network;

	private MockDatabase m_db;

	private MockPollerConfig m_pollerConfig;

	private MockEventIpcManager m_eventMgr;

	private boolean m_daemonsStarted = false;

	private EventAnticipator m_anticipator;

	private OutageAnticipator m_outageAnticipator;

	//private DemandPollDao m_demandPollDao;

	//
	// SetUp and TearDown
	//

    @Override
	protected void setUp() throws Exception {
        super.setUp();
        
		// System.setProperty("mock.logLevel", "DEBUG");
		// System.setProperty("mock.debug", "true");
		MockUtil.println("------------ Begin Test " + getName()
				+ " --------------------------");
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

		m_db = new MockDatabase();
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
		m_pollerConfig.addService(m_network
				.getService(2, "192.168.1.3", "HTTP"));

		m_anticipator = new EventAnticipator();
		m_outageAnticipator = new OutageAnticipator(m_db);

		m_eventMgr = new MockEventIpcManager();
		m_eventMgr.setEventWriter(m_db);
		m_eventMgr.setEventAnticipator(m_anticipator);
		m_eventMgr.addEventListener(m_outageAnticipator);
		m_eventMgr.setSynchronous(false);

		m_poller = new Poller();
		m_poller.setEventManager(m_eventMgr);
		m_poller.setDbConnectionFactory(m_db);
		m_poller.setPollerConfig(m_pollerConfig);
		m_poller.setPollOutagesConfig(m_pollerConfig);

		MockOutageConfig config = new MockOutageConfig();
		config.setGetNextOutageID(m_db.getNextOutageIdStatement());
		
		RrdTestUtils.initializeNullStrategy();

		// m_outageMgr = new OutageManager();
		// m_outageMgr.setEventMgr(m_eventMgr);
		// m_outageMgr.setOutageMgrConfig(config);
		// m_outageMgr.setDbConnectionFactory(m_db);

	}

	public void tearDown() throws Exception {
		m_eventMgr.finishProcessingEvents();
		stopDaemons();
		sleep(200);
		MockLogAppender.assertNoWarningsOrGreater();
		DataSourceFactory.setInstance(null);
		m_db.drop();
		MockUtil.println("------------ End Test " + getName()
				+ " --------------------------");
	}

	//
	// Tests
	//
    
    public void testIsRemotePackage() {
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

	public void FIXMEtestBug1564() {
		// NODE processing = true;
		m_pollerConfig.setNodeOutageProcessingEnabled(true);
		MockNode node = m_network.getNode(2);
		MockService icmpService = m_network
				.getService(2, "192.168.1.3", "ICMP");
		MockService smtpService = m_network
				.getService(2, "192.168.1.3", "SMTP");
		MockService snmpService = m_network
				.getService(2, "192.168.1.3", "SNMP");

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

	public void testNodeLostServiceWithReason() {
		m_pollerConfig.setNodeOutageProcessingEnabled(true);

		MockService svc = m_network.getService(1, "192.168.1.1", "ICMP");
		Event e = svc.createDownEvent();
		String reasonParm = "eventReason";
		String val = EventUtil.getNamedParmValue("parm[" + reasonParm + "]", e);
		assertEquals("Service Not Responding.", val);
	}

	public void testCritSvcStatusPropagation() {
		m_pollerConfig.setNodeOutageProcessingEnabled(true);

		MockNode node = m_network.getNode(1);

		anticipateDown(node);

		startDaemons();

		bringDownCritSvcs(node);

		verifyAnticipated(8000);
	}

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
	public void testDontPollDuringScheduledOutages() {
		long start = System.currentTimeMillis();

		MockInterface iface = m_network.getInterface(1, "192.168.1.2");
		m_pollerConfig.addScheduledOutage(m_pollerConfig
				.getPackage("TestPackage"), "TestOutage", start, start + 5000,
				iface.getIpAddr());
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

		assertEquals("Received a poll for an element that doesn't exist", 0,
				m_network.getInvalidPollCount());

	}

	// serviceDeleted: EventConstants.SERVICE_DELETED_EVENT_UEI
	public void testServiceDeleted() {
		MockService svc = m_network.getService(1, "192.168.1.1", "SMTP");
		testElementDeleted(svc);
	}

	// interfaceDeleted: EventConstants.INTERFACE_DELETED_EVENT_UEI
	public void testInterfaceDeleted() {
		MockInterface iface = m_network.getInterface(1, "192.168.1.1");
		testElementDeleted(iface);
	}

	// nodeDeleted: EventConstants.NODE_DELETED_EVENT_UEI
	public void testNodeDeleted() {
		MockNode node = m_network.getNode(1);
		testElementDeleted(node);
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

	public void testServiceOutagesClosedOnDelete() {
		MockService element = m_network.getService(1, "192.168.1.1", "SMTP");
		testOutagesClosedOnDelete(element);

	}

	public void testInterfaceOutagesClosedOnDelete() {
		MockInterface element = m_network.getInterface(1, "192.168.1.1");
		testOutagesClosedOnDelete(element);
	}

	public void testNodeOutagesClosedOnDelete() {
		MockNode element = m_network.getNode(1);
		testOutagesClosedOnDelete(element);
	}

	// interfaceReparented: EventConstants.INTERFACE_REPARENTED_EVENT_UEI
	public void testInterfaceReparented() {

		m_pollerConfig.setNodeOutageProcessingEnabled(true);

		MockNode node1 = m_network.getNode(1);
		MockNode node2 = m_network.getNode(2);

		MockInterface dotTwo = m_network.getInterface(1, "192.168.1.2");
		MockInterface dotThree = m_network.getInterface(2, "192.168.1.3");

		Event reparentEvent = MockEventUtil.createReparentEvent("Test",
				"192.168.1.2", 1, 2);

		// we are going to reparent to node 2 so when we bring down its only
		// current interface we expect an interface down not the whole node.
		anticipateDown(dotThree);

		startDaemons();

		sleep(2000);

		// move the reparted interface and send a reparented event
		dotTwo.moveTo(node2);
		m_db.reparentInterface(dotTwo.getIpAddr(), node1.getNodeId(), node2
				.getNodeId());

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

		// sleep(5000);

		verifyAnticipated(2000);

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

	public void testNoEventsOnNoOutages() throws Exception {

		testElementDownUp(m_network.getService(1, "192.168.1.1", "SMTP"));

		resetAnticipated();
		verifyAnticipated(8000, true);

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
		assertEquals(0, anticipator.waitForAnticipated(4500L).size());


	}

    // test open outages for unmanaged services
    public void testUnmangedWithOpenOutageAtStartup() {
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

        assertEquals(iface.getServices().size(), m_db
                .countOutagesForInterface(iface));
        assertEquals(iface.getServices().size(), m_db
                .countOpenOutagesForInterface(iface));

        startDaemons();

        // assert that we have no open outages
        assertEquals(0, m_db.countOpenOutagesForService(svc));
        assertEquals(1, m_db.countOutagesForService(svc));

        assertEquals(0, m_db.countOpenOutagesForInterface(iface));
        assertEquals(iface.getServices().size(), m_db
                .countOutagesForInterface(iface));

    }
    
    public void testNodeGainedServiceWhileNodeAndServiceDown() {
        
        // get node 2... bring it down
        // when the poller knows its down add down service and send nodeGainedService event
        // expect?
        
        startDaemons();
        
        MockNode node = m_network.getNode(4);
        
        anticipateDown(node);
        
        node.bringDown();
        
        verifyAnticipated(5000);
        
        resetAnticipated();
        
        MockService svc = m_network.addService(4, "192.168.1.6", "SMTP");
        
        svc.bringDown();
        
        m_db.writeService(svc);
        
        Event e = MockEventUtil.createNodeGainedServiceEvent("Test", svc);
        m_eventMgr.sendEventToListeners(e);
        
        
        sleep(5000);
        
        System.err.println(m_db.getOutages());
        verifyAnticipated(8000);

        
        
//        MockNode node = m_network.addNode(99, "TestNode");
//        m_db.writeNode(node);
//        MockInterface iface = m_network.addInterface(99, "10.1.1.1");
//        m_db.writeInterface(iface);
//        MockService element = m_network.addService(99, "10.1.1.1", svcName);
//        m_db.writeService(element);
//        m_pollerConfig.addService(element);
//        MockService smtp = m_network.addService(99, "10.1.1.1", "HTTP");
//        m_db.writeService(smtp);
//        m_pollerConfig.addService(smtp);
//
//        MockVisitor gainSvcSender = new MockVisitorAdapter() {
//            public void visitService(MockService svc) {
//                Event event = MockEventUtil
//                        .createNodeGainedServiceEvent("Test", svc);
//                m_eventMgr.sendEventToListeners(event);
//            }
//        };
//        node.visit(gainSvcSender);
//
//        PollAnticipator anticipator = new PollAnticipator();
//        element.addAnticipator(anticipator);
//
//        anticipator.anticipateAllServices(element);
//
//        assertEquals(0, anticipator.waitForAnticipated(10000).size());
//
//        anticipateDown(element);
//
//        element.bringDown();
//
//        verifyAnticipated(10000);

    }

    // test open outages for unmanaged services
    public void testMissingOutagesOnStartup() {
        
        //fail("we need to write this one still");
        
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

        assertEquals(iface.getServices().size(), m_db
                .countOutagesForInterface(iface));
        assertEquals(iface.getServices().size(), m_db
                .countOpenOutagesForInterface(iface));

        startDaemons();

        // assert that we have no open outages
        assertEquals(0, m_db.countOpenOutagesForService(svc));
        assertEquals(1, m_db.countOutagesForService(svc));

        assertEquals(0, m_db.countOpenOutagesForInterface(iface));
        assertEquals(iface.getServices().size(), m_db
                .countOutagesForInterface(iface));

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
	public void testSendNodeGainedService() {
		m_pollerConfig.setNodeOutageProcessingEnabled(false);

		startDaemons();
		testSendNodeGainedService("SMTP");
	}

	public void testSendNodeGainedServiceNodeOutages() {
		m_pollerConfig.setNodeOutageProcessingEnabled(true);

		startDaemons();
		testSendNodeGainedService("SMTP");
	}

	public void testSendNodeGainedService(String svcName) {

		MockNode node = m_network.addNode(99, "TestNode");
		m_db.writeNode(node);
		MockInterface iface = m_network.addInterface(99, "10.1.1.1");
		m_db.writeInterface(iface);
		MockService element = m_network.addService(99, "10.1.1.1", svcName);
		m_db.writeService(element);
		m_pollerConfig.addService(element);
		MockService smtp = m_network.addService(99, "10.1.1.1", "HTTP");
		m_db.writeService(smtp);
		m_pollerConfig.addService(smtp);

		MockVisitor gainSvcSender = new MockVisitorAdapter() {
			public void visitService(MockService svc) {
				Event event = MockEventUtil
						.createNodeGainedServiceEvent("Test", svc);
				m_eventMgr.sendEventToListeners(event);
			}
		};
		node.visit(gainSvcSender);

		PollAnticipator anticipator = new PollAnticipator();
		element.addAnticipator(anticipator);

		anticipator.anticipateAllServices(element);

		assertEquals(0, anticipator.waitForAnticipated(10000).size());

		anticipateDown(element);

		element.bringDown();

		verifyAnticipated(10000);

	}

	public void testNodeGainedDynamicService() throws Exception {
		m_pollerConfig.setNodeOutageProcessingEnabled(true);

		startDaemons();
        
        TestCapsdConfigManager capsdConfig = new TestCapsdConfigManager(CAPSD_CONFIG);
        OpennmsServerConfigFactory onmsSvrConfig = new OpennmsServerConfigFactory(ConfigurationTestUtils.getReaderForConfigFile("opennms-server.xml"));
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(ConfigurationTestUtils.getReaderForConfigFile("database-schema.xml")));
        CollectdConfigFactory collectdConfig = new CollectdConfigFactory(ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/capsd/collectd-configuration.xml"), onmsSvrConfig.getServerName(), onmsSvrConfig.verifyServer());
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(m_db);

        JdbcCapsdDbSyncer syncer = new JdbcCapsdDbSyncer();
        syncer.setJdbcTemplate(jdbcTemplate);
        syncer.setOpennmsServerConfig(onmsSvrConfig);
        syncer.setCapsdConfig(capsdConfig);
        syncer.setPollerConfig(m_pollerConfig);
        syncer.setCollectdConfig(collectdConfig);
        syncer.setNextSvcIdSql(m_db.getNextServiceIdStatement());
        syncer.afterPropertiesSet();

		OpenNMSProvisioner provisioner = new OpenNMSProvisioner();
		provisioner.setPollerConfig(m_pollerConfig);
		provisioner.setCapsdConfig(capsdConfig);
		provisioner.setCapsdDbSyncer(syncer);

		provisioner.setEventManager(m_eventMgr);
		provisioner.addServiceDNS("MyDNS", 3, 100, 1000, 500, 3000, 53,
				"www.opennms.org");

		assertNotNull("The service id for MyDNS is null", m_db
				.getServiceID("MyDNS"));
		MockUtil.println("The service id for MyDNS is: "
				+ m_db.getServiceID("MyDNS").toString());

		m_anticipator.reset();
		testSendNodeGainedService("MyDNS");

	}

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
		}
	}

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

	private void verifyAnticipated(long millis) {
		verifyAnticipated(millis, true);
	}

	private void verifyAnticipated(long millis, boolean checkUnanticapted) {
		// make sure the down events are received
		MockEventUtil.printEvents("Events we're still waiting for: ", m_anticipator
				.waitForAnticipated(millis));
		assertTrue("Expected events not forthcoming", m_anticipator
				.waitForAnticipated(0).isEmpty());
		if (checkUnanticapted) {
			sleep(2000);
			MockEventUtil.printEvents("Unanticipated: ", m_anticipator
					.unanticipatedEvents());
			assertEquals("Received unexpected events", 0, m_anticipator
					.unanticipatedEvents().size());
		}
		sleep(1000);
		m_eventMgr.finishProcessingEvents();
		assertEquals("Wrong number of outages opened", m_outageAnticipator
				.getExpectedOpens(), m_outageAnticipator.getActualOpens());
		assertEquals("Wrong number of outages in outage table",
				m_outageAnticipator.getExpectedOutages(), m_outageAnticipator
						.getActualOutages());
		assertTrue("Created outages don't match the expected outages",
				m_outageAnticipator.checkAnticipated());
	}

	private void anticipateUp(MockElement element) {
		anticipateUp(element, false);
	}

	private void anticipateUp(MockElement element, boolean force) {
		if (force || !element.getPollStatus().equals(PollStatus.up())) {
			Event event = element.createUpEvent();
			m_anticipator.anticipateEvent(event);
			m_outageAnticipator.anticipateOutageClosed(element, event);
		}
	}

	private void anticipateDown(MockElement element) {
		anticipateDown(element, false);
	}

	private void anticipateDown(MockElement element, boolean force) {
		if (force || !element.getPollStatus().equals(PollStatus.down())) {
			Event event = element.createDownEvent();
			m_anticipator.anticipateEvent(event);
			m_outageAnticipator.anticipateOutageOpened(element, event);
		}
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

	private void createOutages(MockElement element, final Event event) {
		MockVisitor outageCreater = new MockVisitorAdapter() {
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
			m_lostSvcTime = m_db.convertEventTimeToTimeStamp(m_lostSvcEvent
					.getTime());
			m_regainedSvcEvent = regainedSvcEvent;
			if (m_regainedSvcEvent != null)
				m_regainedSvcTime = m_db
						.convertEventTimeToTimeStamp(m_regainedSvcEvent
								.getTime());
		}

		public void processRow(ResultSet rs) throws SQLException {
			assertEquals(m_svc.getNodeId(), rs.getInt("nodeId"));
			assertEquals(m_svc.getIpAddr(), rs.getString("ipAddr"));
			assertEquals(m_svc.getId(), rs.getInt("serviceId"));
			assertEquals(m_lostSvcEvent.getDbid(), rs.getInt("svcLostEventId"));
			assertEquals(m_lostSvcTime, rs.getTimestamp("ifLostService"));
			assertEquals(getRegainedEventId(), rs
					.getObject("svcRegainedEventId"));
			assertEquals(m_regainedSvcTime, rs
					.getTimestamp("ifRegainedService"));
		}

		private Integer getRegainedEventId() {
			if (m_regainedSvcEvent == null)
				return null;
			return new Integer(m_regainedSvcEvent.getDbid());
		}
	}

	// TODO: test multiple polling packages

	// TODO: test overlapping polling packages

	// TODO: test two packages both with the crit service and status propagation

	// TODO: how does unmanaging a node/iface/service work with the poller

	// TODO: test over lapping poll outages

	public static void main(String[] args) {
		junit.textui.TestRunner.run(PollerTest.class);
	}

}
