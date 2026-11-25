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

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates the real bridge on first use. Test contexts that load this module's
 * beans without the OSGi framework jars on the classpath then get an
 * unavailable container instead of a failed bean.
 */
class LazyKarafBridge implements KarafBridge {
    private static final Logger LOG = LoggerFactory.getLogger(LazyKarafBridge.class);

    private final Supplier<KarafBridge> factory;
    private volatile KarafBridge delegate;
    private volatile boolean unavailable;

    LazyKarafBridge(final Supplier<KarafBridge> factory) {
        this.factory = factory;
    }

    private KarafBridge delegate() {
        KarafBridge bridge = delegate;
        if (bridge != null || unavailable) {
            return bridge;
        }
        synchronized (this) {
            if (delegate == null && !unavailable) {
                try {
                    delegate = factory.get();
                } catch (final LinkageError e) {
                    LOG.warn("The Karaf container classes are not on the class path; plugin management is unavailable: {}", e.toString());
                    unavailable = true;
                }
            }
            return delegate;
        }
    }

    private KarafBridge required() throws KarafOperationException {
        final KarafBridge bridge = delegate();
        if (bridge == null) {
            throw new KarafOperationException("The Karaf container is not available.");
        }
        return bridge;
    }

    @Override
    public boolean isAvailable() {
        final KarafBridge bridge = delegate();
        return bridge != null && bridge.isAvailable();
    }

    @Override
    public List<InstalledFeature> installedFeatures() {
        final KarafBridge bridge = delegate();
        return bridge == null ? Collections.emptyList() : bridge.installedFeatures();
    }

    @Override
    public List<InstalledFeature> features() {
        final KarafBridge bridge = delegate();
        return bridge == null ? Collections.emptyList() : bridge.features();
    }

    @Override
    public List<String> installedKars() {
        final KarafBridge bridge = delegate();
        return bridge == null ? Collections.emptyList() : bridge.installedKars();
    }

    @Override
    public Map<String, List<String>> frameworkExports() {
        final KarafBridge bridge = delegate();
        return bridge == null ? Collections.emptyMap() : bridge.frameworkExports();
    }

    @Override
    public String javaVersion() {
        final KarafBridge bridge = delegate();
        return bridge == null ? System.getProperty("java.specification.version") : bridge.javaVersion();
    }

    @Override
    public String oiaVersion() {
        final KarafBridge bridge = delegate();
        return bridge == null ? null : bridge.oiaVersion();
    }

    @Override
    public List<InstalledFeature> pluginFeatures() {
        final KarafBridge bridge = delegate();
        return bridge == null ? Collections.emptyList() : bridge.pluginFeatures();
    }

    @Override
    public List<String> karRepositories(final String karName) {
        final KarafBridge bridge = delegate();
        return bridge == null ? Collections.emptyList() : bridge.karRepositories(karName);
    }

    @Override
    public void addRepository(final String uri) throws KarafOperationException {
        required().addRepository(uri);
    }

    @Override
    public void installFeatures(final Set<String> features) throws KarafOperationException {
        required().installFeatures(features);
    }

    @Override
    public void uninstallFeatures(final Set<String> features) throws KarafOperationException {
        required().uninstallFeatures(features);
    }

    @Override
    public Map<String, String> featureDiagnostics(final Set<String> features) {
        final KarafBridge bridge = delegate();
        return bridge == null ? Collections.emptyMap() : bridge.featureDiagnostics(features);
    }

    @Override
    public Set<String> bundleNames(final Set<String> features) {
        final KarafBridge bridge = delegate();
        return bridge == null ? Collections.emptySet() : bridge.bundleNames(features);
    }

    @Override
    public boolean refreshBundles(final Set<String> symbolicNames, final Duration timeout) {
        final KarafBridge bridge = delegate();
        return bridge != null && bridge.refreshBundles(symbolicNames, timeout);
    }

    @Override
    public List<PluginHealth> health(final Set<String> features) {
        final KarafBridge bridge = delegate();
        return bridge == null ? Collections.emptyList() : bridge.health(features);
    }
}
