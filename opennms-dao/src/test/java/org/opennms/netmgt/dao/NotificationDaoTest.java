/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.dao;

import java.util.Date;

import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;

public class NotificationDaoTest extends AbstractTransactionalDaoTestCase {
    public void testNotificationSave() {
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(getDistPollerDao().load("localhost"));
        event.setEventCreateTime(new Date());
        event.setEventDescr("event dao test");
        event.setEventHost("localhost");
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventLogGroup("event dao test log group");
        event.setEventLogMsg("event dao test log msg");
        event.setEventSeverity(new Integer(7));
        event.setEventSource("EventDaoTest");
        event.setEventTime(new Date());
        event.setEventUei("uei://org/opennms/test/NotificationDaoTest");
//        OnmsAlarm alarm = new OnmsAlarm();
//        event.setAlarm(alarm);

        OnmsNode node = getNodeDao().findAll().iterator().next();
        OnmsIpInterface iface = node.getIpInterfaces().iterator().next();
        OnmsMonitoredService service = iface.getMonitoredServices().iterator().next();
        event.setNode(node);
	    event.setServiceType(service.getServiceType());
        event.setIpAddr(iface.getIpAddress());
        getEventDao().save(event);
        OnmsEvent newEvent = getEventDao().load(event.getId());
        assertEquals("uei://org/opennms/test/NotificationDaoTest", newEvent.getEventUei());
        
        OnmsNotification notification = new OnmsNotification();
        notification.setEvent(newEvent);
        notification.setTextMsg("Tests are fun!");
        getNotificationDao().save(notification);
       
        OnmsNotification newNotification = getNotificationDao().load(notification.getNotifyId());
        assertEquals("uei://org/opennms/test/NotificationDaoTest", newNotification.getEvent().getEventUei());
    }
}
