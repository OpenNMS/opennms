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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.BundleLists;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Groupinfo;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.groups.Time;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.model.usermgmt.ContactType;
import org.opennms.netmgt.model.usermgmt.OnCallSchedule;
import org.opennms.netmgt.model.usermgmt.SecurityRoles;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * Copies users.xml, groups.xml and security-roles.properties into the user management tables (37.0.0).
 *
 * <p>Everything is written in one transaction. Data that already exists by name (a user, a group, an
 * on-call role or a security role) is kept as is, so the task can run again safely. Entries that the
 * tables cannot hold are skipped with a message: unknown security roles (which login drops today as
 * well), group members and schedule users that do not exist, on-call roles whose membership group or
 * supervisor does not exist, a second contact of the same type, unknown contact and schedule types, and
 * values longer than their column. Names are matched exactly, without trimming, as OpenNMS matches them
 * today. The XSDs normally reject unknown types, but JaxbUtils reads a file without validation when it
 * cannot find the XSD.</p>
 *
 * <p>The files stay in {@code etc}, because OpenNMS still reads them; copies go to {@code etc_archive}.</p>
 */
public class UsersGroupsXmlMigratorOffline extends AbstractOnmsUpgrade {

    private static final String USERS_FILE_NAME = "users.xml";
    private static final String GROUPS_FILE_NAME = "groups.xml";
    private static final String SECURITY_ROLES_FILE_NAME = "security-roles.properties";

    private static final List<String> TABLES = Arrays.asList(
            "security_roles", "users", "user_contacts", "user_duty_schedules", "user_security_roles",
            "groups", "group_members", "group_duty_schedules",
            "oncall_roles", "oncall_schedules", "oncall_schedule_times");

    // the column sizes in core/schema/src/main/liquibase/37.0.0/changelog.xml
    private static final int SECURITY_ROLE_NAME_LENGTH = 128;
    private static final int USERNAME_LENGTH = 256;
    private static final int FULL_NAME_LENGTH = 256;
    private static final int PASSWORD_LENGTH = 512;
    private static final int TUI_PIN_LENGTH = 32;
    private static final int TIME_ZONE_ID_LENGTH = 64;
    private static final int CONTACT_LENGTH = 256;
    private static final int DUTY_SCHEDULE_LENGTH = 64;
    private static final int GROUP_NAME_LENGTH = 256;
    private static final int DEFAULT_MAP_LENGTH = 256;
    private static final int ONCALL_ROLE_NAME_LENGTH = 256;
    private static final int TIME_ID_LENGTH = 64;
    private static final int DAY_LENGTH = 16;
    private static final int TIME_LENGTH = 32;

    private static final Set<String> CONTACT_TYPES = names(ContactType.class);
    private static final Set<String> SCHEDULE_TYPES = names(OnCallSchedule.Type.class);

    private boolean m_migrated;

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
        return true;
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
        final List<String> customRoles = readSecurityRoles(etcFile(SECURITY_ROLES_FILE_NAME));
        final Userinfo userinfo = read(Userinfo.class, etcFile(USERS_FILE_NAME));
        final Groupinfo groupinfo = read(Groupinfo.class, etcFile(GROUPS_FILE_NAME));

        try (final Connection connection = DataSourceFactory.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                final Import migration = new Import(connection);
                migration.securityRoles(customRoles);
                migration.users(userinfo == null ? Collections.emptyList() : userinfo.getUsers());
                migration.groups(groupinfo == null ? Collections.emptyList() : groupinfo.getGroups());
                migration.onCallRoles(groupinfo == null ? Collections.emptyList() : groupinfo.getRoles());
                connection.commit();
                migration.logSummary();
            } catch (final SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (final SQLException rollbackException) {
                    // keep the cause of the failure; the connection may be gone
                    e.addSuppressed(rollbackException);
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (final SQLException | RuntimeException e) {
            throw new OnmsUpgradeException("Can't copy users and groups to the database: " + e.getMessage(), e);
        }
        m_migrated = true;
    }

    /** Copies the files to {@code etc_archive}, but only after a successful {@link #execute()}. */
    @Override
    public void postExecute() throws OnmsUpgradeException {
        if (!m_migrated) {
            return;
        }
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
            return JaxbUtils.unmarshal(type, file);
        } catch (final RuntimeException e) {
            throw new OnmsUpgradeException("Can't read '" + file + "': " + e.getMessage(), e);
        }
    }

    /** Reads the custom roles the way {@code Authentication.loadRoles()} does. */
    private List<String> readSecurityRoles(final File file) throws OnmsUpgradeException {
        final List<String> roles = new ArrayList<>();
        if (!file.exists()) {
            return roles;
        }
        final Properties properties = new Properties();
        try (final InputStream in = new FileInputStream(file)) {
            properties.load(in);
        } catch (final IOException e) {
            throw new OnmsUpgradeException("Can't read '" + file + "': " + e.getMessage(), e);
        }
        final String roleList = properties.getProperty("roles");
        if (roleList != null) {
            for (final String role : BundleLists.parseBundleList(roleList)) {
                if (!role.isEmpty()) {
                    roles.add(SecurityRoles.toRoleName(role));
                }
            }
        }
        return roles;
    }

    private static <E extends Enum<E>> Set<String> names(final Class<E> type) {
        final Set<String> names = new HashSet<>();
        EnumSet.allOf(type).forEach(e -> names.add(e.name()));
        return names;
    }

    /** One run of the copy, on one connection in one transaction. */
    private class Import {
        private final Connection m_connection;

        // name -> id for every row by name, the existing ones included, to resolve references
        private final Map<String, Integer> m_securityRoleIds;
        private final Map<String, Integer> m_userIds;
        private final Map<String, Integer> m_groupIds;
        private final Set<String> m_onCallRoleNames;

        private final Map<String, int[]> m_counts = new LinkedHashMap<>();
        private int m_skipped;

        Import(final Connection connection) throws SQLException {
            m_connection = connection;
            m_securityRoleIds = loadIds("SELECT name, id FROM security_roles");
            m_userIds = loadIds("SELECT username, id FROM users");
            m_groupIds = loadIds("SELECT name, id FROM groups");
            m_onCallRoleNames = new HashSet<>(loadIds("SELECT name, id FROM oncall_roles").keySet());
        }

        void securityRoles(final List<String> names) throws SQLException {
            for (final String name : new LinkedHashSet<>(names)) {
                if (m_securityRoleIds.containsKey(name)) {
                    count("security roles", false);
                } else if (fits(name, SECURITY_ROLE_NAME_LENGTH, "security role", name, "security-roles.properties")) {
                    m_securityRoleIds.put(name, insert("INSERT INTO security_roles (id, name, builtin, created_time, last_modified) "
                            + "VALUES (nextval('security_roles_id_seq'), ?, false, NOW(), NOW()) RETURNING id", name));
                    count("security roles", true);
                }
            }
        }

        void users(final List<User> xmlUsers) throws SQLException {
            // users.xml can repeat a user; UserManager keeps the last one
            final Map<String, User> users = new LinkedHashMap<>();
            for (final User user : xmlUsers) {
                final String username = user.getUserId();
                if (username == null || username.isEmpty()) {
                    skip("a user without a user-id in users.xml");
                } else if (users.put(username, user) != null) {
                    warn("User '%s' is in users.xml more than once; the last one is used", username);
                }
            }
            for (final User user : users.values()) {
                final String username = user.getUserId();
                if (m_userIds.containsKey(username)) {
                    count("users", false);
                    continue;
                }
                final String password = user.getPassword() == null ? null : user.getPassword().getEncryptedPassword();
                if (!fits(username, USERNAME_LENGTH, "user", username, "the user name") || !fits(password, PASSWORD_LENGTH, "user", username, "the password")) {
                    continue;
                }
                final int userId = insert("INSERT INTO users (id, username, full_name, comments, password_hash, password_salted, tui_pin, time_zone_id, created_time, last_modified) "
                                + "VALUES (nextval('users_id_seq'), ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) RETURNING id",
                        username,
                        valueOrNull(user.getFullName().orElse(null), FULL_NAME_LENGTH, "user", username, "full-name"),
                        user.getUserComments().orElse(null),
                        password,
                        user.getPassword() != null && Boolean.TRUE.equals(user.getPassword().getSalt()),
                        valueOrNull(user.getTuiPin().orElse(null), TUI_PIN_LENGTH, "user", username, "tui-pin"),
                        valueOrNull(user.getTimeZoneId().map(ZoneId::getId).orElse(null), TIME_ZONE_ID_LENGTH, "user", username, "time-zone-id"));
                m_userIds.put(username, userId);
                count("users", true);

                contacts(userId, username, user.getContacts());
                dutySchedules("INSERT INTO user_duty_schedules (user_id, position, schedule) VALUES (?, ?, ?)", userId, "user", username, user.getDutySchedules());
                userSecurityRoles(userId, username, user.getRoles());
            }
        }

        private void contacts(final int userId, final String username, final List<Contact> contacts) throws SQLException {
            final Set<String> types = new HashSet<>();
            for (final Contact contact : contacts) {
                final String type = contact.getType();
                final String info = contact.getInfo().orElse(null);
                final String provider = contact.getServiceProvider().orElse(null);
                if (!CONTACT_TYPES.contains(type)) {
                    skip("user '%s': contact type '%s' is not known", username, type);
                } else if (!types.add(type)) {
                    // UserManager reads only the first contact of a type, even when the first one is skipped below
                    skip("user '%s': a second '%s' contact (%s)", username, type, info);
                } else if (fits(info, CONTACT_LENGTH, "user", username, type + " contact info") && fits(provider, CONTACT_LENGTH, "user", username, type + " contact serviceProvider")) {
                    update("INSERT INTO user_contacts (id, user_id, contact_type, info, service_provider) VALUES (nextval('user_contacts_id_seq'), ?, ?, ?, ?)",
                            userId, type, info, provider);
                }
            }
        }

        private void userSecurityRoles(final int userId, final String username, final List<String> roles) throws SQLException {
            // matched as is: login does not trim them either (Authentication.isValidRole)
            for (final String name : new LinkedHashSet<>(roles)) {
                if (name == null) {
                    continue;
                }
                final Integer roleId = m_securityRoleIds.get(name);
                if (roleId == null) {
                    skip("user '%s': security role '%s' is not built in and not in security-roles.properties", username, name);
                } else {
                    update("INSERT INTO user_security_roles (user_id, role_id) VALUES (?, ?)", userId, roleId);
                }
            }
        }

        void groups(final List<Group> xmlGroups) throws SQLException {
            // groups.xml can repeat a group; GroupManager keeps the last one
            final Map<String, Group> groups = new LinkedHashMap<>();
            for (final Group group : xmlGroups) {
                final String name = group.getName();
                if (name == null || name.isEmpty()) {
                    skip("a group without a name in groups.xml");
                } else if (groups.put(name, group) != null) {
                    warn("Group '%s' is in groups.xml more than once; the last one is used", name);
                }
            }
            for (final Group group : groups.values()) {
                final String name = group.getName();
                if (m_groupIds.containsKey(name)) {
                    count("groups", false);
                    continue;
                }
                if (!fits(name, GROUP_NAME_LENGTH, "group", name, "the name")) {
                    continue;
                }
                final int groupId = insert("INSERT INTO groups (id, name, default_map, comments, created_time, last_modified) "
                                + "VALUES (nextval('groups_id_seq'), ?, ?, ?, NOW(), NOW()) RETURNING id",
                        name,
                        valueOrNull(group.getDefaultMap().orElse(null), DEFAULT_MAP_LENGTH, "group", name, "default-map"),
                        group.getComments().orElse(null));
                m_groupIds.put(name, groupId);
                count("groups", true);

                // matched as is: users.xml trims user ids, but nothing trims group members
                int position = 0;
                for (final String member : new LinkedHashSet<>(group.getUsers())) {
                    final Integer userId = member == null ? null : m_userIds.get(member);
                    if (userId == null) {
                        skip("group '%s': member '%s' is not a user", name, member);
                    } else {
                        update("INSERT INTO group_members (group_id, position, user_id) VALUES (?, ?, ?)", groupId, position++, userId);
                    }
                }
                dutySchedules("INSERT INTO group_duty_schedules (group_id, position, schedule) VALUES (?, ?, ?)", groupId, "group", name, group.getDutySchedules());
            }
        }

        void onCallRoles(final List<Role> xmlRoles) throws SQLException {
            // groups.xml can repeat a role; GroupManager keeps the last one
            final Map<String, Role> roles = new LinkedHashMap<>();
            for (final Role role : xmlRoles) {
                final String name = role.getName();
                if (name == null || name.isEmpty()) {
                    skip("an on-call role without a name in groups.xml");
                } else if (roles.put(name, role) != null) {
                    warn("On-call role '%s' is in groups.xml more than once; the last one is used", name);
                }
            }
            for (final Role role : roles.values()) {
                final String name = role.getName();
                if (m_onCallRoleNames.contains(name)) {
                    count("on-call roles", false);
                    continue;
                }
                final Integer groupId = m_groupIds.get(role.getMembershipGroup());
                final Integer supervisorId = m_userIds.get(role.getSupervisor());
                if (groupId == null) {
                    skip("on-call role '%s': membership group '%s' is not a group", name, role.getMembershipGroup());
                    continue;
                }
                if (supervisorId == null) {
                    skip("on-call role '%s': supervisor '%s' is not a user", name, role.getSupervisor());
                    continue;
                }
                if (!fits(name, ONCALL_ROLE_NAME_LENGTH, "on-call role", name, "the name")) {
                    continue;
                }
                final int roleId = insert("INSERT INTO oncall_roles (id, name, description, membership_group_id, supervisor_user_id, created_time, last_modified) "
                                + "VALUES (nextval('oncall_roles_id_seq'), ?, ?, ?, ?, NOW(), NOW()) RETURNING id",
                        name, role.getDescription().orElse(null), groupId, supervisorId);
                m_onCallRoleNames.add(name);
                count("on-call roles", true);

                int position = 0;
                for (final Schedule schedule : role.getSchedules()) {
                    if (schedule(roleId, name, position, schedule)) {
                        position++;
                    }
                }
            }
        }

        /** @return true if the schedule was written */
        private boolean schedule(final int roleId, final String roleName, final int position, final Schedule schedule) throws SQLException {
            final Integer userId = m_userIds.get(schedule.getName());
            if (userId == null) {
                skip("on-call role '%s': the schedule of '%s', who is not a user", roleName, schedule.getName());
                return false;
            }
            if (!SCHEDULE_TYPES.contains(schedule.getType())) {
                skip("on-call role '%s': the schedule of '%s' has the unknown type '%s'", roleName, schedule.getName(), schedule.getType());
                return false;
            }
            final List<Time> times = new ArrayList<>();
            for (final Time time : schedule.getTimes()) {
                final String what = "a time of the schedule of '" + schedule.getName() + "'";
                if (time.getBegins() == null || time.getEnds() == null) {
                    skip("on-call role '%s': %s has no begins or ends", roleName, what);
                } else if (fits(time.getId().orElse(null), TIME_ID_LENGTH, "on-call role", roleName, what + " (id)")
                        && fits(time.getDay().orElse(null), DAY_LENGTH, "on-call role", roleName, what + " (day)")
                        && fits(time.getBegins(), TIME_LENGTH, "on-call role", roleName, what + " (begins)")
                        && fits(time.getEnds(), TIME_LENGTH, "on-call role", roleName, what + " (ends)")) {
                    times.add(time);
                }
            }
            if (times.isEmpty()) {
                skip("on-call role '%s': the schedule of '%s' has no usable times", roleName, schedule.getName());
                return false;
            }
            final int scheduleId = insert("INSERT INTO oncall_schedules (id, oncall_role_id, user_id, schedule_type, position) "
                    + "VALUES (nextval('oncall_schedules_id_seq'), ?, ?, ?, ?) RETURNING id", roleId, userId, schedule.getType(), position);
            int timePosition = 0;
            for (final Time time : times) {
                update("INSERT INTO oncall_schedule_times (id, schedule_id, position, time_id, day, begins, ends) "
                                + "VALUES (nextval('oncall_schedule_times_id_seq'), ?, ?, ?, ?, ?, ?)",
                        scheduleId, timePosition++, time.getId().orElse(null), time.getDay().orElse(null), time.getBegins(), time.getEnds());
            }
            return true;
        }

        private void dutySchedules(final String sql, final int ownerId, final String ownerType, final String ownerName, final List<String> schedules) throws SQLException {
            int position = 0;
            // copied as is: DutySchedule parses the exact string
            for (final String schedule : schedules) {
                if (schedule != null && fits(schedule, DUTY_SCHEDULE_LENGTH, ownerType, ownerName, "duty schedule '" + schedule + "'")) {
                    update(sql, ownerId, position++, schedule);
                }
            }
        }

        void logSummary() {
            for (final Map.Entry<String, int[]> entry : m_counts.entrySet()) {
                log("%s: %d copied, %d already in the database\n", entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
            }
            if (m_skipped > 0) {
                log("%d entries were skipped; see the messages above\n", m_skipped);
            }
        }

        private void count(final String what, final boolean copied) {
            m_counts.computeIfAbsent(what, k -> new int[2])[copied ? 0 : 1]++;
        }

        /** @return true if the value is null or fits its column; otherwise logs that the entry is skipped */
        private boolean fits(final String value, final int length, final String ownerType, final String ownerName, final String what) {
            if (value == null || value.length() <= length) {
                return true;
            }
            skip("%s '%s': %s is longer than %d characters", ownerType, ownerName, what, length);
            return false;
        }

        /** For an optional column: a value too long for it is dropped, with a message, and the row is kept. */
        private String valueOrNull(final String value, final int length, final String ownerType, final String ownerName, final String what) {
            if (value == null || value.length() <= length) {
                return value;
            }
            warn("%s '%s': %s is longer than %d characters and was not copied", ownerType, ownerName, what, length);
            return null;
        }

        private void skip(final String format, final Object... args) {
            m_skipped++;
            log("Skipped " + format + "\n", args);
        }

        private void warn(final String format, final Object... args) {
            log("Warning: " + format + "\n", args);
        }

        private Map<String, Integer> loadIds(final String sql) throws SQLException {
            final Map<String, Integer> ids = new HashMap<>();
            try (final PreparedStatement statement = m_connection.prepareStatement(sql);
                 final ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ids.put(rs.getString(1), rs.getInt(2));
                }
            }
            return ids;
        }

        private int insert(final String sql, final Object... args) throws SQLException {
            try (final PreparedStatement statement = prepare(sql, args);
                 final ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }

        private void update(final String sql, final Object... args) throws SQLException {
            try (final PreparedStatement statement = prepare(sql, args)) {
                statement.executeUpdate();
            }
        }

        private PreparedStatement prepare(final String sql, final Object... args) throws SQLException {
            final PreparedStatement statement = m_connection.prepareStatement(sql);
            try {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null) {
                        statement.setNull(i + 1, Types.VARCHAR);
                    } else {
                        statement.setObject(i + 1, args[i]);
                    }
                }
            } catch (final SQLException e) {
                statement.close();
                throw e;
            }
            return statement;
        }
    }
}
