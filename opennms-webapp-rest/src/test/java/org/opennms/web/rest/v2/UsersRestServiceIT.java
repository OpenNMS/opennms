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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.User;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

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
        // in-memory user/group managers so users.xml is never touched
        "classpath:/META-INF/opennms/applicationContext-mock-usergroup.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class UsersRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private UserManager m_userManager;

    @Autowired
    private org.opennms.netmgt.config.GroupManager m_groupManager;

    public UsersRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void beforeServletStart() {
        MockLogAppender.setupLogging();
    }

    @Test
    public void testListNeverContainsPasswordHashes() throws Exception {
        final String json = getJson("/users", 200);
        final JSONArray users = new JSONArray(json);
        assertTrue(users.length() >= 1);
        assertEquals("admin", users.getJSONObject(0).getString("userId"));
        // the v1 API leaks hashes to admins; the v2 contract is that no
        // response ever carries the password in any form
        assertFalse(json.contains("password"));
        assertFalse(json.contains("21232F29"));
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
        sendData(POST, MediaType.APPLICATION_JSON, "/users", body, 201);

        final JSONObject created = new JSONObject(getJson("/users/junituser", 200));
        assertEquals("JUnit User", created.getString("fullName"));
        assertEquals("junit@example.com", created.getString("email"));
        assertEquals("junit-pager@example.com", created.getString("pagerEmail"));
        assertEquals("MoWeFr800-1700", created.getJSONArray("dutySchedules").getString(0));
        assertEquals("ROLE_USER", created.getJSONArray("roles").getString(0));
        assertTrue(m_userManager.comparePasswords("junituser", "S3cret!pw"));

        // creating the same user again must be rejected
        sendData(POST, MediaType.APPLICATION_JSON, "/users", body, 400);

        sendRequest(DELETE, "/users/junituser", 204);
        sendRequest(GET, "/users/junituser", 404);
    }

    @Test
    public void testCreateValidation() throws Exception {
        // missing password
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"userId\":\"nopass\"}", 400);
        // markup in the user id
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"userId\":\"bad<script>\",\"password\":\"x\"}", 400);
        // colon breaks basic auth, whitespace breaks group references
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"userId\":\"with:colon\",\"password\":\"x\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"userId\":\"with space\",\"password\":\"x\"}", 400);
        // unknown security role
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"userId\":\"badrole\",\"password\":\"x\",\"roles\":[\"ROLE_NOT_A_THING\"]}", 400);
        // invalid time zone
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"userId\":\"badzone\",\"password\":\"x\",\"timeZoneId\":\"Mars/Olympus\"}", 400);
        // duty schedules that would break notifd's parsing at runtime
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"userId\":\"badduty\",\"password\":\"x\",\"dutySchedules\":[\"garbage\"]}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"userId\":\"badduty\",\"password\":\"x\",\"dutySchedules\":[\"Mo899-999\"]}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"userId\":\"badduty\",\"password\":\"x\",\"dutySchedules\":[\"Mo800-2400\"]}", 400);
        // ids that cannot be addressed as a URL path segment
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"userId\":\"team/lead\",\"password\":\"x\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"userId\":\"pct%20\",\"password\":\"x\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"userId\":\"frag#1\",\"password\":\"x\"}", 400);
        // none of the rejects may have been created
        sendRequest(GET, "/users/badrole", 404);
        sendRequest(GET, "/users/badzone", 404);
        sendRequest(GET, "/users/badduty", 404);
    }

    @Test
    public void testUpdatePreservesUnexposedContacts() throws Exception {
        // seed a user carrying an XMPP contact, as a hand-edited users.xml would
        final User user = new User();
        user.setUserId("xmppuser");
        user.setPassword(m_userManager.encryptedPassword("pw", true), Boolean.TRUE);
        final Contact xmpp = new Contact("xmppAddress");
        xmpp.setInfo("xmpp@jabber.example.com");
        user.getContacts().add(xmpp);
        m_userManager.saveUser("xmppuser", user);

        sendData(PUT, MediaType.APPLICATION_JSON, "/users/xmppuser",
                "{\"userId\":\"xmppuser\",\"fullName\":\"XMPP User\",\"email\":\"new@example.com\"}", 204);

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
    public void testWritesDoNotReEscapeFullNameAndContacts() throws Exception {
        // full name and email carry HTML-escapable characters; the config model
        // sanitizes them to a single layer on create
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"userId\":\"escuser\",\"password\":\"pw\",\"fullName\":\"O'Brien & Sons\","
                + "\"email\":\"bill&ted@example.com\"}", 201);

        final User created = m_userManager.getUser("escuser");
        final String storedFullName = created.getFullName().orElse(null);
        final String storedEmail = emailOf(created);
        assertNotNull(storedFullName);
        assertNotNull(storedEmail);

        // a password change copies the stored user; it must not re-escape the
        // full name or contacts it leaves untouched
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/escuser/password", "{\"password\":\"new\"}", 204);
        User after = m_userManager.getUser("escuser");
        assertEquals(storedFullName, after.getFullName().orElse(null));
        assertEquals(storedEmail, emailOf(after));

        // an unrelated edit that echoes the already-escaped values back must
        // leave full name and email byte-for-byte unchanged (no cumulative escaping)
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/escuser",
                "{\"userId\":\"escuser\",\"fullName\":\"" + storedFullName + "\",\"email\":\""
                + storedEmail + "\",\"userComments\":\"touched\"}", 204);
        after = m_userManager.getUser("escuser");
        assertEquals(storedFullName, after.getFullName().orElse(null));
        assertEquals(storedEmail, emailOf(after));

        sendRequest(DELETE, "/users/escuser", 204);
    }

    private static String emailOf(final User user) {
        return user.getContacts().stream()
                .filter(c -> "email".equals(c.getType()))
                .findFirst()
                .flatMap(Contact::getInfo)
                .orElse(null);
    }

    @Test
    public void testPasswordChange() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"userId\":\"pwuser\",\"password\":\"first\"}", 201);
        assertTrue(m_userManager.comparePasswords("pwuser", "first"));

        sendData(PUT, MediaType.APPLICATION_JSON, "/users/pwuser/password", "{\"password\":\"second\"}", 204);
        assertFalse(m_userManager.comparePasswords("pwuser", "first"));
        assertTrue(m_userManager.comparePasswords("pwuser", "second"));

        sendData(PUT, MediaType.APPLICATION_JSON, "/users/pwuser/password", "{}", 400);
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/missing/password", "{\"password\":\"x\"}", 404);

        sendRequest(DELETE, "/users/pwuser", 204);
    }

    @Test
    public void testRename() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"userId\":\"renameme\",\"password\":\"pw\"}", 201);
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"userId\":\"occupied\",\"password\":\"pw\"}", 201);

        // renaming onto an existing user must be rejected
        sendData(POST, MediaType.APPLICATION_JSON, "/users/renameme/rename", "{\"newUserId\":\"occupied\"}", 400);
        // markup in the new id
        sendData(POST, MediaType.APPLICATION_JSON, "/users/renameme/rename", "{\"newUserId\":\"bad<b>\"}", 400);

        sendData(POST, MediaType.APPLICATION_JSON, "/users/renameme/rename", "{\"newUserId\":\"renamed\"}", 204);
        sendRequest(GET, "/users/renameme", 404);
        sendRequest(GET, "/users/renamed", 200);

        sendRequest(DELETE, "/users/renamed", 204);
        sendRequest(DELETE, "/users/occupied", 204);
    }

    @Test
    public void testSystemAccountProtections() throws Exception {
        sendRequest(DELETE, "/users/admin", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users/admin/rename", "{\"newUserId\":\"root\"}", 400);
        sendRequest(GET, "/users/admin", 200);

        // the protection is by name, wherever the account came from
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"userId\":\"rtc\",\"password\":\"pw\"}", 201);
        sendRequest(DELETE, "/users/rtc", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users/rtc/rename", "{\"newUserId\":\"rtc2\"}", 400);
    }

    @Test
    public void testPartialUpdatePreservesRolesAndSchedules() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"userId\":\"partial\",\"password\":\"pw\",\"roles\":[\"ROLE_USER\"],\"dutySchedules\":[\"MoWeFr800-1700\"]}", 201);

        // a body that omits the roles and dutySchedules keys must preserve both
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/partial",
                "{\"userId\":\"partial\",\"fullName\":\"Partial Update\"}", 204);

        final JSONObject after = new JSONObject(getJson("/users/partial", 200));
        assertEquals("Partial Update", after.getString("fullName"));
        assertEquals("ROLE_USER", after.getJSONArray("roles").getString(0));
        assertEquals("MoWeFr800-1700", after.getJSONArray("dutySchedules").getString(0));

        sendRequest(DELETE, "/users/partial", 204);
    }

    @Test
    public void testRejectedUpdateLeavesNoPartialState() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"userId\":\"atomic\",\"password\":\"pw\",\"fullName\":\"Original Name\",\"email\":\"orig@example.com\"}", 201);

        // invalid role arrives with new name/email; nothing may be applied
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/atomic",
                "{\"userId\":\"atomic\",\"fullName\":\"Changed Name\",\"email\":\"changed@example.com\",\"roles\":[\"ROLE_TYPO\"]}", 400);

        final JSONObject after = new JSONObject(getJson("/users/atomic", 200));
        assertEquals("Original Name", after.getString("fullName"));
        assertEquals("orig@example.com", after.getString("email"));
        final User inManager = m_userManager.getUser("atomic");
        assertEquals("Original Name", inManager.getFullName().orElse(null));

        sendRequest(DELETE, "/users/atomic", 204);
    }

    @Test
    public void testOvernightDutyScheduleRejectedForNewEntries() throws Exception {
        // DutySchedule.isInSchedule compares within one calendar day, so an
        // overnight range never matches and would silently disable the schedule
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"userId\":\"nightshift\",\"password\":\"pw\",\"dutySchedules\":[\"MoTu2000-800\"]}", 400);
        sendRequest(GET, "/users/nightshift", 404);
    }

    @Test
    public void testPreExistingOddDutyScheduleStaysEditable() throws Exception {
        // a hand-edited users.xml may already carry an overnight string; the
        // record must remain editable when the UI round-trips it unchanged
        final User user = new User();
        user.setUserId("legacynight");
        user.setPassword(m_userManager.encryptedPassword("pw", true), Boolean.TRUE);
        user.getDutySchedules().add("MoTu2000-800");
        m_userManager.saveUser("legacynight", user);

        sendData(PUT, MediaType.APPLICATION_JSON, "/users/legacynight",
                "{\"userId\":\"legacynight\",\"fullName\":\"Night Shift\",\"dutySchedules\":[\"MoTu2000-800\"]}", 204);
        final JSONObject after = new JSONObject(getJson("/users/legacynight", 200));
        assertEquals("MoTu2000-800", after.getJSONArray("dutySchedules").getString(0));

        // but ADDING another overnight entry is still rejected
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/legacynight",
                "{\"userId\":\"legacynight\",\"dutySchedules\":[\"MoTu2000-800\",\"WeTh2100-700\"]}", 400);

        sendRequest(DELETE, "/users/legacynight", 204);
    }

    @Test
    public void testDotSegmentIdsRejected() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"userId\":\".\",\"password\":\"x\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"userId\":\"..\",\"password\":\"x\"}", 400);
    }

    @Test
    public void testFieldsCanBeCleared() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"userId\":\"clearme\",\"password\":\"pw\",\"fullName\":\"Full\",\"email\":\"c@example.com\"}", 201);
        // explicit empty strings clear; omitted keys preserve
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/clearme",
                "{\"userId\":\"clearme\",\"fullName\":\"\",\"email\":\"\"}", 204);
        final JSONObject after = new JSONObject(getJson("/users/clearme", 200));
        assertFalse(after.has("fullName") && !after.isNull("fullName"));
        assertFalse(after.has("email") && !after.isNull("email"));
        sendRequest(DELETE, "/users/clearme", 204);
    }

    @Test
    public void testAdminRoleCannotBeStripped() throws Exception {
        // removing ROLE_ADMIN from admin would lock every administrator out
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/admin",
                "{\"userId\":\"admin\",\"roles\":[\"ROLE_USER\"]}", 400);
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/admin",
                "{\"userId\":\"admin\",\"roles\":[\"ROLE_USER\",\"ROLE_ADMIN\"]}", 204);
    }

    @Test
    public void testPartialUpdatePreservesScalarFields() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"userId\":\"partial\",\"password\":\"pw\",\"fullName\":\"Full Name\",\"userComments\":\"kept\",\"email\":\"p@example.org\"}", 201);
        // a roles-only body must not wipe the other fields
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/partial",
                "{\"userId\":\"partial\",\"roles\":[\"ROLE_USER\"]}", 204);
        final JSONObject after = new JSONObject(getJson("/users/partial", 200));
        assertEquals("Full Name", after.getString("fullName"));
        assertEquals("kept", after.getString("userComments"));
        assertEquals("p@example.org", after.getString("email"));
        sendRequest(DELETE, "/users/partial", 204);
    }

    @Test
    public void testCommentsMarkupRejectedEvenWithNewlines() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"userId\":\"markup\",\"password\":\"pw\"}", 201);
        // newlines must not smuggle markup past the check
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/markup",
                "{\"userId\":\"markup\",\"userComments\":\"hello\\n<script>alert(1)</script>\"}", 400);
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/markup",
                "{\"userId\":\"markup\",\"userComments\":\"plain text\"}", 204);
        sendRequest(DELETE, "/users/markup", 204);
    }

    @Test
    public void testHandEditedMarkupCommentStaysEditable() throws Exception {
        // a hand-edited users.xml comment with markup characters must not
        // make the user uneditable when it round-trips unchanged
        final User user = new User();
        user.setUserId("legacycomment");
        user.setPassword(m_userManager.encryptedPassword("pw", true), Boolean.TRUE);
        user.setUserComments("Bob's R&D user");
        m_userManager.saveUser("legacycomment", user);

        sendData(PUT, MediaType.APPLICATION_JSON, "/users/legacycomment",
                "{\"userId\":\"legacycomment\",\"userComments\":\"Bob's R&D user\",\"fullName\":\"Touched\"}", 204);
        final JSONObject after = new JSONObject(getJson("/users/legacycomment", 200));
        assertEquals("Bob's R&D user", after.getString("userComments"));
        assertEquals("Touched", after.getString("fullName"));
        sendRequest(DELETE, "/users/legacycomment", 204);
    }

    @Test
    public void testDeleteBlockedWhileSupervisingOnCallRole() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"userId\":\"rolesuper\",\"password\":\"pw\"}", 201);
        final org.opennms.netmgt.config.groups.Role role = new org.opennms.netmgt.config.groups.Role();
        role.setName("super-role");
        role.setMembershipGroup("Admin");
        role.setSupervisor("rolesuper");
        m_groupManager.saveRole(role);

        // deleting the supervisor would leave the rota's fallback dangling
        sendData(DELETE, MediaType.APPLICATION_JSON, "/users/rolesuper", "", 400);

        m_groupManager.deleteRole("super-role");
        sendRequest(DELETE, "/users/rolesuper", 204);
    }

    @Test
    public void testRenameCarriesOnCallRoleSupervisor() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"userId\":\"superrename\",\"password\":\"pw\"}", 201);
        final org.opennms.netmgt.config.groups.Role role = new org.opennms.netmgt.config.groups.Role();
        role.setName("carry-role");
        role.setMembershipGroup("Admin");
        role.setSupervisor("superrename");
        m_groupManager.saveRole(role);

        // renaming must follow the supervisor over to the new id, not leave the rota dangling
        sendData(POST, MediaType.APPLICATION_JSON, "/users/superrename/rename", "{\"newUserId\":\"superrenamed\"}", 204);
        assertEquals("superrenamed", m_groupManager.getRole("carry-role").getSupervisor());

        m_groupManager.deleteRole("carry-role");
        sendRequest(DELETE, "/users/superrenamed", 204);
    }

    @Test
    public void testBodyPathUserIdMismatchRejected() throws Exception {
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/admin",
                "{\"userId\":\"somebody-else\",\"fullName\":\"X\"}", 400);
    }

    @Test
    public void testForbiddenForNonAdmin() throws Exception {
        setUser("nobody", new String[]{ "ROLE_USER" });
        try {
            sendRequest(GET, "/users", 403);
            sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"userId\":\"x\",\"password\":\"x\"}", 403);
            sendRequest(DELETE, "/users/admin", 403);
        } finally {
            setUser("admin", new String[]{ "ROLE_ADMIN" });
        }
    }

    private String getJson(final String url, final int expectedStatus) throws Exception {
        final MockHttpServletRequest request = createRequest(GET, url);
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        return sendRequest(request, expectedStatus);
    }
}
