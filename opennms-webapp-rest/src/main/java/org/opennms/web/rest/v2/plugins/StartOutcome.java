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
package org.opennms.web.rest.v2.plugins;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

/** What happened to a plugin's features when the page tried to start them in the running container. */
public class StartOutcome {
    /** every chosen feature is Started */
    public static final String STARTED = "started";
    /** the container refused the install or a feature did not reach Started */
    public static final String FAILED = "failed";
    /** the container never reported the KAR */
    public static final String TIMEOUT = "timeout";
    /** the container cannot be reached from the web application */
    public static final String UNAVAILABLE = "unavailable";
    /** the catalog says this plugin needs a server restart, so no live start was attempted */
    public static final String RESTART_REQUIRED = "restart-required";

    private String state;
    private String message;
    private Map<String, String> diagnostics = new LinkedHashMap<>();
    /** false when a feature is Started but a health check of its bundles is not passing */
    private boolean healthy = true;
    private List<PluginHealth> health = new ArrayList<>();

    public StartOutcome() {
    }

    public StartOutcome(final String state, final String message) {
        this.state = state;
        this.message = message;
    }

    public static StartOutcome started(final String message) {
        return new StartOutcome(STARTED, message);
    }

    public static StartOutcome failed(final String message, final Map<String, String> diagnostics) {
        final StartOutcome outcome = new StartOutcome(FAILED, message);
        if (diagnostics != null) {
            outcome.diagnostics.putAll(diagnostics);
        }
        return outcome;
    }

    public boolean isStarted() {
        return STARTED.equals(state);
    }

    /** Started and every health signal passing. */
    public boolean isRunningWell() {
        return isStarted() && healthy;
    }

    /** Records the health signals; unhealthy ones extend the message so logs and events carry them. */
    public void recordHealth(final List<PluginHealth> signals) {
        health = new ArrayList<>(signals);
        healthy = signals.stream().allMatch(PluginHealth::isSuccess);
        if (!healthy) {
            message = (message == null ? "" : message + " ") + "Health checks not passing: " + healthSummary();
        }
    }

    /** The failing signals, "; "-joined; empty when healthy. */
    public String healthSummary() {
        return health.stream().filter(h -> !h.isSuccess()).map(PluginHealth::summary).collect(Collectors.joining("; "));
    }

    public boolean isHealthy() { return healthy; }
    public void setHealthy(final boolean healthy) { this.healthy = healthy; }
    public List<PluginHealth> getHealth() { return health; }
    public void setHealth(final List<PluginHealth> health) { this.health = health; }

    public String getState() { return state; }
    public void setState(final String state) { this.state = state; }
    public String getMessage() { return message; }
    public void setMessage(final String message) { this.message = message; }
    public Map<String, String> getDiagnostics() { return diagnostics; }
    public void setDiagnostics(final Map<String, String> diagnostics) { this.diagnostics = diagnostics; }
}
