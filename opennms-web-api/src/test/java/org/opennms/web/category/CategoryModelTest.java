/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.web.category;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.support.NullRrdStrategy;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:/META-INF/opennms/applicationContext-mockDao.xml",
		"classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class,reuseDatabase=false)
public class CategoryModelTest implements TemporaryDatabaseAware<MockDatabase> {

	private MockNetwork m_network;

	private MockDatabase m_db;

	@Override
	public void setTemporaryDatabase(MockDatabase database) {
		m_db = database;
	}

	//
	// SetUp and TearDown
	//

	@Before
	public void setUp() throws Exception {

		MockUtil.println("------------ Begin Test  --------------------------");
		MockLogAppender.setupLogging();

		m_network = new MockNetwork();
		m_network.setCriticalService("ICMP");
		m_network.addNode(1, "Router");
		m_network.addInterface("192.168.1.1");
		m_network.addService("ICMP");
		m_network.addPathOutage(1, InetAddressUtils.addr("192.168.1.1"), "ICMP");
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
		m_network.addPathOutage(3, InetAddressUtils.addr("192.168.1.4"), "SMTP");
		m_network.addService("HTTP");
		m_network.addInterface("192.168.1.5");
		m_network.addService("SMTP");
		m_network.addService("HTTP");
		m_network.addNode(4, "DownNode");
		m_network.addInterface("192.168.1.6");
		m_network.addService("SNMP");

		m_db.populate(m_network);
		DataSourceFactory.setInstance(m_db);

		RrdUtils.setStrategy(new NullRrdStrategy());
	}

	@After
	public void tearDown() throws Exception {
		MockUtil.println("------------ End Test  --------------------------");
	}

	@Test
	public void test() throws SQLException {

		MockService service = m_network.getService(1, "192.168.1.1", "ICMP");
		MockService upSmtp = m_network.getService(1, "192.168.1.1", "SMTP");
		MockService upSnmp = m_network.getService(1, "192.168.1.1", "SNMP");

		// Generate some events for referential integrity
		Event[] events = new Event[10];
		for (int i = 0; i < events.length; i++) {
			events[i] = new Event();
			events[i].setTime(new Date());
			events[i].setCreationTime(new Date());
			events[i].setUei(EventConstants.NODE_LOST_SERVICE_EVENT_UEI);
			events[i].setSource(getClass().getSimpleName());
			events[i].setNodeid((long)service.getNodeId());
			events[i].setInterface(service.getIpAddr());
			events[i].setService(service.getSvcName());
			m_db.writeEvent(events[i]);
		}

		long startTime = 0L;
		long timeframe = 60L * 60L * 24L * 1000L;

		/// 24 HOUR TIMEFRAME
		// There are 3 services total
		assertEquals(100.0, CategoryModel.getInterfaceAvailability(service.getNodeId(), service.getIpAddr(), new Date(0), new Date(startTime + timeframe)), 0.0000001);
		// No downtime yet
		assertEquals(100.0, CategoryModel.getServiceAvailability(service.getNodeId(), service.getIpAddr(), service.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSmtp.getNodeId(), upSmtp.getIpAddr(), upSmtp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSnmp.getNodeId(), upSnmp.getIpAddr(), upSnmp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 

		// Take down one service
		assertEquals(0, m_db.countOpenOutagesForService(service));
		m_db.createOutage(service, events[0].getDbid(), new Timestamp(0L));
		assertEquals(1, m_db.countOpenOutagesForService(service));

		/// 24 HOUR TIMEFRAME
		// There are 3 services total, one totally down, the other 2 up
		assertEquals(48.0 / 72.0 * 100.0, CategoryModel.getInterfaceAvailability(service.getNodeId(), service.getIpAddr(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001);
		// One hour of downtime
		assertEquals(0.0, CategoryModel.getServiceAvailability(service.getNodeId(), service.getIpAddr(), service.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSmtp.getNodeId(), upSmtp.getIpAddr(), upSmtp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSnmp.getNodeId(), upSnmp.getIpAddr(), upSnmp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 

		timeframe = 60L * 60L * 4L * 1000L;

		/// 4 HOUR TIMEFRAME
		// There are 3 services total, one totally down, the other 2 up
		assertEquals(8.0 / 12.0 * 100.0, CategoryModel.getInterfaceAvailability(service.getNodeId(), service.getIpAddr(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001);
		// One hour of downtime
		assertEquals(0.0, CategoryModel.getServiceAvailability(service.getNodeId(), service.getIpAddr(), service.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSmtp.getNodeId(), upSmtp.getIpAddr(), upSmtp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSnmp.getNodeId(), upSnmp.getIpAddr(), upSnmp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 

		// Bring the service back up after 2 hours
		m_db.resolveOutage(service, events[1].getDbid(), new Timestamp(2L * 60L * 60L * 1000L));
		assertEquals(0, m_db.countOpenOutagesForService(service));

		timeframe = 60L * 60L * 24L * 1000L;

		/// 24 HOUR TIMEFRAME
		// There are 3 services total
		assertEquals(70.0 / 72.0 * 100.0, CategoryModel.getInterfaceAvailability(service.getNodeId(), service.getIpAddr(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001);
		// One hour of downtime
		assertEquals(22.0 / 24.0 * 100.0, CategoryModel.getServiceAvailability(service.getNodeId(), service.getIpAddr(), service.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSmtp.getNodeId(), upSmtp.getIpAddr(), upSmtp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSnmp.getNodeId(), upSnmp.getIpAddr(), upSnmp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 

		// Shift the start time forward 30 minutes so that we only see 30 minutes of downtime
		startTime = 30L * 60L * 1000L;

		/// 24 HOUR TIMEFRAME
		// There are 3 services total
		assertEquals(70.5 / 72.0 * 100.0, CategoryModel.getInterfaceAvailability(service.getNodeId(), service.getIpAddr(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001);
		// One hour of downtime
		assertEquals(22.5 / 24.0 * 100.0, CategoryModel.getServiceAvailability(service.getNodeId(), service.getIpAddr(), service.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSmtp.getNodeId(), upSmtp.getIpAddr(), upSmtp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSnmp.getNodeId(), upSnmp.getIpAddr(), upSnmp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 

		startTime = 0L;
		timeframe = 60L * 60L * 4L * 1000L;

		/// 4 HOUR TIMEFRAME
		// There are 3 services total
		assertEquals(10.0 / 12.0 * 100.0, CategoryModel.getInterfaceAvailability(service.getNodeId(), service.getIpAddr(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001);
		// One hour of downtime
		assertEquals(2.0 / 4.0 * 100.0, CategoryModel.getServiceAvailability(service.getNodeId(), service.getIpAddr(), service.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSmtp.getNodeId(), upSmtp.getIpAddr(), upSmtp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSnmp.getNodeId(), upSnmp.getIpAddr(), upSnmp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 

		startTime = 0L;
		timeframe = 60L * 60L * 24L * 1000L;

		// Take down the service again at the 23 hour mark
		assertEquals(0, m_db.countOpenOutagesForService(service));
		m_db.createOutage(service, events[2].getDbid(), new Timestamp(23L * 60L * 60L * 1000L));
		assertEquals(1, m_db.countOpenOutagesForService(service));

		/// 24 HOUR TIMEFRAME
		// There are 3 services total, one totally down, the other 2 up
		assertEquals(69.0 / 72.0 * 100.0, CategoryModel.getInterfaceAvailability(service.getNodeId(), service.getIpAddr(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001);
		// One hour of downtime
		assertEquals(21.0 / 24.0 * 100.0, CategoryModel.getServiceAvailability(service.getNodeId(), service.getIpAddr(), service.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSmtp.getNodeId(), upSmtp.getIpAddr(), upSmtp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSnmp.getNodeId(), upSnmp.getIpAddr(), upSnmp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 

		// Shift the start time forward 60 minutes so that we only see 60 minutes of downtime
		// from the first outage and 2 hours from the second outage
		startTime = 60L * 60L * 1000L;

		/// 24 HOUR TIMEFRAME
		// There are 3 services total
		assertEquals(69.0 / 72.0 * 100.0, CategoryModel.getInterfaceAvailability(service.getNodeId(), service.getIpAddr(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001);
		// One hour of downtime
		assertEquals(21.0 / 24.0 * 100.0, CategoryModel.getServiceAvailability(service.getNodeId(), service.getIpAddr(), service.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSmtp.getNodeId(), upSmtp.getIpAddr(), upSmtp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSnmp.getNodeId(), upSnmp.getIpAddr(), upSnmp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 

		// Bring the service back up after 1.5 hours
		m_db.resolveOutage(service, events[3].getDbid(), new Timestamp((24L * 60L * 60L * 1000L) /* 24 hours */ + (30L * 60L * 1000L) /* 30 minutes */));
		assertEquals(0, m_db.countOpenOutagesForService(service));

		startTime = 0L;
		timeframe = 60L * 60L * 24L * 1000L;

		/// 24 HOUR TIMEFRAME
		// There are 3 services total
		assertEquals(69.0 / 72.0 * 100.0, CategoryModel.getInterfaceAvailability(service.getNodeId(), service.getIpAddr(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001);
		// One hour of downtime
		assertEquals(21.0 / 24.0 * 100.0, CategoryModel.getServiceAvailability(service.getNodeId(), service.getIpAddr(), service.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSmtp.getNodeId(), upSmtp.getIpAddr(), upSmtp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSnmp.getNodeId(), upSnmp.getIpAddr(), upSnmp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 

		// Shift the start time forward 60 minutes so that we only see 60 minutes of downtime
		// from the first outage and 1.5 hours from the second outage
		startTime = 60L * 60L * 1000L;

		/// 24 HOUR TIMEFRAME
		// There are 3 services total
		assertEquals(69.5 / 72.0 * 100.0, CategoryModel.getInterfaceAvailability(service.getNodeId(), service.getIpAddr(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001);
		// One hour of downtime
		assertEquals(21.5 / 24.0 * 100.0, CategoryModel.getServiceAvailability(service.getNodeId(), service.getIpAddr(), service.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSmtp.getNodeId(), upSmtp.getIpAddr(), upSmtp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
		assertEquals(100.0, CategoryModel.getServiceAvailability(upSnmp.getNodeId(), upSnmp.getIpAddr(), upSnmp.getId(), new Date(startTime), new Date(startTime + timeframe)), 0.0000001); 
	}
}
