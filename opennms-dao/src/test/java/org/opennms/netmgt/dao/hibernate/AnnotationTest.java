/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsUserNotification;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AnnotationTest implements InitializingBean {
	@Autowired
	private SessionFactory m_sessionFactory;
        
	@Autowired
	private DatabasePopulator m_databasePopulator;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

	@Before
	public void setUp() {
		m_databasePopulator.populateDatabase();
	}

	public interface Checker<T> {
		public void checkCollection(Collection<T> collection);
		public void check(T entity);
	}
	
	public class NullChecker<T> implements Checker<T> {

                @Override
		public void check(T entity) {
		}

                @Override
		public void checkCollection(Collection<T> collection) {
		}
		
	}


	public abstract class EmptyChecker<T> implements Checker<T> {
                @Override
		public void checkCollection(Collection<T> collection) {
			assertFalse("collection should not be empty", collection.isEmpty());
		}
	}

	@Test
	@Transactional
	public void testDistPoller() {
		assertLoadAll(OnmsDistPoller.class, new EmptyChecker<OnmsDistPoller>() {

                        @Override
			public void check(OnmsDistPoller entity) {
				assertNotNull("name not should be null", entity.getName());
			}

		});
	}
	
	@Test
	@Transactional
	public void testAssetRecord() {
		assertLoadAll(OnmsAssetRecord.class, new EmptyChecker<OnmsAssetRecord>() {

                        @Override
			public void check(OnmsAssetRecord entity) {
				assertNotNull("node should not be null", entity.getNode());
				assertNotNull("node label should not be null", entity.getNode().getLabel());
			}
			
		});
	}
	
	@Test
	@Transactional
	public void testNode() {
		assertLoadAll(OnmsNode.class, new EmptyChecker<OnmsNode>() {

                        @Override
			public void check(OnmsNode entity) {
				assertNotNull("asset record should not be null", entity.getAssetRecord());
				assertNotNull("asset record ID should not be null", entity.getAssetRecord().getId());
				assertNotNull("dist poller should not be null", entity.getDistPoller());
				assertNotNull("dist poller name should not be null", entity.getDistPoller().getName());
				assertNotNull("categories list should not be null", entity.getCategories());
				entity.getCategories().size();
				assertNotNull("ip interfaces list should not be null", entity.getIpInterfaces());
				assertTrue("ip interfaces list size should be greater than zero", entity.getIpInterfaces().size() > 0);
				assertNotNull("snmp interfaces list should not be null", entity.getSnmpInterfaces());
				assertTrue("snmp interfaces list should be greater than or equal to zero", entity.getSnmpInterfaces().size() >= 0);
			}
			
		});
		
	}

	@Test
	@Transactional
	public void testIpInterfaces() {
		assertLoadAll(OnmsIpInterface.class, new EmptyChecker<OnmsIpInterface>() {

                        @Override
			public void check(OnmsIpInterface entity) {
				assertNotNull("ip address should not be null", entity.getIpAddress());
				assertNotNull("node should not be null", entity.getNode());
				assertNotNull("node label should not be null", entity.getNode().getLabel());
				assertNotNull("monitored services list should not be null", entity.getMonitoredServices());
				assertTrue("number of monitored services should be greater than or equal to zero", entity.getMonitoredServices().size() >= 0);
			}
			
		});
	}
	
	@Test
	@Transactional
	public void testSnmpInterfaces() {
		assertLoadAll(OnmsSnmpInterface.class, new EmptyChecker<OnmsSnmpInterface>() {

                        @Override
			public void check(OnmsSnmpInterface entity) {
				assertNotNull("ifindex should not be null", entity.getIfIndex());
				assertNotNull("node should not be null", entity.getNode());
				assertNotNull("node label should not be null", entity.getNode().getLabel());
				assertNotNull("collect should not by null", entity.getCollect());
				assertNotNull("ip interfaces list should not be null", entity.getIpInterfaces());
				assertTrue("ip interfaces list size should be greater than 0", entity.getIpInterfaces().size() > 0);
			}
			
		});
	}
	
	@Test
	@Transactional
	public void testCategories() {
		assertLoadAll(OnmsCategory.class, new EmptyChecker<OnmsCategory>() {

                        @Override
			public void check(OnmsCategory entity) {
				assertNotNull("name should not be null", entity.getName());
			}
			
		});
	}
	
	@Test
	@Transactional
	public void testMonitoredServices() {
		assertLoadAll(OnmsMonitoredService.class, new EmptyChecker<OnmsMonitoredService>() {

                        @Override
			public void check(OnmsMonitoredService entity) {
				assertNotNull("ip interface should be null", entity.getIpInterface());
				assertNotNull("ip address should not be null", entity.getIpAddress());
				assertNotNull("node ID should not be null", entity.getNodeId());
				assertNotNull("current outages list should not be null", entity.getCurrentOutages());
				assertTrue("current outage count should be greater than or equal to zero", entity.getCurrentOutages().size() >= 0);
				assertNotNull("service type should not be null", entity.getServiceType());
				assertNotNull("service name should not be null", entity.getServiceName());
			}
			
		});
	}
	
	@Test
	@Transactional
	public void testServiceTypes() {
		assertLoadAll(OnmsServiceType.class, new EmptyChecker<OnmsServiceType>() {

                        @Override
			public void check(OnmsServiceType entity) {
				assertNotNull("id should not be null", entity.getId());
				assertNotNull("name should not be null", entity.getName());
			}
			
		});
	}
	
	@Test
	@Transactional
    public void testOutages() {
		assertLoadAll(OnmsOutage.class, new EmptyChecker<OnmsOutage>() {

                        @Override
			public void check(OnmsOutage entity) {
				assertNotNull("monitored service should not be null", entity.getMonitoredService());
				assertNotNull("ip address should not be null", entity.getIpAddress());
				assertNotNull("node ID should not be null", entity.getNodeId());
				assertNotNull("service lost event should not be null", entity.getServiceLostEvent());
				assertNotNull("service lost event UEI should not be null", entity.getServiceLostEvent().getEventUei());
				if (entity.getIfRegainedService() != null) {
					assertNotNull("outage has ended (ifregainedservice) so service regained event should not be null", entity.getServiceRegainedEvent());
					assertNotNull("outage has ended (ifregainedservice) so service regained event UEI should not be null", entity.getServiceRegainedEvent().getEventUei());
				}
					
			}
			
		});
	}
	
	@Test
	@Transactional
	public void testEvents() {
		assertLoadAll(OnmsEvent.class, new EmptyChecker<OnmsEvent>() {

                        @Override
			public void check(OnmsEvent entity) {
				if (entity.getAlarm() != null) {
					assertEquals("event UEI should equal the alarm UEI", entity.getEventUei(), entity.getAlarm().getUei());
				}
				assertNotNull("associated service lost outages list should not be null", entity.getAssociatedServiceLostOutages());
				assertTrue("there should be zero or more associated service lost outages", entity.getAssociatedServiceLostOutages().size() >= 0);
				assertNotNull("associated service regained outages list should not be null", entity.getAssociatedServiceRegainedOutages());
				assertTrue("there should be zero or more associated service regained outages", entity.getAssociatedServiceRegainedOutages().size() >= 0);
				assertNotNull("dist poller should not be null", entity.getDistPoller());
				assertNotNull("dist poller name should not be null", entity.getDistPoller().getName());
				assertNotNull("notifications list should not be null", entity.getNotifications());
				assertTrue("notifications list size should be greater than or equal to zero", entity.getNotifications().size() >= 0);
			}
			
		});
	}
	
	@Test
	@Transactional
    public void testAlarms() {
		assertLoadAll(OnmsAlarm.class, new EmptyChecker<OnmsAlarm>() {

                        @Override
			public void check(OnmsAlarm entity) {
				assertNotNull("last event should not be null", entity.getLastEvent());
				assertEquals("alarm UEI should match the last event UEI", entity.getUei(), entity.getLastEvent().getEventUei());
				assertNotNull("dist poller should not be null", entity.getDistPoller());
				assertNotNull("dist poller name should not be null", entity.getDistPoller().getName());
			}
			
		});
	}
	
	@Test
	@Transactional
	public void testNotifacations() {
		assertLoadAll(OnmsNotification.class, new NullChecker<OnmsNotification>());
	}
	
	@Test
	@Transactional
	public void testUsersNotified() {
		assertLoadAll(OnmsUserNotification.class, new NullChecker<OnmsUserNotification>());
	}
	
	private <T> void assertLoadAll(Class<T> annotatedClass, Checker<T> checker) {
		HibernateTemplate template = new HibernateTemplate(m_sessionFactory);
		Collection<T> results = template.loadAll(annotatedClass);
		assertNotNull(results);
		
		checker.checkCollection(results);
		
		for (T t : results) {
			checker.check(t);
            // we only need to check one
            break;
		}
	}
}
