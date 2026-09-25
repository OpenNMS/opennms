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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.web.rest.v2.plugins.KarInspection.BundleDescriptor;
import org.opennms.web.rest.v2.plugins.KarInspection.Check;
import org.opennms.web.rest.v2.plugins.KarInspection.DependencyInfo;
import org.opennms.web.rest.v2.plugins.KarInspection.FeatureInfo;
import org.opennms.web.rest.v2.plugins.KarInspection.Level;

/**
 * Compares what a KAR needs with what the running container provides.
 */
public class CompatibilityChecker {

    public static final String CHECK_CONTAINER_UNAVAILABLE = "container-unavailable";
    public static final String CHECK_IMPORT_VERSION = "import-version";
    public static final String CHECK_IMPORT_MISSING = "import-missing";
    public static final String CHECK_IMPORTS_RESOLVABLE = "imports-resolvable";
    public static final String CHECK_JAVA_VERSION = "java-version";
    public static final String CHECK_OIA_VERSION = "oia-version";

    private static final String OPENNMS_PACKAGE_PREFIX = "org.opennms.";

    private final KarafBridge bridge;

    public CompatibilityChecker(final KarafBridge bridge) {
        this.bridge = bridge;
    }

    /** Appends the compatibility checks to the inspection and returns them. */
    public List<Check> check(final KarInspection inspection) {
        final List<Check> checks = new ArrayList<>();
        if (!bridge.isAvailable()) {
            checks.add(new Check(CHECK_CONTAINER_UNAVAILABLE, Level.WARN, "The Karaf container could not be reached; package, Java and Integration API compatibility were not verified"));
            inspection.getChecks().addAll(checks);
            return checks;
        }
        checkImports(inspection, checks);
        checkJavaVersion(inspection, checks);
        checkOiaVersion(inspection, checks);
        inspection.getChecks().addAll(checks);
        return checks;
    }

    private void checkImports(final KarInspection inspection, final List<Check> checks) {
        final Map<String, List<String>> exports = bridge.frameworkExports();
        final Set<String> karExports = new HashSet<>();
        for (final BundleDescriptor bundle : inspection.getBundles()) {
            karExports.addAll(bundle.getExports());
        }
        int considered = 0;
        int problems = 0;
        for (final BundleDescriptor bundle : inspection.getBundles()) {
            final String bundleName = bundle.getSymbolicName() != null ? bundle.getSymbolicName() : bundle.getPath();
            for (final Map.Entry<String, String> imp : bundle.getImports().entrySet()) {
                final String pkg = imp.getKey();
                if (!pkg.startsWith(OPENNMS_PACKAGE_PREFIX) || karExports.contains(pkg)) {
                    continue;
                }
                considered++;
                final List<String> exported = exports.get(pkg);
                final String range = imp.getValue();
                if (exported == null || exported.isEmpty()) {
                    problems++;
                    checks.add(new Check(CHECK_IMPORT_MISSING, Level.WARN, String.format("bundle %s imports %s%s but the server does not export that package", bundleName, pkg, describeRange(range))));
                    continue;
                }
                boolean satisfied = false;
                for (final String version : exported) {
                    if (OsgiHeaders.inRange(range, version)) {
                        satisfied = true;
                        break;
                    }
                }
                if (!satisfied) {
                    problems++;
                    checks.add(new Check(CHECK_IMPORT_VERSION, Level.FAIL, String.format("bundle %s imports %s in range %s but the server exports %s", bundleName, pkg, range, String.join(", ", exported))));
                }
            }
        }
        if (problems == 0) {
            checks.add(new Check(CHECK_IMPORTS_RESOLVABLE, Level.PASS, considered == 0
                    ? "No bundle imports org.opennms.* packages from the server"
                    : considered + " org.opennms.* package imports across " + inspection.getBundles().size() + " bundles resolve against the server"));
        }
    }

    private void checkJavaVersion(final KarInspection inspection, final List<Check> checks) {
        final String running = bridge.javaVersion();
        final int runningFeature = OsgiHeaders.javaFeatureVersion(running);
        int highest = -1;
        boolean failed = false;
        for (final BundleDescriptor bundle : inspection.getBundles()) {
            final int required = OsgiHeaders.javaFeatureVersion(bundle.getRequiredJavaVersion());
            if (required < 0) {
                continue;
            }
            highest = Math.max(highest, required);
            if (runningFeature >= 0 && required > runningFeature) {
                failed = true;
                final String bundleName = bundle.getSymbolicName() != null ? bundle.getSymbolicName() : bundle.getPath();
                checks.add(new Check(CHECK_JAVA_VERSION, Level.FAIL, String.format("bundle %s requires JavaSE %s but the server runs Java %s", bundleName, bundle.getRequiredJavaVersion(), running)));
            }
        }
        if (!failed) {
            checks.add(new Check(CHECK_JAVA_VERSION, Level.PASS, highest < 0
                    ? "No bundle declares a JavaSE requirement; the server runs Java " + running
                    : "The bundles require at most JavaSE " + highest + "; the server runs Java " + running));
        }
    }

    private void checkOiaVersion(final KarInspection inspection, final List<Check> checks) {
        final String serverOia = bridge.oiaVersion();
        boolean declared = false;
        boolean warned = false;
        for (final FeatureInfo feature : inspection.getFeatures()) {
            for (final DependencyInfo dep : feature.getDependencies()) {
                if (!OsgiKarafBridge.OIA_FEATURE_NAME.equals(dep.getName())) {
                    continue;
                }
                declared = true;
                final String wanted = dep.getVersion();
                if (serverOia == null) {
                    warned = true;
                    checks.add(new Check(CHECK_OIA_VERSION, Level.WARN, String.format("feature %s depends on %s%s but the server's Integration API version could not be determined", feature.getName(), dep.getName(), describeRange(wanted))));
                } else if (wanted == null || wanted.isEmpty()) {
                    continue;
                } else if (OsgiHeaders.looksLikeRange(wanted)) {
                    if (!OsgiHeaders.inRange(wanted, serverOia)) {
                        warned = true;
                        checks.add(new Check(CHECK_OIA_VERSION, Level.WARN, String.format("feature %s depends on %s in range %s but the server provides %s", feature.getName(), dep.getName(), wanted, serverOia)));
                    }
                } else if (OsgiHeaders.majorVersion(wanted) != OsgiHeaders.majorVersion(serverOia)) {
                    warned = true;
                    checks.add(new Check(CHECK_OIA_VERSION, Level.WARN, String.format("feature %s depends on %s %s but the server provides %s (different major version)", feature.getName(), dep.getName(), wanted, serverOia)));
                }
            }
        }
        if (!warned) {
            checks.add(new Check(CHECK_OIA_VERSION, Level.PASS, declared
                    ? "The Integration API dependency matches the server's version " + serverOia
                    : "No feature declares a dependency on " + OsgiKarafBridge.OIA_FEATURE_NAME + (serverOia == null ? "" : "; the server provides " + serverOia)));
        }
    }

    private static String describeRange(final String range) {
        return range == null || range.isEmpty() ? "" : " in range " + range;
    }
}
