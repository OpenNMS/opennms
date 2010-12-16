//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Mar 25: Convert to use AbstractTransactionalDaoTestCase. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao;

import java.util.Date;

import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsUserNotification;

public class UserNotificationDaoTest extends AbstractTransactionalDaoTestCase {
    public void testSaveUserNotification() {
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
        event.setEventUei("uei://org/opennms/test/UserNotificationDaoTest");
        OnmsAlarm alarm = new OnmsAlarm();
        event.setAlarm(alarm);

        OnmsNode node = (OnmsNode) getNodeDao().findAll().iterator().next();
        OnmsIpInterface iface = (OnmsIpInterface)node.getIpInterfaces().iterator().next();
        OnmsMonitoredService service = (OnmsMonitoredService)iface.getMonitoredServices().iterator().next();
        event.setNode(node);
	    event.setServiceType(service.getServiceType());
        event.setIpAddr(iface.getInetAddress());
        getEventDao().save(event);
        OnmsEvent newEvent = getEventDao().load(event.getId());
        assertEquals("uei://org/opennms/test/UserNotificationDaoTest", newEvent.getEventUei());
        
        
        OnmsNotification notification = new OnmsNotification();
        notification.setEvent(newEvent);
        notification.setTextMsg("Tests are fun!");
        getNotificationDao().save(notification);
       
        OnmsNotification newNotification = getNotificationDao().load(notification.getNotifyId());
        assertEquals("uei://org/opennms/test/UserNotificationDaoTest", newNotification.getEvent().getEventUei());
        
        OnmsUserNotification userNotif = new OnmsUserNotification();
        userNotif.setNotification(notification);
        userNotif.setNotifyTime(new Date());
        userNotif.setUserId("OpenNMS User");
        userNotif.setMedia("E-mail");
        userNotif.setContactInfo("test@opennms.org");
        getUserNotificationDao().save(userNotif);
        
        assertNotNull(userNotif.getNotification());
        assertEquals(userNotif.getUserId(), "OpenNMS User");
//        assertNotNull(newEvent.getServiceType());
//        assertEquals(service.getNodeId(), newEvent.getNode().getId());
//        assertEquals(event.getIpAddr(), newEvent.getIpAddr());
    }
}
