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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Groupinfo;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
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
import org.opennms.netmgt.model.usermgmt.UserContact;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.upgrade.implementations.UsersGroupsXmlImport.Existing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * The import into a database that already holds users, groups and on-call roles. Every test starts from the
 * seeded admin, rtc and Admin, plus jane, the group Ops (member jane) and the on-call role Night on Ops, which
 * schedules jane.
 */
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
public class UsersGroupsXmlImportIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    private static final String USERS_XML = "users.xml";
    private static final String GROUPS_XML = "groups.xml";
    private static final String SECURITY_ROLES = "security-roles.properties";

    @Rule
    public TemporaryFolder m_home = new TemporaryFolder();

    @Autowired
    private UserAccountDao m_userDao;

    @Autowired
    private UserGroupDao m_groupDao;

    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    @Autowired
    private SessionUtils m_sessionUtils;

    private final List<String> m_messages = new ArrayList<>();
    private List<String> m_problems;

    private JdbcTemplate m_jdbc;
    private String m_previousHome;
    private File m_files;

    private int m_admin;
    private int m_jane;
    private int m_ops;
    private int m_night;

    @Override
    public void setTemporaryDatabase(final TemporaryDatabase database) {
        m_jdbc = database.getJdbcTemplate();
    }

    @Before
    public void setUp() throws Exception {
        BeanUtils.assertAutowiring(this);
        m_previousHome = System.getProperty("opennms.home");
        System.setProperty("opennms.home", m_home.getRoot().getAbsolutePath());
        final File etc = m_home.newFolder("etc");
        // AbstractOnmsUpgrade reads these in its constructor
        write(new File(etc, "opennms.properties"), "");
        write(new File(etc, "rrd-configuration.properties"), "");
        // the files to import are not in etc, as for the tool
        m_files = m_home.newFolder("import");

        final DefaultUsersSeedOffline seed = new DefaultUsersSeedOffline();
        seed.preExecute();
        seed.execute();
        seed.postExecute();
        m_admin = id("SELECT id FROM users WHERE username = 'admin'");

        m_jane = id("INSERT INTO users (id, username, full_name, comments, password_hash, password_salted, tui_pin, created_time, last_modified) "
                + "VALUES (nextval('users_id_seq'), 'jane', 'Jane Doe', 'Added by hand', 'janepw', true, '9999', NOW(), NOW()) RETURNING id");
        m_jdbc.update("INSERT INTO user_contacts (id, user_id, contact_type, info) VALUES (nextval('user_contacts_id_seq'), ?, 'email', 'jane@example.org')", m_jane);
        m_jdbc.update("INSERT INTO user_contacts (id, user_id, contact_type, info) VALUES (nextval('user_contacts_id_seq'), ?, 'workPhone', '5550000')", m_jane);
        m_jdbc.update("INSERT INTO user_duty_schedules (user_id, position, schedule) VALUES (?, 0, 'MoTuWeThFr800-1700')", m_jane);
        m_jdbc.update("INSERT INTO user_security_roles (user_id, role_id) SELECT ?, id FROM security_roles WHERE name = ?", m_jane, SecurityRoles.ROLE_DASHBOARD);

        m_ops = id("INSERT INTO groups (id, name, comments, created_time, last_modified) VALUES (nextval('groups_id_seq'), 'Ops', 'Operations', NOW(), NOW()) RETURNING id");
        m_jdbc.update("INSERT INTO group_members (group_id, position, user_id) VALUES (?, 0, ?)", m_ops, m_jane);
        m_jdbc.update("INSERT INTO group_duty_schedules (group_id, position, schedule) VALUES (?, 0, 'SaSu900-1200')", m_ops);

        m_night = id("INSERT INTO oncall_roles (id, name, description, membership_group_id, supervisor_user_id, created_time, last_modified) "
                + "VALUES (nextval('oncall_roles_id_seq'), 'Night', 'Night shift', ?, ?, NOW(), NOW()) RETURNING id", m_ops, m_admin);
        final int schedule = id("INSERT INTO oncall_schedules (id, oncall_role_id, user_id, schedule_type, position) "
                + "VALUES (nextval('oncall_schedules_id_seq'), ?, ?, 'weekly', 0) RETURNING id", m_night, m_jane);
        m_jdbc.update("INSERT INTO oncall_schedule_times (id, schedule_id, position, time_id, day, begins, ends) "
                + "VALUES (nextval('oncall_schedule_times_id_seq'), ?, 0, '0', 'monday', '17:00:00', '23:59:59')", schedule);
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
    public void testSkipLeavesExistingEntriesAndListsThem() throws Exception {
        writeSkipFiles();
        final int adminVersion = version("users", m_admin);
        final int opsVersion = version("groups", m_ops);

        final UsersGroupsXmlImport xmlImport = importFiles(Existing.SKIP, true);

        assertEquals(Map.of("users", List.of("admin"), "groups", List.of("Ops")), xmlImport.existingEntries());
        assertEquals(adminVersion, version("users", m_admin));
        assertEquals(opsVersion, version("groups", m_ops));
        m_sessionUtils.withReadOnlyTransaction(() -> {
            assertEquals("Administrator", m_userDao.findByUsername("admin").getFullName());
            assertEquals("bobpw", m_userDao.findByUsername("bob").getPasswordHash());
            assertEquals(List.of("jane"), usernames(m_groupDao.findByName("Ops").getMembers()));

            final UserGroup dev = m_groupDao.findByName("Dev");
            assertEquals(List.of("admin", "bob"), usernames(dev.getMembers()));
            assertEquals(Integer.valueOf(m_admin), dev.getMembers().get(0).getId());
        });
        assertTrue(m_messages.toString(), m_messages.contains("users: 1 imported, 0 overwritten, 1 existing skipped"));
        assertTrue(m_messages.toString(), m_messages.contains("groups: 1 imported, 0 overwritten, 1 existing skipped"));
        assertFalse(m_messages.toString(), m_messages.stream().anyMatch(m -> m.startsWith("Skipped") || m.startsWith("Warning")));
    }

    @Test
    public void testOverwriteUpdatesInPlaceAndKeepsReferences() throws Exception {
        writeUsers("<user><user-id>jane</user-id><full-name>Jane Smith</full-name><password salt=\"false\">newpw</password>"
                + "<contact type=\"email\" info=\"jane.smith@example.org\"/><role>ROLE_USER</role></user>");
        writeGroups("<groups><group><name>Ops</name><user>admin</user></group></groups>");

        final UsersGroupsXmlImport xmlImport = importFiles(Existing.OVERWRITE, true);

        assertEquals(Map.of("users", List.of("jane"), "groups", List.of("Ops")), xmlImport.existingEntries());
        m_sessionUtils.withReadOnlyTransaction(() -> {
            final UserAccount jane = m_userDao.findByUsername("jane");
            assertEquals(Integer.valueOf(m_jane), jane.getId());
            assertEquals("Jane Smith", jane.getFullName());
            assertNull(jane.getComments());
            assertEquals("newpw", jane.getPasswordHash());
            assertFalse(jane.isPasswordSalted());
            assertNull(jane.getTuiPin());
            assertEquals(1, jane.getContacts().size());
            assertEquals("jane.smith@example.org", jane.getContact(ContactType.email).getInfo());
            assertTrue(jane.getDutySchedules().isEmpty());
            assertEquals(List.of(SecurityRoles.ROLE_USER), roleNames(jane));

            final UserGroup ops = m_groupDao.findByName("Ops");
            assertEquals(Integer.valueOf(m_ops), ops.getId());
            assertNull(ops.getComments());
            assertEquals(List.of("admin"), usernames(ops.getMembers()));
            assertTrue(ops.getDutySchedules().isEmpty());

            // the on-call role refers to both by id, so it still has them
            final OnCallRole night = m_onCallRoleDao.findByName("Night");
            assertEquals(Integer.valueOf(m_night), night.getId());
            assertEquals("Ops", night.getMembershipGroup().getName());
            assertEquals(List.of("jane"), night.getSchedules().stream().map(s -> s.getUser().getUsername()).collect(Collectors.toList()));
            // a group that is not in the file keeps its members
            assertEquals(List.of("admin"), usernames(m_groupDao.findByName("Admin").getMembers()));
        });
        assertTrue(m_messages.toString(), m_messages.contains("users: 0 imported, 1 overwritten, 0 existing skipped"));
        assertTrue(m_messages.toString(), m_messages.contains("groups: 0 imported, 1 overwritten, 0 existing skipped"));
    }

    @Test
    public void testOverwriteReplacesOnCallSchedules() throws Exception {
        writeGroups("<roles><role name=\"Night\" membership-group=\"Ops\" supervisor=\"admin\" description=\"Nights\">"
                + "<schedule name=\"admin\" type=\"daily\"><time begins=\"00:00:00\" ends=\"06:00:00\"/></schedule>"
                + "</role></roles>");

        final UsersGroupsXmlImport xmlImport = importFiles(Existing.OVERWRITE, true);

        assertEquals(Map.of("on-call roles", List.of("Night")), xmlImport.existingEntries());
        m_sessionUtils.withReadOnlyTransaction(() -> {
            final OnCallRole night = m_onCallRoleDao.findByName("Night");
            assertEquals(Integer.valueOf(m_night), night.getId());
            assertEquals("Nights", night.getDescription());
            assertEquals(1, night.getSchedules().size());
            final OnCallSchedule schedule = night.getSchedules().get(0);
            assertEquals("admin", schedule.getUser().getUsername());
            assertEquals(OnCallSchedule.Type.daily, schedule.getType());
            assertEquals(1, schedule.getTimes().size());
            final OnCallScheduleTime time = schedule.getTimes().get(0);
            assertNull(time.getTimeId());
            assertNull(time.getDay());
            assertEquals("00:00:00", time.getBegins());
            assertEquals("06:00:00", time.getEnds());
        });
        // the times of the old schedule went with it
        assertEquals(1, count("oncall_schedule_times"));
        assertTrue(m_messages.toString(), m_messages.contains("on-call roles: 0 imported, 1 overwritten, 0 existing skipped"));
    }

    @Test
    public void testReferencesResolveAgainstTheDatabase() throws Exception {
        final int custom = id("INSERT INTO security_roles (id, name, builtin) VALUES (nextval('security_roles_id_seq'), 'ROLE_CUSTOM', false) RETURNING id");
        writeUsers("<user><user-id>carol</user-id><password>carolpw</password><role>ROLE_CUSTOM</role></user>");
        writeGroups("<groups><group><name>Ops2</name><user>jane</user></group></groups>"
                + "<roles><role name=\"Day\" membership-group=\"Ops\" supervisor=\"admin\">"
                + "<schedule name=\"jane\" type=\"daily\"><time begins=\"08:00:00\" ends=\"17:00:00\"/></schedule>"
                + "</role></roles>");

        final UsersGroupsXmlImport xmlImport = importFiles(Existing.SKIP, true);

        assertTrue(xmlImport.existingEntries().isEmpty());
        assertFalse(m_messages.toString(), m_messages.stream().anyMatch(m -> m.startsWith("Skipped")));
        final int carol = id("SELECT id FROM users WHERE username = 'carol'");
        assertEquals(List.of(custom), ids("SELECT role_id FROM user_security_roles WHERE user_id = ?", carol));
        assertEquals(List.of(m_jane), ids("SELECT m.user_id FROM group_members m JOIN groups g ON g.id = m.group_id WHERE g.name = 'Ops2'"));
        assertEquals(List.of(m_ops), ids("SELECT membership_group_id FROM oncall_roles WHERE name = 'Day'"));
        assertEquals(List.of(m_admin), ids("SELECT supervisor_user_id FROM oncall_roles WHERE name = 'Day'"));
        assertEquals(List.of(m_jane), ids("SELECT s.user_id FROM oncall_schedules s JOIN oncall_roles r ON r.id = s.oncall_role_id WHERE r.name = 'Day'"));
    }

    @Test
    public void testUnknownSecurityRoleMessage() throws Exception {
        writeUsers("<user><user-id>dan</user-id><password>danpw</password><role>ROLE_NOPE</role><role>ROLE_USER</role></user>");

        importFiles(Existing.SKIP, true);

        assertTrue(m_messages.toString(), m_messages.contains("Skipped user 'dan': security role 'ROLE_NOPE' does not exist"));
        m_sessionUtils.withReadOnlyTransaction(() -> assertEquals(List.of(SecurityRoles.ROLE_USER), roleNames(m_userDao.findByUsername("dan"))));
    }

    @Test
    public void testExistingSecurityRolesAreLeftAlone() throws Exception {
        m_jdbc.update("INSERT INTO security_roles (id, name, builtin) VALUES (nextval('security_roles_id_seq'), 'ROLE_CUSTOM', false)");
        write(SECURITY_ROLES, "roles=custom, admin");
        final List<Map<String, Object>> roles = securityRoles();
        final List<Map<String, Object>> versions = usermgmtVersions();

        final UsersGroupsXmlImport xmlImport = importFiles(Existing.OVERWRITE, true);

        assertEquals(roles, securityRoles());
        assertEquals(versions, usermgmtVersions());
        assertTrue(xmlImport.existingEntries().isEmpty());
    }

    @Test
    public void testVersionsMoveForChangedEntries() throws Exception {
        writeUsers("<user><user-id>jane</user-id><full-name>Jane Smith</full-name><password salt=\"true\">janepw</password></user>");
        writeGroups("<groups><group><name>Ops</name><user>jane</user></group></groups>");
        final int janeVersion = version("users", m_jane);
        final int opsVersion = version("groups", m_ops);
        final List<Map<String, Object>> versions = usermgmtVersions();

        importFiles(Existing.OVERWRITE, true);

        assertTrue(version("users", m_jane) > janeVersion);
        assertTrue(version("groups", m_ops) > opsVersion);
        final List<Map<String, Object>> after = usermgmtVersions();
        for (int i = 0; i < versions.size(); i++) {
            assertEquals(versions.get(i).get("name"), after.get(i).get("name"));
            assertTrue(after.get(i).get("name") + " moves",
                    ((Number) after.get(i).get("version")).longValue() > ((Number) versions.get(i).get("version")).longValue());
        }
    }

    @Test
    public void testNoDecodeKeepsEntities() throws Exception {
        // "a &amp; b" once the XML is parsed
        writeUsers("<user><user-id>dan</user-id><full-name>a &amp;amp; b</full-name><password>danpw</password>"
                + "<contact type=\"email\" info=\"x&amp;amp;y@example.org\"/>"
                + "<contact type=\"textPage\" info=\"5551234\" serviceProvider=\"A&amp;amp;B\"/></user>");

        importFiles(Existing.SKIP, false);
        m_sessionUtils.withReadOnlyTransaction(() -> {
            final UserAccount dan = m_userDao.findByUsername("dan");
            assertEquals("a &amp; b", dan.getFullName());
            assertEquals("x&amp;y@example.org", dan.getContact(ContactType.email).getInfo());
            assertEquals("A&amp;B", dan.getContact(ContactType.textPage).getServiceProvider());
        });

        importFiles(Existing.OVERWRITE, true);
        m_sessionUtils.withReadOnlyTransaction(() -> {
            final UserAccount dan = m_userDao.findByUsername("dan");
            assertEquals("a & b", dan.getFullName());
            assertEquals("x&y@example.org", dan.getContact(ContactType.email).getInfo());
            final UserContact textPage = dan.getContact(ContactType.textPage);
            assertEquals("A&B", textPage.getServiceProvider());
        });
    }

    @Test
    public void testRepeatedEntryInFileWithOverwrite() throws Exception {
        writeUsers("<user><user-id>jane</user-id><full-name>First</full-name><password>a</password></user>"
                + "<user><user-id>jane</user-id><full-name>Second</full-name><password>b</password></user>");

        final UsersGroupsXmlImport xmlImport = importFiles(Existing.OVERWRITE, true);

        assertEquals(Map.of("users", List.of("jane")), xmlImport.existingEntries());
        m_sessionUtils.withReadOnlyTransaction(() -> assertEquals("Second", m_userDao.findByUsername("jane").getFullName()));
        assertEquals(m_messages.toString(), List.of("Warning: User 'jane' is in users.xml more than once; the last one is used"),
                m_messages.stream().filter(m -> m.startsWith("Warning")).collect(Collectors.toList()));
        assertTrue(m_messages.toString(), m_messages.contains("users: 0 imported, 1 overwritten, 0 existing skipped"));
    }

    @Test
    public void testFindExistingMatchesImport() throws Exception {
        writeSkipFiles();
        final Map<String, List<String>> found;
        try (final Connection connection = DataSourceFactory.getInstance().getConnection()) {
            found = UsersGroupsXmlImport.findExisting(connection, users(), groups(), onCallRoles());
        }

        final UsersGroupsXmlImport xmlImport = importFiles(Existing.SKIP, true);

        assertEquals(Map.of("users", List.of("admin"), "groups", List.of("Ops")), found);
        assertEquals(xmlImport.existingEntries(), found);
    }

    @Test
    public void testOverwriteKeepsReferencesFromEntriesNotInTheFile() throws Exception {
        writeUsers("<user><user-id>jane</user-id><full-name>Jane Smith</full-name><password>newpw</password></user>");

        final UsersGroupsXmlImport xmlImport = importFiles(Existing.OVERWRITE, true);

        assertEquals(Map.of("users", List.of("jane")), xmlImport.existingEntries());
        m_sessionUtils.withReadOnlyTransaction(() -> {
            assertEquals("Jane Smith", m_userDao.findByUsername("jane").getFullName());
            // Ops and Night are not in the file, so they keep jane
            assertEquals(List.of("jane"), usernames(m_groupDao.findByName("Ops").getMembers()));
            assertEquals(List.of("jane"), m_onCallRoleDao.findByName("Night").getSchedules().stream()
                    .map(s -> s.getUser().getUsername()).collect(Collectors.toList()));
        });
    }

    @Test
    public void testSkipLeavesExistingOnCallRole() throws Exception {
        writeGroups("<roles><role name=\"Night\" membership-group=\"Ops\" supervisor=\"admin\" description=\"Nights\">"
                + "<schedule name=\"admin\" type=\"daily\"><time begins=\"00:00:00\" ends=\"06:00:00\"/></schedule>"
                + "</role></roles>");
        final int nightVersion = version("oncall_roles", m_night);
        final Map<String, List<String>> found;
        try (final Connection connection = DataSourceFactory.getInstance().getConnection()) {
            found = UsersGroupsXmlImport.findExisting(connection, users(), groups(), onCallRoles());
        }

        final UsersGroupsXmlImport xmlImport = importFiles(Existing.SKIP, true);

        assertEquals(Map.of("on-call roles", List.of("Night")), found);
        assertEquals(found, xmlImport.existingEntries());
        assertEquals(nightVersion, version("oncall_roles", m_night));
        m_sessionUtils.withReadOnlyTransaction(() -> {
            final OnCallRole night = m_onCallRoleDao.findByName("Night");
            assertEquals("Night shift", night.getDescription());
            assertEquals(List.of("jane"), night.getSchedules().stream().map(s -> s.getUser().getUsername()).collect(Collectors.toList()));
        });
        assertTrue(m_messages.toString(), m_messages.contains("on-call roles: 0 imported, 0 overwritten, 1 existing skipped"));
    }

    @Test
    public void testSecurityRolesSummaryHasOnlyImported() throws Exception {
        m_jdbc.update("INSERT INTO security_roles (id, name, builtin) VALUES (nextval('security_roles_id_seq'), 'ROLE_CUSTOM', false)");
        write(SECURITY_ROLES, "roles=custom, extra, admin");

        importFiles(Existing.OVERWRITE, true);

        assertEquals(m_messages.toString(), List.of("custom security roles: 1 imported"),
                m_messages.stream().filter(m -> m.startsWith("custom security roles")).collect(Collectors.toList()));
    }

    @Test
    public void testOverwriteTakingRoleAdminFromAdminIsAProblem() throws Exception {
        // jane stays an administrator, so only the rule for admin is broken
        grantRoleAdmin(m_jane);
        writeUsers("<user><user-id>admin</user-id><password>adminpw</password><role>ROLE_USER</role></user>");

        importFiles(Existing.OVERWRITE, true);

        assertEquals(List.of("The admin user must keep the ROLE_ADMIN role."), m_problems);
    }

    @Test
    public void testOverwriteLeavingNoAdministratorIsAProblem() throws Exception {
        deleteAdmin();
        grantRoleAdmin(m_jane);
        writeUsers("<user><user-id>jane</user-id><password>janepw</password><role>ROLE_USER</role></user>");

        importFiles(Existing.OVERWRITE, true);

        assertEquals(List.of("At least one user must keep the ROLE_ADMIN role, but the import takes it from all who hold it: jane"), m_problems);
    }

    @Test
    public void testUnknownRoleForAdminIsAProblem() throws Exception {
        writeUsers("<user><user-id>admin</user-id><password>adminpw</password><role>ROLE_NOPE</role></user>");

        importFiles(Existing.OVERWRITE, true);

        assertTrue(m_messages.toString(), m_messages.contains("Skipped user 'admin': security role 'ROLE_NOPE' does not exist"));
        assertEquals(List.of("The admin user must keep the ROLE_ADMIN role.",
                "At least one user must keep the ROLE_ADMIN role, but the import takes it from all who hold it: admin"), m_problems);
    }

    @Test
    public void testSkipNeverHasProblems() throws Exception {
        writeUsers("<user><user-id>admin</user-id><password>adminpw</password><role>ROLE_USER</role></user>");

        importFiles(Existing.SKIP, true);

        assertEquals(List.of(), m_problems);
        m_sessionUtils.withReadOnlyTransaction(() -> assertEquals(List.of(SecurityRoles.ROLE_ADMIN), roleNames(m_userDao.findByUsername("admin"))));
    }

    @Test
    public void testNoAdministratorBeforeOnlyWarns() throws Exception {
        deleteAdmin();
        assertEquals(0, id("SELECT COUNT(*) FROM user_security_roles r JOIN security_roles s ON s.id = r.role_id WHERE s.name = ?", SecurityRoles.ROLE_ADMIN));
        writeUsers("<user><user-id>bob</user-id><password>bobpw</password></user>");

        importFiles(Existing.OVERWRITE, true);

        assertEquals(List.of(), m_problems);
        assertTrue(m_messages.toString(), m_messages.contains("Warning: No user holds ROLE_ADMIN."));
    }

    @Test
    public void testNewAdministratorAfterNoneBeforeDoesNotWarn() throws Exception {
        deleteAdmin();
        writeUsers("<user><user-id>bob</user-id><password>bobpw</password><role>ROLE_ADMIN</role></user>");

        importFiles(Existing.OVERWRITE, true);

        assertEquals(List.of(), m_problems);
        assertFalse(m_messages.toString(), m_messages.stream().anyMatch(m -> m.startsWith("Warning")));
    }

    @Test
    public void testNewAdminWithoutRoleAdminIsNoProblem() throws Exception {
        // the rule for admin is about an admin that the import overwrites; jane keeps an administrator
        deleteAdmin();
        grantRoleAdmin(m_jane);
        writeUsers("<user><user-id>admin</user-id><password>adminpw</password><role>ROLE_USER</role></user>");

        importFiles(Existing.OVERWRITE, true);

        assertEquals(List.of(), m_problems);
        m_sessionUtils.withReadOnlyTransaction(() -> assertEquals(List.of(SecurityRoles.ROLE_USER), roleNames(m_userDao.findByUsername("admin"))));
    }

    @Test
    public void testKindWithOnlyInvalidEntriesHasSummary() throws Exception {
        writeUsers("<user><user-id>" + "x".repeat(257) + "</user-id><password>pw</password></user>");
        writeGroups("<groups><group><name>Night shift</name><user>jane</user></group></groups>"
                + "<roles><role name=\"Day\" membership-group=\"Nobody\" supervisor=\"admin\">"
                + "<schedule name=\"jane\" type=\"daily\"><time begins=\"08:00:00\" ends=\"17:00:00\"/></schedule>"
                + "</role></roles>");

        importFiles(Existing.SKIP, true);

        assertTrue(m_messages.toString(), m_messages.contains("users: 0 imported, 0 overwritten, 0 existing skipped"));
        assertTrue(m_messages.toString(), m_messages.contains("groups: 1 imported, 0 overwritten, 0 existing skipped"));
        assertTrue(m_messages.toString(), m_messages.contains("on-call roles: 0 imported, 0 overwritten, 0 existing skipped"));
        assertTrue(m_messages.toString(), m_messages.contains("Entries skipped: 2, warnings: 0; see the messages above"));
    }

    /** The files of {@link #testSkipLeavesExistingEntriesAndListsThem()}. */
    private void writeSkipFiles() throws Exception {
        writeUsers("<user><user-id>admin</user-id><full-name>Someone Else</full-name><password>other</password></user>"
                + "<user><user-id>bob</user-id><password>bobpw</password></user>");
        writeGroups("<groups><group><name>Ops</name><user>bob</user></group>"
                + "<group><name>Dev</name><user>admin</user><user>bob</user></group></groups>");
    }

    /** Imports the files in one transaction, which it commits, and keeps the administrator problems in m_problems. */
    private UsersGroupsXmlImport importFiles(final Existing existing, final boolean decode) throws Exception {
        m_messages.clear();
        final List<String> securityRoles = UsersGroupsXmlImport.readSecurityRoles(new File(m_files, SECURITY_ROLES));
        final List<User> users = users();
        final List<Group> groups = groups();
        final List<Role> onCallRoles = onCallRoles();
        try (final Connection connection = DataSourceFactory.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            final UsersGroupsXmlImport xmlImport = new UsersGroupsXmlImport(connection, existing, decode, m_messages::add);
            xmlImport.securityRoles(securityRoles);
            xmlImport.users(users);
            xmlImport.groups(groups);
            xmlImport.onCallRoles(onCallRoles);
            xmlImport.logSummary();
            m_problems = xmlImport.administratorProblems();
            connection.commit();
            connection.setAutoCommit(true);
            return xmlImport;
        }
    }

    private List<User> users() {
        final Userinfo userinfo = UsersGroupsXmlImport.read(Userinfo.class, new File(m_files, USERS_XML));
        return userinfo == null ? Collections.emptyList() : userinfo.getUsers();
    }

    private List<Group> groups() {
        final Groupinfo groupinfo = UsersGroupsXmlImport.read(Groupinfo.class, new File(m_files, GROUPS_XML));
        return groupinfo == null ? Collections.emptyList() : groupinfo.getGroups();
    }

    private List<Role> onCallRoles() {
        final Groupinfo groupinfo = UsersGroupsXmlImport.read(Groupinfo.class, new File(m_files, GROUPS_XML));
        return groupinfo == null ? Collections.emptyList() : groupinfo.getRoles();
    }

    private void writeUsers(final String users) throws Exception {
        write(USERS_XML, "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"><users>" + users + "</users></userinfo>");
    }

    private void writeGroups(final String content) throws Exception {
        write(GROUPS_XML, "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\">" + content + "</groupinfo>");
    }

    private void write(final String name, final String content) throws Exception {
        write(new File(m_files, name), content);
    }

    private static void write(final File file, final String content) throws Exception {
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }

    private void grantRoleAdmin(final int userId) {
        m_jdbc.update("INSERT INTO user_security_roles (user_id, role_id) SELECT ?, id FROM security_roles WHERE name = ?", userId, SecurityRoles.ROLE_ADMIN);
    }

    /** Leaves no user with ROLE_ADMIN: admin is the only one the seed gives it. */
    private void deleteAdmin() {
        // Night's supervisor cannot be deleted
        m_jdbc.update("UPDATE oncall_roles SET supervisor_user_id = ? WHERE id = ?", m_jane, m_night);
        m_jdbc.update("DELETE FROM users WHERE id = ?", m_admin);
    }

    private int id(final String sql, final Object... args) {
        return m_jdbc.queryForObject(sql, Integer.class, args);
    }

    private List<Integer> ids(final String sql, final Object... args) {
        return m_jdbc.queryForList(sql, Integer.class, args);
    }

    private int version(final String table, final int id) {
        return id("SELECT version FROM " + table + " WHERE id = ?", id);
    }

    private int count(final String table) {
        return id("SELECT COUNT(*) FROM " + table);
    }

    private List<Map<String, Object>> securityRoles() {
        return m_jdbc.queryForList("SELECT id, name, builtin, version FROM security_roles ORDER BY id");
    }

    private List<Map<String, Object>> usermgmtVersions() {
        return m_jdbc.queryForList("SELECT name, version FROM usermgmt_version ORDER BY name");
    }

    private static List<String> roleNames(final UserAccount user) {
        return user.getSecurityRoles().stream().map(SecurityRole::getName).sorted().collect(Collectors.toList());
    }

    private static List<String> usernames(final List<UserAccount> users) {
        return users.stream().map(UserAccount::getUsername).collect(Collectors.toList());
    }
}
