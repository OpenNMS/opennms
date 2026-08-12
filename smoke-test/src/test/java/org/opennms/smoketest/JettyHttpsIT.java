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
import static org.hamcrest.Matchers.containsString;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.OpenNMSProfile;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.SelfSignedTlsHelper;

/**
 * Verifies the embedded Jetty server can terminate TLS itself, configured
 * the way the "HTTPS server" admin documentation describes: an etc/jetty.xml
 * with the HTTPS connector enabled, a keystore, and the
 * org.opennms.netmgt.jetty.https-* properties.
 */
public class JettyHttpsIT {

    // must be initialized before the stack: the profile overlay reads the file
    private static final Path KEYSTORE = SelfSignedTlsHelper.generateKeystore(
            Paths.get("target", "jetty-https"), "jetty.keystore");

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withOpenNMS(OpenNMSProfile.newBuilder()
                    .withFile("https-server/jetty.xml", "etc/jetty.xml")
                    .withFile("https-server/https.properties", "etc/opennms.properties.d/https.properties")
                    .withFile(KEYSTORE, "etc/jetty.keystore")
                    .build())
            .build());

    @Test
    public void canServeWebUiOverHttps() {
        final InetSocketAddress httpsAddr = stack.opennms().getNetworkProtocolAddress(NetworkProtocol.HTTPS);
        final String baseUrl = String.format("https://%s:%d/opennms", httpsAddr.getHostName(), httpsAddr.getPort());

        // REST over TLS with the self-signed certificate
        given().relaxedHTTPSValidation()
                .auth().preemptive().basic(OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD)
                .get(baseUrl + "/rest/info")
                .then()
                .statusCode(200)
                .body(containsString("version"));

        // The login page renders over TLS
        given().relaxedHTTPSValidation()
                .get(baseUrl + "/login.jsp")
                .then()
                .statusCode(200)
                .body(containsString("login"));
    }

    @Test
    public void plainHttpStillWorksAlongsideHttps() {
        final InetSocketAddress httpAddr = stack.opennms().getWebAddress();
        given().auth().preemptive().basic(OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD)
                .get(String.format("http://%s:%d/opennms/rest/info", httpAddr.getHostName(), httpAddr.getPort()))
                .then()
                .statusCode(200);
    }
}
