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
package org.opennms.smoketest.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * Walks a user, a group, a security role and an on-call role through the v2 user-management REST API,
 * including the ETag and If-Match handling and the refusal to delete a role that is still assigned.
 */
public class UserMgmtRestIT extends AbstractRestIT {
    private static final String BASE = "/opennms/api/v2/";
    private static final String ROLE = "ROLE_SMOKE";
    private static final String USER = "smoke1";
    private static final String PASSWORD = "SmokeTestPassword1";
    private static final String RENAMED_USER = "smoke2";
    private static final String GROUP = "SmokeOps";
    private static final String RENAMED_GROUP = "SmokeOps2";
    private static final String ONCALL = "Smoke shift";

    public UserMgmtRestIT() {
        super(Version.V2, "");
    }

    @Before
    public void cleanBefore() {
        cleanUp();
    }

    @After
    public void cleanAfter() {
        cleanUp();
    }

    /** Delete everything the test creates, dependents first. Missing objects are fine. */
    private void cleanUp() {
        applyDefaultCredentials();
        given().basePath(BASE + "oncall-roles").delete(ONCALL);
        given().basePath(BASE + "groups").delete(GROUP);
        given().basePath(BASE + "groups").delete(RENAMED_GROUP);
        given().basePath(BASE + "users").delete(USER);
        given().basePath(BASE + "users").delete(RENAMED_USER);
        given().basePath(BASE + "security-roles").delete(ROLE);
    }

    private String etagOf(final String resource, final String id) {
        final Response response = given().basePath(BASE + resource).get(id);
        response.then().statusCode(200);
        final String etag = response.getHeader("ETag");
        org.junit.Assert.assertNotNull("GET " + resource + "/" + id + " should return an ETag", etag);
        return etag;
    }

    @Test
    public void testLifecycle() {
        applyDefaultCredentials();

        // 1. a custom security role
        given().basePath(BASE + "security-roles")
                .contentType(ContentType.JSON)
                .body("{\"name\": \"" + ROLE + "\", \"description\": \"Smoke test role\"}")
                .post()
                .then().log().status()
                .statusCode(201)
                .header("ETag", notNullValue());

        // 2. a user that holds the role
        given().basePath(BASE + "users")
                .contentType(ContentType.JSON)
                .body("{\"userId\": \"" + USER + "\", \"fullName\": \"Smoke One\", \"email\": \"smoke1@example.org\","
                        + " \"password\": \"" + PASSWORD + "\", \"roles\": [\"ROLE_USER\", \"" + ROLE + "\"]}")
                .post()
                .then().log().status()
                .statusCode(201)
                .header("ETag", notNullValue());

        // 3. a group with the user as member
        given().basePath(BASE + "groups")
                .contentType(ContentType.JSON)
                .body("{\"name\": \"" + GROUP + "\", \"comments\": \"Smoke test group\", \"users\": [\"" + USER + "\"]}")
                .post()
                .then().log().status()
                .statusCode(201)
                .header("ETag", notNullValue());

        // 4. an on-call role with a weekly schedule for the user
        given().basePath(BASE + "oncall-roles")
                .contentType(ContentType.JSON)
                .body("{\"name\": \"" + ONCALL + "\", \"description\": \"Smoke test shift\","
                        + " \"membershipGroup\": \"" + GROUP + "\", \"supervisor\": \"admin\","
                        + " \"schedules\": [{\"user\": \"" + USER + "\", \"type\": \"weekly\","
                        + " \"times\": [{\"day\": \"monday\", \"begins\": \"08:00:00\", \"ends\": \"17:00:00\"}]}]}")
                .post()
                .then().log().status()
                .statusCode(201)
                .header("ETag", notNullValue());

        // 5. read each object back and capture its ETag
        given().basePath(BASE + "security-roles").get(ROLE).then().statusCode(200).body("name", is(ROLE));
        // the user count is only in the list
        given().basePath(BASE + "security-roles").get().then().statusCode(200)
                .body("find { it.name == '" + ROLE + "' }.userCount", is(1));
        given().basePath(BASE + "groups").get(GROUP).then().statusCode(200)
                .body("users", hasItem(USER));
        given().basePath(BASE + "oncall-roles").get(ONCALL).then().statusCode(200)
                .body("membershipGroup", is(GROUP)).body("supervisor", is("admin"));
        final String userEtag = etagOf("users", USER);
        etagOf("groups", GROUP);
        etagOf("oncall-roles", ONCALL);
        etagOf("security-roles", ROLE);

        // 6. a stale If-Match is refused and changes nothing
        given().basePath(BASE + "users")
                .contentType(ContentType.JSON)
                .header("If-Match", "\"999999\"")
                .body("{\"userId\": \"" + USER + "\", \"fullName\": \"Stale Name\"}")
                .put(USER)
                .then().log().status()
                .statusCode(412);
        given().basePath(BASE + "users").get(USER).then().statusCode(200).body("fullName", is("Smoke One"));

        // 7. the current If-Match is accepted and the ETag moves on
        final Response updated = given().basePath(BASE + "users")
                .contentType(ContentType.JSON)
                .header("If-Match", userEtag)
                .body("{\"userId\": \"" + USER + "\", \"fullName\": \"Smoke Uno\"}")
                .put(USER);
        updated.then().log().status().statusCode(200).body("fullName", is("Smoke Uno"));
        org.junit.Assert.assertNotEquals("A write should change the ETag", userEtag, updated.getHeader("ETag"));

        // 8. the new user can log in and holds the custom role; the role source is cached briefly, so poll
        useCredentials(USER, PASSWORD);
        org.awaitility.Awaitility.await().atMost(30, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> given().basePath("/opennms/rest/whoami").get()
                        .then().statusCode(200)
                        .body("id", is(USER))
                        .body("roles", hasItem(ROLE)));
        applyDefaultCredentials();

        // 9. a role that a user still holds cannot be deleted
        given().basePath(BASE + "security-roles").delete(ROLE)
                .then().log().status()
                .statusCode(409)
                .body(containsString(USER));
        given().basePath(BASE + "security-roles").get(ROLE).then().statusCode(200);

        // 10. rename the group: a stale If-Match is refused, the current one is accepted, and the on-call role follows
        given().basePath(BASE + "groups")
                .contentType(ContentType.JSON)
                .header("If-Match", "\"999999\"")
                .body("{\"newName\": \"" + RENAMED_GROUP + "\"}")
                .post(GROUP + "/rename")
                .then().log().status()
                .statusCode(412);
        given().basePath(BASE + "groups")
                .contentType(ContentType.JSON)
                .header("If-Match", etagOf("groups", GROUP))
                .body("{\"newName\": \"" + RENAMED_GROUP + "\"}")
                .post(GROUP + "/rename")
                .then().log().status()
                .statusCode(200)
                .header("ETag", notNullValue());
        given().basePath(BASE + "groups").get(GROUP).then().statusCode(404);
        given().basePath(BASE + "groups").get(RENAMED_GROUP).then().statusCode(200)
                .body("users", hasItem(USER));
        given().basePath(BASE + "oncall-roles").get(ONCALL).then().statusCode(200)
                .body("membershipGroup", is(RENAMED_GROUP));

        // 11. rename the user: the group membership and the on-call schedule follow
        given().basePath(BASE + "users")
                .contentType(ContentType.JSON)
                .header("If-Match", etagOf("users", USER))
                .body("{\"newUserId\": \"" + RENAMED_USER + "\"}")
                .post(USER + "/rename")
                .then().log().status()
                .statusCode(200)
                .header("ETag", notNullValue());
        given().basePath(BASE + "users").get(USER).then().statusCode(404);
        given().basePath(BASE + "users").get(RENAMED_USER).then().statusCode(200)
                .body("roles", hasItem(ROLE));
        given().basePath(BASE + "groups").get(RENAMED_GROUP).then().statusCode(200)
                .body("users", hasItem(RENAMED_USER)).body("users", not(hasItem(USER)));
        given().basePath(BASE + "oncall-roles").get(ONCALL).then().statusCode(200)
                .body("schedules[0].user", is(RENAMED_USER));

        // 12. clean up, dependents first
        given().basePath(BASE + "oncall-roles").delete(ONCALL).then().statusCode(204);
        given().basePath(BASE + "groups").delete(RENAMED_GROUP).then().statusCode(204);
        given().basePath(BASE + "users").delete(RENAMED_USER).then().statusCode(204);
        given().basePath(BASE + "security-roles").delete(ROLE).then().statusCode(204);
        given().basePath(BASE + "security-roles").get().then().statusCode(200)
                .body("name", not(hasItem(ROLE)));
    }
}
