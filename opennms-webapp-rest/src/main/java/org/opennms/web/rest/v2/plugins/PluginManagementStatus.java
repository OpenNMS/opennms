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

public class PluginManagementStatus {
    private boolean containerAvailable;
    private String opennmsHome;
    private String deployDir;
    private String javaVersion;
    private String oiaVersion;
    private boolean restartRequired;
    private List<PluginEntry> plugins = new ArrayList<>();
    private List<InstalledFeature> pluginFeatures = new ArrayList<>();
    private RestartInstructions restartInstructions;
    private String tempDir;
    private long tempBytes;
    private int tempFiles;

    public boolean isContainerAvailable() { return containerAvailable; }
    public void setContainerAvailable(final boolean containerAvailable) { this.containerAvailable = containerAvailable; }
    public String getOpennmsHome() { return opennmsHome; }
    public void setOpennmsHome(final String opennmsHome) { this.opennmsHome = opennmsHome; }
    public String getDeployDir() { return deployDir; }
    public void setDeployDir(final String deployDir) { this.deployDir = deployDir; }
    public String getJavaVersion() { return javaVersion; }
    public void setJavaVersion(final String javaVersion) { this.javaVersion = javaVersion; }
    public String getOiaVersion() { return oiaVersion; }
    public void setOiaVersion(final String oiaVersion) { this.oiaVersion = oiaVersion; }
    public boolean isRestartRequired() { return restartRequired; }
    public void setRestartRequired(final boolean restartRequired) { this.restartRequired = restartRequired; }
    public List<PluginEntry> getPlugins() { return plugins; }
    public void setPlugins(final List<PluginEntry> plugins) { this.plugins = plugins; }
    public List<InstalledFeature> getPluginFeatures() { return pluginFeatures; }
    public void setPluginFeatures(final List<InstalledFeature> pluginFeatures) { this.pluginFeatures = pluginFeatures; }
    public RestartInstructions getRestartInstructions() { return restartInstructions; }
    public void setRestartInstructions(final RestartInstructions restartInstructions) { this.restartInstructions = restartInstructions; }
    public String getTempDir() { return tempDir; }
    public void setTempDir(final String tempDir) { this.tempDir = tempDir; }
    public long getTempBytes() { return tempBytes; }
    public void setTempBytes(final long tempBytes) { this.tempBytes = tempBytes; }
    public int getTempFiles() { return tempFiles; }
    public void setTempFiles(final int tempFiles) { this.tempFiles = tempFiles; }
}
