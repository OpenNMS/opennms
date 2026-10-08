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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
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
import org.opennms.netmgt.model.usermgmt.UserAccount;
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
public class UserGroupDaoIT implements InitializingBean {

    @Autowired
    private SessionFactory m_sessionFactory;

    @Autowired
    private UserGroupDao m_groupDao;

    @Autowired
    private UserAccountDao m_userDao;

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

    private UserGroup createGroup(final String name, final UserAccount... members) {
        final UserGroup group = new UserGroup(name);
        group.getMembers().addAll(Arrays.asList(members));
        m_groupDao.save(group);
        return group;
    }

    private static List<String> usernames(final List<UserAccount> users) {
        return users.stream().map(UserAccount::getUsername).collect(Collectors.toList());
    }

    private static List<String> names(final List<UserGroup> groups) {
        return groups.stream().map(UserGroup::getName).collect(Collectors.toList());
    }

    @Test
    @Transactional
    public void testRoundTrip() {
        final UserAccount alice = createUser("alice");
        final UserAccount bob = createUser("bob");
        final UserAccount carol = createUser("carol");
        final UserGroup group = createGroup("ops", carol, alice, bob);
        group.setDefaultMap("network");
        group.setComments("the operators");
        group.getDutySchedules().addAll(Arrays.asList("MoTuWeThFr800-1700", "SaSu0-2359"));
        m_groupDao.flush();
        m_groupDao.clear();

        final UserGroup loaded = m_groupDao.findByName("ops");
        assertNotNull(loaded);
        assertEquals("network", loaded.getDefaultMap());
        assertEquals("the operators", loaded.getComments());
        assertEquals("members keep their order", Arrays.asList("carol", "alice", "bob"), usernames(loaded.getMembers()));
        assertEquals(Arrays.asList("MoTuWeThFr800-1700", "SaSu0-2359"), loaded.getDutySchedules());
        assertTrue(loaded.hasMember("alice"));
        assertNotNull(loaded.getCreatedTime());
        assertNotNull(loaded.getLastModified());
        assertNull(m_groupDao.findByName("OPS"));
    }

    /** Reordering rewrites user_id position by position; the deferred unique constraint lets that through. */
    @Test
    @Transactional
    public void testReorderMembers() {
        final UserAccount alice = createUser("alice");
        final UserAccount bob = createUser("bob");
        final UserAccount carol = createUser("carol");
        createGroup("ops", alice, bob, carol);
        m_groupDao.flush();
        m_groupDao.clear();

        UserGroup loaded = m_groupDao.findByName("ops");
        Collections.reverse(loaded.getMembers());
        UserMgmtSql.checkDeferredConstraints(m_sessionFactory);
        m_groupDao.clear();

        loaded = m_groupDao.findByName("ops");
        assertEquals(Arrays.asList("carol", "bob", "alice"), usernames(loaded.getMembers()));

        loaded.getMembers().remove(1);
        loaded.getMembers().add(0, m_userDao.findByUsername("bob"));
        UserMgmtSql.checkDeferredConstraints(m_sessionFactory);
        m_groupDao.clear();

        assertEquals(Arrays.asList("bob", "carol", "alice"), usernames(m_groupDao.findByName("ops").getMembers()));
    }

    @Test
    @Transactional
    public void testConstraints() {
        final UserAccount alice = createUser("alice");
        final UserGroup group = createGroup("ops", alice);

        UserMgmtSql.assertRejected(m_sessionFactory, UserMgmtSql.UNIQUE_VIOLATION,
                "INSERT INTO groups (id, name) VALUES (nextval('groups_id_seq'), 'ops')");
        UserMgmtSql.assertRejected(m_sessionFactory, UserMgmtSql.UNIQUE_VIOLATION,
                "INSERT INTO group_members (group_id, position, user_id) VALUES (?, 1, ?)", group.getId(), alice.getId());
        UserMgmtSql.assertRejected(m_sessionFactory, UserMgmtSql.FOREIGN_KEY_VIOLATION,
                "INSERT INTO group_members (group_id, position, user_id) VALUES (?, 1, -1)", group.getId());
    }

    @Test
    @Transactional
    public void testFindGroupsForUser() {
        final UserAccount alice = createUser("alice");
        final UserAccount bob = createUser("bob");
        createGroup("zeta", alice, bob);
        createGroup("alpha", alice);
        createGroup("empty");
        m_groupDao.flush();
        m_groupDao.clear();

        assertEquals(Arrays.asList("alpha", "zeta"), names(m_groupDao.findGroupsForUser("alice")));
        assertEquals(List.of("zeta"), names(m_groupDao.findGroupsForUser("bob")));
        assertTrue(m_groupDao.findGroupsForUser("nobody").isEmpty());
    }

    @Test
    @Transactional
    public void testDeleteGroupKeepsItsUsers() {
        final UserAccount alice = createUser("alice");
        final UserGroup group = createGroup("ops", alice);
        group.getDutySchedules().add("Mo800-1700");
        m_groupDao.flush();
        final Integer groupId = group.getId();

        m_groupDao.delete(group);
        m_groupDao.flush();
        m_groupDao.clear();

        assertNull(m_groupDao.findByName("ops"));
        assertNotNull(m_userDao.findByUsername("alice"));
        assertEquals(0, UserMgmtSql.count(m_sessionFactory, "SELECT COUNT(*) FROM group_members WHERE group_id = ?", groupId));
        assertEquals(0, UserMgmtSql.count(m_sessionFactory, "SELECT COUNT(*) FROM group_duty_schedules WHERE group_id = ?", groupId));
    }

    /** An on-call role draws its members from a group: the database refuses to delete it. groups.xml allowed it. */
    @Test
    @Transactional
    public void testDeletingAMembershipGroupIsRefused() {
        final UserAccount supervisor = createUser("boss");
        final UserGroup group = createGroup("ops", supervisor);
        m_onCallRoleDao.save(new OnCallRole("night", group, supervisor));
        m_groupDao.flush();

        assertThrows(DataIntegrityViolationException.class, () -> {
            m_groupDao.delete(group);
            m_groupDao.flush();
        });
    }
}
