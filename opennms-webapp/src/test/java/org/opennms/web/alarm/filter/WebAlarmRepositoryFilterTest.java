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
package org.opennms.web.alarm.filter;

import static org.junit.Assert.assertEquals;

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
import org.opennms.web.alarm.Alarm;
import org.opennms.web.alarm.DaoWebAlarmRepository;
import org.opennms.web.alarm.JdbcWebAlarmRepository;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations= {"classpath:/META-INF/opennms/applicationContext-dao.xml",
                                  "classpath:/daoWebAlarmRepositoryTestContext.xml",
                                  "classpath:/jdbcWebAlarmRepositoryTest.xml"})
@JUnitTemporaryDatabase()
public class WebAlarmRepositoryFilterTest {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    DaoWebAlarmRepository m_daoAlarmRepo;
    
    @Autowired
    JdbcWebAlarmRepository m_jdbcWebAlarmRepo;
    
    @Before
    public void setUp(){
        m_dbPopulator.populateDatabase();
    }
    
    @After
    public void tearDown(){
        
    }
    
    @Test
    @Transactional
    public void testAlarmTypeFilter(){
        Alarm[] alarm = m_daoAlarmRepo.getMatchingAlarms(new AlarmCriteria(new AlarmTypeFilter(3)));
        assertEquals(0, alarm.length);
        
        alarm = m_jdbcWebAlarmRepo.getMatchingAlarms(new AlarmCriteria(new AlarmTypeFilter(3)));
        assertEquals(0, alarm.length);
        
        alarm = m_daoAlarmRepo.getMatchingAlarms(new AlarmCriteria(new AlarmTypeFilter(1)));
        assertEquals(1, alarm.length);
        
        alarm = m_jdbcWebAlarmRepo.getMatchingAlarms(new AlarmCriteria(new AlarmTypeFilter(1)));
        assertEquals(1, alarm.length);
    }
    
    @Test
    @Transactional
    public void testBeforeFirstEventTimeFilter(){
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(new AlarmCriteria(new BeforeFirstEventTimeFilter(new Date())));
        assertEquals(1, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(new AlarmCriteria(new BeforeFirstEventTimeFilter(new Date())));
        assertEquals(1, alarms.length);
        
        alarms = m_daoAlarmRepo.getMatchingAlarms(new AlarmCriteria(new BeforeLastEventTimeFilter(new Date())));
        assertEquals(1, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(new AlarmCriteria(new BeforeLastEventTimeFilter(new Date())));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    public void testBeforeLastEventTime(){
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(new AlarmCriteria(new BeforeLastEventTimeFilter(new Date())));
        assertEquals(1, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(new AlarmCriteria(new BeforeLastEventTimeFilter(new Date())));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    public void testExactUeiFilter(){
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(new AlarmCriteria(new ExactUEIFilter("test uei")));
        assertEquals(0, alarms.length);
        
        alarms = m_daoAlarmRepo.getMatchingAlarms(new AlarmCriteria(new ExactUEIFilter("uei.opennms.org/test")));
        assertEquals(1, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(new AlarmCriteria(new ExactUEIFilter("test uei")));
        assertEquals(0, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(new AlarmCriteria(new ExactUEIFilter("uei.opennms.org/test")));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    public void testInterfaceFilter(){
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(new AlarmCriteria(new InterfaceFilter("192.168.1.1")));
        assertEquals(1, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(new AlarmCriteria(new InterfaceFilter("192.168.1.1")));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    public void testNegativeAcknowledgeByFilter(){
        AlarmCriteria criteria = new AlarmCriteria(new NegativeAcknowledgedByFilter("non user"));
        
        Alarm[] alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
        
        alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    public void testIPLikeFilter(){
        AlarmCriteria criteria = new AlarmCriteria(new IPAddrLikeFilter("192.168.1.1"));
        
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
        assertEquals("192.168.1.1", alarms[0].getIpAddress());
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
        assertEquals("192.168.1.1", alarms[0].getIpAddress());
        
    }
    
    @Test
    @Transactional
    public void testNegativeInterfaceFilter(){
        AlarmCriteria criteria = new AlarmCriteria(new NegativeInterfaceFilter("192.168.1.101"));
        
        Alarm[] alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
        
        alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    public void testNegativeNodeFilter(){
        AlarmCriteria criteria = getCriteria(new NegativeNodeFilter(11));
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    public void testNegativeExactUeiFilter(){
        AlarmCriteria criteria = getCriteria(new NegativeExactUEIFilter("uei.opennms.org/bogus"));
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    public void testNegativePartialUEIFilter(){
        AlarmCriteria criteria = getCriteria(new NegativePartialUEIFilter("uei.opennms.org"));
        
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(0, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(0, alarms.length);
    }
    
    @Test
    @Transactional
    public void testNegativeServiceFilter(){
        AlarmCriteria criteria = getCriteria(new NegativeServiceFilter(12));
        
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    public void testNegativeSeverityFilter(){
        AlarmCriteria criteria = getCriteria(new NegativeSeverityFilter(OnmsSeverity.CRITICAL));
        
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    public void testNodeFilter(){
        AlarmCriteria criteria = getCriteria(new NodeFilter(1));
        
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
        
        criteria = getCriteria(new NodeFilter(100));
        
        alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(0, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(0, alarms.length);
    }
    
    @Test
    @Transactional
    public void testNodeNameLikeFilter(){
        AlarmCriteria criteria = getCriteria(new NodeNameLikeFilter("mr"));
        
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(0, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(0, alarms.length);
    }
    
    @Test
    @Transactional
    public void testSeverityBetweenFilter(){
        AlarmCriteria criteria = getCriteria(new SeverityBetweenFilter(OnmsSeverity.CLEARED, OnmsSeverity.MAJOR));
        
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    public void testServiceFilter(){
        AlarmCriteria criteria = getCriteria(new ServiceFilter(1));
        
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(0, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(0, alarms.length);
    }
    
    @Test
    @Transactional
    public void testAfterFirstEventTime(){
        AlarmCriteria criteria = getCriteria(new AfterFirstEventTimeFilter(new Date()));
        
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(0, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(0, alarms.length);
    }
    
    @Test
    @Transactional
    public void testDescriptionSubstringFilter(){
        AlarmCriteria criteria = getCriteria(new DescriptionSubstringFilter("alarm"));
        
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
        
        //alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        //assertEquals(1, alarms);
    }
    
    @Test
    @Transactional
    public void testLogMessageSubstringFilter(){
        AlarmCriteria criteria = getCriteria(new LogMessageSubstringFilter("this is a test"));
        
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
        
        //alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        //assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    public void testLogMessageMatchAnyFilter(){
        AlarmCriteria criteria = getCriteria(new LogMessageMatchesAnyFilter("log"));
        
        Alarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
        
        alarms = m_jdbcWebAlarmRepo.getMatchingAlarms(criteria);
        assertEquals(1, alarms.length);
    }
    
    private AlarmCriteria getCriteria(Filter...filters){
        return new AlarmCriteria(filters);
    }

    
}
