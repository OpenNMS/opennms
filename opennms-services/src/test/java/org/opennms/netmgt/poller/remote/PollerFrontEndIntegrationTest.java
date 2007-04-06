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

import java.io.File;
import java.util.Properties;

import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.test.BaseIntegrationTestCase;
import org.opennms.test.DaoTestConfigBean;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PollerFrontEndIntegrationTest extends BaseIntegrationTestCase {

    private PollerFrontEnd m_frontEnd;
    private PollerSettings m_settings;
    private ClassPathXmlApplicationContext m_frontEndContext;
    
    public PollerFrontEndIntegrationTest() {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.setRelativeHomeDirectory("src/test/test-configurations/PollerBackEndIntegrationTest");
        daoTestConfig.afterPropertiesSet();
        
        EventIpcManagerFactory.setIpcManager(new MockEventIpcManager());
        String configFile = "/tmp/remote-poller.configuration";
        File config = new File(configFile);
        config.delete();
        System.setProperty("opennms.poller.configuration.resource", "file://"+configFile);
        System.setProperty("test.overridden.properties", "file:src/test/test-configurations/PollerBackEndIntegrationTest/test.overridden.properties");
    }
    

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
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
    protected void onTearDown() throws Exception {
        m_frontEndContext.stop();
        m_frontEndContext.close();
        super.onTearDown();
    }



    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:/META-INF/opennms/applicationContext-dao.xml",
                "classpath:/META-INF/opennms/applicationContext-pollerBackEnd.xml",
                "classpath:/META-INF/opennms/applicationContext-exportedPollerBackEnd.xml",
        };

    }
    
    public void testRegister() throws Exception {
       
        assertFalse(m_frontEnd.isRegistered());
        
        m_frontEnd.register("RDU");
        
        assertTrue(m_frontEnd.isRegistered());
        Integer monitorId = m_settings.getMonitorId();
        
        assertEquals(1, getJdbcTemplate().queryForInt("select count(*) from location_monitors where id=?", monitorId));
        assertEquals(5, getJdbcTemplate().queryForInt("select count(*) from location_monitor_details where locationMonitorId = ?", monitorId));

        assertEquals(System.getProperty("os.name"), getJdbcTemplate().queryForObject("select propertyValue from location_monitor_details where locationMonitorId = ? and property = ?", String.class, monitorId, "os.name"));
        
        Thread.sleep(10000);
        
        assertEquals(0, getJdbcTemplate().queryForInt("select count(*) from location_monitors where status='DISCONNECTED' and id=?", monitorId));
        
        m_frontEnd.stop();
        assertTrue("Could not found any pollResults", 0 < getJdbcTemplate().queryForInt("select count(*) from location_specific_status_changes where locationMonitorId = ?", monitorId));
        
      
    }
    
}
