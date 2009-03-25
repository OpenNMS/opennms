package org.opennms.web.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.web.event.filter.EventCriteria;
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

}
