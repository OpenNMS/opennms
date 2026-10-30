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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    @Autowired
    private SessionFactory m_sessionFactory;

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

    private final TestDestinationPaths m_paths = new TestDestinationPaths();

    @After
    public void clearStoreProperty() {
        System.clearProperty("org.opennms.usermgmt.store");
        m_paths.restore();
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
    public void testCommentLimit() throws Exception {
        final String limit = "x".repeat(4096);
        final String tooLong = "x".repeat(4097);
        exec(POST, "/groups", "{\"name\":\"freetext\",\"comments\":\"" + limit + "\"}", null, 201);
        final MockHttpServletResponse put = exec(PUT, "/groups/freetext", "{\"name\":\"freetext\",\"comments\":\"" + tooLong + "\"}", null, 400);
        assertEquals("Group \"freetext\": the comment is longer than 4096 characters.", put.getContentAsString());
        // the error names the group as it would be stored, without the surrounding whitespace
        final MockHttpServletResponse post = exec(POST, "/groups", "{\"name\":\" freetext2 \",\"comments\":\"" + tooLong + "\"}", null, 400);
        assertEquals("Group \"freetext2\": the comment is longer than 4096 characters.", post.getContentAsString());
        sendRequest(GET, "/groups/freetext2", 404);
        assertEquals(limit, groupColumn("freetext", "comments"));
        sendRequest(DELETE, "/groups/freetext", 204);
    }

    @Test
    public void testStoredLongCommentIsGrandfathered() throws Exception {
        // written before the limit, e.g. by the JSPs or a migration
        final String stored = "y".repeat(5000);
        createGroup("longcomment", g -> g.setComments(stored));
        final int version = rowVersion("longcomment");

        // echoed back unchanged, along with another change
        exec(PUT, "/groups/longcomment", "{\"name\":\"longcomment\",\"comments\":\"" + stored + "\",\"users\":[\"admin\"]}", null, 200);
        assertEquals(stored, groupColumn("longcomment", "comments"));
        assertEquals(Arrays.asList("admin"), members("longcomment"));
        final int edited = rowVersion("longcomment");
        assertNotEquals(version, edited);
        exec(PUT, "/groups/longcomment", "{\"name\":\"longcomment\",\"comments\":\"" + stored + "\"}", null, 200);
        assertEquals(edited, rowVersion("longcomment"));

        // but a changed comment over the limit is refused
        exec(PUT, "/groups/longcomment", "{\"name\":\"longcomment\",\"comments\":\"" + stored + "z\"}", null, 400);
        assertEquals(stored, groupColumn("longcomment", "comments"));
        assertEquals(edited, rowVersion("longcomment"));
        sendRequest(DELETE, "/groups/longcomment", 204);
    }

    /**
     * The unique (group, member) constraint is deferred, so a duplicate member is checked at commit, after the service method has returned, where a violation would be a 500.
     * {@link UserMgmtRestSupport#flushAndCheckConstraints}, which every write calls after its last change, checks it
     * inside the method instead, so that {@link UserMgmtRestSupport#translate} answers 409.
     */
    @Test
    public void testDeferredDuplicateMemberIsReportedBeforeTheCommit() throws Exception {
        createGroup("dupmember", g -> g.getMembers().add(m_userAccountDao.findByUsername("admin")));
        final UserMgmtRestSupport support = new UserMgmtRestSupport();
        support.setSessionFactory(m_sessionFactory);
        final RuntimeException translated = new TransactionTemplate(m_transactionManager).execute(status -> {
            status.setRollbackOnly();
            final Session session = m_sessionFactory.getCurrentSession();
            session.createNativeQuery("INSERT INTO group_members (group_id, user_id, position) SELECT g.id, u.id, 1 FROM groups g, users u"
                    + " WHERE g.name = 'dupmember' AND u.username = 'admin'").executeUpdate();
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

    /** After the check, the constraints are deferred again: a later duplicate is reported by the next check, not by its statement. */
    @Test
    public void testConstraintsAreDeferredAgainAfterTheCheck() throws Exception {
        createGroup("deferagain", g -> g.getMembers().add(m_userAccountDao.findByUsername("admin")));
        final UserMgmtRestSupport support = new UserMgmtRestSupport();
        support.setSessionFactory(m_sessionFactory);
        final RuntimeException reported = new TransactionTemplate(m_transactionManager).execute(status -> {
            status.setRollbackOnly();
            support.flushAndCheckConstraints();
            final Session session = m_sessionFactory.getCurrentSession();
            // an immediate constraint would refuse this statement
            session.createNativeQuery("INSERT INTO group_members (group_id, user_id, position) SELECT g.id, u.id, 1 FROM groups g, users u"
                    + " WHERE g.name = 'deferagain' AND u.username = 'admin'").executeUpdate();
            try {
                support.flushAndCheckConstraints();
                return null;
            } catch (final RuntimeException e) {
                return e;
            }
        });
        assertTrue(String.valueOf(reported), reported instanceof DataIntegrityViolationException);
    }

    /** A fresh database, so that the number of groups is known. */
    @Test
    @JUnitTemporaryDatabase
    public void testListStatementCountDoesNotGrowWithTheGroups() throws Exception {
        createListedGroups(0, 2);
        final long few = statements(() -> getJson("/groups", 200));
        createListedGroups(2, 7);
        final long many = statements(() -> getJson("/groups", 200));
        assertTrue("no statement counted", few > 0);
        assertEquals("statements for 3 groups vs. 8 groups", few, many);

        // the members and duty schedules keep their order
        final JSONArray groups = new JSONArray(getJson("/groups", 200));
        int listed = 0;
        for (int i = 0; i < groups.length(); i++) {
            final JSONObject group = groups.getJSONObject(i);
            final String name = group.getString("name");
            if (name.startsWith("listed")) {
                listed++;
                final JSONArray users = group.getJSONArray("users");
                assertEquals(Arrays.asList(name + "-c", name + "-a", name + "-b"),
                        Arrays.asList(users.getString(0), users.getString(1), users.getString(2)));
                final JSONArray schedules = group.getJSONArray("dutySchedules");
                assertEquals(Arrays.asList("SaSu100-200", "MoTu900-1000"), Arrays.asList(schedules.getString(0), schedules.getString(1)));
            }
        }
        assertEquals(7, listed);
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
    public void testDeleteIs409WhileADestinationPathNamesTheGroup() throws Exception {
        createGroup("pagedgroup", g -> { });
        m_paths.add("Page-Ops", "someone@example.org", "pagedgroup");

        final MockHttpServletResponse response = exec(DELETE, "/groups/pagedgroup", null, null, 409);
        assertTrue(response.getContentAsString(), response.getContentAsString().contains("Page-Ops"));
        assertEquals(1, groupRowCount("pagedgroup"));

        m_paths.clear();
        exec(DELETE, "/groups/pagedgroup", null, null, 204);
        assertEquals(0, groupRowCount("pagedgroup"));
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
            // the query form checks the role before the parameter
            sendRequest(GET, "/groups/item?name=Admin", 403);
            sendRequest(GET, "/groups/item", 403);
            exec(PUT, "/groups/item?name=Admin", "{\"comments\":\"x\"}", null, 403);
            exec(POST, "/groups/item/rename?name=Admin", "{\"newName\":\"x\"}", null, 403);
            sendRequest(DELETE, "/groups/item?name=Admin", 403);
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

    @Test
    public void testGetReturnsAuthorizedCategoriesSorted() throws Exception {
        exec(POST, "/groups", "{\"name\":\"catsorted\"}", null, 201);
        authorizeCategory("Servers", "catsorted");
        authorizeCategory("Routers", "catsorted");

        final JSONObject group = new JSONObject(getJson("/groups/catsorted", 200));
        assertEquals(Arrays.asList("Routers", "Servers"), strings(group.getJSONArray("categories")));

        final JSONArray list = new JSONArray(getJson("/groups", 200));
        for (int i = 0; i < list.length(); i++) {
            if ("catsorted".equals(list.getJSONObject(i).getString("name"))) {
                assertEquals(Arrays.asList("Routers", "Servers"), strings(list.getJSONObject(i).getJSONArray("categories")));
                return;
            }
        }
        throw new AssertionError("catsorted is not in the list");
    }

    @Test
    public void testCategoriesSortIgnoringCase() throws Exception {
        exec(POST, "/groups", "{\"name\":\"catcase\"}", null, 201);
        for (final String category : Arrays.asList("Servers", "routers", "Firewalls")) {
            authorizeCategory(category, "catcase");
        }
        // a case-sensitive sort would put routers last
        final List<String> expected = Arrays.asList("Firewalls", "routers", "Servers");

        assertEquals(expected, strings(new JSONObject(getJson("/groups/catcase", 200)).getJSONArray("categories")));
        final JSONArray list = new JSONArray(getJson("/groups", 200));
        for (int i = 0; i < list.length(); i++) {
            if ("catcase".equals(list.getJSONObject(i).getString("name"))) {
                assertEquals(expected, strings(list.getJSONObject(i).getJSONArray("categories")));
                return;
            }
        }
        throw new AssertionError("catcase is not in the list");
    }

    /** A fresh database, so that the number of categories is known. */
    @Test
    @JUnitTemporaryDatabase
    public void testListStatementCountDoesNotGrowWithTheCategories() throws Exception {
        createListedGroups(0, 2);
        authorizeCategory("cat0", "listed0");
        authorizeCategory("cat0", "listed1");
        final long few = statements(() -> getJson("/groups", 200));
        for (int i = 1; i < 8; i++) {
            authorizeCategory("cat" + i, "listed0");
            authorizeCategory("cat" + i, "listed1");
        }
        final long many = statements(() -> getJson("/groups", 200));
        assertTrue("no statement counted", few > 0);
        assertEquals("statements for 1 category vs. 8 categories", few, many);

        final JSONArray groups = new JSONArray(getJson("/groups", 200));
        for (int i = 0; i < groups.length(); i++) {
            if (groups.getJSONObject(i).getString("name").startsWith("listed")) {
                assertEquals(8, groups.getJSONObject(i).getJSONArray("categories").length());
            }
        }
    }

    @Test
    public void testAvailableCategoriesLeavesTheCategoryCacheAlone() throws Exception {
        createCategory("Uncached");
        // the DAO's cache is per thread, and the requests of this test run on the test's thread
        inTx(() -> {
            m_categoryDao.clear();
            return null;
        });
        getJson("/groups/available-categories", 200);
        // a category cached by the request would come from its closed session, with a lazy collection it cannot load
        assertEquals(0, (int) inTx(() -> m_categoryDao.findByName("Uncached").getAuthorizedGroups().size()));
    }

    @Test
    public void testSaveStatementCountDoesNotGrowWithTheCategories() throws Exception {
        final List<String> all = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            createCategory("save" + i);
            all.add("save" + i);
        }
        exec(POST, "/groups", "{\"name\":\"catsave\",\"categories\":[\"save0\"]}", null, 201);
        final long unchangedFew = statements(() -> exec(PUT, "/groups/catsave", "{\"categories\":[\"save0\"]}", null, 200));
        final long addedMany = statements(() -> exec(PUT, "/groups/catsave", new JSONObject().put("categories", all).toString(), null, 200));
        final long unchangedMany = statements(() -> exec(PUT, "/groups/catsave", new JSONObject().put("categories", all).toString(), null, 200));
        assertEquals(all, authorizedCategories("catsave"));
        exec(PUT, "/groups/catsave", "{\"categories\":[]}", null, 200);
        final long addedOne = statements(() -> exec(PUT, "/groups/catsave", "{\"categories\":[\"save0\"]}", null, 200));

        assertTrue("no statement counted", unchangedFew > 0);
        assertEquals("statements of an unchanged save, 1 category vs. 8 categories", unchangedFew, unchangedMany);
        // beyond the reads, an added authorization costs its own INSERT: 7 added vs. 1 added
        assertEquals("statements to add 7 categories vs. 1 category, less the 6 extra INSERTs", addedOne, addedMany - 6);
        assertEquals(List.of("save0"), authorizedCategories("catsave"));
    }

    @Test
    public void testCreateWithCategories() throws Exception {
        createCategory("Servers");
        createCategory("Routers");
        final MockHttpServletResponse response = exec(POST, "/groups",
                "{\"name\":\"catcreate\",\"categories\":[\"Servers\",\"Routers\"]}", null, 201);
        assertEquals(Arrays.asList("Routers", "Servers"), strings(new JSONObject(response.getContentAsString()).getJSONArray("categories")));
        assertEquals(Arrays.asList("Routers", "Servers"), authorizedCategories("catcreate"));
    }

    @Test
    public void testUpdateReplacesCategories() throws Exception {
        createCategory("Servers");
        createCategory("Routers");
        createCategory("Switches");
        exec(POST, "/groups", "{\"name\":\"catreplace\",\"categories\":[\"Servers\",\"Routers\"]}", null, 201);

        final MockHttpServletResponse response = exec(PUT, "/groups/catreplace",
                "{\"name\":\"catreplace\",\"categories\":[\"Routers\",\"Switches\"]}", null, 200);
        assertEquals(Arrays.asList("Routers", "Switches"), strings(new JSONObject(response.getContentAsString()).getJSONArray("categories")));
        assertEquals(Arrays.asList("Routers", "Switches"), authorizedCategories("catreplace"));
    }

    @Test
    public void testUpdateWithoutCategoriesKeepsThem() throws Exception {
        authorizeCategory("Servers", "catkeep");
        createGroup("catkeep", g -> { });
        final int version = rowVersion("catkeep");

        exec(PUT, "/groups/catkeep", "{\"name\":\"catkeep\",\"comments\":\"touched\"}", null, 200);

        assertEquals(Arrays.asList("Servers"), authorizedCategories("catkeep"));
        assertNotEquals(version, rowVersion("catkeep"));
    }

    @Test
    public void testUpdateWithEmptyCategoriesClearsThem() throws Exception {
        authorizeCategory("Servers", "catclear");
        authorizeCategory("Routers", "catclear");
        createGroup("catclear", g -> { });

        final MockHttpServletResponse response = exec(PUT, "/groups/catclear", "{\"name\":\"catclear\",\"categories\":[]}", null, 200);
        assertEquals(0, new JSONObject(response.getContentAsString()).getJSONArray("categories").length());
        assertTrue(authorizedCategories("catclear").isEmpty());
    }

    @Test
    public void testUnknownCategoryIs400() throws Exception {
        createCategory("Servers");
        exec(POST, "/groups", "{\"name\":\"catunknown\",\"categories\":[\"Servers\"]}", null, 201);
        final int version = rowVersion("catunknown");

        final MockHttpServletResponse response = exec(PUT, "/groups/catunknown",
                "{\"name\":\"catunknown\",\"comments\":\"changed\",\"categories\":[\"Servers\",\"Nope\"]}", null, 400);
        assertEquals("Unknown category: Nope", response.getContentAsString());
        assertEquals(Arrays.asList("Servers"), authorizedCategories("catunknown"));
        assertEquals(version, rowVersion("catunknown"));

        final MockHttpServletResponse create = exec(POST, "/groups", "{\"name\":\"catunknown2\",\"categories\":[\"Nope\"]}", null, 400);
        assertEquals("Unknown category: Nope", create.getContentAsString());
        sendRequest(GET, "/groups/catunknown2", 404);
    }

    @Test
    public void testUnchangedCategoriesKeepVersion() throws Exception {
        createCategory("Servers");
        createCategory("Routers");
        exec(POST, "/groups", "{\"name\":\"catsame\",\"categories\":[\"Servers\",\"Routers\"]}", null, 201);
        final String etag = exec(GET, "/groups/catsame", null, null, 200).getHeader("ETag");

        final MockHttpServletResponse response = exec(PUT, "/groups/catsame",
                "{\"name\":\"catsame\",\"categories\":[\"Routers\",\"Servers\"]}", etag, 200);
        assertEquals(etag, response.getHeader("ETag"));
    }

    @Test
    public void testChangedCategoriesBumpVersion() throws Exception {
        createCategory("Servers");
        createCategory("Routers");
        exec(POST, "/groups", "{\"name\":\"catbump\",\"categories\":[\"Servers\"]}", null, 201);
        final String etag = exec(GET, "/groups/catbump", null, null, 200).getHeader("ETag");

        final MockHttpServletResponse response = exec(PUT, "/groups/catbump",
                "{\"name\":\"catbump\",\"categories\":[\"Servers\",\"Routers\"]}", etag, 200);
        assertNotEquals(etag, response.getHeader("ETag"));
    }

    @Test
    public void testStaleWriteAfterCategoryChangeIs412() throws Exception {
        createCategory("Servers");
        exec(POST, "/groups", "{\"name\":\"catstale\"}", null, 201);
        final String etag = exec(GET, "/groups/catstale", null, null, 200).getHeader("ETag");

        // a v1-style write: straight into category_group, not through the API
        new JdbcTemplate(m_dataSource).update(
                "insert into category_group (categoryid, groupid) select categoryid, 'catstale' from categories where categoryname = 'Servers'");

        exec(PUT, "/groups/catstale", "{\"name\":\"catstale\",\"comments\":\"late\"}", etag, 412);
    }

    @Test
    public void testStaleWriteAfterCategoryDeleteIs412() throws Exception {
        authorizeCategory("Doomed", "catdoomed");
        createGroup("catdoomed", g -> { });
        final String etag = exec(GET, "/groups/catdoomed", null, null, 200).getHeader("ETag");

        inTx(() -> {
            m_categoryDao.delete(m_categoryDao.findByName("Doomed"));
            return null;
        });

        exec(PUT, "/groups/catdoomed", "{\"name\":\"catdoomed\",\"categories\":[\"Doomed\"]}", etag, 412);
    }

    @Test
    public void testCategoryDeletedBehindTheCacheIs400() throws Exception {
        createGroup("catcached", g -> { });
        createCategory("Fleeting");
        // the DAO caches the category, then it goes without the DAO knowing (another process, a SQL script)
        assertNotNull(inTx(() -> m_categoryDao.findByName("Fleeting")));
        new JdbcTemplate(m_dataSource).update("delete from categories where categoryname = 'Fleeting'");

        assertEquals("Unknown category: Fleeting",
                exec(PUT, "/groups/catcached", "{\"name\":\"catcached\",\"categories\":[\"Fleeting\"]}", null, 400).getContentAsString());
    }

    @Test
    public void testAvailableCategories() throws Exception {
        createCategory("Servers");
        createCategory("routers");
        createCategory("Switches");
        final List<String> names = strings(new JSONArray(getJson("/groups/available-categories", 200)));
        assertTrue(names.containsAll(Arrays.asList("Servers", "routers", "Switches")));
        assertEquals(names.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(java.util.stream.Collectors.toList()), names);

        setUser("nobody", new String[]{ "ROLE_USER" });
        try {
            sendRequest(GET, "/groups/available-categories", 403);
        } finally {
            setUser("admin", new String[]{ "ROLE_ADMIN" });
        }
    }

    /** Commits the authorization, creating the category if it does not exist yet. */
    private void authorizeCategory(final String categoryName, final String groupName) {
        inTx(() -> {
            // not from the DAO's cache: a cached category is detached, and a change to it would be lost
            OnmsCategory category = m_categoryDao.findByName(categoryName, false);
            if (category == null) {
                category = new OnmsCategory(categoryName);
                category.getAuthorizedGroups().add(groupName);
                m_categoryDao.save(category);
            } else {
                category.getAuthorizedGroups().add(groupName);
            }
            return null;
        });
    }

    private void createCategory(final String categoryName) {
        inTx(() -> {
            if (m_categoryDao.findByName(categoryName) == null) {
                m_categoryDao.save(new OnmsCategory(categoryName));
            }
            return null;
        });
    }

    private List<String> authorizedCategories(final String group) {
        return new JdbcTemplate(m_dataSource).queryForList(
                "select c.categoryname from category_group g join categories c on c.categoryid = g.categoryid"
                + " where g.groupid = ? order by c.categoryname", String.class, group);
    }

    private static List<String> strings(final JSONArray array) {
        final List<String> values = new java.util.ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            values.add(array.getString(i));
        }
        return values;
    }

    /** The group names the running test commits; see {@link #committing}. */
    private final Set<String> m_committed = new LinkedHashSet<>();

    /**
     * Registers the group names a test commits, which the other tests of this class (one database) must not meet:
     * they are deleted after the test, even a failed one. A name that already exists was left behind by another test,
     * and fails here rather than being erased.
     */
    private void committing(final String... names) {
        for (final String name : names) {
            assertEquals(name + " was left behind by another test", 0, groupRowCount(name));
            m_committed.add(name);
        }
    }

    @After
    public void deleteCommittedGroups() {
        for (final String name : m_committed) {
            new JdbcTemplate(m_dataSource).update("delete from groups where name = ?", name);
        }
    }

    /** The query string for a group name that a path cannot carry. */
    private static String byName(final String name) {
        return "?name=" + URLEncoder.encode(name, StandardCharsets.UTF_8);
    }

    private static final String MIGRATED = "ops/east";

    @Test
    public void testQueryFormLifecycle() throws Exception {
        committing(MIGRATED, "ops-east");
        createGroup(MIGRATED, g -> g.setComments("East"));

        final MockHttpServletResponse read = exec(GET, "/groups/item" + byName(MIGRATED), null, null, 200);
        assertEquals(MIGRATED, new JSONObject(read.getContentAsString()).getString("name"));
        String etag = read.getHeader("ETag");
        assertNotNull(etag);

        final MockHttpServletResponse updated = exec(PUT, "/groups/item" + byName(MIGRATED), "{\"comments\":\"Edited\"}", etag, 200);
        assertEquals("Edited", groupColumn(MIGRATED, "comments"));
        assertNotEquals(etag, updated.getHeader("ETag"));
        etag = updated.getHeader("ETag");

        final MockHttpServletResponse renamed = exec(POST, "/groups/item/rename" + byName(MIGRATED), "{\"newName\":\"ops-east\"}", etag, 200);
        assertEquals("ops-east", new JSONObject(renamed.getContentAsString()).getString("name"));
        assertEquals(0, groupRowCount(MIGRATED));
        etag = renamed.getHeader("ETag");

        exec(DELETE, "/groups/item" + byName("ops-east"), null, etag, 204);
        assertEquals(0, groupRowCount("ops-east"));
    }

    @Test
    public void testQueryFormStaleIs412() throws Exception {
        committing(MIGRATED, "ops-east");
        createGroup(MIGRATED, g -> { });
        final String stale = exec(GET, "/groups/item" + byName(MIGRATED), null, null, 200).getHeader("ETag");
        exec(PUT, "/groups/item" + byName(MIGRATED), "{\"comments\":\"Second\"}", null, 200);
        final int version = rowVersion(MIGRATED);

        exec(PUT, "/groups/item" + byName(MIGRATED), "{\"comments\":\"Third\"}", stale, 412);
        exec(POST, "/groups/item/rename" + byName(MIGRATED), "{\"newName\":\"ops-east\"}", stale, 412);
        exec(DELETE, "/groups/item" + byName(MIGRATED), null, stale, 412);
        assertEquals(version, rowVersion(MIGRATED));
        assertEquals("Second", groupColumn(MIGRATED, "comments"));
    }

    @Test
    public void testQueryFormMissingParamIs400() throws Exception {
        for (final String query : new String[] { "", "?name=", "?name=%20", "?userId=Admin" }) {
            assertEquals("name is required", exec(GET, "/groups/item" + query, null, null, 400).getContentAsString());
            assertEquals("name is required", exec(PUT, "/groups/item" + query, "{\"comments\":\"x\"}", null, 400).getContentAsString());
            assertEquals("name is required", exec(POST, "/groups/item/rename" + query, "{\"newName\":\"x\"}", null, 400).getContentAsString());
            assertEquals("name is required", exec(DELETE, "/groups/item" + query, null, null, 400).getContentAsString());
        }
        exec(GET, "/groups/Admin", null, null, 200);
    }

    @Test
    public void testQueryFormSameAsPathForm() throws Exception {
        committing("plain", "plain2");
        createGroup("plain", g -> g.setComments("Plain"));
        createGroup("plain2", g -> { });
        final MockHttpServletResponse path = exec(GET, "/groups/plain", null, null, 200);
        final MockHttpServletResponse query = exec(GET, "/groups/item" + byName("plain"), null, null, 200);
        assertEquals(path.getContentAsString(), query.getContentAsString());
        assertEquals(path.getHeader("ETag"), query.getHeader("ETag"));

        assertSame(exec(GET, "/groups/ghost", null, null, 404), exec(GET, "/groups/item" + byName("ghost"), null, null, 404));
        assertSame(exec(PUT, "/groups/ghost", "{\"comments\":\"x\"}", "\"1\"", 412), exec(PUT, "/groups/item" + byName("ghost"), "{\"comments\":\"x\"}", "\"1\"", 412));
        assertSame(exec(PUT, "/groups/plain", "{\"name\":\"other\"}", null, 400), exec(PUT, "/groups/item" + byName("plain"), "{\"name\":\"other\"}", null, 400));
        assertSame(exec(POST, "/groups/plain/rename", "{\"newName\":\"plain2\"}", null, 409), exec(POST, "/groups/item/rename" + byName("plain"), "{\"newName\":\"plain2\"}", null, 409));
        assertSame(exec(POST, "/groups/plain/rename", "{\"newName\":\"plain\"}", null, 400), exec(POST, "/groups/item/rename" + byName("plain"), "{\"newName\":\"plain\"}", null, 400));
        assertSame(exec(DELETE, "/groups/ghost", null, null, 404), exec(DELETE, "/groups/item" + byName("ghost"), null, null, 404));

        final MockHttpServletResponse pathUpdate = exec(PUT, "/groups/plain", "{\"comments\":\"Same\"}", null, 200);
        final MockHttpServletResponse queryUpdate = exec(PUT, "/groups/item" + byName("plain2"), "{\"comments\":\"Same\"}", null, 200);
        assertEquals(new JSONObject(pathUpdate.getContentAsString()).getString("comments"), new JSONObject(queryUpdate.getContentAsString()).getString("comments"));
        exec(DELETE, "/groups/plain", null, null, 204);
        exec(DELETE, "/groups/item" + byName("plain2"), null, null, 204);
    }

    private static void assertSame(final MockHttpServletResponse path, final MockHttpServletResponse query) throws Exception {
        assertEquals(path.getStatus(), query.getStatus());
        assertEquals(path.getContentAsString(), query.getContentAsString());
    }

    @Test
    public void testQueryFormCannotCreateOrRenameToUnaddressable() throws Exception {
        committing(MIGRATED, "a\\b", "a%b", "ops/west");
        createGroup(MIGRATED, g -> { });
        final String message = exec(POST, "/groups/item/rename" + byName(MIGRATED), "{\"newName\":\"a\\\\b\"}", null, 400).getContentAsString();
        assertEquals("The group name must not contain markup, whitespace, control or invisible characters, or the characters : / \\ % ? #", message);
        assertEquals(message, exec(POST, "/groups", "{\"name\":\"a\\\\b\"}", null, 400).getContentAsString());
        assertEquals(message, exec(POST, "/groups", "{\"name\":\"ops/west\"}", null, 400).getContentAsString());
        assertEquals(message, exec(POST, "/groups/item/rename" + byName(MIGRATED), "{\"newName\":\"a%b\"}", null, 400).getContentAsString());
        assertEquals(0, groupRowCount("a%b"));
        assertEquals(1, groupRowCount(MIGRATED));
        assertEquals(0, groupRowCount("a\\b"));
    }

    @Test
    public void testReservedNamesAreRefused() throws Exception {
        committing(MIGRATED, ".", "..", "item", "available-roles", "available-categories");
        createGroup(MIGRATED, g -> { });
        for (final String name : new String[] { ".", "..", "item", "available-roles", "available-categories" }) {
            final String message = "The group name \"" + name + "\" is reserved.";
            assertEquals(message, exec(POST, "/groups", "{\"name\":\"" + name + "\"}", null, 400).getContentAsString());
            assertEquals(message, exec(POST, "/groups/item/rename" + byName(MIGRATED), "{\"newName\":\"" + name + "\"}", null, 400).getContentAsString());
            assertEquals(0, groupRowCount(name));
        }
        assertEquals(1, groupRowCount(MIGRATED));
    }

    @Test
    public void testLookAlikesOfReservedGroupNamesAreRefused() throws Exception {
        committing("admin", "ADMIN", "plain");
        for (final String name : new String[] { "admin", "ADMIN" }) {
            assertEquals("The group name \"" + name + "\" is reserved.",
                    exec(POST, "/groups", "{\"name\":\"" + name + "\"}", null, 400).getContentAsString());
            assertEquals(0, groupRowCount(name));
        }
        createGroup("plain", g -> { });
        final MockHttpServletResponse path = exec(POST, "/groups/plain/rename", "{\"newName\":\"admin\"}", null, 400);
        assertEquals("The group name \"admin\" is reserved.", path.getContentAsString());
        assertSame(path, exec(POST, "/groups/item/rename" + byName("plain"), "{\"newName\":\"admin\"}", null, 400));
        assertEquals(1, groupRowCount("plain"));
        // the exact name is not a look-alike: it exists
        exec(POST, "/groups", "{\"name\":\"Admin\"}", null, 409);
    }

    @Test
    public void testExistingLookAlikeGroupStaysManageable() throws Exception {
        committing("admin");
        createGroup("admin", g -> g.setComments("Old"));
        final MockHttpServletResponse read = exec(GET, "/groups/admin", null, null, 200);
        exec(PUT, "/groups/admin", "{\"name\":\"admin\",\"comments\":\"New\"}", read.getHeader("ETag"), 200);
        exec(DELETE, "/groups/admin", null, null, 204);
        assertEquals(0, groupRowCount("admin"));
    }

    @Test
    public void testQueryFormProtectedNames() throws Exception {
        assertSame(exec(DELETE, "/groups/Admin", null, null, 400), exec(DELETE, "/groups/item" + byName("Admin"), null, null, 400));
        assertSame(exec(POST, "/groups/Admin/rename", "{\"newName\":\"other\"}", null, 400),
                exec(POST, "/groups/item/rename" + byName("Admin"), "{\"newName\":\"other\"}", null, 400));
        assertEquals("The system group Admin cannot be deleted.", exec(DELETE, "/groups/item" + byName("Admin"), null, null, 400).getContentAsString());
        assertEquals(1, groupRowCount("Admin"));
    }

    @Test
    public void testQueryFormReachesReservedNames() throws Exception {
        // groups stored before the names were reserved
        committing("item", "item-2", "available-categories", "available-categories-2", ".", ".-2", "..", "..-2");
        createGroup("item", g -> g.setComments("An item"));
        createGroup("available-categories", g -> g.setComments("Some categories"));
        createGroup(".", g -> g.setComments("A dot"));
        createGroup("..", g -> g.setComments("Two dots"));
        for (final String name : new String[] { "item", "available-categories", ".", ".." }) {
            final MockHttpServletResponse read = exec(GET, "/groups/item" + byName(name), null, null, 200);
            assertEquals(name, new JSONObject(read.getContentAsString()).getString("name"));
            exec(PUT, "/groups/item" + byName(name), "{\"comments\":\"Edited\"}", read.getHeader("ETag"), 200);
            assertEquals("Edited", groupColumn(name, "comments"));
            exec(POST, "/groups/item/rename" + byName(name), "{\"newName\":\"" + name + "-2\"}", null, 200);
            exec(DELETE, "/groups/item" + byName(name + "-2"), null, null, 204);
            assertEquals(0, groupRowCount(name + "-2"));
        }
        // the path form of a literal is the literal endpoint
        assertTrue(getJson("/groups/available-categories", 200).startsWith("["));
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

    /** Commits the groups listed{from} to listed{to - 1}, each with three members out of name order and two duty schedules. */
    private void createListedGroups(final int from, final int to) {
        for (int i = from; i < to; i++) {
            final String name = "listed" + i;
            for (final String suffix : Arrays.asList("-a", "-b", "-c")) {
                createUser(name + suffix);
            }
            inTx(() -> {
                final UserGroup group = new UserGroup(name);
                for (final String suffix : Arrays.asList("-c", "-a", "-b")) {
                    group.getMembers().add(m_userAccountDao.findByUsername(name + suffix));
                }
                group.getDutySchedules().addAll(Arrays.asList("SaSu100-200", "MoTu900-1000"));
                m_userGroupDao.save(group);
                return null;
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
