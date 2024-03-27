/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.poller;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

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
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.CriticalPath;
import org.opennms.netmgt.dao.api.PathOutageManager;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockPollerConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
		"classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-pollerdTest.xml",
		"classpath:/META-INF/opennms/applicationContext-test-deviceConfig.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class,reuseDatabase=false)
public class PathOutageManagerDaoIT implements TemporaryDatabaseAware<MockDatabase> {

	private MockNetwork m_network;

	private MockDatabase m_db;

	private MockPollerConfig m_pollerConfig;

	@Autowired
	private QueryManager m_queryManager;
	
    @Autowired
    private PathOutageManager m_pathOutageManager;

    protected PathOutageManager getPathOutageManager() {
        return m_pathOutageManager;
    }

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
		
//		m_network.addInterface("fe80:0000:0000:0000:0231:f982:0123:4567");
//		m_network.addService("SNMP");

		m_db.populate(m_network);
		DataSourceFactory.setInstance(m_db);

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

		DefaultPollContext pollContext = new DefaultPollContext();
		pollContext.setLocalHostName("localhost");
		pollContext.setName("Test.DefaultPollContext");
		pollContext.setPollerConfig(m_pollerConfig);
		pollContext.setQueryManager(m_queryManager);
	}

	@After
	public void tearDown() throws Exception {
		sleep(200);
		MockUtil.println("------------ End Test  --------------------------");
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (final InterruptedException e) {
		}
	}

	@Test
	public void test() throws SQLException, UnknownHostException {
		final Connection conn = m_db.getConnection();

		String[] ar = getPathOutageManager().getLabelAndStatus("1", conn);
		assertEquals("Router", ar[0]);
		assertEquals("Normal", ar[1]);
		assertEquals("All Services Up", ar[2]);
		String[] cr = getPathOutageManager().getLabelAndStatus("3", conn);
		assertEquals("Firewall", cr[0]);
		assertEquals("Normal", cr[1]);
		assertEquals("All Services Up", cr[2]);
		Set<Integer> lno = getPathOutageManager().getNodesInPath("192.168.1.1", "ICMP");
		assertEquals(new Integer(1), lno.iterator().next());
		Set<Integer> vno = getPathOutageManager().getNodesInPath("192.168.1.4", "SMTP");
		assertEquals(new Integer(3), vno.iterator().next());

		// This list should order by node label so Firewall should precede Router
		List<String[]> all = getPathOutageManager().getAllCriticalPaths();
		assertEquals(2, all.size());

		assertEquals("Firewall",all.get(0)[0]);
		assertEquals("192.168.1.4",all.get(0)[1]);
		assertEquals("SMTP",all.get(0)[2]);

		assertEquals("Router",all.get(1)[0]);
		assertEquals("192.168.1.1",all.get(1)[1]);
		assertEquals("ICMP", all.get(1)[2]);

		String[] dat = getPathOutageManager().getCriticalPathData("192.168.1.1", "ICMP");
		assertEquals("Router", dat[0]);
		assertEquals("1", dat[1]);
		assertEquals("1", dat[2]);
		assertEquals("Normal", dat[3]);
		String mm = getPathOutageManager().getPrettyCriticalPath(1);
		assertEquals("192.168.1.1 ICMP", mm);
		String nn = getPathOutageManager().getPrettyCriticalPath(3);
		assertEquals("192.168.1.4 SMTP", nn);
		CriticalPath pa = getPathOutageManager().getCriticalPath(1);
		assertEquals(InetAddress.getByName("192.168.1.1"), pa.getIpAddress());
		assertEquals("ICMP", pa.getServiceName());
		CriticalPath nc = getPathOutageManager().getCriticalPath(3);
		assertEquals(InetAddress.getByName("192.168.1.4"), nc.getIpAddress());
		assertEquals("SMTP", nc.getServiceName());

		Set<Integer> test = getPathOutageManager().getAllNodesDependentOnAnyServiceOnInterface("192.168.1.1");
		assertEquals(1, test.size());
		
		Set<Integer> less = getPathOutageManager().getAllNodesDependentOnAnyServiceOnNode(3);
		assertEquals(1, less.size());

		conn.close();
	}
	
	/**
	 * Use this method to compare the speed of Hibernate to JDBC
	 * @throws UnknownHostException 
	 **/
	@Ignore("manual test for benchmarking")
	@Test
	public void testMethod500Times() throws SQLException, UnknownHostException {
		for (int i = 0; i < 500; i++) {
			test();
		}
	}
}
