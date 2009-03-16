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

import java.util.Date;

import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.dao.DataIntegrityViolationException;

public class AlarmDaoTest extends AbstractTransactionalDaoTestCase {
    
    public void testActions() {
        OnmsEvent event = new OnmsEvent();
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventCreateTime(new Date());
        event.setDistPoller(getDistPollerDao().load("localhost"));
        event.setEventTime(new Date());
        event.setEventSeverity(new Integer(6));
        event.setEventUei("uei://org/opennms/test/EventDaoTest");
        event.setEventSource("test");
        getEventDao().save(event);
        
        OnmsNode node = getNodeDao().findAll().iterator().next();

        OnmsAlarm alarm = new OnmsAlarm();
        
        alarm.setNode(node);
        alarm.setUei(event.getEventUei());
        alarm.setSeverity(OnmsSeverity.get(event.getEventSeverity()));
        alarm.setSeverityId(event.getEventSeverity());
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        alarm.setCounter(new Integer(1));
        alarm.setDistPoller(getDistPollerDao().load("localhost"));
        
        getAlarmDao().save(alarm);
        
        OnmsAlarm newAlarm = getAlarmDao().load(alarm.getId());
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
    
    
    public void testSave() {
        OnmsEvent event = new OnmsEvent();
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventCreateTime(new Date());
        event.setDistPoller(getDistPollerDao().load("localhost"));
        event.setEventTime(new Date());
        event.setEventSeverity(new Integer(7));
        event.setEventUei("uei://org/opennms/test/EventDaoTest");
        event.setEventSource("test");
        getEventDao().save(event);
        
        OnmsNode node = getNodeDao().findAll().iterator().next();

        OnmsAlarm alarm = new OnmsAlarm();
        
        alarm.setNode(node);
        alarm.setUei(event.getEventUei());
        alarm.setSeverityId(event.getEventSeverity());
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        alarm.setCounter(new Integer(1));
        alarm.setDistPoller(getDistPollerDao().load("localhost"));
        
        getAlarmDao().save(alarm);
        // It works we're so smart! hehe
        
        OnmsAlarm newAlarm = getAlarmDao().load(alarm.getId());
        assertEquals("uei://org/opennms/test/EventDaoTest", newAlarm.getUei());
        assertEquals(alarm.getLastEvent().getId(), newAlarm.getLastEvent().getId());
    }
    
    public void testWithoutDistPoller() {
        OnmsEvent event = new OnmsEvent();
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventCreateTime(new Date());
        event.setDistPoller(getDistPollerDao().load("localhost"));
        event.setEventTime(new Date());
        event.setEventSeverity(new Integer(7));
        event.setEventUei("uei://org/opennms/test/EventDaoTest");
        event.setEventSource("test");
        getEventDao().save(event);
        
        OnmsNode node = getNodeDao().findAll().iterator().next();

        OnmsAlarm alarm = new OnmsAlarm();
        
        alarm.setNode(node);
        alarm.setUei(event.getEventUei());
        alarm.setSeverityId(event.getEventSeverity());
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        alarm.setCounter(new Integer(1));
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new DataIntegrityViolationException("not-null property references a null or transient value: org.opennms.netmgt.model.OnmsAlarm.distPoller; nested exception is org.hibernate.PropertyValueException: not-null property references a null or transient value: org.opennms.netmgt.model.OnmsAlarm.distPoller"));
        
        try {
            getAlarmDao().save(alarm);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }
}
