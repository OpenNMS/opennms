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
 * Modifications:
 * 
 * Created: January 19, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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

import org.junit.Test;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;

/**
 * Tests for Acknowledgment DAO
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class AcknowledgmentDaoTest extends AbstractTransactionalDaoTestCase {

    @Test
    public void testSaveUnspecified() {
        OnmsAcknowledgment ack = new OnmsAcknowledgment();
        ack.setAckTime(new Date());
        ack.setAckType(AckType.UNSPECIFIED);
        ack.setAckAction(AckAction.UNSPECIFIED);
        ack.setAckUser("not-admin");
        getAcknowledgmentDao().save(ack);
        getAcknowledgmentDao().flush();
        Integer id = new Integer(ack.getId());
        ack = null;
        
        OnmsAcknowledgment ack2 = getAcknowledgmentDao().get(id);
        assertNotNull(ack2);
        assertEquals(id, ack2.getId());
        assertFalse("admin".equals(ack2.getAckUser()));
        assertEquals("not-admin", ack2.getAckUser());
        
    }

    @Test
    public void testSaveWithAlarm() {
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
        alarm.setAlarmAckTime(new Date());
        alarm.setAlarmAckUser("not-admin");
        
        getAlarmDao().save(alarm);
        getAlarmDao().flush();
        
        OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm);
        getAcknowledgmentDao().save(ack);
        Integer ackId = new Integer(ack.getId());
        ack = null;
        
        OnmsAcknowledgment ack2 = getAcknowledgmentDao().get(ackId);
        OnmsAlarm alarm2 = getAlarmDao().get(ack2.getRefId());
        
        assertEquals(ack2.getAckUser(), alarm2.getAlarmAckUser());
        assertEquals(ack2.getAckTime(), alarm2.getAlarmAckTime());
        
    }

    /*
    @Test
    public void testAcknowledgmentDaoHibernate() {
        fail("Not yet implemented");
    }

    @Test
    public void testFindAcknowledgables() {
        fail("Not yet implemented");
    }

    @Test
    public void testUpdateAckable() {
        fail("Not yet implemented");
    }
    */

}
