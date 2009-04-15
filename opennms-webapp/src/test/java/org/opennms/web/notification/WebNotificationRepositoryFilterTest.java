/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.web.filter.Filter;
import org.opennms.web.notification.filter.AcknowledgedByFilter;
import org.opennms.web.notification.filter.InterfaceFilter;
import org.opennms.web.notification.filter.NodeFilter;
import org.opennms.web.notification.filter.NotificationCriteria;
import org.opennms.web.notification.filter.NotificationIdFilter;
import org.opennms.web.notification.filter.NotificationIdListFilter;
import org.opennms.web.notification.filter.ResponderFilter;
import org.opennms.web.notification.filter.ServiceFilter;
import org.opennms.web.notification.filter.UserFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={"classpath:/META-INF/opennms/applicationContext-dao.xml",
                                 "classpath:/daoWebNotificationRepositoryTestContext.xml",
                                 "classpath:/jdbcWebNotificationRepositoryTestContext.xml"})
@JUnitTemporaryDatabase()
public class WebNotificationRepositoryFilterTest {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    DaoWebNotificationRepository m_daoNotificationRepo;
    
    @Autowired
    JdbcWebNotificationRepository m_jdbcNotificationRepo;
    
    @BeforeClass
    public static void setUpLogging(){
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");

        MockLogAppender.setupLogging(props);
    }
    
    @Before
    public void setUp(){
        assertNotNull(m_daoNotificationRepo);
        m_dbPopulator.populateDatabase();
    }
    
    @Test
    @Transactional
    public void testAcknowledgeByFilter(){
        m_daoNotificationRepo.acknowledgeMatchingNotification("TestUser", new Date(), new NotificationCriteria());
        
        AcknowledgedByFilter filter = new AcknowledgedByFilter("TestUser");
        assert1Result(filter);
    }
    
    @Test
    public void testInterfaceFilter(){
        InterfaceFilter filter = new InterfaceFilter("192.168.1.1");
        assert1Result(filter);
    }
    
    @Test
    public void testNodeFilter(){
        
        NodeFilter filter = new NodeFilter(1);
        assert1Result(filter);
        
    }
    
    @Test
    @Transactional
    public void testNotificationIdFilter(){
        NotificationIdFilter filter = new NotificationIdFilter(1);
        
        assert1Result(filter);
    }
    
    @Test
    @Transactional
    public void testNotificationIdListFilter(){
        int[] ids = {1};
        NotificationIdListFilter filter = new NotificationIdListFilter(ids);
        assert1Result(filter);
    }
    
    @Test
    @Transactional
    public void testResponderFilter(){
        m_daoNotificationRepo.acknowledgeMatchingNotification("TestUser", new Date(), new NotificationCriteria());
        
        ResponderFilter filter = new ResponderFilter("TestUser");
        assert1Result(filter);
        
    }
    
    @Test
    @Transactional
    public void testServiceFilter(){
        Notification[] notifs = m_daoNotificationRepo.getMatchingNotifications(new NotificationCriteria());
        System.out.println(notifs[0].getServiceId());
        
        ServiceFilter filter = new ServiceFilter(1);
        assert1Result(filter);
    }
    
    @Test
    @Transactional
    public void testUserFilter(){
        UserFilter filter = new UserFilter("TestUser");
        assert1Result(filter);
    }
    
    private void assert1Result(Filter filter){
        System.out.println(filter.getSql());
        NotificationCriteria criteria = new NotificationCriteria(filter);
        Notification[] notifs = m_daoNotificationRepo.getMatchingNotifications(criteria);
        assertEquals(1, notifs.length);
        
        notifs = m_jdbcNotificationRepo.getMatchingNotifications(criteria);
        assertEquals(1, notifs.length);
    }
}
