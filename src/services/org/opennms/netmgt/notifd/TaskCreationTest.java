//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
package org.opennms.netmgt.notifd;

import java.util.LinkedList;
import java.util.Map;

import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.xml.event.Event;


public class TaskCreationTest extends NotificationsTestCase {
    
    BroadcastEventProcessor m_eventProcessor;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TaskCreationTest.class);
    }
    
    
    
    protected void setUp() throws Exception {
        super.setUp();
        m_eventProcessor = m_notifd.getBroadcastEventProcessor();
    }



    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testMakeEmailTask() throws Exception {
        long startTime = System.currentTimeMillis();
        
        Notification notif = m_notificationManager.getNotification("nodeDown");
        
        MockNode node = m_network.getNode(1);
        Event nodeDownEvent = node.createDownEvent();
        
        Map params = m_eventProcessor.buildParameterMap(notif, nodeDownEvent, 1);
        
        String[] commands = new String[]{ "email" };
        NotificationTask task = m_eventProcessor.makeEmailTask(startTime, params, 1, "brozow@opennms.org", commands, new LinkedList());
        assertNotNull(task);
        assertEquals("brozow@opennms.org", task.getEmail());
        assertEquals(startTime, task.getSendTime());
    
        
    }

    public void testMakeUserTask() throws Exception {
        long startTime = System.currentTimeMillis();
        
        Notification notif = m_notificationManager.getNotification("nodeDown");
        
        MockNode node = m_network.getNode(1);
        Event nodeDownEvent = node.createDownEvent();
        
        Map params = m_eventProcessor.buildParameterMap(notif, nodeDownEvent, 1);
        
        String[] commands = new String[]{ "email" };
        NotificationTask task = m_eventProcessor.makeUserTask(startTime, params, 1, "brozow", commands, new LinkedList());
        assertNotNull(task);
        assertEquals("brozow@opennms.org", task.getEmail());
        assertEquals(startTime, task.getSendTime());
    
        
    }

    public void testMakeGroupTasks() throws Exception {
        long startTime = System.currentTimeMillis();
        
        Notification notif = m_notificationManager.getNotification("nodeDown");
        
        MockNode node = m_network.getNode(1);
        Event nodeDownEvent = node.createDownEvent();
        
        Map params = m_eventProcessor.buildParameterMap(notif, nodeDownEvent, 1);
        
        String[] commands = new String[]{ "email" };
        NotificationTask[] tasks = m_eventProcessor.makeGroupTasks(startTime, params, 1, "EscalationGroup", commands, new LinkedList(), 1000);
        assertNotNull(tasks);
        assertEquals(2, tasks.length);
        assertEquals("brozow@opennms.org", tasks[0].getEmail());
        assertEquals(startTime, tasks[0].getSendTime());
        assertEquals("david@opennms.org", tasks[1].getEmail());
        assertEquals(startTime+1000, tasks[1].getSendTime());
    
        
    }

}
