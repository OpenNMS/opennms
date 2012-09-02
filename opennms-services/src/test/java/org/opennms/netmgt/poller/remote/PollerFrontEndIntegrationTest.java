/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.test.FileAnticipator;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-pollerBackEnd.xml",
        "classpath:/META-INF/opennms/applicationContext-exportedPollerBackEnd-rmi.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/org/opennms/netmgt/poller/remote/applicationContext-configOverride.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class PollerFrontEndIntegrationTest implements InitializingBean, TemporaryDatabaseAware<TemporaryDatabase> {
    @Autowired
    private DatabasePopulator m_populator;

    private FileAnticipator m_fileAnticipator;
    private PollerFrontEnd m_frontEnd;
    private PollerSettings m_settings;
    private ClassPathXmlApplicationContext m_frontEndContext;

    private TemporaryDatabase m_database;

    @Override
    public void setTemporaryDatabase(TemporaryDatabase database) {
        m_database = database;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @After
    public void afterTest() throws Throwable {
        m_frontEndContext.stop();
        m_frontEndContext.close();

        if (m_fileAnticipator.isInitialized()) {
            m_fileAnticipator.deleteExpected();
        }

        m_fileAnticipator.tearDown();
    }

    @Before
    public void onSetUpInTransactionIfEnabled() throws Exception {
        m_fileAnticipator = new FileAnticipator();

        String filename = m_fileAnticipator.expecting("remote-poller.configuration").getCanonicalPath();
        filename = filename.replace("+", "%2B");
        System.setProperty("opennms.poller.configuration.resource", "file://" + filename);

        m_populator.populateDatabase();

        /**
         * We complete and end the transaction here so that the populated
         * database gets committed.  If we don't do this, the poller back
         * end (setup with the contexts in getConfigLocations) won't see
         * the populated database because it's working in another
         * transaction.  This will cause one of the asserts in testRegister
         * to fail because no services will be monitored by the remote
         * poller.
         */
        /*
        setComplete();
        endTransaction();
         */

        m_frontEndContext = new ClassPathXmlApplicationContext(
                                                               new String[] { 
                                                                       "classpath:/META-INF/opennms/applicationContext-remotePollerBackEnd-rmi.xml",
                                                                       "classpath:/META-INF/opennms/applicationContext-pollerFrontEnd.xml",
                                                               },
                                                               false
        );

        Properties props = new Properties();
        props.setProperty("configCheckTrigger.repeatInterval", "1000");

        PropertyOverrideConfigurer testPropertyConfigurer = new PropertyOverrideConfigurer();
        testPropertyConfigurer.setProperties(props);
        m_frontEndContext.addBeanFactoryPostProcessor(testPropertyConfigurer);

        m_frontEndContext.refresh();
        m_frontEnd = (PollerFrontEnd)m_frontEndContext.getBean("pollerFrontEnd");
        m_settings = (PollerSettings)m_frontEndContext.getBean("pollerSettings");
    }

    @Test
    public void testRegister() throws Exception {

        // Check preconditions
        assertFalse(m_frontEnd.isRegistered());
        assertEquals(0, m_database.getJdbcTemplate().queryForInt("select count(*) from location_monitors"));
        assertEquals(0, m_database.getJdbcTemplate().queryForInt("select count(*) from location_monitor_details"));
        assertTrue("There were unexpected poll results", 0 == m_database.getJdbcTemplate().queryForInt("select count(*) from location_specific_status_changes"));

        // Start up the remote poller
        m_frontEnd.register("RDU");
        Integer monitorId = m_settings.getMonitorId();

        assertTrue(m_frontEnd.isRegistered());
        assertEquals(1, m_database.getJdbcTemplate().queryForInt("select count(*) from location_monitors where id=?", monitorId));
        assertEquals(5, m_database.getJdbcTemplate().queryForInt("select count(*) from location_monitor_details where locationMonitorId = ?", monitorId));

        assertEquals(System.getProperty("os.name"), m_database.getJdbcTemplate().queryForObject("select propertyValue from location_monitor_details where locationMonitorId = ? and property = ?", String.class, monitorId, "os.name"));

        Thread.sleep(60000);

        assertEquals(1, m_database.getJdbcTemplate().queryForInt("select count(*) from location_monitors where id=?", monitorId));
        assertEquals(0, m_database.getJdbcTemplate().queryForInt("select count(*) from location_monitors where status='DISCONNECTED' and id=?", monitorId));

        assertTrue("Could not find any pollResults", 0 < m_database.getJdbcTemplate().queryForInt("select count(*) from location_specific_status_changes where locationMonitorId = ?", monitorId));

        m_frontEnd.stop();
    }
}
