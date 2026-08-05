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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.health.api.Status;

/**
 * The health check must stay quiet while this backend is switched off.
 *
 * <p>The bundle ships in the default {@code flow} feature, so a check that probed
 * {@code localhost:9428} regardless of configuration would fail on every install that has not
 * deployed VictoriaLogs.
 */
public class VictoriaLogsHealthCheckTest {

    private VictoriaLogsClient client;
    private VictoriaLogsHealthCheck healthCheck;

    @Before
    public void setUp() {
        client = mock(VictoriaLogsClient.class);
        healthCheck = new VictoriaLogsHealthCheck(client);
    }

    /** Disabled by default, so a check performed before any configuration arrives cannot fail. */
    @Test
    public void reportsNotConfiguredWhileDisabled() throws Exception {
        assertEquals(Status.Success, healthCheck.perform(null).getStatus());
        assertEquals("Not configured", healthCheck.perform(null).getMessage());
        verify(client, never()).isHealthy();
    }

    @Test
    public void checksReachabilityOnceEnabled() throws Exception {
        healthCheck.setDisabled(false);
        when(client.isHealthy()).thenReturn(true);

        assertEquals(Status.Success, healthCheck.perform(null).getStatus());
        verify(client).isHealthy();
    }

    @Test
    public void failsWhenEnabledAndUnreachable() throws Exception {
        healthCheck.setDisabled(false);
        when(client.isHealthy()).thenReturn(false);

        assertEquals(Status.Failure, healthCheck.perform(null).getStatus());
    }

    /**
     * The branch the whole client redesign was justified by, which nothing exercised.
     *
     * <p>Mockito returns null for {@code getConfigurationError()} by default, so every pre-existing
     * test here took the healthy path and deleting the misconfiguration branch left the suite green.
     */
    @Test
    public void aMisconfiguredClientIsReportedAsSuchRatherThanAsUnreachable() throws Exception {
        when(client.getConfigurationError()).thenReturn("the url must be absolute -- got: <empty>");
        healthCheck.setDisabled(false);

        assertEquals(Status.Failure, healthCheck.perform(null).getStatus());
        assertTrue(healthCheck.perform(null).getMessage(),
                healthCheck.perform(null).getMessage().contains("misconfigured"));
        assertTrue(healthCheck.perform(null).getMessage(),
                healthCheck.perform(null).getMessage().contains("absolute"));
    }

    /**
     * Query-only is a real, documented state, and the check has to see it.
     *
     * <p>With ingest off and querying on this backend answers every flow query in the UI. Gating on
     * the persistence flag alone reported "Not configured" for the component causing the problem,
     * and made the misconfiguration branch unreachable in exactly that configuration.
     */
    @Test
    public void queryOnlyModeIsNotReportedAsUnconfigured() throws Exception {
        healthCheck.setDisabled(true);          // ingest off
        healthCheck.setQueriesDisabled(false);  // queries on
        when(client.isHealthy()).thenReturn(false);

        assertEquals("a backend answering every flow query is not 'Not configured'",
                Status.Failure, healthCheck.perform(null).getStatus());
    }

    /** Both off is the default, and the one state that genuinely warrants a pass. */
    @Test
    public void bothDisabledIsReportedAsNotConfigured() throws Exception {
        healthCheck.setDisabled(true);
        healthCheck.setQueriesDisabled(true);

        assertEquals(Status.Success, healthCheck.perform(null).getStatus());
        assertEquals("Not configured", healthCheck.perform(null).getMessage());
    }
}
