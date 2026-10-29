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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.model.usermgmt.SecurityRole;
import org.opennms.netmgt.model.usermgmt.SecurityRoles;
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
public class SecurityRolesRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static final String JSON = MediaType.APPLICATION_JSON;

    @Autowired
    private UserAccountDao m_userAccountDao;

    @Autowired
    private UserGroupDao m_userGroupDao;

    @Autowired
    private SecurityRoleDao m_securityRoleDao;

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private DataSource m_dataSource;

    public SecurityRolesRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void beforeServletStart() {
        MockLogAppender.setupLogging();
    }

    @Before
    public void createAdminAndAdminGroup() {
        // the database starts without users or groups; the REST calls run as admin
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
    public void testListBuiltinsFirstThenByName() throws Exception {
        createRole("ROLE_ZED", null);
        createRole("ROLE_ALPHA", "first custom");
        final JSONArray roles = new JSONArray(getJson("/security-roles", 200));
        final int builtins = SecurityRoles.BUILTIN_ROLES.size();
        final List<String> expected = m_securityRoleDao.findBuiltin().stream().map(SecurityRole::getName).collect(Collectors.toList());
        // the changelog seeds exactly the built-in roles that the code knows
        assertEquals(builtins, expected.size());
        final List<String> names = new ArrayList<>();
        for (int i = 0; i < roles.length(); i++) {
            final JSONObject role = roles.getJSONObject(i);
            names.add(role.getString("name"));
            // the built-ins come first, then the custom roles
            assertEquals(role.getString("name"), i < builtins, role.getBoolean("builtin"));
        }
        assertEquals(expected, names.subList(0, builtins));
        final List<String> custom = names.subList(builtins, names.size());
        assertEquals(m_securityRoleDao.findUserDefined().stream().map(SecurityRole::getName).collect(Collectors.toList()), custom);
        assertTrue(custom.indexOf("ROLE_ALPHA") >= 0 && custom.indexOf("ROLE_ALPHA") < custom.indexOf("ROLE_ZED"));
        final JSONObject alpha = roles.getJSONObject(builtins + custom.indexOf("ROLE_ALPHA"));
        assertEquals("first custom", alpha.getString("description"));
    }

    @Test
    public void testUserCount() throws Exception {
        createRole("ROLE_COUNTED", null);
        createUser("u1", "ROLE_COUNTED", "ROLE_USER");
        createUser("u2", "ROLE_COUNTED");
        assertEquals(2, userCountInList("ROLE_COUNTED"));
        assertEquals(1, userCountInList("ROLE_USER"));
        assertEquals(1, userCountInList("ROLE_ADMIN"));
        assertEquals(0, userCountInList("ROLE_JMX"));
        sendRequest(GET, "/security-roles/ROLE_NOPE", 404);
    }

    @Test
    public void testSingleRoleHasNoUserCountAndGrantsKeepItsVersion() throws Exception {
        createRole("ROLE_GRANTED", null);
        final MockHttpServletResponse before = exec(GET, "/security-roles/ROLE_GRANTED", null, null, 200);
        assertFalse(new JSONObject(before.getContentAsString()).has("userCount"));

        // a grant changes the user, not the role: the role's version and ETag stay as they are
        createUser("granted", "ROLE_GRANTED");
        final MockHttpServletResponse after = exec(GET, "/security-roles/ROLE_GRANTED", null, null, 200);
        assertEquals(before.getHeader("ETag"), after.getHeader("ETag"));
        assertEquals(before.getContentAsString(), after.getContentAsString());
        assertEquals(1, userCountInList("ROLE_GRANTED"));
    }

    @Test
    public void testCreateNormalizesName() throws Exception {
        final MockHttpServletResponse response = exec(POST, "/security-roles", "{\"name\":\"operator\",\"description\":\"Ops\"}", null, 201);
        final JSONObject created = new JSONObject(response.getContentAsString());
        assertEquals("ROLE_OPERATOR", created.getString("name"));
        assertEquals("Ops", created.getString("description"));
        assertFalse(created.getBoolean("builtin"));
        assertFalse(created.has("userCount"));
        assertEquals("\"" + created.getInt("version") + "\"", response.getHeader("ETag"));
        assertEquals(rowVersion("ROLE_OPERATOR"), created.getInt("version"));

        final JSONObject mixed = new JSONObject(exec(POST, "/security-roles", "{\"name\":\"ROLE_Stage\"}", null, 201).getContentAsString());
        assertEquals("ROLE_STAGE", mixed.getString("name"));
        sendRequest(GET, "/security-roles/ROLE_STAGE", 200);
    }

    @Test
    public void testPathNameIsNormalizedLikeThePost() throws Exception {
        exec(POST, "/security-roles", "{\"name\":\"auditor\"}", null, 201);
        // GET, PUT and DELETE take the short name, in any case, as the POST does
        for (final String path : new String[] { "/security-roles/auditor", "/security-roles/Auditor", "/security-roles/ROLE_AUDITOR" }) {
            assertEquals("ROLE_AUDITOR", new JSONObject(getJson(path, 200)).getString("name"));
        }
        final JSONObject updated = new JSONObject(exec(PUT, "/security-roles/auditor", "{\"description\":\"Reads\"}", null, 200).getContentAsString());
        assertEquals("ROLE_AUDITOR", updated.getString("name"));
        assertEquals("Reads", updated.getString("description"));
        // the name in the body is normalized too
        exec(PUT, "/security-roles/auditor", "{\"name\":\"auditor\",\"description\":\"Reads more\"}", null, 200);
        exec(PUT, "/security-roles/auditor", "{\"name\":\"other\"}", null, 400);
        sendRequest(DELETE, "/security-roles/auditor", 204);
        sendRequest(GET, "/security-roles/ROLE_AUDITOR", 404);
        sendRequest(GET, "/security-roles/auditor", 404);
    }

    @Test
    public void testBlankNamesAreNotFoundOrBadRequest() throws Exception {
        sendRequest(GET, "/security-roles/%20", 404);
        exec(PUT, "/security-roles/%20", "{\"description\":\"x\"}", null, 404);
        sendRequest(DELETE, "/security-roles/%20", 404);
        exec(PUT, "/security-roles/%20", "{\"description\":\"x\"}", "\"1\"", 412);
        createRole("ROLE_BLANKBODY", null);
        exec(PUT, "/security-roles/ROLE_BLANKBODY", "{\"name\":\"\"}", null, 400);
        exec(PUT, "/security-roles/ROLE_BLANKBODY", "{\"name\":\"  \"}", null, 400);
        exec(POST, "/security-roles", "{\"name\":\"  \"}", null, 400);
    }

    @Test
    public void testCreateRejectsBadNames() throws Exception {
        exec(POST, "/security-roles", "{\"name\":\"ROLE_\"}", null, 400);
        exec(POST, "/security-roles", "{\"name\":\"ROLE_A-B\"}", null, 400);
        exec(POST, "/security-roles", "{\"name\":\"a b\"}", null, 400);
        exec(POST, "/security-roles", "{\"name\":\"" + "A".repeat(129 - "ROLE_".length()) + "\"}", null, 400);
        exec(POST, "/security-roles", "{\"name\":\"\"}", null, 400);
        exec(POST, "/security-roles", "{}", null, 400);
        // 128 characters is the limit
        exec(POST, "/security-roles", "{\"name\":\"" + "A".repeat(128 - "ROLE_".length()) + "\"}", null, 201);
    }

    @Test
    public void testCreateDuplicateIs409() throws Exception {
        exec(POST, "/security-roles", "{\"name\":\"dupe\"}", null, 201);
        exec(POST, "/security-roles", "{\"name\":\"ROLE_DUPE\"}", null, 409);
        // a built-in name is a duplicate too
        exec(POST, "/security-roles", "{\"name\":\"admin\"}", null, 409);
        exec(POST, "/security-roles", "{\"name\":\"ROLE_ADMIN\"}", null, 409);
    }

    @Test
    public void testUpdateDescription() throws Exception {
        exec(POST, "/security-roles", "{\"name\":\"editme\",\"description\":\"before\"}", null, 201);
        final String before = exec(GET, "/security-roles/ROLE_EDITME", null, null, 200).getHeader("ETag");
        final MockHttpServletResponse updated = exec(PUT, "/security-roles/ROLE_EDITME", "{\"description\":\"after\"}", null, 200);
        assertEquals("after", new JSONObject(updated.getContentAsString()).getString("description"));
        assertNotEquals(before, updated.getHeader("ETag"));
        assertEquals("after", new JSONObject(getJson("/security-roles/ROLE_EDITME", 200)).getString("description"));
        // a body that omits the description leaves it alone
        exec(PUT, "/security-roles/ROLE_EDITME", "{}", null, 200);
        exec(PUT, "/security-roles/ROLE_EDITME", "{\"name\":\"ROLE_EDITME\"}", null, 200);
        assertEquals("after", new JSONObject(getJson("/security-roles/ROLE_EDITME", 200)).getString("description"));
        // an empty description clears it; a mismatching body name is refused
        exec(PUT, "/security-roles/ROLE_EDITME", "{\"name\":\"ROLE_EDITME\",\"description\":\"\"}", null, 200);
        assertTrue(new JSONObject(getJson("/security-roles/ROLE_EDITME", 200)).isNull("description"));
        exec(PUT, "/security-roles/ROLE_EDITME", "{\"name\":\"ROLE_OTHER\",\"description\":\"x\"}", null, 400);
        exec(PUT, "/security-roles/ROLE_NOPE", "{\"description\":\"x\"}", null, 404);
    }

    @Test
    public void testDescriptionLimit() throws Exception {
        final String limit = "x".repeat(4096);
        final String tooLong = "x".repeat(4097);
        exec(POST, "/security-roles", "{\"name\":\"freetext\",\"description\":\"" + limit + "\"}", null, 201);
        final MockHttpServletResponse put = exec(PUT, "/security-roles/ROLE_FREETEXT", "{\"description\":\"" + tooLong + "\"}", null, 400);
        assertEquals("Security role \"ROLE_FREETEXT\": the description is longer than 4096 characters.", put.getContentAsString());
        final MockHttpServletResponse post = exec(POST, "/security-roles", "{\"name\":\"freetext2\",\"description\":\"" + tooLong + "\"}", null, 400);
        assertEquals("Security role \"ROLE_FREETEXT2\": the description is longer than 4096 characters.", post.getContentAsString());
        sendRequest(GET, "/security-roles/ROLE_FREETEXT2", 404);
        assertEquals(limit, new JSONObject(getJson("/security-roles/ROLE_FREETEXT", 200)).getString("description"));
    }

    @Test
    public void testStoredLongDescriptionIsGrandfathered() throws Exception {
        // written before the limit
        final String stored = "y".repeat(5000);
        createRole("ROLE_LONGTEXT", stored);
        final int version = rowVersion("ROLE_LONGTEXT");

        // echoed back unchanged, with surrounding whitespace that is not stored
        exec(PUT, "/security-roles/ROLE_LONGTEXT", "{\"name\":\"ROLE_LONGTEXT\",\"description\":\" " + stored + "\"}", null, 200);
        assertEquals(stored, new JSONObject(getJson("/security-roles/ROLE_LONGTEXT", 200)).getString("description"));
        assertEquals(version, rowVersion("ROLE_LONGTEXT"));

        // but a changed description over the limit is refused
        exec(PUT, "/security-roles/ROLE_LONGTEXT", "{\"description\":\"" + stored + "z\"}", null, 400);
        assertEquals(stored, new JSONObject(getJson("/security-roles/ROLE_LONGTEXT", 200)).getString("description"));
        assertEquals(version, rowVersion("ROLE_LONGTEXT"));
    }

    @Test
    public void testBuiltinDescriptionReadOnly() throws Exception {
        final MockHttpServletResponse response = exec(PUT, "/security-roles/ROLE_USER", "{\"description\":\"mine\"}", null, 400);
        assertNotNull(response.getContentAsString());
        assertEquals(SecurityRoles.BUILTIN_ROLES.get("ROLE_USER"),
                new JSONObject(getJson("/security-roles/ROLE_USER", 200)).getString("description"));
    }

    @Test
    public void testDeleteBuiltinIs400() throws Exception {
        exec(DELETE, "/security-roles/ROLE_ADMIN", null, null, 400);
        sendRequest(GET, "/security-roles/ROLE_ADMIN", 200);
    }

    @Test
    public void testDeleteAssignedIs409ListingUsers() throws Exception {
        createRole("ROLE_HELD", null);
        createUser("zoe", "ROLE_HELD");
        createUser("adam", "ROLE_HELD");
        createUser("mike", "ROLE_HELD");
        final String body = exec(DELETE, "/security-roles/ROLE_HELD", null, null, 409).getContentAsString();
        assertTrue(body, body.contains("adam, mike, zoe"));
        sendRequest(GET, "/security-roles/ROLE_HELD", 200);
    }

    @Test
    public void testDeleteUnassigned() throws Exception {
        createRole("ROLE_FREE", null);
        exec(DELETE, "/security-roles/ROLE_FREE", null, null, 204);
        sendRequest(GET, "/security-roles/ROLE_FREE", 404);
        exec(DELETE, "/security-roles/ROLE_FREE", null, null, 404);
    }

    @Test
    public void testStaleIfMatchIs412() throws Exception {
        exec(POST, "/security-roles", "{\"name\":\"stale\",\"description\":\"First\"}", null, 201);
        final String stale = exec(GET, "/security-roles/ROLE_STALE", null, null, 200).getHeader("ETag");
        exec(PUT, "/security-roles/ROLE_STALE", "{\"description\":\"Second\"}", null, 200);
        final int version = rowVersion("ROLE_STALE");

        exec(PUT, "/security-roles/ROLE_STALE", "{\"description\":\"Third\"}", stale, 412);
        exec(DELETE, "/security-roles/ROLE_STALE", null, stale, 412);
        assertEquals(version, rowVersion("ROLE_STALE"));
        assertEquals("Second", new JSONObject(getJson("/security-roles/ROLE_STALE", 200)).getString("description"));

        final String current = exec(GET, "/security-roles/ROLE_STALE", null, null, 200).getHeader("ETag");
        exec(PUT, "/security-roles/ROLE_STALE", "{\"description\":\"Fourth\"}", current, 200);
        exec(DELETE, "/security-roles/ROLE_STALE", null, "*", 204);
    }

    @Test
    public void testXmlStoreIs503() throws Exception {
        System.setProperty("org.opennms.usermgmt.store", "xml");
        final String message = "User management through this API needs org.opennms.usermgmt.store=db.";
        final List<MockHttpServletResponse> responses = Arrays.asList(
                exec(GET, "/security-roles", null, null, 503),
                exec(GET, "/security-roles/ROLE_USER", null, null, 503),
                exec(POST, "/security-roles", "{\"name\":\"x\"}", null, 503),
                exec(PUT, "/security-roles/ROLE_USER", "{\"description\":\"x\"}", null, 503),
                exec(DELETE, "/security-roles/ROLE_USER", null, null, 503));
        for (final MockHttpServletResponse response : responses) {
            assertEquals(message, response.getContentAsString());
        }
    }

    @Test
    public void testForbiddenForNonAdmin() throws Exception {
        setUser("nobody", new String[]{ "ROLE_USER" });
        try {
            sendRequest(GET, "/security-roles", 403);
            exec(POST, "/security-roles", "{\"name\":\"x\"}", null, 403);
            sendRequest(DELETE, "/security-roles/ROLE_USER", 403);
        } finally {
            setUser("admin", new String[]{ "ROLE_ADMIN" });
        }
    }

    @Test
    public void testNewRoleAvailableToUsersApi() throws Exception {
        exec(POST, "/security-roles", "{\"name\":\"newrole\"}", null, 201);
        final JSONArray available = new JSONArray(getJson("/users/available-roles", 200));
        boolean found = false;
        for (int i = 0; i < available.length(); i++) {
            found |= "ROLE_NEWROLE".equals(available.getString(i));
        }
        assertTrue(found);

        createUser("carol");
        exec(PUT, "/users/carol", "{\"userId\":\"carol\",\"roles\":[\"ROLE_NEWROLE\"]}", null, 200);
        assertEquals(1, userCountInList("ROLE_NEWROLE"));
    }

    private int userCountInList(final String name) throws Exception {
        final JSONArray roles = new JSONArray(getJson("/security-roles", 200));
        for (int i = 0; i < roles.length(); i++) {
            if (name.equals(roles.getJSONObject(i).getString("name"))) {
                return roles.getJSONObject(i).getInt("userCount");
            }
        }
        throw new AssertionError(name + " is not in the list");
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

    /** Commits a custom role, as an earlier API call would have left it. */
    private void createRole(final String name, final String description) {
        inTx(() -> {
            m_securityRoleDao.save(new SecurityRole(name, description));
            return null;
        });
    }

    /** Commits a user holding the given roles. */
    private void createUser(final String username, final String... roles) {
        inTx(() -> {
            if (m_userAccountDao.findByUsername(username) == null) {
                final UserAccount user = new UserAccount(username);
                user.setPasswordHash("");
                for (final String role : roles) {
                    user.getSecurityRoles().add(m_securityRoleDao.findByName(role));
                }
                m_userAccountDao.save(user);
            }
            return null;
        });
    }

    private int rowVersion(final String role) {
        return new JdbcTemplate(m_dataSource).queryForObject("select version from security_roles where name = ?", Integer.class, role);
    }
}
