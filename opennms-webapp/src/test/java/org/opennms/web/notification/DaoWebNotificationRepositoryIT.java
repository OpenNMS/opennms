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
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.filter.Filter;
import org.opennms.web.notification.filter.AcknowledgedByFilter;
import org.opennms.web.notification.filter.NotificationCriteria;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-mockEventd.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/daoWebRepositoryTestContext.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class DaoWebNotificationRepositoryIT implements InitializingBean {

    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    WebNotificationRepository m_daoNotificationRepo;
    
    @Before
    public void setUp(){
        m_dbPopulator.populateDatabase();
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Test
    @Transactional
    public void testNotificationCount(){
        List<Filter> filterList = new ArrayList<>();
        Filter[] filters = filterList.toArray(new Filter[0]);
        AcknowledgeType ackType = AcknowledgeType.UNACKNOWLEDGED;
        int notificationCount = m_daoNotificationRepo.countMatchingNotifications(new NotificationCriteria(ackType, filters));
        assertEquals(1, notificationCount);
    }

    @Test
    @Transactional
    public void testGetMatchingNotifications() {
        List<Filter> filterList = new ArrayList<>();
        int limit = 10;
        int multiple = 0;
        AcknowledgeType ackType = AcknowledgeType.UNACKNOWLEDGED;
        SortStyle sortStyle = SortStyle.DEFAULT_SORT_STYLE;
        Filter[] filters = filterList.toArray(new Filter[0]);
        Notification[] notices = m_daoNotificationRepo.getMatchingNotifications(new NotificationCriteria(filters, sortStyle, ackType, limit, limit * multiple));
        assertEquals(1, notices.length);
        assertEquals("This is a test notification", notices[0].getTextMessage());
    }

    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testGetNotification(){
        Notification notice = m_daoNotificationRepo.getNotification(1);
        assertNotNull(notice);
    }
    
    @Test
    @Transactional
    public void testAcknowledgeNotification(){
        m_daoNotificationRepo.acknowledgeMatchingNotification("TestUser", new Date(), new NotificationCriteria());
        
        int notifCount = m_daoNotificationRepo.countMatchingNotifications(new NotificationCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(1, notifCount);
        
        Notification[] notif = m_daoNotificationRepo.getMatchingNotifications(new NotificationCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(1, notif.length);
        assertEquals("TestUser", notif[0].m_responder);
    }
}
