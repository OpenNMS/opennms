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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class FakeKarafBridge implements KarafBridge {
    boolean available = true;
    String javaVersion = "21";
    String oiaVersion = "2.0.1";
    final Map<String, List<String>> exports = new TreeMap<>();
    final List<InstalledFeature> installedFeatures = new ArrayList<>();
    final List<String> installedKars = new ArrayList<>();
    final List<InstalledFeature> pluginFeatures = new ArrayList<>();
    /** Runs when the compatibility checks query the container, i.e. between inspecting a KAR and writing it. */
    Runnable beforeExports;

    FakeKarafBridge export(final String pkg, final String... versions) {
        exports.put(pkg, Arrays.asList(versions));
        return this;
    }

    FakeKarafBridge feature(final String name, final String version, final String state) {
        installedFeatures.add(new InstalledFeature(name, version, state));
        return this;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public List<InstalledFeature> installedFeatures() {
        return installedFeatures;
    }

    @Override
    public List<String> installedKars() {
        return installedKars;
    }

    @Override
    public Map<String, List<String>> frameworkExports() {
        if (beforeExports != null) {
            beforeExports.run();
        }
        return exports;
    }

    @Override
    public String javaVersion() {
        return javaVersion;
    }

    @Override
    public String oiaVersion() {
        return oiaVersion;
    }

    @Override
    public List<InstalledFeature> pluginFeatures() {
        return pluginFeatures;
    }
}
