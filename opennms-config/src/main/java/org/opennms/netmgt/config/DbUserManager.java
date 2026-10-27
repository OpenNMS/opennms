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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao.Domain;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.SecurityRole;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * UserManager on the usermgmt tables. Caches the users like UserFactory does and reloads them when the "users" row
 * of usermgmt_version changes; {@link #getLastModified()} returns that version, so the Spring Security user cache
 * follows it.
 */
public class DbUserManager extends UserManager {
    private static final long RELOAD_CHECK_INTERVAL_MS = 1000;

    private final DbGroupManager m_dbGroupManager;
    private final UserAccountDao m_userDao;
    private final SecurityRoleDao m_securityRoleDao;
    private final OnCallRoleDao m_roleDao;
    private final UserMgmtVersionDao m_versionDao;
    private final TransactionOperations m_transactions;

    private volatile long m_version = -1;
    private final AtomicLong m_lastCheck = new AtomicLong(0);

    public DbUserManager(final DbGroupManager groupManager, final UserAccountDao userDao, final SecurityRoleDao securityRoleDao,
                         final OnCallRoleDao roleDao, final UserMgmtVersionDao versionDao, final PlatformTransactionManager transactionManager) {
        super(groupManager);
        m_dbGroupManager = groupManager;
        m_userDao = Objects.requireNonNull(userDao);
        m_securityRoleDao = Objects.requireNonNull(securityRoleDao);
        m_roleDao = Objects.requireNonNull(roleDao);
        m_versionDao = Objects.requireNonNull(versionDao);
        // a change is committed on its own, like the write of the XML file: a caller's transaction must not roll it
        // back, hold its locks while this manager waits for its own lock, or show its unflushed state to a reload
        final TransactionTemplate transactions = new TransactionTemplate(Objects.requireNonNull(transactionManager));
        transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        m_transactions = transactions;
    }

    @Override
    protected void doUpdate() {
        if (isUpdateNeeded()) {
            reload();
        }
    }

    @Override
    public boolean isUpdateNeeded() {
        if (m_version < 0) {
            return true;
        }
        final long now = System.currentTimeMillis();
        final long last = m_lastCheck.get();
        if (now < last + RELOAD_CHECK_INTERVAL_MS || !m_lastCheck.compareAndSet(last, now)) {
            return false;
        }
        return m_transactions.execute(status -> m_versionDao.getVersion(Domain.users)) != m_version;
    }

    /** Loads the users from the database now. */
    @Override
    public void reload() {
        final Loaded loaded = m_transactions.execute(status -> {
            // read the version first: a write that commits in between makes the next check reload again
            final long version = m_versionDao.getVersion(Domain.users);
            return new Loaded(UserMgmtJaxbMapper.toUserinfo(m_userDao.findAllOrderByUsername()), version);
        });
        writeLock().lock();
        try {
            load(loaded.m_userinfo);
            m_version = loaded.m_version;
            m_lastCheck.set(System.currentTimeMillis());
        } finally {
            writeLock().unlock();
        }
    }

    @Override
    public long getLastModified() {
        return m_version;
    }

    @Override
    public long getFileSize() {
        return 0;
    }

    @Override
    protected void saveXML(final String writerString) {
        throw new UnsupportedOperationException("DbUserManager does not write users.xml");
    }

    /** Inserts or updates all cached users by name. Never deletes: a missing name can be another writer's new user. */
    @Override
    protected void saveUsers(final Userinfo userinfo) throws Exception {
        final long cachedVersion = m_version;
        try {
            m_version = UserMgmtTransactions.inTransaction(m_transactions, () -> {
                m_versionDao.lockAll();
                if (m_versionDao.getVersion(Domain.users) != cachedVersion) {
                    throw new UserDataChangedException();
                }
                write(userinfo.getUsers(), false);
                return m_versionDao.getVersion(Domain.users);
            });
        } catch (final Exception e) {
            // the cache holds the change that was not saved: make it match the database again
            reload();
            throw e;
        }
    }

    /** Unlike the other saves, replaces all users: the ones that are not in the list are deleted. */
    @Override
    public void saveUsers(final Collection<User> users) throws Exception {
        update();
        try {
            UserMgmtTransactions.inTransaction(m_transactions, () -> {
                m_versionDao.lockAll();
                write(users, true);
                return null;
            });
        } finally {
            reload();
            m_dbGroupManager.reload();
        }
    }

    private void write(final Collection<User> users, final boolean deleteOthers) {
        final Map<String, SecurityRole> roles = new HashMap<>();
        m_securityRoleDao.findAll().forEach(r -> roles.put(r.getName(), r));
        final Map<String, UserAccount> existing = new HashMap<>();
        m_userDao.findAll().forEach(u -> existing.put(u.getUsername(), u));
        final Set<String> written = new HashSet<>();
        for (final User user : users) {
            final UserAccount entity = existing.computeIfAbsent(user.getUserId(), UserAccount::new);
            UserMgmtJaxbMapper.applyUser(user, entity, roles);
            m_userDao.saveOrUpdate(entity);
            written.add(user.getUserId());
        }
        if (deleteOthers) {
            existing.values().stream().filter(u -> !written.contains(u.getUsername())).forEach(m_userDao::delete);
        }
        m_userDao.flush();
    }

    /** Changes the name in place: group members, supervisors and schedules refer to the user's id and follow. */
    @Override
    public void renameUser(final String oldName, final String newName) throws Exception {
        update();
        try {
            UserMgmtTransactions.inTransaction(m_transactions, () -> {
                // a rename fires triggers that bump the groups version: take both locks in order first
                m_versionDao.lockAll();
                final UserAccount user = m_userDao.findByUsername(oldName);
                if (user == null) {
                    throw new Exception("UserFactory:rename the old user name " + oldName + " is not found");
                }
                if (m_userDao.findByUsername(newName) != null) {
                    throw new Exception("UserFactory: cannot rename user " + oldName + ". An user with the given name " + newName + " already exists");
                }
                user.setUsername(UserMgmtJaxbMapper.checkLength("User \"" + newName + "\"", "user name", newName, UserMgmtJaxbMapper.USERNAME_LENGTH));
                m_userDao.update(user);
                m_userDao.flush();
                return null;
            });
        } finally {
            reload();
            m_dbGroupManager.reload();
        }
    }

    /** Deletes the user with its group memberships and on-call schedules; a supervisor cannot be deleted. */
    @Override
    public void deleteUser(final String name) throws Exception {
        update();
        try {
            UserMgmtTransactions.inTransaction(m_transactions, () -> {
                // the delete cascades to memberships and schedules, which bumps the groups version: take both locks in order first
                m_versionDao.lockAll();
                final UserAccount user = m_userDao.findByUsername(name);
                if (user == null) {
                    throw new Exception("UserFactory:delete The old user name " + name + " is not found");
                }
                final List<OnCallRole> supervised = m_roleDao.findBySupervisor(name);
                if (!supervised.isEmpty()) {
                    throw new IllegalStateException("User \"" + name + "\" is the supervisor of the on-call role \""
                            + supervised.get(0).getName() + "\". Change the supervisor first.");
                }
                m_userDao.delete(user);
                m_userDao.flush();
                return null;
            });
        } finally {
            reload();
            m_dbGroupManager.reload();
        }
    }

    private static final class Loaded {
        private final Userinfo m_userinfo;
        private final long m_version;

        private Loaded(final Userinfo userinfo, final long version) {
            m_userinfo = userinfo;
            m_version = version;
        }
    }
}
