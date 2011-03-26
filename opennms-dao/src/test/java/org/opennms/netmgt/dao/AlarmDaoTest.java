/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2009 Jan 26: Added tests for Acknowledgment actions
 * 2008 Oct 04: Use OnmsAlarm.setSeverityId() instead of setSeverity (which now takes an OnmsSeverity object). - dj@opennms.org
 * 2007 Apr 05: Convert to use AbstractTransactionalDaoTestCase, reorganized testSave(), added a test for the case where distPoller is not set. - dj@opennms.org
 * 
 *
 * Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitTemporaryDatabase()
public class AlarmDaoTest {
    
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
	
	@Before
	public void setUp() {
		m_databasePopulator.populateDatabase();
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
    }
    
	@Test
	@Transactional
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
}
