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
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

/**
 * The documents and the Swagger UI are read off the classpath, and the resources
 * serving them answer 404 rather than failing when they are absent. That makes a
 * packaging slip, the documents jar dropping out of webapp-full for instance,
 * invisible to anything but a request against a running instance.
 */
public class OpenApiRestIT extends AbstractRestIT {

    private static final String V2_BASE_PATH = "/opennms/api/v2";

    public OpenApiRestIT() {
        super(Version.V1, "");
    }

    @Test
    public void servesTheV1Document() {
        given().get("openapi.json")
                .then().log().status()
                .assertThat()
                .statusCode(200)
                .body("openapi", is("3.0.1"));
    }

    @Test
    public void servesTheV2Document() {
        given().basePath(V2_BASE_PATH).get("openapi.json")
                .then().log().status()
                .assertThat()
                .statusCode(200)
                .body("openapi", is("3.0.1"));
    }

    @Test
    public void servesTheSwaggerUi() {
        given().get("api-docs/")
                .then().log().status()
                .assertThat()
                .statusCode(200);

        given().basePath(V2_BASE_PATH).get("api-docs/")
                .then().log().status()
                .assertThat()
                .statusCode(200);
    }

    /**
     * Without the redirect the page loads, but every relative reference in it,
     * the assets and the spec URL alike, resolves one directory too high.
     */
    @Test
    public void redirectsToTheSwaggerUiDirectory() {
        given().redirects().follow(false).get("api-docs")
                .then().log().status()
                .assertThat()
                .statusCode(303)
                .header("Location", endsWith("/opennms/rest/api-docs/"));
    }
}
