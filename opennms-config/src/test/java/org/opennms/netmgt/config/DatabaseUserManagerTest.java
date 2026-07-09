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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.Password;
import org.opennms.netmgt.config.users.User;

public class DatabaseUserManagerTest {

    private Connection conn;
    private DatabaseUserManager manager;

    @Before
    public void setUp() throws Exception {
        Class.forName("org.h2.Driver");
        // Each test gets its own named in-memory DB so tests don't bleed into each other
        final String dbName = "users_test_" + Thread.currentThread().getId();
        conn = DriverManager.getConnection("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
        createSchema(conn);

        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
        DataSourceFactory.setInstance(ds);

        manager = new DatabaseUserManager(mock(GroupManager.class));
        manager.reload();  // initializes m_users from empty DB
    }

    @After
    public void tearDown() throws Exception {
        try (Statement s = conn.createStatement()) {
            s.execute("DROP ALL OBJECTS");
        }
        conn.close();
        DataSourceFactory.close();
    }

    @Test
    public void testSaveAndReloadUser() throws Exception {
        final User user = buildUser("alice", "Alice Smith", "ROLE_USER", "ROLE_ADMIN");
        addEmail(user, "alice@example.com");
        user.addDutySchedule("MoTuWeThFr800-1700");

        manager.saveUser("alice", user);

        manager.reload();
        final User loaded = manager.getUser("alice");

        assertNotNull(loaded);
        assertEquals("alice", loaded.getUserId());
        assertEquals("Alice Smith", loaded.getFullName().orElse(null));
        assertTrue(loaded.getRoles().contains("ROLE_USER"));
        assertTrue(loaded.getRoles().contains("ROLE_ADMIN"));
        assertEquals("alice@example.com", emailOf(loaded));
        assertEquals(1, loaded.getDutySchedules().size());
        assertEquals("MoTuWeThFr800-1700", loaded.getDutySchedules().get(0));
    }

    @Test
    public void testDeleteUserCascadesToChildTables() throws Exception {
        manager.saveUser("bob", buildUser("bob", "Bob Jones", "ROLE_USER"));
        manager.reload();
        assertNotNull(manager.getUser("bob"));

        manager.deleteUser("bob");
        manager.reload();
        assertNull(manager.getUser("bob"));

        // All child rows must be gone
        assertRowCount(0, "user_roles", "bob");
        assertRowCount(0, "user_contacts", "bob");
        assertRowCount(0, "user_duty_schedules", "bob");
    }

    @Test
    public void testRenameUser() throws Exception {
        manager.saveUser("old", buildUser("old", "Original", "ROLE_USER"));
        manager.reload();

        manager.renameUser("old", "new");
        manager.reload();

        assertNull(manager.getUser("old"));
        assertNotNull(manager.getUser("new"));
        assertEquals("Original", manager.getUser("new").getFullName().orElse(null));
    }

    @Test
    public void testPasswordRoundTrip() throws Exception {
        final User user = buildUser("carol", "Carol", "ROLE_USER");
        manager.saveUser("carol", user);

        final String rawPw = "s3cr3t";
        manager.setUnencryptedPassword("carol", rawPw);

        manager.reload();
        assertTrue(manager.comparePasswords("carol", rawPw));
    }

    @Test
    public void testSaveUsersReplacesAll() throws Exception {
        manager.saveUser("u1", buildUser("u1", "User 1", "ROLE_USER"));
        manager.saveUser("u2", buildUser("u2", "User 2", "ROLE_USER"));
        manager.reload();
        assertEquals(2, manager.getUsers().size());

        // saveUsers() with only u3 should replace both u1 and u2
        manager.saveUsers(Arrays.asList(buildUser("u3", "User 3", "ROLE_ADMIN")));
        manager.reload();

        final Map<String, User> users = manager.getUsers();
        assertEquals(1, users.size());
        assertTrue(users.containsKey("u3"));
        assertFalse(users.containsKey("u1"));
    }

    @Test
    public void testDuplicateContactTypeDeduplication() throws Exception {
        final User user = buildUser("dup", "Dup User", "ROLE_USER");
        // Add two email contacts — only last one should persist
        final Contact c1 = new Contact();
        c1.setType("email");
        c1.setInfo("first@example.com");
        user.addContact(c1);
        final Contact c2 = new Contact();
        c2.setType("email");
        c2.setInfo("second@example.com");
        user.addContact(c2);

        manager.saveUser("dup", user);  // should not throw UNIQUE constraint violation
        manager.reload();

        final User loaded = manager.getUser("dup");
        final long emailCount = loaded.getContacts().stream()
                .filter(c -> "email".equals(c.getType()))
                .count();
        assertEquals(1, emailCount);
        assertEquals("second@example.com", emailOf(loaded));
    }

    @Test
    public void testMigrateFromXmlOnEmptyDatabase() throws Exception {
        final File tmpHome = Files.createTempDirectory("opennms-test").toFile();
        final File etc = new File(tmpHome, "etc");
        etc.mkdirs();

        try (FileWriter fw = new FileWriter(new File(etc, "users.xml"))) {
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<userinfo>\n"
                + "  <users>\n"
                + "    <user>\n"
                + "      <user-id>migrated</user-id>\n"
                + "      <full-name>Migrated User</full-name>\n"
                + "      <password salt=\"true\">abc123</password>\n"
                + "    </user>\n"
                + "  </users>\n"
                + "</userinfo>\n");
        }

        final String prev = System.getProperty("opennms.home");
        System.setProperty("opennms.home", tmpHome.getAbsolutePath());
        try {
            // Reload with empty DB triggers XML migration
            manager.reload();
            assertNotNull(manager.getUser("migrated"));
            assertEquals("Migrated User", manager.getUser("migrated").getFullName().orElse(null));
        } finally {
            if (prev != null) System.setProperty("opennms.home", prev);
            else System.clearProperty("opennms.home");
        }
    }

    @Test
    public void testMultipleUsersWithContactsAndDutySchedules() throws Exception {
        for (int i = 0; i < 5; i++) {
            final User u = buildUser("user" + i, "User " + i, "ROLE_USER");
            addEmail(u, "user" + i + "@example.com");
            u.addDutySchedule("MoTuWeThFr800-1700");
            manager.saveUser("user" + i, u);
        }

        manager.reload();
        assertEquals(5, manager.getUsers().size());

        for (int i = 0; i < 5; i++) {
            final User loaded = manager.getUser("user" + i);
            assertEquals("user" + i + "@example.com", emailOf(loaded));
            assertEquals(1, loaded.getDutySchedules().size());
        }
    }

    @Test
    public void testIsUserOnDuty() throws Exception {
        final User u = buildUser("oncall", "On Call", "ROLE_USER");
        u.addDutySchedule("MoTuWeThFrSaSu0000-2359");  // always on duty
        manager.saveUser("oncall", u);

        manager.reload();
        assertTrue(manager.isUserOnDuty("oncall", java.util.Calendar.getInstance()));
    }

    @Test
    public void testGetFileSize() throws Exception {
        assertEquals(0L, manager.getFileSize());
    }

    @Test
    public void testIsUpdateNeededAlwaysFalse() {
        assertFalse(manager.isUpdateNeeded());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static User buildUser(final String id, final String fullName, final String... roles) {
        final User user = new User();
        user.setUserId(id);
        user.setFullName(fullName);
        final Password pass = new Password();
        pass.setEncryptedPassword("hashed_" + id);
        pass.setSalt(true);
        user.setPassword(pass);
        for (final String role : roles) {
            user.addRole(role);
        }
        return user;
    }

    private static void addEmail(final User user, final String email) {
        final Contact c = new Contact();
        c.setType("email");
        c.setInfo(email);
        user.addContact(c);
    }

    private static String emailOf(final User user) {
        return user.getContacts().stream()
                .filter(c -> "email".equals(c.getType()))
                .findFirst()
                .flatMap(Contact::getInfo)
                .orElse(null);
    }

    private void assertRowCount(final int expected, final String table, final String userId) throws Exception {
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT COUNT(*) FROM " + table + " WHERE user_id = '" + userId + "'")) {
            rs.next();
            assertEquals(expected, rs.getInt(1));
        }
    }

    private static void createSchema(final Connection c) throws Exception {
        final Statement s = c.createStatement();
        s.execute(
            "CREATE TABLE users ("
            + "  user_id VARCHAR(256) PRIMARY KEY NOT NULL,"
            + "  full_name VARCHAR(256),"
            + "  user_comments TEXT,"
            + "  password VARCHAR(512) NOT NULL,"
            + "  password_salt BOOLEAN NOT NULL DEFAULT TRUE,"
            + "  tui_pin VARCHAR(32),"
            + "  time_zone_id VARCHAR(64),"
            + "  created_at TIMESTAMP NOT NULL DEFAULT NOW(),"
            + "  updated_at TIMESTAMP NOT NULL DEFAULT NOW()"
            + ")"
        );
        s.execute(
            "CREATE TABLE user_roles ("
            + "  id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
            + "  user_id VARCHAR(256) NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,"
            + "  role VARCHAR(128) NOT NULL,"
            + "  UNIQUE (user_id, role)"
            + ")"
        );
        s.execute(
            "CREATE TABLE user_contacts ("
            + "  id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
            + "  user_id VARCHAR(256) NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,"
            + "  contact_type VARCHAR(64) NOT NULL,"
            + "  contact_info TEXT,"
            + "  service_provider TEXT,"
            + "  UNIQUE (user_id, contact_type)"
            + ")"
        );
        s.execute(
            "CREATE TABLE user_duty_schedules ("
            + "  id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
            + "  user_id VARCHAR(256) NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,"
            + "  schedule VARCHAR(128) NOT NULL"
            + ")"
        );
        s.close();
    }
}
