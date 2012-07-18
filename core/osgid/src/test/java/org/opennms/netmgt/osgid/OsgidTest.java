/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.osgid;

import java.io.File;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * Acknowledgment Daemon tests
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-dao.xml",
		"classpath*:/META-INF/opennms/component-dao.xml",
		"classpath*:/META-INF/opennms/component-service.xml",
		"classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
		"classpath:/META-INF/opennms/applicationContext-osgid.xml",
		"classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
		//"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
@Transactional
public class OsgidTest implements InitializingBean {

	@Autowired
	private Osgid m_daemon;

	@Autowired
	private DatabasePopulator m_populator;

	private static boolean m_populated = false;

	@BeforeTransaction
	public void populateDatabase() {
		try {
			if (!m_populated) {
				m_populator.populateDatabase();
			}
		} catch (Throwable e) {
			e.printStackTrace(System.err);
		} finally {
			m_populated = true;
		}
	}

	@Before
	public void setUp() throws Exception {
		Properties props = new Properties();
		props.setProperty("log4j.logger.org.hibernate", "INFO");
		props.setProperty("log4j.logger.org.springframework", "INFO");
		//props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");
		MockLogAppender.setupLogging(props);
		
		m_daemon.setHomeDirectory(new File("target/test-classes/karaf").getPath());
	}

	@After
	public void tearDown() throws Exception {
		// Clean up the Karaf module cache
		FileUtils.deleteDirectory(new File(m_daemon.getHomeDirectory(), "data"));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		BeanUtils.assertAutowiring(this);
	}

	@Test
	public void testOsgidStartup() throws Exception {
		m_daemon.start();
		m_daemon.destroy();
	}
}
