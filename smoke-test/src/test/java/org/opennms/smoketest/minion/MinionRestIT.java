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
package org.opennms.smoketest.minion;

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
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;

import java.util.Arrays;
import java.util.List;


@Category(MinionTests.class)
public class MinionRestIT {

    private static final Logger LOG = LoggerFactory.getLogger(MinionRestIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINION;

    @Before
    public void setUp() {
        RestAssured.baseURI = stack.minion().getWebUrl().toString();
        RestAssured.port = stack.minion().getWebPort();
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
    }

    @Test
    public void testMbeansOnMinionWithJolokia() throws Exception {

        given().get("/jolokia")
                .then().assertThat()
                .statusCode(200);

        given().get("/jolokia/read/java.lang:type=Memory/HeapMemoryUsage")
                .then().assertThat().body(Matchers.containsString("HeapMemoryUsage"));

        given().get("/jolokia/read/org.opennms.core.ipc.sink.producer:name=*.dispatch,type=timers")
                .then().assertThat().body(Matchers.containsString("Heartbeat"));
    }

    @Test
    public void testRestHealthServiceOnMinion() throws Exception {

        LOG.info("testing /minion/rest/health .........");
        given().get("/minion/rest/health")
                .then().log().ifStatusCodeIsEqualTo(200)
                .statusCode(200);

        LOG.info("testing /minion/rest/health?tag=local .........");
        List<String> localDescriptions = Arrays.asList("Verifying installed bundles", "Retrieving NodeDao", "DNS Lookups (Netty)", "Karaf extender");
        List<String> descriptions = given().get("/minion/rest/health?tag=local")
                .then()
                .log().ifStatusCodeIsEqualTo(200)
                .statusCode(200)
                .body("healthy", Matchers.notNullValue())
                .extract()
                .body()
                .jsonPath().getList("responses.description",String.class);

        LOG.info("descriptions in tag 'local' is: {}", Arrays.toString(descriptions.toArray()));
        descriptions.stream().forEach(d-> Assert.assertTrue(
                String.format("Service health description '%s' must be in %s or contain '%s'", d, localDescriptions, "Verifying Listener"),
                localDescriptions.contains(d) || d.contains("Verifying Listener")));

        LOG.info("testing /minion/rest/health/probe?tag=local  .......");
        given().get("/minion/rest/health/probe?tag=local")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .body(Matchers.anyOf(Matchers.equalTo("Everything is awesome"), Matchers.equalTo("Oh no, something is wrong")));
    }
}
