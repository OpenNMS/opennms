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
package org.opennms.smoketest.health;

import static io.restassured.RestAssured.preemptive;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME;

import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.KarafShellUtils;
import org.opennms.smoketest.utils.TestContainerUtils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * Verifies that the health check succeeds when daemons that publish OSGi services used by
 * other Karaf features are disabled (NMS-20470).
 */
public class DisabledServicesHealthCheckIT {

    private static final List<String> DISABLED_SERVICES = List.of(
            "OpenNMS:Name=Pollerd",
            "OpenNMS:Name=Collectd",
            "OpenNMS:Name=Provisiond");

    // The default wait strategy already requires a successful health probe before the tests run
    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.minimal(b -> DISABLED_SERVICES.forEach(b::withDisabledService));

    @Before
    public void before() {
        RestAssured.reset();
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.basePath = "/opennms/rest/health";
    }

    @Test
    public void verifyServicesAreDisabled() throws Exception {
        final String xml = TestContainerUtils.getFileFromContainerAsString(stack.opennms(),
                Paths.get("/opt/opennms/etc/service-configuration.xml"));
        for (final String serviceName : DISABLED_SERVICES) {
            final String envVar = OpenNMSContainer.getServiceEnabledEnvVar(serviceName);
            assertThat("service-configuration.xml should resolve " + serviceName + " from " + envVar,
                    Pattern.compile("<service enabled=\"\\$\\{env:" + Pattern.quote(envVar) + "\\|[^}]*\\}\">\\s*<name>" + Pattern.quote(serviceName) + "</name>")
                            .matcher(xml).find(), is(true));
            assertThat(envVar + " should be set in the container",
                    stack.opennms().execInContainer("printenv", envVar).getStdout().trim(), is("false"));
        }
    }

    @Test
    public void verifyProbeHealth() {
        RestAssured.get("probe")
                .then().assertThat()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .header("Health", "Everything is awesome");
    }

    @Test
    public void verifyRestHealth() {
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
        final Response response = RestAssured.get();
        final String body = response.asString();
        assertThat(body, response.getStatusCode(), is(200));
        assertThat(body, response.jsonPath().getBoolean("healthy"), is(true));
        final List<String> statuses = response.jsonPath().getList("responses.status");
        assertThat(body, statuses, not(empty()));
        assertThat(body, statuses, everyItem(is("Success")));
    }

    @Test
    public void verifyKarafHealthCheck() {
        await().atMost(5, MINUTES)
                .pollInterval(5, SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    final var result = KarafShellUtils.executeHealthCheck(stack.opennms().getSshAddress());
                    assertThat("health check result: " + result, result.isSuccess(), is(true));
                });
    }
}
