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
package org.opennms.netmgt.discovery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Seth
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:/META-INF/opennms/applicationContext-dao.xml",
		"classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
		"classpath*:/META-INF/opennms/component-dao.xml",
		"classpath:/META-INF/opennms/applicationContext-daemon.xml",
		"classpath:/META-INF/opennms/applicationContext-pinger.xml",
		"classpath:/META-INF/opennms/mockEventIpcManager.xml",
		"classpath:/META-INF/opennms/applicationContext-discovery.xml",
		"classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
		"classpath:/META-INF/opennms/applicationContext-rpc-detect.xml",
		"classpath:/applicationContext-discovery-mock.xml",

		// Override the Pinger with a Pinger that always returns true
		"classpath:/applicationContext-testPinger.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class UnmanagedInterfaceFilterIT implements InitializingBean {

	@Autowired
	private InterfaceToNodeCache m_cache;

	@Autowired
	private UnmanagedInterfaceFilter m_filter;

	@Autowired
	DatabasePopulator m_populator;

	@Override
	public void afterPropertiesSet() throws Exception {
		BeanUtils.assertAutowiring(this);
	}

	@Before
	public void setUp() throws Exception {
		MockLogAppender.setupLogging(true, "INFO");
		m_populator.populateDatabase();
	}

	@Test
	public void testUnmanagedInterfaceFilter() throws Exception {
		// Sync the cache's database state
		m_cache.dataSourceSync();

		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.1.1"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.1.2"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.1.3"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.2.1"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.2.2"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.2.3"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.3.1"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.3.2"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.3.3"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.4.1"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.4.2"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.4.3"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "10.1.1.1"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "10.1.1.2"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "10.1.1.3"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "10.1.2.1"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "10.1.2.2"));
		assertFalse(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "10.1.2.3"));

		assertTrue(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, InetAddressUtils.ONE_TWENTY_SEVEN));
		assertTrue(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "99.99.99.99"));
		assertTrue(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "10.1.2.0"));
		assertTrue(m_filter.matches(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "10.1.2.4"));
	}
}
