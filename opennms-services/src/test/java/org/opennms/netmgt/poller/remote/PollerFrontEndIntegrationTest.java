//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Aug 24: Use mockEventIpcManager.xml Spring context and remove commented-out code. - dj@opennms.org
// 2007 Apr 16: Don't use test.overridden.properties; use beans and override them instead. - dj@opennms.org
// 2007 Apr 06: Use DaoTestConfigBean to setup system properties. - dj@opennms.org
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.poller.remote;

import java.io.IOException;
import java.util.Properties;

import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.test.FileAnticipator;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PollerFrontEndIntegrationTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private DatabasePopulator m_populator;
    private FileAnticipator m_fileAnticipator;

    private PollerFrontEnd m_frontEnd;
    private PollerSettings m_settings;
    private ClassPathXmlApplicationContext m_frontEndContext;
    
    @Override
    protected void setUpConfiguration() throws IOException {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.afterPropertiesSet();
        
        m_fileAnticipator = new FileAnticipator();

        String filename = m_fileAnticipator.expecting("remote-poller.configuration").getCanonicalPath();
        filename = filename.replace("+", "%2B");
        System.setProperty("opennms.poller.configuration.resource", "file://" + filename);
    }



    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:/META-INF/opennms/mockEventIpcManager.xml",
                "classpath:/META-INF/opennms/applicationContext-dao.xml",
                "classpath*:/META-INF/opennms/component-dao.xml",
                "classpath:/META-INF/opennms/applicationContext-daemon.xml",
                "classpath:/META-INF/opennms/applicationContext-pollerBackEnd.xml",
                "classpath:/META-INF/opennms/applicationContext-exportedPollerBackEnd.xml",
                "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
                "classpath:/org/opennms/netmgt/poller/remote/applicationContext-configOverride.xml",
        };
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        
        if (m_fileAnticipator.isInitialized()) {
            m_fileAnticipator.deleteExpected();
        }
    }

    @Override
    protected void onSetUpInTransactionIfEnabled() throws Exception {
        super.onSetUpInTransactionIfEnabled();
        
        getPopulator().populateDatabase();
        
        /**
         * We complete and end the transaction here so that the populated
         * database gets committed.  If we don't do this, the poller back
         * end (setup with the contexts in getConfigLocations) won't see
         * the populated database because it's working in another
         * transaction.  This will cause one of the asserts in testRegister
         * to fail because no services will be monitored by the remote
         * poller.
         */
        setComplete();
        endTransaction();
        
        m_frontEndContext = new ClassPathXmlApplicationContext(
                new String[] { 
                        "classpath:/META-INF/opennms/applicationContext-remotePollerBackEnd.xml",
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

    @Override
    protected void onTearDownInTransactionIfEnabled() throws Exception {
        m_frontEndContext.stop();
        m_frontEndContext.close();
        
        super.onTearDownInTransactionIfEnabled();
    }
    
    @Override
    protected void onTearDownAfterTransaction() throws Exception {
        m_fileAnticipator.tearDown();
        
        super.onTearDownAfterTransaction();
    }
    
    public void testRegister() throws Exception {
        assertFalse(m_frontEnd.isRegistered());
        
        m_frontEnd.register("RDU");
        
        assertTrue(m_frontEnd.isRegistered());
        Integer monitorId = m_settings.getMonitorId();
        
        assertEquals(1, getSimpleJdbcTemplate().queryForInt("select count(*) from location_monitors where id=?", monitorId));
        assertEquals(5, getSimpleJdbcTemplate().queryForInt("select count(*) from location_monitor_details where locationMonitorId = ?", monitorId));

        assertEquals(System.getProperty("os.name"), getSimpleJdbcTemplate().queryForObject("select propertyValue from location_monitor_details where locationMonitorId = ? and property = ?", String.class, monitorId, "os.name"));
        
        Thread.sleep(60000);
        
        assertEquals(0, getSimpleJdbcTemplate().queryForInt("select count(*) from location_monitors where status='DISCONNECTED' and id=?", monitorId));
        
        assertTrue("Could not find any pollResults", 0 < getSimpleJdbcTemplate().queryForInt("select count(*) from location_specific_status_changes where locationMonitorId = ?", monitorId));

        m_frontEnd.stop();
    }

    public DatabasePopulator getPopulator() {
        return m_populator;
    }

    public void setPopulator(DatabasePopulator populator) {
        m_populator = populator;
    }
}
