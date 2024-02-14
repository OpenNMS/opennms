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

import static io.restassured.RestAssured.preemptive;
import static org.junit.Assert.assertThat;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.KarafShell;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.FlakyTests.class)
public class HealthCheckRestIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    @Before
    public void before() {
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.basePath = "/opennms/rest/health";
    }

    @Test
    public void verifyProbeHealthWithoutAuthenticationOK() {
        final String response = RestAssured.get("probe")
                .then().assertThat()
                    .statusCode(200)
                    .contentType(ContentType.TEXT)
                    .header("Health", "Everything is awesome")
                .extract().response().asString();
        assertThat(response, Matchers.is("Everything is awesome"));
    }

    @Test
    public void verifyProbeHealthWithoutAuthenticationError() {
        // Configure the elastic url incorrectly, so the rest health probe returns 599
        final KarafShell karafShell = new KarafShell(stack.opennms().getSshAddress());
        karafShell.runCommand("config:edit org.opennms.features.flows.persistence.elastic\n" +
            "config:property-set elasticUrl 192.0.2.200\n" +
            "config:update");

        // Execute the request, but limit the timeout to 1 second
        final String response = RestAssured.get("probe?t=1000")
                .then().assertThat()
                .statusCode(599)
                .statusLine("HTTP/1.1 599 Unhealthy")
                .contentType(ContentType.TEXT)
                .header("Health", "Oh no, something is wrong")
                .extract().response().asString();
        assertThat(response, Matchers.is("Oh no, something is wrong"));
    }

    @Test
    public void verifyHealth() {
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
        RestAssured.get()
                .then().assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Health", "Everything is awesome")
                .body("healthy", Matchers.is(true))
                .body("responses[0].status", Matchers.is("Success"))
                .body("responses[0].description", Matchers.is("Verifying installed bundles"))
                .body("responses[1].status", Matchers.is("Success"))
                .body("responses[1].description", Matchers.is("Connecting to ElasticSearch ReST API (Flows)"))
                .body("responses[1].message", Matchers.is("Not configured"));
    }
}
