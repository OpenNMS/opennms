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
import java.util.Map;

/**
 * A plugin as shown to the UI: the registry record plus the state derived from
 * the container at read time.
 */
public class PluginEntry {
    public static final String STATUS_INSTALLED = "installed";
    public static final String STATUS_STAGED = "staged";
    public static final String STATUS_UNLOADED = "unloaded";
    public static final String STATUS_UNMANAGED = "unmanaged";
    public static final String STATUS_UNKNOWN = "unknown";

    private String karName;
    private String fileName;
    private String sha256;
    private long size;
    private String uploadedBy;
    private String uploadedAt;
    private String unloadedBy;
    private String unloadedAt;
    private List<String> features = new ArrayList<>();
    private String bootFile;
    private boolean autoStart;
    private boolean managed;
    private boolean deployed;
    private boolean karLoaded;
    private Map<String, String> featureStates = new LinkedHashMap<>();
    private String status;
    private boolean pendingRestart;

    public String getKarName() { return karName; }
    public void setKarName(final String karName) { this.karName = karName; }
    public String getFileName() { return fileName; }
    public void setFileName(final String fileName) { this.fileName = fileName; }
    public String getSha256() { return sha256; }
    public void setSha256(final String sha256) { this.sha256 = sha256; }
    public long getSize() { return size; }
    public void setSize(final long size) { this.size = size; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(final String uploadedBy) { this.uploadedBy = uploadedBy; }
    public String getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(final String uploadedAt) { this.uploadedAt = uploadedAt; }
    public String getUnloadedBy() { return unloadedBy; }
    public void setUnloadedBy(final String unloadedBy) { this.unloadedBy = unloadedBy; }
    public String getUnloadedAt() { return unloadedAt; }
    public void setUnloadedAt(final String unloadedAt) { this.unloadedAt = unloadedAt; }
    public List<String> getFeatures() { return features; }
    public void setFeatures(final List<String> features) { this.features = features; }
    public String getBootFile() { return bootFile; }
    public void setBootFile(final String bootFile) { this.bootFile = bootFile; }
    public boolean isAutoStart() { return autoStart; }
    public void setAutoStart(final boolean autoStart) { this.autoStart = autoStart; }
    public boolean isManaged() { return managed; }
    public void setManaged(final boolean managed) { this.managed = managed; }
    public boolean isDeployed() { return deployed; }
    public void setDeployed(final boolean deployed) { this.deployed = deployed; }
    public boolean isKarLoaded() { return karLoaded; }
    public void setKarLoaded(final boolean karLoaded) { this.karLoaded = karLoaded; }
    public Map<String, String> getFeatureStates() { return featureStates; }
    public void setFeatureStates(final Map<String, String> featureStates) { this.featureStates = featureStates; }
    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }
    public boolean isPendingRestart() { return pendingRestart; }
    public void setPendingRestart(final boolean pendingRestart) { this.pendingRestart = pendingRestart; }
}
