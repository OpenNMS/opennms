/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.alarm.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/daoWebRepositoryTestContext.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@DirtiesContext
public class AlarmRepositoryFilterTest implements InitializingBean {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    AlarmRepository m_daoAlarmRepo;
    
    @Autowired
    ApplicationContext m_appContext;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Before
    public void setUp(){
        m_dbPopulator.populateDatabase();
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testAlarmTypeFilter(){
        OnmsAlarm[] alarm = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AlarmTypeFilter(3))));
        assertEquals(0, alarm.length);
        
        alarm = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AlarmTypeFilter(1))));
        assertEquals(1, alarm.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testBeforeFirstEventTimeFilter(){
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new BeforeFirstEventTimeFilter(new Date()))));
        assertEquals(1, alarms.length);

        alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new BeforeLastEventTimeFilter(new Date()))));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testBeforeLastEventTime(){
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new BeforeLastEventTimeFilter(new Date()))));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testExactUeiFilter(){
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new ExactUEIFilter("test uei"))));
        assertEquals(0, alarms.length);
        
        alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new ExactUEIFilter("uei.opennms.org/test"))));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testInterfaceFilter(){
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new InterfaceFilter(addr("192.168.1.1")))));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNegativeAcknowledgeByFilter(){
        AlarmCriteria criteria = new AlarmCriteria(new NegativeAcknowledgedByFilter("non user"));
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testIPLikeFilter(){
        AlarmCriteria criteria = new AlarmCriteria(new IPAddrLikeFilter("192.168.1.1"));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
        assertEquals(addr("192.168.1.1"), alarms[0].getIpAddr());
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNegativeInterfaceFilter(){
        AlarmCriteria criteria = new AlarmCriteria(new NegativeInterfaceFilter(addr("192.168.1.101")));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNegativeNodeFilter(){
        AlarmCriteria criteria = getCriteria(new NegativeNodeFilter(11, m_appContext));
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
        
        NegativeNodeFilter filter = new NegativeNodeFilter(11, m_appContext);
        assertEquals("node is not 11", filter.getTextDescription());
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNegativeExactUeiFilter(){
        AlarmCriteria criteria = getCriteria(new NegativeExactUEIFilter("uei.opennms.org/bogus"));
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNegativePartialUEIFilter(){
        AlarmCriteria criteria = getCriteria(new NegativePartialUEIFilter("uei.opennms.org"));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(0, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNegativeServiceFilter(){
        AlarmCriteria criteria = getCriteria(new NegativeServiceFilter(12, null));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNegativeSeverityFilter(){
        AlarmCriteria criteria = getCriteria(new NegativeSeverityFilter(OnmsSeverity.CRITICAL));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }

    /*
    @Ignore
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNodeFilter(){
        AlarmCriteria criteria = getCriteria(new NodeFilter(1));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
        
        criteria = getCriteria(new NodeFilter(100));
        
        alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(0, alarms.length);
    }
    */

    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNodeNameLikeFilter(){
        AlarmCriteria criteria = getCriteria(new NodeNameLikeFilter("mr"));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(0, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testSeverityBetweenFilter(){
        AlarmCriteria criteria = getCriteria(new SeverityBetweenFilter(OnmsSeverity.CLEARED, OnmsSeverity.MAJOR));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testServiceFilter(){
        AlarmCriteria criteria = getCriteria(new ServiceFilter(1, null));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(0, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testAfterFirstEventTime(){
        AlarmCriteria criteria = getCriteria(new AfterFirstEventTimeFilter(new Date()));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(0, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testDescriptionSubstringFilter(){
        AlarmCriteria criteria = getCriteria(new DescriptionSubstringFilter("alarm"));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testLogMessageSubstringFilter(){
        AlarmCriteria criteria = getCriteria(new LogMessageSubstringFilter("this is a test"));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    @SuppressWarnings("deprecation")
    public void testLogMessageMatchAnyFilter(){
        AlarmCriteria criteria = getCriteria(new LogMessageMatchesAnyFilter("log"));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testParmsLikeFilter() {
        List<OnmsEvent> events = m_dbPopulator.getEventDao().findAll();
        assertNotNull(events);
        OnmsEvent event = events.get(0);
        
        List<OnmsDistPoller> pollers = m_dbPopulator.getDistPollerDao().findAll();
        assertNotNull(pollers);
        OnmsDistPoller poller = pollers.get(0);
        
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp");
        alarm.setLastEvent(event);
        alarm.setSeverityId(3);
        alarm.setDistPoller(poller);
        alarm.setCounter(100);
        alarm.setEventParms("url=http://localhost:8980/opennms/rtc/post/Network+Interfaces(string,text);user=rtc(string,text);passwd=rtc(string,text);catlabel=Network Interfaces(string,text)");
        
        AlarmDao alarmDao = m_dbPopulator.getAlarmDao();
        alarmDao.save(alarm);
        alarmDao.flush();
        
        OnmsAlarm alarm2 = new OnmsAlarm();
        alarm2.setUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp");
        alarm2.setLastEvent(event);
        alarm2.setSeverityId(3);
        alarm2.setDistPoller(poller);
        alarm2.setCounter(100);
        alarm2.setEventParms("componentType=serviceElement(string,text);url=http://localhost:8980/opennms/rtc/post/Network+Interfaces(string,text);user=rtcbomb(string,text);passwd=rtc(string,text);catlabel=Network Interfaces(string,text)");
        
        alarmDao.save(alarm2);
        alarmDao.flush();
        
        EventParmLikeFilter eventParmFilter = new EventParmLikeFilter("user=rtc");
        assertEquals("user=\"rtc\"", eventParmFilter.getTextDescription());
        AlarmCriteria criteria = new AlarmCriteria(eventParmFilter);
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testParmsNotLikeFilter() {
        List<OnmsEvent> events = m_dbPopulator.getEventDao().findAll();
        assertNotNull(events);
        OnmsEvent event = events.get(0);
        
        List<OnmsDistPoller> pollers = m_dbPopulator.getDistPollerDao().findAll();
        assertNotNull(pollers);
        OnmsDistPoller poller = pollers.get(0);
        
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp");
        alarm.setLastEvent(event);
        alarm.setSeverityId(3);
        alarm.setDistPoller(poller);
        alarm.setCounter(100);
        alarm.setEventParms("componentType=service(string,text);url=http://localhost:8980/opennms/rtc/post/Network+Interfaces(string,text);user=rtc(string,text);passwd=rtc(string,text);catlabel=Network Interfaces(string,text)");
        
        AlarmDao alarmDao = m_dbPopulator.getAlarmDao();
        alarmDao.save(alarm);
        alarmDao.flush();
        
        OnmsAlarm alarm2 = new OnmsAlarm();
        alarm2.setUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp");
        alarm2.setLastEvent(event);
        alarm2.setSeverityId(3);
        alarm2.setDistPoller(poller);
        alarm2.setCounter(100);
        alarm2.setEventParms("componentType=serviceElement(string,text);url=http://localhost:8980/opennms/rtc/post/Network+Interfaces(string,text);user=admin(string,text);passwd=rtc(string,text);catlabel=Network Interfaces(string,text)");
        
        alarmDao.save(alarm2);
        alarmDao.flush();
        
        NegativeEventParmLikeFilter parmFilter = new NegativeEventParmLikeFilter("user=rtc");
        assertEquals("user is not \"rtc\"", parmFilter.getTextDescription());
        
        AlarmCriteria criteria = new AlarmCriteria(parmFilter);
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
        
    }
    
    private AlarmCriteria getCriteria(Filter...filters){
        return new AlarmCriteria(filters);
    }
    
}
