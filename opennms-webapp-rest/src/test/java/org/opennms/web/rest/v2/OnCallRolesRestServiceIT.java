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
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.groups.Time;
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
public class OnCallRolesRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private GroupManager m_groupManager;

    @Autowired
    private UserManager m_userManager;

    public OnCallRolesRestServiceIT() {
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

    private void ensureGroup(final String name, final String... members) throws Exception {
        final Group group = new Group();
        group.setName(name);
        for (final String member : members) {
            ensureUser(member);
            group.addUser(member);
        }
        m_groupManager.saveGroup(name, group);
    }

    @Test
    public void testCreateLifecycle() throws Exception {
        ensureGroup("noc-team", "oncall1", "oncall2");
        final String body = "{\"name\":\"junit-role\",\"membership-group\":\"noc-team\",\"supervisor\":\"admin\","
                + "\"description\":\"junit on-call\","
                + "\"schedule\":[{\"user\":\"oncall1\",\"type\":\"specific\","
                + "\"time\":[{\"begins\":\"15-Jun-2093 08:00:00\",\"ends\":\"15-Jun-2093 17:00:00\"}]}]}";
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", body, 201);

        final JSONObject created = new JSONObject(getJson("/on-call-roles/junit-role", 200));
        assertEquals("noc-team", created.getString("membership-group"));
        assertEquals("admin", created.getString("supervisor"));
        assertEquals("junit on-call", created.getString("description"));
        final JSONObject schedule = created.getJSONArray("schedule").getJSONObject(0);
        assertEquals("oncall1", schedule.getString("user"));
        assertEquals("specific", schedule.getString("type"));
        assertEquals("15-Jun-2093 08:00:00", schedule.getJSONArray("time").getJSONObject(0).getString("begins"));

        // creating the same role again must be rejected
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", body, 400);

        sendRequest(DELETE, "/on-call-roles/junit-role", 204);
        sendRequest(GET, "/on-call-roles/junit-role", 404);
    }

    @Test
    public void testListIncludesCurrentlyOnCall() throws Exception {
        ensureGroup("list-team", "listuser");
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles",
                "{\"name\":\"list-role\",\"membership-group\":\"list-team\",\"supervisor\":\"admin\"}", 201);
        final JSONArray roles = new JSONArray(getJson("/on-call-roles", 200));
        boolean found = false;
        for (int i = 0; i < roles.length(); i++) {
            final JSONObject role = roles.getJSONObject(i);
            if ("list-role".equals(role.getString("name"))) {
                found = true;
                // nobody scheduled -> nobody on call (supervisor is a
                // notification fallback, not a scheduled user)
                assertTrue(role.has("currently-on-call"));
            }
        }
        assertTrue(found);
        sendRequest(DELETE, "/on-call-roles/list-role", 204);
    }

    @Test
    public void testCreateValidation() throws Exception {
        ensureGroup("val-team", "valuser");
        // name problems
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", "{\"name\":\"bad<b>\",\"membership-group\":\"val-team\",\"supervisor\":\"admin\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", "{\"name\":\"a/b\",\"membership-group\":\"val-team\",\"supervisor\":\"admin\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", "{\"name\":\"..\",\"membership-group\":\"val-team\",\"supervisor\":\"admin\"}", 400);
        // identity problems
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", "{\"name\":\"v1\",\"membership-group\":\"nosuchgroup\",\"supervisor\":\"admin\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", "{\"name\":\"v2\",\"membership-group\":\"val-team\",\"supervisor\":\"nosuchuser\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", "{\"name\":\"v3\",\"supervisor\":\"admin\"}", 400);
        // schedule problems
        final String prefix = "{\"name\":\"v4\",\"membership-group\":\"val-team\",\"supervisor\":\"admin\",\"schedule\":[";
        // user not a member of the membership group
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", prefix
                + "{\"user\":\"admin\",\"type\":\"specific\",\"time\":[{\"begins\":\"15-Jun-2093 08:00:00\",\"ends\":\"15-Jun-2093 17:00:00\"}]}]}", 400);
        // unknown type
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", prefix
                + "{\"user\":\"valuser\",\"type\":\"sometimes\",\"time\":[{\"begins\":\"08:00:00\",\"ends\":\"17:00:00\"}]}]}", 400);
        // specific with start after end
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", prefix
                + "{\"user\":\"valuser\",\"type\":\"specific\",\"time\":[{\"begins\":\"15-Jun-2093 17:00:00\",\"ends\":\"15-Jun-2093 08:00:00\"}]}]}", 400);
        // specific with bad date format
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", prefix
                + "{\"user\":\"valuser\",\"type\":\"specific\",\"time\":[{\"begins\":\"2093-06-15 08:00\",\"ends\":\"2093-06-15 17:00\"}]}]}", 400);
        // weekly without a weekday
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", prefix
                + "{\"user\":\"valuser\",\"type\":\"weekly\",\"time\":[{\"begins\":\"08:00:00\",\"ends\":\"17:00:00\"}]}]}", 400);
        // monthly with an out-of-range day
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", prefix
                + "{\"user\":\"valuser\",\"type\":\"monthly\",\"time\":[{\"day\":\"32\",\"begins\":\"08:00:00\",\"ends\":\"17:00:00\"}]}]}", 400);
        // schedule without times
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", prefix
                + "{\"user\":\"valuser\",\"type\":\"daily\",\"time\":[]}]}", 400);
        // none of the rejects may have been created
        for (final String name : new String[]{"v1","v2","v3","v4"}) {
            sendRequest(GET, "/on-call-roles/" + name, 404);
        }
    }

    @Test
    public void testWeeklyAndDailySchedulesAccepted() throws Exception {
        ensureGroup("shift-team", "shifter");
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles",
                "{\"name\":\"shift-role\",\"membership-group\":\"shift-team\",\"supervisor\":\"admin\",\"schedule\":["
                + "{\"user\":\"shifter\",\"type\":\"weekly\",\"time\":[{\"day\":\"monday\",\"begins\":\"08:00:00\",\"ends\":\"17:00:00\"}]},"
                + "{\"user\":\"shifter\",\"type\":\"daily\",\"time\":[{\"begins\":\"18:00:00\",\"ends\":\"20:00:00\"}]}]}", 201);
        final JSONObject role = new JSONObject(getJson("/on-call-roles/shift-role", 200));
        assertEquals(2, role.getJSONArray("schedule").length());
        sendRequest(DELETE, "/on-call-roles/shift-role", 204);
    }

    @Test
    public void testHandEditedScheduleStaysEditable() throws Exception {
        // a hand-edited groups.xml may carry schedules the API's validation
        // would reject (e.g. a user no longer in the membership group); the
        // role must remain editable when they round-trip unchanged
        ensureGroup("legacy-team", "member1");
        ensureUser("outsider");
        final Role role = new Role();
        role.setName("legacy-role");
        role.setMembershipGroup("legacy-team");
        role.setSupervisor("admin");
        final Schedule schedule = new Schedule();
        schedule.setName("outsider");
        schedule.setType("weekly");
        final Time time = new Time();
        time.setDay("friday");
        time.setBegins("08:00:00");
        time.setEnds("17:00:00");
        schedule.getTimes().add(time);
        role.getSchedules().add(schedule);
        m_groupManager.saveRole(role);

        // round-trip the schedule unchanged while editing the description
        sendData(PUT, MediaType.APPLICATION_JSON, "/on-call-roles/legacy-role",
                "{\"name\":\"legacy-role\",\"description\":\"touched\",\"schedule\":["
                + "{\"user\":\"outsider\",\"type\":\"weekly\",\"time\":[{\"day\":\"friday\",\"begins\":\"08:00:00\",\"ends\":\"17:00:00\"}]}]}", 204);
        final JSONObject after = new JSONObject(getJson("/on-call-roles/legacy-role", 200));
        assertEquals("touched", after.getString("description"));
        assertEquals("outsider", after.getJSONArray("schedule").getJSONObject(0).getString("user"));

        // but a NEW schedule for a non-member is still rejected
        sendData(PUT, MediaType.APPLICATION_JSON, "/on-call-roles/legacy-role",
                "{\"name\":\"legacy-role\",\"schedule\":["
                + "{\"user\":\"outsider\",\"type\":\"weekly\",\"time\":[{\"day\":\"friday\",\"begins\":\"08:00:00\",\"ends\":\"17:00:00\"}]},"
                + "{\"user\":\"outsider\",\"type\":\"daily\",\"time\":[{\"begins\":\"01:00:00\",\"ends\":\"02:00:00\"}]}]}", 400);

        sendRequest(DELETE, "/on-call-roles/legacy-role", 204);
    }

    @Test
    public void testOmittedSchedulesArePreserved() throws Exception {
        ensureGroup("keep-team", "keeper");
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles",
                "{\"name\":\"keep-role\",\"membership-group\":\"keep-team\",\"supervisor\":\"admin\",\"schedule\":["
                + "{\"user\":\"keeper\",\"type\":\"daily\",\"time\":[{\"begins\":\"08:00:00\",\"ends\":\"17:00:00\"}]}]}", 201);

        // a body without the schedule key preserves the schedules
        sendData(PUT, MediaType.APPLICATION_JSON, "/on-call-roles/keep-role",
                "{\"name\":\"keep-role\",\"description\":\"described\"}", 204);
        final JSONObject after = new JSONObject(getJson("/on-call-roles/keep-role", 200));
        assertEquals(1, after.getJSONArray("schedule").length());
        assertEquals("described", after.getString("description"));

        sendRequest(DELETE, "/on-call-roles/keep-role", 204);
    }

    @Test
    public void testRejectedUpdateLeavesNoPartialState() throws Exception {
        ensureGroup("atomic-team", "atomuser");
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles",
                "{\"name\":\"atomic-role\",\"membership-group\":\"atomic-team\",\"supervisor\":\"admin\",\"description\":\"original\"}", 201);

        // a new description arrives with an invalid schedule; nothing may apply
        sendData(PUT, MediaType.APPLICATION_JSON, "/on-call-roles/atomic-role",
                "{\"name\":\"atomic-role\",\"description\":\"changed\",\"schedule\":["
                + "{\"user\":\"atomuser\",\"type\":\"nonsense\",\"time\":[{\"begins\":\"08:00:00\",\"ends\":\"17:00:00\"}]}]}", 400);

        final JSONObject after = new JSONObject(getJson("/on-call-roles/atomic-role", 200));
        assertEquals("original", after.getString("description"));
        assertEquals("original", m_groupManager.getRole("atomic-role").getDescription().orElse(null));

        sendRequest(DELETE, "/on-call-roles/atomic-role", 204);
    }

    @Test
    public void testBodyPathNameMismatchRejected() throws Exception {
        ensureGroup("mm-team");
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles",
                "{\"name\":\"mm-role\",\"membership-group\":\"mm-team\",\"supervisor\":\"admin\"}", 201);
        sendData(PUT, MediaType.APPLICATION_JSON, "/on-call-roles/mm-role",
                "{\"name\":\"other-role\",\"description\":\"x\"}", 400);
        sendRequest(DELETE, "/on-call-roles/mm-role", 204);
    }

    @Test
    public void testRename() throws Exception {
        ensureGroup("ren-team");
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles",
                "{\"name\":\"ren-role\",\"membership-group\":\"ren-team\",\"supervisor\":\"admin\"}", 201);
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles",
                "{\"name\":\"occupied-role\",\"membership-group\":\"ren-team\",\"supervisor\":\"admin\"}", 201);

        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles/ren-role/rename", "{\"new-name\":\"occupied-role\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles/ren-role/rename", "{\"new-name\":\"bad<b>\"}", 400);

        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles/ren-role/rename", "{\"new-name\":\"renamed-role\"}", 204);
        sendRequest(GET, "/on-call-roles/ren-role", 404);
        sendRequest(GET, "/on-call-roles/renamed-role", 200);

        sendRequest(DELETE, "/on-call-roles/renamed-role", 204);
        sendRequest(DELETE, "/on-call-roles/occupied-role", 204);
    }

    @Test
    public void testCalendarShowsScheduledUserAndSupervisorGaps() throws Exception {
        ensureGroup("cal-team", "caluser");
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles",
                "{\"name\":\"cal-role\",\"membership-group\":\"cal-team\",\"supervisor\":\"admin\",\"schedule\":["
                + "{\"user\":\"caluser\",\"type\":\"specific\",\"time\":[{\"begins\":\"15-Jun-2093 08:00:00\",\"ends\":\"15-Jun-2093 17:00:00\"}]}]}", 201);

        final JSONObject calendar = new JSONObject(getJson("/on-call-roles/cal-role/calendar?year=2093&month=6", 200));
        assertEquals(30, calendar.getJSONArray("day").length());

        JSONObject day15 = null, day14 = null;
        for (int i = 0; i < calendar.getJSONArray("day").length(); i++) {
            final JSONObject day = calendar.getJSONArray("day").getJSONObject(i);
            if ("2093-06-15".equals(day.getString("date"))) day15 = day;
            if ("2093-06-14".equals(day.getString("date"))) day14 = day;
        }
        // the scheduled interval belongs to the user, not the supervisor
        boolean foundUserEntry = false;
        for (int i = 0; i < day15.getJSONArray("entry").length(); i++) {
            final JSONObject entry = day15.getJSONArray("entry").getJSONObject(i);
            if (!entry.getBoolean("supervisor")) {
                assertEquals("caluser", entry.getJSONArray("user").getString(0));
                foundUserEntry = true;
            }
        }
        assertTrue(foundUserEntry);
        // an uncovered day falls back to the supervisor
        assertTrue(day14.getJSONArray("entry").length() >= 1);
        assertTrue(day14.getJSONArray("entry").getJSONObject(0).getBoolean("supervisor"));

        // bad parameters
        sendRequest(GET, "/on-call-roles/cal-role/calendar", 400);
        sendRequest(GET, "/on-call-roles/cal-role/calendar?year=2093&month=13", 400);
        sendRequest(GET, "/on-call-roles/nosuchrole/calendar?year=2093&month=6", 404);

        sendRequest(DELETE, "/on-call-roles/cal-role", 204);
    }

    @Test
    public void testScheduleTimesNormalizedToRuntimeWidths() throws Exception {
        // BasicScheduleUtils.setOutCalTime dispatches on exact string length,
        // so loosely formatted input must be stored zero-padded
        ensureGroup("norm-team", "normuser");
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles",
                "{\"name\":\"norm-role\",\"membership-group\":\"norm-team\",\"supervisor\":\"admin\",\"schedule\":["
                + "{\"user\":\"normuser\",\"type\":\"specific\",\"time\":[{\"begins\":\"5-Jun-2093 8:00:00\",\"ends\":\"5-Jun-2093 17:00:00\"}]},"
                + "{\"user\":\"normuser\",\"type\":\"daily\",\"time\":[{\"begins\":\"8:00:00\",\"ends\":\"9:05:00\"}]}]}", 201);
        final JSONObject role = new JSONObject(getJson("/on-call-roles/norm-role", 200));
        final JSONArray schedules = role.getJSONArray("schedule");
        for (int i = 0; i < schedules.length(); i++) {
            final JSONObject schedule = schedules.getJSONObject(i);
            final JSONObject time = schedule.getJSONArray("time").getJSONObject(0);
            if ("specific".equals(schedule.getString("type"))) {
                assertEquals("05-Jun-2093 08:00:00", time.getString("begins"));
            } else {
                assertEquals("08:00:00", time.getString("begins"));
            }
        }
        sendRequest(DELETE, "/on-call-roles/norm-role", 204);
    }

    @Test
    public void testWeeklyDayCaseAndMonthlyZeroPaddingAccepted() throws Exception {
        // the runtime lowercases weekday lookups and parses 01 as 1; the API
        // must not be stricter than the scheduler it feeds
        ensureGroup("case-team", "caseuser");
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles",
                "{\"name\":\"case-role\",\"membership-group\":\"case-team\",\"supervisor\":\"admin\",\"schedule\":["
                + "{\"user\":\"caseuser\",\"type\":\"weekly\",\"time\":[{\"day\":\"Monday\",\"begins\":\"08:00:00\",\"ends\":\"17:00:00\"}]},"
                + "{\"user\":\"caseuser\",\"type\":\"monthly\",\"time\":[{\"day\":\"01\",\"begins\":\"08:00:00\",\"ends\":\"17:00:00\"}]}]}", 201);
        final JSONObject role = new JSONObject(getJson("/on-call-roles/case-role", 200));
        final JSONArray schedules = role.getJSONArray("schedule");
        for (int i = 0; i < schedules.length(); i++) {
            final JSONObject schedule = schedules.getJSONObject(i);
            if ("weekly".equals(schedule.getString("type"))) {
                assertEquals("monday", schedule.getJSONArray("time").getJSONObject(0).getString("day"));
            }
        }
        sendRequest(DELETE, "/on-call-roles/case-role", 204);
    }

    @Test
    public void testPartialIdentityUpdates() throws Exception {
        ensureGroup("pid-team");
        ensureGroup("pid-other");
        ensureUser("pidsuper");
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles",
                "{\"name\":\"pid-role\",\"membership-group\":\"pid-team\",\"supervisor\":\"admin\"}", 201);

        // each identity field is updatable on its own
        sendData(PUT, MediaType.APPLICATION_JSON, "/on-call-roles/pid-role",
                "{\"name\":\"pid-role\",\"supervisor\":\"pidsuper\"}", 204);
        JSONObject after = new JSONObject(getJson("/on-call-roles/pid-role", 200));
        assertEquals("pidsuper", after.getString("supervisor"));
        assertEquals("pid-team", after.getString("membership-group"));

        sendData(PUT, MediaType.APPLICATION_JSON, "/on-call-roles/pid-role",
                "{\"name\":\"pid-role\",\"membership-group\":\"pid-other\"}", 204);
        after = new JSONObject(getJson("/on-call-roles/pid-role", 200));
        assertEquals("pidsuper", after.getString("supervisor"));
        assertEquals("pid-other", after.getString("membership-group"));

        // but a provided value must still be valid
        sendData(PUT, MediaType.APPLICATION_JSON, "/on-call-roles/pid-role",
                "{\"name\":\"pid-role\",\"supervisor\":\"nosuchuser\"}", 400);

        sendRequest(DELETE, "/on-call-roles/pid-role", 204);
    }

    @Test
    public void testHandEditedTimeIdRoundTrips() throws Exception {
        ensureGroup("id-team", "iduser");
        final Role role = new Role();
        role.setName("id-role");
        role.setMembershipGroup("id-team");
        role.setSupervisor("admin");
        final Schedule schedule = new Schedule();
        schedule.setName("iduser");
        schedule.setType("weekly");
        final Time time = new Time();
        time.setId("hand-made-id");
        time.setDay("friday");
        time.setBegins("08:00:00");
        time.setEnds("17:00:00");
        schedule.getTimes().add(time);
        role.getSchedules().add(schedule);
        m_groupManager.saveRole(role);

        // the id is exposed on read and survives a schedule-list update that
        // sends the entry back plus a new one
        final JSONObject read = new JSONObject(getJson("/on-call-roles/id-role", 200));
        assertEquals("hand-made-id",
                read.getJSONArray("schedule").getJSONObject(0).getJSONArray("time").getJSONObject(0).getString("id"));
        sendData(PUT, MediaType.APPLICATION_JSON, "/on-call-roles/id-role",
                "{\"name\":\"id-role\",\"schedule\":["
                + "{\"user\":\"iduser\",\"type\":\"weekly\",\"time\":[{\"id\":\"hand-made-id\",\"day\":\"friday\",\"begins\":\"08:00:00\",\"ends\":\"17:00:00\"}]},"
                + "{\"user\":\"iduser\",\"type\":\"daily\",\"time\":[{\"begins\":\"18:00:00\",\"ends\":\"19:00:00\"}]}]}", 204);
        assertEquals("hand-made-id",
                m_groupManager.getRole("id-role").getSchedules().get(0).getTimes().get(0).getId().orElse(null));

        sendRequest(DELETE, "/on-call-roles/id-role", 204);
    }

    @Test
    public void testCalendarReportsServerTimeZone() throws Exception {
        ensureGroup("tz-team");
        sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles",
                "{\"name\":\"tz-role\",\"membership-group\":\"tz-team\",\"supervisor\":\"admin\"}", 201);
        final JSONObject calendar = new JSONObject(getJson("/on-call-roles/tz-role/calendar?year=2093&month=6", 200));
        assertEquals(java.time.ZoneId.systemDefault().getId(), calendar.getString("time-zone"));
        sendRequest(DELETE, "/on-call-roles/tz-role", 204);
    }

    @Test
    public void testForbiddenForNonAdmin() throws Exception {
        setUser("nobody", new String[]{ "ROLE_USER" });
        try {
            sendRequest(GET, "/on-call-roles", 403);
            sendData(POST, MediaType.APPLICATION_JSON, "/on-call-roles", "{\"name\":\"x\"}", 403);
            sendRequest(DELETE, "/on-call-roles/anything", 403);
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
