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
package org.opennms.web.notification;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.test.JUnitConfigurationEnvironment;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/daoWebRepositoryTestContext.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class WebNotificationRepositoryFilterIT implements InitializingBean {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    @Qualifier("dao")
    WebNotificationRepository m_daoNotificationRepo;

    @Override
    public void afterPropertiesSet() throws Exception {

    }
    
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
    @Transactional
    public void testInterfaceFilter(){
        InterfaceFilter filter = new InterfaceFilter("192.168.1.1");
        assert1Result(filter);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testNodeFilter(){
        
        NodeFilter filter = new NodeFilter(1);
        assert1Result(filter);
        
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testNotificationIdFilter(){
        NotificationIdFilter filter = new NotificationIdFilter(1);
        
        assert1Result(filter);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
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
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testServiceFilter(){
        Notification[] notifs = m_daoNotificationRepo.getMatchingNotifications(new NotificationCriteria());
        System.out.println(notifs[0].getServiceId());
        
        ServiceFilter filter = new ServiceFilter(1, null);
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
    }
}
