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

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import java.util.concurrent.TimeUnit;
import static org.awaitility.Awaitility.await;
import static io.restassured.RestAssured.given;
public class RoleBaseRestAccessIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    @Before
    public void setUp() {
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.basePath = "/opennms/rest";

    }

    @Test
    public void addUserWthoutRESTRoleAndCheckApisAccess() {
        String xmlPayload =
                "<user>" +
                        "<user-id>testuser</user-id>" +
                        "<full-name>API Test User</full-name>" +
                        "<password>testuser123</password>" +
                        "<passwordSalt>true</passwordSalt>" +
                        "<role>ROLE_USER</role>" +
                        "</user>";

        given()
                .auth().basic("admin", "admin")
                .contentType("application/xml")
                .accept("application/xml")
                .body(xmlPayload)
                .log().all()
                .when()
                .post("/users?hashPassword=true")
                .then()
                .log().all()
                .statusCode(201);

        await().atMost(10, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                    .auth().basic("testuser", "testuser123")
                    .when()
                    .get("/classifications?groupFilter=1&limit=100&offset=0")
                    .then()
                    .statusCode(403);
        });


    }

    @Test
    public void addUserWthRESTRoleAndCheckApisAccess() {
        String xmlPayload =
                "<user>" +
                        "<user-id>testuser1</user-id>" +
                        "<full-name>API Test User</full-name>" +
                        "<password>testuser1231</password>" +
                        "<passwordSalt>true</passwordSalt>" +
                        "<role>ROLE_REST</role>" +
                        "</user>";

        given()
                .auth().basic("admin", "admin")
                .contentType("application/xml")
                .accept("application/xml")
                .body(xmlPayload)
                .log().all()
                .when()
                .post("/users?hashPassword=true")
                .then()
                .log().all()
                .statusCode(201);

        await().atMost(10, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                    .auth().basic("testuser1", "testuser1231")
                    .when()
                    .get("/classifications?groupFilter=1&limit=100&offset=0")
                    .then()
                    .statusCode(200);
        });


    }


}
