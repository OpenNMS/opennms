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

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.config.groups.Groupinfo;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * Copies users.xml, groups.xml and security-roles.properties into the user management tables (37.0.0).
 *
 * <p>The task copies only into a database without user data: if there is a user, a group, an on-call
 * role or a custom security role, it copies nothing and logs why. It runs on every install, but once per
 * database: it sets the usermgmt_initialized marker when it copied a users.xml (also one without users) or
 * found data already there, and it skips a database with the marker. A run-once marker in etc would skip a
 * restored or new database on an upgraded host, and the default-users seed would then add admin/admin
 * instead of the users of users.xml. Thus, the copy never changes existing data.</p>
 *
 * <p>Everything is written in one transaction. Entries that the tables cannot hold are skipped with a message: unknown security roles (which login drops today as
 * well), group members and schedule users that do not exist, on-call roles whose membership group or
 * supervisor does not exist, a second contact of the same type, unknown contact and schedule types, and
 * values longer than their column. Names are matched exactly, without trimming, as OpenNMS matches them
 * today. The XSDs normally reject unknown types, but JaxbUtils reads a file without validation when it
 * cannot find the XSD.</p>
 *
 * <p>The files stay in {@code etc}, because OpenNMS still reads them; copies go to {@code etc_archive}
 * before the data is written.</p>
 */
public class UsersGroupsXmlMigratorOffline extends AbstractOnmsUpgrade {

    private static final String USERS_FILE_NAME = "users.xml";
    private static final String GROUPS_FILE_NAME = "groups.xml";
    private static final String SECURITY_ROLES_FILE_NAME = "security-roles.properties";

    private static final List<String> TABLES = Arrays.asList(
            "security_roles", "users", "user_contacts", "user_duty_schedules", "user_security_roles",
            "groups", "group_members", "group_duty_schedules",
            "oncall_roles", "oncall_schedules", "oncall_schedule_times", "usermgmt_initialized");

    public UsersGroupsXmlMigratorOffline() throws OnmsUpgradeException {
        super();
    }

    @Override
    public int getOrder() {
        // after MagicUsersMigratorOffline (12), which edits users.xml
        return 18;
    }

    @Override
    public String getDescription() {
        return "Copies users.xml, groups.xml and security-roles.properties into the database: NMS-20431";
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    @Override
    public boolean runOnlyOnce() {
        // once per database: see the class comment
        return false;
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
        try (final Connection connection = DataSourceFactory.getInstance().getConnection();
             final PreparedStatement statement = connection.prepareStatement("SELECT to_regclass(?) IS NOT NULL")) {
            for (final String table : TABLES) {
                statement.setString(1, table);
                try (final ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    if (!rs.getBoolean(1)) {
                        throw new OnmsUpgradeException("The table '" + table + "' does not exist");
                    }
                }
            }
        } catch (final SQLException e) {
            throw new OnmsUpgradeException("Can't check the user management tables: " + e.getMessage(), e);
        }
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        try (final Connection connection = DataSourceFactory.getInstance().getConnection()) {
            if (UserMgmtInitialized.isSet(connection)) {
                log("The user data of this database was initialized before (see the table usermgmt_initialized), so nothing is copied from the files\n");
                return;
            }
            final String existingData = existingData(connection);
            if (existingData != null) {
                log("The database already contains user data (%s), so nothing is copied from the files\n", existingData);
                UserMgmtInitialized.set(connection, getId());
                return;
            }

            final List<String> customRoles = readSecurityRoles(etcFile(SECURITY_ROLES_FILE_NAME));
            final Userinfo userinfo = read(Userinfo.class, etcFile(USERS_FILE_NAME));
            final Groupinfo groupinfo = read(Groupinfo.class, etcFile(GROUPS_FILE_NAME));
            // before the data, not in postExecute(): Upgrade marks the task as done before postExecute(),
            // so a failure there would never be retried, and after the data the next run copies nothing
            archive();

            connection.setAutoCommit(false);
            final UsersGroupsXmlImport migration = new UsersGroupsXmlImport(connection, UsersGroupsXmlImport.Existing.NONE_EXPECTED, true, line -> log("%s\n", line));
            try {
                migration.securityRoles(customRoles);
                migration.users(userinfo == null ? Collections.emptyList() : userinfo.getUsers());
                migration.groups(groupinfo == null ? Collections.emptyList() : groupinfo.getGroups());
                migration.onCallRoles(groupinfo == null ? Collections.emptyList() : groupinfo.getRoles());
                if (userinfo != null) {
                    // also with no users: the default-users seed must not add admin/admin to this database later.
                    // Without a users.xml (a new installation once the files are gone), the seed adds them.
                    UserMgmtInitialized.set(connection, getId());
                }
                connection.commit();
            } catch (final SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (final SQLException rollbackException) {
                    // keep the cause of the failure; the connection may be gone
                    e.addSuppressed(rollbackException);
                }
                throw e;
            }
            // not in a finally block, where a failure on a lost connection would hide the real error
            connection.setAutoCommit(true);
            migration.logSummary();
        } catch (final SQLException | RuntimeException e) {
            throw new OnmsUpgradeException("Can't copy users and groups to the database: " + e.getMessage(), e);
        }
    }

    /** @return a description of the user data in the database, or null if there is none */
    private static String existingData(final Connection connection) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement("SELECT "
                + "(SELECT COUNT(*) FROM users), (SELECT COUNT(*) FROM groups), (SELECT COUNT(*) FROM oncall_roles), "
                + "(SELECT COUNT(*) FROM security_roles WHERE NOT builtin)");
             final ResultSet rs = statement.executeQuery()) {
            rs.next();
            final List<String> data = new ArrayList<>();
            final String[] names = { "users", "groups", "on-call roles", "custom security roles" };
            for (int i = 0; i < names.length; i++) {
                final long count = rs.getLong(i + 1);
                if (count > 0) {
                    data.add(names[i] + ": " + count);
                }
            }
            return data.isEmpty() ? null : String.join(", ", data);
        }
    }

    @Override
    public void postExecute() throws OnmsUpgradeException {
    }

    /** Copies the files to {@code etc_archive}. */
    private void archive() throws OnmsUpgradeException {
        final File archive = new File(getHomeDirectory(), "etc_archive");
        if (!archive.isDirectory() && !archive.mkdirs()) {
            throw new OnmsUpgradeException("Can't create the folder '" + archive + "'");
        }
        for (final String name : Arrays.asList(USERS_FILE_NAME, GROUPS_FILE_NAME, SECURITY_ROLES_FILE_NAME)) {
            final File source = etcFile(name);
            if (source.exists()) {
                try {
                    Files.copy(source.toPath(), new File(archive, name).toPath(), REPLACE_EXISTING, COPY_ATTRIBUTES);
                    log("Copied %s to %s\n", source, archive);
                } catch (final IOException e) {
                    throw new OnmsUpgradeException("Can't copy '" + source + "' to '" + archive + "': " + e.getMessage(), e);
                }
            }
        }
    }

    /** Nothing to undo: {@link #execute()} rolls its transaction back. */
    @Override
    public void rollback() throws OnmsUpgradeException {
    }

    private File etcFile(final String name) {
        return new File(getHomeDirectory(), "etc" + File.separator + name);
    }

    private <T> T read(final Class<T> type, final File file) throws OnmsUpgradeException {
        if (!file.exists()) {
            log("%s not found, nothing to copy from it\n", file);
            return null;
        }
        try {
            return UsersGroupsXmlImport.read(type, file);
        } catch (final RuntimeException e) {
            throw new OnmsUpgradeException("Can't read '" + file + "': " + e.getMessage(), e);
        }
    }

    private List<String> readSecurityRoles(final File file) throws OnmsUpgradeException {
        try {
            return UsersGroupsXmlImport.readSecurityRoles(file);
        } catch (final IOException e) {
            throw new OnmsUpgradeException("Can't read '" + file + "': " + e.getMessage(), e);
        }
    }
}
