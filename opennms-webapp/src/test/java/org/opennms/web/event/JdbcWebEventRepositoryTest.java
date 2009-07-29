package org.opennms.web.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.event.filter.AcknowledgedByFilter;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.EventIdFilter;
import org.opennms.web.event.filter.NegativeSeverityFilter;
import org.opennms.web.event.filter.SeverityFilter;
import org.opennms.web.filter.Filter;
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
                                  "classpath*:/META-INF/opennms/component-dao.xml",
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
        EventCriteria criteria = new EventCriteria();
        int[] matchingEvents = m_eventRepo.countMatchingEventsBySeverity(criteria);
        
        assertEquals(8, matchingEvents.length);
        
        assertEquals(0, matchingEvents[OnmsSeverity.CLEARED.getId()]);
        assertEquals(0, matchingEvents[OnmsSeverity.CRITICAL.getId()]);
        assertEquals(1, matchingEvents[OnmsSeverity.INDETERMINATE.getId()]);
        assertEquals(0, matchingEvents[OnmsSeverity.MAJOR.getId()]);
        assertEquals(0, matchingEvents[OnmsSeverity.MINOR.getId()]);
        assertEquals(0, matchingEvents[OnmsSeverity.NORMAL.getId()]);
        assertEquals(0, matchingEvents[OnmsSeverity.WARNING.getId()]);
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
    
    @Test
    public void testFilterBySeverity() {
        NegativeSeverityFilter filter = new NegativeSeverityFilter(OnmsSeverity.NORMAL.getId());
        
        EventCriteria criteria = new EventCriteria(filter);
        Event[] events = m_eventRepo.getMatchingEvents(criteria);
        assertTrue(events.length > 0);
        
        EventCriteria sortedCriteria = new EventCriteria(new Filter[] { filter }, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, 100, 0);
        Event[] sortedEvents = m_eventRepo.getMatchingEvents(sortedCriteria);
        assertTrue(sortedEvents.length > 0);
         
    }
    
    @Test
    public void testDoubleFilterTest(){
        m_eventRepo.acknowledgeAll("TestUser", new Date());
        
        EventIdFilter filter1 = new EventIdFilter(1);
        AcknowledgedByFilter filter2 = new AcknowledgedByFilter("TestUser");
        EventCriteria criteria = new EventCriteria(filter1, filter2);
        
        Event[] events = m_eventRepo.getMatchingEvents(criteria);
        assertEquals(1, events.length);
        
    }

}
