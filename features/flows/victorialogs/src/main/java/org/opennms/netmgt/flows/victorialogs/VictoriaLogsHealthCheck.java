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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;

/**
 * Reports whether VictoriaLogs is reachable, so {@code opennms:health-check} can see it.
 *
 * <p><strong>Silent while the backend is switched off.</strong> This bundle ships in the default
 * {@code flow} feature but persistence to VictoriaLogs is disabled unless someone turns it on, and
 * the URL then still points at {@code localhost:9428}. Checking reachability regardless would report
 * a failure on every install that has not deployed VictoriaLogs — turning an opt-in backend into a
 * permanent health-check failure for people not using it. So when disabled the check reports success
 * with "Not configured", the same accommodation {@code RequireConfigurationElasticHealthCheck} makes
 * for Elasticsearch.
 */
public class VictoriaLogsHealthCheck implements HealthCheck {

    private final VictoriaLogsClient client;

    /** Mirrors {@code skipVictoriaLogsPersistence}; see the class javadoc. */
    private volatile boolean disabled = true;

    /**
     * Mirrors {@code skipVictoriaLogsQueries}.
     *
     * <p>Separate from {@link #disabled} because the two are opted into separately, and the state
     * that matters most is the asymmetric one: with ingest off and querying on, this backend answers
     * every flow query in the UI while contributing nothing to storage. Reporting on the ingest flag
     * alone called that "Not configured".
     */
    private volatile boolean queriesDisabled = true;

    public VictoriaLogsHealthCheck(final VictoriaLogsClient client) {
        this.client = Objects.requireNonNull(client);
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setQueriesDisabled(final boolean queriesDisabled) {
        this.queriesDisabled = queriesDisabled;
    }

    public void setDisabled(final boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public String getDescription() {
        return "Connecting to VictoriaLogs (Flows)";
    }

    @Override
    public List<String> getTags() {
        return Collections.singletonList("flows");
    }

    @Override
    public Response perform(final Context context) {
        // Both flags, not just persistence. Querying is opted into separately, so the state
        // "ingest off, queries on" is reachable and normal -- and in it this backend answers every
        // flow query in the UI. Reporting "Not configured" there declared success for the component
        // causing the problem, and made the misconfiguration branch below unreachable in exactly
        // the configuration where it matters most.
        if (disabled && queriesDisabled) {
            return new Response(Status.Success, "Not configured");
        }
        // A misconfiguration is reported as itself rather than as unreachability. The two need
        // different things looked at, and this check is the one place an operator goes to find out
        // which -- previously a bad url failed the whole blueprint container, so this check was
        // never registered and said nothing at all.
        final String misconfigured = client.getConfigurationError();
        if (misconfigured != null) {
            return new Response(Status.Failure, "VictoriaLogs is misconfigured: " + misconfigured);
        }
        return client.isHealthy()
                ? Response.SUCCESS
                : new Response(Status.Failure, "VictoriaLogs is not reachable");
    }
}
