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
package org.opennms.core.password;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

/** The database path of the password CLI and the installer. Here because core/upgrade has the database setup. */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class DatabasePasswordsIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    private JdbcTemplate m_jdbc;
    private DataSource m_dataSource;

    @Override
    public void setTemporaryDatabase(final TemporaryDatabase database) {
        m_jdbc = database.getJdbcTemplate();
        m_dataSource = m_jdbc.getDataSource();
    }

    @Before
    public void setUp() {
        m_jdbc.update("INSERT INTO users (id, username, password_hash, password_salted, created_time) VALUES (nextval('users_id_seq'), 'admin', 'old', false, NOW())");
    }

    @Test
    public void testSetsTheSaltedHash() throws Exception {
        DatabasePasswords.setPassword(m_dataSource, "admin", "n3w");
        final String hash = m_jdbc.queryForObject("SELECT password_hash FROM users WHERE username = 'admin'", String.class);
        assertTrue(new StrongPasswordEncryptor().checkPassword("n3w", hash));
        assertTrue(m_jdbc.queryForObject("SELECT password_salted FROM users WHERE username = 'admin'", Boolean.class));
    }

    @Test
    public void testUnknownUserFails() {
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> DatabasePasswords.setPassword(m_dataSource, "ghost", "x"));
        assertEquals("User \"ghost\" does not exist.", e.getMessage());
    }

    @Test
    public void testExactUsernameMatch() {
        // the DOM path used contains(), so "dmi" matched "admin"; the database path matches exactly
        assertThrows(IllegalArgumentException.class, () -> DatabasePasswords.setPassword(m_dataSource, "dmi", "x"));
    }

    @Test
    public void testSetPasswordBumpsUserVersion() throws Exception {
        final int before = m_jdbc.queryForObject("SELECT version FROM users WHERE username = 'admin'", Integer.class);
        final long usersVersion = m_jdbc.queryForObject("SELECT version FROM usermgmt_version WHERE name = 'users'", Long.class);
        DatabasePasswords.setPassword(m_dataSource, "admin", "n3w");
        assertTrue(m_jdbc.queryForObject("SELECT version FROM users WHERE username = 'admin'", Integer.class) > before);
        assertTrue(m_jdbc.queryForObject("SELECT version FROM usermgmt_version WHERE name = 'users'", Long.class) > usersVersion);
    }

    /** Runs the real method of a proxied JDBC object. */
    @FunctionalInterface
    private interface Call {
        Object proceed() throws Throwable;
    }

    /** Runs in place of each method of the DataSource, its connections and their prepared statements. */
    @FunctionalInterface
    private interface Hook {
        Object invoke(String method, Call call) throws Throwable;
    }

    private DataSource intercepted(final Hook hook) {
        return proxy(DataSource.class, m_dataSource, hook);
    }

    private static <T> T proxy(final Class<T> type, final T target, final Hook hook) {
        return type.cast(Proxy.newProxyInstance(DatabasePasswordsIT.class.getClassLoader(), new Class<?>[] {type}, (p, method, args) -> {
            final Object result = hook.invoke(method.getName(), () -> {
                try {
                    return method.invoke(target, args);
                } catch (final InvocationTargetException e) {
                    throw e.getCause();
                }
            });
            if (result instanceof Connection) {
                return proxy(Connection.class, (Connection) result, hook);
            }
            if (result instanceof PreparedStatement) {
                return proxy(PreparedStatement.class, (PreparedStatement) result, hook);
            }
            return result;
        }));
    }

    private String passwordHash() {
        return m_jdbc.queryForObject("SELECT password_hash FROM users WHERE username = 'admin'", String.class);
    }

    @Test
    public void testFailedCommitKeepsTheExceptionAndTheRow() {
        final SQLException failure = new SQLException("commit failed");
        final DataSource dataSource = intercepted((method, call) -> {
            if ("commit".equals(method)) {
                throw failure;
            }
            return call.proceed();
        });
        assertSame(failure, assertThrows(SQLException.class, () -> DatabasePasswords.setPassword(dataSource, "admin", "n3w")));
        assertEquals("old", passwordHash());
    }

    @Test
    public void testFailedRollbackIsSuppressedByTheOriginalException() {
        final SQLException failure = new SQLException("commit failed");
        final SQLException rollbackFailure = new SQLException("rollback failed");
        final DataSource dataSource = intercepted((method, call) -> {
            if ("commit".equals(method)) {
                throw failure;
            }
            if ("rollback".equals(method)) {
                call.proceed();
                throw rollbackFailure;
            }
            return call.proceed();
        });
        final SQLException e = assertThrows(SQLException.class, () -> DatabasePasswords.setPassword(dataSource, "admin", "n3w"));
        assertSame(failure, e);
        assertArrayEquals(new Throwable[] {rollbackFailure}, e.getSuppressed());
        assertEquals("old", passwordHash());
    }

    @Test
    public void testErrorAfterTheUpdateIsRolledBack() {
        // restoring auto-commit commits an open transaction: it must not run before the rollback
        final Error failure = new Error("after the update");
        final DataSource dataSource = intercepted((method, call) -> {
            final Object result = call.proceed();
            if ("executeUpdate".equals(method)) {
                throw failure;
            }
            return result;
        });
        assertSame(failure, assertThrows(Error.class, () -> DatabasePasswords.setPassword(dataSource, "admin", "n3w")));
        assertEquals("old", passwordHash());
    }

    @Test
    public void testSetPasswordTakesTheUsersLockBeforeTheGroupsLock() throws Exception {
        // B holds the groups row; setPassword must take users and then wait for groups. While it waits, it must
        // hold the users row, so C cannot lock it. With the order groups-then-users, it would hold nothing and C would succeed.
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try (final Connection b = m_dataSource.getConnection()) {
            b.setAutoCommit(false);
            lockVersionRow(b, "groups");
            final Future<?> setPassword = executor.submit(() -> {
                DatabasePasswords.setPassword(m_dataSource, "admin", "n3w");
                return null;
            });
            final String sqlState;
            try {
                waitUntilAnotherBackendWaitsForALock(b);
                sqlState = sqlStateOfLockingUsers();
            } finally {
                b.commit(); // releases the groups row
            }
            setPassword.get(30, TimeUnit.SECONDS);
            assertEquals("users row while setPassword waits for groups", "55P03", sqlState);
        } finally {
            executor.shutdownNow();
        }
        assertTrue(new StrongPasswordEncryptor().checkPassword("n3w", passwordHash()));
    }

    private static void lockVersionRow(final Connection connection, final String name) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement("SELECT version FROM usermgmt_version WHERE name = ? FOR UPDATE")) {
            statement.setString(1, name);
            statement.executeQuery().close();
        }
    }

    /** Tries to lock the users row on a new connection and returns the SQL state of the failure. */
    private String sqlStateOfLockingUsers() throws SQLException {
        try (final Connection c = m_dataSource.getConnection()) {
            c.setAutoCommit(false);
            try (final Statement statement = c.createStatement()) {
                statement.execute("SET LOCAL lock_timeout = '500ms'");
                lockVersionRow(c, "users");
                return "not blocked";
            } catch (final SQLException e) {
                return e.getSQLState();
            } finally {
                c.rollback();
            }
        }
    }

    /** Polls pg_stat_activity, on the given connection, until another backend waits for a lock. */
    private static void waitUntilAnotherBackendWaitsForALock(final Connection connection) throws Exception {
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            try (final Statement statement = connection.createStatement()) {
                // pg_stat_activity is a snapshot per transaction: drop it, or the poll never sees a change
                statement.execute("SELECT pg_stat_clear_snapshot()");
                try (final ResultSet rs = statement.executeQuery("SELECT count(*) FROM pg_stat_activity"
                         + " WHERE datname = current_database() AND pid <> pg_backend_pid() AND wait_event_type = 'Lock'")) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        return;
                    }
                }
            }
            Thread.sleep(20);
        }
        fail("no backend started waiting for a lock within 10 s");
    }
}
