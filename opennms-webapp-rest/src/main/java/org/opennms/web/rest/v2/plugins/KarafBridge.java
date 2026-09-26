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

/**
 * Read-only view of the embedded Karaf container. Every method degrades to an
 * empty or null answer when the container cannot be reached.
 */
public interface KarafBridge {

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
}
