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
import org.opennms.netmgt.config.users.User;
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

    private static final List<String> ARCHIVED = List.of(
            "users.xml", "groups.xml", "security-roles.properties", "users.xml.rpmsave", "groups.xml.rpmsave");

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
            assertEquals(Arrays.asList("admin", "bob", "carol", "rtc", "zoe"),
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
    public void testDecodesHtmlEncodedTextFields() throws Exception {
        // older versions wrote full names and contacts HTML-encoded to users.xml; the database holds them raw
        copyFixtures();
        migrate();

        m_sessionUtils.withReadOnlyTransaction(() -> {
            final UserAccount zoe = m_userDao.findByUsername("zoe");
            assertEquals("O'Brien & Co", zoe.getFullName());
            assertEquals("a&b@x.org", zoe.getContact(ContactType.email).getInfo());
        });
    }

    @Test
    public void testDecodedTextIsCheckedForLength() throws Exception {
        // 256 characters once decoded, but 1280 encoded
        final String encoded = "&amp;".repeat(256);
        // replace("&", "&amp;") escapes the encoded text for the XML file, so that the parser yields the encoded text
        write("users.xml", "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"><users>"
                + "<user><user-id>eve</user-id><full-name>" + encoded.replace("&", "&amp;") + "</full-name><password>a</password>"
                + "<contact type=\"email\" info=\"" + encoded.replace("&", "&amp;") + "\"/></user></users></userinfo>");
        migrate();

        m_sessionUtils.withReadOnlyTransaction(() -> {
            final UserAccount eve = m_userDao.findByUsername("eve");
            assertEquals("&".repeat(256), eve.getFullName());
            assertEquals("&".repeat(256), eve.getContact(ContactType.email).getInfo());
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
    public void testCopiesNothingWhenTheDatabaseHasUsers() throws Exception {
        copyFixtures();
        // for example, a seeded admin: the file must not be merged into it
        m_jdbc.update("INSERT INTO users (id, username, full_name, password_hash, password_salted) VALUES (nextval('users_id_seq'), 'admin', 'Seeded', 'seed', true)");
        final Map<String, Integer> counts = countAll();
        migrate();

        assertEquals(counts, countAll());
        m_sessionUtils.withReadOnlyTransaction(() -> {
            final UserAccount admin = m_userDao.findByUsername("admin");
            assertEquals("Seeded", admin.getFullName());
            assertEquals("seed", admin.getPasswordHash());
            assertTrue(admin.getContacts().isEmpty());
        });
        // the files are not read, but archived, so that nothing reads them later
        assertNoneInEtc();
        assertTrue(new File(archive(), "users.xml").exists());
    }

    @Test
    public void testCopiesNothingWhenTheDatabaseHasACustomSecurityRole() throws Exception {
        copyFixtures();
        m_jdbc.update("INSERT INTO security_roles (id, name, builtin) VALUES (nextval('security_roles_id_seq'), 'ROLE_CUSTOM', false)");
        final Map<String, Integer> counts = countAll();
        migrate();

        assertEquals(counts, countAll());
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
        assertFalse("the files are only archived after a successful copy", archive().exists());
    }

    @Test
    public void testAReservedNameFailsTheTaskAndCopiesNothing() throws Exception {
        copyFixtures();
        write("users.xml", "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"><users>"
                + "<user><user-id>jane</user-id><password>x</password></user><user><user-id>item</user-id><password>x</password></user>"
                + "</users></userinfo>");
        write("groups.xml", "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\"><groups>"
                + "<group><name>..</name></group><group><name>Ops</name></group></groups></groupinfo>");

        final OnmsUpgradeException e = assertThrows(OnmsUpgradeException.class, this::migrate);

        assertTrue(e.getMessage(), e.getMessage().contains("the user 'item', the group '..'"));
        // so the failure fails the upgrade and the installer, and OpenNMS does not start without users
        assertTrue(new UsersGroupsXmlMigratorOffline().isCritical());
        // and skipping it while OpenNMS runs fails the upgrade too
        assertTrue(new UsersGroupsXmlMigratorOffline().isPending());
        assertEquals(0, count("users"));
        assertEquals(0, count("groups"));
        assertEquals(0, count("usermgmt_initialized"));
        assertFalse("the files are only archived after a successful copy", archive().exists());
    }

    @Test
    public void testAReservedOnCallRoleNameFailsTheTaskAndCopiesNothing() throws Exception {
        copyFixtures();
        write("groups.xml", "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\"><groups>"
                + "<group><name>Ops</name></group></groups>"
                + "<roles><role name=\"calendar\" supervisor=\"admin\" membership-group=\"Ops\"/></roles></groupinfo>");

        final OnmsUpgradeException e = assertThrows(OnmsUpgradeException.class, this::migrate);

        assertTrue(e.getMessage(), e.getMessage().contains("the on-call role 'calendar'"));
        assertEquals(0, count("groups"));
        assertEquals(0, count("oncall_roles"));
    }

    @Test
    public void testTheEndpointNamesOfTheOtherCollectionAreCopied() throws Exception {
        copyFixtures();
        // /users has no available-categories endpoint, and /groups no available-roles one
        write("users.xml", "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"><users>"
                + "<user><user-id>available-categories</user-id><password>x</password></user>"
                + "</users></userinfo>");
        write("groups.xml", "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\"><groups>"
                + "<group><name>available-roles</name></group></groups></groupinfo>");

        migrate();

        assertEquals(1, m_jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username = 'available-categories'", Integer.class).intValue());
        assertEquals(1, m_jdbc.queryForObject("SELECT COUNT(*) FROM groups WHERE name = 'available-roles'", Integer.class).intValue());
    }

    @Test
    public void testIsPendingUntilTheFilesAreCopied() throws Exception {
        // the upgrade asks when it skips the task because OpenNMS is running: the files are not copied yet
        copyFixtures();
        assertTrue(new UsersGroupsXmlMigratorOffline().isPending());

        migrate();

        // an install while OpenNMS runs, after a good migration, does not fail
        assertFalse(new UsersGroupsXmlMigratorOffline().isPending());
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
    }

    @Test
    public void testALostConnectionReportsTheRealError() throws Exception {
        copyFixtures();
        // the connection dies in the middle of the writes
        m_jdbc.execute("CREATE FUNCTION kill_backend() RETURNS trigger AS $$ BEGIN PERFORM pg_terminate_backend(pg_backend_pid()); RETURN NEW; END $$ LANGUAGE plpgsql");
        m_jdbc.execute("CREATE TRIGGER kill_backend BEFORE INSERT ON groups FOR EACH ROW EXECUTE PROCEDURE kill_backend()");

        final UsersGroupsXmlMigratorOffline migrator = new UsersGroupsXmlMigratorOffline();
        migrator.preExecute();
        final OnmsUpgradeException e = assertThrows(OnmsUpgradeException.class, migrator::execute);
        // the error of the insert, not of a later call on the closed connection
        assertFalse(e.getMessage(), e.getMessage().contains("Connection is closed"));
        assertEquals(0, count("users"));
    }

    @Test
    public void testAFailedMoveIsRetriedByTheNextRun() throws Exception {
        copyFixtures();
        // a file where the folder must be
        final File archive = archive();
        write(archive, "");

        final OnmsUpgradeException e = assertThrows(OnmsUpgradeException.class, this::migrate);
        assertTrue(e.getMessage(), e.getMessage().startsWith("Can't move '"));
        // the data is committed and the marker set: the next run takes the skip branch, which retries the move
        assertEquals(5, count("users"));
        assertEquals(1, count("usermgmt_initialized"));
        assertTrue(new File(m_etc, "users.xml").exists());

        assertTrue(archive.delete());
        migrate();
        assertEquals(5, count("users"));
        assertTrue(new File(archive, "users.xml").exists());
        assertNoneInEtc();
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
    public void testMovesTheFilesToTheArchive() throws Exception {
        copyFixtures();
        migrate();

        for (final String name : List.of("users.xml", "groups.xml", "security-roles.properties")) {
            final File archived = new File(archive(), name);
            assertTrue(name + " is archived", archived.exists());
            assertTrue(FileUtils.contentEquals(new File(FIXTURES, name), archived));
        }
        assertNoneInEtc();
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
    public void testCopiesTheDefaultFiles() throws Exception {
        final File shipped = new File("src/main/resources/org/opennms/upgrade/implementations");
        FileUtils.copyFile(new File(shipped, "default-users.xml"), new File(m_etc, "users.xml"));
        FileUtils.copyFile(new File(shipped, "default-groups.xml"), new File(m_etc, "groups.xml"));
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

    private File archive() {
        return new File(m_home.getRoot(), "etc_archive");
    }

    private void assertNoneInEtc() {
        for (final String name : ARCHIVED) {
            assertFalse(name + " is still in etc", new File(m_etc, name).exists());
        }
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

    @Test
    public void testMarksTheDatabaseAsInitialized() throws Exception {
        // also with no users to copy: the default-users seed must not add admin/admin to it later
        write("users.xml", "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"/>");
        migrate();
        assertEquals(1, count("usermgmt_initialized"));
    }

    @Test
    public void testMarksADatabaseThatAlreadyHasUserData() throws Exception {
        m_jdbc.update("INSERT INTO users (id, username, password_salted, created_time) VALUES (nextval('users_id_seq'), 'jane', false, NOW())");
        migrate();
        assertEquals(1, count("usermgmt_initialized"));
    }

    @Test
    public void testDoesNotMarkAFailedMigration() throws Exception {
        write("users.xml", "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"><users><user/></users></userinfo>");
        assertThrows(OnmsUpgradeException.class, this::migrate);
        assertEquals(0, count("usermgmt_initialized"));
    }

    @Test
    public void testRunsOnEveryInstall() throws Exception {
        // the run-once rule is per database (usermgmt_initialized), not per etc: a restored or new database on an
        // upgraded host still gets the users of users.xml
        assertFalse(new UsersGroupsXmlMigratorOffline().runOnlyOnce());
    }

    @Test
    public void testSkipsADatabaseThatWasInitialized() throws Exception {
        copyFixtures();
        m_jdbc.update("INSERT INTO usermgmt_initialized (initialized_by) VALUES ('DefaultUsersSeedOffline')");
        migrate();
        assertEquals(0, count("users"));
        assertEquals(0, count("groups"));
    }

    @Test
    public void testDoesNotMarkADatabaseWithoutUsersXml() throws Exception {
        // the default-users seed adds admin/admin to such a database (a new installation once the files are gone)
        migrate();
        assertEquals(0, count("usermgmt_initialized"));
    }

    @Test
    public void testRpmSavedUsersAndDeletedGroupsGetTheDefaultAdminGroup() throws Exception {
        // RPM renamed the changed users.xml and deleted the unchanged groups.xml
        write("users.xml.rpmsave", USERS_ADMIN_ALICE);
        migrate();

        m_sessionUtils.withReadOnlyTransaction(() -> {
            assertEquals(List.of("admin", "alice"),
                    m_userDao.findAllOrderByUsername().stream().map(UserAccount::getUsername).collect(Collectors.toList()));
            final UserGroup admin = m_groupDao.findByName("Admin");
            assertEquals("The administrators", admin.getComments());
            assertEquals(List.of("admin"), usernames(admin.getMembers()));
        });
        assertEquals(1, count("groups"));
        assertEquals(1, count("usermgmt_initialized"));
        assertNoneInEtc();
        assertArchived("users.xml.rpmsave");
    }

    @Test
    public void testPlainUsersAndDeletedGroupsGetTheDefaultAdminGroup() throws Exception {
        write("users.xml", USERS_ADMIN_ALICE);
        migrate();

        assertEquals(2, count("users"));
        m_sessionUtils.withReadOnlyTransaction(() ->
                assertEquals(List.of("admin"), usernames(m_groupDao.findByName("Admin").getMembers())));
        assertEquals(1, count("usermgmt_initialized"));
        assertArchived("users.xml");
    }

    @Test
    public void testRpmSavedGroupsAndDeletedUsersGetTheDefaultUsers() throws Exception {
        write("groups.xml.rpmsave", GROUPS_ADMIN_OPS);
        migrate();
        seed();

        final Map<String, String> defaultHashes = UserMgmtDefaults.users().getUsers().stream()
                .collect(Collectors.toMap(User::getUserId, u -> u.getPassword().getEncryptedPassword()));
        m_sessionUtils.withReadOnlyTransaction(() -> {
            assertEquals(List.of("admin", "rtc"),
                    m_userDao.findAllOrderByUsername().stream().map(UserAccount::getUsername).collect(Collectors.toList()));
            assertEquals(defaultHashes.get("admin"), m_userDao.findByUsername("admin").getPasswordHash());
            assertEquals(defaultHashes.get("rtc"), m_userDao.findByUsername("rtc").getPasswordHash());
            assertEquals(List.of(SecurityRoles.ROLE_ADMIN), roleNames(m_userDao.findByUsername("admin")));
            assertEquals(List.of("admin"), usernames(m_groupDao.findByName("Admin").getMembers()));
            assertEquals(List.of("admin"), usernames(m_groupDao.findByName("ops").getMembers()));
        });
        // the seed added nothing
        assertEquals(2, count("users"));
        assertEquals(2, count("groups"));
        assertEquals(1, count("usermgmt_initialized"));
        assertNoneInEtc();
        assertArchived("groups.xml.rpmsave");
    }

    @Test
    public void testPlainGroupsAndDeletedUsersGetTheDefaultUsers() throws Exception {
        write("groups.xml", GROUPS_ADMIN_OPS);
        migrate();

        assertEquals(2, count("users"));
        assertEquals(2, count("groups"));
        assertEquals(1, count("usermgmt_initialized"));
        assertArchived("groups.xml");
    }

    @Test
    public void testBothFilesRpmSaved() throws Exception {
        write("users.xml.rpmsave", USERS_ADMIN_ALICE);
        write("groups.xml.rpmsave", GROUPS_ADMIN_OPS);
        migrate();

        m_sessionUtils.withReadOnlyTransaction(() -> {
            assertEquals(List.of("admin", "alice"),
                    m_userDao.findAllOrderByUsername().stream().map(UserAccount::getUsername).collect(Collectors.toList()));
            assertEquals(List.of("admin"), usernames(m_groupDao.findByName("ops").getMembers()));
        });
        assertEquals(2, count("groups"));
        assertNoneInEtc();
        assertArchived("users.xml.rpmsave");
        assertArchived("groups.xml.rpmsave");
    }

    @Test
    public void testPlainFileWinsOverRpmsave() throws Exception {
        write("users.xml", USERS_ADMIN_ALICE);
        write("users.xml.rpmsave", "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"><users>"
                + "<user><user-id>mallory</user-id><password>x</password></user></users></userinfo>");
        write("groups.xml.rpmsave", GROUPS_ADMIN_OPS);
        migrate();

        m_sessionUtils.withReadOnlyTransaction(() -> assertEquals(List.of("admin", "alice"),
                m_userDao.findAllOrderByUsername().stream().map(UserAccount::getUsername).collect(Collectors.toList())));
        // the groups come from the .rpmsave file, as there is no plain one
        assertEquals(2, count("groups"));
        // the .rpmsave file is archived unread
        assertNoneInEtc();
        assertArchived("users.xml");
        assertArchived("users.xml.rpmsave");
        assertArchived("groups.xml.rpmsave");
    }

    @Test
    public void testFailedCopyLeavesTheFilesInEtc() throws Exception {
        copyFixtures();
        write("users.xml.rpmsave", USERS_ADMIN_ALICE);
        // a failure inside the transaction, after the security roles are written
        m_jdbc.execute("CREATE FUNCTION fail_users() RETURNS trigger AS $$ BEGIN RAISE EXCEPTION 'test failure'; END $$ LANGUAGE plpgsql");
        m_jdbc.execute("CREATE TRIGGER fail_users BEFORE INSERT ON users FOR EACH ROW EXECUTE PROCEDURE fail_users()");
        try {
            final OnmsUpgradeException e = assertThrows(OnmsUpgradeException.class, this::migrate);
            assertTrue(e.getMessage(), e.getMessage().contains("test failure"));
        } finally {
            m_jdbc.execute("DROP TRIGGER fail_users ON users");
            m_jdbc.execute("DROP FUNCTION fail_users()");
        }

        assertAllInEtc("users.xml", "groups.xml", "security-roles.properties", "users.xml.rpmsave");
        assertFalse(archive().exists());
        assertEquals(0, count("usermgmt_initialized"));
        assertEquals(SecurityRoles.BUILTIN_ROLES.size(), count("security_roles"));

        // the next run copies them
        migrate();
        assertEquals(5, count("users"));
        assertNoneInEtc();
    }

    @Test
    public void testAReservedNameLeavesTheFilesInEtc() throws Exception {
        // fails before anything is written
        write("users.xml.rpmsave", "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"><users>"
                + "<user><user-id>item</user-id><password>x</password></user></users></userinfo>");
        write("groups.xml.rpmsave", GROUPS_ADMIN_OPS);
        write("security-roles.properties", "roles=operator");

        assertThrows(OnmsUpgradeException.class, this::migrate);

        assertAllInEtc("users.xml.rpmsave", "groups.xml.rpmsave", "security-roles.properties");
        assertFalse(archive().exists());
        assertEquals(0, count("usermgmt_initialized"));
    }

    @Test
    public void testAFailedGroupsOnlyMigrationKeepsTheGroups() throws Exception {
        // the package deleted the unchanged users.xml; the groups file has a reserved name
        write("groups.xml.rpmsave", "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\"><groups>"
                + "<group><name>Admin</name><user>admin</user></group>"
                + "<group><name>..</name><user>admin</user></group></groups></groupinfo>");
        assertThrows(OnmsUpgradeException.class, this::migrate);

        // the same install runs the seed next: it must not seed, or the next run would archive the groups unread
        seed();
        assertEquals(0, count("users"));
        assertEquals(0, count("groups"));
        assertEquals(0, count("usermgmt_initialized"));
        assertAllInEtc("groups.xml.rpmsave");

        // the admin renames the group and runs the installer again
        write("groups.xml.rpmsave", GROUPS_ADMIN_OPS);
        migrate();
        seed();
        assertEquals(2, count("users"));
        m_sessionUtils.withReadOnlyTransaction(() ->
                assertEquals(List.of("admin"), usernames(m_groupDao.findByName("ops").getMembers())));
        assertEquals(2, count("groups"));
        assertNoneInEtc();
        assertArchived("groups.xml.rpmsave");
    }

    @Test
    public void testUsersWithoutAdminGetAnEmptyDefaultAdminGroup() throws Exception {
        // a users.xml without admin, and the unchanged groups.xml deleted by the package
        write("users.xml.rpmsave", "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"><users>"
                + "<user><user-id>alice</user-id><password>x</password><role>ROLE_ADMIN</role></user></users></userinfo>");
        migrate();

        m_sessionUtils.withReadOnlyTransaction(() -> {
            assertEquals(List.of("alice"), m_userDao.findAll().stream().map(UserAccount::getUsername).collect(Collectors.toList()));
            // the member admin is skipped, not a failure
            assertTrue(m_groupDao.findByName("Admin").getMembers().isEmpty());
        });
        assertEquals(1, count("usermgmt_initialized"));
        assertNoneInEtc();
    }

    @Test
    public void testSkipBranchMovesLeftoverFiles() throws Exception {
        // for example, files left in etc by an earlier version, or a users.xml that a container copies on every start
        m_jdbc.update("INSERT INTO usermgmt_initialized (initialized_by) VALUES ('DefaultUsersSeedOffline')");
        write("users.xml", USERS_ADMIN_ALICE);
        write("groups.xml.rpmsave", GROUPS_ADMIN_OPS);
        migrate();

        assertEquals(0, count("users"));
        assertNoneInEtc();
        assertArchived("users.xml");
        assertArchived("groups.xml.rpmsave");
    }

    @Test
    public void testIdenticalArchivedFileIsNotDuplicated() throws Exception {
        m_jdbc.update("INSERT INTO usermgmt_initialized (initialized_by) VALUES ('DefaultUsersSeedOffline')");
        assertTrue(archive().mkdir());
        write(new File(archive(), "users.xml"), USERS_ADMIN_ALICE);
        write("users.xml", USERS_ADMIN_ALICE);
        migrate();

        assertNoneInEtc();
        assertEquals(List.of("users.xml"), List.of(archive().list()));
    }

    @Test
    public void testDifferentArchivedFileGetsATimestampSuffix() throws Exception {
        m_jdbc.update("INSERT INTO usermgmt_initialized (initialized_by) VALUES ('DefaultUsersSeedOffline')");
        assertTrue(archive().mkdir());
        write(new File(archive(), "users.xml"), "older");
        write("users.xml", USERS_ADMIN_ALICE);
        migrate();

        assertNoneInEtc();
        assertEquals("older", read(new File(archive(), "users.xml")));
        final List<String> suffixed = Arrays.stream(archive().list()).filter(name -> !name.equals("users.xml")).collect(Collectors.toList());
        assertEquals(1, suffixed.size());
        assertTrue(suffixed.get(0), suffixed.get(0).matches("users\\.xml\\.\\d{14}"));
        assertEquals(USERS_ADMIN_ALICE, read(new File(archive(), suffixed.get(0))));

        // the same file again, as a container copies it on every start: it matches the suffixed copy
        write("users.xml", USERS_ADMIN_ALICE);
        migrate();
        assertNoneInEtc();
        assertEquals(2, archive().list().length);
    }

    @Test
    public void testNeitherFileLetsTheSeedRun() throws Exception {
        // a new installation: no users.xml, no groups.xml and no custom security roles
        write("security-roles.properties", "roles=admin,rest");
        migrate();

        assertEquals(0, count("users"));
        assertEquals(0, count("groups"));
        assertEquals(0, count("security_roles WHERE NOT builtin"));
        assertEquals(0, count("usermgmt_initialized"));

        seed();
        assertEquals(2, count("users"));
        assertEquals(1, count("groups"));
    }

    @Test
    public void testCustomRolesWithoutUsersAndGroupsGetTheDefaults() throws Exception {
        // the custom roles, the default users and group and the marker are committed together, so the custom roles
        // never make a database without users look initialized
        write("security-roles.properties", "roles=operator");
        migrate();

        assertEquals(1, count("security_roles WHERE NOT builtin"));
        assertEquals(1, count("usermgmt_initialized"));
        m_sessionUtils.withReadOnlyTransaction(() -> {
            assertEquals(List.of(SecurityRoles.ROLE_ADMIN), roleNames(m_userDao.findByUsername("admin")));
            assertEquals(List.of(SecurityRoles.ROLE_RTC), roleNames(m_userDao.findByUsername("rtc")));
            assertEquals(List.of("admin"), usernames(m_groupDao.findByName("Admin").getMembers()));
        });
        assertNoneInEtc();
        assertArchived("security-roles.properties");
    }

    @Test
    public void testCustomRolesWithoutUsersAndGroupsNeedNoSeed() throws Exception {
        // the installer stops after the migrator, before the seed runs: the next install must still find the users
        write("security-roles.properties", "roles=operator");
        migrate();
        migrate();

        assertEquals(2, count("users"));
        assertEquals(1, count("groups"));
        assertEquals(1, count("security_roles WHERE NOT builtin"));

        seed();
        assertEquals(2, count("users"));
        assertEquals(1, count("groups"));
    }

    private static final String USERS_ADMIN_ALICE = "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"><users>"
            + "<user><user-id>admin</user-id><password salt=\"true\">changed</password><role>ROLE_ADMIN</role></user>"
            + "<user><user-id>alice</user-id><password>x</password></user></users></userinfo>";

    private static final String GROUPS_ADMIN_OPS = "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\"><groups>"
            + "<group><name>Admin</name><user>admin</user></group>"
            + "<group><name>ops</name><user>admin</user></group></groups></groupinfo>";

    private void assertArchived(final String name) {
        assertTrue(name + " is archived", new File(archive(), name).exists());
    }

    private void assertAllInEtc(final String... names) {
        for (final String name : names) {
            assertTrue(name + " is in etc", new File(m_etc, name).exists());
        }
    }

    private static String read(final File file) throws Exception {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    private static void seed() throws OnmsUpgradeException {
        final DefaultUsersSeedOffline seed = new DefaultUsersSeedOffline();
        seed.preExecute();
        seed.execute();
        seed.postExecute();
    }
}
