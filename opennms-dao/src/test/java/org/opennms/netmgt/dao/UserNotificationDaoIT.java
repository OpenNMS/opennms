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
package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.dao.api.UserNotificationDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsUserNotification;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class UserNotificationDaoIT implements InitializingBean {
	
    @Autowired
    private DistPollerDao m_distPollerDao;
	
    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private UserNotificationDao m_userNotificationDao;

    @Autowired
    private NotificationDao m_notificationDao;

    @Autowired
    private EventDao m_eventDao;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    private static boolean m_populated = false;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @BeforeTransaction
    public void setUp() {
        if (!m_populated) {
            m_databasePopulator.populateDatabase();
            m_populated = true;
        }
    }

    @Test
    @Transactional
    public void testSaveUserNotification() {
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(m_distPollerDao.whoami());
        event.setEventCreateTime(new Date());
        event.setEventDescr("event dao test");
        event.setEventHost("localhost");
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventLogGroup("event dao test log group");
        event.setEventLogMsg("event dao test log msg");
        event.setEventSeverity(OnmsSeverity.CRITICAL.getId());
        event.setEventSource("EventDaoTest");
        event.setEventTime(new Date());
        event.setEventUei("uei://org/opennms/test/UserNotificationDaoTest");
        OnmsAlarm alarm = new OnmsAlarm();
        event.setAlarm(alarm);

        OnmsNode node = (OnmsNode) m_nodeDao.findAll().iterator().next();
        OnmsIpInterface iface = (OnmsIpInterface)node.getIpInterfaces().iterator().next();
        OnmsMonitoredService service = (OnmsMonitoredService)iface.getMonitoredServices().iterator().next();
        event.setNode(node);
	    event.setServiceType(service.getServiceType());
        event.setIpAddr(iface.getIpAddress());
        m_eventDao.save(event);
        OnmsEvent newEvent = m_eventDao.load(event.getId());
        assertEquals("uei://org/opennms/test/UserNotificationDaoTest", newEvent.getEventUei());
        
        
        OnmsNotification notification = new OnmsNotification();
        notification.setEvent(newEvent);
        notification.setTextMsg("Tests are fun!");
        m_notificationDao.save(notification);
       
        OnmsNotification newNotification = m_notificationDao.load(notification.getNotifyId());
        assertEquals("uei://org/opennms/test/UserNotificationDaoTest", newNotification.getEvent().getEventUei());
        
        OnmsUserNotification userNotif = new OnmsUserNotification();
        userNotif.setNotification(notification);
        userNotif.setNotifyTime(new Date());
        userNotif.setUserId("OpenNMS User");
        userNotif.setMedia("E-mail");
        userNotif.setContactInfo("test@opennms.org");
        m_userNotificationDao.save(userNotif);
        
        assertNotNull(userNotif.getNotification());
        assertEquals(userNotif.getUserId(), "OpenNMS User");
    }
}
