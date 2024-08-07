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

import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Order;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
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
public class NotificationDaoIT implements InitializingBean {
	@Autowired
	private DatabasePopulator m_databasePopulator;

	@Autowired
	private DistPollerDao m_distPollerDao;
	
	@Autowired
	private NodeDao m_nodeDao;

	@Autowired
	private NotificationDao m_notificationDao;

	@Autowired
	private EventDao m_eventDao;
	
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

	@Before
	public void setUp() {
		m_databasePopulator.populateDatabase();
	}
	
	@Test
	@Transactional
    public void testNotificationSave() {
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
        event.setEventUei("uei://org/opennms/test/NotificationDaoTest");
//        OnmsAlarm alarm = new OnmsAlarm();
//        event.setAlarm(alarm);

        OnmsNode node = m_nodeDao.findAll().iterator().next();
        OnmsIpInterface iface = node.getIpInterfaces().iterator().next();
        OnmsMonitoredService service = iface.getMonitoredServices().iterator().next();
        event.setNode(node);
	    event.setServiceType(service.getServiceType());
        event.setIpAddr(iface.getIpAddress());
        m_eventDao.save(event);
        OnmsEvent newEvent = m_eventDao.load(event.getId());
        assertEquals("uei://org/opennms/test/NotificationDaoTest", newEvent.getEventUei());
        
        OnmsNotification notification = new OnmsNotification();
        notification.setEvent(newEvent);
        notification.setTextMsg("Tests are fun!");
        m_notificationDao.save(notification);
       
        OnmsNotification newNotification = m_notificationDao.load(notification.getNotifyId());
        assertEquals("uei://org/opennms/test/NotificationDaoTest", newNotification.getEvent().getEventUei());
    }

    /**
     * Test to make sure that orderBy across an alias works.
     */
    @Test
    @Transactional
    public void testCriteria() {
        Criteria criteria = new Criteria(OnmsNotification.class);
        criteria.setAliases(Arrays.asList(new Alias[] { 
            new Alias("node", "node", JoinType.LEFT_JOIN),
            new Alias("node.snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN),
            new Alias("node.ipInterfaces", "ipInterface", JoinType.LEFT_JOIN),
            new Alias("event", "event", JoinType.LEFT_JOIN),
            new Alias("usersNotified", "usersNotified", JoinType.LEFT_JOIN),
            new Alias("serviceType", "serviceType", JoinType.LEFT_JOIN)
        }));
        criteria.setOrders(Arrays.asList(new Order[] {
            new Order("event.id", false),
            new Order("event.eventSeverity", false),
            new Order("node.label", false),
            new Order("serviceType.name", false)
        }));
        m_notificationDao.findMatching(criteria);

        CriteriaBuilder builder = new CriteriaBuilder(OnmsNotification.class);
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.alias("node.snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN);
        builder.alias("node.ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);
        builder.alias("event", "event", JoinType.LEFT_JOIN);
        builder.alias("usersNotified", "usersNotified", JoinType.LEFT_JOIN);
        builder.alias("serviceType", "serviceType", JoinType.LEFT_JOIN);
        builder.orderBy("event.id", false);
        builder.orderBy("event.eventSeverity", false);
        builder.orderBy("node.label", false);
        builder.orderBy("serviceType.name", false);

        m_notificationDao.findMatching(builder.toCriteria());
    }
}
