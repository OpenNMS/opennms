package org.opennms.netmgt.dao.hibernate;

import java.util.Collection;

import org.hibernate.SessionFactory;
import org.opennms.netmgt.dao.BaseDaoTestCase;
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
import org.springframework.orm.hibernate3.HibernateTemplate;

public class AnnotationTest extends BaseDaoTestCase {
	
	private SessionFactory m_sessionFactory;
        
            
	public void setSessionFactory(SessionFactory sessionFactory) {
		m_sessionFactory = sessionFactory;
	}
	
	
	public interface Checker<T> {
		public void checkCollection(Collection<T> collection);
		public void check(T entity);
	}
	
	public class NullChecker<T> implements Checker<T> {

		public void check(T entity) {
		}

		public void checkCollection(Collection<T> collection) {
		}
		
	}


	public abstract class EmptyChecker<T> implements Checker<T> {
		public void checkCollection(Collection<T> collection) {
			assertFalse(collection.isEmpty());
		}
	}

	public void testDistPoller() {
		assertLoadAll(OnmsDistPoller.class, new EmptyChecker<OnmsDistPoller>() {

			public void check(OnmsDistPoller entity) {
				assertNotNull(entity.getName());
			}

		});
	}
	
	public void testAssetRecord() {
		assertLoadAll(OnmsAssetRecord.class, new EmptyChecker<OnmsAssetRecord>() {

			public void check(OnmsAssetRecord entity) {
				assertNotNull(entity.getNode());
				assertNotNull(entity.getNode().getLabel());
			}
			
		});
	}
	
	public void testNode() {
		assertLoadAll(OnmsNode.class, new EmptyChecker<OnmsNode>() {

			public void check(OnmsNode entity) {
				assertNotNull(entity.getAssetRecord());
				assertNotNull(entity.getAssetRecord().getId());
				assertNotNull(entity.getDistPoller());
				assertNotNull(entity.getDistPoller().getName());
				assertNotNull(entity.getCategories());
				entity.getCategories().size();
				assertNotNull(entity.getIpInterfaces());
				assertTrue(entity.getIpInterfaces().size() > 0);
				assertNotNull(entity.getSnmpInterfaces());
				assertTrue(entity.getSnmpInterfaces().size() >= 0);
			}
			
		});
		
	}
	
	public void testIpInterfaces() {
		assertLoadAll(OnmsIpInterface.class, new EmptyChecker<OnmsIpInterface>() {

			public void check(OnmsIpInterface entity) {
				assertNotNull(entity.getIpAddress());
				assertNotNull(entity.getNode());
				assertNotNull(entity.getNode().getLabel());
				assertNotNull(entity.getMonitoredServices());
				assertTrue(entity.getMonitoredServices().size() >= 0);
			}
			
		});
	}
	
	public void testSnmpInterfaces() {
		assertLoadAll(OnmsSnmpInterface.class, new EmptyChecker<OnmsSnmpInterface>() {

			public void check(OnmsSnmpInterface entity) {
				assertNotNull(entity.getIfIndex());
				assertNotNull(entity.getNode());
				assertNotNull(entity.getNode().getLabel());
				assertNotNull(entity.getCollectionType());
				assertNotNull(entity.getIpInterfaces());
				assertTrue(entity.getIpInterfaces().size() > 0);
			}
			
		});
	}
	
	public void testCategories() {
		assertLoadAll(OnmsCategory.class, new EmptyChecker<OnmsCategory>() {

			public void check(OnmsCategory entity) {
				assertNotNull(entity.getName());
			}
			
		});
	}
	
	public void testMonitoredServices() {
		assertLoadAll(OnmsMonitoredService.class, new EmptyChecker<OnmsMonitoredService>() {

			public void check(OnmsMonitoredService entity) {
				assertNotNull(entity.getIpInterface());
				assertNotNull(entity.getIpAddress());
				assertNotNull(entity.getNodeId());
				assertNotNull(entity.getCurrentOutages());
				assertTrue(entity.getCurrentOutages().size() >= 0);
				assertNotNull(entity.getServiceType());
				assertNotNull(entity.getServiceName());
			}
			
		});
	}
	
	public void testServiceTypes() {
		assertLoadAll(OnmsServiceType.class, new EmptyChecker<OnmsServiceType>() {

			public void check(OnmsServiceType entity) {
				assertNotNull(entity.getId());
				assertNotNull(entity.getName());
			}
			
		});
	}
	
	public void testOutages() {
		assertLoadAll(OnmsOutage.class, new EmptyChecker<OnmsOutage>() {

			public void check(OnmsOutage entity) {
				assertNotNull(entity.getMonitoredService());
				assertNotNull(entity.getIpAddress());
				assertNotNull(entity.getNodeId());
				assertNotNull(entity.getServiceLostEvent());
				assertNotNull(entity.getServiceLostEvent().getEventUei());
				if (entity.getIfRegainedService() != null) {
					assertNotNull(entity.getServiceRegainedEvent());
					assertNotNull(entity.getServiceRegainedEvent().getEventUei());
				}
					
			}
			
		});
	}
	
	public void testEvents() {
		assertLoadAll(OnmsEvent.class, new EmptyChecker<OnmsEvent>() {

			public void check(OnmsEvent entity) {
				if (entity.getAlarm() != null) {
					assertEquals(entity.getEventUei(), entity.getAlarm().getUei());
				}
				assertNotNull(entity.getAssociatedServiceLostOutages());
				assertTrue(entity.getAssociatedServiceLostOutages().size() >= 0);
				assertNotNull(entity.getAssociatedServiceRegainedOutages());
				assertTrue(entity.getAssociatedServiceRegainedOutages().size() >= 0);
				assertNotNull(entity.getDistPoller());
				assertNotNull(entity.getDistPoller().getName());
				assertNotNull(entity.getNotifications());
				assertTrue(entity.getNotifications().size() >= 0);
			}
			
		});
	}
	
	public void testAlarms() {
		assertLoadAll(OnmsAlarm.class, new EmptyChecker<OnmsAlarm>() {

			public void check(OnmsAlarm entity) {
				assertNotNull(entity.getLastEvent());
				assertEquals(entity.getUei(), entity.getLastEvent().getEventUei());
				assertNotNull(entity.getDistPoller());
				assertNotNull(entity.getDistPoller().getName());
			}
			
		});
	}
	
	public void testNotifacations() {
		assertLoadAll(OnmsNotification.class, new NullChecker<OnmsNotification>());
	}
	
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
