package org.opennms.web.alarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.alarm.filter.AcknowledgedByFilter;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.alarm.filter.AlarmIdFilter;
import org.opennms.web.alarm.filter.SeverityFilter;
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
        
        //Make sure that the count is correct per severity
        assertEquals(0, matchingAlarms[OnmsSeverity.CLEARED.getId()]);
        assertEquals(0, matchingAlarms[OnmsSeverity.CRITICAL.getId()]);
        assertEquals(0, matchingAlarms[OnmsSeverity.INDETERMINATE.getId()]);
        assertEquals(0, matchingAlarms[OnmsSeverity.MINOR.getId()]);
        assertEquals(1, matchingAlarms[OnmsSeverity.NORMAL.getId()]);
        assertEquals(0, matchingAlarms[OnmsSeverity.WARNING.getId()]);
        assertEquals(0, matchingAlarms[OnmsSeverity.MAJOR.getId()]);
    }
    
    @Test
    public void testGetAlarm(){
        Alarm[] alarms = m_alarmRepo.getMatchingAlarms(new AlarmCriteria(new AlarmIdFilter(1)));
        assertNotNull(alarms);
        assertEquals(1, alarms.length);
        
        Alarm alarm = m_alarmRepo.getAlarm(1);
        assertNotNull(alarm);
    }
    
    @Test
    public void testAcknowledgeUnacknowledgeMatchingAlarms(){
        String user = "TestUser";
        m_alarmRepo.acknowledgeMatchingAlarms(user, new Date(), new AlarmCriteria(new AlarmIdFilter(1)));
        
        int matchingAlarmCount = m_alarmRepo.countMatchingAlarms(new AlarmCriteria(new AcknowledgedByFilter(user)));
        
        assertEquals(1, matchingAlarmCount);
        
        m_alarmRepo.unacknowledgeMatchingAlarms(new AlarmCriteria(new AlarmIdFilter(1)), user);
        
        matchingAlarmCount = m_alarmRepo.countMatchingAlarms(new AlarmCriteria(new AcknowledgedByFilter(user)));
        
        assertEquals(0, matchingAlarmCount);
        
    }
    
    @Test
    public void testAcknowledgeUnacknowledgeAllAlarms(){
        String user = "TestUser";
        m_alarmRepo.acknowledgeAll(user, new Date());
        
        int matchingAlarmCount = m_alarmRepo.countMatchingAlarms(new AlarmCriteria(new AcknowledgedByFilter(user)));
        assertEquals(1, matchingAlarmCount);
        
        m_alarmRepo.unacknowledgeAll(user);
        
        matchingAlarmCount = m_alarmRepo.countMatchingAlarms(new AlarmCriteria(new AcknowledgedByFilter(user)));
        assertEquals(0, matchingAlarmCount);
    }
    
    @Test
    public void testCountMatchingBySeverity(){
        int[] matchingAlarmCount = m_alarmRepo.countMatchingAlarmsBySeverity(new AlarmCriteria(new SeverityFilter(OnmsSeverity.NORMAL)));
        assertEquals(8, matchingAlarmCount.length);
    }
    
    @Test
    public void testEscalateAlarms(){
        int[] alarmIds = {1};
        m_alarmRepo.escalateAlarms(alarmIds, "TestUser", new Date());
        
        Alarm[] alarms = m_alarmRepo.getMatchingAlarms(new AlarmCriteria(new AlarmIdFilter(1)));
        
        assertNotNull(alarms);
        
        assertEquals(OnmsSeverity.WARNING.getId(), alarms[0].severity.getId());
    }
    
    @Test
    public void testClearAlarms(){
        Alarm alarm = m_alarmRepo.getAlarm(1);
        
        assertNotNull(alarm);
        assertEquals(OnmsSeverity.NORMAL.getId(), alarm.severity.getId());
        
        int[] alarmIds = {1};
        m_alarmRepo.clearAlarms(alarmIds, "TestUser", new Date());
        
        alarm = m_alarmRepo.getAlarm(1);
        assertNotNull(alarm);
        assertEquals(OnmsSeverity.CLEARED.getId(), alarm.severity.getId());
    }
    
}
