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
// Modifications:
//
// 2008 Aug 31: Make testMakeRoleTasks run more reliably.  The NotificationTasks
//              might not be in order because the ordering in the user map can
//              vary. - dj@opennms.org
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.xml.event.Event;


public class TaskCreationTest extends NotificationsTestCase {
    
    private BroadcastEventProcessor m_eventProcessor;
    private Notification m_notif;
    private Map<String, String> m_params;
    private String[] m_commands;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TaskCreationTest.class);
    }
    
    
    
    protected void setUp() throws Exception {
        super.setUp();
        m_eventProcessor = m_notifd.getBroadcastEventProcessor();
        
        m_notif = m_notificationManager.getNotification("nodeDown");
        MockNode node = m_network.getNode(1);
        Event nodeDownEvent = node.createDownEvent();
        
        m_params = m_eventProcessor.buildParameterMap(m_notif, nodeDownEvent, 1);
        m_commands = new String[]{ "email" };

    }



    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testMakeEmailTask() throws Exception {
        long startTime = System.currentTimeMillis();
        NotificationTask task = m_eventProcessor.makeEmailTask(startTime, m_params, 1, "brozow@opennms.org", m_commands, new LinkedList(), null);
        assertNotNull(task);
        assertEquals("brozow@opennms.org", task.getEmail());
        assertEquals(startTime, task.getSendTime());
    
        
    }

    public void testMakeUserTask() throws Exception {
        long startTime = System.currentTimeMillis();
        NotificationTask task = m_eventProcessor.makeUserTask(startTime, m_params, 1, "brozow", m_commands, new LinkedList(), null);
        assertNotNull(task);
        assertEquals("brozow@opennms.org", task.getEmail());
        assertEquals(startTime, task.getSendTime());
    
        
    }

    public void testMakeGroupTasks() throws Exception {
        long startTime = System.currentTimeMillis();
        NotificationTask[] tasks = m_eventProcessor.makeGroupTasks(startTime, m_params, 1, "EscalationGroup", m_commands, new LinkedList(), null, 1000);
        assertNotNull(tasks);
        assertEquals(2, tasks.length);
        assertEquals("brozow@opennms.org", tasks[0].getEmail());
        assertEquals(startTime, tasks[0].getSendTime());
        assertEquals("david@opennms.org", tasks[1].getEmail());
        assertEquals(startTime+1000, tasks[1].getSendTime());
        
    }
    
    public void testMakeGroupTasksWithDutySchedule() throws Exception {
        final String groupName = "EscalationGroup";

        // set up a duty schedule for the group
        Group group = m_groupManager.getGroup(groupName);
        group.addDutySchedule("MoTuWeThFr0900-1700");
        m_groupManager.saveGroups();
        
        Date day = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse("21-FEB-2005 11:59:56");
        long dayTime = day.getTime();
        NotificationTask[] dayTasks = m_eventProcessor.makeGroupTasks(dayTime, m_params, 1, "EscalationGroup", m_commands, new LinkedList(), null, 1000);
        assertNotNull(dayTasks);
        assertEquals(2, dayTasks.length);
        assertEquals("brozow@opennms.org", dayTasks[0].getEmail());
        assertEquals(dayTime, dayTasks[0].getSendTime());
        assertEquals("david@opennms.org", dayTasks[1].getEmail());
        assertEquals(dayTime+1000, dayTasks[1].getSendTime());
        
        Date night = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse("21-FEB-2005 23:00:00");
        long nightTime = night.getTime();
        NotificationTask[] nightTasks = m_eventProcessor.makeGroupTasks(nightTime, m_params, 1, "EscalationGroup", m_commands, new LinkedList(), null, 1000);
        assertNotNull(nightTasks);
        assertEquals(2, nightTasks.length);
        assertEquals("brozow@opennms.org", nightTasks[0].getEmail());
        assertEquals(nightTime+36000000, nightTasks[0].getSendTime());
        assertEquals("david@opennms.org", nightTasks[1].getEmail());
        assertEquals(nightTime+1000+36000000, nightTasks[1].getSendTime());
    }
    
    public void testMakeRoleTasks() throws Exception {
        int interval = 1000;

        Date day = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse("21-FEB-2005 11:59:56");
        long dayTime = day.getTime();
        NotificationTask[] tasks = m_eventProcessor.makeRoleTasks(dayTime, m_params, 1, "oncall", m_commands, new LinkedList(), null, interval);
        assertNotNull(tasks);
        assertEquals(1, tasks.length);
        assertEquals("brozow@opennms.org", tasks[0].getEmail());
        assertEquals(dayTime, tasks[0].getSendTime());
        
        Date sunday = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse("30-JAN-2005 11:59:56"); // sunday
        long sundayTime = sunday.getTime();
        NotificationTask[] sundayTasks = m_eventProcessor.makeRoleTasks(sundayTime, m_params, 1, "oncall", m_commands, new LinkedList(), null, interval);
        assertNotNull(sundayTasks);
        assertEquals(2, sundayTasks.length);
        
        // The NotificationTasks might not be in order because the ordering in the user map can vary.
        int brozowsFound = 0;
        int adminsFound = 0;
        long notificationExpectedTime = sundayTime;
        for (NotificationTask task : sundayTasks) {
            if ("brozow@opennms.org".equals(task.getEmail())) {
                brozowsFound++;
                assertEquals("time for brozow notification", notificationExpectedTime, task.getSendTime());
            } else if ("admin@opennms.org".equals(task.getEmail())) {
                adminsFound++;
                assertEquals("time for admin notification", notificationExpectedTime, task.getSendTime());
            } else {
                fail("Sunday notification tasks list contains an unexpected user email address '" + task.getEmail() + "': " + task);
            }
            
            notificationExpectedTime += interval; // add the interval for the next notification (if any)
        }
        
        assertEquals("Number of brozows found", 1, brozowsFound);
        assertEquals("Number of admins found", 1, adminsFound);
    }
    
    

}
