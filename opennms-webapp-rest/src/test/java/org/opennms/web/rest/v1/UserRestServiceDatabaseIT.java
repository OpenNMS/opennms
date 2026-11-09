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
package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.destinationPaths.Path;
import org.opennms.netmgt.config.destinationPaths.Target;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.v2.UserMgmtRestSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The v1 user endpoints with the database store (no mock user and group managers, unlike {@link UserRestServiceIT}):
 * a write is validated and guarded like the v2 one and answers 400 or 409 for what it refuses, never 500.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase(reuseDatabase = false)
public class UserRestServiceDatabaseIT extends AbstractSpringJerseyRestTestCase {
    private static final String PASSWORD = "s3cret";
    /** A password is only accepted in plain text, with hashPassword. */
    private static final String USERS = "/users?hashPassword=true";

    @Autowired
    private DataSource m_dataSource;

    @Autowired
    private UserManager m_userManager;

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private UserAccountDao m_userAccountDao;

    @Autowired
    private UserGroupDao m_userGroupDao;

    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    @Autowired
    private SecurityRoleDao m_securityRoleDao;

    @Override
    protected void beforeServletStart() {
        MockLogAppender.setupLogging();
    }

    @Before
    public void createAdmin() {
        // the database starts without users; the REST calls run as admin, which must exist for its own protections
        inTx(() -> {
            if (m_userAccountDao.findByUsername("admin") == null) {
                final UserAccount admin = new UserAccount("admin");
                admin.setPasswordHash("");
                admin.getSecurityRoles().add(m_securityRoleDao.findByName("ROLE_ADMIN"));
                m_userAccountDao.save(admin);
            }
            return null;
        });
        setUser("admin", new String[] { "ROLE_ADMIN" });
    }

    @After
    public void restoreDestinationPaths() {
        UserMgmtRestSupport.setDestinationPaths(UserMgmtRestSupport.NOTIFD_DESTINATION_PATHS);
    }

    @Test
    public void testCreateRefusesReservedAndInvalidIds() throws Exception {
        for (final String id : new String[] { "item", "a/b", "&lt;b&gt;", "ADMIN" }) {
            sendPost(USERS, userXml(id, null, null), 400);
            assertFalse(id + " must not exist", exists(id));
        }
        assertEquals(1, count("select count(*) from users"));
    }

    @Test
    public void testBadDutyScheduleIsRefused() throws Exception {
        sendPost(USERS, userXml("duty", "garbage", null), 400);
        assertFalse(exists("duty"));

        createUser("duty");
        sendPut("/users/duty", "dutySchedule=garbage", 400);
        assertEquals(0, count("select count(*) from user_duty_schedules where user_id = (select id from users where username = 'duty')"));
    }

    @Test
    public void testStoredBadDutyScheduleIsGrandfathered() throws Exception {
        createUser("old");
        new JdbcTemplate(m_dataSource).update("insert into user_duty_schedules (user_id, position, schedule) select id, 0, 'garbage' from users where username = 'old'");
        // the manager's cache was loaded before this edit behind its back
        m_userManager.reload();

        sendPut("/users/old", "fullName=x", 204);
        assertEquals("x", new JdbcTemplate(m_dataSource).queryForObject("select full_name from users where username = 'old'", String.class));
    }

    @Test
    public void testStoredBlankDutyScheduleIsGrandfathered() throws Exception {
        createUser("blank");
        new JdbcTemplate(m_dataSource).update("insert into user_duty_schedules (user_id, position, schedule) select id, 0, '' from users where username = 'blank'");
        // the manager's cache was loaded before this edit behind its back
        m_userManager.reload();

        sendPut("/users/blank", "fullName=x", 204);
        assertEquals("x", new JdbcTemplate(m_dataSource).queryForObject("select full_name from users where username = 'blank'", String.class));
        // the v1 save goes through the users.xml model of the user manager, which drops a blank schedule (as before);
        // the duty evaluation skips a blank schedule anyway, so the user is treated as always on duty either way
        assertEquals(List.of(), new JdbcTemplate(m_dataSource).queryForList("select schedule from user_duty_schedules"
                + " where user_id = (select id from users where username = 'blank')", String.class));
    }

    @Test
    public void testUnknownRoleIs400() throws Exception {
        sendPost(USERS, userXml("roley", null, "ROLE_BOGUS"), 400);
        assertFalse(exists("roley"));

        createUser("roley");
        assertEquals("Unknown security role ROLE_BOGUS.", sendData(PUT, "application/x-www-form-urlencoded", "/users/roley", "roles=ROLE_BOGUS", 400).getContentAsString());
        // the role endpoints look the role up in the same place
        assertEquals("Unknown security role ROLE_BOGUS.", sendData(PUT, "application/xml", "/users/roley/roles/ROLE_BOGUS", "", 400).getContentAsString());
        assertEquals("Unknown security role ROLE_BOGUS.", sendData(DELETE, "application/xml", "/users/roley/roles/ROLE_BOGUS", "", 400).getContentAsString());
    }

    @Test
    public void testAdminKeepsRoleAdmin() throws Exception {
        final String message = "The admin user must keep the ROLE_ADMIN role.";
        assertEquals(message, sendData(POST, "application/xml", USERS, userXml("admin", null, "ROLE_USER"), 400).getContentAsString());
        assertEquals(message, sendData(DELETE, "application/xml", "/users/admin/roles/ROLE_ADMIN", "", 400).getContentAsString());
        assertEquals(message, sendData(PUT, "application/x-www-form-urlencoded", "/users/admin", "roles=ROLE_USER", 400).getContentAsString());
        assertEquals(List.of("ROLE_ADMIN"), roles("admin"));
    }

    @Test
    public void testLastAdministratorCannotBeDeletedOrDemoted() throws Exception {
        createUser("second");
        sendRequest(PUT, "/users/second/roles/ROLE_ADMIN", 204);

        // two administrators: the second can be demoted, in each of the three ways
        sendRequest(DELETE, "/users/second/roles/ROLE_ADMIN", 204);
        sendRequest(PUT, "/users/second/roles/ROLE_ADMIN", 204);
        sendPut("/users/second", "roles=ROLE_USER", 204);
        assertEquals(List.of("ROLE_USER"), roles("second"));
        sendRequest(PUT, "/users/second/roles/ROLE_ADMIN", 204);
        sendPost(USERS, userXml("second", null, "ROLE_USER"), 201);
        assertEquals(List.of("ROLE_USER"), roles("second"));

        // admin is protected by its own rule, so take the role from admin behind the API's back: second is the only holder
        sendRequest(PUT, "/users/second/roles/ROLE_ADMIN", 204);
        inTx(() -> {
            m_userAccountDao.findByUsername("admin").getSecurityRoles().clear();
            return null;
        });
        final String last = "User second is the only user with the ROLE_ADMIN role and cannot lose the ROLE_ADMIN role; give the role to another user first.";
        assertEquals(last, sendData(DELETE, "application/xml", "/users/second/roles/ROLE_ADMIN", "", 409).getContentAsString());
        assertEquals(last, sendData(PUT, "application/x-www-form-urlencoded", "/users/second", "roles=ROLE_USER", 409).getContentAsString());
        assertEquals(last, sendData(POST, "application/xml", USERS, userXml("second", null, "ROLE_USER"), 409).getContentAsString());
        assertEquals("User second is the only user with the ROLE_ADMIN role and cannot be deleted; give the role to another user first.",
                sendData(DELETE, "application/xml", "/users/second", "", 409).getContentAsString());
        assertTrue(roles("second").contains("ROLE_ADMIN"));
        assertTrue(exists("second"));
    }

    @Test
    public void testSystemUsersCannotBeDeleted() throws Exception {
        inTx(() -> {
            final UserAccount rtc = new UserAccount("rtc");
            rtc.setPasswordHash("");
            m_userAccountDao.save(rtc);
            return null;
        });
        assertEquals("The system user admin cannot be deleted.", sendData(DELETE, "application/xml", "/users/admin", "", 400).getContentAsString());
        assertEquals("The system user rtc cannot be deleted.", sendData(DELETE, "application/xml", "/users/rtc", "", 400).getContentAsString());
        assertTrue(exists("admin"));
        assertTrue(exists("rtc"));
    }

    @Test
    public void testSupervisorCannotBeDeleted() throws Exception {
        createUser("boss");
        inTx(() -> {
            final UserGroup group = new UserGroup("bossgroup");
            m_userGroupDao.save(group);
            m_onCallRoleDao.save(new OnCallRole("bossrole", group, m_userAccountDao.findByUsername("boss")));
            return null;
        });

        assertEquals("User boss is the supervisor of on-call role(s) bossrole; assign a different supervisor first.",
                sendData(DELETE, "application/xml", "/users/boss", "", 409).getContentAsString());
        assertTrue(exists("boss"));
    }

    @Test
    public void testDestinationPathTargetCannotBeDeleted() throws Exception {
        createUser("target");
        final Path path = new Path("night");
        path.addTarget(new Target("target", "javaEmail"));
        UserMgmtRestSupport.setDestinationPaths(() -> (Collection<Path>) List.of(path));

        assertEquals("User target is a target of destination path(s) night; remove it from them first.",
                sendData(DELETE, "application/xml", "/users/target", "", 409).getContentAsString());
        assertTrue(exists("target"));

        UserMgmtRestSupport.setDestinationPaths(() -> List.<Path>of());
        sendRequest(DELETE, "/users/target", 204);
        assertFalse(exists("target"));
    }

    @Test
    public void testPostTakesOnlyAPlainPasswordWithHashPassword() throws Exception {
        final String noHash = "Send the password in plain text with hashPassword=true; a password hash is not accepted.";
        final String required = "A password is required.";
        // a new user: the password is plain text, hashed and salted, and passwordSalt is ignored
        assertEquals(noHash, sendData(POST, "application/xml", "/users", userXml("plain", null, null), 400).getContentAsString());
        assertEquals(required, sendData(POST, "application/xml", USERS, "<user><user-id>plain</user-id></user>", 400).getContentAsString());
        assertEquals(required, sendData(POST, "application/xml", USERS,
                "<user><user-id>plain</user-id><password> </password></user>", 400).getContentAsString());
        assertFalse(exists("plain"));
        sendPost(USERS, "<user><user-id>plain</user-id><password>" + PASSWORD + "</password><passwordSalt>false</passwordSalt></user>",
                201, null);
        assertTrue(m_userManager.comparePasswords("plain", PASSWORD));
        assertTrue(salted("plain"));
        final String hash = passwordHash("plain");

        // an overwrite may send the stored password back unchanged, as GET returns it, without hashPassword
        final String read = sendRequest(GET, "/users/plain", 200);
        assertTrue(read, read.contains("<password>" + hash + "</password>"));
        sendPost("/users", read.replace("<user-id>plain</user-id>", "<user-id>plain</user-id><full-name>Round Trip</full-name>"), 201, "/users/plain");
        assertEquals("Round Trip", new JdbcTemplate(m_dataSource).queryForObject("select full_name from users where username = 'plain'", String.class));
        assertEquals(hash, passwordHash("plain"));
        assertTrue(salted("plain"));

        // the stored hash with hashPassword would be hashed again
        assertEquals("The password is the stored hash; send it without hashPassword to keep it.",
                sendData(POST, "application/xml", USERS, read, 400).getContentAsString());
        assertEquals(hash, passwordHash("plain"));

        // any other password needs hashPassword, and a missing one is refused
        assertEquals(noHash, sendData(POST, "application/xml", "/users", userXml("plain", null, null), 400).getContentAsString());
        assertEquals(noHash, sendData(POST, "application/xml", "/users", read.replace("<passwordSalt>true</passwordSalt>",
                "<passwordSalt>false</passwordSalt>"), 400).getContentAsString());
        assertEquals(required, sendData(POST, "application/xml", USERS, "<user><user-id>plain</user-id></user>", 400).getContentAsString());
        assertEquals(hash, passwordHash("plain"));

        sendPost(USERS, userXml("plain", null, null).replace(PASSWORD, "other"), 201, null);
        assertTrue(m_userManager.comparePasswords("plain", "other"));
        assertTrue(salted("plain"));
    }

    /** The Location of a new user carries no query, such as the ?hashPassword=true every password needs. */
    @Test
    public void testTheLocationOfANewUserHasNoQuery() throws Exception {
        // sendPost keeps the query in the request path; here it is a query string, as from a real client
        final MockHttpServletRequest request = createRequest(POST, "/users");
        request.setQueryString("hashPassword=true");
        request.addParameter("hashPassword", "true");
        request.setContentType("application/xml");
        request.setContent(userXml("located", null, null).getBytes(StandardCharsets.UTF_8));
        final MockHttpServletResponse response = createResponse();
        dispatch(request, response);

        assertEquals(201, response.getStatus());
        final String location = String.valueOf(response.getHeader("Location"));
        assertTrue(location, location.endsWith("/users/located"));
        assertTrue(salted("located"));
    }

    /**
     * A refused change must not stay in the user manager's cache: the next save of any user writes every cached user,
     * so it would reach the database with that unrelated save.
     */
    @Test
    public void testARefusedRoleChangeIsNotSavedWithTheNextUser() throws Exception {
        createUser("bystander");
        sendData(DELETE, "application/xml", "/users/admin/roles/ROLE_ADMIN", "", 400);
        sendPut("/users/bystander", "fullName=First", 204);
        assertEquals(List.of("ROLE_ADMIN"), roles("admin"));

        // the last holder of ROLE_ADMIN (409)
        createUser("second");
        sendRequest(PUT, "/users/second/roles/ROLE_ADMIN", 204);
        inTx(() -> {
            m_userAccountDao.findByUsername("admin").getSecurityRoles().clear();
            return null;
        });
        m_userManager.reload();
        sendData(DELETE, "application/xml", "/users/second/roles/ROLE_ADMIN", "", 409);
        sendPut("/users/bystander", "fullName=Second", 204);
        assertEquals(List.of("ROLE_ADMIN"), roles("second"));
    }

    /** With org.opennms.users.allowUnsalted=false, a write of a user whose stored password is unsalted is a 400, not a 500. */
    @Test
    public void testAStoredUnsaltedPasswordWhenUnsaltedIsNotAllowed() throws Exception {
        createUser("legacy");
        new JdbcTemplate(m_dataSource).update("update users set password_salted = false where username = 'legacy'");
        m_userManager.reload();
        final String read = sendRequest(GET, "/users/legacy", 200);
        final String message = "User legacy has an unsalted password, which org.opennms.users.allowUnsalted=false does not allow;"
                + " set a new password with hashPassword=true.";
        allowUnsalted(false);
        try {
            assertEquals(message, sendData(PUT, "application/x-www-form-urlencoded", "/users/legacy", "fullName=x", 400).getContentAsString());
            assertEquals(message, sendData(PUT, "application/xml", "/users/legacy/roles/ROLE_USER", "", 400).getContentAsString());
            assertEquals(message, sendData(POST, "application/xml", "/users", read, 400).getContentAsString());
            assertEquals("legacy Full Name", new JdbcTemplate(m_dataSource).queryForObject("select full_name from users where username = 'legacy'", String.class));

            // a new password fixes it
            sendPut("/users/legacy", "password=n3w&hashPassword=true", 204);
            assertTrue(salted("legacy"));
        } finally {
            allowUnsalted(true);
        }
    }

    @Test
    public void testFormPutTakesOnlyAPlainPasswordWithHashPassword() throws Exception {
        createUser("formpw");
        final String hash = passwordHash("formpw");
        assertEquals("Send the password in plain text with hashPassword=true; a password hash is not accepted.",
                sendData(PUT, "application/x-www-form-urlencoded", "/users/formpw", "password=" + hash, 400).getContentAsString());
        assertEquals("A password is required.",
                sendData(PUT, "application/x-www-form-urlencoded", "/users/formpw", "password=&hashPassword=true", 400).getContentAsString());
        assertEquals(hash, passwordHash("formpw"));

        sendPut("/users/formpw", "password=n3w&hashPassword=true", 204);
        assertTrue(m_userManager.comparePasswords("formpw", "n3w"));
        assertTrue(salted("formpw"));
        // without a password, hashPassword changes nothing
        final String newHash = passwordHash("formpw");
        sendPut("/users/formpw", "hashPassword=true", 304);
        sendPut("/users/formpw", "email=formpw@example.org&hashPassword=true", 204);
        assertEquals(newHash, passwordHash("formpw"));
    }

    @Test
    public void testFormKeysOutsideTheAllowlistAre400() throws Exception {
        createUser("keys");
        final String hash = passwordHash("keys");
        final String supported = "The form field %s is not supported. Supported fields: comments, dutySchedule, email, fullName, hashPassword, password, roles.";
        final String rename = "Renaming is not supported by the deprecated v1 API; use PUT /api/v2/users/{userId}/rename.";
        // the bean wrapper takes these spellings too, past the checks of the plain fields
        assertEquals(rename, sendData(PUT, "application/x-www-form-urlencoded", "/users/keys", "Username=keys2", 400).getContentAsString());
        assertEquals(rename, sendData(PUT, "application/x-www-form-urlencoded", "/users/keys", "Name=keys2", 400).getContentAsString());
        assertEquals(String.format(supported, "Password"),
                sendData(PUT, "application/x-www-form-urlencoded", "/users/keys", "Password=x&hashPassword=true", 400).getContentAsString());
        assertEquals(String.format(supported, "passwordSalted"),
                sendData(PUT, "application/x-www-form-urlencoded", "/users/keys", "passwordSalted=false", 400).getContentAsString());
        // every key is checked before any is applied
        assertEquals(String.format(supported, "foo"),
                sendData(PUT, "application/x-www-form-urlencoded", "/users/keys", "fullName=changed&foo=bar", 400).getContentAsString());
        assertEquals(String.format(supported, "roles[0]"),
                sendData(PUT, "application/x-www-form-urlencoded", "/users/keys", "roles%5B0%5D=ROLE_ADMIN", 400).getContentAsString());

        assertFalse(exists("keys2"));
        assertEquals(2, count("select count(*) from users"));
        assertEquals("keys Full Name", new JdbcTemplate(m_dataSource).queryForObject("select full_name from users where username = 'keys'", String.class));
        assertEquals(hash, passwordHash("keys"));
        assertTrue(salted("keys"));
        assertEquals(List.of(), roles("keys"));
    }

    @Test
    public void testCommentsAndLengthsAreChecked() throws Exception {
        final String markup = "The comments must not contain any HTML markup.";
        assertEquals(markup, sendData(POST, "application/xml", USERS, userXml("fields", null, null)
                .replace("</user>", "<user-comments>&lt;b&gt;x&lt;/b&gt;</user-comments></user>"), 400).getContentAsString());
        sendPost(USERS, userXml("fields", null, null).replace("<full-name>fields Full Name</full-name>",
                "<full-name>" + "x".repeat(257) + "</full-name>"), 400);
        assertFalse(exists("fields"));

        createUser("fields");
        assertEquals(markup, sendData(PUT, "application/x-www-form-urlencoded", "/users/fields", "comments=%3Cb%3Ex%3C%2Fb%3E", 400).getContentAsString());
        sendPut("/users/fields", "email=" + "x".repeat(300) + "%40example.org", 400);
        sendPut("/users/fields", "comments=" + "x".repeat(4097), 400);

        // stored comments that today's rules refuse are grandfathered
        new JdbcTemplate(m_dataSource).update("update users set comments = '<b>old</b>' where username = 'fields'");
        m_userManager.reload();
        sendPut("/users/fields", "fullName=Still+Editable", 204);
        assertEquals("<b>old</b>", new JdbcTemplate(m_dataSource).queryForObject("select comments from users where username = 'fields'", String.class));
    }

    @Test
    public void testAPathTargetNamedLikeAGroupIsNoObstacle() throws Exception {
        // notifd resolves the target noc as the group noc, so the user noc can go, as in v2
        createUser("noc");
        inTx(() -> {
            m_userGroupDao.save(new UserGroup("noc"));
            return null;
        });
        final Path path = new Path("night");
        path.addTarget(new Target("noc", "javaEmail"));
        UserMgmtRestSupport.setDestinationPaths(() -> (Collection<Path>) List.of(path));

        sendRequest(DELETE, "/users/noc", 204);
        assertFalse(exists("noc"));
    }

    @Test
    public void testRenameThroughFormPutIs400() throws Exception {
        createUser("before");
        assertEquals("Renaming is not supported by the deprecated v1 API; use PUT /api/v2/users/{userId}/rename.",
                sendData(PUT, "application/x-www-form-urlencoded", "/users/before", "username=after", 400).getContentAsString());
        sendPut("/users/before", "Username=after", 400);
        sendPut("/users/before", "name=after", 400);
        assertTrue(exists("before"));
        assertFalse(exists("after"));
    }

    @Test
    public void testIndexedAndNestedFormKeysAre400() throws Exception {
        // the bean wrapper would set roles[0] and dutySchedule[0], past the checks for the roles and duty schedules
        assertEquals("The form field roles[0] is not supported. Supported fields: comments, dutySchedule, email, fullName, hashPassword, password, roles.",
                sendData(PUT, "application/x-www-form-urlencoded", "/users/admin", "roles%5B0%5D=ROLE_USER", 400).getContentAsString());
        assertEquals(List.of("ROLE_ADMIN"), roles("admin"));

        sendPost(USERS, userXml("indexed", "MoTuWeThFr800-1700", null), 201, null);
        sendPut("/users/indexed", "dutySchedule%5B0%5D=garbage", 400);
        sendPut("/users/indexed", "fullName=x&password.bytes=y", 400);
        assertEquals(List.of("MoTuWeThFr800-1700"), new JdbcTemplate(m_dataSource).queryForList("select schedule from user_duty_schedules"
                + " where user_id = (select id from users where username = 'indexed')", String.class));
        assertEquals("indexed Full Name", new JdbcTemplate(m_dataSource).queryForObject("select full_name from users where username = 'indexed'", String.class));
    }

    @Test
    public void testUserWithoutIdIs400() throws Exception {
        assertEquals("user-id is required.", sendData(POST, "application/xml", USERS,
                "<user><full-name>Nobody</full-name><password>" + PASSWORD + "</password></user>", 400).getContentAsString());
        assertEquals(1, count("select count(*) from users"));
    }

    @Test
    public void testTooLongValueIs400() throws Exception {
        createUser("long");
        // 257 code points, two chars each
        final String tooLong = "%F0%9F%98%80".repeat(257);
        sendPut("/users/long", "fullName=" + tooLong, 400);
        assertEquals("long Full Name", new JdbcTemplate(m_dataSource).queryForObject("select full_name from users where username = 'long'", String.class));
    }

    @Test
    public void testWritesNeedTheAdminRole() throws Exception {
        createUser("victim");
        final JdbcTemplate jdbc = new JdbcTemplate(m_dataSource);
        final String message = "User management requires the admin role.";
        final List<String> victimRoles = roles("victim");

        setUser("rest", new String[] { "ROLE_REST" });
        try {
            assertEquals(message, sendData(POST, "application/xml", USERS, userXml("intruder", null, "ROLE_ADMIN"), 403).getContentAsString());
            assertEquals(message, sendData(POST, "application/xml", USERS, userXml("victim", null, "ROLE_ADMIN"), 403).getContentAsString());
            assertEquals(message, sendData(PUT, "application/x-www-form-urlencoded", "/users/victim", "fullName=changed", 403).getContentAsString());
            assertEquals(message, sendData(PUT, "application/xml", "/users/victim/roles/ROLE_ADMIN", "", 403).getContentAsString());
            assertEquals(message, sendData(DELETE, "application/xml", "/users/admin/roles/ROLE_ADMIN", "", 403).getContentAsString());
            assertEquals(message, sendData(DELETE, "application/xml", "/users/victim", "", 403).getContentAsString());
            // a missing user is a 403 too, not a 404
            assertEquals(message, sendData(DELETE, "application/xml", "/users/nobody", "", 403).getContentAsString());
        } finally {
            setUser("admin", new String[] { "ROLE_ADMIN" });
        }

        assertFalse(exists("intruder"));
        assertTrue(exists("victim"));
        assertEquals("victim Full Name", jdbc.queryForObject("select full_name from users where username = 'victim'", String.class));
        assertEquals(victimRoles, roles("victim"));
        assertEquals(List.of("ROLE_ADMIN"), roles("admin"));

        // the admin role still writes
        sendPut("/users/victim", "fullName=changed", 204);
        sendRequest(PUT, "/users/victim/roles/ROLE_PROVISION", 204);
        sendRequest(DELETE, "/users/victim", 204);
        assertFalse(exists("victim"));
    }

    @Test
    public void testCrudStillWorks() throws Exception {
        sendPost(USERS, userXml("crud", "MoTuWeThFr800-1700", "ROLE_USER"), 201, null);
        assertEquals("crud Full Name", new JdbcTemplate(m_dataSource).queryForObject("select full_name from users where username = 'crud'", String.class));
        assertTrue(sendRequest(GET, "/users/crud", 200).contains("<user-id>crud</user-id>"));
        assertEquals(List.of("ROLE_USER"), roles("crud"));

        sendPut("/users/crud", "fullName=Crud+Two&email=crud@example.org&dutySchedule=SaSu0-2359", 204);
        final JdbcTemplate jdbc = new JdbcTemplate(m_dataSource);
        assertEquals("Crud Two", jdbc.queryForObject("select full_name from users where username = 'crud'", String.class));
        final String xml = sendRequest(GET, "/users/crud", 200);
        assertTrue(xml, xml.contains("crud@example.org"));
        assertTrue(xml, xml.contains("SaSu0-2359"));

        sendRequest(PUT, "/users/crud/roles/ROLE_PROVISION", 204);
        assertEquals(List.of("ROLE_PROVISION", "ROLE_USER"), roles("crud"));
        sendRequest(DELETE, "/users/crud/roles/ROLE_PROVISION", 204);
        assertEquals(List.of("ROLE_USER"), roles("crud"));

        sendRequest(DELETE, "/users/crud", 204);
        assertFalse(exists("crud"));
        sendRequest(GET, "/users/crud", 404);
    }

    private void createUser(final String name) throws Exception {
        sendPost(USERS, userXml(name, null, null), 201, null);
    }

    private static String userXml(final String id, final String dutySchedule, final String role) {
        return "<user><user-id>" + id + "</user-id><full-name>" + id + " Full Name</full-name>"
                + "<password>" + PASSWORD + "</password>"
                + (dutySchedule == null ? "" : "<duty-schedule>" + dutySchedule + "</duty-schedule>")
                + (role == null ? "" : "<role>" + role + "</role>")
                + "</user>";
    }

    /** The manager reads the property when it is built; this changes what it read. */
    private void allowUnsalted(final boolean allowed) {
        ReflectionTestUtils.setField((Object) AopTestUtils.getUltimateTargetObject(m_userManager), "m_allowUnsalted", allowed);
    }

    private String passwordHash(final String id) {
        return new JdbcTemplate(m_dataSource).queryForObject("select password_hash from users where username = ?", String.class, id);
    }

    private boolean salted(final String id) {
        return new JdbcTemplate(m_dataSource).queryForObject("select password_salted from users where username = ?", Boolean.class, id);
    }

    private boolean exists(final String id) {
        return count("select count(*) from users where username = '" + id.replace("'", "''") + "'") > 0;
    }

    private int count(final String sql) {
        return new JdbcTemplate(m_dataSource).queryForObject(sql, Integer.class);
    }

    private List<String> roles(final String username) {
        return new JdbcTemplate(m_dataSource).queryForList(
                "select r.name from user_security_roles ur join security_roles r on r.id = ur.role_id"
                + " join users u on u.id = ur.user_id where u.username = ? order by r.name", String.class, username);
    }

    private <T> T inTx(final Supplier<T> work) {
        return new TransactionTemplate(m_transactionManager).execute(status -> work.get());
    }
}
