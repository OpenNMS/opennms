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
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.KarafShell;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Verifies how the web application publishes ReST resources that bundles export as OSGi services.
 * Each test uses a resource from a bundle that the default installation starts.
 */
public class OsgiRestIT {

    private static final String SCV_REST_BUNDLE = "org.opennms.features.scv.rest";
    private static final String MASKED_VALUE = "******";
    private static final String XML_UTF_8 = "application/xml; charset=UTF-8";
    private static final Duration PUBLISH_TIMEOUT = Duration.ofMinutes(2);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    @Before
    public void setUp() {
        RestAssured.reset();
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.basePath = "/opennms";
    }

    @Test
    public void servesResourceWithRestApplicationPath() {
        asAdmin()
                .accept(ContentType.JSON)
        .when()
                .get("/rest/cm")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("", hasItem("provisiond"));
    }

    @Test
    public void servesResourceWithApiV2ApplicationPath() {
        asAdmin()
                .accept(ContentType.JSON)
        .when()
                .get("/api/v2/graphs")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", hasItem("application"));
    }

    @Test
    public void convertsJsonRequestAndResponseEntities() {
        final String alias = "osgi-rest-it-json";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("token", "secret-token");
        final Map<String, Object> credentials = new HashMap<>();
        credentials.put("alias", alias);
        credentials.put("username", "osgi-user");
        credentials.put("password", "osgi-password");
        credentials.put("attributes", attributes);

        asAdmin()
                .contentType(ContentType.JSON)
                .body(credentials)
        .when()
                .post("/rest/scv")
        .then()
                .statusCode(202);
        try {
            asAdmin()
                    .accept(ContentType.JSON)
            .when()
                    .get("/rest/scv")
            .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("", hasItem(alias));

            asAdmin()
                    .accept(ContentType.JSON)
            .when()
                    .get("/rest/scv/{alias}", alias)
            .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("alias", equalTo(alias))
                    .body("username", equalTo("osgi-user"))
                    .body("password", equalTo(MASKED_VALUE))
                    .body("attributes.token", equalTo(MASKED_VALUE));
        } finally {
            asAdmin()
            .when()
                    .delete("/rest/scv/{alias}", alias)
            .then()
                    .statusCode(204);
        }

        asAdmin()
                .accept(ContentType.JSON)
        .when()
                .get("/rest/scv/{alias}", alias)
        .then()
                .statusCode(204);
    }

    @Test
    public void convertsXmlRequestAndResponseEntities() throws IOException {
        final String graphName = "osgi-rest-it-xml";

        asAdmin()
                .contentType(XML_UTF_8)
                .body(graphml())
        .when()
                .post("/rest/graphml/{name}", graphName)
        .then()
                .statusCode(201);
        try {
            asAdmin()
                    .accept(ContentType.XML)
            .when()
                    .get("/rest/graphml/{name}", graphName)
            .then()
                    .statusCode(200)
                    .contentType(ContentType.XML)
                    .body(containsString("<graphml"));
        } finally {
            asAdmin()
            .when()
                    .delete("/rest/graphml/{name}", graphName)
            .then()
                    .statusCode(200);
        }
    }

    @Test
    public void appliesExceptionMapperServices() {
        // The resource throws a NoSuchElementException. An ExceptionMapper service converts it.
        asAdmin()
                .accept(ContentType.XML)
        .when()
                .get("/rest/graphml/{name}", "osgi-rest-it-missing")
        .then()
                .statusCode(404)
                .contentType(ContentType.TEXT)
                .body(containsString("No GraphML file found"));
    }

    @Test
    public void returnsNotFoundForUnknownSubPath() {
        asAdmin()
        .when()
                .get("/rest/health/osgi-rest-it-missing")
        .then()
                .statusCode(404)
                .body(not(containsString("<html")));
    }

    @Test
    public void returnsBadRequestForInvalidEntity() {
        asAdmin()
                .contentType(XML_UTF_8)
                .body("INVALID-XML".getBytes(StandardCharsets.UTF_8))
        .when()
                .post("/rest/graphml/{name}", "osgi-rest-it-invalid")
        .then()
                .statusCode(400)
                .body(not(containsString("<html")));
    }

    @Test
    public void returnsMethodNotAllowedForUnsupportedMethod() {
        asAdmin()
        .when()
                .delete("/rest/scv")
        .then()
                .statusCode(405)
                .body(not(containsString("<html")));
    }

    @Test
    public void returnsNotAcceptableForUnsupportedAcceptType() {
        asAdmin()
                .accept(ContentType.XML)
        .when()
                .get("/rest/scv")
        .then()
                .statusCode(406)
                .body(not(containsString("<html")));
    }

    @Test
    public void returnsUnsupportedMediaTypeForUnsupportedContentType() {
        asAdmin()
                .contentType(ContentType.TEXT)
                .body("osgi-rest-it")
        .when()
                .post("/rest/scv")
        .then()
                .statusCode(415)
                .body(not(containsString("<html")));
    }

    @Test
    public void passesAuthenticatedUserToResource() {
        // The search resource reads the user and the roles of the user from the SecurityContext.
        asAdmin()
                .accept(ContentType.JSON)
                .queryParam("_s", "Configure Users")
        .when()
                .get("/api/v2/search")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("results.label.flatten()", hasItem("Configure Users"));
    }

    @Test
    public void rejectsAnonymousRequest() {
        given()
        .when()
                .get("/rest/scv")
        .then()
                .statusCode(401);
    }

    @Test
    public void leavesWebApplicationResourcesReachable() {
        // The web application serves this resource. No bundle exports it.
        asAdmin()
                .accept(ContentType.JSON)
        .when()
                .get("/rest/info")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("packageName", equalTo("opennms"));
    }

    @Test
    public void followsBundleLifecycle() {
        final KarafShell karafShell = new KarafShell(stack.opennms().getSshAddress());
        assertScvStatus(200);

        karafShell.runCommand("bundle:stop " + SCV_REST_BUNDLE);
        try {
            assertScvStatus(404);
        } finally {
            karafShell.runCommand("bundle:start " + SCV_REST_BUNDLE);
        }
        assertScvStatus(200);
    }

    private static void assertScvStatus(final int expectedStatus) {
        await().atMost(PUBLISH_TIMEOUT)
                .pollInterval(Duration.ofSeconds(2))
                .until(() -> asAdmin().accept(ContentType.JSON).get("/rest/scv").statusCode(), equalTo(expectedStatus));
    }

    private static RequestSpecification asAdmin() {
        return given().auth().preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
    }

    private static byte[] graphml() throws IOException {
        try (InputStream stream = OsgiRestIT.class.getResourceAsStream("/topology/graphml/test-topology.xml")) {
            return stream.readAllBytes();
        }
    }
}
