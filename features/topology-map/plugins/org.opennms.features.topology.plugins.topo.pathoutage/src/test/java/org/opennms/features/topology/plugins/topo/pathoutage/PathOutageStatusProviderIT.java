/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.pathoutage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-dao.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
@Transactional
public class PathOutageStatusProviderIT {

	/**
	 * Constant IP-address used for testing purposes
	 */
	private static final String address_1 = "205.113.5.72";
	/**
	 * Constant IP-address used for testing purposes
	 */
	private static final String address_2 = "192.18.35.14";
	/**
	 * Constant IP-address used for testing purposes
	 */
	private static final String address_3 = "100.25.122.1";
	/**
	 * Constant IP-address used for testing purposes
	 */
	private static final String address_4 = "8.8.8.8";

	@Autowired
	private NodeDao nodeDao;
	@Autowired
	private EventDao eventDao;
	@Autowired
	private OutageDao outageDao;
	@Autowired
	private GenericPersistenceAccessor genericPersistenceAccessor;
	@Autowired
	private MonitoringLocationDao locationDao;
	@Autowired
	private DistPollerDao distPollerDao;
	@Autowired
	private MonitoredServiceDao monitoredServiceDao;
	@Autowired
	private IpInterfaceDao interfaceDao;
	@Autowired
	private ServiceTypeDao serviceTypeDao;

	/**
	 * {@link PathOutageProvider} object, responsible for generating vertex topology
	 */
	private PathOutageProvider pathOutageProvider;
	/**
	 * {@link PathOutageStatusProvider} object, responsible for mapping alarms to vertices
	 */
	private PathOutageStatusProvider pathOutageStatusProvider;

	@Before
	public void setUp() {
		// Generating test nodes and saving them to the temporary database
		Map<OnmsNode, Integer> nodes = TestNodeGenerator.generateNodes(locationDao.getDefaultLocation());
		for (OnmsNode node : nodes.keySet()) {
			this.nodeDao.save(node);
		}
		// Generating test interfaces and updating corresponding nodes
		this.generateInterfaces(nodes);
		for (OnmsNode node : nodes.keySet()) {
			this.nodeDao.saveOrUpdate(node);
		}
	}

	@Test
	/**
	 * This method tests if the {@link PathOutageStatusProvider} retrieves alarm data correctly
	 */
	public void verify() throws UnknownHostException {
		this.pathOutageStatusProvider = new PathOutageStatusProvider(this.genericPersistenceAccessor);
		this.pathOutageProvider = new PathOutageProvider(this.nodeDao, this.pathOutageStatusProvider);
		this.pathOutageProvider.refresh();

		// Very basic test - check that PathOutageStatusProvider finds a Status for each vertex
		// and that all these statuses are at the moment NORMAL
		Map<VertexRef, Status> stats = this.calculateStatuses();
		for (VertexRef vertex : stats.keySet()) {
			Assert.assertNotNull(stats.get(vertex));
			Assert.assertEquals(OnmsSeverity.NORMAL.getLabel().toLowerCase(), stats.get(vertex).computeStatus().toLowerCase());
		}

		// Also check that the PathOutageProvider calculates the default focus strategy correctly
		// the list of criteria should always contain exactly 1 vertex (with the worst status)
		List<Criteria> criteria = this.pathOutageProvider.getDefaults().getCriteria();
		Assert.assertNotNull(criteria);
		Assert.assertEquals(1, criteria.size());

		// Adding a single path outage with MINOR severity.
		// PathOutageStatusProvider should be able to find this outage + all other nodes should have a state NORMAL
		this.outageDao.save(createOutage(EventConstants.NODE_DOWN_EVENT_UEI, address_1, this.nodeDao.get(1), OnmsSeverity.MINOR));
		stats = this.calculateStatuses();
		for (VertexRef vertex : stats.keySet()) {
			Assert.assertNotNull(stats.get(vertex));
			if (vertex.getId().equalsIgnoreCase("1")) {
				Assert.assertEquals(OnmsSeverity.MINOR.getLabel().toLowerCase(), stats.get(vertex).computeStatus().toLowerCase());
			} else {
				Assert.assertEquals(OnmsSeverity.NORMAL.getLabel().toLowerCase(), stats.get(vertex).computeStatus().toLowerCase());
			}
		}

		// List of criteria should contain exactly 1 vertex (with the worst status, meaning that its id should be 1)
		criteria = this.pathOutageProvider.getDefaults().getCriteria();
		Assert.assertNotNull(criteria);
		Assert.assertEquals(1, criteria.size());
		VertexHopGraphProvider.DefaultVertexHopCriteria criterion = (VertexHopGraphProvider.DefaultVertexHopCriteria) criteria.get(0);
		Assert.assertEquals("1", criterion.getId());

		// Adding a MAJOR path outage to the same node.
		// PathOutageStatusProvider should display the alarm with a MAJOR severity level
		this.outageDao.save(createOutage(EventConstants.NODE_DOWN_EVENT_UEI, address_1, this.nodeDao.get(1), OnmsSeverity.MAJOR));
		stats = this.calculateStatuses();
		for (VertexRef vertex : stats.keySet()) {
			Assert.assertNotNull(stats.get(vertex));
			if (vertex.getId().equalsIgnoreCase("1")) {
				Assert.assertEquals(OnmsSeverity.MAJOR.getLabel().toLowerCase(), stats.get(vertex).computeStatus().toLowerCase());
				break;
			}
		}

		// List of criteria should contain exactly 1 vertex (with the worst status, meaning that its id should still be 1)
		criteria = this.pathOutageProvider.getDefaults().getCriteria();
		Assert.assertNotNull(criteria);
		Assert.assertEquals(1, criteria.size());
		criterion = (VertexHopGraphProvider.DefaultVertexHopCriteria) criteria.get(0);
		Assert.assertEquals("1", criterion.getId());

		// Adding several more path outages of different types
		// PathOutageStatusProvider should display them all, plus the one from the previous test
		this.outageDao.save(createOutage(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, address_2, this.nodeDao.get(2), OnmsSeverity.MINOR));
		this.outageDao.save(createOutage(EventConstants.INTERFACE_DOWN_EVENT_UEI, address_3 , this.nodeDao.get(3), OnmsSeverity.MINOR));
		this.outageDao.save(createOutage(EventConstants.PATH_OUTAGE_EVENT_UEI,address_4, this.nodeDao.get(4), OnmsSeverity.MAJOR));
		stats = this.calculateStatuses();
		for (VertexRef vertex : stats.keySet()) {
			Assert.assertNotNull(stats.get(vertex));
			if (vertex.getId().equalsIgnoreCase("1")) {
				Assert.assertEquals(OnmsSeverity.MAJOR.getLabel().toLowerCase(), stats.get(vertex).computeStatus().toLowerCase());
			} else if (vertex.getId().equalsIgnoreCase("2")) {
				Assert.assertEquals(OnmsSeverity.MINOR.getLabel().toLowerCase(), stats.get(vertex).computeStatus().toLowerCase());
			} else if (vertex.getId().equalsIgnoreCase("3")) {
				Assert.assertEquals(OnmsSeverity.MINOR.getLabel().toLowerCase(), stats.get(vertex).computeStatus().toLowerCase());
			} else if (vertex.getId().equalsIgnoreCase("4")) {
				Assert.assertEquals(OnmsSeverity.MAJOR.getLabel().toLowerCase(), stats.get(vertex).computeStatus().toLowerCase());
			}
		}

		// List of criteria should contain exactly 1 vertex (with the worst status, meaning that its id should be either 1 or 4)
		criteria = this.pathOutageProvider.getDefaults().getCriteria();
		Assert.assertNotNull(criteria);
		Assert.assertEquals(1, criteria.size());
		criterion = (VertexHopGraphProvider.DefaultVertexHopCriteria) criteria.get(0);
		Assert.assertTrue(criterion.getId().equalsIgnoreCase("1") || criterion.getId().equalsIgnoreCase("4"));
	}

	/**
	 * This method uses {@link PathOutageStatusProvider} object to retrieve status information for the visible vertices
	 * from the test database
	 * @return Map with {@link Status} information for all vertices
	 */
	private Map<VertexRef, Status> calculateStatuses() {
		Map<VertexRef, Status> retvals = this.pathOutageStatusProvider.getStatusForVertices(pathOutageProvider,
				Lists.newArrayList(pathOutageProvider.getVertices()), null);
		return retvals;
	}

	/**
	 * In this method we create {@link OnmsIpInterface} objects for some of the {@link OnmsNode}s
	 * and save them to the {@link IpInterfaceDao}
	 * @param nodes List of nodes to use
	 */
	private void generateInterfaces(Map<OnmsNode, Integer> nodes) {
		Map<Integer, OnmsNode> nodesConverted = new HashMap<>();
		for (OnmsNode node : nodes.keySet()) {
			nodesConverted.put(node.getId(), node);
		}
		OnmsIpInterface interface_1 = new OnmsIpInterface(InetAddressUtils.getInetAddress(address_1), nodesConverted.get(1));
		OnmsIpInterface interface_2 = new OnmsIpInterface(InetAddressUtils.getInetAddress(address_2), nodesConverted.get(2));
		OnmsIpInterface interface_3 = new OnmsIpInterface(InetAddressUtils.getInetAddress(address_3), nodesConverted.get(3));
		OnmsIpInterface interface_4 = new OnmsIpInterface(InetAddressUtils.getInetAddress(address_4), nodesConverted.get(4));
		interfaceDao.save(interface_1);
		interfaceDao.save(interface_2);
		interfaceDao.save(interface_3);
		interfaceDao.save(interface_4);
		nodesConverted.get(1).addIpInterface(interface_1);
		nodesConverted.get(2).addIpInterface(interface_2);
		nodesConverted.get(3).addIpInterface(interface_3);
		nodesConverted.get(4).addIpInterface(interface_4);
	}
	/**
	 * This method creates a single event
	 * @param eventUei Event UEI
	 * @param address IP-address (in dot-format)
	 * @param node Node
	 * @param severity Severity of the event
	 * @return Resulting event
	 * @throws UnknownHostException
	 */
	private OnmsEvent createEvent(String eventUei, String address, OnmsNode node, OnmsSeverity severity)
			throws UnknownHostException {
		OnmsEvent event = new OnmsEvent();
		event.setEventUei(eventUei);
		event.setEventTime(new Date());
		event.setEventHost("eventhost");
		event.setEventSource("eventsource");
		event.setIpAddr(InetAddress.getByName(address));
		event.setDistPoller(distPollerDao.whoami());
		event.setEventSnmpHost("eventssnmphost");
		event.setEventSnmp("eventsnmp");
		event.setEventCreateTime(new Date());
		event.setEventDescr("eventdescr");
		event.setEventLogGroup("eventloggroup");
		event.setEventLogMsg("eventlogmsg");
		event.setEventSeverity(severity.getId());
		event.setEventSuppressedCount(0);
		event.setEventOperInstruct("operinstruct");
		event.setEventTTicket("tticketid");
		event.setEventTTicketState(1);
		event.setEventLog("Y");
		event.setEventDisplay("Y");
		event.setNode(node);
		return event;
	}

	/**
	 * This method creates an outage with a given parameters for the specified node
	 * @param uie Event UIE
	 * @param ipaddress IP-address (in dot-format)
	 * @param node Node
	 * @param severity Severity
	 * @return Resulting outage
	 * @throws UnknownHostException
	 */
	private OnmsOutage createOutage(String uie, String ipaddress, OnmsNode node, OnmsSeverity severity)
			throws UnknownHostException {
		OnmsEvent event = createEvent(uie, ipaddress, node, severity);
		eventDao.save(event);
		OnmsMonitoredService service = createService(node);
		monitoredServiceDao.save(service);
		OnmsOutage outage = new OnmsOutage(new Date(), event, service);
		return outage;
	}

	/**
	 * This method creates a service for a given node
	 * @param node Node
	 * @return Resulting service
	 */
	private OnmsMonitoredService createService(OnmsNode node) {
		OnmsMonitoredService monitoredService = new OnmsMonitoredService();
		OnmsServiceType serviceType = createType("someType");
		serviceTypeDao.save(serviceType);
		monitoredService.setServiceType(serviceType);
		monitoredService.setIpInterface(node.getIpInterfaces().toArray(new OnmsIpInterface[node.getIpInterfaces().size()])[0]);
		return monitoredService;
	}

	/**
	 * This method generates a service type
	 * @param name
	 * @return Resulting service type
	 */
	private OnmsServiceType createType(String name) {
		OnmsServiceType type = new OnmsServiceType(name);
		return type;
	}
}