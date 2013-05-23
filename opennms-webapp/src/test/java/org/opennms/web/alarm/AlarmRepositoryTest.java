/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.AcknowledgmentDao;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.AlarmRepository;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.alarm.filter.AcknowledgedByFilter;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.alarm.filter.AlarmIdFilter;
import org.opennms.web.alarm.filter.NodeNameLikeFilter;
import org.opennms.web.alarm.filter.SeverityFilter;
import org.opennms.web.event.Event;
import org.opennms.web.event.WebEventRepository;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.ExactUEIFilter;
import org.opennms.web.event.filter.IPAddrLikeFilter;
import org.opennms.web.event.filter.IfIndexFilter;
import org.opennms.web.event.filter.InterfaceFilter;
import org.opennms.web.event.filter.NodeFilter;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/daoWebRepositoryTestContext.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AlarmRepositoryTest implements InitializingBean {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    AlarmRepository m_alarmRepo;
    
    @Autowired
    AlarmDao m_alarmDao;
    
    @Autowired
    DistPollerDao m_distPollerDao;
	
	@Autowired
	EventDao m_eventDao;

	@Autowired
	NodeDao m_nodeDao;
    
    @Autowired
    WebEventRepository m_eventRepo;
    
    @Autowired
    ApplicationContext m_appContext;
    
    @Autowired
    AcknowledgmentDao m_acknowledgmentDao;
    
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
    public void testGetAlarmById(){
        OnmsAlarm alarm = m_alarmRepo.getAlarm(1);
        assertNotNull(alarm);
        
        assertEquals(1, alarm.getId().intValue());
        assertEquals("uei.opennms.org/test", alarm.getUei());
        assertEquals("localhost", alarm.getDistPoller().getName());
        assertEquals(1, alarm.getCounter().intValue());
        assertEquals(3, alarm.getSeverity().getId());
        
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testCountMatchingAlarms(){
        int alarms = m_alarmRepo.countMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AlarmIdFilter(1))));
        assertEquals(1, alarms);
        
        alarms = m_alarmRepo.countMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AlarmIdFilter(2))));
        assertEquals(0, alarms);
    }
    
    @Test
    @Transactional
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
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testGetMatchingAlarms(){
        OnmsAlarm[] alarms = m_alarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new SeverityFilter(OnmsSeverity.NORMAL), new AlarmIdFilter(1))));
        assertNotNull(alarms);
        assertEquals(1, alarms.length);
        
        alarms = m_alarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new SeverityFilter(OnmsSeverity.MAJOR))));
        assertNotNull(alarms);
        assertEquals(0, alarms.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testGetUnacknowledgedAlarms() {
        OnmsCriteria acked = AlarmUtil.getOnmsCriteria(new AlarmCriteria(AcknowledgeType.ACKNOWLEDGED, new Filter[0]));
        OnmsCriteria unacked = AlarmUtil.getOnmsCriteria(new AlarmCriteria(AcknowledgeType.UNACKNOWLEDGED, new Filter[0]));
        OnmsCriteria all = AlarmUtil.getOnmsCriteria(new AlarmCriteria(AcknowledgeType.BOTH, new Filter[0]));
        
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
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
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
    public void testSort() {
        
        for(SortStyle style : SortStyle.values()) {
            AlarmCriteria sorted = new AlarmCriteria(new Filter[0], style, AcknowledgeType.UNACKNOWLEDGED, 100, 0);
            OnmsAlarm[] alarms = m_alarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(sorted));
            assertTrue("Failed to sort with style "+style, alarms.length > 0);
        }
    }

    @Test
    @Transactional
    public void testSortAndSearchBySameProperty() {
        
        Filter[] filters = new Filter[] { new NodeNameLikeFilter("node") };
        
        AlarmCriteria sorted = new AlarmCriteria(filters, SortStyle.NODE, AcknowledgeType.UNACKNOWLEDGED, 100, 0);
        OnmsAlarm[] alarms = m_alarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(sorted));
        assertTrue("Failed to sort with style "+SortStyle.NODE, alarms.length > 0);
    }

    @Test
    @Transactional
    public void testAcknowledgeUnacknowledgeAllAlarms(){
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
    public void testCountMatchingBySeverity(){
        int[] matchingAlarmCount = m_alarmRepo.countMatchingAlarmsBySeverity(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new SeverityFilter(OnmsSeverity.NORMAL))));
        assertEquals(8, matchingAlarmCount.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testEscalateAlarms(){
        int[] alarmIds = {1};
        m_alarmRepo.escalateAlarms(alarmIds, "TestUser", new Date());
        
        OnmsAlarm[] alarms = m_alarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AlarmIdFilter(1))));
        
        assertNotNull(alarms);
        
        assertEquals(OnmsSeverity.WARNING.getId(), alarms[0].getSeverity().getId());
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
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
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testAcknowledgements(){
        m_alarmRepo.acknowledgeAlarms(new int[] { 1 }, "agalue", new Date());
        List<OnmsAcknowledgment> acks = m_alarmRepo.getAcknowledgments(1);
        Assert.assertNotNull(acks);
        Assert.assertEquals(1, acks.size());
        Assert.assertEquals("agalue", acks.get(0).getAckUser());
    }
    
/*    @Test
    @Transactional
    public void testPurgeAlarms(){
    	
    	OnmsEvent event = new OnmsEvent();
	    OnmsNode node = m_nodeDao.findAll().iterator().next();
	    event.setNode(node);
	    OnmsIpInterface iface = (OnmsIpInterface)node.getIpInterfaces().iterator().next();
	    event.setIpAddr(iface.getIpAddress());
	    event.setEventUei("uei://org/opennms/test/deleteAlarmPurgeTest");
		event.setEventTime(new Date());
		event.setEventSource("deleteAlarmPurgeTest");
		event.setDistPoller(m_distPollerDao.load("localhost"));
		event.setEventCreateTime(new Date());
		event.setEventSeverity(new Integer(7));
		event.setIfIndex(7);
		event.setEventLog("Y");
        event.setEventDisplay("Y");
	    
	    OnmsAlarm alarm = new OnmsAlarm();
	    alarm.setNode(node);
	    alarm.setIpAddr(event.getIpAddr());
	    alarm.setUei(event.getEventUei());
	    alarm.setDistPoller(m_distPollerDao.load("localhost"));
	    alarm.setIfIndex(event.getIfIndex());
	    alarm.setCounter(1);
	    alarm.setSeverityId(event.getEventSeverity());
	    m_alarmDao.save(alarm);
	    
	    event.setAlarm(alarm);
	    m_eventDao.save(event);
	    
	    OnmsAcknowledgment ack = new OnmsAcknowledgment();
        ack.setAckTime(new Date());
        ack.setAckUser("test-admin");
        ack.setAckType(AckType.UNSPECIFIED);
        ack.setAckAction(AckAction.UNSPECIFIED);
        ack.setRefId(event.getAlarm().getId());
        m_acknowledgmentDao.save(ack);

    	List<Integer> alarmIds = new ArrayList<Integer>();
        assertNotNull(alarm);
        alarmIds.add(alarm.getId());
        
        assertNotNull(alarm.getNodeId());
        assertNotNull(alarm.getIpAddr());
        assertNotNull(alarm.getUei());
        assertNotNull(alarm.getIfIndex());
        Filter[] filters = new Filter[] { new InterfaceFilter(InetAddressUtils.str(alarm.getIpAddr())) , new ExactUEIFilter(alarm.getUei()) , new NodeFilter(alarm.getNodeId(), m_appContext) , new IfIndexFilter(alarm.getIfIndex())};
        EventCriteria eventCriteria = new EventCriteria(filters);
        Event[] events = m_eventRepo.getMatchingEvents(eventCriteria);

        List<Integer> eventIdsList = new ArrayList<Integer>();
        List<Integer> ackRefIdsList = new ArrayList<Integer>();
        HashMap<Integer, List<Integer>> eventIdsForAlarms = new HashMap<Integer, List<Integer>>();
        HashMap<Integer, List<Integer>> ackRefIdsForAlarms = new HashMap<Integer, List<Integer>>();
        
    	for(Event eventIterator : events){
    		assertNotNull(eventIterator);
    		eventIdsList.add(eventIterator.getId());
    		assertNotNull(eventIterator.getAlarmId());
    		ackRefIdsList.add(eventIterator.getAlarmId());
		}
    	
    	alarmIds.add(alarm.getId());
        eventIdsForAlarms.put(alarm.getId(), eventIdsList);
        ackRefIdsForAlarms.put(alarm.getId(), ackRefIdsList);
        
        m_alarmRepo.purgeAlarms(alarmIds, eventIdsForAlarms, ackRefIdsForAlarms);
    }*/
}
