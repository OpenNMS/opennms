package org.opennms.web.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.web.event.filter.AcknowledgedByFilter;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.EventIdFilter;
import org.opennms.web.event.filter.SeverityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations= {"classpath:/META-INF/opennms/applicationContext-dao.xml",
                                  "classpath:/jdbcWebEventRepositoryTestContext.xml"})
@JUnitTemporaryDatabase()
public class JdbcWebEventRepositoryTest {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    WebEventRepository m_eventRepo;
    
    @Before
    public void setUp(){
        assertNotNull(m_eventRepo);
        m_dbPopulator.populateDatabase();
    }
    
    @Test
    public void testCountMatchingEvents(){ 
        EventCriteria criteria = new EventCriteria();
        int event = m_eventRepo.countMatchingEvents(criteria);
        
        assertEquals(1, event);
    }
    
    @Test
    public void testCountMatchingEventsBySeverity(){
        EventCriteria criteria = new EventCriteria(new SeverityFilter(3));
        int[] matchingAlarms = m_eventRepo.countMatchingEventsBySeverity(criteria);
        
        assertEquals(8, matchingAlarms.length);
    }
    
    @Test
    public void testGetEvent(){
        Event event = m_eventRepo.getEvent(1);
        assertNotNull(event);
    }
    
    @Test
    public void testAcknowledgeUnacknowledgeMatchingAlarms(){
        m_eventRepo.acknowledgeMatchingEvents("TestUser", new Date(), new EventCriteria(new EventIdFilter(1)));
        
        int matchingEventCount = m_eventRepo.countMatchingEvents(new EventCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(1, matchingEventCount);
        
        m_eventRepo.unacknowledgeMatchingEvents(new EventCriteria(new AcknowledgedByFilter("TestUser")));
        
        matchingEventCount = m_eventRepo.countMatchingEvents(new EventCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(0, matchingEventCount);
    }
    
    @Test
    public void testAcknowledgeUnacknowledgeAllAlarms(){
        m_eventRepo.acknowledgeAll("TestUser", new Date());
        
        int matchingEventCount = m_eventRepo.countMatchingEvents(new EventCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(1, matchingEventCount);
        
        m_eventRepo.unacknowledgeAll();
        
        matchingEventCount = m_eventRepo.countMatchingEvents(new EventCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(0, matchingEventCount);
    }
    
    @Test
    public void testCountMatchingBySeverity(){
        
        int[] matchingEventCount = m_eventRepo.countMatchingEventsBySeverity(new EventCriteria(new SeverityFilter(3)));
        assertEquals(8, matchingEventCount.length);
    }

}
