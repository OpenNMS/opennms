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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import java.util.regex.Pattern;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.BundleLists;
import org.opennms.core.utils.HtmlEncoding;
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
import org.opennms.netmgt.model.usermgmt.ReservedNames;
import org.opennms.netmgt.model.usermgmt.SecurityRoles;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * Copies users.xml, groups.xml and security-roles.properties into the user management tables (37.0.0), then moves
 * them to {@code etc_archive}.
 *
 * <p>The task copies only into a database without user data: if there is a user, a group, an on-call
 * role or a custom security role, it copies nothing and logs why. It runs on every install, but once per
 * database: it sets the usermgmt_initialized marker when it copied users (also from a users.xml without users, or
 * the default users, see below) or found data already there, and it skips a database with the marker. Thus, the copy
 * never changes existing data.</p>
 *
 * <p>OpenNMS no longer ships users.xml and groups.xml. When a package upgrade removes them, RPM renames a changed
 * file to {@code <name>.rpmsave} and deletes an unchanged one. So each of the two files is read from
 * {@code etc/<name>}, or else from {@code etc/<name>.rpmsave}; when both exist, the plain one is read and the
 * {@code .rpmsave} one is archived unread. security-roles.properties was never shipped, so it is read from
 * {@code etc} only.</p>
 *
 * <p>When only one of users.xml and groups.xml is found, the other one was deleted unchanged, so the task copies
 * what OpenNMS shipped in its place ({@link UserMgmtDefaults}): the users admin and rtc, or the group Admin with
 * admin. Without the Admin group, the default destination path would name a missing group; without the users, nobody
 * could log in. When neither is found but security-roles.properties has custom roles, the task copies them together
 * with the default users and group, and sets the marker in the same transaction: the custom roles alone would make
 * its next run skip the database as one with user data, so a failed or interrupted seed would leave it without
 * users. When neither is found and there are no custom roles (a new installation), the task copies nothing and leaves
 * the marker unset, and {@link DefaultUsersSeedOffline} adds the default users. It still archives a
 * security-roles.properties with only built-in roles: the seed adds no users while the file is in etc.</p>
 *
 * <p>Everything is written in one transaction. Entries that the tables cannot hold are skipped with a message: unknown security roles (which login drops today as
 * well), group members and schedule users that do not exist, on-call roles whose membership group or
 * supervisor does not exist, a second contact of the same type, unknown contact and schedule types, and
 * values longer than their column. Names are matched exactly, without trimming, as OpenNMS matches them
 * today. The XSDs normally reject unknown types, but JaxbUtils reads a file without validation when it
 * cannot find the XSD.</p>
 *
 * <p>A user or group with one of the {@link ReservedNames} fails the task before anything is written: such an entry
 * is not dropped, since group members, on-call roles and destination paths refer to it by name.</p>
 *
 * <p>The task is critical: when it fails, the upgrade fails, so OpenNMS does not start without users. The admin
 * fixes the cause and runs the installer again, which copies the files.</p>
 *
 * <p>After the commit, the task moves users.xml, groups.xml, security-roles.properties and the two {@code .rpmsave}
 * files from {@code etc} to {@code etc_archive}: nothing reads them any more, and {@code opennms start} refuses to
 * start while {@code etc} holds {@code .rpmsave} files. It moves them also when it skips a database that has the
 * marker or already has user data. That covers files that an earlier version or a failed move left in {@code etc},
 * and a container that copies users.xml from its overlay on every start. A file is never overwritten: a file whose
 * content {@code etc_archive} already holds under its name is just removed from {@code etc}, and a file whose name
 * is taken gets the suffix {@code .yyyyMMddHHmmss} (and {@code -1}, {@code -2}... if that is taken too). On a failure
 * before the commit, nothing is moved, so the next install reads the files again.</p>
 *
 * <p>A file that cannot be moved (e.g. a root-owned {@code etc_archive}, or a read-only users.xml) is only a warning
 * once the marker is set: nothing reads the file any more, and the next install tries again. It still fails the task
 * when it is a {@code .rpmsave} file, as {@code opennms start} refuses to start with it in {@code etc}, and when the
 * marker is not set, as the default-users seed adds no users while the file is in {@code etc}.</p>
 */
public class UsersGroupsXmlMigratorOffline extends AbstractOnmsUpgrade {

    private static final String USERS_FILE_NAME = "users.xml";
    private static final String GROUPS_FILE_NAME = "groups.xml";
    private static final String SECURITY_ROLES_FILE_NAME = "security-roles.properties";
    private static final String RPMSAVE = ".rpmsave";
    private static final String ARCHIVE_DIR_NAME = "etc_archive";

    /** The files that are moved to etc_archive, in this order. */
    private static final List<String> ARCHIVED_FILE_NAMES = Arrays.asList(USERS_FILE_NAME, GROUPS_FILE_NAME,
            SECURITY_ROLES_FILE_NAME, USERS_FILE_NAME + RPMSAVE, GROUPS_FILE_NAME + RPMSAVE);

    private static final DateTimeFormatter ARCHIVE_SUFFIX = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final List<String> TABLES = Arrays.asList(
            "security_roles", "users", "user_contacts", "user_duty_schedules", "user_security_roles",
            "groups", "group_members", "group_duty_schedules",
            "oncall_roles", "oncall_schedules", "oncall_schedule_times", "usermgmt_initialized");

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

    public UsersGroupsXmlMigratorOffline() throws OnmsUpgradeException {
        super();
    }

    @Override
    public int getOrder() {
        // before DefaultUsersSeedOffline (19), which adds the default users only when no user data was copied
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
    public boolean isCritical() {
        // a failed copy leaves no users, so no one could log in
        return true;
    }

    /** Until the usermgmt_initialized marker is set, the files still have to be copied, or the seed has to run. */
    @Override
    public boolean isPending() throws OnmsUpgradeException {
        return !UserMgmtInitialized.isSet();
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
                archive(true);
                return;
            }
            final String existingData = existingData(connection);
            if (existingData != null) {
                log("The database already contains user data (%s), so nothing is copied from the files\n", existingData);
                UserMgmtInitialized.set(connection, getId());
                archive(true);
                return;
            }

            final List<String> customRoles = readSecurityRoles(etcFile(SECURITY_ROLES_FILE_NAME));
            final File usersFile = source(USERS_FILE_NAME);
            final File groupsFile = source(GROUPS_FILE_NAME);
            Userinfo userinfo = read(Userinfo.class, usersFile);
            Groupinfo groupinfo = read(Groupinfo.class, groupsFile);
            final boolean neitherFound = usersFile == null && groupsFile == null;
            if (neitherFound) {
                log("Neither %s nor %s was found, so no users or groups are copied from files\n", USERS_FILE_NAME, GROUPS_FILE_NAME);
            } else if (groupsFile == null) {
                log("%s not found; the default Admin group is used, as the package removed the unmodified file\n", GROUPS_FILE_NAME);
                groupinfo = UserMgmtDefaults.groups();
            } else if (usersFile == null) {
                log("%s not found; the default users admin and rtc are used, as the package removed the unmodified file\n", USERS_FILE_NAME);
                userinfo = UserMgmtDefaults.users();
            }
            checkNoReservedNames(userinfo, groupinfo);

            connection.setAutoCommit(false);
            final Import migration = new Import(connection, this::log);
            try {
                migration.securityRoles(customRoles);
                if (neitherFound && migration.copiedCustomSecurityRoles()) {
                    // the custom roles alone would make the next run skip this database as one with user data: if the
                    // seed did not run (it failed, or the installer was stopped), nobody could log in
                    log("Custom security roles were copied, so the default users admin and rtc and the group Admin are added with them\n");
                    userinfo = UserMgmtDefaults.users();
                    groupinfo = UserMgmtDefaults.groups();
                }
                migration.users(userinfo == null ? Collections.emptyList() : userinfo.getUsers());
                migration.groups(groupinfo == null ? Collections.emptyList() : groupinfo.getGroups());
                migration.onCallRoles(groupinfo == null ? Collections.emptyList() : groupinfo.getRoles());
                if (userinfo != null) {
                    // also with no users: the default-users seed must not add admin/admin to this database later.
                    // Without users.xml, groups.xml and custom security roles (a new installation), the seed adds them.
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
            // only after the commit: on a failure before it, the next install reads the files again
            archive(userinfo != null);
        } catch (final SQLException | RuntimeException e) {
            throw new OnmsUpgradeException("Can't copy users and groups to the database: " + e.getMessage(), e);
        }
    }

    /**
     * Fails the task on a user or group with one of the {@link ReservedNames}: the admin renames it in the files, so
     * nothing is dropped silently. The default-users seed adds no users while users.xml or users.xml.rpmsave exists,
     * and the files stay in etc, so the next install copies them.
     */
    private static void checkNoReservedNames(final Userinfo userinfo, final Groupinfo groupinfo) throws OnmsUpgradeException {
        final List<String> found = new ArrayList<>();
        if (userinfo != null) {
            userinfo.getUsers().stream().map(User::getUserId).filter(ReservedNames.USER_IDS::contains).distinct()
                    .forEach(name -> found.add("the user '" + name + "'"));
        }
        if (groupinfo != null) {
            groupinfo.getGroups().stream().map(Group::getName).filter(ReservedNames.GROUP_NAMES::contains).distinct()
                    .forEach(name -> found.add("the group '" + name + "'"));
            groupinfo.getRoles().stream().map(Role::getName).filter(ReservedNames.ONCALL_ROLE_NAMES::contains).distinct()
                    .forEach(name -> found.add("the on-call role '" + name + "'"));
        }
        if (!found.isEmpty()) {
            throw new OnmsUpgradeException("Nothing was copied: users must not be named " + String.join(", ", ReservedNames.USER_IDS)
                    + ", and groups must not be named " + String.join(", ", ReservedNames.GROUP_NAMES)
                    + ", and on-call roles must not be named " + String.join(", ", ReservedNames.ONCALL_ROLE_NAMES)
                    + ", but the files have " + String.join(", ", found)
                    + ". Rename them in users.xml and groups.xml (or their .rpmsave copies), and where the files refer to them (group members, on-call roles, "
                    + "destination paths in notifications.xml), then run the installer again.");
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

    /**
     * Moves the files that exist in {@code etc} to {@code etc_archive}, without overwriting a file there. A file whose
     * content is already archived under its name ({@code <name>} or {@code <name>.<yyyyMMddHHmmss>[-n]}) is just
     * removed: a container copies its overlay users.xml to {@code etc} on every start. Tries every file, then fails
     * if one that must be moved could not be (see the class comment).
     *
     * @param initialized whether the usermgmt_initialized marker is set
     */
    private void archive(final boolean initialized) throws OnmsUpgradeException {
        final Path archive = new File(getHomeDirectory(), ARCHIVE_DIR_NAME).toPath();
        final List<String> failures = new ArrayList<>();
        for (final String name : ARCHIVED_FILE_NAMES) {
            final Path source = etcFile(name).toPath();
            if (!Files.exists(source)) {
                continue;
            }
            try {
                Files.createDirectories(archive);
                final Path copy = archivedCopy(archive, name, source);
                if (copy != null) {
                    Files.delete(source);
                    log("Removed %s, as %s has the same content\n", source, copy);
                    continue;
                }
                final Path target = archiveTarget(archive, name, LocalDateTime.now());
                // without REPLACE_EXISTING: fails rather than overwrites
                Files.move(source, target);
                log("Moved %s to %s\n", source, target);
            } catch (final IOException | RuntimeException e) {
                final String failure = "Can't move '" + source + "' to '" + archive + "': " + e.getMessage();
                if (initialized && !name.endsWith(RPMSAVE)) {
                    log("Warning: %s. Nothing reads it any more, but it may hold password hashes: move or delete it. The next install tries again.\n", failure);
                } else {
                    log("%s\n", failure);
                    failures.add(failure);
                }
            }
        }
        if (!failures.isEmpty()) {
            throw new OnmsUpgradeException(String.join("; ", failures)
                    + (initialized ? ". OpenNMS does not start while etc holds .rpmsave files"
                                   : ". The default users are not added while the file is in etc")
                    + ": fix the cause and run the installer again.");
        }
    }

    /**
     * @return {@code <archive>/<name>}, or else {@code <name>.<yyyyMMddHHmmss>}, or else that name with the first free
     * suffix {@code -1}, {@code -2}...: two runs in the same second, or in the hour that a clock change repeats, must
     * not fail on a taken name
     */
    static Path archiveTarget(final Path archive, final String name, final LocalDateTime now) {
        Path target = archive.resolve(name);
        if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            return target;
        }
        final String stamped = name + "." + ARCHIVE_SUFFIX.format(now);
        target = archive.resolve(stamped);
        for (int i = 1; Files.exists(target, LinkOption.NOFOLLOW_LINKS); i++) {
            target = archive.resolve(stamped + "-" + i);
        }
        return target;
    }

    /** @return the archived copy of the file with the same content, or null if there is none */
    private static Path archivedCopy(final Path archive, final String name, final Path source) throws IOException {
        final Pattern copyName = Pattern.compile(Pattern.quote(name) + "(\\.\\d{14}(-\\d+)?)?");
        try (final DirectoryStream<Path> copies = Files.newDirectoryStream(archive, p -> copyName.matcher(p.getFileName().toString()).matches())) {
            for (final Path copy : copies) {
                if (Files.isRegularFile(copy) && Files.mismatch(source, copy) == -1L) {
                    return copy;
                }
            }
        }
        return null;
    }

    /** Nothing to undo: {@link #execute()} rolls its transaction back. */
    @Override
    public void rollback() throws OnmsUpgradeException {
    }

    private File etcFile(final String name) {
        return new File(getHomeDirectory(), "etc" + File.separator + name);
    }

    /**
     * @return {@code etc/<name>}, or else {@code etc/<name>.rpmsave}, or null if neither exists
     */
    private File source(final String name) {
        final File plain = etcFile(name);
        final File rpmsave = etcFile(name + RPMSAVE);
        if (plain.exists()) {
            if (rpmsave.exists()) {
                log("Both %s and %s exist: %s is copied, and %s is archived unread\n", plain, rpmsave, plain, rpmsave);
            }
            return plain;
        }
        if (rpmsave.exists()) {
            log("%s not found; %s is copied\n", plain, rpmsave);
            return rpmsave;
        }
        return null;
    }

    /** @return the file read, or null if the file is null */
    private static <T> T read(final Class<T> type, final File file) throws OnmsUpgradeException {
        if (file == null) {
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
        } catch (final IOException | IllegalArgumentException e) {
            // IllegalArgumentException: a malformed Unicode escape
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

    /** Where {@link Import} writes its messages: the log of the task that runs it. */
    @FunctionalInterface
    interface Log {
        void log(String format, Object... args);
    }

    /**
     * One run of the copy, on one connection in one transaction. Also used by {@link DefaultUsersSeedOffline}, to
     * insert the default users and groups.
     */
    static class Import {
        private final Connection m_connection;
        private final Log m_log;

        // name -> id, to resolve references; the database has no user data yet, only the built-in roles
        private final Map<String, Integer> m_securityRoleIds;
        private final Map<String, Integer> m_userIds = new HashMap<>();
        private final Map<String, Integer> m_groupIds = new HashMap<>();

        private static final String CUSTOM_SECURITY_ROLES = "custom security roles";

        private final Map<String, Integer> m_counts = new LinkedHashMap<>();
        private int m_skipped;
        private int m_warnings;

        Import(final Connection connection, final Log log) throws SQLException {
            m_connection = connection;
            m_log = log;
            m_securityRoleIds = loadIds("SELECT name, id FROM security_roles");
        }

        void securityRoles(final List<String> names) throws SQLException {
            for (final String name : new LinkedHashSet<>(names)) {
                // a built-in role in security-roles.properties is no custom role
                if (!m_securityRoleIds.containsKey(name) && fits(name, SECURITY_ROLE_NAME_LENGTH, "security role", name, "security-roles.properties")) {
                    m_securityRoleIds.put(name, insert("INSERT INTO security_roles (id, name, builtin, created_time, last_modified) "
                            + "VALUES (nextval('security_roles_id_seq'), ?, false, NOW(), NOW()) RETURNING id", name));
                    count(CUSTOM_SECURITY_ROLES);
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
                final String password = user.getPassword() == null ? null : user.getPassword().getEncryptedPassword();
                if (!fits(username, USERNAME_LENGTH, "user", username, "the user name") || !fits(password, PASSWORD_LENGTH, "user", username, "the password")) {
                    continue;
                }
                final int userId = insert("INSERT INTO users (id, username, full_name, comments, password_hash, password_salted, tui_pin, time_zone_id, created_time, last_modified) "
                                + "VALUES (nextval('users_id_seq'), ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) RETURNING id",
                        username,
                        valueOrNull(HtmlEncoding.decodeHtmlEncoded(user.getFullName().orElse(null)), FULL_NAME_LENGTH, "user", username, "full-name"),
                        user.getUserComments().orElse(null),
                        password,
                        user.getPassword() != null && Boolean.TRUE.equals(user.getPassword().getSalt()),
                        valueOrNull(user.getTuiPin().orElse(null), TUI_PIN_LENGTH, "user", username, "tui-pin"),
                        valueOrNull(user.getTimeZoneId().map(ZoneId::getId).orElse(null), TIME_ZONE_ID_LENGTH, "user", username, "time-zone-id"));
                m_userIds.put(username, userId);
                count("users");

                contacts(userId, username, user.getContacts());
                dutySchedules("INSERT INTO user_duty_schedules (user_id, position, schedule) VALUES (?, ?, ?)", userId, "user", username, user.getDutySchedules());
                userSecurityRoles(userId, username, user.getRoles());
            }
        }

        private void contacts(final int userId, final String username, final List<Contact> contacts) throws SQLException {
            final Set<String> types = new HashSet<>();
            for (final Contact contact : contacts) {
                final String type = contact.getType();
                final String info = HtmlEncoding.decodeHtmlEncoded(contact.getInfo().orElse(null));
                final String provider = HtmlEncoding.decodeHtmlEncoded(contact.getServiceProvider().orElse(null));
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
                if (!fits(name, GROUP_NAME_LENGTH, "group", name, "the name")) {
                    continue;
                }
                final int groupId = insert("INSERT INTO groups (id, name, default_map, comments, created_time, last_modified) "
                                + "VALUES (nextval('groups_id_seq'), ?, ?, ?, NOW(), NOW()) RETURNING id",
                        name,
                        valueOrNull(group.getDefaultMap().orElse(null), DEFAULT_MAP_LENGTH, "group", name, "default-map"),
                        group.getComments().orElse(null));
                m_groupIds.put(name, groupId);
                count("groups");

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
                count("on-call roles");

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

        /** @return true if {@link #securityRoles} inserted a role */
        boolean copiedCustomSecurityRoles() {
            return m_counts.containsKey(CUSTOM_SECURITY_ROLES);
        }

        void logSummary() {
            for (final Map.Entry<String, Integer> entry : m_counts.entrySet()) {
                m_log.log("%s: %d copied\n", entry.getKey(), entry.getValue());
            }
            if (m_skipped > 0 || m_warnings > 0) {
                m_log.log("Entries skipped: %d, warnings: %d; see the messages above\n", m_skipped, m_warnings);
            }
        }

        private void count(final String what) {
            m_counts.merge(what, 1, Integer::sum);
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
            m_log.log("Skipped " + format + "\n", args);
        }

        private void warn(final String format, final Object... args) {
            m_warnings++;
            m_log.log("Warning: " + format + "\n", args);
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
