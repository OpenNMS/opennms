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
package org.opennms.netmgt.dao.usermgmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.model.usermgmt.ContactType;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.OnCallSchedule;
import org.opennms.netmgt.model.usermgmt.OnCallScheduleTime;
import org.opennms.netmgt.model.usermgmt.SecurityRoles;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserContact;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class UserAccountDaoIT implements InitializingBean {

    @Autowired
    private SessionFactory m_sessionFactory;

    @Autowired
    private UserAccountDao m_userDao;

    @Autowired
    private SecurityRoleDao m_roleDao;

    @Autowired
    private UserGroupDao m_groupDao;

    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    private UserAccount createUser(final String username) {
        final UserAccount user = new UserAccount(username);
        m_userDao.save(user);
        return user;
    }

    private static List<String> usernames(final List<UserAccount> users) {
        return users.stream().map(UserAccount::getUsername).collect(Collectors.toList());
    }

    @Test
    @Transactional
    public void testRoundTrip() {
        final UserAccount user = new UserAccount("jane");
        user.setFullName("Jane Doe");
        user.setComments("on the night shift");
        user.setPasswordHash("hashed");
        user.setPasswordSalted(true);
        user.setTuiPin("1234");
        user.setTimeZoneId("America/New_York");
        user.setContact(ContactType.email, "jane@example.org", null);
        user.setContact(ContactType.pagerEmail, "page-jane@example.org", null);
        user.setContact(ContactType.mobilePhone, "555-0100", "ACME Mobile");
        user.setContact(ContactType.snmpTrap, "192.0.2.1", null);
        user.getDutySchedules().addAll(Arrays.asList("MoTuWeThFr800-1700", "SaSu900-1200", "Mo0-800"));
        user.getSecurityRoles().add(m_roleDao.findByName(SecurityRoles.ROLE_USER));
        user.getSecurityRoles().add(m_roleDao.findByName(SecurityRoles.ROLE_ADMIN));
        m_userDao.save(user);
        m_userDao.flush();
        m_userDao.clear();

        final UserAccount loaded = m_userDao.findByUsername("jane");
        assertNotNull(loaded);
        assertEquals("Jane Doe", loaded.getFullName());
        assertEquals("on the night shift", loaded.getComments());
        assertEquals("hashed", loaded.getPasswordHash());
        assertTrue(loaded.isPasswordSalted());
        assertEquals("1234", loaded.getTuiPin());
        assertEquals("America/New_York", loaded.getTimeZoneId());
        assertNotNull(loaded.getCreatedTime());
        assertNotNull(loaded.getLastModified());

        assertEquals(4, loaded.getContacts().size());
        assertEquals("jane@example.org", loaded.getContact(ContactType.email).getInfo());
        assertEquals("page-jane@example.org", loaded.getContact(ContactType.pagerEmail).getInfo());
        assertEquals("555-0100", loaded.getContact(ContactType.mobilePhone).getInfo());
        assertEquals("ACME Mobile", loaded.getContact(ContactType.mobilePhone).getServiceProvider());
        assertEquals("192.0.2.1", loaded.getContact(ContactType.snmpTrap).getInfo());
        assertNull(loaded.getContact(ContactType.workPhone));

        assertEquals(Arrays.asList("MoTuWeThFr800-1700", "SaSu900-1200", "Mo0-800"), loaded.getDutySchedules());

        assertEquals(2, loaded.getSecurityRoles().size());
        assertTrue(loaded.hasSecurityRole(SecurityRoles.ROLE_USER));
        assertTrue(loaded.hasSecurityRole(SecurityRoles.ROLE_ADMIN));

        assertEquals("the user's contact type is stored by name", 1, UserMgmtSql.count(m_sessionFactory,
                "SELECT COUNT(*) FROM user_contacts WHERE user_id = ? AND contact_type = 'snmpTrap'", loaded.getId()));
    }

    @Test
    @Transactional
    public void testDefaults() {
        createUser("minimal");
        m_userDao.flush();
        m_userDao.clear();

        final UserAccount loaded = m_userDao.findByUsername("minimal");
        assertFalse(loaded.isPasswordSalted());
        assertNull(loaded.getPasswordHash());
        assertTrue(loaded.getContacts().isEmpty());
        assertTrue(loaded.getDutySchedules().isEmpty());
        assertTrue(loaded.getSecurityRoles().isEmpty());
    }

    @Test
    @Transactional
    public void testSetContactUpdatesTheExistingContact() {
        final UserAccount user = new UserAccount("jane");
        user.setContact(ContactType.email, "old@example.org", null);
        m_userDao.save(user);
        m_userDao.flush();
        final Integer contactId = user.getContact(ContactType.email).getId();

        final UserContact updated = user.setContact(ContactType.email, "new@example.org", "provider");
        assertEquals(contactId, updated.getId());
        UserMgmtSql.checkDeferredConstraints(m_sessionFactory);
        m_userDao.clear();

        UserAccount loaded = m_userDao.findByUsername("jane");
        assertEquals(1, loaded.getContacts().size());
        assertEquals("new@example.org", loaded.getContact(ContactType.email).getInfo());
        assertEquals("provider", loaded.getContact(ContactType.email).getServiceProvider());

        assertTrue(loaded.removeContact(ContactType.email));
        assertFalse(loaded.removeContact(ContactType.email));
        m_userDao.flush();
        m_userDao.clear();
        loaded = m_userDao.findByUsername("jane");
        assertTrue(loaded.getContacts().isEmpty());
        assertEquals("the removed contact is deleted, not orphaned", 0,
                UserMgmtSql.count(m_sessionFactory, "SELECT COUNT(*) FROM user_contacts WHERE user_id = ?", loaded.getId()));
    }

    @Test
    @Transactional
    public void testReplacingTheContactListKeepsOneContactPerType() {
        final UserAccount user = new UserAccount("jane");
        user.setContact(ContactType.email, "old@example.org", null);
        m_userDao.save(user);
        m_userDao.flush();

        // a new contact of the same type is inserted before the old one is deleted; the deferred
        // unique constraint lets that pass
        user.setContacts(List.of(new UserContact(ContactType.email, "new@example.org", null)));
        UserMgmtSql.checkDeferredConstraints(m_sessionFactory);
        m_userDao.clear();

        final UserAccount loaded = m_userDao.findByUsername("jane");
        assertEquals(1, loaded.getContacts().size());
        assertEquals("new@example.org", loaded.getContact(ContactType.email).getInfo());
    }

    @Test
    @Transactional
    public void testDutyScheduleOrderSurvivesEdits() {
        final UserAccount user = new UserAccount("jane");
        user.getDutySchedules().addAll(Arrays.asList("Mo800-1700", "Tu800-1700", "We800-1700"));
        m_userDao.save(user);
        m_userDao.flush();
        m_userDao.clear();

        UserAccount loaded = m_userDao.findByUsername("jane");
        loaded.getDutySchedules().remove(0);
        loaded.getDutySchedules().add(0, "Su0-2359");
        loaded.getDutySchedules().add("Fr800-1700");
        m_userDao.flush();
        m_userDao.clear();

        loaded = m_userDao.findByUsername("jane");
        assertEquals(Arrays.asList("Su0-2359", "Tu800-1700", "We800-1700", "Fr800-1700"), loaded.getDutySchedules());
    }

    @Test
    @Transactional
    public void testUsernameIsUniqueAndCaseSensitive() {
        createUser("jane");
        createUser("Jane");
        m_userDao.flush();
        m_userDao.clear();

        assertEquals("Jane", m_userDao.findByUsername("Jane").getUsername());
        assertEquals("jane", m_userDao.findByUsername("jane").getUsername());
        assertNull(m_userDao.findByUsername("JANE"));

        UserMgmtSql.assertRejected(m_sessionFactory, UserMgmtSql.UNIQUE_VIOLATION,
                "INSERT INTO users (id, username, password_salted) VALUES (nextval('users_id_seq'), 'jane', false)");
    }

    @Test
    @Transactional
    public void testContactConstraints() {
        final UserAccount user = new UserAccount("jane");
        user.setContact(ContactType.email, "jane@example.org", null);
        m_userDao.save(user);

        UserMgmtSql.assertRejected(m_sessionFactory, UserMgmtSql.CHECK_VIOLATION,
                "INSERT INTO user_contacts (id, user_id, contact_type, info) VALUES (nextval('user_contacts_id_seq'), ?, 'fax', '555-0199')",
                user.getId());
        UserMgmtSql.assertRejected(m_sessionFactory, UserMgmtSql.UNIQUE_VIOLATION,
                "INSERT INTO user_contacts (id, user_id, contact_type, info) VALUES (nextval('user_contacts_id_seq'), ?, 'email', 'other@example.org')",
                user.getId());
    }

    @Test
    @Transactional
    public void testFinders() {
        final UserAccount carol = createUser("carol");
        final UserAccount alice = createUser("alice");
        final UserAccount bob = createUser("bob");
        alice.getSecurityRoles().add(m_roleDao.findByName(SecurityRoles.ROLE_ADMIN));
        carol.getSecurityRoles().add(m_roleDao.findByName(SecurityRoles.ROLE_ADMIN));
        carol.getSecurityRoles().add(m_roleDao.findByName(SecurityRoles.ROLE_USER));
        bob.getSecurityRoles().add(m_roleDao.findByName(SecurityRoles.ROLE_USER));
        m_userDao.flush();
        m_userDao.clear();

        assertEquals(Arrays.asList("alice", "bob", "carol"), usernames(m_userDao.findAllOrderByUsername()));
        assertEquals(Arrays.asList("alice", "carol"), usernames(m_userDao.findByUsernames(Set.of("carol", "alice", "nobody"))));
        assertTrue(m_userDao.findByUsernames(Collections.emptyList()).isEmpty());
        assertEquals(Arrays.asList("alice", "carol"), usernames(m_userDao.findBySecurityRole(SecurityRoles.ROLE_ADMIN)));
        assertEquals(Arrays.asList("bob", "carol"), usernames(m_userDao.findBySecurityRole(SecurityRoles.ROLE_USER)));
        assertTrue(m_userDao.findBySecurityRole(SecurityRoles.ROLE_JMX).isEmpty());
    }

    /**
     * Deleting a user removes what belongs to them (contacts, duty schedules, role grants) and takes them out of
     * group member lists and on-call schedules, which close up around the gap.
     */
    @Test
    @Transactional
    public void testDeleteRemovesTheUserEverywhere() {
        final UserAccount alice = createUser("alice");
        final UserAccount bob = new UserAccount("bob");
        bob.setContact(ContactType.email, "bob@example.org", null);
        bob.getDutySchedules().add("MoTuWeThFr800-1700");
        bob.getSecurityRoles().add(m_roleDao.findByName(SecurityRoles.ROLE_USER));
        m_userDao.save(bob);
        final UserAccount carol = createUser("carol");

        final UserGroup group = new UserGroup("ops");
        group.getMembers().addAll(Arrays.asList(alice, bob, carol));
        m_groupDao.save(group);

        final OnCallRole role = new OnCallRole("night", group, alice);
        for (final UserAccount user : Arrays.asList(alice, bob, carol)) {
            final OnCallSchedule schedule = new OnCallSchedule(user, OnCallSchedule.Type.weekly);
            schedule.getTimes().add(new OnCallScheduleTime("monday", "00:00:00", "08:00:00"));
            role.getSchedules().add(schedule);
        }
        m_onCallRoleDao.save(role);
        m_userDao.flush();
        final Integer bobId = bob.getId();

        m_userDao.delete(bob);
        m_userDao.flush();
        m_userDao.clear();

        assertNull(m_userDao.findByUsername("bob"));
        assertEquals(Arrays.asList("alice", "carol"), usernames(m_groupDao.findByName("ops").getMembers()));
        assertEquals(Arrays.asList("alice", "carol"), m_onCallRoleDao.findByName("night").getSchedules().stream()
                .map(s -> s.getUser().getUsername()).collect(Collectors.toList()));
        for (final String table : Arrays.asList("user_contacts", "user_duty_schedules", "user_security_roles",
                "group_members", "oncall_schedules")) {
            assertEquals(table, 0, UserMgmtSql.count(m_sessionFactory, "SELECT COUNT(*) FROM " + table + " WHERE user_id = ?", bobId));
        }
        assertEquals("bob's schedule's times are gone, the others' remain", 2,
                UserMgmtSql.count(m_sessionFactory, "SELECT COUNT(*) FROM oncall_schedule_times"));
        assertNotNull("roles are not deleted with their holders", m_roleDao.findByName(SecurityRoles.ROLE_USER));
        assertEquals("positions are dense after the delete", 1, UserMgmtSql.count(m_sessionFactory,
                "SELECT COUNT(*) FROM group_members m JOIN users u ON u.id = m.user_id WHERE u.username = 'carol' AND m.position = 1"));
    }

    @Test
    @Transactional
    public void testDeleteByIdGoesThroughTheCleanup() {
        final UserAccount alice = createUser("alice");
        final UserAccount bob = createUser("bob");
        final UserAccount carol = createUser("carol");
        final UserGroup group = new UserGroup("ops");
        group.getMembers().addAll(Arrays.asList(alice, bob, carol));
        m_groupDao.save(group);
        m_userDao.flush();

        m_userDao.delete(bob.getId());
        m_userDao.flush();
        m_userDao.clear();

        assertEquals(Arrays.asList("alice", "carol"), usernames(m_groupDao.findByName("ops").getMembers()));
    }

    /** An on-call role needs its supervisor: the database refuses to delete them. groups.xml allowed it. */
    @Test
    @Transactional
    public void testDeletingASupervisorIsRefused() {
        final UserAccount supervisor = createUser("boss");
        final UserGroup group = new UserGroup("ops");
        m_groupDao.save(group);
        m_onCallRoleDao.save(new OnCallRole("night", group, supervisor));
        m_userDao.flush();

        assertThrows(DataIntegrityViolationException.class, () -> {
            m_userDao.delete(supervisor);
            m_userDao.flush();
        });
    }
}
