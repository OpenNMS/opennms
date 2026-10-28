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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.sql.DataSource;
import javax.ws.rs.core.MediaType;

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
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
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
public class GroupsRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static final String JSON = MediaType.APPLICATION_JSON;

    /** The database group manager, as the JSPs and notifd use it. */
    @Autowired
    private GroupManager m_groupManager;

    @Autowired
    private UserAccountDao m_userAccountDao;

    @Autowired
    private UserGroupDao m_userGroupDao;

    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    @Autowired
    private SecurityRoleDao m_securityRoleDao;

    @Autowired
    private CategoryDao m_categoryDao;

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private DataSource m_dataSource;

    public GroupsRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void beforeServletStart() {
        MockLogAppender.setupLogging();
    }

    @Before
    public void createAdminAndAdminGroup() {
        // the database starts without users or groups; the REST calls run as admin, and the Admin group is protected
        inTx(() -> {
            UserAccount admin = m_userAccountDao.findByUsername("admin");
            if (admin == null) {
                admin = new UserAccount("admin");
                admin.setPasswordHash("");
                admin.getSecurityRoles().add(m_securityRoleDao.findByName("ROLE_ADMIN"));
                m_userAccountDao.save(admin);
            }
            if (m_userGroupDao.findByName("Admin") == null) {
                final UserGroup group = new UserGroup("Admin");
                group.getMembers().add(admin);
                m_userGroupDao.save(group);
            }
            return null;
        });
    }

    @After
    public void clearStoreProperty() {
        System.clearProperty("org.opennms.usermgmt.store");
    }

    @Test
    public void testListAndGet() throws Exception {
        final JSONArray groups = new JSONArray(getJson("/groups", 200));
        assertTrue(groups.length() >= 1);
        assertEquals("Admin", groups.getJSONObject(0).getString("name"));

        final JSONObject admin = new JSONObject(getJson("/groups/Admin", 200));
        assertEquals("admin", admin.getJSONArray("users").getString(0));

        sendRequest(GET, "/groups/idontexist", 404);
    }

    @Test
    public void testCreateLifecycle() throws Exception {
        createUser("alpha");
        createUser("beta");
        final String body = "{\"name\":\"junitgroup\",\"comments\":\"a junit group\","
                + "\"users\":[\"beta\",\"alpha\"],\"dutySchedules\":[\"MoWeFr800-1700\"]}";
        final MockHttpServletResponse response = exec(POST, "/groups", body, null, 201);
        assertEquals("junitgroup", new JSONObject(response.getContentAsString()).getString("name"));
        assertNotNull(response.getHeader("ETag"));

        final JSONObject created = new JSONObject(getJson("/groups/junitgroup", 200));
        assertEquals("a junit group", created.getString("comments"));
        // the member order drives notification escalation and must round-trip
        assertEquals("beta", created.getJSONArray("users").getString(0));
        assertEquals("alpha", created.getJSONArray("users").getString(1));
        assertEquals("MoWeFr800-1700", created.getJSONArray("dutySchedules").getString(0));
        // and the managers see the same order
        assertEquals(Arrays.asList("beta", "alpha"), m_groupManager.getGroup("junitgroup").getUsers());

        // creating the same group again must be rejected
        exec(POST, "/groups", body, null, 409);

        sendRequest(DELETE, "/groups/junitgroup", 204);
        sendRequest(GET, "/groups/junitgroup", 404);
    }

    @Test
    public void testCreateDuplicateIs409() throws Exception {
        exec(POST, "/groups", "{\"name\":\"dupgroup\"}", null, 201);
        final MockHttpServletResponse response = exec(POST, "/groups", "{\"name\":\"dupgroup\"}", null, 409);
        assertEquals("Group dupgroup already exists.", response.getContentAsString());
    }

    @Test
    public void testCreateValidation() throws Exception {
        // markup / path-segment characters in the name
        exec(POST, "/groups", "{\"name\":\"bad<b>\"}", null, 400);
        exec(POST, "/groups", "{\"name\":\"team/lead\"}", null, 400);
        exec(POST, "/groups", "{\"name\":\"with space\"}", null, 400);
        // markup in the comments
        exec(POST, "/groups", "{\"name\":\"badc\",\"comments\":\"<script>\"}", null, 400);
        // unknown member
        exec(POST, "/groups", "{\"name\":\"badu\",\"users\":[\"nosuchuser\"]}", null, 400);
        // duplicate member
        createUser("dupuser");
        exec(POST, "/groups", "{\"name\":\"badd\",\"users\":[\"dupuser\",\"dupuser\"]}", null, 400);
        // broken duty schedules
        exec(POST, "/groups", "{\"name\":\"bads\",\"dutySchedules\":[\"garbage\"]}", null, 400);
        exec(POST, "/groups", "{\"name\":\"bads\",\"dutySchedules\":[\"Mo899-999\"]}", null, 400);
        // too long
        exec(POST, "/groups", "{\"name\":\"" + "x".repeat(257) + "\"}", null, 400);
        // none of the rejects may have been created
        sendRequest(GET, "/groups/badc", 404);
        sendRequest(GET, "/groups/badu", 404);
        sendRequest(GET, "/groups/badd", 404);
        sendRequest(GET, "/groups/bads", 404);
    }

    @Test
    public void testUnknownNewMemberIs400() throws Exception {
        createUser("known");
        exec(POST, "/groups", "{\"name\":\"memgroup\",\"users\":[\"known\"]}", null, 201);
        final int version = rowVersion("memgroup");

        final MockHttpServletResponse response = exec(PUT, "/groups/memgroup",
                "{\"name\":\"memgroup\",\"users\":[\"known\",\"ghostuser\"]}", null, 400);
        assertEquals("Unknown user: ghostuser", response.getContentAsString());

        assertEquals(version, rowVersion("memgroup"));
        assertEquals(Arrays.asList("known"), members("memgroup"));
    }

    @Test
    public void testNullListEntriesAre400() throws Exception {
        exec(POST, "/groups", "{\"name\":\"nullduty\",\"dutySchedules\":[null]}", null, 400);
        exec(POST, "/groups", "{\"name\":\"nullduty\",\"dutySchedules\":[\"\"]}", null, 400);
        exec(POST, "/groups", "{\"name\":\"nulluser\",\"users\":[null]}", null, 400);
        sendRequest(GET, "/groups/nullduty", 404);
        sendRequest(GET, "/groups/nulluser", 404);
    }

    @Test
    public void testOvernightDutyScheduleRejectedForNewEntries() throws Exception {
        // DutySchedule.isInSchedule compares within one calendar day, so an
        // overnight range never matches - accepting it would silently put the
        // group permanently off duty
        exec(POST, "/groups", "{\"name\":\"nightgroup\",\"dutySchedules\":[\"MoTu2000-800\"]}", null, 400);
        sendRequest(GET, "/groups/nightgroup", 404);
    }

    @Test
    public void testPreExistingOddDutyScheduleStaysEditable() throws Exception {
        // a migrated groups.xml may already carry an overnight string; the
        // record must remain editable when the UI round-trips it unchanged
        createGroup("legacynight", g -> g.getDutySchedules().add("MoTu2000-800"));

        exec(PUT, "/groups/legacynight",
                "{\"name\":\"legacynight\",\"comments\":\"touched\",\"dutySchedules\":[\"MoTu2000-800\"]}", null, 200);
        final JSONObject after = new JSONObject(getJson("/groups/legacynight", 200));
        assertEquals("MoTu2000-800", after.getJSONArray("dutySchedules").getString(0));

        // but ADDING another overnight entry is still rejected
        exec(PUT, "/groups/legacynight",
                "{\"name\":\"legacynight\",\"dutySchedules\":[\"MoTu2000-800\",\"WeTh2100-700\"]}", null, 400);

        sendRequest(DELETE, "/groups/legacynight", 204);
    }

    @Test
    public void testCommentsMarkupRejectedEvenWithNewlines() throws Exception {
        // newlines must not smuggle markup past the full-string match
        exec(POST, "/groups", "{\"name\":\"nlgroup\",\"comments\":\"hello\\n<script>alert(1)</script>\"}", null, 400);
        sendRequest(GET, "/groups/nlgroup", 404);
    }

    @Test
    public void testStoredCommentWithTrailingWhitespaceIsGrandfathered() throws Exception {
        // a JSP-written comment can end in a newline; the Vue page echoes it trimmed
        createGroup("legacytrail", g -> g.setComments("Don't page us\n"));
        final int version = rowVersion("legacytrail");

        exec(PUT, "/groups/legacytrail", "{\"name\":\"legacytrail\",\"comments\":\"Don't page us\"}", null, 200);

        assertEquals(version, rowVersion("legacytrail"));
        assertEquals("Don't page us\n", new JSONObject(getJson("/groups/legacytrail", 200)).getString("comments"));
        sendRequest(DELETE, "/groups/legacytrail", 204);
    }

    @Test
    public void testHandEditedMarkupCommentStaysEditable() throws Exception {
        // "Bob's R&D team" is legal free text in a hand-edited groups.xml;
        // the group must stay editable while the comment is unchanged
        createGroup("legacycomment", g -> g.setComments("Bob's R&D team"));

        exec(PUT, "/groups/legacycomment",
                "{\"name\":\"legacycomment\",\"comments\":\"Bob's R&D team\",\"users\":[\"admin\"]}", null, 200);
        final JSONObject after = new JSONObject(getJson("/groups/legacycomment", 200));
        assertEquals("Bob's R&D team", after.getString("comments"));
        assertEquals("admin", after.getJSONArray("users").getString(0));

        // but CHANGING the comment to new markup is still rejected
        exec(PUT, "/groups/legacycomment", "{\"name\":\"legacycomment\",\"comments\":\"<b>new markup</b>\"}", null, 400);

        sendRequest(DELETE, "/groups/legacycomment", 204);
    }

    @Test
    public void testDotSegmentNamesRejected() throws Exception {
        exec(POST, "/groups", "{\"name\":\".\"}", null, 400);
        exec(POST, "/groups", "{\"name\":\"..\"}", null, 400);
    }

    @Test
    public void testCommentsCanBeCleared() throws Exception {
        exec(POST, "/groups", "{\"name\":\"commentgroup\",\"comments\":\"to be removed\"}", null, 201);
        // an explicit empty string clears; an omitted key preserves
        exec(PUT, "/groups/commentgroup", "{\"name\":\"commentgroup\",\"comments\":\"\"}", null, 200);
        final JSONObject json = new JSONObject(getJson("/groups/commentgroup", 200));
        assertTrue(!json.has("comments") || json.isNull("comments"));
        sendRequest(DELETE, "/groups/commentgroup", 204);
    }

    @Test
    public void testUpdateReordersMembers() throws Exception {
        createUser("first");
        createUser("second");
        exec(POST, "/groups", "{\"name\":\"ordergroup\",\"users\":[\"first\",\"second\"]}", null, 201);

        exec(PUT, "/groups/ordergroup", "{\"name\":\"ordergroup\",\"users\":[\"second\",\"first\"]}", null, 200);

        final JSONObject after = new JSONObject(getJson("/groups/ordergroup", 200));
        assertEquals("second", after.getJSONArray("users").getString(0));
        assertEquals("first", after.getJSONArray("users").getString(1));
        assertEquals(Arrays.asList("second", "first"), members("ordergroup"));

        sendRequest(DELETE, "/groups/ordergroup", 204);
    }

    @Test
    public void testMemberReorderBumpsVersion() throws Exception {
        createUser("first");
        createUser("second");
        exec(POST, "/groups", "{\"name\":\"bumpgroup\",\"users\":[\"first\",\"second\"]}", null, 201);
        final String before = exec(GET, "/groups/bumpgroup", null, null, 200).getHeader("ETag");

        final MockHttpServletResponse updated = exec(PUT, "/groups/bumpgroup",
                "{\"name\":\"bumpgroup\",\"users\":[\"second\",\"first\"]}", null, 200);

        assertNotEquals(before, updated.getHeader("ETag"));
        assertEquals("\"" + rowVersion("bumpgroup") + "\"", updated.getHeader("ETag"));
    }

    @Test
    public void testPartialUpdatePreservesOmittedLists() throws Exception {
        createUser("keepme");
        exec(POST, "/groups", "{\"name\":\"partialgroup\",\"users\":[\"keepme\"],\"dutySchedules\":[\"MoWeFr800-1700\"]}", null, 201);

        // a body that omits the user and dutySchedules keys must preserve both
        exec(PUT, "/groups/partialgroup", "{\"name\":\"partialgroup\",\"comments\":\"updated\"}", null, 200);

        final JSONObject after = new JSONObject(getJson("/groups/partialgroup", 200));
        assertEquals("updated", after.getString("comments"));
        assertEquals("keepme", after.getJSONArray("users").getString(0));
        assertEquals("MoWeFr800-1700", after.getJSONArray("dutySchedules").getString(0));

        sendRequest(DELETE, "/groups/partialgroup", 204);
    }

    @Test
    public void testDefaultMapUntouched() throws Exception {
        // default-map has no editor anywhere but exists in migrated files
        createGroup("mapgroup", g -> g.setDefaultMap("some-map"));

        final MockHttpServletResponse response = exec(PUT, "/groups/mapgroup", "{\"name\":\"mapgroup\",\"comments\":\"touched\"}", null, 200);
        assertFalse(response.getContentAsString().contains("some-map"));
        assertEquals("some-map", groupColumn("mapgroup", "default_map"));

        // nor does a rename lose it
        exec(POST, "/groups/mapgroup/rename", "{\"newName\":\"mapgroup2\"}", null, 200);
        assertEquals("some-map", groupColumn("mapgroup2", "default_map"));
        assertEquals("some-map", m_groupManager.getGroup("mapgroup2").getDefaultMap().orElse(null));
    }

    @Test
    public void testRejectedUpdateLeavesNoPartialState() throws Exception {
        exec(POST, "/groups", "{\"name\":\"atomicgroup\",\"comments\":\"original\"}", null, 201);
        final int version = rowVersion("atomicgroup");

        // new comments arrive with an unknown member; nothing may be applied
        exec(PUT, "/groups/atomicgroup", "{\"name\":\"atomicgroup\",\"comments\":\"changed\",\"users\":[\"nosuchuser\"]}", null, 400);

        final JSONObject after = new JSONObject(getJson("/groups/atomicgroup", 200));
        assertEquals("original", after.getString("comments"));
        assertEquals("original", m_groupManager.getGroup("atomicgroup").getComments().orElse(null));
        assertEquals(version, rowVersion("atomicgroup"));

        sendRequest(DELETE, "/groups/atomicgroup", 204);
    }

    @Test
    public void testBodyPathNameMismatchRejected() throws Exception {
        exec(PUT, "/groups/Admin", "{\"name\":\"somebody-else\",\"comments\":\"x\"}", null, 400);
    }

    @Test
    public void testRename() throws Exception {
        exec(POST, "/groups", "{\"name\":\"renamegroup\"}", null, 201);
        exec(POST, "/groups", "{\"name\":\"occupiedgroup\"}", null, 201);

        exec(POST, "/groups/renamegroup/rename", "{\"newName\":\"occupiedgroup\"}", null, 409);
        exec(POST, "/groups/renamegroup/rename", "{\"newName\":\"bad<b>\"}", null, 400);
        // the current name: there is nothing to rename
        exec(POST, "/groups/renamegroup/rename", "{\"newName\":\"renamegroup\"}", null, 400);

        final MockHttpServletResponse renamed = exec(POST, "/groups/renamegroup/rename", "{\"newName\":\"renamedgroup\"}", null, 200);
        assertEquals("renamedgroup", new JSONObject(renamed.getContentAsString()).getString("name"));
        assertNotNull(renamed.getHeader("ETag"));
        sendRequest(GET, "/groups/renamegroup", 404);
        sendRequest(GET, "/groups/renamedgroup", 200);

        sendRequest(DELETE, "/groups/renamedgroup", 204);
        sendRequest(DELETE, "/groups/occupiedgroup", 204);
    }

    @Test
    public void testRenameToALongNameKeepsCategoryAuthorizations() throws Exception {
        // groups.name holds 256 characters, so category_group.groupid must as well
        final String longName = "g" + "x".repeat(199);
        exec(POST, "/groups", "{\"name\":\"longcatgroup\"}", null, 201);
        authorizeCategory("junit-cat-long", "longcatgroup");

        exec(POST, "/groups/longcatgroup/rename", "{\"newName\":\"" + longName + "\"}", null, 200);

        inTx(() -> {
            assertEquals(0, m_categoryDao.getCategoriesWithAuthorizedGroup("longcatgroup").size());
            assertEquals(1, m_categoryDao.getCategoriesWithAuthorizedGroup(longName).size());
            return null;
        });

        sendRequest(DELETE, "/groups/" + longName, 204);
    }

    @Test
    public void testRenameFollowsRoleReferences() throws Exception {
        createUser("roleboss");
        createRole("junit-oncall-role", "rolegroup", "roleboss");

        exec(POST, "/groups/rolegroup/rename", "{\"newName\":\"rolegroup2\"}", null, 200);

        // the role's membership group is the same row, so it follows the rename
        inTx(() -> {
            assertEquals("rolegroup2", m_onCallRoleDao.findByName("junit-oncall-role").getMembershipGroup().getName());
            assertEquals(1, m_onCallRoleDao.findByMembershipGroup("rolegroup2").size());
            assertEquals(0, m_onCallRoleDao.findByMembershipGroup("rolegroup").size());
            return null;
        });
        assertEquals("rolegroup2", m_groupManager.getRole("junit-oncall-role").getMembershipGroup());
    }

    @Test
    public void testDeleteMembershipGroupIs409NamingRoles() throws Exception {
        createUser("deleteboss");
        createRole("role-b", "guardedgroup", "deleteboss");
        createRole("role-a", "guardedgroup", "deleteboss");

        final MockHttpServletResponse response = exec(DELETE, "/groups/guardedgroup", null, null, 409);
        assertTrue(response.getContentAsString(), response.getContentAsString().contains("role-a, role-b"));
        assertEquals(1, groupRowCount("guardedgroup"));

        // once the roles are gone the delete goes through
        inTx(() -> {
            m_onCallRoleDao.findByMembershipGroup("guardedgroup").forEach(m_onCallRoleDao::delete);
            return null;
        });
        exec(DELETE, "/groups/guardedgroup", null, null, 204);
        assertEquals(0, groupRowCount("guardedgroup"));
    }

    @Test
    public void testRenameMovesCategoryAuthorizations() throws Exception {
        exec(POST, "/groups", "{\"name\":\"catgroup\"}", null, 201);
        authorizeCategory("junit-cat-rename", "catgroup");

        exec(POST, "/groups/catgroup/rename", "{\"newName\":\"catgroup2\"}", null, 200);

        // the authorization must follow the group to its new name and be gone from the old one
        inTx(() -> {
            assertEquals(0, m_categoryDao.getCategoriesWithAuthorizedGroup("catgroup").size());
            assertEquals(1, m_categoryDao.getCategoriesWithAuthorizedGroup("catgroup2").size());
            return null;
        });

        sendRequest(DELETE, "/groups/catgroup2", 204);
    }

    @Test
    public void testRenameToExistingIs409AndChangesNothing() throws Exception {
        exec(POST, "/groups", "{\"name\":\"fromgroup\"}", null, 201);
        exec(POST, "/groups", "{\"name\":\"togroup\"}", null, 201);
        authorizeCategory("junit-cat-from", "fromgroup");
        authorizeCategory("junit-cat-to", "togroup");
        final int version = rowVersion("fromgroup");

        exec(POST, "/groups/fromgroup/rename", "{\"newName\":\"togroup\"}", null, 409);

        inTx(() -> {
            assertEquals(1, m_categoryDao.getCategoriesWithAuthorizedGroup("fromgroup").size());
            assertEquals(1, m_categoryDao.getCategoriesWithAuthorizedGroup("togroup").size());
            return null;
        });
        assertEquals(version, rowVersion("fromgroup"));
        assertEquals(1, groupRowCount("fromgroup"));
    }

    @Test
    public void testDeleteClearsCategoryAuthorizations() throws Exception {
        exec(POST, "/groups", "{\"name\":\"delcatgroup\"}", null, 201);
        authorizeCategory("junit-cat-delete", "delcatgroup");

        sendRequest(DELETE, "/groups/delcatgroup", 204);

        // deleting the group must clear its DB authorizations so a future group
        // reusing the name can't inherit them
        inTx(() -> {
            assertEquals(0, m_categoryDao.getCategoriesWithAuthorizedGroup("delcatgroup").size());
            return null;
        });
    }

    @Test
    public void testAdminGroupProtections() throws Exception {
        sendRequest(DELETE, "/groups/Admin", 400);
        exec(POST, "/groups/Admin/rename", "{\"newName\":\"Admins2\"}", null, 400);
        sendRequest(GET, "/groups/Admin", 200);
    }

    @Test
    public void testForbiddenForNonAdmin() throws Exception {
        setUser("nobody", new String[]{ "ROLE_USER" });
        try {
            sendRequest(GET, "/groups", 403);
            exec(POST, "/groups", "{\"name\":\"x\"}", null, 403);
            sendRequest(DELETE, "/groups/Admin", 403);
        } finally {
            setUser("admin", new String[]{ "ROLE_ADMIN" });
        }
    }

    @Test
    public void testGetSendsETag() throws Exception {
        exec(POST, "/groups", "{\"name\":\"etaggroup\"}", null, 201);
        final MockHttpServletResponse response = exec(GET, "/groups/etaggroup", null, null, 200);
        final JSONObject dto = new JSONObject(response.getContentAsString());
        assertEquals("\"" + dto.getInt("version") + "\"", response.getHeader("ETag"));
        assertEquals(rowVersion("etaggroup"), dto.getInt("version"));
    }

    @Test
    public void testRecreatedGroupDoesNotMatchOldETag() throws Exception {
        final String body = "{\"name\":\"recreated\",\"comments\":\"Same\"}";
        exec(POST, "/groups", body, null, 201);
        final String old = exec(GET, "/groups/recreated", null, null, 200).getHeader("ETag");
        exec(DELETE, "/groups/recreated", null, null, 204);
        exec(POST, "/groups", body, null, 201);
        // the same name and the same content, but not the object the tag was read from
        assertNotEquals(old, exec(GET, "/groups/recreated", null, null, 200).getHeader("ETag"));
        exec(PUT, "/groups/recreated", "{\"name\":\"recreated\",\"comments\":\"Overwritten\"}", old, 412);
        assertEquals("Same", groupColumn("recreated", "comments"));
    }

    @Test
    public void testStaleIfMatchIs412AndChangesNothing() throws Exception {
        exec(POST, "/groups", "{\"name\":\"stalegroup\",\"comments\":\"First\"}", null, 201);
        final String stale = exec(GET, "/groups/stalegroup", null, null, 200).getHeader("ETag");
        exec(PUT, "/groups/stalegroup", "{\"name\":\"stalegroup\",\"comments\":\"Second\"}", null, 200);
        final int version = rowVersion("stalegroup");

        exec(PUT, "/groups/stalegroup", "{\"name\":\"stalegroup\",\"comments\":\"Third\"}", stale, 412);
        exec(POST, "/groups/stalegroup/rename", "{\"newName\":\"stalegroup2\"}", stale, 412);
        exec(DELETE, "/groups/stalegroup", null, stale, 412);

        assertEquals("Second", groupColumn("stalegroup", "comments"));
        assertEquals(version, rowVersion("stalegroup"));

        // a current tag, and *, are honoured
        final String current = exec(GET, "/groups/stalegroup", null, null, 200).getHeader("ETag");
        final MockHttpServletResponse updated = exec(PUT, "/groups/stalegroup", "{\"name\":\"stalegroup\",\"comments\":\"Fourth\"}", current, 200);
        assertNotEquals(current, updated.getHeader("ETag"));
        exec(POST, "/groups/stalegroup/rename", "{\"newName\":\"stalegroup2\"}", "*", 200);
        exec(DELETE, "/groups/stalegroup2", null, "*", 204);
    }

    @Test
    public void testXmlStoreIs503() throws Exception {
        System.setProperty("org.opennms.usermgmt.store", "xml");
        try {
            final String message = "User management through this API needs org.opennms.usermgmt.store=db.";
            final List<MockHttpServletResponse> responses = Arrays.asList(
                    exec(GET, "/groups", null, null, 503),
                    exec(GET, "/groups/Admin", null, null, 503),
                    exec(POST, "/groups", "{\"name\":\"x\"}", null, 503),
                    exec(PUT, "/groups/Admin", "{\"name\":\"Admin\"}", null, 503),
                    exec(POST, "/groups/Admin/rename", "{\"newName\":\"x\"}", null, 503),
                    exec(DELETE, "/groups/Admin", null, null, 503));
            for (final MockHttpServletResponse response : responses) {
                assertEquals(message, response.getContentAsString());
            }
        } finally {
            System.clearProperty("org.opennms.usermgmt.store");
        }
    }

    @Test
    public void testManagersSeeChangeAtOnce() throws Exception {
        exec(POST, "/groups", "{\"name\":\"seengroup\",\"comments\":\"before\"}", null, 201);
        assertEquals("before", m_groupManager.getGroup("seengroup").getComments().orElse(null));

        exec(PUT, "/groups/seengroup", "{\"name\":\"seengroup\",\"comments\":\"after\"}", null, 200);

        // no sleep: the manager reloads when the request's transaction commits
        assertEquals("after", m_groupManager.getGroup("seengroup").getComments().orElse(null));
    }

    private void authorizeCategory(final String categoryName, final String groupName) {
        inTx(() -> {
            final OnmsCategory category = new OnmsCategory(categoryName);
            category.getAuthorizedGroups().add(groupName);
            m_categoryDao.save(category);
            return null;
        });
    }

    private String getJson(final String url, final int expectedStatus) throws Exception {
        final MockHttpServletRequest request = createRequest(GET, url);
        request.addHeader("Accept", JSON);
        return sendRequest(request, expectedStatus);
    }

    /** Sends a JSON request, with an If-Match header if the tag is not null, and returns the response. */
    private MockHttpServletResponse exec(final String method, final String url, final String body, final String ifMatch,
            final int expectedStatus) throws Exception {
        final MockHttpServletRequest request = createRequest(servletContext, method, url, getUser(), getUserRoles());
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
    private void createUser(final String username) {
        inTx(() -> {
            if (m_userAccountDao.findByUsername(username) == null) {
                final UserAccount user = new UserAccount(username);
                user.setPasswordHash("");
                m_userAccountDao.save(user);
            }
            return null;
        });
    }

    /** Commits a group, as a migrated groups.xml would have left it. */
    private void createGroup(final String name, final Consumer<UserGroup> customize) {
        inTx(() -> {
            final UserGroup group = new UserGroup(name);
            customize.accept(group);
            m_userGroupDao.save(group);
            return null;
        });
    }

    /** Commits a group (created if needed) and an on-call role over it. */
    private void createRole(final String roleName, final String groupName, final String supervisor) {
        inTx(() -> {
            UserGroup group = m_userGroupDao.findByName(groupName);
            if (group == null) {
                group = new UserGroup(groupName);
                m_userGroupDao.save(group);
            }
            m_onCallRoleDao.save(new OnCallRole(roleName, group, m_userAccountDao.findByUsername(supervisor)));
            return null;
        });
    }

    private int rowVersion(final String group) {
        return new JdbcTemplate(m_dataSource).queryForObject("select version from groups where name = ?", Integer.class, group);
    }

    private String groupColumn(final String group, final String column) {
        return new JdbcTemplate(m_dataSource).queryForObject("select " + column + " from groups where name = ?", String.class, group);
    }

    private int groupRowCount(final String group) {
        return new JdbcTemplate(m_dataSource).queryForObject("select count(*) from groups where name = ?", Integer.class, group);
    }

    private List<String> members(final String group) {
        return new JdbcTemplate(m_dataSource).queryForList(
                "select u.username from group_members m join groups g on g.id = m.group_id join users u on u.id = m.user_id"
                + " where g.name = ? order by m.position", String.class, group);
    }
}
