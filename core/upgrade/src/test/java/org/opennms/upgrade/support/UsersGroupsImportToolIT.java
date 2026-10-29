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
package org.opennms.upgrade.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.upgrade.implementations.DefaultUsersSeedOffline;
import org.opennms.upgrade.support.UsersGroupsImportTool.Prompt;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

/** The tool on a database seeded with admin, rtc and the group Admin, as after an upgrade without users.xml. */
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
public class UsersGroupsImportToolIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    private static final String QUESTION_END = "Overwrite these 1 existing entries? [y/N]";

    @Rule
    public TemporaryFolder m_home = new TemporaryFolder();

    private final List<String> m_questions = new ArrayList<>();
    private ByteArrayOutputStream m_out;
    private ByteArrayOutputStream m_err;

    private JdbcTemplate m_jdbc;
    private String m_previousHome;
    private File m_users;
    private File m_groups;

    @Override
    public void setTemporaryDatabase(final TemporaryDatabase database) {
        m_jdbc = database.getJdbcTemplate();
    }

    @Before
    public void setUp() throws Exception {
        m_previousHome = System.getProperty("opennms.home");
        System.setProperty("opennms.home", m_home.getRoot().getAbsolutePath());
        final File etc = m_home.newFolder("etc");
        // AbstractOnmsUpgrade reads these in its constructor; the empty opennms.properties means the database store
        write(new File(etc, "opennms.properties"), "");
        write(new File(etc, "rrd-configuration.properties"), "");
        final File files = m_home.newFolder("import");
        m_users = new File(files, "users.xml");
        m_groups = new File(files, "groups.xml");

        final DefaultUsersSeedOffline seed = new DefaultUsersSeedOffline();
        seed.preExecute();
        seed.execute();
        seed.postExecute();
    }

    @After
    public void tearDown() {
        if (m_previousHome == null) {
            System.clearProperty("opennms.home");
        } else {
            System.setProperty("opennms.home", m_previousHome);
        }
    }

    @Test
    public void testImportSkipsExistingAndPrintsReport() throws Exception {
        writeUsers(user("admin", "Someone Else", "ROLE_USER") + user("bob", "Bob", "ROLE_USER"));

        assertEquals(err(), 0, run(this::unexpected, "--users", m_users.getPath()));

        assertTrue(out(), lines(out()).containsAll(List.of("Existing entries skipped:", "  users: admin",
                "users: 1 imported, 0 overwritten, 1 existing skipped, 0 invalid skipped")));
        assertEquals("Administrator", fullName("admin"));
        assertEquals("Bob", fullName("bob"));
        assertEquals("", err());
    }

    @Test
    public void testOverwriteAsksOnceAndYesWrites() throws Exception {
        writeUsers(user("admin", "New Admin", "ROLE_ADMIN") + user("bob", "Bob", "ROLE_USER"));

        assertEquals(err(), 0, run(answer("Yes"), "--users", m_users.getPath(), "--overwrite"));

        assertEquals(1, m_questions.size());
        final String question = m_questions.get(0);
        assertTrue(question, question.contains("  users: admin\n"));
        assertFalse(question, question.contains("bob"));
        assertTrue(question, question.endsWith(QUESTION_END));
        assertEquals("New Admin", fullName("admin"));
        assertTrue(out(), lines(out()).containsAll(List.of("Existing entries overwritten:", "  users: admin",
                "users: 1 imported, 1 overwritten, 0 existing skipped, 0 invalid skipped")));
    }

    @Test
    public void testOverwriteAnswerNoWritesNothing() throws Exception {
        writeUsers(user("admin", "New Admin", "ROLE_ADMIN") + user("bob", "Bob", "ROLE_USER"));
        final List<Object> before = snapshot();

        for (final String answer : List.of("n", "")) {
            assertEquals(answer, 3, run(answer(answer), "--users", m_users.getPath(), "--overwrite"));
            assertEquals(answer, before, snapshot());
        }
        assertEquals(2, m_questions.size());
    }

    @Test
    public void testOverwriteWithoutConsoleExits3() throws Exception {
        writeUsers(user("admin", "New Admin", "ROLE_ADMIN"));
        final List<Object> before = snapshot();

        assertEquals(3, run(answer(null), "--users", m_users.getPath(), "--overwrite"));

        assertTrue(err(), err().contains("--quiet"));
        assertEquals(before, snapshot());
    }

    @Test
    public void testQuietOverwritesWithoutPrompt() throws Exception {
        writeUsers(user("admin", "New Admin", "ROLE_ADMIN"));

        assertEquals(err(), 0, run(this::unexpected, "--users", m_users.getPath(), "--overwrite", "--quiet"));

        assertEquals("New Admin", fullName("admin"));
    }

    @Test
    public void testOverwriteWithNothingExistingDoesNotPrompt() throws Exception {
        writeUsers(user("bob", "Bob", "ROLE_USER"));

        assertEquals(err(), 0, run(this::unexpected, "--users", m_users.getPath(), "--overwrite"));

        assertEquals("Bob", fullName("bob"));
    }

    @Test
    public void testDryRunChangesNothing() throws Exception {
        writeUsers(user("admin", "New Admin", "ROLE_ADMIN") + user("bob", "Bob", "ROLE_NOPE") + user("x".repeat(257), "Too Long", "ROLE_USER"));
        writeGroups("<groups><group><name>Admin</name><user>admin</user><user>bob</user></group>"
                + "<group><name>Dev</name><user>bob</user><user>ghost</user></group></groups>");
        final List<Object> before = snapshot();

        assertEquals(err(), 0, run(this::unexpected, "--users", m_users.getPath(), "--groups", m_groups.getPath(), "--overwrite", "--dry-run"));
        final String dryRun = out();

        assertEquals(before, snapshot());
        assertTrue(dryRun, dryRun.startsWith("Dry run"));
        assertTrue(dryRun, lines(dryRun).contains("Existing entries that would be overwritten:"));

        assertEquals(err(), 0, run(this::unexpected, "--users", m_users.getPath(), "--groups", m_groups.getPath(), "--overwrite", "--quiet"));
        final List<String> report = report(out());
        assertTrue(report.toString(), report.containsAll(List.of("  users: admin", "  groups: Admin",
                "Skipped user 'bob': security role 'ROLE_NOPE' does not exist", "Skipped group 'Dev': member 'ghost' is not a user",
                "users: 1 imported, 1 overwritten, 0 existing skipped, 1 invalid skipped", "groups: 1 imported, 1 overwritten, 0 existing skipped, 0 invalid skipped",
                "Entries skipped: 3, warnings: 0; see the messages above")));
        assertEquals(report, report(dryRun));
    }

    @Test
    public void testEntryAddedAfterConfirmationAborts() throws Exception {
        writeUsers(user("admin", "New Admin", "ROLE_ADMIN") + user("bob", "Bob", "ROLE_USER"));
        final List<List<Object>> before = new ArrayList<>();

        final int exitCode = run(question -> {
            m_questions.add(question);
            // through a connection of its own, as someone else would
            m_jdbc.update("INSERT INTO users (id, username, full_name, password_hash, password_salted, created_time, last_modified) "
                    + "VALUES (nextval('users_id_seq'), 'bob', 'Other Bob', 'x', true, NOW(), NOW())");
            before.add(snapshot());
            return "y";
        }, "--users", m_users.getPath(), "--overwrite");

        assertEquals(1, exitCode);
        assertEquals(1, m_questions.size());
        assertTrue(err(), err().contains("bob"));
        assertEquals(before.get(0), snapshot());
        assertEquals("Other Bob", fullName("bob"));
        assertEquals("Administrator", fullName("admin"));
    }

    @Test
    public void testAdministratorProblemRollsBack() throws Exception {
        writeUsers(user("admin", "New Admin", "ROLE_USER") + user("bob", "Bob", "ROLE_USER"));
        final List<Object> before = snapshot();

        assertEquals(1, run(this::unexpected, "--users", m_users.getPath(), "--overwrite", "--quiet"));
        assertTrue(err(), err().contains("The admin user must keep the ROLE_ADMIN role."));
        assertTrue(err(), err().contains("takes it from all who hold it: admin"));
        assertEquals(before, snapshot());

        assertEquals(1, run(this::unexpected, "--users", m_users.getPath(), "--overwrite", "--dry-run"));
        assertTrue(err(), err().contains("The admin user must keep the ROLE_ADMIN role."));
        assertEquals(before, snapshot());
    }

    @Test
    public void testNoDecodeOption() throws Exception {
        // "a &amp; b" once the XML is parsed
        writeUsers(user("dan", "a &amp;amp; b", "ROLE_USER"));

        assertEquals(err(), 0, run(this::unexpected, "--users", m_users.getPath(), "--no-decode"));

        assertEquals("a &amp; b", fullName("dan"));
    }

    @Test
    public void testDecodesByDefault() throws Exception {
        writeUsers(user("dan", "a &amp;amp; b", "ROLE_USER"));

        assertEquals(err(), 0, run(this::unexpected, "--users", m_users.getPath()));

        assertEquals("a & b", fullName("dan"));
    }

    private int run(final Prompt prompt, final String... args) {
        m_out = new ByteArrayOutputStream();
        m_err = new ByteArrayOutputStream();
        final UsersGroupsImportTool tool = new UsersGroupsImportTool(m_jdbc.getDataSource(), m_home.getRoot(), prompt,
                new PrintStream(m_out, true, StandardCharsets.UTF_8), new PrintStream(m_err, true, StandardCharsets.UTF_8));
        return tool.run(args);
    }

    private Prompt answer(final String answer) {
        return question -> {
            m_questions.add(question);
            return answer;
        };
    }

    private String unexpected(final String question) {
        throw new AssertionError("asked " + question);
    }

    private String out() {
        return m_out.toString(StandardCharsets.UTF_8);
    }

    private String err() {
        return m_err == null ? "" : m_err.toString(StandardCharsets.UTF_8);
    }

    private static List<String> lines(final String text) {
        return Arrays.asList(text.split("\n"));
    }

    /** The lines that a dry run and a real run have in common: the messages, the existing entries and the summary. */
    private static List<String> report(final String text) {
        return lines(text).stream()
                .filter(line -> !line.startsWith("Dry run") && !line.startsWith("Existing entries") && !line.startsWith("Summary")
                        && !line.startsWith("The import"))
                .collect(Collectors.toList());
    }

    /** The row counts of the user tables and the versions of their rows, which every change moves. */
    private List<Object> snapshot() {
        final List<Object> snapshot = new ArrayList<>();
        for (final String table : List.of("users", "user_contacts", "user_duty_schedules", "user_security_roles", "groups", "group_members",
                "group_duty_schedules", "oncall_roles", "oncall_schedules", "oncall_schedule_times", "security_roles")) {
            snapshot.add(table + ": " + m_jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class));
        }
        for (final String table : List.of("users", "groups", "oncall_roles", "security_roles")) {
            snapshot.add(m_jdbc.queryForList("SELECT id, version FROM " + table + " ORDER BY id"));
        }
        snapshot.add(m_jdbc.queryForList("SELECT name, version FROM usermgmt_version ORDER BY name"));
        return snapshot;
    }

    private String fullName(final String username) {
        return m_jdbc.queryForObject("SELECT full_name FROM users WHERE username = ?", String.class, username);
    }

    private static String user(final String username, final String fullName, final String role) {
        return "<user><user-id>" + username + "</user-id><full-name>" + fullName + "</full-name><password salt=\"true\">pw</password>"
                + "<role>" + role + "</role></user>";
    }

    private void writeUsers(final String users) throws Exception {
        write(m_users, "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"><users>" + users + "</users></userinfo>");
    }

    private void writeGroups(final String content) throws Exception {
        write(m_groups, "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\">" + content + "</groupinfo>");
    }

    private static void write(final File file, final String content) throws Exception {
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }
}
