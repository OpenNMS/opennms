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
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Role;
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
        // in-memory user/group managers so groups.xml is never touched
        "classpath:/META-INF/opennms/applicationContext-mock-usergroup.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class GroupsRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private GroupManager m_groupManager;

    @Autowired
    private UserManager m_userManager;

    public GroupsRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void beforeServletStart() {
        MockLogAppender.setupLogging();
    }

    private void ensureUser(final String userId) throws Exception {
        if (!m_userManager.hasUser(userId)) {
            final User user = new User();
            user.setUserId(userId);
            user.setPassword(m_userManager.encryptedPassword("pw", true), Boolean.TRUE);
            m_userManager.saveUser(userId, user);
        }
    }

    @Test
    public void testListAndGet() throws Exception {
        final JSONArray groups = new JSONArray(getJson("/groups", 200));
        assertTrue(groups.length() >= 1);
        assertEquals("Admin", groups.getJSONObject(0).getString("name"));

        final JSONObject admin = new JSONObject(getJson("/groups/Admin", 200));
        assertEquals("admin", admin.getJSONArray("user").getString(0));

        sendRequest(GET, "/groups/idontexist", 404);
    }

    @Test
    public void testCreateLifecycle() throws Exception {
        ensureUser("alpha");
        ensureUser("beta");
        final String body = "{\"name\":\"junitgroup\",\"comments\":\"a junit group\","
                + "\"user\":[\"beta\",\"alpha\"],\"duty-schedule\":[\"MoWeFr800-1700\"]}";
        sendData(POST, MediaType.APPLICATION_JSON, "/groups", body, 201);

        final JSONObject created = new JSONObject(getJson("/groups/junitgroup", 200));
        assertEquals("a junit group", created.getString("comments"));
        // the member order drives notification escalation and must round-trip
        assertEquals("beta", created.getJSONArray("user").getString(0));
        assertEquals("alpha", created.getJSONArray("user").getString(1));
        assertEquals("MoWeFr800-1700", created.getJSONArray("duty-schedule").getString(0));

        // creating the same group again must be rejected
        sendData(POST, MediaType.APPLICATION_JSON, "/groups", body, 400);

        sendRequest(DELETE, "/groups/junitgroup", 204);
        sendRequest(GET, "/groups/junitgroup", 404);
    }

    @Test
    public void testCreateValidation() throws Exception {
        // markup / path-segment characters in the name
        sendData(POST, MediaType.APPLICATION_JSON, "/groups", "{\"name\":\"bad<b>\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/groups", "{\"name\":\"team/lead\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/groups", "{\"name\":\"with space\"}", 400);
        // markup in the comments
        sendData(POST, MediaType.APPLICATION_JSON, "/groups", "{\"name\":\"badc\",\"comments\":\"<script>\"}", 400);
        // unknown member
        sendData(POST, MediaType.APPLICATION_JSON, "/groups", "{\"name\":\"badu\",\"user\":[\"nosuchuser\"]}", 400);
        // duplicate member
        ensureUser("dupuser");
        sendData(POST, MediaType.APPLICATION_JSON, "/groups", "{\"name\":\"badd\",\"user\":[\"dupuser\",\"dupuser\"]}", 400);
        // broken duty schedules
        sendData(POST, MediaType.APPLICATION_JSON, "/groups", "{\"name\":\"bads\",\"duty-schedule\":[\"garbage\"]}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/groups", "{\"name\":\"bads\",\"duty-schedule\":[\"Mo899-999\"]}", 400);
        // none of the rejects may have been created
        sendRequest(GET, "/groups/badc", 404);
        sendRequest(GET, "/groups/badu", 404);
        sendRequest(GET, "/groups/badd", 404);
        sendRequest(GET, "/groups/bads", 404);
    }

    @Test
    public void testOvernightDutyScheduleRejectedForNewEntries() throws Exception {
        // DutySchedule.isInSchedule compares within one calendar day, so an
        // overnight range never matches — accepting it would silently put the
        // group permanently off duty
        sendData(POST, MediaType.APPLICATION_JSON, "/groups",
                "{\"name\":\"nightgroup\",\"duty-schedule\":[\"MoTu2000-800\"]}", 400);
        sendRequest(GET, "/groups/nightgroup", 404);
    }

    @Test
    public void testPreExistingOddDutyScheduleStaysEditable() throws Exception {
        // a hand-edited groups.xml may already carry an overnight string; the
        // record must remain editable when the UI round-trips it unchanged
        final Group group = new Group();
        group.setName("legacynight");
        group.addDutySchedule("MoTu2000-800");
        m_groupManager.saveGroup("legacynight", group);

        sendData(PUT, MediaType.APPLICATION_JSON, "/groups/legacynight",
                "{\"name\":\"legacynight\",\"comments\":\"touched\",\"duty-schedule\":[\"MoTu2000-800\"]}", 204);
        final JSONObject after = new JSONObject(getJson("/groups/legacynight", 200));
        assertEquals("MoTu2000-800", after.getJSONArray("duty-schedule").getString(0));

        // but ADDING another overnight entry is still rejected
        sendData(PUT, MediaType.APPLICATION_JSON, "/groups/legacynight",
                "{\"name\":\"legacynight\",\"duty-schedule\":[\"MoTu2000-800\",\"WeTh2100-700\"]}", 400);

        sendRequest(DELETE, "/groups/legacynight", 204);
    }

    @Test
    public void testDotSegmentNamesRejected() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/groups", "{\"name\":\".\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/groups", "{\"name\":\"..\"}", 400);
    }

    @Test
    public void testCommentsCanBeCleared() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/groups",
                "{\"name\":\"commentgroup\",\"comments\":\"to be removed\"}", 201);
        // an explicit empty string clears; an omitted key preserves
        sendData(PUT, MediaType.APPLICATION_JSON, "/groups/commentgroup",
                "{\"name\":\"commentgroup\",\"comments\":\"\"}", 204);
        final String json = getJson("/groups/commentgroup", 200);
        assertTrue(!new JSONObject(json).has("comments") || new JSONObject(json).isNull("comments"));
        sendRequest(DELETE, "/groups/commentgroup", 204);
    }

    @Test
    public void testUpdateReordersMembers() throws Exception {
        ensureUser("first");
        ensureUser("second");
        sendData(POST, MediaType.APPLICATION_JSON, "/groups",
                "{\"name\":\"ordergroup\",\"user\":[\"first\",\"second\"]}", 201);

        sendData(PUT, MediaType.APPLICATION_JSON, "/groups/ordergroup",
                "{\"name\":\"ordergroup\",\"user\":[\"second\",\"first\"]}", 204);

        final JSONObject after = new JSONObject(getJson("/groups/ordergroup", 200));
        assertEquals("second", after.getJSONArray("user").getString(0));
        assertEquals("first", after.getJSONArray("user").getString(1));

        sendRequest(DELETE, "/groups/ordergroup", 204);
    }

    @Test
    public void testPartialUpdatePreservesOmittedLists() throws Exception {
        ensureUser("keepme");
        sendData(POST, MediaType.APPLICATION_JSON, "/groups",
                "{\"name\":\"partialgroup\",\"user\":[\"keepme\"],\"duty-schedule\":[\"MoWeFr800-1700\"]}", 201);

        // a body that omits the user and duty-schedule keys must preserve both
        sendData(PUT, MediaType.APPLICATION_JSON, "/groups/partialgroup",
                "{\"name\":\"partialgroup\",\"comments\":\"updated\"}", 204);

        final JSONObject after = new JSONObject(getJson("/groups/partialgroup", 200));
        assertEquals("updated", after.getString("comments"));
        assertEquals("keepme", after.getJSONArray("user").getString(0));
        assertEquals("MoWeFr800-1700", after.getJSONArray("duty-schedule").getString(0));

        sendRequest(DELETE, "/groups/partialgroup", 204);
    }

    @Test
    public void testUpdatePreservesDefaultMap() throws Exception {
        // default-map has no editor anywhere but exists in hand-edited files
        final Group group = new Group();
        group.setName("mapgroup");
        group.setDefaultMap("some-map");
        m_groupManager.saveGroup("mapgroup", group);

        sendData(PUT, MediaType.APPLICATION_JSON, "/groups/mapgroup",
                "{\"name\":\"mapgroup\",\"comments\":\"touched\"}", 204);

        assertEquals("some-map", m_groupManager.getGroup("mapgroup").getDefaultMap().orElse(null));
        sendRequest(DELETE, "/groups/mapgroup", 204);
    }

    @Test
    public void testRejectedUpdateLeavesNoPartialState() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/groups",
                "{\"name\":\"atomicgroup\",\"comments\":\"original\"}", 201);

        // new comments arrive with an unknown member; nothing may be applied
        sendData(PUT, MediaType.APPLICATION_JSON, "/groups/atomicgroup",
                "{\"name\":\"atomicgroup\",\"comments\":\"changed\",\"user\":[\"nosuchuser\"]}", 400);

        final JSONObject after = new JSONObject(getJson("/groups/atomicgroup", 200));
        assertEquals("original", after.getString("comments"));
        assertEquals("original", m_groupManager.getGroup("atomicgroup").getComments().orElse(null));

        sendRequest(DELETE, "/groups/atomicgroup", 204);
    }

    @Test
    public void testBodyPathNameMismatchRejected() throws Exception {
        sendData(PUT, MediaType.APPLICATION_JSON, "/groups/Admin",
                "{\"name\":\"somebody-else\",\"comments\":\"x\"}", 400);
    }

    @Test
    public void testRename() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/groups", "{\"name\":\"renamegroup\"}", 201);
        sendData(POST, MediaType.APPLICATION_JSON, "/groups", "{\"name\":\"occupiedgroup\"}", 201);

        sendData(POST, MediaType.APPLICATION_JSON, "/groups/renamegroup/rename", "{\"new-name\":\"occupiedgroup\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/groups/renamegroup/rename", "{\"new-name\":\"bad<b>\"}", 400);

        sendData(POST, MediaType.APPLICATION_JSON, "/groups/renamegroup/rename", "{\"new-name\":\"renamedgroup\"}", 204);
        sendRequest(GET, "/groups/renamegroup", 404);
        sendRequest(GET, "/groups/renamedgroup", 200);

        sendRequest(DELETE, "/groups/renamedgroup", 204);
        sendRequest(DELETE, "/groups/occupiedgroup", 204);
    }

    @Test
    public void testRenameFollowsRoleReferences() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/groups", "{\"name\":\"rolegroup\"}", 201);
        final Role role = new Role();
        role.setName("junit-oncall-role");
        role.setMembershipGroup("rolegroup");
        role.setSupervisor("admin");
        m_groupManager.saveRole(role);

        sendData(POST, MediaType.APPLICATION_JSON, "/groups/rolegroup/rename", "{\"new-name\":\"rolegroup2\"}", 204);

        assertEquals("rolegroup2", m_groupManager.getRole("junit-oncall-role").getMembershipGroup());

        // and delete is rejected while the role still references the group
        sendRequest(DELETE, "/groups/rolegroup2", 400);
        m_groupManager.deleteRole("junit-oncall-role");
        sendRequest(DELETE, "/groups/rolegroup2", 204);
    }

    @Test
    public void testAdminGroupProtections() throws Exception {
        sendRequest(DELETE, "/groups/Admin", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/groups/Admin/rename", "{\"new-name\":\"Admins2\"}", 400);
        sendRequest(GET, "/groups/Admin", 200);
    }

    @Test
    public void testForbiddenForNonAdmin() throws Exception {
        setUser("nobody", new String[]{ "ROLE_USER" });
        try {
            sendRequest(GET, "/groups", 403);
            sendData(POST, MediaType.APPLICATION_JSON, "/groups", "{\"name\":\"x\"}", 403);
            sendRequest(DELETE, "/groups/Admin", 403);
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
