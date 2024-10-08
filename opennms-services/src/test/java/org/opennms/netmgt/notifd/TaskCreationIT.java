/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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


public class TaskCreationIT extends NotificationsITCase {
    
    private static final int INTERVAL = 1000;
    private Notification m_notif;
    private Map<String, String> m_params;
    private String[] m_commands;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        m_notif = m_notificationManager.getNotification("nodeDown");
        MockNode node = m_network.getNode(1);
        Event nodeDownEvent = node.createDownEvent();

        m_params = m_eventProcessor.buildParameterMap(m_notif, nodeDownEvent, 1);
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
