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
package org.opennms.upgrade.implementations;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.opennms.upgrade.support.UpgradeStatus;

/**
 * Adds the default users (admin, rtc) and the Admin group to a database without users, with the values of the
 * shipped users.xml and groups.xml. Runs after UsersGroupsXmlMigratorOffline, on every install: the empty-table
 * check makes it safe, and a run-once marker in etc would block a new database used with an old etc.
 *
 * It does not seed while the migration of users.xml is pending (users.xml exists and the migrator has not run
 * successfully): Upgrade continues after a failed task, and a seeded admin would make the migrator's next run copy
 * nothing, so the users of the file would be lost.
 */
public class DefaultUsersSeedOffline extends AbstractOnmsUpgrade {

    // the hashes of the shipped users.xml: admin/admin and rtc/rtc
    private static final String ADMIN_HASH = "gU2wmSW7k9v1xg4/MrAsaI+VyddBAhJJt4zPX5SGG0BK+qiASGnJsqM8JOug/aEL";
    private static final String RTC_HASH = "sHMy+HycWKGJC/uUMF0IGlXUXP1KhcqD0GEchFlvYTw40jT9r+zMxOb3F+phWNzX";

    public DefaultUsersSeedOffline() throws OnmsUpgradeException {
        super();
    }

    @Override
    public int getOrder() {
        // after UsersGroupsXmlMigratorOffline (18)
        return 19;
    }

    @Override
    public String getDescription() {
        return "Adds the default admin and rtc users to a database that has no users: NMS-20443";
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    @Override
    public boolean runOnlyOnce() {
        return false;
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        try (final Connection connection = DataSourceFactory.getInstance().getConnection()) {
            if (count(connection, "SELECT COUNT(*) FROM users") > 0) {
                log("The database already contains users, so no default users are added\n");
                return;
            }
            final File usersXml = new File(getHomeDirectory(), "etc" + File.separator + "users.xml");
            if (usersXml.exists() && !migratorComplete()) {
                log("No default users are added: %s exists, and its migration (UsersGroupsXmlMigratorOffline) is not complete. See the messages of that task.\n", usersXml);
                return;
            }
            connection.setAutoCommit(false);
            try {
                final int admin = insertUser(connection, "admin", "Administrator", "Default administrator, do not delete", ADMIN_HASH, "ROLE_ADMIN");
                insertUser(connection, "rtc", "RTC", "RTC user, do not delete", RTC_HASH, "ROLE_RTC");
                final int group = insertReturningId(connection,
                        "INSERT INTO groups (id, name, comments, created_time, last_modified) VALUES (nextval('groups_id_seq'), ?, ?, NOW(), NOW()) RETURNING id",
                        "Admin", "The administrators");
                update(connection, "INSERT INTO group_members (group_id, position, user_id) VALUES (?, 0, ?)", group, admin);
                connection.commit();
            } catch (final SQLException e) {
                try {
                    connection.rollback();
                } catch (final SQLException rollbackFailure) {
                    e.addSuppressed(rollbackFailure);
                }
                throw e;
            }
            connection.setAutoCommit(true);
            log("Added the default users admin and rtc, and the group Admin\n");
        } catch (final SQLException e) {
            throw new OnmsUpgradeException("Cannot add the default users: " + e.getMessage(), e);
        }
    }

    private boolean migratorComplete() throws OnmsUpgradeException {
        final File statusFile = new File(getHomeDirectory(), "etc" + File.separator + UpgradeStatus.STATUS_FILE);
        if (!statusFile.exists()) {
            return false;
        }
        final Properties status = new Properties();
        try (final Reader reader = Files.newBufferedReader(statusFile.toPath(), StandardCharsets.ISO_8859_1)) {
            status.load(reader);
        } catch (final IOException e) {
            throw new OnmsUpgradeException("Cannot read " + statusFile + ": " + e.getMessage(), e);
        }
        return status.containsKey(UsersGroupsXmlMigratorOffline.class.getSimpleName());
    }

    private static int insertUser(final Connection connection, final String username, final String fullName,
                                  final String comments, final String hash, final String role) throws SQLException {
        final int id = insertReturningId(connection,
                "INSERT INTO users (id, username, full_name, comments, password_hash, password_salted, created_time, last_modified) "
                        + "VALUES (nextval('users_id_seq'), ?, ?, ?, ?, true, NOW(), NOW()) RETURNING id",
                username, fullName, comments, hash);
        update(connection, "INSERT INTO user_security_roles (user_id, role_id) SELECT ?, id FROM security_roles WHERE name = ?", id, role);
        return id;
    }

    private static int count(final Connection connection, final String sql) throws SQLException {
        try (final Statement statement = connection.createStatement(); final ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static int insertReturningId(final Connection connection, final String sql, final Object... args) throws SQLException {
        try (final PreparedStatement statement = prepare(connection, sql, args); final ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static void update(final Connection connection, final String sql, final Object... args) throws SQLException {
        try (final PreparedStatement statement = prepare(connection, sql, args)) {
            statement.executeUpdate();
        }
    }

    private static PreparedStatement prepare(final Connection connection, final String sql, final Object... args) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
        return statement;
    }

    @Override
    public void postExecute() throws OnmsUpgradeException {
    }

    @Override
    public void rollback() throws OnmsUpgradeException {
    }
}
