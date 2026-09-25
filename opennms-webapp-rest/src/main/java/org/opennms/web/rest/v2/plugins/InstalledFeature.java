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

public class InstalledFeature {
    public static final String STATE_STARTED = "Started";

    private String name;
    private String version;
    private String state;
    private List<String> dependencies = new ArrayList<>();

    public InstalledFeature() {
    }

    public InstalledFeature(final String name, final String version, final String state) {
        this.name = name;
        this.version = version;
        this.state = state;
    }

    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }
    public String getVersion() { return version; }
    public void setVersion(final String version) { this.version = version; }
    public String getState() { return state; }
    public void setState(final String state) { this.state = state; }
    public List<String> getDependencies() { return dependencies; }
    public void setDependencies(final List<String> dependencies) { this.dependencies = dependencies; }

    public boolean isStarted() {
        return STATE_STARTED.equals(state);
    }

    @Override
    public String toString() {
        return name + "/" + version + " (" + state + ")";
    }
}
