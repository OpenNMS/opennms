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
package org.opennms.netmgt.config.mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.api.UserConfig.ContactType;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.model.OnmsUser;

public class UserGroupManagerTest {
    private GroupManager m_groupManager;
    private UserManager m_userManager;

    private User brozow;
    private User admin;
    private User upUser;
    private User david;

    private Role oncall;
    private Role unscheduled;

    private Date night;
    private Date day;
    private Date sunday;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        m_groupManager = new MockGroupManager(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "groups.xml", new String[][] {}));
        m_userManager = new MockUserManager(m_groupManager, ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "users.xml", new String[][] {}));
        
        night = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse("21-FEB-2005 23:00:00"); // monday
        day = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse("21-FEB-2005 11:59:56"); // monday
        sunday = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse("30-JAN-2005 11:59:56"); // sunday

        brozow = m_userManager.getUser("brozow");
        assertNotNull(brozow);
        assertEquals("brozow", brozow.getUserId());
        admin = m_userManager.getUser("admin");
        assertNotNull(admin);
        assertEquals("admin", admin.getUserId());
        upUser = m_userManager.getUser("upUser");
        assertNotNull(upUser);
        assertEquals("upUser", upUser.getUserId());
        david = m_userManager.getUser("david");
        assertNotNull(david);
        assertEquals("david", david.getUserId());
        
        oncall = m_groupManager.getRole("oncall");
        assertNotNull(oncall);
        assertEquals("oncall", oncall.getName());

        unscheduled = m_groupManager.getRole("unscheduled");
        assertNotNull(unscheduled);
        assertEquals("unscheduled", unscheduled.getName());
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }
    
    @Test
    public void testContactInfo() throws Exception {
    	// verify that email address is loadable by default
    	User admin = m_userManager.getUser("admin");
    	String adminEmail = m_userManager.getContactInfo("admin",  ContactType.email);
    	
    	OnmsUser onmsAdmin = m_userManager.getOnmsUser("admin");
    	String onmsAdminEmail = onmsAdmin.getEmail();
    	
    	assertEquals("admin@opennms.org", adminEmail);
    	assertEquals(adminEmail, onmsAdminEmail);
    	
    	// verify that email is overwritten
    	onmsAdmin.setEmail("admin@opennms.com");
    	m_userManager.save(onmsAdmin); 
    	assertEquals("admin@opennms.com", m_userManager.getContactInfo("admin", ContactType.email));

    	m_userManager.setContactInfo("admin", ContactType.email, "admin@opennms.org"); // reset
    	admin = m_userManager.getUser("admin"); // reload
    	m_userManager.saveUser("admin", admin); // should be saved too
    	assertEquals("admin@opennms.org", m_userManager.getContactInfo("admin", ContactType.email));
    }
    
    @Test
    public void testGetUserNames() throws Exception {
        List<String> userNameList = m_userManager.getUserNames();
        assertEquals(4, userNameList.size());
        assertTrue(userNameList.contains("admin"));
        assertTrue(userNameList.contains("brozow"));
        assertTrue(userNameList.contains("upUser"));
        assertTrue(userNameList.contains("david"));
    }
    
    @Test
    public void testRenameUser() throws Exception{
        m_userManager.renameUser("brozow", "brozow2");
        List<String> userNameList = m_userManager.getUserNames();
       
        assertEquals(4, userNameList.size());
        assertTrue(userNameList.contains("brozow2"));
        
        List<String> group1Users = m_groupManager.getGroup("InitialGroup").getUsers();
        List<String> group2Users = m_groupManager.getGroup("EscalationGroup").getUsers();
        List<String> group3Users = m_groupManager.getGroup("UpGroup").getUsers();
        
        assertFalse(group1Users.contains("brozow"));
        assertFalse(group2Users.contains("brozow"));
        assertFalse(group3Users.contains("brozow"));
        
    }
    
    @Test
    public void testSaveUser() throws Exception {
        String userName = "brozow";
        User user = brozow;
        
        Calendar nightCal = Calendar.getInstance();
        nightCal.setTime(night);

        Calendar dayCal = Calendar.getInstance();
        dayCal.setTime(day);
        
        // initial has no duty schedule so always on duty
        assertTrue(m_userManager.isUserOnDuty(userName, dayCal));
        assertTrue(m_userManager.isUserOnDuty(userName, nightCal));

        brozow.addDutySchedule("MoTuWeThFr0900-1700");
        m_userManager.saveUser(userName, user);
        
        // now user is on duty only from 9-5
        assertTrue(m_userManager.isUserOnDuty(userName, dayCal));
        assertFalse(m_userManager.isUserOnDuty(userName, nightCal));
        
    }
    
    @Test
    public void testGetGroupNames() throws Exception {
        List<String> userNameList = m_groupManager.getGroupNames();
        assertEquals(3, userNameList.size());
        assertTrue(userNameList.contains("InitialGroup"));
        assertTrue(userNameList.contains("EscalationGroup"));
        assertTrue(userNameList.contains("UpGroup"));

    }
    
    @Test
    public void testSaveGroups() throws Exception {
        final String groupName = "UpGroup";
        Group group = m_groupManager.getGroup(groupName);
        
        Calendar nightCal = Calendar.getInstance();
        nightCal.setTime(night);

        Calendar dayCal = Calendar.getInstance();
        dayCal.setTime(day);
        
        // initial has no duty schedule so always on duty
        assertTrue(m_groupManager.isGroupOnDuty(groupName, dayCal));
        assertEquals(0, m_groupManager.groupNextOnDuty(groupName, dayCal));
        assertTrue(m_groupManager.isGroupOnDuty(groupName, nightCal));
        assertEquals(0, m_groupManager.groupNextOnDuty(groupName, nightCal));

        group.addDutySchedule("MoTuWeThFr0900-1700");
        
        
        m_groupManager.saveGroups();
        
        // now user is on duty only from 9-5
        assertTrue(m_groupManager.isGroupOnDuty(groupName, dayCal));
        assertEquals(0, m_groupManager.groupNextOnDuty(groupName, dayCal));
        assertFalse(m_groupManager.isGroupOnDuty(groupName, nightCal));
        assertEquals(36000000, m_groupManager.groupNextOnDuty(groupName, nightCal));
        

    }
    
    @Test
    public void testGetRoles() {
        assertRoles(m_groupManager.getRoleNames(), new Role[] { oncall, unscheduled });
    }
    
    @Test
    public void testUserHasRole() throws Exception {
        assertTrue(m_userManager.userHasRole(brozow, "oncall"));
        assertTrue(m_userManager.userHasRole(admin, "oncall"));
        assertFalse(m_userManager.userHasRole(upUser, "oncall"));
        assertTrue(m_userManager.userHasRole(david, "oncall"));
    }
    
    @Test
    public void testGetUsersWithRole() throws Exception {
        String[] userNames = m_userManager.getUsersWithRole("oncall");
        assertUsers(userNames, new User[] { brozow, admin, david });
        
    }
    
    @Test
    public void testUserScheduledForRoleNew() throws Exception {
        Date[] dates = new Date[] {night, day, sunday};
        for (int i = 0; i < dates.length; i++) {
            testUsersScheduledForRolesAt(dates[i]);
        }
    }
    
    private void testUsersScheduledForRolesAt(Date date) throws Exception {
        String[] roles = m_groupManager.getRoleNames();
        for (int i = 0; i < roles.length; i++) {
            testUsersScheduleForRoleAt(roles[i], date);
            
        }
    }
    
    private void testUsersScheduleForRoleAt(String role, Date date) throws Exception {
        for(String userId : m_userManager.getUserNames()) {
            User u = m_userManager.getUser(userId);
            testUserScheduledForRoleAt(u, role, date);
        }
    }
    
    private void testUserScheduledForRoleAt(User u, String role, Date date) throws Exception {
        assertEquals("Unexpected value "+u.getUserId()+" for role "+role+" at "+date, m_userManager.isUserScheduledForRole(u, role, date), m_userManager.isUserScheduledForRole(u, role, date));
    }
    
    @Test
    public void testUserScheduledForRole() throws Exception {
        // day and night are mondays at 11 am and 11 pm respectively
        
        // brozow scheduled only MoWeFr during the day
        assertFalse(m_userManager.isUserScheduledForRole(brozow, "oncall", night));
        assertTrue(m_userManager.isUserScheduledForRole(brozow, "oncall", day));
        assertTrue(m_userManager.isUserScheduledForRole(brozow, "oncall", sunday));
        assertFalse(m_userManager.isUserScheduledForRole(brozow, "unscheduled", day));
        
        // admin scheduled only TuThSa
        assertFalse(m_userManager.isUserScheduledForRole(admin, "oncall", night));
        assertFalse(m_userManager.isUserScheduledForRole(admin, "oncall", day));
        assertTrue(m_userManager.isUserScheduledForRole(admin, "oncall", sunday));
        assertFalse(m_userManager.isUserScheduledForRole(admin, "unscheduled", day));
        
        // user upUser is not schedule for the role 'oncall' at all
        assertFalse(m_userManager.isUserScheduledForRole(upUser, "oncall", night));
        assertFalse(m_userManager.isUserScheduledForRole(upUser, "oncall", day));
        assertFalse(m_userManager.isUserScheduledForRole(upUser, "oncall", sunday));
        assertTrue(m_userManager.isUserScheduledForRole(upUser, "unscheduled", day));
        
        // david is scheduled for the night shifts
        assertTrue(m_userManager.isUserScheduledForRole(david, "oncall", night));
        assertFalse(m_userManager.isUserScheduledForRole(david, "oncall", day));
        assertFalse(m_userManager.isUserScheduledForRole(david, "oncall", sunday));
        assertFalse(m_userManager.isUserScheduledForRole(david, "unscheduled", day));

    }
    
    @Test
    public void testGetUsersScheduledForRole() throws Exception {
        String[] nightUserNames = m_userManager.getUsersScheduledForRole("oncall", night);
        assertUsers(nightUserNames, new User[]{ david });
        
        String[] dayUserNames = m_userManager.getUsersScheduledForRole("oncall", day);
        assertUsers(dayUserNames, new User[]{ brozow });
        
        String[] sundayUserNames = m_userManager.getUsersScheduledForRole("oncall", sunday);
        assertUsers(sundayUserNames, new User[] { brozow, admin });
        
    }
    
    private void assertRoles(String[] roleNames, Role[] expected) {
        if (expected == null)
            assertNull("Expected null list", roleNames);
        
        assertNotNull("Unexpected null user list", roleNames);
        assertEquals("Unexpected number of users", expected.length, roleNames.length);
        
        List<String> nameList = Arrays.asList(roleNames);
        for(int i = 0; i < expected.length; i++) {
            Role r = expected[i];
            assertTrue("Expected user "+r.getName()+" in list "+nameList, nameList.contains(r.getName()));
        }
    }


    private void assertUsers(String[] userNames, User[] expected) {
        if (expected == null)
            assertNull("Expected null list", userNames);
        
        assertNotNull("Unexpected null user list", userNames);
        assertEquals("Unexpected number of users", expected.length, userNames.length);
        
        List<String> nameList = Arrays.asList(userNames);
        for(int i = 0; i < expected.length; i++) {
            User u = expected[i];
            assertTrue("Expected user "+u.getUserId()+" in list "+nameList, nameList.contains(u.getUserId()));
        }
    }
    

}
