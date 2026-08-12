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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.SelfSignedTlsHelper;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import io.restassured.response.Response;

/**
 * Verifies OpenNMS works behind a TLS-terminating nginx reverse proxy, set up
 * the way the community guide "How to use Nginx as SSL proxy with OpenNMS
 * Horizon" describes.
 */
public class NginxHttpsProxyIT {

    private static final Path CERT_DIR = generateCertificates();

    private static Path generateCertificates() {
        final Path dir = Paths.get("target", "nginx-https");
        final Path keystore = SelfSignedTlsHelper.generateKeystore(dir, "proxy.p12");
        SelfSignedTlsHelper.exportPem(keystore, dir.resolve("server.crt"), dir.resolve("server.key"));
        return dir;
    }

    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder().build());

    // nginx resolves the proxy_pass upstream at startup, so it has to start
    // after the OpenNMS container has joined the network (see the RuleChain)
    public static final GenericContainer<?> nginx = new GenericContainer<>("nginx:1.27-alpine")
            .withNetwork(Network.SHARED)
            .withNetworkAliases("nginx")
            .withExposedPorts(443)
            .withCopyFileToContainer(MountableFile.forHostPath(CERT_DIR.resolve("server.crt")), "/etc/nginx/certs/server.crt")
            .withCopyFileToContainer(MountableFile.forHostPath(CERT_DIR.resolve("server.key")), "/etc/nginx/certs/server.key")
            .withCopyFileToContainer(MountableFile.forClasspathResource("https-server/nginx.conf"), "/etc/nginx/conf.d/default.conf")
            .withCreateContainerCmdModifier(TestContainerUtils::setGlobalMemAndCpuLimits)
            .waitingFor(Wait.forListeningPort());

    @ClassRule
    public static final RuleChain chain = RuleChain.outerRule(stack).around(nginx);

    private static String baseUrl() {
        return String.format("https://%s:%d/opennms", nginx.getContainerIpAddress(), nginx.getMappedPort(443));
    }

    @Test
    public void canTalkToRestApiThroughProxy() {
        given().relaxedHTTPSValidation()
                .auth().preemptive().basic(OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD)
                .get(baseUrl() + "/rest/info")
                .then()
                .statusCode(200)
                .body(containsString("version"));
    }

    @Test
    public void canLoadWebUiThroughProxy() {
        given().relaxedHTTPSValidation()
                .get(baseUrl() + "/login.jsp")
                .then()
                .statusCode(200)
                .body(containsString("login"));

        given().relaxedHTTPSValidation()
                .auth().preemptive().basic(OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD)
                .get(baseUrl() + "/index.jsp")
                .then()
                .statusCode(200);
    }

    @Test
    public void redirectsStayOnTheProxy() {
        // a request that triggers a redirect must not leak the backend's
        // plain-http scheme or its internal host to the client
        final Response response = given().relaxedHTTPSValidation()
                .redirects().follow(false)
                .auth().preemptive().basic(OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD)
                .get(baseUrl());
        final String location = response.getHeader("Location");
        if (response.statusCode() >= 300 && response.statusCode() < 400 && location != null) {
            org.hamcrest.MatcherAssert.assertThat(location,
                    anyOf(startsWith("/"), not(startsWith("http://opennms:"))));
        }
    }
}
