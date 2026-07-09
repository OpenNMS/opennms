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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.DutySchedule;
import org.opennms.netmgt.config.users.Password;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database-backed replacement for {@link UserFactory}.
 *
 * <p>Reads and writes all user data from the {@code users} table (and child
 * tables {@code user_roles}, {@code user_contacts}, {@code user_duty_schedules})
 * created by Liquibase changeset 36.0.2.  The {@code users.xml} file is not
 * read or written.</p>
 *
 * <p>The in-memory cache inherited from {@link UserManager} is still used so
 * that hot-path reads (duty-schedule checks, contact lookups) do not hit the
 * database.  The cache is populated once at startup via {@link #reload()} and
 * is refreshed whenever {@link #saveXML} is called (i.e. after any write
 * operation).</p>
 */
public class DatabaseUserManager extends UserManager {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseUserManager.class);

    private static UserManager instance;
    private static boolean initialized = false;

    private volatile long m_lastModified = 0L;

    protected DatabaseUserManager(final GroupManager groupManager) {
        super(groupManager);
    }

    // -------------------------------------------------------------------------
    // Singleton lifecycle (mirrors UserFactory pattern)
    // -------------------------------------------------------------------------

    public static synchronized void init() throws IOException, FileNotFoundException {
        if (instance == null || !initialized) {
            GroupFactory.init();
            instance = new DatabaseUserManager(GroupFactory.getInstance());
            instance.reload();
            initialized = true;
            // Register as the UserFactory singleton so legacy code that calls
            // UserFactory.init() / UserFactory.getInstance() gets this instance.
            // UserFactory.setInstance() sets UserFactory.initialized = true, which
            // makes subsequent UserFactory.init() calls a no-op.
            UserFactory.setInstance(instance);
        }
    }

    public static synchronized UserManager getInstance() {
        return instance;
    }

    public static synchronized void setInstance(final UserManager mgr) {
        initialized = true;
        instance = mgr;
    }

    // -------------------------------------------------------------------------
    // UserManager abstract methods
    // -------------------------------------------------------------------------

    @Override
    public synchronized void reload() throws IOException, FileNotFoundException {
        loadFromDatabase();
        m_lastModified = System.currentTimeMillis();
    }

    /** No-op: the database is always the authoritative source; no file to watch. */
    @Override
    public void doUpdate() throws IOException {
    }

    /** Always false: writes go directly to the DB via {@link #saveXML}. */
    @Override
    public boolean isUpdateNeeded() {
        return false;
    }

    @Override
    public long getLastModified() {
        return m_lastModified;
    }

    /** Not meaningful for a database backend; returns 0. */
    @Override
    public long getFileSize() {
        return 0;
    }

    /**
     * Writes the current in-memory user map directly to the database, bypassing
     * JAXB marshalling (which fails when the user list is empty due to XSD constraints).
     */
    @Override
    protected void _saveCurrent() throws Exception {
        final DataSource ds = DataSourceFactory.getInstance();
        try (Connection conn = ds.getConnection()) {
            final boolean prevAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users")) {
                    ps.executeUpdate();
                }
                for (final User user : m_users.values()) {
                    insertUser(conn, user);
                }
                conn.commit();
                m_lastModified = System.currentTimeMillis();
                LOG.debug("Saved {} users to database", m_users.size());
            } catch (final Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(prevAutoCommit);
            }
        }
    }

    /**
     * Receives a fully-serialized user list (from the REST API) and syncs it
     * atomically to the database.
     */
    @Override
    protected void saveXML(final String writerString) throws IOException {
        if (writerString == null) return;
        try {
            final Userinfo userinfo = JaxbUtils.unmarshal(Userinfo.class, new StringReader(writerString));
            final DataSource ds = DataSourceFactory.getInstance();
            try (Connection conn = ds.getConnection()) {
                final boolean prevAutoCommit = conn.getAutoCommit();
                conn.setAutoCommit(false);
                try {
                    // Replace all users atomically; child rows cascade on DELETE.
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users")) {
                        ps.executeUpdate();
                    }
                    for (final User user : userinfo.getUsers()) {
                        insertUser(conn, user);
                    }
                    conn.commit();
                    m_lastModified = System.currentTimeMillis();
                    LOG.debug("Saved {} users to database", userinfo.getUsers().size());
                } catch (final Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(prevAutoCommit);
                }
            }
        } catch (final Exception e) {
            throw new IOException("Failed to save users to database", e);
        }
    }

    // -------------------------------------------------------------------------
    // Database helpers
    // -------------------------------------------------------------------------

    private void loadFromDatabase() throws IOException {
        final DataSource ds = DataSourceFactory.getInstance();
        try (Connection conn = ds.getConnection()) {
            final Map<String, User> newUsers = new TreeMap<>();

            // Core user fields
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT user_id, full_name, user_comments, password, password_salt, tui_pin, time_zone_id FROM users");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final User user = new User();
                    user.setUserId(rs.getString("user_id"));
                    final String fullName = rs.getString("full_name");
                    if (fullName != null) user.setFullName(fullName);
                    final String comments = rs.getString("user_comments");
                    if (comments != null) user.setUserComments(comments);
                    final Password pass = new Password();
                    pass.setEncryptedPassword(rs.getString("password"));
                    pass.setSalt(rs.getBoolean("password_salt"));
                    user.setPassword(pass);
                    final String tuiPin = rs.getString("tui_pin");
                    if (tuiPin != null) user.setTuiPin(tuiPin);
                    final String tzId = rs.getString("time_zone_id");
                    if (tzId != null) user.setTimeZoneId(ZoneId.of(tzId));
                    newUsers.put(user.getUserId(), user);
                }
            }

            // Roles
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT user_id, role FROM user_roles ORDER BY user_id");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final User user = newUsers.get(rs.getString("user_id"));
                    if (user != null) user.addRole(rs.getString("role"));
                }
            }

            // Contacts (email, pager, phone, xmpp, etc.)
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT user_id, contact_type, contact_info, service_provider FROM user_contacts ORDER BY user_id");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final User user = newUsers.get(rs.getString("user_id"));
                    if (user != null) {
                        final Contact contact = new Contact();
                        contact.setType(rs.getString("contact_type"));
                        final String info = rs.getString("contact_info");
                        if (info != null) contact.setInfo(info);
                        final String provider = rs.getString("service_provider");
                        if (provider != null) contact.setServiceProvider(provider);
                        user.addContact(contact);
                    }
                }
            }

            // Duty schedules
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT user_id, schedule FROM user_duty_schedules ORDER BY user_id");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final User user = newUsers.get(rs.getString("user_id"));
                    if (user != null) user.addDutySchedule(rs.getString("schedule"));
                }
            }

            m_users = newUsers;
            rebuildDutySchedules();
            LOG.debug("Loaded {} users from database", newUsers.size());

        } catch (final SQLException e) {
            throw new IOException("Failed to load users from database", e);
        }
    }

    private void insertUser(final Connection conn, final User user) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (user_id, full_name, user_comments, password, password_salt, tui_pin, time_zone_id)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getFullName().orElse(null));
            ps.setString(3, user.getUserComments().orElse(null));
            ps.setString(4, user.getPassword().getEncryptedPassword());
            ps.setBoolean(5, user.getPassword().getSalt());
            ps.setString(6, user.getTuiPin().orElse(null));
            ps.setString(7, user.getTimeZoneId().map(ZoneId::getId).orElse(null));
            ps.executeUpdate();
        }

        for (final String role : user.getRoles()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_roles (user_id, role) VALUES (?, ?)")) {
                ps.setString(1, user.getUserId());
                ps.setString(2, role);
                ps.executeUpdate();
            }
        }

        // Deduplicate contacts by type: last value wins, matching UserManager._setContact() behavior.
        // The user_contacts table has a UNIQUE(user_id, contact_type) constraint.
        final LinkedHashMap<String, Contact> contactsByType = new LinkedHashMap<>();
        for (final Contact contact : user.getContacts()) {
            contactsByType.put(contact.getType(), contact);
        }
        for (final Contact contact : contactsByType.values()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_contacts (user_id, contact_type, contact_info, service_provider)"
                    + " VALUES (?, ?, ?, ?)")) {
                ps.setString(1, user.getUserId());
                ps.setString(2, contact.getType());
                ps.setString(3, contact.getInfo().orElse(null));
                ps.setString(4, contact.getServiceProvider().orElse(null));
                ps.executeUpdate();
            }
        }

        for (final String schedule : user.getDutySchedules()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_duty_schedules (user_id, schedule) VALUES (?, ?)")) {
                ps.setString(1, user.getUserId());
                ps.setString(2, schedule);
                ps.executeUpdate();
            }
        }
    }

    // Replicates UserManager._buildDutySchedules() which is private.
    private void rebuildDutySchedules() {
        m_dutySchedules = new HashMap<>();
        for (final Map.Entry<String, User> entry : m_users.entrySet()) {
            final User user = entry.getValue();
            if (!user.getDutySchedules().isEmpty()) {
                final List<DutySchedule> dutyList = new ArrayList<>();
                for (final String duty : user.getDutySchedules()) {
                    dutyList.add(new DutySchedule(duty));
                }
                m_dutySchedules.put(entry.getKey(), dutyList);
            }
        }
    }
}
