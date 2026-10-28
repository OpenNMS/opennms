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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.OnCallSchedule;
import org.opennms.netmgt.model.usermgmt.OnCallScheduleTime;
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
public class OnCallRolesRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static final String JSON = MediaType.APPLICATION_JSON;

    private static final String NIGHT = "{\"user\":\"bob\",\"type\":\"weekly\",\"times\":["
            + "{\"id\":\"t1\",\"day\":\"monday\",\"begins\":\"18:00:00\",\"ends\":\"23:59:59\"},"
            + "{\"day\":\"tuesday\",\"begins\":\"00:00:00\",\"ends\":\"06:00:00\"}]}";

    private static final String DAILY_CAROL = "{\"user\":\"carol\",\"type\":\"daily\",\"times\":["
            + "{\"begins\":\"08:00:00\",\"ends\":\"17:00:00\"}]}";

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
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private DataSource m_dataSource;

    private Locale m_locale;

    public OnCallRolesRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void beforeServletStart() {
        MockLogAppender.setupLogging();
    }

    @Before
    public void createFixtures() {
        m_locale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        // the database starts without users or groups; the REST calls run as admin
        inTx(() -> {
            UserAccount admin = m_userAccountDao.findByUsername("admin");
            if (admin == null) {
                admin = new UserAccount("admin");
                admin.setPasswordHash("");
                admin.getSecurityRoles().add(m_securityRoleDao.findByName("ROLE_ADMIN"));
                m_userAccountDao.save(admin);
            }
            final UserAccount alice = user("alice");
            final UserAccount bob = user("bob");
            user("carol");
            user("outsider");
            // the temporary database lives as long as the class, so the fixtures are created once
            if (m_userGroupDao.findByName("Ops") == null) {
                final UserGroup ops = new UserGroup("Ops");
                ops.getMembers().add(alice);
                ops.getMembers().add(bob);
                ops.getMembers().add(user("carol"));
                m_userGroupDao.save(ops);
                m_userGroupDao.save(new UserGroup("Empty"));
            }
            return null;
        });
    }

    @After
    public void clearStoreProperty() {
        Locale.setDefault(m_locale);
        System.clearProperty("org.opennms.usermgmt.store");
    }

    private static String role(final String name, final String schedules) {
        return "{\"name\":\"" + name + "\",\"description\":\"the rota\",\"membershipGroup\":\"Ops\",\"supervisor\":\"alice\""
                + (schedules == null ? "" : ",\"schedules\":[" + schedules + "]") + "}";
    }

    @Test
    public void testCreateGetRoundTrip() throws Exception {
        final MockHttpServletResponse created = exec(POST, "/oncall-roles", role("rota1", NIGHT + "," + DAILY_CAROL), null, 201);
        assertNotNull(created.getHeader("ETag"));
        final JSONObject createdJson = new JSONObject(created.getContentAsString());
        assertEquals("rota1", createdJson.getString("name"));
        assertEquals("\"" + createdJson.getInt("version") + "\"", created.getHeader("ETag"));

        final MockHttpServletResponse got = exec(GET, "/oncall-roles/rota1", null, null, 200);
        assertEquals("\"" + rowVersion("rota1") + "\"", got.getHeader("ETag"));
        final JSONObject json = new JSONObject(got.getContentAsString());
        assertEquals("the rota", json.getString("description"));
        assertEquals("Ops", json.getString("membershipGroup"));
        assertEquals("alice", json.getString("supervisor"));
        final JSONArray schedules = json.getJSONArray("schedules");
        assertEquals(2, schedules.length());
        assertEquals("bob", schedules.getJSONObject(0).getString("user"));
        assertEquals("weekly", schedules.getJSONObject(0).getString("type"));
        final JSONArray times = schedules.getJSONObject(0).getJSONArray("times");
        assertEquals("t1", times.getJSONObject(0).getString("id"));
        assertEquals("monday", times.getJSONObject(0).getString("day"));
        assertEquals("18:00:00", times.getJSONObject(0).getString("begins"));
        assertEquals("23:59:59", times.getJSONObject(0).getString("ends"));
        assertEquals("tuesday", times.getJSONObject(1).getString("day"));
        assertEquals("carol", schedules.getJSONObject(1).getString("user"));
        assertEquals("daily", schedules.getJSONObject(1).getString("type"));

        final JSONArray list = new JSONArray(getJson("/oncall-roles", 200));
        JSONObject listed = null;
        for (int i = 0; i < list.length(); i++) {
            if ("rota1".equals(list.getJSONObject(i).getString("name"))) {
                listed = list.getJSONObject(i);
            }
        }
        assertNotNull(listed);
        assertEquals(2, listed.getJSONArray("schedules").length());

        // an If-Match on a read-free request is ignored; the ETag round trips through PUT
        exec(PUT, "/oncall-roles/rota1", role("rota1", null), got.getHeader("ETag"), 200);
    }

    @Test
    public void testPutReplacesSchedules() throws Exception {
        exec(POST, "/oncall-roles", role("rota2", NIGHT), null, 201);

        final MockHttpServletResponse put = exec(PUT, "/oncall-roles/rota2", role("rota2", DAILY_CAROL), null, 200);
        final JSONArray schedules = new JSONObject(put.getContentAsString()).getJSONArray("schedules");
        assertEquals(1, schedules.length());
        assertEquals("carol", schedules.getJSONObject(0).getString("user"));
        assertEquals(1, new JSONObject(getJson("/oncall-roles/rota2", 200)).getJSONArray("schedules").length());

        // omitted schedules are preserved; an explicit empty list clears
        exec(PUT, "/oncall-roles/rota2", "{\"name\":\"rota2\",\"description\":\"changed\"}", null, 200);
        final JSONObject kept = new JSONObject(getJson("/oncall-roles/rota2", 200));
        assertEquals("changed", kept.getString("description"));
        assertEquals("Ops", kept.getString("membershipGroup"));
        assertEquals(1, kept.getJSONArray("schedules").length());
        exec(PUT, "/oncall-roles/rota2", "{\"name\":\"rota2\",\"schedules\":[]}", null, 200);
        assertEquals(0, new JSONObject(getJson("/oncall-roles/rota2", 200)).getJSONArray("schedules").length());

        // an unchanged PUT does not rewrite the rows
        exec(PUT, "/oncall-roles/rota2", role("rota2", NIGHT), null, 200);
        final int version = rowVersion("rota2");
        exec(PUT, "/oncall-roles/rota2", role("rota2", NIGHT), null, 200);
        assertEquals(version, rowVersion("rota2"));
    }

    @Test
    public void testNewScheduleUserMustBeMember() throws Exception {
        final MockHttpServletResponse response = exec(POST, "/oncall-roles",
                role("rota3", "{\"user\":\"outsider\",\"type\":\"daily\",\"times\":[{\"begins\":\"08:00:00\",\"ends\":\"17:00:00\"}]}"), null, 400);
        assertTrue(response.getContentAsString(), response.getContentAsString().contains("outsider"));
        sendRequest(GET, "/oncall-roles/rota3", 404);

        // unknown user, bad times and a bad type are 400 as well
        exec(POST, "/oncall-roles", role("rota3", "{\"user\":\"ghost\",\"type\":\"daily\",\"times\":[{\"begins\":\"08:00:00\",\"ends\":\"17:00:00\"}]}"), null, 400);
        exec(POST, "/oncall-roles", role("rota3", "{\"user\":\"bob\",\"type\":\"daily\",\"times\":[{\"begins\":\"18:00:00\",\"ends\":\"17:00:00\"}]}"), null, 400);
        exec(POST, "/oncall-roles", role("rota3", "{\"user\":\"bob\",\"type\":\"daily\",\"times\":[]}"), null, 400);
        exec(POST, "/oncall-roles", role("rota3", "{\"user\":\"bob\",\"type\":\"hourly\",\"times\":[{\"begins\":\"08:00:00\",\"ends\":\"17:00:00\"}]}"), null, 400);
        sendRequest(GET, "/oncall-roles/rota3", 404);
    }

    @Test
    public void testStoredScheduleForNonMemberIsGrandfathered() throws Exception {
        inTx(() -> {
            final OnCallRole stored = new OnCallRole("rota4", m_userGroupDao.findByName("Ops"), m_userAccountDao.findByUsername("alice"));
            final OnCallSchedule schedule = new OnCallSchedule(m_userAccountDao.findByUsername("outsider"), OnCallSchedule.Type.daily);
            // a time notifd's format would reject, as a hand-edited groups.xml could have left it
            schedule.getTimes().add(new OnCallScheduleTime(null, "8:00:00", "17:00:00"));
            stored.getSchedules().add(schedule);
            m_onCallRoleDao.save(stored);
            return null;
        });

        final String existing = "{\"user\":\"outsider\",\"type\":\"daily\",\"times\":[{\"begins\":\"8:00:00\",\"ends\":\"17:00:00\"}]}";
        final JSONObject read = new JSONObject(getJson("/oncall-roles/rota4", 200));
        assertEquals("outsider", read.getJSONArray("schedules").getJSONObject(0).getString("user"));

        final MockHttpServletResponse put = exec(PUT, "/oncall-roles/rota4", role("rota4", existing + "," + NIGHT), null, 200);
        assertEquals(2, new JSONObject(put.getContentAsString()).getJSONArray("schedules").length());

        // a new schedule for the same non-member is not grandfathered
        exec(PUT, "/oncall-roles/rota4", role("rota4", existing + "," + NIGHT + ","
                + "{\"user\":\"outsider\",\"type\":\"daily\",\"times\":[{\"begins\":\"09:00:00\",\"ends\":\"10:00:00\"}]}"), null, 400);
    }

    @Test
    public void testStoredScheduleWithoutTimesEchoedWithoutTimesIsAccepted() throws Exception {
        inTx(() -> {
            final OnCallRole stored = new OnCallRole("rota7", m_userGroupDao.findByName("Ops"), m_userAccountDao.findByUsername("alice"));
            stored.getSchedules().add(new OnCallSchedule(m_userAccountDao.findByUsername("outsider"), OnCallSchedule.Type.daily));
            m_onCallRoleDao.save(stored);
            return null;
        });

        // `times` omitted matches the stored zero-time schedule, so it must not be revalidated or blow up
        final MockHttpServletResponse put = exec(PUT, "/oncall-roles/rota7",
                role("rota7", "{\"user\":\"outsider\",\"type\":\"daily\"}"), null, 200);

        final JSONObject schedule = new JSONObject(put.getContentAsString()).getJSONArray("schedules").getJSONObject(0);
        assertEquals("outsider", schedule.getString("user"));
    }

    @Test
    public void testUnknownGroupOrSupervisorIs400() throws Exception {
        exec(POST, "/oncall-roles", "{\"name\":\"rota5\",\"membershipGroup\":\"NoSuchGroup\",\"supervisor\":\"alice\"}", null, 400);
        exec(POST, "/oncall-roles", "{\"name\":\"rota5\",\"membershipGroup\":\"Ops\",\"supervisor\":\"nobody\"}", null, 400);
        exec(POST, "/oncall-roles", "{\"name\":\"rota5\",\"supervisor\":\"alice\"}", null, 400);
        exec(POST, "/oncall-roles", "{\"name\":\"rota5\",\"membershipGroup\":\"Ops\"}", null, 400);
        sendRequest(GET, "/oncall-roles/rota5", 404);

        exec(POST, "/oncall-roles", role("rota5", null), null, 201);
        exec(PUT, "/oncall-roles/rota5", "{\"membershipGroup\":\"NoSuchGroup\"}", null, 400);
        exec(PUT, "/oncall-roles/rota5", "{\"supervisor\":\"nobody\"}", null, 400);
        final int version = rowVersion("rota5");
        exec(PUT, "/oncall-roles/rota5", "{\"supervisor\":\"bob\"}", null, 200);
        assertEquals("bob", new JSONObject(getJson("/oncall-roles/rota5", 200)).getString("supervisor"));
        assertNotEquals(version, rowVersion("rota5"));
        // the name rules and lengths
        exec(POST, "/oncall-roles", role("bad<b>", null), null, 400);
        exec(POST, "/oncall-roles", role("x".repeat(257), null), null, 400);
        exec(POST, "/oncall-roles", "{\"membershipGroup\":\"Ops\",\"supervisor\":\"alice\"}", null, 400);
        exec(PUT, "/oncall-roles/rota5", role("other", null), null, 400);
    }

    @Test
    public void testDuplicateIs409() throws Exception {
        exec(POST, "/oncall-roles", role("rota6", null), null, 201);
        final MockHttpServletResponse response = exec(POST, "/oncall-roles", role("rota6", null), null, 409);
        assertEquals("On-call role rota6 already exists.", response.getContentAsString());
    }

    @Test
    public void testRenameInPlace() throws Exception {
        exec(POST, "/oncall-roles", role("rota7", NIGHT), null, 201);
        exec(POST, "/oncall-roles", role("rota7b", null), null, 201);

        exec(POST, "/oncall-roles/rota7/rename", "{\"newName\":\"rota7b\"}", null, 409);
        exec(POST, "/oncall-roles/rota7/rename", "{\"newName\":\"bad/name\"}", null, 400);
        exec(POST, "/oncall-roles/rota7/rename", "{}", null, 400);
        // the current name: there is nothing to rename
        exec(POST, "/oncall-roles/rota7/rename", "{\"newName\":\"rota7\"}", null, 400);
        exec(POST, "/oncall-roles/nosuch/rename", "{\"newName\":\"x\"}", null, 404);

        final int id = roleId("rota7");
        final MockHttpServletResponse renamed = exec(POST, "/oncall-roles/rota7/rename", "{\"newName\":\"rota7 renamed\"}", null, 200);
        final JSONObject json = new JSONObject(renamed.getContentAsString());
        assertEquals("rota7 renamed", json.getString("name"));
        assertEquals(1, json.getJSONArray("schedules").length());
        assertNotNull(renamed.getHeader("ETag"));
        assertEquals(id, roleId("rota7 renamed"));
        sendRequest(GET, "/oncall-roles/rota7", 404);
    }

    @Test
    public void testDelete() throws Exception {
        final JdbcTemplate jdbc = new JdbcTemplate(m_dataSource);
        final int schedulesBefore = jdbc.queryForObject("select count(*) from oncall_schedules", Integer.class);
        exec(POST, "/oncall-roles", role("rota8", NIGHT), null, 201);
        assertEquals(schedulesBefore + 1, jdbc.queryForObject("select count(*) from oncall_schedules", Integer.class).intValue());
        sendRequest(DELETE, "/oncall-roles/rota8", 204);
        sendRequest(GET, "/oncall-roles/rota8", 404);
        sendRequest(DELETE, "/oncall-roles/rota8", 404);
        assertEquals(schedulesBefore, jdbc.queryForObject("select count(*) from oncall_schedules", Integer.class).intValue());
    }

    @Test
    public void testStaleIfMatchIs412() throws Exception {
        exec(POST, "/oncall-roles", role("rota9", null), null, 201);
        final String stale = exec(GET, "/oncall-roles/rota9", null, null, 200).getHeader("ETag");
        exec(PUT, "/oncall-roles/rota9", "{\"description\":\"second\"}", null, 200);
        final int version = rowVersion("rota9");

        exec(PUT, "/oncall-roles/rota9", "{\"description\":\"third\"}", stale, 412);
        exec(POST, "/oncall-roles/rota9/rename", "{\"newName\":\"rota9b\"}", stale, 412);
        exec(DELETE, "/oncall-roles/rota9", null, stale, 412);
        assertEquals(version, rowVersion("rota9"));
        assertEquals("second", new JSONObject(getJson("/oncall-roles/rota9", 200)).getString("description"));

        final String current = exec(GET, "/oncall-roles/rota9", null, null, 200).getHeader("ETag");
        final MockHttpServletResponse updated = exec(PUT, "/oncall-roles/rota9", "{\"description\":\"fourth\"}", current, 200);
        assertNotEquals(current, updated.getHeader("ETag"));
        exec(POST, "/oncall-roles/rota9/rename", "{\"newName\":\"rota9b\"}", "*", 200);
        exec(DELETE, "/oncall-roles/rota9b", null, "*", 204);
    }

    @Test
    public void testEmptyDescriptionStoredAsNull() throws Exception {
        exec(POST, "/oncall-roles", "{\"name\":\"rota10\",\"description\":\"\",\"membershipGroup\":\"Ops\",\"supervisor\":\"alice\"}", null, 201);
        assertNull(description("rota10"));
        exec(PUT, "/oncall-roles/rota10", "{\"description\":\"text\"}", null, 200);
        assertEquals("text", description("rota10"));
        // an explicit empty string clears, an omitted key preserves
        exec(PUT, "/oncall-roles/rota10", "{\"supervisor\":\"alice\"}", null, 200);
        assertEquals("text", description("rota10"));
        exec(PUT, "/oncall-roles/rota10", "{\"description\":\"\"}", null, 200);
        assertNull(description("rota10"));
        final JSONObject json = new JSONObject(getJson("/oncall-roles/rota10", 200));
        assertTrue(!json.has("description") || json.isNull("description"));
    }

    @Test
    public void testNameWithSpacesRoundTrips() throws Exception {
        exec(POST, "/oncall-roles", role("Night shift", NIGHT), null, 201);
        assertEquals("Night shift", new JSONObject(getJson("/oncall-roles/Night%20shift", 200)).getString("name"));
        exec(PUT, "/oncall-roles/Night%20shift", "{\"description\":\"x\"}", null, 200);
        exec(POST, "/oncall-roles/Night%20shift/rename", "{\"newName\":\"Day shift\"}", null, 200);
        sendRequest(GET, "/oncall-roles/Night%20shift", 404);
        sendRequest(DELETE, "/oncall-roles/Day%20shift", 204);
    }

    @Test
    public void testSurroundingWhitespaceInNameIs400() throws Exception {
        exec(POST, "/oncall-roles", role(" Night ", NIGHT), null, 400);
        exec(POST, "/oncall-roles", role("Night ", NIGHT), null, 400);
        exec(POST, "/oncall-roles", role("wsrota", NIGHT), null, 201);
        exec(POST, "/oncall-roles/wsrota/rename", "{\"newName\":\" Night \"}", null, 400);
        exec(POST, "/oncall-roles/wsrota/rename", "{\"newName\":\"Night \"}", null, 400);
        sendRequest(GET, "/oncall-roles/wsrota", 200);
        sendRequest(DELETE, "/oncall-roles/wsrota", 204);
    }

    @Test
    public void testStoredBadNameIsGrandfathered() throws Exception {
        inTx(() -> {
            m_onCallRoleDao.save(new OnCallRole("a&b", m_userGroupDao.findByName("Ops"), m_userAccountDao.findByUsername("alice")));
            return null;
        });
        sendRequest(GET, "/oncall-roles/a%26b", 200);
        exec(PUT, "/oncall-roles/a%26b", "{\"description\":\"still editable\"}", null, 200);
        exec(POST, "/oncall-roles/a%26b/rename", "{\"newName\":\"a and b\"}", null, 200);
        sendRequest(DELETE, "/oncall-roles/a%20and%20b", 204);
    }

    @Test
    public void testForbiddenForNonAdmin() throws Exception {
        setUser("nobody", new String[]{ "ROLE_USER" });
        try {
            sendRequest(GET, "/oncall-roles", 403);
            exec(POST, "/oncall-roles", role("x", null), null, 403);
            sendRequest(DELETE, "/oncall-roles/x", 403);
        } finally {
            setUser("admin", new String[]{ "ROLE_ADMIN" });
        }
    }

    @Test
    public void testXmlStoreIs503() throws Exception {
        System.setProperty("org.opennms.usermgmt.store", "xml");
        try {
            final String message = "User management through this API needs org.opennms.usermgmt.store=db.";
            final List<MockHttpServletResponse> responses = Arrays.asList(
                    exec(GET, "/oncall-roles", null, null, 503),
                    exec(GET, "/oncall-roles/x", null, null, 503),
                    exec(POST, "/oncall-roles", role("x", null), null, 503),
                    exec(PUT, "/oncall-roles/x", "{}", null, 503),
                    exec(POST, "/oncall-roles/x/rename", "{\"newName\":\"y\"}", null, 503),
                    exec(DELETE, "/oncall-roles/x", null, null, 503));
            for (final MockHttpServletResponse response : responses) {
                assertEquals(message, response.getContentAsString());
            }
        } finally {
            System.clearProperty("org.opennms.usermgmt.store");
        }
    }

    @Test
    public void testManagersSeeChangeAtOnce() throws Exception {
        exec(POST, "/oncall-roles", role("rota11", NIGHT), null, 201);
        assertEquals("the rota", m_groupManager.getRole("rota11").getDescription().orElse(null));
        assertEquals(1, m_groupManager.getRole("rota11").getSchedules().size());

        exec(PUT, "/oncall-roles/rota11", role("rota11", NIGHT + "," + DAILY_CAROL), null, 200);

        // no sleep: the manager reloads when the request's transaction commits
        assertEquals(2, m_groupManager.getRole("rota11").getSchedules().size());
        exec(POST, "/oncall-roles/rota11/rename", "{\"newName\":\"rota11b\"}", null, 200);
        assertNotNull(m_groupManager.getRole("rota11b"));
    }

    @Test
    public void testJspStyleGroupSaveKeepsRoleVersion() throws Exception {
        exec(POST, "/oncall-roles", role("rota12", NIGHT + "," + DAILY_CAROL), null, 201);
        final int version = rowVersion("rota12");

        // what a JSP group edit does: write all groups and roles back through the manager
        m_groupManager.saveGroups();

        assertEquals(version, rowVersion("rota12"));
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

    /** Finds or creates a user (inside a transaction). */
    private UserAccount user(final String username) {
        UserAccount user = m_userAccountDao.findByUsername(username);
        if (user == null) {
            user = new UserAccount(username);
            user.setPasswordHash("");
            m_userAccountDao.save(user);
        }
        return user;
    }

    private int rowVersion(final String role) {
        return new JdbcTemplate(m_dataSource).queryForObject("select version from oncall_roles where name = ?", Integer.class, role);
    }

    private int roleId(final String role) {
        return new JdbcTemplate(m_dataSource).queryForObject("select id from oncall_roles where name = ?", Integer.class, role);
    }

    private String description(final String role) {
        return new JdbcTemplate(m_dataSource).queryForObject("select description from oncall_roles where name = ?", String.class, role);
    }
}
