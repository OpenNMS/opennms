package org.opennms.web.alarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.alarm.filter.AlarmIdFilter;
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
                                  "classpath:/jdbcWebAlarmRepositoryTest.xml"})
@JUnitTemporaryDatabase()
public class JdbcWebAlarmRepositoryTest{
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    WebAlarmRepository m_alarmRepo;
    
    @Before
    public void setUp(){
        assertNotNull(m_alarmRepo);
        m_dbPopulator.populateDatabase();
    }
    
    @After
    public void tearDown(){
        
    }
   
    @Test
    public void testCountMatchingAlarms(){
        AlarmCriteria criteria = new AlarmCriteria(new AlarmIdFilter(1));
        int alamrs = m_alarmRepo.countMatchingAlarms(criteria);
        
        assertEquals(1, alamrs);
    }
    
    
    @Test
    public void testCountMatchingAlarmsBySeverity(){
        AlarmCriteria criteria = new AlarmCriteria();
        int [] matchingAlarms = m_alarmRepo.countMatchingAlarmsBySeverity(criteria);
        
        assertEquals(8, matchingAlarms.length);
    }
}
