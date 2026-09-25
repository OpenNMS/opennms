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
import java.util.List;

/** Response of install and unload: the affected plugin plus what to do next. */
public class PluginActionResult {
    private PluginEntry plugin;
    private boolean restartRequired;
    private RestartInstructions restartInstructions;
    private List<KarInspection.Check> checks = new ArrayList<>();
    /** featuresBoot.d files (relative to OPENNMS_HOME) edited or deleted by an unload. */
    private List<String> bootFilesRemoved = new ArrayList<>();

    public PluginEntry getPlugin() { return plugin; }
    public void setPlugin(final PluginEntry plugin) { this.plugin = plugin; }
    public boolean isRestartRequired() { return restartRequired; }
    public void setRestartRequired(final boolean restartRequired) { this.restartRequired = restartRequired; }
    public RestartInstructions getRestartInstructions() { return restartInstructions; }
    public void setRestartInstructions(final RestartInstructions restartInstructions) { this.restartInstructions = restartInstructions; }
    public List<KarInspection.Check> getChecks() { return checks; }
    public void setChecks(final List<KarInspection.Check> checks) { this.checks = checks; }
    public List<String> getBootFilesRemoved() { return bootFilesRemoved; }
    public void setBootFilesRemoved(final List<String> bootFilesRemoved) { this.bootFilesRemoved = bootFilesRemoved; }
}
