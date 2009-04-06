/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web.alarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
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
@ContextConfiguration(locations={"classpath:/META-INF/opennms/applicationContext-dao.xml",
                                 "classpath:/daoWebAlarmRepositoryTestContext.xml"})
public class DaoWebAlarmRepositoryTest {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    DaoWebAlarmRepository m_alarmRepo;
    
    @Before
    public void setUp(){
       m_dbPopulator.populateDatabase();
    }
    
    @After
    public void tearDown(){
        
    }
    
    @Test
    public void testGetAlarmById(){
        Alarm alarm = m_alarmRepo.getAlarm(1);
        assertNotNull(alarm);
        
        assertEquals(1, alarm.id);
        assertEquals("uei.opennms.org/test", alarm.uei);
        assertEquals("localhost", alarm.dpName);
        assertEquals(1, alarm.count);
        assertEquals(3, alarm.severity.getId());
        
    }
    
    @Test
    public void testCountMatchingAlarms(){
        int alarms = m_alarmRepo.countMatchingAlarms(new AlarmCriteria(new AlarmIdFilter(1)));
        assertEquals(1, alarms);
        
        alarms = m_alarmRepo.countMatchingAlarms(new AlarmCriteria(new AlarmIdFilter(2)));
        assertEquals(0, alarms);
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
    public void testGetMatchingAlarms(){
        Alarm[] alarms = m_alarmRepo.getMatchingAlarms(new AlarmCriteria(new SeverityFilter(OnmsSeverity.NORMAL), new AlarmIdFilter(1)));
        assertNotNull(alarms);
        assertEquals(1, alarms.length);
        
        alarms = m_alarmRepo.getMatchingAlarms(new AlarmCriteria(new SeverityFilter(OnmsSeverity.MAJOR)));
        assertNotNull(alarms);
        assertEquals(0, alarms.length);
    }
    
    @Test
    public void testAcknowledgeUnacknowledge(){
        m_alarmRepo.acknowledgeMatchingAlarms("TestUser", new Date(), new AlarmCriteria(new AlarmIdFilter(1)));
        
        int matchingAlarmCount = m_alarmRepo.countMatchingAlarms(new AlarmCriteria(new AcknowledgedByFilter("TestUser")));
        
        assertEquals(1, matchingAlarmCount);
        
        m_alarmRepo.unacknowledgeMatchingAlarms(new AlarmCriteria(new AlarmIdFilter(1)));
        
        matchingAlarmCount = m_alarmRepo.countMatchingAlarms(new AlarmCriteria(new AcknowledgedByFilter("TestUser")));
        
        assertEquals(0, matchingAlarmCount);
    }
    
    @Test
    public void testAcknowledgeUnacknowledgeAllAlarms(){
        m_alarmRepo.acknowledgeAll("TestUser", new Date());
        
        int matchingAlarmCount = m_alarmRepo.countMatchingAlarms(new AlarmCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(1, matchingAlarmCount);
        
        m_alarmRepo.unacknowledgeAll();
        
        matchingAlarmCount = m_alarmRepo.countMatchingAlarms(new AlarmCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(0, matchingAlarmCount);
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
    
    @Test
    public void testRepsoitoryWiredIn(){
        assertNotNull(m_alarmRepo);
    }
}
