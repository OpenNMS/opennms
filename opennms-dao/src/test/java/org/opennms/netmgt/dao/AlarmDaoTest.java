/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.Order;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class AlarmDaoTest implements InitializingBean {
    
	@Autowired
    private DistPollerDao m_distPollerDao;
	
	@Autowired
	private EventDao m_eventDao;

	@Autowired
	private NodeDao m_nodeDao;

	@Autowired
	private AlarmDao m_alarmDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;
	
    private static boolean m_populated = false;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @BeforeTransaction
    public void setUp() {
        try {
            if (!m_populated) {
                m_databasePopulator.populateDatabase();
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        } finally {
            m_populated = true;
        }
    }

	@Test
	@Transactional
	public void testActions() {
        OnmsEvent event = new OnmsEvent();
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventCreateTime(new Date());
        event.setDistPoller(m_distPollerDao.load("localhost"));
        event.setEventTime(new Date());
        event.setEventSeverity(Integer.valueOf(6));
        event.setEventUei("uei://org/opennms/test/EventDaoTest");
        event.setEventSource("test");
        m_eventDao.save(event);
        
        OnmsNode node = m_nodeDao.findAll().iterator().next();

        OnmsAlarm alarm = new OnmsAlarm();
        
        alarm.setNode(node);
        alarm.setUei(event.getEventUei());
        alarm.setSeverity(OnmsSeverity.get(event.getEventSeverity()));
        alarm.setSeverityId(event.getEventSeverity());
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        alarm.setCounter(1);
        alarm.setDistPoller(m_distPollerDao.load("localhost"));
        
        m_alarmDao.save(alarm);
        
        OnmsAlarm newAlarm = m_alarmDao.load(alarm.getId());
        assertEquals("uei://org/opennms/test/EventDaoTest", newAlarm.getUei());
        assertEquals(alarm.getLastEvent().getId(), newAlarm.getLastEvent().getId());
        
        assertEquals(OnmsSeverity.MAJOR, newAlarm.getSeverity());
        
        newAlarm.escalate("admin");
        assertEquals(OnmsSeverity.CRITICAL, newAlarm.getSeverity());
        
        newAlarm.clear("admin");
        assertEquals(OnmsSeverity.CLEARED, newAlarm.getSeverity());
        
        newAlarm.unacknowledge("admin");
        assertNull(newAlarm.getAckUser());
        assertNull(newAlarm.getAlarmAckTime());
        
    }
    
    
    @Test
	@Transactional
	public void testSave() {
        OnmsEvent event = new OnmsEvent();
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventCreateTime(new Date());
        event.setDistPoller(m_distPollerDao.load("localhost"));
        event.setEventTime(new Date());
        event.setEventSeverity(new Integer(7));
        event.setEventUei("uei://org/opennms/test/EventDaoTest");
        event.setEventSource("test");
        m_eventDao.save(event);
        
        OnmsNode node = m_nodeDao.findAll().iterator().next();

        OnmsAlarm alarm = new OnmsAlarm();
        
        alarm.setNode(node);
        alarm.setUei(event.getEventUei());
        alarm.setSeverityId(event.getEventSeverity());
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        alarm.setCounter(1);
        alarm.setDistPoller(m_distPollerDao.load("localhost"));
        
        m_alarmDao.save(alarm);
        // It works we're so smart! hehe
        
        OnmsAlarm newAlarm = m_alarmDao.load(alarm.getId());
        assertEquals("uei://org/opennms/test/EventDaoTest", newAlarm.getUei());
        assertEquals(alarm.getLastEvent().getId(), newAlarm.getLastEvent().getId());
        
        Collection<OnmsAlarm> alarms;
        Criteria criteria = new Criteria(OnmsAlarm.class);
        criteria.addRestriction(new EqRestriction("node.id", node.getId()));
        alarms = m_alarmDao.findMatching(criteria);
        assertEquals(1, alarms.size());
        newAlarm = alarms.iterator().next();
        assertEquals("uei://org/opennms/test/EventDaoTest", newAlarm.getUei());
        assertEquals(alarm.getLastEvent().getId(), newAlarm.getLastEvent().getId());
    }
    
	@Test
	@Transactional
	@Ignore
    public void testWithoutDistPoller() {
        OnmsEvent event = new OnmsEvent();
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventCreateTime(new Date());
        event.setDistPoller(m_distPollerDao.load("localhost"));
        event.setEventTime(new Date());
        event.setEventSeverity(new Integer(7));
        event.setEventUei("uei://org/opennms/test/EventDaoTest");
        event.setEventSource("test");
        m_eventDao.save(event);
        
        OnmsNode node = m_nodeDao.findAll().iterator().next();

        OnmsAlarm alarm = new OnmsAlarm();
        
        alarm.setNode(node);
        alarm.setUei(event.getEventUei());
        alarm.setSeverityId(event.getEventSeverity());
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        alarm.setCounter(1);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new DataIntegrityViolationException("not-null property references a null or transient value: org.opennms.netmgt.model.OnmsAlarm.distPoller; nested exception is org.hibernate.PropertyValueException: not-null property references a null or transient value: org.opennms.netmgt.model.OnmsAlarm.distPoller"));
        
        try {
            m_alarmDao.save(alarm);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }

	@Test
	@Transactional
	public void testAlarmSummary() {
	    OnmsEvent event = new OnmsEvent();
	    event.setEventLog("Y");
	    event.setEventDisplay("Y");
	    event.setEventCreateTime(new Date());
	    event.setDistPoller(m_distPollerDao.load("localhost"));
	    event.setEventTime(new Date());
	    event.setEventSeverity(new Integer(7));
	    event.setEventUei("uei://org/opennms/test/EventDaoTest");
	    event.setEventSource("test");
	    m_eventDao.save(event);

	    OnmsNode node = m_nodeDao.findAll().iterator().next();

	    OnmsAlarm alarm = new OnmsAlarm();

	    alarm.setNode(node);
	    alarm.setUei(event.getEventUei());
	    alarm.setSeverityId(event.getEventSeverity());
	    alarm.setFirstEventTime(event.getEventTime());
	    alarm.setLastEvent(event);
	    alarm.setCounter(1);
	    alarm.setDistPoller(m_distPollerDao.load("localhost"));

	    m_alarmDao.save(alarm);

	    List<AlarmSummary> summary = m_alarmDao.getNodeAlarmSummaries();
	    Assert.assertNotNull(summary);
	    Assert.assertEquals(1, summary.size());
	    AlarmSummary sum = summary.get(0);
	    Assert.assertEquals(node.getLabel(), sum.getNodeLabel());
            Assert.assertEquals(alarm.getSeverity().getId(), sum.getMaxSeverity().getId());
            Assert.assertNotSame("N/A", sum.getFuzzyTimeDown());
	}

    @Test
    @Transactional
    public void testAlarmSummary_WithEmptyNodeIdsArray() {
        List<AlarmSummary> summary = m_alarmDao.getNodeAlarmSummaries();
        Assert.assertNotNull(summary); // the result does not really matter, as long as we get a result
        summary = null;
        summary = m_alarmDao.getNodeAlarmSummaries();
        Assert.assertNotNull(summary);
    }

        @Test
        @Transactional
        public void testAlarmSummary_AlarmWithNoEvent() {
            OnmsNode node = m_nodeDao.findAll().iterator().next();

            OnmsAlarm alarm = new OnmsAlarm();
            alarm.setNode(node);
            alarm.setUei("uei://org/opennms/test/badAlarmTest");
            alarm.setSeverityId(new Integer(7));
            alarm.setCounter(1);
            alarm.setDistPoller(m_distPollerDao.load("localhost"));

            m_alarmDao.save(alarm);

            List<AlarmSummary> summary = m_alarmDao.getNodeAlarmSummaries();
            Assert.assertNotNull(summary);
            Assert.assertEquals(1, summary.size());
            AlarmSummary sum = summary.get(0);
            Assert.assertEquals(node.getLabel(), sum.getNodeLabel());
            Assert.assertEquals(alarm.getSeverity().getId(), sum.getMaxSeverity().getId());
            Assert.assertEquals("N/A", sum.getFuzzyTimeDown());
        }

        @Test
        @Transactional
        public void testSortOnNodeLabel() {
            Criteria criteria = new Criteria(OnmsAlarm.class);
            criteria.setAliases(Arrays.asList(new Alias[] {
                    new Alias("node", "node", JoinType.LEFT_JOIN)
            }));
            criteria.setOrders(Arrays.asList(new Order[] {
                    Order.asc("node.label")
            }));
            m_alarmDao.findMatching(criteria);
        }
}
