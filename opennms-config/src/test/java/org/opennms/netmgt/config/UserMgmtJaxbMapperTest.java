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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.groups.Time;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.model.usermgmt.ContactType;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.OnCallSchedule;
import org.opennms.netmgt.model.usermgmt.SecurityRole;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;

public class UserMgmtJaxbMapperTest {

    private static Map<String, SecurityRole> roles(final String... names) {
        final Map<String, SecurityRole> map = new HashMap<>();
        for (final String name : names) {
            map.put(name, new SecurityRole(name, null));
        }
        return map;
    }

    private static User user(final String id) {
        final User user = new User();
        user.setUserId(id);
        user.setPassword("hash", true);
        return user;
    }

    private static Contact contact(final String type, final String info) {
        final Contact contact = new Contact();
        contact.setType(type);
        contact.setInfo(info);
        return contact;
    }

    private static Role nightRole() {
        final Role role = new Role();
        role.setName("Night");
        role.setMembershipGroup("Ops");
        role.setSupervisor("jane");
        return role;
    }

    private static Map<String, UserAccount> users(final String... names) {
        final Map<String, UserAccount> map = new HashMap<>();
        for (final String name : names) {
            map.put(name, new UserAccount(name));
        }
        return map;
    }

    private static Map<String, UserGroup> groups(final String... names) {
        final Map<String, UserGroup> map = new HashMap<>();
        for (final String name : names) {
            map.put(name, new UserGroup(name));
        }
        return map;
    }

    @Test
    public void testUserRoundTrip() {
        final User source = user("jane");
        source.setFullName("Jane Doe");
        source.setUserComments("night shift");
        source.setTuiPin("1234");
        source.setTimeZoneId("America/New_York");
        source.addContact(contact("email", "jane@example.org"));
        final Contact mobile = contact("mobilePhone", "555-0100");
        mobile.setServiceProvider("ACME");
        source.addContact(mobile);
        source.addDutySchedule("MoTuWeThFr0800-1700");
        source.addRole("ROLE_USER");

        final UserAccount account = new UserAccount();
        UserMgmtJaxbMapper.applyUser(source, account, roles("ROLE_USER"));
        final User back = UserMgmtJaxbMapper.toUser(account);

        assertEquals("jane", back.getUserId());
        assertEquals("Jane Doe", back.getFullName().get());
        assertEquals("night shift", back.getUserComments().get());
        assertEquals("hash", back.getPassword().getEncryptedPassword());
        assertTrue(back.getPassword().getSalt());
        assertEquals("1234", back.getTuiPin().get());
        assertEquals("America/New_York", back.getTimeZoneId().get().getId());
        assertEquals(Arrays.asList("MoTuWeThFr0800-1700"), back.getDutySchedules());
        assertEquals(Arrays.asList("ROLE_USER"), back.getRoles());
        assertEquals("ACME", back.getContacts().stream()
                .filter(c -> "mobilePhone".equals(c.getType())).findFirst().get().getServiceProvider().get());
    }

    @Test
    public void testFirstContactOfATypeWins() {
        final User source = user("jane");
        source.addContact(contact("email", "first@example.org"));
        source.addContact(contact("email", "second@example.org"));
        final UserAccount account = new UserAccount();
        UserMgmtJaxbMapper.applyUser(source, account, roles());
        assertEquals(1, account.getContacts().size());
        assertEquals("first@example.org", account.getContact(ContactType.email).getInfo());
    }

    @Test
    public void testRemovedContactIsRemoved() {
        final UserAccount account = new UserAccount("jane");
        account.setContact(ContactType.email, "old@example.org", null);
        UserMgmtJaxbMapper.applyUser(user("jane"), account, roles());
        assertTrue(account.getContacts().isEmpty());
    }

    @Test
    public void testUnknownContactTypeFails() {
        final User source = user("jane");
        source.addContact(contact("carrierPigeon", "coop 4"));
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> UserMgmtJaxbMapper.applyUser(source, new UserAccount(), roles()));
        assertEquals("User \"jane\" has a contact of the unknown type \"carrierPigeon\".", e.getMessage());
    }

    @Test
    public void testUnknownSecurityRoleFails() {
        final User source = user("jane");
        source.addRole("ROLE_WIZARD");
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> UserMgmtJaxbMapper.applyUser(source, new UserAccount(), roles("ROLE_USER")));
        assertEquals("User \"jane\" has the unknown security role \"ROLE_WIZARD\".", e.getMessage());
    }

    @Test
    public void testTooLongValueFailsAndCountsCodePoints() {
        final User source = user("jane");
        // 256 emoji are 512 UTF-16 chars but 256 code points: this fits
        source.setFullName(new String(new char[256]).replace("\0", "😀"));
        UserMgmtJaxbMapper.applyUser(source, new UserAccount(), roles());

        source.setFullName(new String(new char[257]).replace("\0", "x"));
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> UserMgmtJaxbMapper.applyUser(source, new UserAccount(), roles()));
        assertEquals("User \"jane\": the full name is longer than 256 characters.", e.getMessage());
    }

    @Test
    public void testInvalidTimeZoneIsSkippedOnLoad() {
        final UserAccount account = new UserAccount("jane");
        account.setTimeZoneId("Mars/Olympus_Mons");
        final User user = UserMgmtJaxbMapper.toUser(account);
        assertEquals("jane", user.getUserId());
        assertFalse(user.getTimeZoneId().isPresent());
    }

    @Test
    public void testGroupWithUnknownMemberFails() {
        final Group group = new Group();
        group.setName("Ops");
        group.addUser("ghost");
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> UserMgmtJaxbMapper.applyGroup(group, new UserGroup(), new HashMap<>()));
        assertEquals("Group \"Ops\" has the member \"ghost\", which is not a user.", e.getMessage());
    }

    @Test
    public void testGroupDuplicateMemberIsKeptOnce() {
        final Group group = new Group();
        group.setName("Ops");
        group.addUser("jane");
        group.addUser("jane");
        final UserGroup target = new UserGroup();
        UserMgmtJaxbMapper.applyGroup(group, target, users("jane"));
        assertEquals(1, target.getMembers().size());
    }

    @Test
    public void testRoleRoundTrip() {
        final Role role = nightRole();
        role.setDescription("nights");
        final Schedule schedule = new Schedule();
        schedule.setName("jane");
        schedule.setType("weekly");
        final Time time = new Time();
        time.setId("t1");
        time.setDay("monday");
        time.setBegins("18:00:00");
        time.setEnds("23:00:00");
        schedule.addTime(time);
        role.addSchedule(schedule);

        final OnCallRole target = new OnCallRole();
        UserMgmtJaxbMapper.applyRole(role, target, groups("Ops"), users("jane"));
        assertEquals(role, UserMgmtJaxbMapper.toRole(target));
    }

    @Test
    public void testRoleWithUnknownSupervisorFails() {
        final Role role = nightRole();
        role.setSupervisor("ghost");
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> UserMgmtJaxbMapper.applyRole(role, new OnCallRole(), groups("Ops"), new HashMap<>()));
        assertEquals("On-call role \"Night\" has the supervisor \"ghost\", which is not a user.", e.getMessage());
    }

    @Test
    public void testUnchangedSchedulesKeepTheirEntities() {
        final Role role = nightRole();
        final Schedule schedule = new Schedule();
        schedule.setName("jane");
        schedule.setType("daily");
        final Time time = new Time();
        time.setBegins("18:00:00");
        time.setEnds("23:00:00");
        schedule.addTime(time);
        role.addSchedule(schedule);

        final OnCallRole target = new OnCallRole();
        final Map<String, UserAccount> users = users("jane");
        final Map<String, UserGroup> groups = groups("Ops");
        UserMgmtJaxbMapper.applyRole(role, target, groups, users);
        final OnCallSchedule first = target.getSchedules().get(0);
        UserMgmtJaxbMapper.applyRole(role, target, groups, users);
        assertSame(first, target.getSchedules().get(0));
    }

    @Test
    public void testLoadKeepsValuesExactly() {
        // the JAXB setters HTML-encode full names and contacts and trim group values; users.xml was loaded through
        // the fields, so a load from the database must not change the stored text either
        final String raw = "O'Brien & <Co>";
        final UserAccount account = new UserAccount("obrien");
        account.setFullName(raw);
        account.setContact(ContactType.email, raw, raw);
        final User user = UserMgmtJaxbMapper.toUser(account);
        assertEquals(raw, user.getFullName().get());
        assertEquals(raw, user.getContacts().get(0).getInfo().get());
        assertEquals(raw, user.getContacts().get(0).getServiceProvider().get());

        final UserGroup group = new UserGroup(" Ops ");
        group.setComments(" spaced ");
        group.setDefaultMap(" map ");
        final Group xml = UserMgmtJaxbMapper.toGroup(group);
        assertEquals(" Ops ", xml.getName());
        assertEquals(" spaced ", xml.getComments().get());
        assertEquals(" map ", xml.getDefaultMap().get());
    }

    @Test
    public void testLoadAndSaveDoNotChangeTheText() {
        final String raw = "O'Brien & <Co>";
        final UserAccount account = new UserAccount("obrien");
        account.setFullName(raw);
        UserMgmtJaxbMapper.applyUser(UserMgmtJaxbMapper.toUser(account), account, roles());
        UserMgmtJaxbMapper.applyUser(UserMgmtJaxbMapper.toUser(account), account, roles());
        assertEquals(raw, account.getFullName());
    }

    @Test
    public void testUserWithoutPasswordHashGetsAPasswordThatMatchesNothing() {
        // password_hash is nullable; UserManager dereferences getPassword() for every user, so a null would stop
        // the whole user list (and all web logins)
        final User user = UserMgmtJaxbMapper.toUser(new UserAccount("nopass"));
        assertEquals("", user.getPassword().getEncryptedPassword());
        assertFalse(user.getPassword().getSalt());

        final UserAccount saved = new UserAccount();
        UserMgmtJaxbMapper.applyUser(user, saved, roles());
        assertNull(saved.getPasswordHash());
    }
}
