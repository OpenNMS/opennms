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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.model.usermgmt.ContactType;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.OnCallSchedule;
import org.opennms.netmgt.model.usermgmt.OnCallScheduleTime;
import org.opennms.netmgt.model.usermgmt.SecurityRole;
import org.opennms.netmgt.model.usermgmt.SecurityRoles;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

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
public class UsersGroupsXmlMigratorOfflineIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    private static final List<String> TABLES = Arrays.asList(
            "security_roles", "users", "user_contacts", "user_duty_schedules", "user_security_roles",
            "groups", "group_members", "group_duty_schedules",
            "oncall_roles", "oncall_schedules", "oncall_schedule_times");

    private static final File FIXTURES = new File("src/test/resources/usersgroups/etc");

    @Rule
    public TemporaryFolder m_home = new TemporaryFolder();

    @Autowired
    private SecurityRoleDao m_securityRoleDao;

    @Autowired
    private UserAccountDao m_userDao;

    @Autowired
    private UserGroupDao m_groupDao;

    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    @Autowired
    private SessionUtils m_sessionUtils;

    private JdbcTemplate m_jdbc;
    private String m_previousHome;
    private File m_etc;

    @Override
    public void setTemporaryDatabase(final TemporaryDatabase database) {
        m_jdbc = database.getJdbcTemplate();
    }

    @Before
    public void setUp() throws Exception {
        BeanUtils.assertAutowiring(this);
        m_previousHome = System.getProperty("opennms.home");
        System.setProperty("opennms.home", m_home.getRoot().getAbsolutePath());
        m_etc = m_home.newFolder("etc");
        // AbstractOnmsUpgrade reads these in its constructor
        write("opennms.properties", "");
        write("rrd-configuration.properties", "");
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
    public void testCopiesTheUsers() throws Exception {
        copyFixtures();
        migrate();

        m_sessionUtils.withReadOnlyTransaction(() -> {
            assertEquals(Arrays.asList("admin", "bob", "carol", "rtc"),
                    m_userDao.findAllOrderByUsername().stream().map(UserAccount::getUsername).collect(Collectors.toList()));

            final UserAccount admin = m_userDao.findByUsername("admin");
            assertEquals("Administrator", admin.getFullName());
            assertEquals("Default administrator, do not delete", admin.getComments());
            assertEquals("gU2wmSW7k9v1xg4/MrAsaI+VyddBAhJJt4zPX5SGG0BK+qiASGnJsqM8JOug/aEL", admin.getPasswordHash());
            assertTrue(admin.isPasswordSalted());
            assertEquals("1234", admin.getTuiPin());
            assertEquals("America/New_York", admin.getTimeZoneId());
            assertEquals(Arrays.asList("MoTuWeThFr800-1700", "SaSu900-1200"), admin.getDutySchedules());
            // the first email wins
            assertEquals(3, admin.getContacts().size());
            assertEquals("admin@example.com", admin.getContact(ContactType.email).getInfo());
            assertEquals("pager@example.com", admin.getContact(ContactType.pagerEmail).getInfo());
            assertEquals("5551234", admin.getContact(ContactType.textPage).getInfo());
            assertEquals("ACME", admin.getContact(ContactType.textPage).getServiceProvider());
            // ROLE_BOGUS is unknown; the duplicate ROLE_ADMIN is one role
            assertEquals(List.of(SecurityRoles.ROLE_ADMIN, "ROLE_OPERATOR", SecurityRoles.ROLE_USER), roleNames(admin));

            final UserAccount bob = m_userDao.findByUsername("bob");
            assertEquals("21232F297A57A5A743894A0E4A801FC3", bob.getPasswordHash());
            assertFalse(bob.isPasswordSalted());
            assertNull(bob.getTimeZoneId());
            assertTrue(bob.getContacts().isEmpty());
            // " ROLE_ADMIN" is not ROLE_ADMIN: login does not trim roles
            assertEquals(List.of(SecurityRoles.ROLE_USER), roleNames(bob));

            // carol is in users.xml twice; the last one wins, as in UserManager
            final UserAccount carol = m_userDao.findByUsername("carol");
            assertEquals("Carol Two", carol.getFullName());
            assertEquals("second", carol.getPasswordHash());
            assertEquals(List.of("ROLE_STAGE"), roleNames(carol));
        });
    }

    @Test
    public void testCopiesTheGroups() throws Exception {
        copyFixtures();
        migrate();

        m_sessionUtils.withReadOnlyTransaction(() -> {
            final UserGroup admin = m_groupDao.findByName("Admin");
            assertEquals("Main", admin.getDefaultMap());
            assertEquals("The administrators", admin.getComments());
            // "ghost" and "carol " are not users, and the second "admin" is a duplicate
            assertEquals(List.of("admin", "bob"), usernames(admin.getMembers()));
            assertEquals(List.of("MoTuWeThFr800-1700"), admin.getDutySchedules());

            assertEquals(List.of("carol", "bob"), usernames(m_groupDao.findByName("Oncall").getMembers()));
            // "Ops" is in groups.xml twice; the last one wins, as in GroupManager
            assertEquals(List.of("bob"), usernames(m_groupDao.findByName("Ops").getMembers()));
        });
        assertEquals(3, count("groups"));
    }

    @Test
    public void testCopiesTheOnCallRoles() throws Exception {
        copyFixtures();
        migrate();

        m_sessionUtils.withReadOnlyTransaction(() -> {
            // "orphan" has no membership group and "unsupervised" has no supervisor
            assertEquals(List.of("night"), m_onCallRoleDao.findAll().stream().map(OnCallRole::getName).collect(Collectors.toList()));

            final OnCallRole night = m_onCallRoleDao.findByName("night");
            assertEquals("Night shift", night.getDescription());
            assertEquals("Oncall", night.getMembershipGroup().getName());
            assertEquals("admin", night.getSupervisor().getUsername());

            // the schedule of "ghost" (not a user) is dropped, without a gap
            final List<OnCallSchedule> schedules = night.getSchedules();
            assertEquals(2, schedules.size());

            final OnCallSchedule weekly = schedules.get(0);
            assertEquals("bob", weekly.getUser().getUsername());
            assertEquals(OnCallSchedule.Type.weekly, weekly.getType());
            assertEquals(2, weekly.getTimes().size());
            final OnCallScheduleTime monday = weekly.getTimes().get(0);
            assertEquals("0", monday.getTimeId());
            assertEquals("monday", monday.getDay());
            assertEquals("17:00:00", monday.getBegins());
            assertEquals("23:59:59", monday.getEnds());
            assertEquals("tuesday", weekly.getTimes().get(1).getDay());

            final OnCallSchedule specific = schedules.get(1);
            assertEquals("carol", specific.getUser().getUsername());
            assertEquals(OnCallSchedule.Type.specific, specific.getType());
            assertNull(specific.getTimes().get(0).getDay());
            assertEquals("21-Feb-2025 18:00:00", specific.getTimes().get(0).getBegins());
        });
    }

    @Test
    public void testCopiesTheCustomSecurityRoles() throws Exception {
        copyFixtures();
        migrate();

        m_sessionUtils.withReadOnlyTransaction(() -> {
            assertEquals(List.of("ROLE_OPERATOR", "ROLE_STAGE"),
                    m_securityRoleDao.findUserDefined().stream().map(SecurityRole::getName).collect(Collectors.toList()));
            assertEquals(SecurityRoles.BUILTIN_ROLES.size(), m_securityRoleDao.findBuiltin().size());
        });
    }

    @Test
    public void testASecondRunChangesNothing() throws Exception {
        copyFixtures();
        migrate();
        final Map<String, Integer> counts = countAll();

        migrate();
        assertEquals(counts, countAll());
    }

    @Test
    public void testKeepsWhatIsAlreadyInTheDatabase() throws Exception {
        copyFixtures();
        m_jdbc.update("INSERT INTO users (id, username, full_name, password_salted) VALUES (nextval('users_id_seq'), 'bob', 'Existing Bob', false)");
        m_jdbc.update("INSERT INTO groups (id, name, comments) VALUES (nextval('groups_id_seq'), 'Ops', 'Existing Ops')");
        migrate();

        m_sessionUtils.withReadOnlyTransaction(() -> {
            final UserAccount bob = m_userDao.findByUsername("bob");
            assertEquals("Existing Bob", bob.getFullName());
            assertNull(bob.getPasswordHash());
            assertTrue(bob.getSecurityRoles().isEmpty());

            final UserGroup ops = m_groupDao.findByName("Ops");
            assertEquals("Existing Ops", ops.getComments());
            assertTrue(ops.getMembers().isEmpty());

            // new rows refer to the existing bob
            assertEquals(List.of("admin", "bob"), usernames(m_groupDao.findByName("Admin").getMembers()));
            assertEquals("bob", m_onCallRoleDao.findByName("night").getSchedules().get(0).getUser().getUsername());
        });
        assertEquals(4, count("users"));
    }

    @Test
    public void testNoFilesCopiesNothing() throws Exception {
        migrate();

        assertEquals(0, count("users"));
        assertEquals(0, count("groups"));
        assertEquals(0, count("oncall_roles"));
        assertEquals(SecurityRoles.BUILTIN_ROLES.size(), count("security_roles"));
    }

    @Test
    public void testAFileThatCannotBeReadCopiesNothing() throws Exception {
        copyFixtures();
        write("groups.xml", "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\"><groups>");

        final UsersGroupsXmlMigratorOffline migrator = new UsersGroupsXmlMigratorOffline();
        migrator.preExecute();
        assertThrows(OnmsUpgradeException.class, migrator::execute);
        migrator.postExecute();

        assertEquals(0, count("users"));
        assertFalse("the files are only archived after a successful copy", new File(m_home.getRoot(), "etc_archive").exists());
    }

    @Test
    public void testAFailedWriteLeavesNothingBehind() throws Exception {
        copyFixtures();
        // fails the last step, after the users and groups are written
        m_jdbc.execute("CREATE FUNCTION fail_oncall_roles() RETURNS trigger AS $$ BEGIN RAISE EXCEPTION 'test failure'; END $$ LANGUAGE plpgsql");
        m_jdbc.execute("CREATE TRIGGER fail_oncall_roles BEFORE INSERT ON oncall_roles FOR EACH ROW EXECUTE PROCEDURE fail_oncall_roles()");

        final UsersGroupsXmlMigratorOffline migrator = new UsersGroupsXmlMigratorOffline();
        migrator.preExecute();
        assertThrows(OnmsUpgradeException.class, migrator::execute);
        migrator.postExecute();

        assertEquals(0, count("users"));
        assertEquals(0, count("groups"));
        assertEquals(SecurityRoles.BUILTIN_ROLES.size(), count("security_roles"));
        assertFalse(new File(m_home.getRoot(), "etc_archive").exists());
    }

    @Test
    public void testAFailedArchiveCopyIsRetried() throws Exception {
        copyFixtures();
        // a file where the folder must be
        final File archive = new File(m_home.getRoot(), "etc_archive");
        write(archive, "");

        final UsersGroupsXmlMigratorOffline migrator = new UsersGroupsXmlMigratorOffline();
        migrator.preExecute();
        // the task fails, so Upgrade does not mark it as done and runs it again next time
        assertThrows(OnmsUpgradeException.class, migrator::execute);
        final Map<String, Integer> counts = countAll();
        assertEquals(4, (int) counts.get("users"));

        assertTrue(archive.delete());
        migrate();
        assertEquals(counts, countAll());
        assertTrue(new File(archive, "users.xml").exists());
    }

    @Test
    public void testAMalformedSecurityRolesFileFailsTheTask() throws Exception {
        copyFixtures();
        write("security-roles.properties", "roles=\\u00zz");

        final UsersGroupsXmlMigratorOffline migrator = new UsersGroupsXmlMigratorOffline();
        migrator.preExecute();
        // an OnmsUpgradeException, which Upgrade reports, not an IllegalArgumentException, which stops the installer
        assertThrows(OnmsUpgradeException.class, migrator::execute);
        assertEquals(0, count("users"));
    }

    @Test
    public void testCopiesTheFilesToTheArchiveAndKeepsThem() throws Exception {
        copyFixtures();
        migrate();

        for (final String name : List.of("users.xml", "groups.xml", "security-roles.properties")) {
            final File archived = new File(m_home.getRoot(), "etc_archive/" + name);
            assertTrue(name + " is archived", archived.exists());
            assertTrue(name + " is still in etc", new File(m_etc, name).exists());
            assertTrue(FileUtils.contentEquals(new File(FIXTURES, name), archived));
        }
    }

    @Test
    public void testValuesTooLongForTheirColumn() throws Exception {
        final String tooLong = "x".repeat(300);
        // 200 characters, but 400 UTF-16 units
        final String emoji = "\uD83D\uDE00".repeat(200);
        write("users.xml", "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"><users>"
                + "<user><user-id>" + tooLong + "</user-id><password>a</password></user>"
                + "<user><user-id>dave</user-id><full-name>" + tooLong + "</full-name><password>a</password>"
                + "<contact type=\"email\" info=\"" + tooLong + "\"/><contact type=\"email\" info=\"dave@example.com\"/>"
                + "<contact type=\"workPhone\" info=\"5551234\"/><contact type=\"homePhone\" info=\"" + emoji + "\"/>"
                + "<duty-schedule>" + tooLong + "</duty-schedule><duty-schedule>MoTuWeThFr800-1700</duty-schedule></user>"
                + "</users></userinfo>");
        migrate();

        m_sessionUtils.withReadOnlyTransaction(() -> {
            assertEquals(List.of("dave"), m_userDao.findAll().stream().map(UserAccount::getUsername).collect(Collectors.toList()));
            final UserAccount dave = m_userDao.findByUsername("dave");
            // an optional value is dropped, the row is kept
            assertNull(dave.getFullName());
            // the first email is too long; the second one is not used today, so it is not copied either
            assertNull(dave.getContact(ContactType.email));
            assertNotNull(dave.getContact(ContactType.workPhone));
            // varchar(256) holds 256 characters, whatever their UTF-16 length
            assertEquals(emoji, dave.getContact(ContactType.homePhone).getInfo());
            assertEquals(List.of("MoTuWeThFr800-1700"), dave.getDutySchedules());
        });
    }

    @Test
    public void testCopiesTheShippedDefaults() throws Exception {
        final File shipped = new File("../../opennms-base-assembly/src/main/filtered/etc");
        FileUtils.copyFileToDirectory(new File(shipped, "users.xml"), m_etc);
        FileUtils.copyFileToDirectory(new File(shipped, "groups.xml"), m_etc);
        migrate();

        m_sessionUtils.withReadOnlyTransaction(() -> {
            assertEquals(List.of(SecurityRoles.ROLE_ADMIN), roleNames(m_userDao.findByUsername("admin")));
            assertEquals(List.of(SecurityRoles.ROLE_RTC), roleNames(m_userDao.findByUsername("rtc")));
            assertTrue(m_userDao.findByUsername("admin").isPasswordSalted());
            assertEquals(List.of("admin"), usernames(m_groupDao.findByName("Admin").getMembers()));
        });
    }

    /** Runs the task the way {@code Upgrade} does. */
    private void migrate() throws OnmsUpgradeException {
        final UsersGroupsXmlMigratorOffline migrator = new UsersGroupsXmlMigratorOffline();
        migrator.preExecute();
        migrator.execute();
        migrator.postExecute();
    }

    private void copyFixtures() throws Exception {
        FileUtils.copyDirectory(FIXTURES, m_etc);
    }

    private void write(final String name, final String content) throws Exception {
        write(new File(m_etc, name), content);
    }

    private static void write(final File file, final String content) throws Exception {
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }

    private int count(final String table) {
        return m_jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    private Map<String, Integer> countAll() {
        final Map<String, Integer> counts = new LinkedHashMap<>();
        TABLES.forEach(table -> counts.put(table, count(table)));
        return counts;
    }

    private static List<String> roleNames(final UserAccount user) {
        return user.getSecurityRoles().stream().map(SecurityRole::getName).sorted().collect(Collectors.toList());
    }

    private static List<String> usernames(final List<UserAccount> users) {
        return users.stream().map(UserAccount::getUsername).collect(Collectors.toList());
    }
}
