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
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
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
import org.opennms.netmgt.model.OnmsGroup;
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
    /** The name each cached Role had when it was loaded, by identity: WebRoleManagerImpl renames the object in place. */
    private final Map<Role, String> m_loadedRoleNames = new IdentityHashMap<>();
    private boolean m_renamedInPlace;
    private long m_usersVersion = -1;
    private long m_groupsVersion = -1;
    private long m_lastCheck;

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

    /** Loads the groups and on-call roles from the database now. */
    public synchronized void reload() {
        final Loaded loaded = m_transactions.execute(status -> {
            // read the versions first: a write that commits in between makes the next check reload again
            final long usersVersion = m_versionDao.getVersion(Domain.users);
            final long groupsVersion = m_versionDao.getVersion(Domain.groups);
            final List<UserGroup> groups = new ArrayList<>(m_groupDao.findAll());
            groups.sort(Comparator.comparing(UserGroup::getId));
            final List<OnCallRole> roles = new ArrayList<>(m_roleDao.findAll());
            roles.sort(Comparator.comparing(OnCallRole::getId));
            return new Loaded(UserMgmtJaxbMapper.toGroupinfo(groups, roles), usersVersion, groupsVersion);
        });
        load(loaded.m_groupinfo);
        m_loadedRoleNames.clear();
        loaded.m_groupinfo.getRoles().forEach(role -> m_loadedRoleNames.put(role, role.getName()));
        m_usersVersion = loaded.m_usersVersion;
        m_groupsVersion = loaded.m_groupsVersion;
        m_lastCheck = System.currentTimeMillis();
    }

    // GroupManager reads these without update(): GroupFactory loads in its constructor, this class on first use.

    @Override
    public synchronized String[] getRoleNames() {
        loadForRead();
        return super.getRoleNames();
    }

    @Override
    public synchronized Collection<Role> getRoles() {
        loadForRead();
        return super.getRoles();
    }

    @Override
    public synchronized Role getRole(final String roleName) {
        loadForRead();
        return super.getRole(roleName);
    }

    @Override
    public synchronized List<Group> findGroupsForUser(final String user) {
        loadForRead();
        return super.findGroupsForUser(user);
    }

    private void loadForRead() {
        try {
            update();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // GroupManager's saveGroup, saveRole and save do not load first. Load here, so a new manager has the data
    // and a manager whose last check is older than the interval sees other writers' changes before it saves.

    @Override
    public synchronized void saveGroup(final String name, final Group details) throws Exception {
        update();
        super.saveGroup(name, details);
    }

    @Override
    public synchronized void saveRole(final Role role) throws Exception {
        update();
        super.saveRole(role);
    }

    @Override
    public synchronized void save(final OnmsGroup group) throws Exception {
        update();
        super.save(group);
    }

    @Override
    protected synchronized void saveGroups(final Groupinfo groupinfo) throws Exception {
        final boolean loaded = m_groupsVersion >= 0;
        m_renamedInPlace = false;
        try {
            m_groupsVersion = UserMgmtTransactions.inTransaction(m_transactions, () -> {
                // a manager that never loaded holds only what the caller added, which an upsert can write safely
                if (m_versionDao.lockVersion(Domain.groups) != m_groupsVersion && loaded) {
                    throw new UserDataChangedException();
                }
                write(groupinfo);
                return m_versionDao.getVersion(Domain.groups);
            });
        } catch (final Exception e) {
            // the cache holds the change that was not saved: make it match the database again
            reload();
            throw e;
        }
        if (!loaded || m_renamedInPlace) {
            // GroupManager keys the cached roles by their old names
            reload();
            return;
        }
        // the database now has the cached roles under their current names
        groupinfo.getRoles().forEach(role -> m_loadedRoleNames.put(role, role.getName()));
    }

    /** Inserts or updates the groups and on-call roles by name. Never deletes: see the class comment. */
    private void write(final Groupinfo groupinfo) {
        final Map<String, UserAccount> users = byName(m_userDao.findAll(), UserAccount::getUsername);
        final Map<String, UserGroup> groups = byName(m_groupDao.findAll(), UserGroup::getName);
        for (final Group group : groupinfo.getGroups()) {
            final UserGroup entity = groups.computeIfAbsent(group.getName(), UserGroup::new);
            UserMgmtJaxbMapper.applyGroup(group, entity, users);
            m_groupDao.saveOrUpdate(entity);
        }
        final Map<String, OnCallRole> roles = byName(m_roleDao.findAll(), OnCallRole::getName);
        renameRolesChangedInPlace(groupinfo.getRoles(), roles);
        for (final Role role : groupinfo.getRoles()) {
            final OnCallRole entity = roles.computeIfAbsent(role.getName(), name -> new OnCallRole());
            UserMgmtJaxbMapper.applyRole(role, entity, groups, users);
            m_roleDao.saveOrUpdate(entity);
        }
        m_groupDao.flush();
    }

    /** Renames the row of a cached Role whose name was changed in place, so the upsert by name does not copy it. */
    private void renameRolesChangedInPlace(final List<Role> cached, final Map<String, OnCallRole> roles) {
        for (final Role role : cached) {
            final String loadedName = m_loadedRoleNames.get(role);
            if (loadedName == null || loadedName.equals(role.getName()) || !roles.containsKey(loadedName)) {
                continue;
            }
            if (roles.containsKey(role.getName())) {
                throw new IllegalArgumentException("On-call role \"" + role.getName() + "\" already exists.");
            }
            final OnCallRole entity = roles.remove(loadedName);
            entity.setName(role.getName());
            roles.put(role.getName(), entity);
            m_renamedInPlace = true;
        }
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

        private Loaded(final Groupinfo groupinfo, final long usersVersion, final long groupsVersion) {
            m_groupinfo = groupinfo;
            m_usersVersion = usersVersion;
            m_groupsVersion = groupsVersion;
        }
    }
}
