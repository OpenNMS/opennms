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
package org.opennms.web.rest.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.model.usermgmt.ContactType;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.OnCallSchedule;
import org.opennms.netmgt.model.usermgmt.OnCallScheduleTime;
import org.opennms.netmgt.model.usermgmt.SecurityRole;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
@JUnitTemporaryDatabase
public class UsersRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static final String JSON = MediaType.APPLICATION_JSON;

    /** The database managers, as the JSPs and Spring Security use them. */
    @Autowired
    private UserManager m_userManager;

    @Autowired
    private UserAccountDao m_userAccountDao;

    @Autowired
    private UserGroupDao m_userGroupDao;

    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    @Autowired
    private SecurityRoleDao m_securityRoleDao;

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private DataSource m_dataSource;

    @Autowired
    private SessionFactory m_sessionFactory;

    public UsersRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

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
    }

    private final TestDestinationPaths m_paths = new TestDestinationPaths();

    @After
    public void clearStoreProperty() {
        System.clearProperty("org.opennms.usermgmt.store");
        m_paths.restore();
    }

    @Test
    public void testListNeverContainsPasswordHashes() throws Exception {
        final String json = getJson("/users", 200);
        final JSONArray users = new JSONArray(json);
        boolean foundAdmin = false;
        for (int i = 0; i < users.length(); i++) {
            foundAdmin |= "admin".equals(users.getJSONObject(i).getString("userId"));
        }
        assertTrue(foundAdmin);
        // the v1 API leaks hashes to admins; the v2 contract is that no
        // response ever carries the password in any form
        assertFalse(json.contains("password"));
    }

    @Test
    public void testGetUser() throws Exception {
        final JSONObject admin = new JSONObject(getJson("/users/admin", 200));
        assertEquals("admin", admin.getString("userId"));
        sendRequest(GET, "/users/idontexist", 404);
    }

    @Test
    public void testAvailableRoles() throws Exception {
        final JSONArray roles = new JSONArray(getJson("/users/available-roles", 200));
        boolean foundAdmin = false;
        for (int i = 0; i < roles.length(); i++) {
            foundAdmin |= "ROLE_ADMIN".equals(roles.getString(i));
        }
        assertTrue(foundAdmin);
    }

    @Test
    public void testCreateLifecycle() throws Exception {
        final String body = "{\"userId\":\"junituser\",\"password\":\"S3cret!pw\",\"fullName\":\"JUnit User\","
                + "\"email\":\"junit@example.com\",\"pagerEmail\":\"junit-pager@example.com\","
                + "\"dutySchedules\":[\"MoWeFr800-1700\"],\"roles\":[\"ROLE_USER\"]}";
        final MockHttpServletResponse response = exec(POST, "/users", body, null, 201);
        assertEquals("junituser", new JSONObject(response.getContentAsString()).getString("userId"));
        assertNotNull(response.getHeader("ETag"));

        final JSONObject created = new JSONObject(getJson("/users/junituser", 200));
        assertEquals("JUnit User", created.getString("fullName"));
        assertEquals("junit@example.com", created.getString("email"));
        assertEquals("junit-pager@example.com", created.getString("pagerEmail"));
        assertEquals("MoWeFr800-1700", created.getJSONArray("dutySchedules").getString(0));
        assertEquals("ROLE_USER", created.getJSONArray("roles").getString(0));
        assertTrue(m_userManager.comparePasswords("junituser", "S3cret!pw"));

        // creating the same user again must be rejected
        exec(POST, "/users", body, null, 409);

        sendRequest(DELETE, "/users/junituser", 204);
        sendRequest(GET, "/users/junituser", 404);
    }

    @Test
    public void testCreateDuplicateIs409() throws Exception {
        exec(POST, "/users", "{\"userId\":\"dupuser\",\"password\":\"pw\"}", null, 201);
        final MockHttpServletResponse response = exec(POST, "/users", "{\"userId\":\"dupuser\",\"password\":\"pw\"}", null, 409);
        assertEquals("User dupuser already exists.", response.getContentAsString());
    }

    @Test
    public void testCreateValidation() throws Exception {
        // missing password
        exec(POST, "/users", "{\"userId\":\"nopass\"}", null, 400);
        // markup in the user id
        exec(POST, "/users", "{\"userId\":\"bad<script>\",\"password\":\"x\"}", null, 400);
        // colon breaks basic auth, whitespace breaks group references
        exec(POST, "/users", "{\"userId\":\"with:colon\",\"password\":\"x\"}", null, 400);
        exec(POST, "/users", "{\"userId\":\"with space\",\"password\":\"x\"}", null, 400);
        // unknown security role
        exec(POST, "/users", "{\"userId\":\"badrole\",\"password\":\"x\",\"roles\":[\"ROLE_NOT_A_THING\"]}", null, 400);
        // invalid time zone
        exec(POST, "/users", "{\"userId\":\"badzone\",\"password\":\"x\",\"timeZoneId\":\"Mars/Olympus\"}", null, 400);
        // duty schedules that would break notifd's parsing at runtime
        exec(POST, "/users", "{\"userId\":\"badduty\",\"password\":\"x\",\"dutySchedules\":[\"garbage\"]}", null, 400);
        exec(POST, "/users", "{\"userId\":\"badduty\",\"password\":\"x\",\"dutySchedules\":[\"Mo899-999\"]}", null, 400);
        exec(POST, "/users", "{\"userId\":\"badduty\",\"password\":\"x\",\"dutySchedules\":[\"Mo800-2400\"]}", null, 400);
        // ids that cannot be addressed as a URL path segment
        exec(POST, "/users", "{\"userId\":\"team/lead\",\"password\":\"x\"}", null, 400);
        exec(POST, "/users", "{\"userId\":\"pct%20\",\"password\":\"x\"}", null, 400);
        exec(POST, "/users", "{\"userId\":\"frag#1\",\"password\":\"x\"}", null, 400);
        // none of the rejects may have been created
        sendRequest(GET, "/users/badrole", 404);
        sendRequest(GET, "/users/badzone", 404);
        sendRequest(GET, "/users/badduty", 404);
    }

    @Test
    public void testLengthLimitsAre400() throws Exception {
        final String tooLong = "x".repeat(257);
        exec(POST, "/users", "{\"userId\":\"longname\",\"password\":\"pw\",\"fullName\":\"" + tooLong + "\"}", null, 400);
        exec(POST, "/users", "{\"userId\":\"longmail\",\"password\":\"pw\",\"email\":\"" + tooLong + "\"}", null, 400);
        sendRequest(GET, "/users/longname", 404);
        sendRequest(GET, "/users/longmail", 404);

        exec(POST, "/users", "{\"userId\":\"longedit\",\"password\":\"pw\"}", null, 201);
        final int version = rowVersion("longedit");
        exec(PUT, "/users/longedit", "{\"userId\":\"longedit\",\"fullName\":\"" + tooLong + "\"}", null, 400);
        exec(PUT, "/users/longedit", "{\"userId\":\"longedit\",\"email\":\"" + tooLong + "\"}", null, 400);
        assertEquals(version, rowVersion("longedit"));
        // the limit is 256 characters
        exec(PUT, "/users/longedit", "{\"userId\":\"longedit\",\"fullName\":\"" + "x".repeat(256) + "\"}", null, 200);
    }

    @Test
    public void testCommentLimit() throws Exception {
        final String limit = "x".repeat(4096);
        final String tooLong = "x".repeat(4097);
        exec(POST, "/users", "{\"userId\":\"freetext\",\"password\":\"pw\",\"userComments\":\"" + limit + "\"}", null, 201);
        // surrounding whitespace is trimmed before the length is counted, as before storing
        exec(PUT, "/users/freetext", "{\"userId\":\"freetext\",\"userComments\":\" " + limit + "\\n\"}", null, 200);
        assertEquals(limit, column("freetext", "comments"));
        final MockHttpServletResponse put = exec(PUT, "/users/freetext", "{\"userId\":\"freetext\",\"userComments\":\"" + tooLong + "\"}", null, 400);
        assertEquals("User \"freetext\": the comment is longer than 4096 characters.", put.getContentAsString());
        final MockHttpServletResponse post = exec(POST, "/users", "{\"userId\":\"freetext2\",\"password\":\"pw\",\"userComments\":\"" + tooLong + "\"}", null, 400);
        assertEquals("User \"freetext2\": the comment is longer than 4096 characters.", post.getContentAsString());
        sendRequest(GET, "/users/freetext2", 404);
        assertEquals(limit, column("freetext", "comments"));
        sendRequest(DELETE, "/users/freetext", 204);
    }

    @Test
    public void testStoredLongCommentIsGrandfathered() throws Exception {
        // written before the limit, e.g. by the JSPs or a migration
        final String stored = "y".repeat(5000);
        createUser("longcomment", u -> u.setComments(stored));
        final int version = rowVersion("longcomment");

        // echoed back unchanged, along with another change
        exec(PUT, "/users/longcomment", "{\"userId\":\"longcomment\",\"userComments\":\"" + stored + "\",\"fullName\":\"Long\"}", null, 200);
        assertEquals(stored, column("longcomment", "comments"));
        assertEquals("Long", column("longcomment", "full_name"));
        final int edited = rowVersion("longcomment");
        assertNotEquals(version, edited);
        exec(PUT, "/users/longcomment", "{\"userId\":\"longcomment\",\"userComments\":\"" + stored + "\"}", null, 200);
        assertEquals(edited, rowVersion("longcomment"));

        // but a changed comment over the limit is refused
        exec(PUT, "/users/longcomment", "{\"userId\":\"longcomment\",\"userComments\":\"" + stored + "z\"}", null, 400);
        assertEquals(stored, column("longcomment", "comments"));
        assertEquals(edited, rowVersion("longcomment"));
        sendRequest(DELETE, "/users/longcomment", 204);
    }

    @Test
    public void testErrorsNameTheTrimmedUserId() throws Exception {
        final MockHttpServletResponse response = exec(POST, "/users",
                "{\"userId\":\" spaced \",\"password\":\"pw\",\"fullName\":\"" + "x".repeat(257) + "\"}", null, 400);
        assertEquals("User \"spaced\": the full name is longer than 256 characters.", response.getContentAsString());
    }

    /**
     * The unique (user, contact type) constraint is deferred, so a second contact of a type is checked at commit, after the service method has returned, where a violation would be a 500.
     * {@link UserMgmtRestSupport#flushAndCheckConstraints}, which every write calls after its last change, checks it
     * inside the method instead, so that {@link UserMgmtRestSupport#translate} answers 409.
     */
    @Test
    public void testDeferredDuplicateContactIsReportedBeforeTheCommit() throws Exception {
        createUser("dupcontact", u -> u.setContact(ContactType.email, "a@example.org", null));
        final UserMgmtRestSupport support = new UserMgmtRestSupport();
        support.setSessionFactory(m_sessionFactory);
        final RuntimeException translated = new TransactionTemplate(m_transactionManager).execute(status -> {
            status.setRollbackOnly();
            final Session session = m_sessionFactory.getCurrentSession();
            session.createNativeQuery("INSERT INTO user_contacts (id, user_id, contact_type, info) SELECT nextval('user_contacts_id_seq'), u.id, 'email', 'b@example.org'"
                    + " FROM users u WHERE u.username = 'dupcontact'").executeUpdate();
            // deferred: neither the statement nor a flush reports the duplicate
            session.flush();
            try {
                support.flushAndCheckConstraints();
                return null;
            } catch (final RuntimeException e) {
                assertTrue(e.toString(), e instanceof DataIntegrityViolationException);
                return support.translate("save", e);
            }
        });
        assertNotNull("the duplicate was not reported", translated);
        assertEquals(409, ((WebApplicationException) translated).getResponse().getStatus());
        assertEquals("The change conflicts with related data. Reload and try again.", ((WebApplicationException) translated).getResponse().getEntity());
    }

    /** A fresh database, so that the number of users is known. */
    @Test
    @JUnitTemporaryDatabase
    public void testListStatementCountDoesNotGrowWithTheUsers() throws Exception {
        createListedUsers(0, 2);
        final long few = statements(() -> getJson("/users", 200));
        createListedUsers(2, 7);
        final long many = statements(() -> getJson("/users", 200));
        assertTrue("no statement counted", few > 0);
        assertEquals("statements for 3 users vs. 8 users", few, many);

        // the lists keep their order
        final JSONArray users = new JSONArray(getJson("/users", 200));
        int listed = 0;
        for (int i = 0; i < users.length(); i++) {
            final JSONObject user = users.getJSONObject(i);
            if (user.getString("userId").startsWith("listed")) {
                listed++;
                assertEquals("MoTu900-1000", user.getJSONArray("dutySchedules").getString(0));
                assertEquals("Fr800-1700", user.getJSONArray("dutySchedules").getString(1));
                assertEquals("SaSu100-200", user.getJSONArray("dutySchedules").getString(2));
                assertEquals(user.getString("userId") + "@example.org", user.getString("email"));
                assertEquals(Arrays.asList("ROLE_REST", "ROLE_USER"),
                        Arrays.asList(user.getJSONArray("roles").getString(0), user.getJSONArray("roles").getString(1)));
            }
        }
        assertEquals(7, listed);
    }

    @Test
    public void testInvalidTuiPinRejected() throws Exception {
        createUser("pinuser", u -> { });
        final int version = rowVersion("pinuser");

        exec(PUT, "/users/pinuser", "{\"userId\":\"pinuser\",\"tuiPin\":\"1234x\"}", null, 400);
        assertEquals(version, rowVersion("pinuser"));
        assertNull(column("pinuser", "tui_pin"));

        // a valid follow-up write still succeeds
        exec(PUT, "/users/pinuser", "{\"userId\":\"pinuser\",\"tuiPin\":\"1234\",\"fullName\":\"Pin User\"}", null, 200);
        assertEquals("1234", column("pinuser", "tui_pin"));
        assertEquals("Pin User", column("pinuser", "full_name"));
    }

    @Test
    public void testUpdatePreservesUnexposedContacts() throws Exception {
        // seed a user carrying an XMPP contact, as a migrated users.xml would
        createUser("xmppuser", u -> {
            u.setPasswordHash(new org.jasypt.util.password.StrongPasswordEncryptor().encryptPassword("pw"));
            u.setPasswordSalted(true);
            u.setContact(ContactType.xmppAddress, "xmpp@jabber.example.com", null);
        });

        exec(PUT, "/users/xmppuser", "{\"userId\":\"xmppuser\",\"fullName\":\"XMPP User\",\"email\":\"new@example.com\"}", null, 200);

        final User updated = m_userManager.getUser("xmppuser");
        assertNotNull(updated);
        assertEquals("XMPP User", updated.getFullName().orElse(null));
        assertTrue("the xmppAddress contact must survive an update that does not expose it",
                updated.getContacts().stream().anyMatch(c ->
                        "xmppAddress".equals(c.getType()) && "xmpp@jabber.example.com".equals(c.getInfo().orElse(null))));
        // the password also survives a details update
        assertTrue(m_userManager.comparePasswords("xmppuser", "pw"));

        sendRequest(DELETE, "/users/xmppuser", 204);
    }

    @Test
    public void testTextIsStoredRaw() throws Exception {
        final String fullName = "A & <B> 'c\"";
        final String email = "a&b@x.org";
        final String body = new JSONObject().put("userId", "rawuser").put("password", "pw")
                .put("fullName", fullName).put("email", email).toString();
        exec(POST, "/users", body, null, 201);

        final JSONObject read = new JSONObject(getJson("/users/rawuser", 200));
        assertEquals(fullName, read.getString("fullName"));
        assertEquals(email, read.getString("email"));
        assertEquals(fullName, column("rawuser", "full_name"));
        assertEquals(email, new JdbcTemplate(m_dataSource).queryForObject(
                "select c.info from user_contacts c join users u on u.id = c.user_id where u.username = ? and c.contact_type = 'email'",
                String.class, "rawuser"));

        // an unrelated edit that echoes the values back leaves them as they are
        exec(PUT, "/users/rawuser", new JSONObject().put("userId", "rawuser").put("fullName", fullName)
                .put("email", email).put("userComments", "touched").toString(), null, 200);
        assertEquals(fullName, column("rawuser", "full_name"));
        assertEquals(fullName, m_userManager.getUser("rawuser").getFullName().orElse(null));
    }

    @Test
    public void testEditsPhoneAndPagerContacts() throws Exception {
        exec(POST, "/users", "{\"userId\":\"contactuser\",\"password\":\"pw\","
                + "\"workPhone\":\"555-1000\",\"mobilePhone\":\"555-2000\",\"homePhone\":\"555-3000\","
                + "\"numericPagerService\":\"AcmePage\",\"numericPagerPin\":\"12345\","
                + "\"textPagerService\":\"AcmeText\",\"textPagerPin\":\"67890\"}", null, 201);

        JSONObject u = new JSONObject(getJson("/users/contactuser", 200));
        assertEquals("555-1000", u.getString("workPhone"));
        assertEquals("555-2000", u.getString("mobilePhone"));
        assertEquals("555-3000", u.getString("homePhone"));
        assertEquals("AcmePage", u.getString("numericPagerService"));
        assertEquals("12345", u.getString("numericPagerPin"));
        assertEquals("AcmeText", u.getString("textPagerService"));
        assertEquals("67890", u.getString("textPagerPin"));

        // a partial update touches only the mobile phone; every other contact is preserved
        exec(PUT, "/users/contactuser", "{\"userId\":\"contactuser\",\"mobilePhone\":\"555-9999\"}", null, 200);
        u = new JSONObject(getJson("/users/contactuser", 200));
        assertEquals("555-9999", u.getString("mobilePhone"));
        assertEquals("555-1000", u.getString("workPhone"));
        assertEquals("AcmePage", u.getString("numericPagerService"));
        assertEquals("12345", u.getString("numericPagerPin"));

        sendRequest(DELETE, "/users/contactuser", 204);
    }

    @Test
    public void testPasswordChange() throws Exception {
        exec(POST, "/users", "{\"userId\":\"pwuser\",\"password\":\"first\"}", null, 201);
        assertTrue(m_userManager.comparePasswords("pwuser", "first"));

        exec(PUT, "/users/pwuser/password", "{\"password\":\"second\"}", null, 204);
        assertFalse(m_userManager.comparePasswords("pwuser", "first"));
        assertTrue(m_userManager.comparePasswords("pwuser", "second"));

        exec(PUT, "/users/pwuser/password", "{}", null, 400);
        exec(PUT, "/users/missing/password", "{\"password\":\"x\"}", null, 404);

        sendRequest(DELETE, "/users/pwuser", 204);
    }

    @Test
    public void testRename() throws Exception {
        exec(POST, "/users", "{\"userId\":\"renameme\",\"password\":\"pw\"}", null, 201);
        exec(POST, "/users", "{\"userId\":\"occupied\",\"password\":\"pw\"}", null, 201);

        // renaming onto an existing user must be rejected
        exec(POST, "/users/renameme/rename", "{\"newUserId\":\"occupied\"}", null, 409);
        // markup in the new id
        exec(POST, "/users/renameme/rename", "{\"newUserId\":\"bad<b>\"}", null, 400);
        // the current id: there is nothing to rename
        exec(POST, "/users/renameme/rename", "{\"newUserId\":\"renameme\"}", null, 400);

        final MockHttpServletResponse renamed = exec(POST, "/users/renameme/rename", "{\"newUserId\":\"renamed\"}", null, 200);
        assertEquals("renamed", new JSONObject(renamed.getContentAsString()).getString("userId"));
        sendRequest(GET, "/users/renameme", 404);
        sendRequest(GET, "/users/renamed", 200);

        sendRequest(DELETE, "/users/renamed", 204);
        sendRequest(DELETE, "/users/occupied", 204);
    }

    @Test
    public void testSystemAccountProtections() throws Exception {
        sendRequest(DELETE, "/users/admin", 400);
        exec(POST, "/users/admin/rename", "{\"newUserId\":\"root\"}", null, 400);
        sendRequest(GET, "/users/admin", 200);

        // the protection is by name, wherever the account came from
        exec(POST, "/users", "{\"userId\":\"rtc\",\"password\":\"pw\"}", null, 201);
        sendRequest(DELETE, "/users/rtc", 400);
        exec(POST, "/users/rtc/rename", "{\"newUserId\":\"rtc2\"}", null, 400);
    }

    @Test
    public void testPartialUpdatePreservesRolesAndSchedules() throws Exception {
        exec(POST, "/users",
                "{\"userId\":\"partial\",\"password\":\"pw\",\"roles\":[\"ROLE_USER\"],\"dutySchedules\":[\"MoWeFr800-1700\"]}", null, 201);

        // a body that omits the roles and dutySchedules keys must preserve both
        exec(PUT, "/users/partial", "{\"userId\":\"partial\",\"fullName\":\"Partial Update\"}", null, 200);

        final JSONObject after = new JSONObject(getJson("/users/partial", 200));
        assertEquals("Partial Update", after.getString("fullName"));
        assertEquals("ROLE_USER", after.getJSONArray("roles").getString(0));
        assertEquals("MoWeFr800-1700", after.getJSONArray("dutySchedules").getString(0));

        sendRequest(DELETE, "/users/partial", 204);
    }

    @Test
    public void testRejectedUpdateLeavesNoPartialState() throws Exception {
        exec(POST, "/users",
                "{\"userId\":\"atomic\",\"password\":\"pw\",\"fullName\":\"Original Name\",\"email\":\"orig@example.com\"}", null, 201);
        final int version = rowVersion("atomic");

        // invalid role arrives with new name/email; nothing may be applied
        exec(PUT, "/users/atomic",
                "{\"userId\":\"atomic\",\"fullName\":\"Changed Name\",\"email\":\"changed@example.com\",\"roles\":[\"ROLE_TYPO\"]}", null, 400);

        final JSONObject after = new JSONObject(getJson("/users/atomic", 200));
        assertEquals("Original Name", after.getString("fullName"));
        assertEquals("orig@example.com", after.getString("email"));
        assertEquals("Original Name", column("atomic", "full_name"));
        assertEquals(version, rowVersion("atomic"));
        assertEquals(version, after.getInt("version"));

        sendRequest(DELETE, "/users/atomic", 204);
    }

    @Test
    public void testOvernightDutyScheduleRejectedForNewEntries() throws Exception {
        // DutySchedule.isInSchedule compares within one calendar day, so an
        // overnight range never matches and would silently disable the schedule
        exec(POST, "/users", "{\"userId\":\"nightshift\",\"password\":\"pw\",\"dutySchedules\":[\"MoTu2000-800\"]}", null, 400);
        sendRequest(GET, "/users/nightshift", 404);
    }

    @Test
    public void testPreExistingOddDutyScheduleStaysEditable() throws Exception {
        // migrated data may already carry an overnight string; the
        // record must remain editable when the UI round-trips it unchanged
        createUser("legacynight", u -> u.getDutySchedules().add("MoTu2000-800"));

        exec(PUT, "/users/legacynight",
                "{\"userId\":\"legacynight\",\"fullName\":\"Night Shift\",\"dutySchedules\":[\"MoTu2000-800\"]}", null, 200);
        final JSONObject after = new JSONObject(getJson("/users/legacynight", 200));
        assertEquals("MoTu2000-800", after.getJSONArray("dutySchedules").getString(0));

        // but ADDING another overnight entry is still rejected
        exec(PUT, "/users/legacynight",
                "{\"userId\":\"legacynight\",\"dutySchedules\":[\"MoTu2000-800\",\"WeTh2100-700\"]}", null, 400);

        sendRequest(DELETE, "/users/legacynight", 204);
    }

    @Test
    public void testDotSegmentIdsRejected() throws Exception {
        exec(POST, "/users", "{\"userId\":\".\",\"password\":\"x\"}", null, 400);
        exec(POST, "/users", "{\"userId\":\"..\",\"password\":\"x\"}", null, 400);
    }

    @Test
    public void testFieldsCanBeCleared() throws Exception {
        exec(POST, "/users", "{\"userId\":\"clearme\",\"password\":\"pw\",\"fullName\":\"Full\",\"email\":\"c@example.com\"}", null, 201);
        // explicit empty strings clear; omitted keys preserve
        exec(PUT, "/users/clearme", "{\"userId\":\"clearme\",\"fullName\":\"\",\"email\":\"\"}", null, 200);
        final JSONObject after = new JSONObject(getJson("/users/clearme", 200));
        assertFalse(after.has("fullName") && !after.isNull("fullName"));
        assertFalse(after.has("email") && !after.isNull("email"));
        sendRequest(DELETE, "/users/clearme", 204);
    }

    @Test
    public void testAdminRoleCannotBeStripped() throws Exception {
        // removing ROLE_ADMIN from admin would lock every administrator out
        exec(PUT, "/users/admin", "{\"userId\":\"admin\",\"roles\":[\"ROLE_USER\"]}", null, 400);
        exec(PUT, "/users/admin", "{\"userId\":\"admin\",\"roles\":[\"ROLE_USER\",\"ROLE_ADMIN\"]}", null, 200);
    }

    @Test
    public void testLastAdministratorKeepsTheAdminRole() throws Exception {
        // admin is guarded by name, so take its role away here: boss is then the only administrator
        setAdminRole("admin", false);
        try {
            createUser("boss", u -> u.getSecurityRoles().add(m_securityRoleDao.findByName("ROLE_ADMIN")));
            createUser("boss2", u -> { });

            exec(PUT, "/users/boss", "{\"roles\":[\"ROLE_USER\"]}", null, 409);
            sendRequest(DELETE, "/users/boss", 409);
            // a change that keeps the role, or one that leaves the roles alone, goes through
            exec(PUT, "/users/boss", "{\"roles\":[\"ROLE_USER\",\"ROLE_ADMIN\"]}", null, 200);
            exec(PUT, "/users/boss", "{\"fullName\":\"Boss\"}", null, 200);
            // a user without the role is not affected
            exec(PUT, "/users/boss2", "{\"roles\":[\"ROLE_USER\"]}", null, 200);

            // with a second administrator, either one can lose the role, and then the other is the last one
            exec(PUT, "/users/boss2", "{\"roles\":[\"ROLE_ADMIN\"]}", null, 200);
            exec(PUT, "/users/boss", "{\"roles\":[\"ROLE_USER\"]}", null, 200);
            sendRequest(DELETE, "/users/boss2", 409);
            exec(PUT, "/users/boss2", "{\"roles\":[\"ROLE_USER\"]}", null, 409);
            assertEquals(List.of("boss2"), inTx(() -> m_userAccountDao.findBySecurityRole("ROLE_ADMIN").stream()
                    .map(UserAccount::getUsername).collect(Collectors.toList())));

            sendRequest(DELETE, "/users/boss", 204);
            setAdminRole("admin", true);
            sendRequest(DELETE, "/users/boss2", 204);
        } finally {
            setAdminRole("admin", true);
        }
    }

    private void setAdminRole(final String username, final boolean hasRole) {
        inTx(() -> {
            final UserAccount user = m_userAccountDao.findByUsername(username);
            final SecurityRole role = m_securityRoleDao.findByName("ROLE_ADMIN");
            if (hasRole) {
                user.getSecurityRoles().add(role);
            } else {
                user.getSecurityRoles().remove(role);
            }
            m_userAccountDao.saveOrUpdate(user);
            return null;
        });
    }

    @Test
    public void testPartialUpdatePreservesScalarFields() throws Exception {
        exec(POST, "/users",
                "{\"userId\":\"partialscalar\",\"password\":\"pw\",\"fullName\":\"Full Name\",\"userComments\":\"kept\",\"email\":\"p@example.org\"}", null, 201);
        // a roles-only body must not wipe the other fields
        exec(PUT, "/users/partialscalar", "{\"userId\":\"partialscalar\",\"roles\":[\"ROLE_USER\"]}", null, 200);
        final JSONObject after = new JSONObject(getJson("/users/partialscalar", 200));
        assertEquals("Full Name", after.getString("fullName"));
        assertEquals("kept", after.getString("userComments"));
        assertEquals("p@example.org", after.getString("email"));
        sendRequest(DELETE, "/users/partialscalar", 204);
    }

    @Test
    public void testCommentsMarkupRejectedEvenWithNewlines() throws Exception {
        exec(POST, "/users", "{\"userId\":\"markup\",\"password\":\"pw\"}", null, 201);
        // newlines must not smuggle markup past the check
        exec(PUT, "/users/markup", "{\"userId\":\"markup\",\"userComments\":\"hello\\n<script>alert(1)</script>\"}", null, 400);
        exec(PUT, "/users/markup", "{\"userId\":\"markup\",\"userComments\":\"plain text\"}", null, 200);
        sendRequest(DELETE, "/users/markup", 204);
    }

    @Test
    public void testStoredCommentWithTrailingWhitespaceIsGrandfathered() throws Exception {
        // a JSP-written comment can end in a newline; the Vue page echoes it trimmed
        createUser("legacytrail", u -> u.setComments("Don't page me\n"));
        final int version = rowVersion("legacytrail");

        final MockHttpServletResponse put = exec(PUT, "/users/legacytrail",
                "{\"userId\":\"legacytrail\",\"userComments\":\"Don't page me\"}", null, 200);

        assertEquals(version, rowVersion("legacytrail"));
        assertEquals("\"" + version + "\"", put.getHeader("ETag"));
        assertEquals("Don't page me\n", column("legacytrail", "comments"));
        sendRequest(DELETE, "/users/legacytrail", 204);
    }

    @Test
    public void testHandEditedMarkupCommentStaysEditable() throws Exception {
        // a comment with markup characters (migrated, or written by the JSPs) must not
        // make the user uneditable when it round-trips unchanged
        createUser("legacycomment", u -> u.setComments("Bob's R&D user"));

        exec(PUT, "/users/legacycomment",
                "{\"userId\":\"legacycomment\",\"userComments\":\"Bob's R&D user\",\"fullName\":\"Touched\"}", null, 200);
        final JSONObject after = new JSONObject(getJson("/users/legacycomment", 200));
        assertEquals("Bob's R&D user", after.getString("userComments"));
        assertEquals("Touched", after.getString("fullName"));
        sendRequest(DELETE, "/users/legacycomment", 204);
    }

    @Test
    public void testDeleteBlockedWhileSupervisingOnCallRole() throws Exception {
        createUser("rolesuper", u -> { });
        createRole("super-role", "super-group", "rolesuper", null);

        // deleting the supervisor would leave the rota's fallback dangling
        final MockHttpServletResponse response = exec(DELETE, "/users/rolesuper", null, null, 409);
        assertTrue(response.getContentAsString(), response.getContentAsString().contains("super-role"));
        assertEquals(1, rowCount("users", "username", "rolesuper"));

        inTx(() -> {
            m_onCallRoleDao.delete(m_onCallRoleDao.findByName("super-role"));
            return null;
        });
        sendRequest(DELETE, "/users/rolesuper", 204);
    }

    @Test
    public void testDeleteIs409WhileADestinationPathNamesTheUser() throws Exception {
        createUser("paged", u -> { });
        m_paths.add("Page-Night", "someone@example.org", "paged");
        m_paths.add("Page-Day", "paged", null);

        final MockHttpServletResponse response = exec(DELETE, "/users/paged", null, null, 409);
        assertTrue(response.getContentAsString(), response.getContentAsString().contains("Page-Day, Page-Night"));
        assertEquals(1, rowCount("users", "username", "paged"));

        // notifd resolves the name as a group first, so with a group of that name the paths do not name the user
        inTx(() -> {
            m_userGroupDao.save(new UserGroup("paged"));
            return null;
        });
        sendRequest(DELETE, "/users/paged", 204);
    }

    @Test
    public void testDeleteSupervisorIs409NamingRoles() throws Exception {
        createUser("twosuper", u -> { });
        createRole("rota-one", "rota-group-one", "twosuper", null);
        createRole("rota-two", "rota-group-two", "twosuper", null);

        final MockHttpServletResponse response = exec(DELETE, "/users/twosuper", null, null, 409);
        final String text = response.getContentAsString();
        assertTrue(text, text.contains("rota-one") && text.contains("rota-two"));
    }

    @Test
    public void testRenameCarriesOnCallRoleSupervisor() throws Exception {
        createUser("superrename", u -> { });
        createRole("carry-role", "carry-group", "superrename", null);

        // renaming must follow the supervisor over to the new id, not leave the rota dangling
        exec(POST, "/users/superrename/rename", "{\"newUserId\":\"superrenamed\"}", null, 200);
        assertEquals("superrenamed", inTx(() -> m_onCallRoleDao.findByName("carry-role").getSupervisor().getUsername()));
    }

    @Test
    public void testRenameBumpsGroupAndRoleVersions() throws Exception {
        createUser("bumpme", u -> { });
        createRole("bump-role", "bump-group", "bumpme", "bumpme");
        final int groupVersion = new JdbcTemplate(m_dataSource).queryForObject(
                "select version from groups where name = 'bump-group'", Integer.class);
        final int roleVersion = new JdbcTemplate(m_dataSource).queryForObject(
                "select version from oncall_roles where name = 'bump-role'", Integer.class);

        exec(POST, "/users/bumpme/rename", "{\"newUserId\":\"bumped\"}", null, 200);

        // both documents show the user name
        assertNotEquals(groupVersion, (int) new JdbcTemplate(m_dataSource).queryForObject(
                "select version from groups where name = 'bump-group'", Integer.class));
        assertNotEquals(roleVersion, (int) new JdbcTemplate(m_dataSource).queryForObject(
                "select version from oncall_roles where name = 'bump-role'", Integer.class));
    }

    @Test
    public void testDeleteRemovesMembershipsAndSchedules() throws Exception {
        createUser("leaver", u -> { });
        createUser("stayer", u -> { });
        createRole("leave-role", "leave-group", "stayer", "leaver");
        inTx(() -> {
            final UserGroup group = m_userGroupDao.findByName("leave-group");
            group.getMembers().add(m_userAccountDao.findByUsername("stayer"));
            final OnCallRole role = m_onCallRoleDao.findByName("leave-role");
            final OnCallSchedule schedule = new OnCallSchedule(m_userAccountDao.findByUsername("stayer"), OnCallSchedule.Type.weekly);
            schedule.getTimes().add(new OnCallScheduleTime("Monday", "08:00:00", "17:00:00"));
            role.getSchedules().add(schedule);
            return null;
        });

        sendRequest(DELETE, "/users/leaver", 204);

        inTx(() -> {
            final UserGroup group = m_userGroupDao.findByName("leave-group");
            assertEquals(1, group.getMembers().size());
            assertEquals("stayer", group.getMembers().get(0).getUsername());
            final OnCallRole role = m_onCallRoleDao.findByName("leave-role");
            assertEquals(1, role.getSchedules().size());
            assertEquals("stayer", role.getSchedules().get(0).getUser().getUsername());
            return null;
        });
        assertEquals(0, rowCount("users", "username", "leaver"));
    }

    @Test
    public void testBodyPathUserIdMismatchRejected() throws Exception {
        exec(PUT, "/users/admin", "{\"userId\":\"somebody-else\",\"fullName\":\"X\"}", null, 400);
    }

    @Test
    public void testForbiddenForNonAdmin() throws Exception {
        setUser("nobody", new String[]{ "ROLE_USER" });
        try {
            sendRequest(GET, "/users", 403);
            exec(POST, "/users", "{\"userId\":\"x\",\"password\":\"x\"}", null, 403);
            sendRequest(DELETE, "/users/admin", 403);
        } finally {
            setUser("admin", new String[]{ "ROLE_ADMIN" });
        }
    }

    @Test
    public void testGetSendsETag() throws Exception {
        exec(POST, "/users", "{\"userId\":\"etaguser\",\"password\":\"pw\",\"roles\":[\"ROLE_USER\"]}", null, 201);
        final MockHttpServletResponse response = exec(GET, "/users/etaguser", null, null, 200);
        final JSONObject dto = new JSONObject(response.getContentAsString());
        assertEquals("\"" + dto.getInt("version") + "\"", response.getHeader("ETag"));
        assertEquals(rowVersion("etaguser"), dto.getInt("version"));
    }

    @Test
    public void testRecreatedUserDoesNotMatchOldETag() throws Exception {
        final String body = "{\"userId\":\"jane\",\"password\":\"pw\",\"fullName\":\"Jane\"}";
        exec(POST, "/users", body, null, 201);
        final String old = exec(GET, "/users/jane", null, null, 200).getHeader("ETag");
        exec(DELETE, "/users/jane", null, null, 204);
        exec(POST, "/users", body, null, 201);
        // the same name and the same content, but not the object the tag was read from
        assertNotEquals(old, exec(GET, "/users/jane", null, null, 200).getHeader("ETag"));
        exec(PUT, "/users/jane", "{\"userId\":\"jane\",\"fullName\":\"Overwritten\"}", old, 412);
        assertEquals("Jane", column("jane", "full_name"));
    }

    @Test
    public void testStaleIfMatchIs412AndChangesNothing() throws Exception {
        exec(POST, "/users", "{\"userId\":\"stale\",\"password\":\"pw\",\"fullName\":\"First\"}", null, 201);
        final String stale = exec(GET, "/users/stale", null, null, 200).getHeader("ETag");
        exec(PUT, "/users/stale", "{\"userId\":\"stale\",\"fullName\":\"Second\"}", null, 200);
        final int version = rowVersion("stale");

        exec(PUT, "/users/stale", "{\"userId\":\"stale\",\"fullName\":\"Third\"}", stale, 412);
        exec(PUT, "/users/stale/password", "{\"password\":\"other\"}", stale, 412);
        exec(POST, "/users/stale/rename", "{\"newUserId\":\"stale2\"}", stale, 412);
        exec(DELETE, "/users/stale", null, stale, 412);

        assertEquals("Second", column("stale", "full_name"));
        assertEquals(version, rowVersion("stale"));
        assertTrue(m_userManager.comparePasswords("stale", "pw"));
    }

    @Test
    public void testCurrentIfMatchUpdatesAndReturnsNewETag() throws Exception {
        exec(POST, "/users", "{\"userId\":\"current\",\"password\":\"pw\"}", null, 201);
        final String etag = exec(GET, "/users/current", null, null, 200).getHeader("ETag");

        final MockHttpServletResponse updated = exec(PUT, "/users/current", "{\"userId\":\"current\",\"fullName\":\"New\"}", etag, 200);
        final String newEtag = updated.getHeader("ETag");
        assertNotEquals(etag, newEtag);
        final JSONObject dto = new JSONObject(updated.getContentAsString());
        assertEquals("New", dto.getString("fullName"));
        assertEquals("\"" + dto.getInt("version") + "\"", newEtag);
        assertEquals(newEtag, exec(GET, "/users/current", null, null, 200).getHeader("ETag"));
        // the new tag is the one that works now
        exec(PUT, "/users/current", "{\"userId\":\"current\",\"fullName\":\"Newer\"}", newEtag, 200);
    }

    @Test
    public void testIfMatchStarAndAbsentGoThrough() throws Exception {
        exec(POST, "/users", "{\"userId\":\"starred\",\"password\":\"pw\"}", null, 201);
        exec(PUT, "/users/starred", "{\"userId\":\"starred\",\"fullName\":\"Star\"}", "*", 200);
        exec(PUT, "/users/starred", "{\"userId\":\"starred\",\"fullName\":\"No header\"}", null, 200);
        assertEquals("No header", column("starred", "full_name"));
        // a version in the body is ignored
        exec(PUT, "/users/starred", "{\"userId\":\"starred\",\"version\":999,\"fullName\":\"Ignored version\"}", null, 200);
        assertEquals("Ignored version", column("starred", "full_name"));
    }

    @Test
    public void testNullListEntriesAre400() throws Exception {
        exec(POST, "/users", "{\"userId\":\"nullduty\",\"password\":\"x\",\"dutySchedules\":[null]}", null, 400);
        exec(POST, "/users", "{\"userId\":\"nullduty\",\"password\":\"x\",\"dutySchedules\":[\"\"]}", null, 400);
        exec(POST, "/users", "{\"userId\":\"nullrole\",\"password\":\"x\",\"roles\":[null]}", null, 400);
        sendRequest(GET, "/users/nullduty", 404);
        sendRequest(GET, "/users/nullrole", 404);
        exec(POST, "/users", "{\"userId\":\"nullok\",\"password\":\"x\"}", null, 201);
        exec(PUT, "/users/nullok", "{\"userId\":\"nullok\",\"dutySchedules\":[null]}", null, 400);
        exec(PUT, "/users/nullok", "{\"userId\":\"nullok\",\"roles\":[null]}", null, 400);
    }

    @Test
    public void testWeakIfMatchTagIsAccepted() throws Exception {
        exec(POST, "/users", "{\"userId\":\"weak\",\"password\":\"pw\"}", null, 201);
        final String etag = exec(GET, "/users/weak", null, null, 200).getHeader("ETag");
        final MockHttpServletResponse updated = exec(PUT, "/users/weak", "{\"userId\":\"weak\",\"fullName\":\"Weak\"}", "W/" + etag, 200);
        assertEquals("Weak", column("weak", "full_name"));
        // the weak tag of the old version is stale like the strong one
        exec(PUT, "/users/weak", "{\"userId\":\"weak\",\"fullName\":\"Again\"}", "W/" + etag, 412);
        exec(PUT, "/users/weak", "{\"userId\":\"weak\",\"fullName\":\"Again\"}", "W/" + updated.getHeader("ETag"), 200);
    }

    @Test
    public void testIfMatchListIsAccepted() throws Exception {
        exec(POST, "/users", "{\"userId\":\"listed\",\"password\":\"pw\"}", null, 201);
        final String etag = exec(GET, "/users/listed", null, null, 200).getHeader("ETag");
        exec(PUT, "/users/listed", "{\"userId\":\"listed\",\"fullName\":\"One\"}", "\"1\", W/\"2\", " + etag, 200);
        assertEquals("One", column("listed", "full_name"));
        // none of the tags is the current one any more
        exec(PUT, "/users/listed", "{\"userId\":\"listed\",\"fullName\":\"Two\"}", "\"1\", " + etag, 412);
        // a header that is not a list of entity tags matches nothing
        exec(PUT, "/users/listed", "{\"userId\":\"listed\",\"fullName\":\"Two\"}", "garbage", 412);
        assertEquals("One", column("listed", "full_name"));
    }

    @Test
    public void testIfMatchOnAMissingUserIs412WhileNoIfMatchIs404() throws Exception {
        final String body = "{\"userId\":\"ghost\",\"fullName\":\"Boo\"}";
        exec(PUT, "/users/ghost", body, null, 404);
        exec(PUT, "/users/ghost/password", "{\"password\":\"x\"}", null, 404);
        exec(POST, "/users/ghost/rename", "{\"newUserId\":\"ghost2\"}", null, 404);
        exec(DELETE, "/users/ghost", null, null, 404);
        for (final String ifMatch : new String[] { "\"1\"", "*", "W/\"1\"" }) {
            exec(PUT, "/users/ghost", body, ifMatch, 412);
            exec(PUT, "/users/ghost/password", "{\"password\":\"x\"}", ifMatch, 412);
            exec(POST, "/users/ghost/rename", "{\"newUserId\":\"ghost2\"}", ifMatch, 412);
            exec(DELETE, "/users/ghost", null, ifMatch, 412);
        }
    }

    @Test
    public void testRenameAndDeleteAndPasswordHonourIfMatch() throws Exception {
        exec(POST, "/users", "{\"userId\":\"honour\",\"password\":\"pw\"}", null, 201);

        String etag = exec(GET, "/users/honour", null, null, 200).getHeader("ETag");
        final MockHttpServletResponse password = exec(PUT, "/users/honour/password", "{\"password\":\"second\"}", etag, 204);
        assertNotEquals(etag, password.getHeader("ETag"));
        assertTrue(m_userManager.comparePasswords("honour", "second"));

        etag = exec(GET, "/users/honour", null, null, 200).getHeader("ETag");
        final MockHttpServletResponse renamed = exec(POST, "/users/honour/rename", "{\"newUserId\":\"honoured\"}", etag, 200);
        assertNotEquals(etag, renamed.getHeader("ETag"));

        etag = exec(GET, "/users/honoured", null, null, 200).getHeader("ETag");
        exec(DELETE, "/users/honoured", null, etag, 204);
        sendRequest(GET, "/users/honoured", 404);
    }

    @Test
    public void testTimeZoneStoredCanonically() throws Exception {
        exec(POST, "/users", "{\"userId\":\"zoned\",\"password\":\"pw\",\"timeZoneId\":\"GMT+1\"}", null, 201);
        assertEquals("GMT+01:00", column("zoned", "time_zone_id"));
        assertEquals("GMT+01:00", new JSONObject(getJson("/users/zoned", 200)).getString("timeZoneId"));
    }

    @Test
    public void testXmlStoreIs503() throws Exception {
        System.setProperty("org.opennms.usermgmt.store", "xml");
        try {
            final String message = "User management through this API needs org.opennms.usermgmt.store=db.";
            final List<MockHttpServletResponse> responses = Arrays.asList(
                    exec(GET, "/users", null, null, 503),
                    exec(GET, "/users/admin", null, null, 503),
                    exec(GET, "/users/available-roles", null, null, 503),
                    exec(POST, "/users", "{\"userId\":\"x\",\"password\":\"x\"}", null, 503),
                    exec(PUT, "/users/admin", "{\"userId\":\"admin\"}", null, 503),
                    exec(PUT, "/users/admin/password", "{\"password\":\"x\"}", null, 503),
                    exec(POST, "/users/admin/rename", "{\"newUserId\":\"x\"}", null, 503),
                    exec(DELETE, "/users/admin", null, null, 503));
            for (final MockHttpServletResponse response : responses) {
                assertEquals(message, response.getContentAsString());
            }
        } finally {
            System.clearProperty("org.opennms.usermgmt.store");
        }
    }

    @Test
    public void testManagersSeeChangeAtOnce() throws Exception {
        exec(POST, "/users", "{\"userId\":\"seen\",\"password\":\"pw\",\"roles\":[\"ROLE_USER\"]}", null, 201);
        assertFalse(m_userManager.getUser("seen").getRoles().contains("ROLE_REST"));

        exec(PUT, "/users/seen", "{\"userId\":\"seen\",\"roles\":[\"ROLE_USER\",\"ROLE_REST\"]}", null, 200);

        // no sleep: the manager reloads when the request's transaction commits
        assertTrue(m_userManager.getUser("seen").getRoles().contains("ROLE_REST"));
    }

    @Test
    public void testJspStyleManagerSaveOnlyBumpsTheEditedUser() throws Exception {
        for (final String id : new String[] { "jsp1", "jsp2", "jsp3" }) {
            exec(POST, "/users", new JSONObject().put("userId", id).put("password", "pw")
                    .put("fullName", "Full & <Name> " + id).put("userComments", "comment " + id)
                    .put("email", id + "@example.org").put("pagerEmail", id + "-pager@example.org")
                    .put("workPhone", "555-1").put("mobilePhone", "555-2").put("homePhone", "555-3")
                    .put("numericPagerService", "NumSvc").put("numericPagerPin", "123")
                    .put("textPagerService", "TextSvc").put("textPagerPin", "456")
                    .put("tuiPin", "4711").put("timeZoneId", "GMT+1")
                    .put("dutySchedules", new JSONArray().put("MoWeFr800-1700").put("TuTh900-1200"))
                    .put("roles", new JSONArray().put("ROLE_USER").put("ROLE_REST")).toString(), null, 201);
        }
        final int v1 = rowVersion("jsp1");
        final int v2 = rowVersion("jsp2");
        final int v3 = rowVersion("jsp3");

        // what the JSPs do: save the user through the manager, which re-applies every cached user
        final User user = m_userManager.getUser("jsp2");
        user.setUserComments("changed comment");
        m_userManager.saveUser("jsp2", user);

        assertEquals(v1, rowVersion("jsp1"));
        assertNotEquals(v2, rowVersion("jsp2"));
        assertEquals(v3, rowVersion("jsp3"));
        assertEquals("changed comment", column("jsp2", "comments"));
    }

    /** The query form tests commit names that the other tests of this class must not meet, and a failed one cannot clean up after itself. */
    @After
    public void purgeQueryFormRows() {
        for (final String name : new String[] { "DOMAIN\\jdoe", "jdoe", "item", "item-2", "available-roles", "available-roles-2", "plain", "plain2" }) {
            new JdbcTemplate(m_dataSource).update("delete from users where username = ?", name);
        }
    }

    /** The query string for a user id that a path cannot carry. */
    private static String byId(final String userId) {
        return "?userId=" + URLEncoder.encode(userId, StandardCharsets.UTF_8);
    }

    private static final String MIGRATED = "DOMAIN\\jdoe";

    @Test
    public void testQueryFormLifecycle() throws Exception {
        createUser(MIGRATED, u -> u.setFullName("John Doe"));

        final MockHttpServletResponse read = exec(GET, "/users/item" + byId(MIGRATED), null, null, 200);
        assertEquals(MIGRATED, new JSONObject(read.getContentAsString()).getString("userId"));
        String etag = read.getHeader("ETag");
        assertNotNull(etag);

        final MockHttpServletResponse updated = exec(PUT, "/users/item" + byId(MIGRATED),
                "{\"userId\":\"" + MIGRATED.replace("\\", "\\\\") + "\",\"fullName\":\"Johnny\"}", etag, 200);
        assertEquals("Johnny", column(MIGRATED, "full_name"));
        assertNotEquals(etag, updated.getHeader("ETag"));
        etag = updated.getHeader("ETag");

        final String hashBefore = column(MIGRATED, "password_hash");
        final MockHttpServletResponse password = exec(PUT, "/users/item/password" + byId(MIGRATED), "{\"password\":\"N3w!secret\"}", etag, 204);
        assertNotEquals(hashBefore, column(MIGRATED, "password_hash"));
        assertNotEquals(etag, password.getHeader("ETag"));
        etag = password.getHeader("ETag");

        final MockHttpServletResponse renamed = exec(POST, "/users/item/rename" + byId(MIGRATED), "{\"newUserId\":\"jdoe\"}", etag, 200);
        assertEquals("jdoe", new JSONObject(renamed.getContentAsString()).getString("userId"));
        assertEquals(0, rowCount("users", "username", MIGRATED));
        etag = renamed.getHeader("ETag");

        exec(DELETE, "/users/item" + byId("jdoe"), null, etag, 204);
        assertEquals(0, rowCount("users", "username", "jdoe"));
    }

    @Test
    public void testQueryFormStaleIs412() throws Exception {
        createUser(MIGRATED, u -> { });
        final String stale = exec(GET, "/users/item" + byId(MIGRATED), null, null, 200).getHeader("ETag");
        exec(PUT, "/users/item" + byId(MIGRATED), "{\"fullName\":\"Second\"}", null, 200);
        final int version = rowVersion(MIGRATED);

        exec(PUT, "/users/item" + byId(MIGRATED), "{\"fullName\":\"Third\"}", stale, 412);
        exec(PUT, "/users/item/password" + byId(MIGRATED), "{\"password\":\"other\"}", stale, 412);
        exec(POST, "/users/item/rename" + byId(MIGRATED), "{\"newUserId\":\"jdoe\"}", stale, 412);
        exec(DELETE, "/users/item" + byId(MIGRATED), null, stale, 412);
        assertEquals(version, rowVersion(MIGRATED));
        assertEquals("Second", column(MIGRATED, "full_name"));
    }

    @Test
    public void testQueryFormMissingParamIs400() throws Exception {
        for (final String query : new String[] { "", "?userId=", "?userId=%20", "?name=admin" }) {
            assertEquals("userId is required", exec(GET, "/users/item" + query, null, null, 400).getContentAsString());
            assertEquals("userId is required", exec(PUT, "/users/item" + query, "{\"fullName\":\"x\"}", null, 400).getContentAsString());
            assertEquals("userId is required", exec(PUT, "/users/item/password" + query, "{\"password\":\"x\"}", null, 400).getContentAsString());
            assertEquals("userId is required", exec(POST, "/users/item/rename" + query, "{\"newUserId\":\"x\"}", null, 400).getContentAsString());
            assertEquals("userId is required", exec(DELETE, "/users/item" + query, null, null, 400).getContentAsString());
        }
        // admin was not touched by any of them
        exec(GET, "/users/admin", null, null, 200);
    }

    @Test
    public void testQueryFormSameAsPathForm() throws Exception {
        createUser("plain", u -> u.setFullName("Plain"));
        createUser("plain2", u -> { });
        final MockHttpServletResponse path = exec(GET, "/users/plain", null, null, 200);
        final MockHttpServletResponse query = exec(GET, "/users/item" + byId("plain"), null, null, 200);
        assertEquals(path.getContentAsString(), query.getContentAsString());
        assertEquals(path.getHeader("ETag"), query.getHeader("ETag"));

        assertSame(exec(GET, "/users/ghost", null, null, 404), exec(GET, "/users/item" + byId("ghost"), null, null, 404));
        assertSame(exec(PUT, "/users/ghost", "{\"fullName\":\"x\"}", "\"1\"", 412), exec(PUT, "/users/item" + byId("ghost"), "{\"fullName\":\"x\"}", "\"1\"", 412));
        assertSame(exec(PUT, "/users/plain", "{\"userId\":\"other\"}", null, 400), exec(PUT, "/users/item" + byId("plain"), "{\"userId\":\"other\"}", null, 400));
        assertSame(exec(PUT, "/users/plain/password", "{}", null, 400), exec(PUT, "/users/item/password" + byId("plain"), "{}", null, 400));
        assertSame(exec(POST, "/users/plain/rename", "{\"newUserId\":\"plain2\"}", null, 409), exec(POST, "/users/item/rename" + byId("plain"), "{\"newUserId\":\"plain2\"}", null, 409));
        assertSame(exec(POST, "/users/plain/rename", "{\"newUserId\":\"plain\"}", null, 400), exec(POST, "/users/item/rename" + byId("plain"), "{\"newUserId\":\"plain\"}", null, 400));
        assertSame(exec(DELETE, "/users/ghost", null, null, 404), exec(DELETE, "/users/item" + byId("ghost"), null, null, 404));

        final MockHttpServletResponse pathUpdate = exec(PUT, "/users/plain", "{\"fullName\":\"Same\"}", null, 200);
        final MockHttpServletResponse queryUpdate = exec(PUT, "/users/item" + byId("plain2"), "{\"fullName\":\"Same\"}", null, 200);
        assertEquals(new JSONObject(pathUpdate.getContentAsString()).getString("fullName"), new JSONObject(queryUpdate.getContentAsString()).getString("fullName"));
        exec(DELETE, "/users/plain", null, null, 204);
        exec(DELETE, "/users/item" + byId("plain2"), null, null, 204);
    }

    private static void assertSame(final MockHttpServletResponse path, final MockHttpServletResponse query) throws Exception {
        assertEquals(path.getStatus(), query.getStatus());
        assertEquals(path.getContentAsString(), query.getContentAsString());
    }

    @Test
    public void testQueryFormCannotCreateOrRenameToUnaddressable() throws Exception {
        createUser(MIGRATED, u -> { });
        final String message = "The userId must not contain markup, whitespace, control or invisible characters, or the characters : / \\ % ? #";
        assertEquals(message, exec(POST, "/users/item/rename" + byId(MIGRATED), "{\"newUserId\":\"a\\\\b\"}", null, 400).getContentAsString());
        assertEquals(message, exec(POST, "/users", "{\"userId\":\"a\\\\b\",\"password\":\"x\"}", null, 400).getContentAsString());
        assertEquals(message, exec(POST, "/users", "{\"userId\":\"DOMAIN\\\\jdoe2\",\"password\":\"x\"}", null, 400).getContentAsString());
        assertEquals(1, rowCount("users", "username", MIGRATED));
        assertEquals(0, rowCount("users", "username", "a\\b"));
    }

    @Test
    public void testQueryFormProtectedNames() throws Exception {
        final boolean createdRtc = rowCount("users", "username", "rtc") == 0;
        if (createdRtc) {
            exec(POST, "/users", "{\"userId\":\"rtc\",\"password\":\"pw\"}", null, 201);
        }
        try {
            assertProtected();
        } finally {
            if (createdRtc) {
                new JdbcTemplate(m_dataSource).update("delete from users where username = 'rtc'");
            }
        }
    }

    private void assertProtected() throws Exception {
        for (final String name : new String[] { "admin", "rtc" }) {
            assertSame(exec(DELETE, "/users/" + name, null, null, 400), exec(DELETE, "/users/item" + byId(name), null, null, 400));
            assertSame(exec(POST, "/users/" + name + "/rename", "{\"newUserId\":\"other\"}", null, 400),
                    exec(POST, "/users/item/rename" + byId(name), "{\"newUserId\":\"other\"}", null, 400));
            assertEquals(1, rowCount("users", "username", name));
        }
        assertEquals("The system user admin cannot be deleted.", exec(DELETE, "/users/item" + byId("admin"), null, null, 400).getContentAsString());
    }

    @Test
    public void testQueryFormReachesLiteralNames() throws Exception {
        createUser("item", u -> u.setFullName("An item"));
        createUser("available-roles", u -> u.setFullName("Some roles"));
        for (final String name : new String[] { "item", "available-roles" }) {
            final MockHttpServletResponse read = exec(GET, "/users/item" + byId(name), null, null, 200);
            assertEquals(name, new JSONObject(read.getContentAsString()).getString("userId"));
            exec(PUT, "/users/item" + byId(name), "{\"fullName\":\"Edited\"}", read.getHeader("ETag"), 200);
            assertEquals("Edited", column(name, "full_name"));
            exec(POST, "/users/item/rename" + byId(name), "{\"newUserId\":\"" + name + "-2\"}", null, 200);
            exec(DELETE, "/users/item" + byId(name + "-2"), null, null, 204);
            assertEquals(0, rowCount("users", "username", name + "-2"));
        }
        // the path form of a literal is the literal endpoint
        assertTrue(getJson("/users/available-roles", 200).startsWith("["));
    }

    private String getJson(final String url, final int expectedStatus) throws Exception {
        final MockHttpServletRequest request = createRequest(GET, url);
        request.addHeader("Accept", JSON);
        return sendRequest(request, expectedStatus);
    }

    /** Sends a JSON request, with an If-Match header if the tag is not null, and returns the response. */
    private MockHttpServletResponse exec(final String method, final String url, final String body, final String ifMatch,
            final int expectedStatus) throws Exception {
        final int queryStart = url.indexOf('?');
        final MockHttpServletRequest request = createRequest(servletContext, method, queryStart < 0 ? url : url.substring(0, queryStart), getUser(), getUserRoles());
        if (queryStart >= 0) {
            request.setQueryString(url.substring(queryStart + 1));
        }
        request.addHeader("Accept", JSON);
        if (body != null) {
            request.setContentType(JSON);
            request.setContent(body.getBytes("UTF-8"));
        }
        if (ifMatch != null) {
            request.addHeader("If-Match", ifMatch);
        }
        final MockHttpServletResponse response = createResponse();
        dispatch(request, response);
        assertEquals(response.getContentAsString(), expectedStatus, response.getStatus());
        return response;
    }

    private <T> T inTx(final Supplier<T> work) {
        return new TransactionTemplate(m_transactionManager).execute(status -> work.get());
    }

    /** Commits a user, so that the REST call (its own transaction) sees it. */
    private void createUser(final String username, final Consumer<UserAccount> customize) {
        inTx(() -> {
            final UserAccount user = new UserAccount(username);
            user.setPasswordHash("");
            customize.accept(user);
            m_userAccountDao.save(user);
            return null;
        });
    }

    /** Commits a group (with that member, if any) and an on-call role over it. */
    private void createRole(final String roleName, final String groupName, final String supervisor, final String member) {
        inTx(() -> {
            final UserGroup group = new UserGroup(groupName);
            if (member != null) {
                group.getMembers().add(m_userAccountDao.findByUsername(member));
            }
            m_userGroupDao.save(group);
            m_onCallRoleDao.save(new OnCallRole(roleName, group, m_userAccountDao.findByUsername(supervisor)));
            return null;
        });
    }

    private int rowVersion(final String username) {
        return new JdbcTemplate(m_dataSource).queryForObject("select version from users where username = ?", Integer.class, username);
    }

    private String column(final String username, final String column) {
        return new JdbcTemplate(m_dataSource).queryForObject("select " + column + " from users where username = ?", String.class, username);
    }

    private int rowCount(final String table, final String column, final String value) {
        return new JdbcTemplate(m_dataSource).queryForObject("select count(*) from " + table + " where " + column + " = ?", Integer.class, value);
    }

    /** Commits the users listed{from} to listed{to - 1}, each with contacts, duty schedules and roles. */
    private void createListedUsers(final int from, final int to) {
        for (int i = from; i < to; i++) {
            final String username = "listed" + i;
            createUser(username, u -> {
                u.setContact(ContactType.email, username + "@example.org", null);
                u.setContact(ContactType.workPhone, "555-0100", null);
                u.getDutySchedules().addAll(Arrays.asList("MoTu900-1000", "Fr800-1700", "SaSu100-200"));
                u.getSecurityRoles().add(m_securityRoleDao.findByName("ROLE_USER"));
                u.getSecurityRoles().add(m_securityRoleDao.findByName("ROLE_REST"));
            });
        }
    }

    /** The SQL statements Hibernate prepares while the request runs. */
    private long statements(final Call request) throws Exception {
        final Statistics statistics = m_sessionFactory.getStatistics();
        final boolean enabled = statistics.isStatisticsEnabled();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
        try {
            request.run();
            return statistics.getPrepareStatementCount();
        } finally {
            statistics.setStatisticsEnabled(enabled);
        }
    }

    @FunctionalInterface
    private interface Call {
        void run() throws Exception;
    }
}
