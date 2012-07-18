/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.alarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.alarm.filter.AcknowledgedByFilter;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.alarm.filter.AlarmIdFilter;
import org.opennms.web.alarm.filter.SeverityFilter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/jdbcWebRepositoryTestContext.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class JdbcWebAlarmRepositoryTest implements InitializingBean {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    WebAlarmRepository m_alarmRepo;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Before
    public void setUp(){
        m_dbPopulator.populateDatabase();
    }
    
    @After
    public void tearDown(){
        
    }
   
    @Test
    @Transactional
    public void testCountMatchingAlarms(){
        AlarmCriteria criteria = new AlarmCriteria(new AlarmIdFilter(1));
        int alarms = m_alarmRepo.countMatchingAlarms(criteria);
        
        assertEquals(1, alarms);
    }
    
    
    @Test
    @Transactional
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
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testGetAlarm(){
        Alarm[] alarms = m_alarmRepo.getMatchingAlarms(new AlarmCriteria(new AlarmIdFilter(1)));
        assertNotNull(alarms);
        assertEquals(1, alarms.length);
        
        Alarm alarm = m_alarmRepo.getAlarm(1);
        assertNotNull(alarm);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
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
    @Transactional
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
    @Transactional
    public void testCountMatchingBySeverity(){
        int[] matchingAlarmCount = m_alarmRepo.countMatchingAlarmsBySeverity(new AlarmCriteria(new SeverityFilter(OnmsSeverity.NORMAL)));
        assertEquals(8, matchingAlarmCount.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testEscalateAlarms(){
        int[] alarmIds = {1};
        m_alarmRepo.escalateAlarms(alarmIds, "TestUser", new Date());
        
        Alarm[] alarms = m_alarmRepo.getMatchingAlarms(new AlarmCriteria(new AlarmIdFilter(1)));
        
        assertNotNull(alarms);
        
        assertEquals(OnmsSeverity.WARNING.getId(), alarms[0].severity.getId());
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
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
