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
package org.opennms.smoketest.sentinel;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME;

import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.junit.SentinelTests;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;

import java.util.Arrays;
import java.util.List;


// @Category(SentinelTests.class)
@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.FlakyTests.class)
public class SentinelRestIT {

    private static final Logger LOG = LoggerFactory.getLogger(SentinelRestIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.SENTINEL;

    @Before
    public void setUp() {
        RestAssured.baseURI = stack.sentinel().getWebUrl().toString();
        RestAssured.port = stack.sentinel().getWebPort();
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
    }

    public void testRestHealthServiceOnSentinel() throws Exception {

        LOG.info("testing /sentinel/rest/health .........");
        given().get("/sentinel/rest/health")
                .then().log().ifStatusCodeIsEqualTo(200)
                .statusCode(200);

        LOG.info("testing /sentinel/rest/health?tag=local .........");
        List<String> localDescriptions = Arrays.asList("Verifying installed bundles", "Retrieving NodeDao", "DNS Lookups (Netty)");
        List<String> descriptions = given().get("/sentinel/rest/health?tag=local")
                .then()
                .log().ifStatusCodeIsEqualTo(200)
                .statusCode(200)
                .body("healthy", Matchers.notNullValue())
                .extract()
                .body()
                .jsonPath().getList("responses.description",String.class);

        LOG.info("descriptions in tag 'local' is: {}", Arrays.toString(descriptions.toArray()));
        descriptions.stream().forEach(d-> Assert.assertTrue(localDescriptions.contains(d) || d.contains("Verifying Listener")));

        LOG.info("testing /sentinel/rest/health/probe?tag=local  .......");
        given().get("/sentinel/rest/health/probe?tag=local")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .body(Matchers.anyOf(Matchers.equalTo("Everything is awesome"), Matchers.equalTo("Oh no, something is wrong")));
    }
}
