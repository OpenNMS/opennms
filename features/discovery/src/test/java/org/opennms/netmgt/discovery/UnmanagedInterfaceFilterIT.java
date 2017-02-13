/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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
		"classpath*:/META-INF/opennms/component-dao.xml",
		"classpath:/META-INF/opennms/applicationContext-daemon.xml",
		"classpath:/META-INF/opennms/applicationContext-pinger.xml",
		"classpath:/META-INF/opennms/mockEventIpcManager.xml",
		"classpath:/META-INF/opennms/applicationContext-discovery.xml",
		"classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",

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
