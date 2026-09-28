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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * View of the embedded Karaf container. Every query degrades to an empty or
 * null answer when the container cannot be reached; the feature operations
 * report their failure instead, so the caller can tell the operator.
 */
public interface KarafBridge {

    /** A feature operation the container refused or could not complete. */
    class KarafOperationException extends Exception {
        private static final long serialVersionUID = 1L;

        public KarafOperationException(final String message) {
            super(message);
        }

        public KarafOperationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    boolean isAvailable();

    /** Features Karaf reports as installed, with their state (Started, Resolved, Installed, Uninstalled). */
    List<InstalledFeature> installedFeatures();

    /** Every feature of every repository the FeaturesService knows, installed or not, with its repository URL and state. */
    List<InstalledFeature> features();

    /** KAR names known to the KarService (the name is the file name without the .kar suffix). */
    List<String> installedKars();

    /** Package name to the versions exported by resolved or active bundles. */
    Map<String, List<String>> frameworkExports();

    /** java.specification.version of the running JVM, e.g. "21". */
    String javaVersion();

    /** Bundle version of the OpenNMS Integration API, or null when it is not installed. */
    String oiaVersion();

    /** Installed features whose dependency tree includes opennms-integration-api and that do not come from a product (boot) features repository. */
    List<InstalledFeature> pluginFeatures();

    /** Feature repository URIs Karaf recorded for a KAR in data/kar/&lt;karName&gt;/features.cfg; empty when the KAR is unknown. */
    List<String> karRepositories(String karName);

    void addRepository(String uri) throws KarafOperationException;

    /** Installs and starts the named features, as the extender does for boot features. */
    void installFeatures(Set<String> features) throws KarafOperationException;

    void uninstallFeatures(Set<String> features) throws KarafOperationException;

    /**
     * For every feature that is not Started: why, as far as the container can tell
     * (feature state, and for each of its bundles that is not active the resolver's
     * diagnosis or the bundle state). Started features are absent from the map.
     */
    Map<String, String> featureDiagnostics(Set<String> features);

    /**
     * Symbolic names of the installed bundles the features and their non-core
     * dependency features list; what {@link #refreshBundles} and {@link #health} act on.
     */
    Set<String> bundleNames(Set<String> features);

    /**
     * Refreshes the removal-pending revisions of the named bundles so they stop
     * serving classes; without it a reinstalled bundle can wire to a stale revision
     * of a sibling. Returns false when the refresh did not complete within the timeout.
     */
    boolean refreshBundles(Set<String> symbolicNames, java.time.Duration timeout);

    /**
     * Health of the features' bundles: every OpenNMS HealthCheck one of them
     * registered, run now, plus every bundle whose blueprint or declarative
     * container is still waiting or has failed. Empty when all is well and the
     * plugin registers no checks.
     */
    List<PluginHealth> health(Set<String> features);
}
