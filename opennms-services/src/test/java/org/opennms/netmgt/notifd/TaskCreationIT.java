/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.xml.event.Event;


public class TaskCreationTest extends NotificationsTestCase {
    
    private static final int INTERVAL = 1000;
    private BroadcastEventProcessor m_eventProcessor;
    private Notification m_notif;
    private Map<String, String> m_params;
    private String[] m_commands;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        m_eventProcessor = m_notifd.getBroadcastEventProcessor();
        
        m_notif = m_notificationManager.getNotification("nodeDown");
        MockNode node = m_network.getNode(1);
        Event nodeDownEvent = node.createDownEvent();
        
        m_params = BroadcastEventProcessor.buildParameterMap(m_notif, nodeDownEvent, 1);
        m_commands = new String[]{ "email" };
    }


    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }


    @Test
    public void testMakeEmailTask() throws Exception {
        long startTime = now();

        NotificationTask task = m_eventProcessor.makeEmailTask(startTime, m_params, 1, "brozow@opennms.org", m_commands, new LinkedList<NotificationTask>(), null);

        assertNotNull(task);
        assertEquals("brozow@opennms.org", task.getEmail());
        assertEquals(startTime, task.getSendTime());
    }



    private long now() {
        return System.currentTimeMillis();
    }

    @Test
    public void testMakeUserTask() throws Exception {
        long startTime = now();

        NotificationTask task = m_eventProcessor.makeUserTask(startTime, m_params, 1, "brozow", m_commands, new LinkedList<NotificationTask>(), null);

        assertNotNull(task);
        assertEquals("brozow@opennms.org", task.getEmail());
        assertEquals(startTime, task.getSendTime());

    }
    
    private void assertTasksWithEmail(NotificationTask[] tasks, String... emails) throws Exception {
        assertNotNull(tasks);
        assertEquals("Unexpected number of tasks", emails.length, tasks.length);
        for(String email : emails) {
            assertNotNull("Expected to find a task with email "+email+" in "+tasks, findTaskWithEmail(tasks, email));
        }
    }
    
    private void assertStartInterval(NotificationTask[] tasks, long startTime, long interval) {
        assertNotNull(tasks);
        long expectedTime = startTime;
        for(NotificationTask task : tasks) {
            assertEquals("Expected task "+task+" to have sendTime "+expectedTime, expectedTime, task.getSendTime());
            expectedTime += interval;
        }
    }

    @Test
    public void testMakeGroupTasks() throws Exception {
        long startTime = now();
        
        NotificationTask[] tasks = m_eventProcessor.makeGroupTasks(startTime, m_params, 1, "EscalationGroup", m_commands, new LinkedList<NotificationTask>(), null, INTERVAL);

        assertTasksWithEmail(tasks, "brozow@opennms.org", "david@opennms.org");
        assertStartInterval(tasks, startTime, INTERVAL);
        
    }
    
    @Test
    public void testMakeGroupTasksWithDutySchedule() throws Exception {
        final String groupName = "EscalationGroup";

        // set up a duty schedule for the group
        Group group = m_groupManager.getGroup(groupName);
        group.addDutySchedule("MoTuWeThFr0900-1700");
        m_groupManager.saveGroups();
        
        long dayTime = getTimeStampFor("21-FEB-2005 11:59:56");
        
        NotificationTask[] dayTasks = m_eventProcessor.makeGroupTasks(dayTime, m_params, 1, "EscalationGroup", m_commands, new LinkedList<NotificationTask>(), null, INTERVAL);
        
        assertTasksWithEmail(dayTasks, "brozow@opennms.org", "david@opennms.org");
        assertStartInterval(dayTasks, dayTime, INTERVAL);
        
        long nightTime = getTimeStampFor("21-FEB-2005 23:00:00");

        NotificationTask[] nightTasks = m_eventProcessor.makeGroupTasks(nightTime, m_params, 1, "EscalationGroup", m_commands, new LinkedList<NotificationTask>(), null, INTERVAL);

        assertTasksWithEmail(nightTasks, "brozow@opennms.org", "david@opennms.org");
        // delayed start due to group duty schedule
        assertStartInterval(nightTasks, nightTime+36000000, INTERVAL);

    }
    
    @Test
    public void testMakeRoleTasks() throws Exception {
        long dayTime = getTimeStampFor("21-FEB-2005 11:59:56");

        NotificationTask[] tasks = m_eventProcessor.makeRoleTasks(dayTime, m_params, 1, "oncall", m_commands, new LinkedList<NotificationTask>(), null, INTERVAL);

        assertTasksWithEmail(tasks, "brozow@opennms.org");
        assertStartInterval(tasks, dayTime, INTERVAL);
        
        long sundayTime = getTimeStampFor("30-JAN-2005 11:59:56"); // sunday

        NotificationTask[] sundayTasks = m_eventProcessor.makeRoleTasks(sundayTime, m_params, 1, "oncall", m_commands, new LinkedList<NotificationTask>(), null, INTERVAL);
        
        assertTasksWithEmail(sundayTasks, "brozow@opennms.org", "admin@opennms.org");
        assertStartInterval(sundayTasks, sundayTime, INTERVAL);

    }



    private long getTimeStampFor(String timeString) throws ParseException {
        return new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse(timeString).getTime();
    }
    
    private NotificationTask findTaskWithEmail(NotificationTask[] tasks, String email) throws Exception {
        assertNotNull(email);
        for(NotificationTask task : tasks) {
            assertNotNull(task);
            if (email.equals(task.getEmail())) {
                return task;
            }
        }
        return null;
    }
    
    

}
