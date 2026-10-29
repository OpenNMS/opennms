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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.opennms.core.utils.BundleLists;
import org.opennms.core.utils.HtmlEncoding;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.groups.Time;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.model.usermgmt.ContactType;
import org.opennms.netmgt.model.usermgmt.OnCallSchedule;
import org.opennms.netmgt.model.usermgmt.SecurityRoles;

/**
 * One run of the copy of users.xml, groups.xml and security-roles.properties into the user management
 * tables, on one connection. The caller owns the transaction: it commits or rolls back.
 *
 * <p>The caller passes security roles, users, groups and on-call roles in that order, so that references
 * resolve: they resolve against the database and the entries written in this run. Names and values are copied
 * exactly, without trimming; full names, contact info and service providers are HTML-decoded once unless
 * {@code decode} is false. A file that repeats an entry uses the last one, with a warning. A custom security
 * role that is in the database already is left alone.</p>
 *
 * <p>Entries and values that the tables cannot hold are skipped with a message: a value longer than its column,
 * an unknown contact or schedule type, a second contact of a type, a reference to a user, group or security role
 * that does not exist, and a schedule without usable times. The messages go to the {@code messages} consumer,
 * one line each, without a trailing newline.</p>
 */
public final class UsersGroupsXmlImport {

    /** What to do with a user, group or on-call role whose name is already in the database. */
    public enum Existing {
        /** The database has no user data, so there is nothing to meet. */
        NONE_EXPECTED,
        /**
         * The entry in the database is left as it is and listed in {@link #existingEntries()}. The other entries of
         * the files can still refer to it.
         */
        SKIP,
        /**
         * The file's entry replaces it, and it is listed in {@link #existingEntries()}. The row is updated in place
         * and keeps its id, so the rows that refer to it stay connected; its child rows (contacts, duty schedules,
         * security roles of a user, members of a group, schedules of an on-call role) are replaced by the file's.
         */
        OVERWRITE
    }

    private static final String USERS = "users";
    private static final String GROUPS = "groups";
    private static final String ONCALL_ROLES = "on-call roles";
    private static final String SECURITY_ROLES = "custom security roles";

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

    private final Connection m_connection;
    private final Existing m_existing;
    private final boolean m_decode;
    private final Consumer<String> m_messages;

    // name -> id, to resolve references: the rows of the database, then also the rows written by this run
    private final Map<String, Integer> m_securityRoleIds;
    private final Map<String, Integer> m_userIds;
    private final Map<String, Integer> m_groupIds;
    private final Map<String, Integer> m_onCallRoleIds;

    private final Map<String, List<String>> m_existingEntries = new LinkedHashMap<>();
    private final Map<String, Counts> m_counts = new LinkedHashMap<>();
    private int m_skipped;
    private int m_warnings;

    private static final class Counts {
        private int m_imported;
        private int m_overwritten;
        private int m_existingSkipped;
    }

    /**
     * @param decode whether full names, contact info and service providers are HTML-decoded once, as files written
     *               before 37 need; a file written by the XML store of 37 or later holds them as they are
     * @param messages receives one line per message, without a trailing newline
     */
    public UsersGroupsXmlImport(final Connection connection, final Existing existing, final boolean decode, final Consumer<String> messages) throws SQLException {
        m_connection = connection;
        m_existing = existing;
        m_decode = decode;
        m_messages = messages;
        m_securityRoleIds = loadIds("SELECT name, id FROM security_roles");
        m_userIds = loadIds("SELECT username, id FROM users");
        m_groupIds = loadIds("SELECT name, id FROM groups");
        m_onCallRoleIds = loadIds("SELECT name, id FROM oncall_roles");
    }

    /** @return the file as the type, or null if the file does not exist; a {@link RuntimeException} from JAXB passes through */
    public static <T> T read(final Class<T> type, final File file) {
        if (!file.exists()) {
            return null;
        }
        return JaxbUtils.unmarshal(type, file);
    }

    /** Reads the custom roles the way {@code Authentication.loadRoles()} does. */
    public static List<String> readSecurityRoles(final File file) throws IOException {
        final List<String> roles = new ArrayList<>();
        if (!file.exists()) {
            return roles;
        }
        final Properties properties = new Properties();
        try (final InputStream in = new FileInputStream(file)) {
            properties.load(in);
        } catch (final IllegalArgumentException e) {
            // a malformed Unicode escape
            throw new IOException(e.getMessage(), e);
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

    /**
     * Looks up which users, groups and on-call roles of the files are in the database, without writing.
     *
     * @return kind ("users", "groups", "on-call roles") -> the names, in file order; a kind without one is left out.
     *         An import with {@link Existing#SKIP} or {@link Existing#OVERWRITE} on the same data finds the same.
     */
    public static Map<String, List<String>> findExisting(final Connection connection, final List<User> users, final List<Group> groups, final List<Role> roles) throws SQLException {
        final Map<String, List<String>> existing = new LinkedHashMap<>();
        findExisting(connection, existing, USERS, "SELECT EXISTS (SELECT 1 FROM users WHERE username = ?)",
                users.stream().map(User::getUserId).collect(Collectors.toList()));
        findExisting(connection, existing, GROUPS, "SELECT EXISTS (SELECT 1 FROM groups WHERE name = ?)",
                groups.stream().map(Group::getName).collect(Collectors.toList()));
        findExisting(connection, existing, ONCALL_ROLES, "SELECT EXISTS (SELECT 1 FROM oncall_roles WHERE name = ?)",
                roles.stream().map(Role::getName).collect(Collectors.toList()));
        return existing;
    }

    private static void findExisting(final Connection connection, final Map<String, List<String>> existing, final String kind, final String sql, final List<String> names) throws SQLException {
        final List<String> found = new ArrayList<>();
        try (final PreparedStatement statement = connection.prepareStatement(sql)) {
            // once each, in the order of their first entry, as the import goes through them
            for (final String name : new LinkedHashSet<>(names)) {
                if (name == null || name.isEmpty()) {
                    continue;
                }
                statement.setString(1, name);
                try (final ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    if (rs.getBoolean(1)) {
                        found.add(name);
                    }
                }
            }
        }
        if (!found.isEmpty()) {
            existing.put(kind, found);
        }
    }

    /**
     * @return kind ("users", "groups", "on-call roles") -> the names of the entries of the files that were in the
     *         database already (skipped or overwritten), in file order; empty with {@link Existing#NONE_EXPECTED}
     */
    public Map<String, List<String>> existingEntries() {
        return Collections.unmodifiableMap(m_existingEntries);
    }

    private static <E extends Enum<E>> Set<String> names(final Class<E> type) {
        final Set<String> names = new HashSet<>();
        EnumSet.allOf(type).forEach(e -> names.add(e.name()));
        return names;
    }

    public void securityRoles(final List<String> names) throws SQLException {
        for (final String name : new LinkedHashSet<>(names)) {
            // a built-in role in security-roles.properties is no custom role
            if (!m_securityRoleIds.containsKey(name) && fits(name, SECURITY_ROLE_NAME_LENGTH, "security role", name, "security-roles.properties")) {
                m_securityRoleIds.put(name, insert("INSERT INTO security_roles (id, name, builtin, created_time, last_modified) "
                        + "VALUES (nextval('security_roles_id_seq'), ?, false, NOW(), NOW()) RETURNING id", name));
                counts(SECURITY_ROLES).m_imported++;
            }
        }
    }

    public void users(final List<User> xmlUsers) throws SQLException {
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
            final Integer existingId = existing(USERS, m_userIds, username);
            if (existingId != null && m_existing == Existing.SKIP) {
                // its id stays in m_userIds, so the groups and on-call roles of the files can refer to it
                counts(USERS).m_existingSkipped++;
                continue;
            }
            final String password = user.getPassword() == null ? null : user.getPassword().getEncryptedPassword();
            if (!fits(username, USERNAME_LENGTH, "user", username, "the user name") || !fits(password, PASSWORD_LENGTH, "user", username, "the password")) {
                continue;
            }
            final String fullName = valueOrNull(decode(user.getFullName().orElse(null)), FULL_NAME_LENGTH, "user", username, "full-name");
            final String comments = user.getUserComments().orElse(null);
            final boolean salted = user.getPassword() != null && Boolean.TRUE.equals(user.getPassword().getSalt());
            final String tuiPin = valueOrNull(user.getTuiPin().orElse(null), TUI_PIN_LENGTH, "user", username, "tui-pin");
            final String timeZoneId = valueOrNull(user.getTimeZoneId().map(ZoneId::getId).orElse(null), TIME_ZONE_ID_LENGTH, "user", username, "time-zone-id");
            final int userId;
            if (existingId == null) {
                userId = insert("INSERT INTO users (id, username, full_name, comments, password_hash, password_salted, tui_pin, time_zone_id, created_time, last_modified) "
                                + "VALUES (nextval('users_id_seq'), ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) RETURNING id",
                        username, fullName, comments, password, salted, tuiPin, timeZoneId);
                m_userIds.put(username, userId);
                counts(USERS).m_imported++;
            } else {
                // in place: memberships and on-call schedules refer to the id
                userId = existingId;
                update("UPDATE users SET full_name = ?, comments = ?, password_hash = ?, password_salted = ?, tui_pin = ?, time_zone_id = ?, last_modified = NOW() WHERE id = ?",
                        fullName, comments, password, salted, tuiPin, timeZoneId, userId);
                update("DELETE FROM user_contacts WHERE user_id = ?", userId);
                update("DELETE FROM user_duty_schedules WHERE user_id = ?", userId);
                update("DELETE FROM user_security_roles WHERE user_id = ?", userId);
                counts(USERS).m_overwritten++;
            }

            contacts(userId, username, user.getContacts());
            dutySchedules("INSERT INTO user_duty_schedules (user_id, position, schedule) VALUES (?, ?, ?)", userId, "user", username, user.getDutySchedules());
            userSecurityRoles(userId, username, user.getRoles());
        }
    }

    private void contacts(final int userId, final String username, final List<Contact> contacts) throws SQLException {
        final Set<String> types = new HashSet<>();
        for (final Contact contact : contacts) {
            final String type = contact.getType();
            final String info = decode(contact.getInfo().orElse(null));
            final String provider = decode(contact.getServiceProvider().orElse(null));
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
            if (roleId == null && m_existing == Existing.NONE_EXPECTED) {
                // the migrator's database has only the built-in roles
                skip("user '%s': security role '%s' is not built in and not in security-roles.properties", username, name);
            } else if (roleId == null) {
                skip("user '%s': security role '%s' does not exist", username, name);
            } else {
                update("INSERT INTO user_security_roles (user_id, role_id) VALUES (?, ?)", userId, roleId);
            }
        }
    }

    public void groups(final List<Group> xmlGroups) throws SQLException {
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
            final Integer existingId = existing(GROUPS, m_groupIds, name);
            if (existingId != null && m_existing == Existing.SKIP) {
                // its id stays in m_groupIds, so the on-call roles of the files can refer to it
                counts(GROUPS).m_existingSkipped++;
                continue;
            }
            if (!fits(name, GROUP_NAME_LENGTH, "group", name, "the name")) {
                continue;
            }
            final String defaultMap = valueOrNull(group.getDefaultMap().orElse(null), DEFAULT_MAP_LENGTH, "group", name, "default-map");
            final String comments = group.getComments().orElse(null);
            final int groupId;
            if (existingId == null) {
                groupId = insert("INSERT INTO groups (id, name, default_map, comments, created_time, last_modified) "
                                + "VALUES (nextval('groups_id_seq'), ?, ?, ?, NOW(), NOW()) RETURNING id",
                        name, defaultMap, comments);
                m_groupIds.put(name, groupId);
                counts(GROUPS).m_imported++;
            } else {
                // in place: on-call roles refer to the id
                groupId = existingId;
                update("UPDATE groups SET default_map = ?, comments = ?, last_modified = NOW() WHERE id = ?", defaultMap, comments, groupId);
                update("DELETE FROM group_members WHERE group_id = ?", groupId);
                update("DELETE FROM group_duty_schedules WHERE group_id = ?", groupId);
                counts(GROUPS).m_overwritten++;
            }

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

    public void onCallRoles(final List<Role> xmlRoles) throws SQLException {
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
            final Integer existingId = existing(ONCALL_ROLES, m_onCallRoleIds, name);
            if (existingId != null && m_existing == Existing.SKIP) {
                counts(ONCALL_ROLES).m_existingSkipped++;
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
            final int roleId;
            if (existingId == null) {
                roleId = insert("INSERT INTO oncall_roles (id, name, description, membership_group_id, supervisor_user_id, created_time, last_modified) "
                                + "VALUES (nextval('oncall_roles_id_seq'), ?, ?, ?, ?, NOW(), NOW()) RETURNING id",
                        name, role.getDescription().orElse(null), groupId, supervisorId);
                m_onCallRoleIds.put(name, roleId);
                counts(ONCALL_ROLES).m_imported++;
            } else {
                roleId = existingId;
                update("UPDATE oncall_roles SET description = ?, membership_group_id = ?, supervisor_user_id = ?, last_modified = NOW() WHERE id = ?",
                        role.getDescription().orElse(null), groupId, supervisorId, roleId);
                // their times go with them (ON DELETE CASCADE)
                update("DELETE FROM oncall_schedules WHERE oncall_role_id = ?", roleId);
                counts(ONCALL_ROLES).m_overwritten++;
            }

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

    public void logSummary() {
        for (final Map.Entry<String, Counts> entry : m_counts.entrySet()) {
            final Counts counts = entry.getValue();
            if (m_existing == Existing.NONE_EXPECTED) {
                log("%s: %d copied", entry.getKey(), counts.m_imported);
            } else {
                log("%s: %d imported, %d overwritten, %d existing skipped", entry.getKey(), counts.m_imported, counts.m_overwritten, counts.m_existingSkipped);
            }
        }
        if (m_skipped > 0 || m_warnings > 0) {
            log("Entries skipped: %d, warnings: %d; see the messages above", m_skipped, m_warnings);
        }
    }

    private void log(final String format, final Object... args) {
        m_messages.accept(String.format(format, args));
    }

    private Counts counts(final String kind) {
        return m_counts.computeIfAbsent(kind, k -> new Counts());
    }

    /**
     * @return the id of the entry of this name that was in the database before this run, which is then listed in
     *         {@link #existingEntries()}, or null if there is none. Each name comes here once per run, so an id in
     *         the map is one from the database.
     */
    private Integer existing(final String kind, final Map<String, Integer> ids, final String name) {
        if (m_existing == Existing.NONE_EXPECTED) {
            return null;
        }
        final Integer id = ids.get(name);
        if (id != null) {
            m_existingEntries.computeIfAbsent(kind, k -> new ArrayList<>()).add(name);
        }
        return id;
    }

    private String decode(final String value) {
        return m_decode ? HtmlEncoding.decodeHtmlEncoded(value) : value;
    }

    /** @return true if the value is null or fits its column; otherwise logs that the entry is skipped */
    private boolean fits(final String value, final int length, final String ownerType, final String ownerName, final String what) {
        // varchar(n) counts characters, not UTF-16 units
        if (value == null || value.codePointCount(0, value.length()) <= length) {
            return true;
        }
        skip("%s '%s': %s is longer than %d characters", ownerType, ownerName, what, length);
        return false;
    }

    /** For an optional column: a value too long for it is dropped, with a message, and the row is kept. */
    private String valueOrNull(final String value, final int length, final String ownerType, final String ownerName, final String what) {
        if (value == null || value.codePointCount(0, value.length()) <= length) {
            return value;
        }
        warn("%s '%s': %s is longer than %d characters and was not copied", ownerType, ownerName, what, length);
        return null;
    }

    private void skip(final String format, final Object... args) {
        m_skipped++;
        log("Skipped " + format, args);
    }

    private void warn(final String format, final Object... args) {
        m_warnings++;
        log("Warning: " + format, args);
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
