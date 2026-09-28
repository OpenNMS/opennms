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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

class FakeKarafBridge implements KarafBridge {
    boolean available = true;
    String javaVersion = "21";
    String oiaVersion = "2.0.1";
    final Map<String, List<String>> exports = new TreeMap<>();
    final List<InstalledFeature> installedFeatures = new ArrayList<>();
    /** every feature the FeaturesService knows; {@link #installedFeatures} are added on top */
    final List<InstalledFeature> repositoryFeatures = new ArrayList<>();
    final List<String> installedKars = new ArrayList<>();
    final List<InstalledFeature> pluginFeatures = new ArrayList<>();
    /** what data/kar/&lt;name&gt;/features.cfg would list */
    final Map<String, List<String>> karRepositories = new LinkedHashMap<>();
    final List<String> addedRepositories = new ArrayList<>();
    final List<Set<String>> installCalls = new ArrayList<>();
    final List<Set<String>> uninstallCalls = new ArrayList<>();
    /** feature to reason: installing such a feature leaves it Resolved and the reason is its diagnosis */
    final Map<String, String> installFailures = new LinkedHashMap<>();
    /** when set, installFeatures throws with this message */
    String installException;
    /** when set, installFeatures blocks on it (the caller's deadline test releases it) */
    java.util.concurrent.CountDownLatch installBlock;
    int refreshCalls;
    final Set<String> refreshedBundles = new LinkedHashSet<>();
    /** what health(features) answers, in order; the last entry repeats */
    final List<List<PluginHealth>> healthAnswers = new ArrayList<>();
    int healthCalls;
    /** Runs when the compatibility checks query the container, i.e. between inspecting a KAR and writing it. */
    Runnable beforeExports;
    /** Runs before every KarService.list() answer, so a test can make a KAR appear after a few polls. */
    Runnable beforeKarList;

    FakeKarafBridge export(final String pkg, final String... versions) {
        exports.put(pkg, Arrays.asList(versions));
        return this;
    }

    FakeKarafBridge feature(final String name, final String version, final String state) {
        installedFeatures.add(new InstalledFeature(name, version, state));
        return this;
    }

    /** A feature of a repository, installed or not; the state is what the FeaturesService would report. */
    FakeKarafBridge repositoryFeature(final String repository, final String name, final String state, final String... dependencies) {
        final InstalledFeature feature = new InstalledFeature(name, "1.0.0", state, repository);
        feature.getDependencies().addAll(Arrays.asList(dependencies));
        repositoryFeatures.add(feature);
        return this;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    /** As Karaf does, lists every repository feature that is not in the Uninstalled state. */
    @Override
    public List<InstalledFeature> installedFeatures() {
        final List<InstalledFeature> result = new ArrayList<>(installedFeatures);
        for (final InstalledFeature f : repositoryFeatures) {
            if (!"Uninstalled".equals(f.getState())) {
                result.add(f);
            }
        }
        return result;
    }

    @Override
    public List<InstalledFeature> features() {
        final List<InstalledFeature> all = new ArrayList<>(repositoryFeatures);
        all.addAll(installedFeatures);
        return all;
    }

    FakeKarafBridge kar(final String karName, final String... repositories) {
        if (!installedKars.contains(karName)) {
            installedKars.add(karName);
        }
        karRepositories.put(karName, new ArrayList<>(Arrays.asList(repositories)));
        return this;
    }

    /** The state the FeaturesService would report for a feature name, Uninstalled when it knows no such feature. */
    String state(final String name) {
        String state = null;
        for (final InstalledFeature f : features()) {
            if (name.equals(f.getName())) {
                if (f.isStarted()) {
                    return InstalledFeature.STATE_STARTED;
                }
                state = f.getState();
            }
        }
        return state == null ? "Uninstalled" : state;
    }

    private void setState(final String name, final String state) {
        boolean known = false;
        for (final InstalledFeature f : features()) {
            if (name.equals(f.getName())) {
                f.setState(state);
                known = true;
            }
        }
        if (!known) {
            installedFeatures.add(new InstalledFeature(name, "1.0.0", state));
        }
    }

    @Override
    public List<String> installedKars() {
        if (beforeKarList != null) {
            beforeKarList.run();
        }
        return installedKars;
    }

    @Override
    public List<String> karRepositories(final String karName) {
        return karRepositories.getOrDefault(karName, new ArrayList<>());
    }

    @Override
    public void addRepository(final String uri) throws KarafOperationException {
        if (!available) {
            throw new KarafOperationException("container unavailable");
        }
        if (!addedRepositories.contains(uri)) {
            addedRepositories.add(uri);
        }
    }

    @Override
    public void installFeatures(final Set<String> features) throws KarafOperationException {
        installCalls.add(new LinkedHashSet<>(features));
        if (installBlock != null) {
            try {
                installBlock.await();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (installException != null) {
            throw new KarafOperationException(installException);
        }
        for (final String name : features) {
            setState(name, installFailures.containsKey(name) ? "Resolved" : InstalledFeature.STATE_STARTED);
        }
    }

    @Override
    public void uninstallFeatures(final Set<String> features) throws KarafOperationException {
        uninstallCalls.add(new LinkedHashSet<>(features));
        for (final String name : features) {
            if ("Uninstalled".equals(state(name))) {
                throw new KarafOperationException("Feature named '" + name + "' is not installed");
            }
            installedFeatures.removeIf(f -> name.equals(f.getName()));
            for (final InstalledFeature f : repositoryFeatures) {
                if (name.equals(f.getName())) {
                    f.setState("Uninstalled");
                }
            }
        }
    }

    @Override
    public Set<String> bundleNames(final Set<String> features) {
        final Set<String> names = new LinkedHashSet<>();
        for (final String feature : features) {
            names.add("org.example." + feature);
        }
        return names;
    }

    @Override
    public boolean refreshBundles(final Set<String> symbolicNames, final java.time.Duration timeout) {
        refreshCalls++;
        refreshedBundles.addAll(symbolicNames);
        return true;
    }

    @Override
    public List<PluginHealth> health(final Set<String> features) {
        healthCalls++;
        if (healthAnswers.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(healthAnswers.get(Math.min(healthCalls, healthAnswers.size()) - 1));
    }

    @Override
    public Map<String, String> featureDiagnostics(final Set<String> features) {
        final Map<String, String> result = new LinkedHashMap<>();
        for (final String name : features) {
            final String state = state(name);
            if (!InstalledFeature.STATE_STARTED.equals(state)) {
                result.put(name, installFailures.getOrDefault(name, "Feature state: " + state));
            }
        }
        return result;
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
