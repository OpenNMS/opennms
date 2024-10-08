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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests that sites deployed in the osgi container can be reached from jetty (see NMS-7785).
 *
 * In addition it verifies that access to /osgi/** is not allowed (see NMS-8431).
 *
 * @author mvrueden
 */
public class OsgiDeployedApplicationsIT extends OpenNMSSeleniumIT {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiDeployedApplicationsIT.class);

    // SEE NMS-8431
    @Test
    public void verifyOsgiUrlIsNotAccessibleDirectly() throws IOException {
        final String[] paths = new String[]{
                "osgi/jmx-config-tool",
                "osgi/vaadin-surveillance-views?dashboard=true",
                "osgi/vaadin-surveillance-views?dashboard=false",
                "osgi/vaadin-surveillance-views-config",
                "osgi/wallboard-config",
                "osgi/bsm-admin-page",
                "osgi/topology"
        };

        verifyUrls(paths, 403, "FORBIDDEN");
    }

    // See NMS-7785
    @Test
    public void verifyOsgiUrlIsAccessibleViaBridge() throws IOException {
        final String[] paths = new String[]{
                "admin/jmx-config-tool",
                "vaadin-surveillance-views?dashboard=true",
                "vaadin-surveillance-views?dashboard=false",
                "admin/vaadin-surveillance-views-config",
                "admin/wallboard-config",
                "admin/bsm-admin-page",
                "topology"
        };

        verifyUrls(paths, 200, "OK");
    }

    private void verifyUrls(final String[] paths, final int expectedStatus, final String expectedStatusText) throws IOException {
        for (final String eachPath : paths) {
            final String urlString = getBaseUrlExternal() + "opennms/" + eachPath;
            LOG.info("Verifying url '{}' ...", urlString);

            final URL url = new URL(urlString);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty ("Authorization", "Basic " + Base64.getEncoder().encodeToString((OpenNMSContainer.ADMIN_USER + ":" + OpenNMSContainer.ADMIN_PASSWORD).getBytes()));
            connection.setConnectTimeout(250);
            connection.setUseCaches(false);
            connection.setDoOutput(false);
            connection.setInstanceFollowRedirects(false); // we do not want to follow redirects, otherwise 200 OK might be returned
            connection.connect();
            LOG.info("Status: {} {}", connection.getResponseCode(), connection.getResponseMessage());

            // All unauthorized requests are forwarded to login.jsp which will result in 200 OK, we
            // therefore fail, because we should have been logged in correctly.
            if (302 == connection.getResponseCode()) {
                LOG.info("Try redirecting to: {}", connection.getHeaderField("Location"));
                Assert.fail("Request was forwarded. This is not allowed.");
            }

            // Valid request, now check for OK
            final String errorMessage = String.format("URL: %s: status %s (%s) expected but %s (%s) received.",
                urlString, expectedStatus, expectedStatusText, connection.getResponseCode(),  connection.getResponseMessage());
            Assert.assertEquals(errorMessage, expectedStatus, connection.getResponseCode());
            LOG.info("Test passed");
        }
    }

}
