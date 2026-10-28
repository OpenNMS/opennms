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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Groupinfo;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao.Domain;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * GroupManager on the usermgmt tables. Caches the groups and on-call roles like GroupFactory does, and reloads them
 * when usermgmt_version changes. Group member and supervisor names come from the users, so both version rows are
 * followed; a save only checks the "groups" row, so a user change does not make a group save fail.
 */
public class DbGroupManager extends GroupManager {
    private static final long RELOAD_CHECK_INTERVAL_MS = 1000;

    private final UserAccountDao m_userDao;
    private final UserGroupDao m_groupDao;
    private final OnCallRoleDao m_roleDao;
    private final UserMgmtVersionDao m_versionDao;
    private final TransactionOperations m_transactions;

    // guarded by this
    /** The user id behind each member, supervisor and schedule name in the cache, as loaded. */
    private Map<String, Integer> m_loadedUserIds = new HashMap<>();
    private long m_usersVersion = -1;
    private long m_groupsVersion = -1;
    /** When the versions were last checked; {@link #expire()} sets it to 0 without taking the monitor. */
    private volatile long m_lastCheck;

    public DbGroupManager(final UserAccountDao userDao, final UserGroupDao groupDao, final OnCallRoleDao roleDao,
                          final UserMgmtVersionDao versionDao, final PlatformTransactionManager transactionManager) {
        m_userDao = Objects.requireNonNull(userDao);
        m_groupDao = Objects.requireNonNull(groupDao);
        m_roleDao = Objects.requireNonNull(roleDao);
        m_versionDao = Objects.requireNonNull(versionDao);
        // a change is committed on its own, like the write of the XML file: a caller's transaction must not roll it
        // back, hold its locks while this manager waits for its own lock, or show its unflushed state to a reload
        final TransactionTemplate transactions = new TransactionTemplate(Objects.requireNonNull(transactionManager));
        transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        m_transactions = transactions;
    }

    @Override
    public synchronized void update() throws IOException {
        if (m_groupsVersion >= 0) {
            final long now = System.currentTimeMillis();
            if (now < m_lastCheck + RELOAD_CHECK_INTERVAL_MS) {
                return;
            }
            m_lastCheck = now;
            final long[] versions = m_transactions.execute(status -> new long[] {
                    m_versionDao.getVersion(Domain.users), m_versionDao.getVersion(Domain.groups) });
            if (versions[0] == m_usersVersion && versions[1] == m_groupsVersion) {
                return;
            }
        }
        reload();
    }

    /**
     * Makes the next read check the versions, without waiting for the check interval; reads nothing and takes no lock.
     * Called after a write that bypassed this manager commits, so the cache reloads once, when it is next read.
     */
    public void expire() {
        m_lastCheck = 0;
    }

    /** Loads the groups and on-call roles from the database now. */
    public synchronized void reload() {
        // record the check before reading: an expire() that comes during the reload must not be overwritten
        m_lastCheck = System.currentTimeMillis();
        final Loaded loaded = m_transactions.execute(status -> {
            // read the versions first: a write that commits in between makes the next check reload again
            final long usersVersion = m_versionDao.getVersion(Domain.users);
            final long groupsVersion = m_versionDao.getVersion(Domain.groups);
            final List<UserGroup> groups = new ArrayList<>(m_groupDao.findAll());
            groups.sort(Comparator.comparing(UserGroup::getId));
            final List<OnCallRole> roles = new ArrayList<>(m_roleDao.findAll());
            roles.sort(Comparator.comparing(OnCallRole::getId));
            final Map<String, Integer> userIds = new HashMap<>();
            groups.forEach(g -> g.getMembers().forEach(u -> userIds.put(u.getUsername(), u.getId())));
            roles.forEach(r -> {
                userIds.put(r.getSupervisor().getUsername(), r.getSupervisor().getId());
                r.getSchedules().forEach(sched -> userIds.put(sched.getUser().getUsername(), sched.getUser().getId()));
            });
            return new Loaded(UserMgmtJaxbMapper.toGroupinfo(groups, roles), usersVersion, groupsVersion, userIds);
        });
        load(loaded.m_groupinfo);
        m_loadedUserIds = loaded.m_userIds;
        m_usersVersion = loaded.m_usersVersion;
        m_groupsVersion = loaded.m_groupsVersion;
    }

    @Override
    protected synchronized void saveGroups(final Groupinfo groupinfo) throws Exception {
        final boolean loaded = m_groupsVersion >= 0;
        final Map<String, Integer> writtenUserIds = new HashMap<>();
        try {
            m_groupsVersion = UserMgmtTransactions.inTransaction(m_transactions, () -> {
                // a manager that never loaded holds only what the caller added, which an upsert can write safely
                m_versionDao.lockAll();
                if (m_versionDao.getVersion(Domain.groups) != m_groupsVersion && loaded) {
                    throw new UserDataChangedException();
                }
                writtenUserIds.putAll(write(groupinfo));
                return m_versionDao.getVersion(Domain.groups);
            });
            m_loadedUserIds = writtenUserIds;
        } catch (final Exception e) {
            // the cache holds the change that was not saved: make it match the database again
            reload();
            throw e;
        }
        if (!loaded) {
            reload();
        }
    }

    /**
     * Inserts or updates the groups and on-call roles by name. Never deletes: see the class comment.
     *
     * @return the user id behind each member, supervisor and schedule name that was written
     */
    private Map<String, Integer> write(final Groupinfo groupinfo) {
        final Map<String, UserAccount> users = byName(m_userDao.findAll(), UserAccount::getUsername);
        final Map<String, Integer> userIds = checkCachedUsers(groupinfo, users);
        final Map<String, UserGroup> groups = byName(m_groupDao.findAll(), UserGroup::getName);
        for (final Group group : groupinfo.getGroups()) {
            final UserGroup entity = groups.computeIfAbsent(group.getName(), UserGroup::new);
            UserMgmtJaxbMapper.applyGroup(group, entity, users);
            m_groupDao.saveOrUpdate(entity);
        }
        final Map<String, OnCallRole> roles = byName(m_roleDao.findAll(), OnCallRole::getName);
        for (final Role role : groupinfo.getRoles()) {
            final OnCallRole entity = roles.computeIfAbsent(role.getName(), name -> new OnCallRole());
            UserMgmtJaxbMapper.applyRole(role, entity, groups, users);
            m_roleDao.saveOrUpdate(entity);
        }
        m_groupDao.flush();
        return userIds;
    }

    /**
     * The save checks only the groups version, so a user renamed by another process since the last load is not
     * noticed by it: the cached name would then match no user, or a new user who now has the old name. Fail
     * instead of binding a membership, supervisor or schedule to the wrong person.
     */
    private Map<String, Integer> checkCachedUsers(final Groupinfo groupinfo, final Map<String, UserAccount> users) {
        final List<String> names = new ArrayList<>();
        groupinfo.getGroups().forEach(g -> names.addAll(g.getUsers()));
        groupinfo.getRoles().forEach(r -> {
            names.add(r.getSupervisor());
            r.getSchedules().forEach(sched -> names.add(sched.getName()));
        });
        for (final String name : names) {
            final Integer loadedId = m_loadedUserIds.get(name);
            final UserAccount user = users.get(name);
            if (loadedId != null && (user == null || !loadedId.equals(user.getId()))) {
                throw new UserDataChangedException();
            }
        }
        final Map<String, Integer> userIds = new HashMap<>();
        for (final String name : names) {
            final UserAccount user = users.get(name);
            if (user != null) {
                userIds.put(name, user.getId());
            }
        }
        return userIds;
    }

    @Override
    protected void saveXml(final String data) {
        throw new UnsupportedOperationException("DbGroupManager does not write groups.xml");
    }

    @Override
    public synchronized void renameGroup(final String oldName, final String newName) throws Exception {
        change(() -> {
            final UserGroup group = m_groupDao.findByName(oldName);
            if (group == null) {
                throw new Exception("GroupFactory.renameGroup: Group doesn't exist: " + oldName);
            }
            if (m_groupDao.findByName(newName) != null) {
                throw new Exception("Group \"" + newName + "\" already exists.");
            }
            group.setName(UserMgmtJaxbMapper.checkLength("Group \"" + newName + "\"", "name", newName, UserMgmtJaxbMapper.GROUP_NAME_LENGTH));
            m_groupDao.update(group);
        });
    }

    @Override
    public synchronized void renameRole(final String oldName, final String newName) throws Exception {
        change(() -> {
            final OnCallRole role = m_roleDao.findByName(oldName);
            if (role == null) {
                throw new Exception("GroupFactory.renameRole: Role doesn't exist: " + oldName);
            }
            if (m_roleDao.findByName(newName) != null) {
                throw new Exception("On-call role \"" + newName + "\" already exists.");
            }
            role.setName(UserMgmtJaxbMapper.checkLength("On-call role \"" + newName + "\"", "name", newName, UserMgmtJaxbMapper.ONCALL_ROLE_NAME_LENGTH));
            m_roleDao.update(role);
        });
    }

    @Override
    public synchronized void deleteGroup(final String name) throws Exception {
        if (name == null || name.isEmpty()) {
            throw new Exception("GroupFactory:delete Invalid user group:" + name);
        }
        change(() -> {
            final UserGroup group = m_groupDao.findByName(name);
            if (group == null) {
                throw new Exception("GroupFactory:delete Group doesnt exist:" + name);
            }
            final List<OnCallRole> roles = m_roleDao.findByMembershipGroup(name);
            if (!roles.isEmpty()) {
                throw new IllegalStateException("Group \"" + name + "\" is the membership group of the on-call role \""
                        + roles.get(0).getName() + "\". Change the membership group first.");
            }
            m_groupDao.delete(group);
        });
    }

    @Override
    public synchronized void deleteRole(final String name) throws Exception {
        if (name == null || name.isEmpty()) {
            throw new Exception("GroupFactory:deleteRole Invalid role name: " + name);
        }
        change(() -> {
            final OnCallRole role = m_roleDao.findByName(name);
            if (role == null) {
                throw new Exception("GroupFacotry:deleteRole Role doesn't exist: " + name);
            }
            m_roleDao.delete(role);
        });
    }

    /** Takes the user out of all groups and on-call schedules. The user itself stays, as in GroupManager. */
    @Override
    public synchronized void deleteUser(final String name) throws Exception {
        if (name == null || name.isEmpty()) {
            throw new Exception("GroupFactory:delete Invalid user name:" + name);
        }
        change(() -> {
            for (final UserGroup group : m_groupDao.findGroupsForUser(name)) {
                group.getMembers().removeIf(m -> name.equals(m.getUsername()));
            }
            for (final OnCallRole role : m_roleDao.findByScheduledUser(name)) {
                role.getSchedules().removeIf(s -> name.equals(s.getUser().getUsername()));
            }
        });
    }

    /** Group members and schedules refer to the user's id, so the database already has the new name. */
    @Override
    public synchronized void renameUser(final String oldName, final String newName) {
        reload();
    }

    @Override
    public void setGroups(final Map<String, Group> groups) {
        throw new UnsupportedOperationException("DbGroupManager does not replace all groups; save or delete them one by one");
    }

    private void change(final ChangeWork work) throws Exception {
        try {
            UserMgmtTransactions.inTransaction(m_transactions, () -> {
                m_versionDao.lockAll();
                work.run();
                m_groupDao.flush();
                return null;
            });
        } finally {
            reload();
        }
    }

    @FunctionalInterface
    private interface ChangeWork {
        void run() throws Exception;
    }

    private static <T> Map<String, T> byName(final Collection<T> entities, final Function<T, String> name) {
        final Map<String, T> map = new HashMap<>();
        entities.forEach(e -> map.put(name.apply(e), e));
        return map;
    }

    private static final class Loaded {
        private final Groupinfo m_groupinfo;
        private final long m_usersVersion;
        private final long m_groupsVersion;
        private final Map<String, Integer> m_userIds;

        private Loaded(final Groupinfo groupinfo, final long usersVersion, final long groupsVersion, final Map<String, Integer> userIds) {
            m_groupinfo = groupinfo;
            m_usersVersion = usersVersion;
            m_groupsVersion = groupsVersion;
            m_userIds = userIds;
        }
    }
}
