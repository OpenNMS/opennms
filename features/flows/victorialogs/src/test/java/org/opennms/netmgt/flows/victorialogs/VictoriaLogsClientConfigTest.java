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

package org.opennms.netmgt.flows.victorialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Configuration that cannot work should be refused where an operator can see it.
 *
 * <p>Both cases here previously started cleanly and then failed on every batch, reported as flows
 * lost to an unreachable backend — pointing at the network rather than at the line of configuration
 * that was wrong.
 */
public class VictoriaLogsClientConfigTest {

    private static VictoriaLogsClientConfig config(final String url) {
        final VictoriaLogsClientConfig config = new VictoriaLogsClientConfig();
        config.setUrl(url);
        return config;
    }

    /**
     * A url that cannot address a server is reported, not thrown.
     *
     * <p>Throwing from the constructor failed the whole blueprint container, which took the health
     * check down with it — so the one place an operator looks to find out what is wrong said nothing
     * about VictoriaLogs at all. The fault is now visible instead: named by the client, reported by
     * the health check, and raised as a VictoriaLogsException by anything that tries to use it.
     */
    @Test
    public void aUrlWithoutASchemeIsReportedRatherThanThrown() throws Exception {
        for (final String url : new String[]{"", "localhost:9428", "/insert"}) {
            try (final VictoriaLogsClient client = new VictoriaLogsClient(config(url))) {
                assertTrue("'" + url + "' should have been rejected",
                        client.getConfigurationError() != null);
                assertTrue(client.getConfigurationError(),
                        client.getConfigurationError().contains("absolute"));
                assertFalse("a misconfigured client is never healthy", client.isHealthy());
                try {
                    client.ingest("{}\n");
                    fail("using a misconfigured client must fail");
                } catch (final VictoriaLogsException expected) {
                    assertTrue(expected.getMessage(), expected.getMessage().contains("misconfigured"));
                }
            }
        }
    }

    @Test
    public void anAbsoluteUrlIsAccepted() {
        for (final String url : new String[]{"http://localhost:9428", "http://localhost:9428/"}) {
            try (final VictoriaLogsClient client = new VictoriaLogsClient(config(url))) {
                assertEquals(null, client.getConfigurationError());
            }
        }
    }

    /** Half a credential pair means every request gets a 401; that must not look like anonymous. */
    @Test
    public void halfConfiguredCredentialsAreReported() {
        final VictoriaLogsClientConfig userOnly = config("http://localhost:9428");
        userOnly.setUsername("someone");
        try (final VictoriaLogsClient client = new VictoriaLogsClient(userOnly)) {
            assertTrue(String.valueOf(client.getConfigurationError()),
                    client.getConfigurationError() != null
                            && client.getConfigurationError().contains("incomplete"));
        }

        final VictoriaLogsClientConfig passwordOnly = config("http://localhost:9428");
        passwordOnly.setPassword("secret");
        try (final VictoriaLogsClient client = new VictoriaLogsClient(passwordOnly)) {
            assertTrue(String.valueOf(client.getConfigurationError()),
                    client.getConfigurationError() != null
                            && client.getConfigurationError().contains("incomplete"));
        }
    }

    @Test
    public void neitherOrBothCredentialsAreFine() {
        final VictoriaLogsClientConfig anonymous = config("http://localhost:9428");
        assertFalse(anonymous.hasCredentials());
        try (final VictoriaLogsClient client = new VictoriaLogsClient(anonymous)) {
            assertEquals(null, client.getConfigurationError());
        }

        final VictoriaLogsClientConfig both = config("http://localhost:9428");
        both.setUsername("someone");
        both.setPassword("secret");
        assertTrue(both.hasCredentials());
        try (final VictoriaLogsClient client = new VictoriaLogsClient(both)) {
            assertEquals(null, client.getConfigurationError());
        }
        assertFalse("credentials must stay out of toString", both.toString().contains("secret"));
    }
}
