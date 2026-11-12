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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.OnCallSchedule;
import org.opennms.netmgt.model.usermgmt.OnCallScheduleTime;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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
public class OnCallRoleDaoIT implements InitializingBean {

    @Autowired
    private SessionFactory m_sessionFactory;

    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    @Autowired
    private UserAccountDao m_userDao;

    @Autowired
    private UserGroupDao m_groupDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    private UserAccount createUser(final String username) {
        final UserAccount user = new UserAccount(username);
        m_userDao.save(user);
        return user;
    }

    private UserGroup createGroup(final String name, final UserAccount... members) {
        final UserGroup group = new UserGroup(name);
        group.getMembers().addAll(Arrays.asList(members));
        m_groupDao.save(group);
        return group;
    }

    private static OnCallSchedule schedule(final UserAccount user, final OnCallSchedule.Type type, final OnCallScheduleTime... times) {
        final OnCallSchedule schedule = new OnCallSchedule(user, type);
        schedule.getTimes().addAll(Arrays.asList(times));
        return schedule;
    }

    private static List<String> names(final List<OnCallRole> roles) {
        return roles.stream().map(OnCallRole::getName).collect(Collectors.toList());
    }

    private static List<String> scheduledUsers(final OnCallRole role) {
        return role.getSchedules().stream().map(s -> s.getUser().getUsername()).collect(Collectors.toList());
    }

    @Test
    @Transactional
    public void testRoundTrip() {
        final UserAccount alice = createUser("alice");
        final UserAccount bob = createUser("bob");
        final UserAccount boss = createUser("boss");
        final UserGroup group = createGroup("ops", alice, bob);

        final OnCallRole role = new OnCallRole("night", group, boss);
        role.setDescription("night shift");
        final OnCallScheduleTime monday = new OnCallScheduleTime("monday", "17:00:00", "23:59:59");
        monday.setTimeId("t1");
        role.getSchedules().add(schedule(bob, OnCallSchedule.Type.weekly,
                monday, new OnCallScheduleTime("tuesday", "00:00:00", "08:00:00")));
        role.getSchedules().add(schedule(alice, OnCallSchedule.Type.specific,
                new OnCallScheduleTime(null, "21-Feb-2025 18:00:00", "22-Feb-2025 06:00:00")));
        role.getSchedules().add(schedule(alice, OnCallSchedule.Type.monthly,
                new OnCallScheduleTime("15", "00:00:00", "23:59:59")));
        role.getSchedules().add(schedule(bob, OnCallSchedule.Type.daily,
                new OnCallScheduleTime(null, "12:00:00", "13:00:00")));
        m_onCallRoleDao.save(role);
        m_onCallRoleDao.flush();
        m_onCallRoleDao.clear();

        final OnCallRole loaded = m_onCallRoleDao.findByName("night");
        assertNotNull(loaded);
        assertEquals("night shift", loaded.getDescription());
        assertEquals("ops", loaded.getMembershipGroup().getName());
        assertEquals("boss", loaded.getSupervisor().getUsername());
        assertNotNull(loaded.getCreatedTime());
        assertNotNull(loaded.getLastModified());

        assertEquals("schedules keep their order", Arrays.asList("bob", "alice", "alice", "bob"), scheduledUsers(loaded));
        assertEquals(Arrays.asList(OnCallSchedule.Type.weekly, OnCallSchedule.Type.specific, OnCallSchedule.Type.monthly, OnCallSchedule.Type.daily),
                loaded.getSchedules().stream().map(OnCallSchedule::getType).collect(Collectors.toList()));

        final List<OnCallScheduleTime> weekly = loaded.getSchedules().get(0).getTimes();
        assertEquals("times keep their order", Arrays.asList("monday", "tuesday"),
                weekly.stream().map(OnCallScheduleTime::getDay).collect(Collectors.toList()));
        assertEquals("t1", weekly.get(0).getTimeId());
        assertEquals("17:00:00", weekly.get(0).getBegins());
        assertEquals("23:59:59", weekly.get(0).getEnds());
        assertNull(weekly.get(1).getTimeId());

        final OnCallScheduleTime specific = loaded.getSchedules().get(1).getTimes().get(0);
        assertNull(specific.getDay());
        assertEquals("21-Feb-2025 18:00:00", specific.getBegins());
        assertEquals("22-Feb-2025 06:00:00", specific.getEnds());
        assertEquals("15", loaded.getSchedules().get(2).getTimes().get(0).getDay());

        assertEquals("the schedule type is stored by name", 1, UserMgmtSql.count(m_sessionFactory,
                "SELECT COUNT(*) FROM oncall_schedules WHERE oncall_role_id = ? AND schedule_type = 'specific' AND position = 1", loaded.getId()));
    }

    @Test
    @Transactional
    public void testRemovingASchedule() {
        final UserAccount alice = createUser("alice");
        final UserAccount bob = createUser("bob");
        final UserAccount carol = createUser("carol");
        final OnCallRole role = new OnCallRole("night", createGroup("ops", alice, bob, carol), alice);
        for (final UserAccount user : Arrays.asList(alice, bob, carol)) {
            role.getSchedules().add(schedule(user, OnCallSchedule.Type.daily,
                    new OnCallScheduleTime(null, "00:00:00", "08:00:00"), new OnCallScheduleTime(null, "16:00:00", "23:59:59")));
        }
        m_onCallRoleDao.save(role);
        m_onCallRoleDao.flush();
        m_onCallRoleDao.clear();

        OnCallRole loaded = m_onCallRoleDao.findByName("night");
        loaded.getSchedules().remove(0);
        loaded.getSchedules().get(0).getTimes().remove(1);
        m_onCallRoleDao.flush();
        m_onCallRoleDao.clear();

        loaded = m_onCallRoleDao.findByName("night");
        assertEquals(Arrays.asList("bob", "carol"), scheduledUsers(loaded));
        assertEquals(1, loaded.getSchedules().get(0).getTimes().size());
        assertEquals(2, loaded.getSchedules().get(1).getTimes().size());
        assertEquals("the removed schedule and time are deleted, not orphaned", 3,
                UserMgmtSql.count(m_sessionFactory, "SELECT COUNT(*) FROM oncall_schedule_times"));
        assertEquals(2, UserMgmtSql.count(m_sessionFactory, "SELECT COUNT(*) FROM oncall_schedules"));
    }

    @Test
    @Transactional
    public void testDeleteRoleKeepsItsGroupAndUsers() {
        final UserAccount alice = createUser("alice");
        final OnCallRole role = new OnCallRole("night", createGroup("ops", alice), alice);
        role.getSchedules().add(schedule(alice, OnCallSchedule.Type.daily, new OnCallScheduleTime(null, "00:00:00", "08:00:00")));
        m_onCallRoleDao.save(role);
        m_onCallRoleDao.flush();

        m_onCallRoleDao.delete(role);
        m_onCallRoleDao.flush();
        m_onCallRoleDao.clear();

        assertNull(m_onCallRoleDao.findByName("night"));
        assertNotNull(m_groupDao.findByName("ops"));
        assertNotNull(m_userDao.findByUsername("alice"));
        assertEquals(0, UserMgmtSql.count(m_sessionFactory, "SELECT COUNT(*) FROM oncall_schedules"));
        assertEquals(0, UserMgmtSql.count(m_sessionFactory, "SELECT COUNT(*) FROM oncall_schedule_times"));
    }

    @Test
    @Transactional
    public void testConstraints() {
        final UserAccount alice = createUser("alice");
        final OnCallRole role = new OnCallRole("night", createGroup("ops", alice), alice);
        m_onCallRoleDao.save(role);

        UserMgmtSql.assertRejected(m_sessionFactory, UserMgmtSql.UNIQUE_VIOLATION,
                "INSERT INTO oncall_roles (id, name, membership_group_id, supervisor_user_id) VALUES (nextval('oncall_roles_id_seq'), 'night', ?, ?)",
                role.getMembershipGroup().getId(), alice.getId());
        UserMgmtSql.assertRejected(m_sessionFactory, UserMgmtSql.CHECK_VIOLATION,
                "INSERT INTO oncall_schedules (id, oncall_role_id, user_id, schedule_type, position) VALUES (nextval('oncall_schedules_id_seq'), ?, ?, 'hourly', 0)",
                role.getId(), alice.getId());
        UserMgmtSql.assertRejected(m_sessionFactory, UserMgmtSql.FOREIGN_KEY_VIOLATION,
                "INSERT INTO oncall_roles (id, name, membership_group_id, supervisor_user_id) VALUES (nextval('oncall_roles_id_seq'), 'day', -1, ?)",
                alice.getId());
    }

    @Test
    @Transactional
    public void testFinders() {
        final UserAccount alice = createUser("alice");
        final UserAccount bob = createUser("bob");
        final UserAccount boss = createUser("boss");
        final UserGroup ops = createGroup("ops", alice, bob);
        final UserGroup dev = createGroup("dev", bob);

        final OnCallRole night = new OnCallRole("night", ops, boss);
        night.getSchedules().add(schedule(alice, OnCallSchedule.Type.daily, new OnCallScheduleTime(null, "00:00:00", "08:00:00")));
        night.getSchedules().add(schedule(alice, OnCallSchedule.Type.daily, new OnCallScheduleTime(null, "16:00:00", "23:59:59")));
        night.getSchedules().add(schedule(bob, OnCallSchedule.Type.daily, new OnCallScheduleTime(null, "08:00:00", "16:00:00")));
        m_onCallRoleDao.save(night);
        final OnCallRole day = new OnCallRole("day", ops, alice);
        day.getSchedules().add(schedule(bob, OnCallSchedule.Type.daily, new OnCallScheduleTime(null, "08:00:00", "16:00:00")));
        m_onCallRoleDao.save(day);
        m_onCallRoleDao.save(new OnCallRole("release", dev, boss));
        m_onCallRoleDao.flush();
        m_onCallRoleDao.clear();

        assertEquals(Arrays.asList("day", "night"), names(m_onCallRoleDao.findByMembershipGroup("ops")));
        assertEquals(List.of("release"), names(m_onCallRoleDao.findByMembershipGroup("dev")));
        assertEquals(Arrays.asList("night", "release"), names(m_onCallRoleDao.findBySupervisor("boss")));
        assertEquals(List.of("day"), names(m_onCallRoleDao.findBySupervisor("alice")));
        assertEquals("a role is listed once however many of its schedules the user has",
                List.of("night"), names(m_onCallRoleDao.findByScheduledUser("alice")));
        assertEquals(Arrays.asList("day", "night"), names(m_onCallRoleDao.findByScheduledUser("bob")));
        assertTrue(m_onCallRoleDao.findByScheduledUser("boss").isEmpty());
    }
}
