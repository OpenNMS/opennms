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

package org.opennms.web.alarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.alarm.filter.AcknowledgedByFilter;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.alarm.filter.AlarmIdFilter;
import org.opennms.web.alarm.filter.NodeNameLikeFilter;
import org.opennms.web.alarm.filter.SeverityFilter;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/daoWebRepositoryTestContext.xml",
        
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class AlarmRepositoryIT implements InitializingBean {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    AlarmRepository m_alarmRepo;
    
    @Autowired
    AlarmDao m_alarmDao;
    
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
    public void testGetAlarmById(){
        OnmsAlarm alarm = m_alarmRepo.getAlarm(1);
        assertNotNull(alarm);
        
        assertEquals(1, alarm.getId().intValue());
        assertEquals("uei.opennms.org/test", alarm.getUei());
        assertEquals("00000000-0000-0000-0000-000000000000", alarm.getDistPoller().getId());
        assertEquals(1, alarm.getCounter().intValue());
        assertEquals(3, alarm.getSeverity().getId());
        
    }
    
    @Test
    @JUnitTemporaryDatabase
    public void testCountMatchingAlarms(){
        int alarms = m_alarmRepo.countMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AlarmIdFilter(1))));
        assertEquals(1, alarms);
        
        alarms = m_alarmRepo.countMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AlarmIdFilter(2))));
        assertEquals(0, alarms);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testCountMatchingAlarmsBySeverity(){
        AlarmCriteria criteria = new AlarmCriteria();
        int [] matchingAlarms = m_alarmRepo.countMatchingAlarmsBySeverity(AlarmUtil.getOnmsCriteria(criteria));
        
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
    @JUnitTemporaryDatabase
    public void testGetMatchingAlarms(){
        OnmsAlarm[] alarms = m_alarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new SeverityFilter(OnmsSeverity.NORMAL), new AlarmIdFilter(1))));
        assertNotNull(alarms);
        assertEquals(1, alarms.length);
        
        alarms = m_alarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new SeverityFilter(OnmsSeverity.MAJOR))));
        assertNotNull(alarms);
        assertEquals(0, alarms.length);
    }
    
    @Test
    @JUnitTemporaryDatabase
    public void testGetUnacknowledgedAlarms() {
        OnmsCriteria acked = AlarmUtil.getOnmsCriteria(new AlarmCriteria(new Filter[0], AcknowledgeType.ACKNOWLEDGED));
        OnmsCriteria unacked = AlarmUtil.getOnmsCriteria(new AlarmCriteria(new Filter[0], AcknowledgeType.UNACKNOWLEDGED));
        OnmsCriteria all = AlarmUtil.getOnmsCriteria(new AlarmCriteria(new Filter[0], AcknowledgeType.BOTH));
        
        int countAll = m_alarmRepo.countMatchingAlarms(all);
        int countAcked = m_alarmRepo.countMatchingAlarms(acked);
        int countUnacked = m_alarmRepo.countMatchingAlarms(unacked);
        
        assertEquals(countAll, countAcked + countUnacked);
        assertTrue(countAll > 0);
        assertTrue(countAcked == 0);
        assertTrue(countUnacked > 0);
        
        OnmsAlarm[] unackedAlarms = m_alarmRepo.getMatchingAlarms(unacked);
        assertEquals(countUnacked, unackedAlarms.length);

        OnmsAlarm[] ackedAlarms = m_alarmRepo.getMatchingAlarms(acked);
        assertEquals(countAcked, ackedAlarms.length);

        OnmsAlarm[] allAlarms = m_alarmRepo.getMatchingAlarms(all);
        assertEquals(countAll, allAlarms.length);
        
        m_alarmRepo.acknowledgeMatchingAlarms("TestUser", new Date(), AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AlarmIdFilter(1))));
        
        assertEquals(countAcked+1, m_alarmRepo.countMatchingAlarms(acked));
        assertEquals(countUnacked-1, m_alarmRepo.countMatchingAlarms(unacked));
        
}
    
    @Test
    @JUnitTemporaryDatabase
    public void testAcknowledgeUnacknowledge() {
        
        String user = "TestUser";
        m_alarmRepo.acknowledgeMatchingAlarms(user, new Date(), AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AlarmIdFilter(1))));
        
        int matchingAlarmCount = m_alarmRepo.countMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AcknowledgedByFilter(user))));
        
        assertEquals(1, matchingAlarmCount);
        
        m_alarmRepo.unacknowledgeMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AlarmIdFilter(1))), user);
        
        matchingAlarmCount = m_alarmRepo.countMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AcknowledgedByFilter(user))));
        
        assertEquals(0, matchingAlarmCount);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testSort() {
        
        for(SortStyle style : SortStyle.values()) {
            AlarmCriteria sorted = new AlarmCriteria(new Filter[0], style, AcknowledgeType.UNACKNOWLEDGED, 100, 0);
            OnmsAlarm[] alarms = m_alarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(sorted));
            assertTrue("Failed to sort with style "+style, alarms.length > 0);
        }
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testSortAndSearchBySameProperty() {
        
        Filter[] filters = new Filter[] { new NodeNameLikeFilter("node") };
        
        AlarmCriteria sorted = new AlarmCriteria(filters, SortStyle.NODE, AcknowledgeType.UNACKNOWLEDGED, 100, 0);
        OnmsAlarm[] alarms = m_alarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(sorted));
        assertTrue("Failed to sort with style "+SortStyle.NODE, alarms.length > 0);
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testAcknowledgeUnacknowledgeAllAlarms() {
        String user = "TestUser";
        m_alarmRepo.acknowledgeAll(user, new Date());
        
        int matchingAlarmCount = m_alarmRepo.countMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AcknowledgedByFilter(user))));
        assertEquals(1, matchingAlarmCount);
        
        m_alarmRepo.unacknowledgeAll(user);
        
        matchingAlarmCount = m_alarmRepo.countMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AcknowledgedByFilter(user))));
        assertEquals(0, matchingAlarmCount);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testCountMatchingBySeverity(){
        int[] matchingAlarmCount = m_alarmRepo.countMatchingAlarmsBySeverity(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new SeverityFilter(OnmsSeverity.NORMAL))));
        assertEquals(8, matchingAlarmCount.length);
    }
    
    @Test
    @JUnitTemporaryDatabase
    public void testEscalateAlarms() {
        int[] alarmIds = {1};
        m_alarmRepo.escalateAlarms(alarmIds, "TestUser", new Date());
        
        OnmsAlarm[] alarms = m_alarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AlarmIdFilter(1))));
        
        assertNotNull(alarms);
        
        assertEquals(OnmsSeverity.WARNING.getId(), alarms[0].getSeverity().getId());
    }
    
    @Test
    @JUnitTemporaryDatabase
    public void testClearAlarms(){
        OnmsAlarm alarm = m_alarmRepo.getAlarm(1);
        
        assertNotNull(alarm);
        assertEquals(OnmsSeverity.NORMAL.getId(), alarm.getSeverity().getId());
        
        int[] alarmIds = {1};
        m_alarmRepo.clearAlarms(alarmIds, "TestUser", new Date());
        
        alarm = m_alarmRepo.getAlarm(1);
        assertNotNull(alarm);
        assertEquals(OnmsSeverity.CLEARED.getId(), alarm.getSeverity().getId());
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAcknowledgements(){
        m_alarmRepo.acknowledgeAlarms(new int[] { 1 }, "agalue", new Date());
        List<OnmsAcknowledgment> acks = m_alarmRepo.getAcknowledgments(1);
        Assert.assertNotNull(acks);
        Assert.assertEquals(1, acks.size());
        Assert.assertEquals("agalue", acks.get(0).getAckUser());
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testSortBySituation() {
        OnmsDistPoller poller = m_dbPopulator.getDistPollerDao().whoami();

        final OnmsEvent event = new OnmsEvent();
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventCreateTime(new Date());
        event.setDistPoller(poller);
        event.setEventTime(new Date());
        event.setEventSeverity(OnmsSeverity.CRITICAL.getId());
        event.setEventUei("uei://org/opennms/test/EventDaoTest");
        event.setEventSource("test");
        m_dbPopulator.getEventDao().save(event);
        m_dbPopulator.getEventDao().flush();

        final OnmsNode node = m_dbPopulator.getNodeDao().findAll().iterator().next();

        final OnmsAlarm alarm1 = new OnmsAlarm();
        alarm1.setNode(node);
        alarm1.setUei(event.getEventUei());
        alarm1.setSeverityId(event.getEventSeverity());
        alarm1.setFirstEventTime(event.getEventTime());
        alarm1.setLastEvent(event);
        alarm1.setCounter(1);
        alarm1.setDistPoller(poller);
        m_dbPopulator.getAlarmDao().save(alarm1);
        m_dbPopulator.getAlarmDao().flush();

        final OnmsAlarm alarm2 = new OnmsAlarm();
        alarm2.setNode(node);
        alarm2.setUei(event.getEventUei());
        alarm2.setSeverityId(event.getEventSeverity());
        alarm2.setFirstEventTime(event.getEventTime());
        alarm2.setLastEvent(event);
        alarm2.setCounter(1);
        alarm2.setDistPoller(poller);
        alarm2.setRelatedAlarms(Sets.newHashSet(alarm1));
        m_dbPopulator.getAlarmDao().save(alarm2);
        m_dbPopulator.getAlarmDao().flush();

        assertEquals(3, m_dbPopulator.getAlarmDao().findAll().size());

        final AlarmCriteria sortedAsc = new AlarmCriteria(new Filter[0], SortStyle.SITUATION, AcknowledgeType.UNACKNOWLEDGED, 100, 0);
        final OnmsAlarm[] alarmsAsc = m_alarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(sortedAsc));

        final AlarmCriteria sortedDesc = new AlarmCriteria(new Filter[0], SortStyle.REVERSE_SITUATION, AcknowledgeType.UNACKNOWLEDGED, 100, 0);
        final OnmsAlarm[] alarmsDesc = m_alarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(sortedDesc));

        assertEquals(3, alarmsAsc.length);
        assertEquals(true, alarmsAsc[0].isSituation());
        assertEquals(false, alarmsAsc[1].isSituation());
        assertEquals(false, alarmsAsc[2].isSituation());
        assertEquals(3, alarmsDesc.length);
        assertEquals(false, alarmsDesc[0].isSituation());
        assertEquals(false, alarmsDesc[1].isSituation());
        assertEquals(true, alarmsDesc[2].isSituation());
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testAckSituation() {
        OnmsDistPoller poller = m_dbPopulator.getDistPollerDao().whoami();

        final OnmsEvent event = new OnmsEvent();
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventCreateTime(new Date());
        event.setDistPoller(poller);
        event.setEventTime(new Date());
        event.setEventSeverity(OnmsSeverity.CRITICAL.getId());
        event.setEventUei("uei://org/opennms/test/EventDaoTest");
        event.setEventSource("test");
        m_dbPopulator.getEventDao().save(event);
        m_dbPopulator.getEventDao().flush();

        final OnmsNode node = m_dbPopulator.getNodeDao().findAll().iterator().next();

        final OnmsAlarm alarm1 = new OnmsAlarm(); // Simple Alarm #1
        alarm1.setNode(node);
        alarm1.setUei(event.getEventUei());
        alarm1.setSeverityId(event.getEventSeverity());
        alarm1.setFirstEventTime(event.getEventTime());
        alarm1.setLastEvent(event);
        alarm1.setCounter(1);
        alarm1.setDistPoller(poller);
        m_dbPopulator.getAlarmDao().save(alarm1);
        m_dbPopulator.getAlarmDao().flush();

        final OnmsAlarm alarm2 = new OnmsAlarm(); // Situation #2 with Alarm #1 associated
        alarm2.setNode(node);
        alarm2.setUei(event.getEventUei());
        alarm2.setSeverityId(event.getEventSeverity());
        alarm2.setFirstEventTime(event.getEventTime());
        alarm2.setLastEvent(event);
        alarm2.setCounter(1);
        alarm2.setDistPoller(poller);
        alarm2.setRelatedAlarms(Sets.newHashSet(alarm1));
        m_dbPopulator.getAlarmDao().save(alarm2);
        m_dbPopulator.getAlarmDao().flush();

        final OnmsAlarm alarm3 = new OnmsAlarm(); // Situation #3 with Situation #2 associated
        alarm3.setNode(node);
        alarm3.setUei(event.getEventUei());
        alarm3.setSeverityId(event.getEventSeverity());
        alarm3.setFirstEventTime(event.getEventTime());
        alarm3.setLastEvent(event);
        alarm3.setCounter(1);
        alarm3.setDistPoller(poller);
        alarm3.setRelatedAlarms(Sets.newHashSet(alarm2));
        m_dbPopulator.getAlarmDao().save(alarm3);
        m_dbPopulator.getAlarmDao().flush();

        final List<OnmsAlarm> before = m_dbPopulator.getAlarmDao().findAll();
        assertEquals(4, before.size());
        assertEquals(false, before.get(0).isAcknowledged());
        assertEquals(false, before.get(1).isAcknowledged());
        assertEquals(false, before.get(2).isAcknowledged());
        assertEquals(false, before.get(3).isAcknowledged());

        m_alarmRepo.acknowledgeAlarms(new int[]{alarm3.getId()},"me", new Date());

        // Ack of Alarm #3 should also ack Alarm #2 because it is a situation. Alarm #2
        // itself is also a situation, so Alarm #1 is also ack'd.

        final List<OnmsAlarm> after = m_dbPopulator.getAlarmDao().findAll().stream()
                .filter(o -> o.isAcknowledged()).collect(Collectors.toList());

        assertEquals(3, after.size());
        assertEquals(true, after.get(0).isAcknowledged());
        assertEquals(true, after.get(1).isAcknowledged());
        assertEquals(true, after.get(2).isAcknowledged());
    }
}
