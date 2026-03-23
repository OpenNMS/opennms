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
package org.opennms.smoketest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Smoke tests for the API token authentication feature.
 *
 * Covers: token lifecycle, bearer auth, admin cross-user ops,
 * revocation, validation, and information leakage prevention.
 */
public class ApiTokenIT {

    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.MINIMAL;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ADMIN_BASIC =
            "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes());

    @After
    public void cleanup() {
        // Best-effort revoke all tokens created during tests
        admin().path("apiTokens").queryParam("username", "admin").request()
                .header("Authorization", ADMIN_BASIC).delete();
        admin().path("apiTokens").queryParam("username", "rtc").request()
                .header("Authorization", ADMIN_BASIC).delete();
    }

    // === Authentication ===

    @Test
    public void testBasicAuthStillWorks() {
        assertEquals(200, admin().path("apiTokens").request()
                .header("Authorization", ADMIN_BASIC).get().getStatus());
    }

    @Test
    public void testValidBearerTokenAuthenticates() throws Exception {
        String token = createToken("admin", "smoke-test bearer", 30);
        assertEquals(200, bearer(token).path("apiTokens").request().get().getStatus());
    }

    @Test
    public void testInvalidBearerTokenRejected() {
        String fakeToken = "onms_" + "0".repeat(64);
        assertEquals(401, bearer(fakeToken).path("apiTokens").request().get().getStatus());
    }

    @Test
    public void testMalformedBearerTokenRejected() {
        assertEquals(401, bearer("notanopennmstoken").path("apiTokens").request().get().getStatus());
    }

    @Test
    public void testNoAuthHeaderRejected() {
        assertEquals(401, admin().path("apiTokens").request().get().getStatus());
    }

    // === Token format and create response ===

    @Test
    public void testCreateTokenReturns201WithExpectedFields() throws Exception {
        Response response = admin().path("apiTokens").request()
                .header("Authorization", ADMIN_BASIC)
                .post(Entity.json("{\"description\":\"format test\",\"expiresInDays\":30}"));
        assertEquals(201, response.getStatus());

        Map<String, Object> body = MAPPER.readValue(response.readEntity(String.class),
                new TypeReference<Map<String, Object>>() {});
        assertNotNull("id must be present", body.get("id"));
        assertNotNull("token must be present", body.get("token"));
        assertNotNull("createdAt must be present", body.get("createdAt"));
        assertNotNull("expiresAt must be present", body.get("expiresAt"));
        assertNull("tokenHash must not be returned", body.get("tokenHash"));
    }

    @Test
    public void testTokenHasCorrectPrefixAndLength() throws Exception {
        String token = createToken("admin", "prefix test", 30);
        assertTrue("token must start with onms_", token.startsWith("onms_"));
        assertEquals("token must be 69 chars (onms_ + 64 hex)", 69, token.length());
    }

    // === Token lifecycle ===

    @Test
    public void testLastUsedAtUpdatedAfterUse() throws Exception {
        String token = createToken("admin", "lastused test", 30);
        int id = tokenIdForToken(token);

        // Use the token once
        bearer(token).path("apiTokens").request().get().close();

        // Fetch token list and check lastUsedAt
        List<Map<String, Object>> tokens = listTokens("admin");
        Map<String, Object> found = tokens.stream()
                .filter(t -> Integer.valueOf(id).equals(t.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Token not found in list"));
        assertNotNull("lastUsedAt must be non-null after use", found.get("lastUsedAt"));
    }

    @Test
    public void testTokenHashNotInListResponse() throws Exception {
        createToken("admin", "hash leak test", 30);
        List<Map<String, Object>> tokens = listTokens("admin");
        for (Map<String, Object> t : tokens) {
            assertFalse("tokenHash must not appear in list response", t.containsKey("tokenHash"));
        }
    }

    @Test
    public void testRevokeOwnToken() throws Exception {
        String token = createToken("admin", "revoke test", 30);
        int id = tokenIdForToken(token);

        Response revoke = admin().path("apiTokens").path(String.valueOf(id)).request()
                .header("Authorization", ADMIN_BASIC).delete();
        assertEquals(204, revoke.getStatus());

        // Token should now be rejected
        assertEquals(401, bearer(token).path("apiTokens").request().get().getStatus());
    }

    @Test
    public void testRevokeNonExistentTokenReturns404() {
        Response response = admin().path("apiTokens").path("99999").request()
                .header("Authorization", ADMIN_BASIC).delete();
        assertEquals(404, response.getStatus());
    }

    // === Validation ===

    @Test
    public void testExpiryExceedingMaxReturns400() {
        Response response = admin().path("apiTokens").request()
                .header("Authorization", ADMIN_BASIC)
                .post(Entity.json("{\"description\":\"too long\",\"expiresInDays\":9999}"));
        assertEquals(400, response.getStatus());
        // Error body must not leak the configured max value
        String body = response.readEntity(String.class);
        assertFalse("error body must not leak max-expiry config value", body.contains("365"));
    }

    @Test
    public void testNegativeExpiryReturns400() {
        Response response = admin().path("apiTokens").request()
                .header("Authorization", ADMIN_BASIC)
                .post(Entity.json("{\"description\":\"negative\",\"expiresInDays\":-1}"));
        assertEquals(400, response.getStatus());
    }

    // === Admin cross-user operations ===

    @Test
    public void testAdminCreatesTokenForOtherUser() {
        Response response = admin().path("apiTokens").queryParam("username", "rtc").request()
                .header("Authorization", ADMIN_BASIC)
                .post(Entity.json("{\"description\":\"admin-created\",\"expiresInDays\":15}"));
        assertEquals(201, response.getStatus());
    }

    @Test
    public void testAdminListsOtherUserTokens() throws Exception {
        // Create a token for rtc as admin, then verify admin can list it
        admin().path("apiTokens").queryParam("username", "rtc").request()
                .header("Authorization", ADMIN_BASIC)
                .post(Entity.json("{\"description\":\"rtc list test\",\"expiresInDays\":15}"));

        Response list = admin().path("apiTokens").queryParam("username", "rtc").request()
                .header("Authorization", ADMIN_BASIC).get();
        assertEquals(200, list.getStatus());
        List<Map<String, Object>> tokens = MAPPER.readValue(list.readEntity(String.class),
                new TypeReference<List<Map<String, Object>>>() {});
        assertFalse("admin should see rtc's tokens", tokens.isEmpty());
    }

    @Test
    public void testAdminRevokesOtherUserToken() throws Exception {
        Response create = admin().path("apiTokens").queryParam("username", "rtc").request()
                .header("Authorization", ADMIN_BASIC)
                .post(Entity.json("{\"description\":\"rtc revoke test\",\"expiresInDays\":15}"));
        int id = MAPPER.readValue(create.readEntity(String.class),
                new TypeReference<Map<String, Object>>() {}).entrySet().stream()
                .filter(e -> "id".equals(e.getKey()))
                .mapToInt(e -> (Integer) e.getValue())
                .findFirst().orElseThrow();

        Response revoke = admin().path("apiTokens").path(String.valueOf(id)).request()
                .header("Authorization", ADMIN_BASIC).delete();
        assertEquals(204, revoke.getStatus());
    }

    // === Bulk revoke ===

    @Test
    public void testRevokeAllTokensForUser() throws Exception {
        createToken("admin", "bulk-1", 30);
        createToken("admin", "bulk-2", 30);

        Response revoke = admin().path("apiTokens").queryParam("username", "admin").request()
                .header("Authorization", ADMIN_BASIC).delete();
        assertEquals(204, revoke.getStatus());

        assertTrue("token list must be empty after revoke-all", listTokens("admin").isEmpty());
    }

    // === Helpers ===

    /** Base WebTarget for /api/v2, no auth header attached. */
    private WebTarget admin() {
        Client client = ClientBuilder.newClient();
        return client.target(stack.opennms().getWebUrl().toString() + "opennms/api/v2");
    }

    /** Base WebTarget for /api/v2 with a Bearer authorization header pre-configured. */
    private WebTarget bearer(String token) {
        Client client = ClientBuilder.newClient();
        // Register a filter that sets the Bearer header on every request
        client.register((javax.ws.rs.client.ClientRequestFilter) ctx ->
                ctx.getHeaders().putSingle("Authorization", "Bearer " + token));
        return client.target(stack.opennms().getWebUrl().toString() + "opennms/api/v2");
    }

    /** Create a token for the given user (as admin) and return the plaintext token string. */
    private String createToken(String username, String description, int expiresInDays) throws Exception {
        WebTarget target = admin().path("apiTokens");
        if (!"admin".equals(username)) {
            target = target.queryParam("username", username);
        }
        Response response = target.request()
                .header("Authorization", ADMIN_BASIC)
                .post(Entity.json(String.format(
                        "{\"description\":\"%s\",\"expiresInDays\":%d}", description, expiresInDays)));
        assertEquals(201, response.getStatus());
        Map<String, Object> body = MAPPER.readValue(response.readEntity(String.class),
                new TypeReference<Map<String, Object>>() {});
        return (String) body.get("token");
    }

    /** Return the token ID by authenticating with the token and matching it in the list. */
    private int tokenIdForToken(String token) throws Exception {
        // Use basic auth to list — bearer auth on the list endpoint also works but
        // this avoids a timing dependency on lastUsedAt being null.
        List<Map<String, Object>> tokens = listTokens("admin");
        // We can't match by plaintext (it's never stored), so return the latest ID.
        // Since tests are sequential within a single class run, the highest ID is the one just created.
        return tokens.stream()
                .mapToInt(t -> (Integer) t.get("id"))
                .max()
                .orElseThrow(() -> new AssertionError("No tokens found for admin"));
    }

    /** List tokens for a user as admin and return parsed JSON. */
    private List<Map<String, Object>> listTokens(String username) throws Exception {
        Response response = admin().path("apiTokens").queryParam("username", username).request()
                .header("Authorization", ADMIN_BASIC).get();
        assertEquals(200, response.getStatus());
        return MAPPER.readValue(response.readEntity(String.class),
                new TypeReference<List<Map<String, Object>>>() {});
    }
}
