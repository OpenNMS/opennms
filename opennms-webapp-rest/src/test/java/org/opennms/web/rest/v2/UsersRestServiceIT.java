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
        assertEquals("admin", users.getJSONObject(0).getString("user-id"));
        // the v1 API leaks hashes to admins; the v2 contract is that no
        // response ever carries the password in any form
        assertFalse(json.contains("password"));
        assertFalse(json.contains("21232F29"));
    }

    @Test
    public void testGetUser() throws Exception {
        final JSONObject admin = new JSONObject(getJson("/users/admin", 200));
        assertEquals("admin", admin.getString("user-id"));
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
        final String body = "{\"user-id\":\"junituser\",\"password\":\"S3cret!pw\",\"full-name\":\"JUnit User\","
                + "\"email\":\"junit@example.com\",\"pager-email\":\"junit-pager@example.com\","
                + "\"duty-schedule\":[\"MoWeFr800-1700\"],\"role\":[\"ROLE_USER\"]}";
        sendData(POST, MediaType.APPLICATION_JSON, "/users", body, 201);

        final JSONObject created = new JSONObject(getJson("/users/junituser", 200));
        assertEquals("JUnit User", created.getString("full-name"));
        assertEquals("junit@example.com", created.getString("email"));
        assertEquals("junit-pager@example.com", created.getString("pager-email"));
        assertEquals("MoWeFr800-1700", created.getJSONArray("duty-schedule").getString(0));
        assertEquals("ROLE_USER", created.getJSONArray("role").getString(0));
        assertTrue(m_userManager.comparePasswords("junituser", "S3cret!pw"));

        // creating the same user again must be rejected
        sendData(POST, MediaType.APPLICATION_JSON, "/users", body, 400);

        sendRequest(DELETE, "/users/junituser", 204);
        sendRequest(GET, "/users/junituser", 404);
    }

    @Test
    public void testCreateValidation() throws Exception {
        // missing password
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"user-id\":\"nopass\"}", 400);
        // markup in the user id
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"user-id\":\"bad<script>\",\"password\":\"x\"}", 400);
        // colon breaks basic auth, whitespace breaks group references
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"user-id\":\"with:colon\",\"password\":\"x\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"user-id\":\"with space\",\"password\":\"x\"}", 400);
        // unknown security role
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"user-id\":\"badrole\",\"password\":\"x\",\"role\":[\"ROLE_NOT_A_THING\"]}", 400);
        // invalid time zone
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"user-id\":\"badzone\",\"password\":\"x\",\"time-zone-id\":\"Mars/Olympus\"}", 400);
        // duty schedules that would break notifd's parsing at runtime
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"user-id\":\"badduty\",\"password\":\"x\",\"duty-schedule\":[\"garbage\"]}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"user-id\":\"badduty\",\"password\":\"x\",\"duty-schedule\":[\"Mo899-999\"]}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"user-id\":\"badduty\",\"password\":\"x\",\"duty-schedule\":[\"Mo800-2400\"]}", 400);
        // ids that cannot be addressed as a URL path segment
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"user-id\":\"team/lead\",\"password\":\"x\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"user-id\":\"pct%20\",\"password\":\"x\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"user-id\":\"frag#1\",\"password\":\"x\"}", 400);
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
                "{\"user-id\":\"xmppuser\",\"full-name\":\"XMPP User\",\"email\":\"new@example.com\"}", 204);

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
    public void testPasswordChange() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"user-id\":\"pwuser\",\"password\":\"first\"}", 201);
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
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"user-id\":\"renameme\",\"password\":\"pw\"}", 201);
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"user-id\":\"occupied\",\"password\":\"pw\"}", 201);

        // renaming onto an existing user must be rejected
        sendData(POST, MediaType.APPLICATION_JSON, "/users/renameme/rename", "{\"new-user-id\":\"occupied\"}", 400);
        // markup in the new id
        sendData(POST, MediaType.APPLICATION_JSON, "/users/renameme/rename", "{\"new-user-id\":\"bad<b>\"}", 400);

        sendData(POST, MediaType.APPLICATION_JSON, "/users/renameme/rename", "{\"new-user-id\":\"renamed\"}", 204);
        sendRequest(GET, "/users/renameme", 404);
        sendRequest(GET, "/users/renamed", 200);

        sendRequest(DELETE, "/users/renamed", 204);
        sendRequest(DELETE, "/users/occupied", 204);
    }

    @Test
    public void testSystemAccountProtections() throws Exception {
        sendRequest(DELETE, "/users/admin", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users/admin/rename", "{\"new-user-id\":\"root\"}", 400);
        sendRequest(GET, "/users/admin", 200);

        // the protection is by name, wherever the account came from
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"user-id\":\"rtc\",\"password\":\"pw\"}", 201);
        sendRequest(DELETE, "/users/rtc", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users/rtc/rename", "{\"new-user-id\":\"rtc2\"}", 400);
    }

    @Test
    public void testPartialUpdatePreservesRolesAndSchedules() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"user-id\":\"partial\",\"password\":\"pw\",\"role\":[\"ROLE_USER\"],\"duty-schedule\":[\"MoWeFr800-1700\"]}", 201);

        // a body that omits the role and duty-schedule keys must preserve both
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/partial",
                "{\"user-id\":\"partial\",\"full-name\":\"Partial Update\"}", 204);

        final JSONObject after = new JSONObject(getJson("/users/partial", 200));
        assertEquals("Partial Update", after.getString("full-name"));
        assertEquals("ROLE_USER", after.getJSONArray("role").getString(0));
        assertEquals("MoWeFr800-1700", after.getJSONArray("duty-schedule").getString(0));

        sendRequest(DELETE, "/users/partial", 204);
    }

    @Test
    public void testRejectedUpdateLeavesNoPartialState() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"user-id\":\"atomic\",\"password\":\"pw\",\"full-name\":\"Original Name\",\"email\":\"orig@example.com\"}", 201);

        // invalid role arrives with new name/email; nothing may be applied
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/atomic",
                "{\"user-id\":\"atomic\",\"full-name\":\"Changed Name\",\"email\":\"changed@example.com\",\"role\":[\"ROLE_TYPO\"]}", 400);

        final JSONObject after = new JSONObject(getJson("/users/atomic", 200));
        assertEquals("Original Name", after.getString("full-name"));
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
                "{\"user-id\":\"nightshift\",\"password\":\"pw\",\"duty-schedule\":[\"MoTu2000-800\"]}", 400);
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
                "{\"user-id\":\"legacynight\",\"full-name\":\"Night Shift\",\"duty-schedule\":[\"MoTu2000-800\"]}", 204);
        final JSONObject after = new JSONObject(getJson("/users/legacynight", 200));
        assertEquals("MoTu2000-800", after.getJSONArray("duty-schedule").getString(0));

        // but ADDING another overnight entry is still rejected
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/legacynight",
                "{\"user-id\":\"legacynight\",\"duty-schedule\":[\"MoTu2000-800\",\"WeTh2100-700\"]}", 400);

        sendRequest(DELETE, "/users/legacynight", 204);
    }

    @Test
    public void testDotSegmentIdsRejected() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"user-id\":\".\",\"password\":\"x\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"user-id\":\"..\",\"password\":\"x\"}", 400);
    }

    @Test
    public void testFieldsCanBeCleared() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"user-id\":\"clearme\",\"password\":\"pw\",\"full-name\":\"Full\",\"email\":\"c@example.com\"}", 201);
        // explicit empty strings clear; omitted keys preserve
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/clearme",
                "{\"user-id\":\"clearme\",\"full-name\":\"\",\"email\":\"\"}", 204);
        final JSONObject after = new JSONObject(getJson("/users/clearme", 200));
        assertFalse(after.has("full-name") && !after.isNull("full-name"));
        assertFalse(after.has("email") && !after.isNull("email"));
        sendRequest(DELETE, "/users/clearme", 204);
    }

    @Test
    public void testAdminRoleCannotBeStripped() throws Exception {
        // removing ROLE_ADMIN from admin would lock every administrator out
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/admin",
                "{\"user-id\":\"admin\",\"role\":[\"ROLE_USER\"]}", 400);
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/admin",
                "{\"user-id\":\"admin\",\"role\":[\"ROLE_USER\",\"ROLE_ADMIN\"]}", 204);
    }

    @Test
    public void testPartialUpdatePreservesScalarFields() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"user-id\":\"partial\",\"password\":\"pw\",\"full-name\":\"Full Name\",\"user-comments\":\"kept\",\"email\":\"p@example.org\"}", 201);
        // a roles-only body must not wipe the other fields
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/partial",
                "{\"user-id\":\"partial\",\"role\":[\"ROLE_USER\"]}", 204);
        final JSONObject after = new JSONObject(getJson("/users/partial", 200));
        assertEquals("Full Name", after.getString("full-name"));
        assertEquals("kept", after.getString("user-comments"));
        assertEquals("p@example.org", after.getString("email"));
        sendRequest(DELETE, "/users/partial", 204);
    }

    @Test
    public void testCommentsMarkupRejectedEvenWithNewlines() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"user-id\":\"markup\",\"password\":\"pw\"}", 201);
        // newlines must not smuggle markup past the check
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/markup",
                "{\"user-id\":\"markup\",\"user-comments\":\"hello\\n<script>alert(1)</script>\"}", 400);
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/markup",
                "{\"user-id\":\"markup\",\"user-comments\":\"plain text\"}", 204);
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
                "{\"user-id\":\"legacycomment\",\"user-comments\":\"Bob's R&D user\",\"full-name\":\"Touched\"}", 204);
        final JSONObject after = new JSONObject(getJson("/users/legacycomment", 200));
        assertEquals("Bob's R&D user", after.getString("user-comments"));
        assertEquals("Touched", after.getString("full-name"));
        sendRequest(DELETE, "/users/legacycomment", 204);
    }

    @Test
    public void testDeleteBlockedWhileSupervisingOnCallRole() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/users",
                "{\"user-id\":\"rolesuper\",\"password\":\"pw\"}", 201);
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
    public void testBodyPathUserIdMismatchRejected() throws Exception {
        sendData(PUT, MediaType.APPLICATION_JSON, "/users/admin",
                "{\"user-id\":\"somebody-else\",\"full-name\":\"X\"}", 400);
    }

    @Test
    public void testForbiddenForNonAdmin() throws Exception {
        setUser("nobody", new String[]{ "ROLE_USER" });
        try {
            sendRequest(GET, "/users", 403);
            sendData(POST, MediaType.APPLICATION_JSON, "/users", "{\"user-id\":\"x\",\"password\":\"x\"}", 403);
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
