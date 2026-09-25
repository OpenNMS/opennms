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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.web.rest.v2.plugins.KarInspection.BundleDescriptor;
import org.opennms.web.rest.v2.plugins.KarInspection.Check;
import org.opennms.web.rest.v2.plugins.KarInspection.DependencyInfo;
import org.opennms.web.rest.v2.plugins.KarInspection.FeatureInfo;
import org.opennms.web.rest.v2.plugins.KarInspection.Level;

public class CompatibilityCheckerTest {

    private FakeKarafBridge bridge;
    private CompatibilityChecker checker;
    private KarInspection inspection;

    @Before
    public void setUp() {
        bridge = new FakeKarafBridge()
                .export("org.opennms.integration.api.v1.alarms", "2.0.1")
                .export("org.opennms.integration.api.v1.model", "2.0.1")
                .export("org.opennms.core.utils", "37.0.0.SNAPSHOT");
        checker = new CompatibilityChecker(bridge);
        inspection = new KarInspection();
    }

    @Test
    public void unavailableContainerYieldsSingleWarning() {
        bridge.available = false;
        inspection.getBundles().add(bundle("org.example.plugin", "17", "org.opennms.integration.api.v1.alarms", "[1.0,2)"));

        final List<Check> checks = checker.check(inspection);

        assertEquals(1, checks.size());
        assertEquals(CompatibilityChecker.CHECK_CONTAINER_UNAVAILABLE, checks.get(0).getId());
        assertEquals(Level.WARN, checks.get(0).getLevel());
        assertEquals(checks, inspection.getChecks());
    }

    @Test
    public void importsInRangePass() {
        inspection.getBundles().add(bundle("org.example.plugin", "17",
                "org.opennms.integration.api.v1.alarms", "[2.0,3)",
                "org.opennms.core.utils", "",
                "javax.xml.parsers", ""));

        checker.check(inspection);

        final Check imports = only(CompatibilityChecker.CHECK_IMPORTS_RESOLVABLE);
        assertEquals(Level.PASS, imports.getLevel());
        assertTrue(imports.getMessage(), imports.getMessage().startsWith("2 org.opennms.* package imports"));
        assertFalse(inspection.hasLevel(Level.FAIL));
        assertFalse(inspection.hasLevel(Level.WARN));
    }

    @Test
    public void importOutsideExportedRangeFails() {
        inspection.getBundles().add(bundle("org.example.plugin", "17", "org.opennms.integration.api.v1.alarms", "[1.0,2)"));

        checker.check(inspection);

        final Check failure = only(CompatibilityChecker.CHECK_IMPORT_VERSION);
        assertEquals(Level.FAIL, failure.getLevel());
        assertEquals("bundle org.example.plugin imports org.opennms.integration.api.v1.alarms in range [1.0,2) but the server exports 2.0.1", failure.getMessage());
        assertTrue(all(CompatibilityChecker.CHECK_IMPORTS_RESOLVABLE).isEmpty());
    }

    @Test
    public void importOfUnexportedOpennmsPackageWarns() {
        inspection.getBundles().add(bundle("org.example.plugin", "17", "org.opennms.netmgt.secret", "[1.0,2)"));

        checker.check(inspection);

        final Check warning = only(CompatibilityChecker.CHECK_IMPORT_MISSING);
        assertEquals(Level.WARN, warning.getLevel());
        assertEquals("bundle org.example.plugin imports org.opennms.netmgt.secret in range [1.0,2) but the server does not export that package", warning.getMessage());
    }

    @Test
    public void packagesExportedByTheKarItselfAreSkipped() {
        final BundleDescriptor api = bundle("org.example.api", "17");
        api.getExports().add("org.opennms.example.api");
        inspection.getBundles().add(api);
        inspection.getBundles().add(bundle("org.example.impl", "17", "org.opennms.example.api", "[1.0,2)"));

        checker.check(inspection);

        assertTrue(all(CompatibilityChecker.CHECK_IMPORT_MISSING).isEmpty());
        assertEquals(Level.PASS, only(CompatibilityChecker.CHECK_IMPORTS_RESOLVABLE).getLevel());
        assertTrue(only(CompatibilityChecker.CHECK_IMPORTS_RESOLVABLE).getMessage().startsWith("No bundle imports"));
    }

    @Test
    public void unversionedBundlePathIsUsedWhenSymbolicNameIsMissing() {
        final BundleDescriptor anonymous = bundle(null, null, "org.opennms.integration.api.v1.alarms", "[1.0,2)");
        anonymous.setPath("repository/org/example/anon/1.0/anon-1.0.jar");
        inspection.getBundles().add(anonymous);

        checker.check(inspection);

        assertTrue(only(CompatibilityChecker.CHECK_IMPORT_VERSION).getMessage().startsWith("bundle repository/org/example/anon/1.0/anon-1.0.jar imports"));
    }

    @Test
    public void javaVersionAboveRunningFails() {
        bridge.javaVersion = "17";
        inspection.getBundles().add(bundle("org.example.plugin", "21"));

        checker.check(inspection);

        final Check java = only(CompatibilityChecker.CHECK_JAVA_VERSION);
        assertEquals(Level.FAIL, java.getLevel());
        assertEquals("bundle org.example.plugin requires JavaSE 21 but the server runs Java 17", java.getMessage());
    }

    @Test
    public void javaVersionAtOrBelowRunningPasses() {
        inspection.getBundles().add(bundle("org.example.plugin", "1.8"));
        inspection.getBundles().add(bundle("org.example.other", "17"));

        checker.check(inspection);

        final Check java = only(CompatibilityChecker.CHECK_JAVA_VERSION);
        assertEquals(Level.PASS, java.getLevel());
        assertEquals("The bundles require at most JavaSE 17; the server runs Java 21", java.getMessage());
    }

    @Test
    public void noJavaRequirementPasses() {
        inspection.getBundles().add(bundle("org.example.plugin", null));

        checker.check(inspection);

        assertEquals(Level.PASS, only(CompatibilityChecker.CHECK_JAVA_VERSION).getLevel());
    }

    @Test
    public void oiaRangeNotCoveringServerWarns() {
        inspection.getFeatures().add(feature("example-plugin", "opennms-integration-api", "[1.0,2)"));

        checker.check(inspection);

        final Check oia = only(CompatibilityChecker.CHECK_OIA_VERSION);
        assertEquals(Level.WARN, oia.getLevel());
        assertEquals("feature example-plugin depends on opennms-integration-api in range [1.0,2) but the server provides 2.0.1", oia.getMessage());
    }

    @Test
    public void oiaSameMajorPasses() {
        inspection.getFeatures().add(feature("example-plugin", "opennms-integration-api", "2.0.0"));

        checker.check(inspection);

        final Check oia = only(CompatibilityChecker.CHECK_OIA_VERSION);
        assertEquals(Level.PASS, oia.getLevel());
        assertEquals("The Integration API dependency matches the server's version 2.0.1", oia.getMessage());
    }

    @Test
    public void oiaDifferentMajorWarns() {
        inspection.getFeatures().add(feature("example-plugin", "opennms-integration-api", "1.4.0"));

        checker.check(inspection);

        final Check oia = only(CompatibilityChecker.CHECK_OIA_VERSION);
        assertEquals(Level.WARN, oia.getLevel());
        assertTrue(oia.getMessage(), oia.getMessage().endsWith("(different major version)"));
    }

    @Test
    public void unknownServerOiaWarns() {
        bridge.oiaVersion = null;
        inspection.getFeatures().add(feature("example-plugin", "opennms-integration-api", "2.0.0"));

        checker.check(inspection);

        assertEquals(Level.WARN, only(CompatibilityChecker.CHECK_OIA_VERSION).getLevel());
    }

    @Test
    public void noOiaDependencyPasses() {
        inspection.getFeatures().add(feature("example-plugin", "some-other-feature", null));

        checker.check(inspection);

        final Check oia = only(CompatibilityChecker.CHECK_OIA_VERSION);
        assertEquals(Level.PASS, oia.getLevel());
        assertTrue(oia.getMessage(), oia.getMessage().startsWith("No feature declares a dependency on opennms-integration-api"));
    }

    // --- fixtures --------------------------------------------------------------

    private static BundleDescriptor bundle(final String symbolicName, final String javaVersion, final String... importsAndRanges) {
        final BundleDescriptor bundle = new BundleDescriptor();
        bundle.setPath("repository/" + symbolicName + ".jar");
        bundle.setSymbolicName(symbolicName);
        bundle.setVersion("1.0.0");
        bundle.setRequiredJavaVersion(javaVersion);
        for (int i = 0; i + 1 < importsAndRanges.length; i += 2) {
            bundle.getImports().put(importsAndRanges[i], importsAndRanges[i + 1]);
        }
        return bundle;
    }

    private static FeatureInfo feature(final String name, final String dependencyName, final String dependencyVersion) {
        final FeatureInfo feature = new FeatureInfo();
        feature.setName(name);
        feature.setVersion("1.0.0");
        feature.getDependencies().add(new DependencyInfo(dependencyName, dependencyVersion, false));
        return feature;
    }

    private Check only(final String id) {
        final List<Check> matches = all(id);
        assertEquals("expected exactly one " + id + " in " + inspection.getChecks(), 1, matches.size());
        return matches.get(0);
    }

    private List<Check> all(final String id) {
        final List<Check> matches = new ArrayList<>();
        for (final Check c : inspection.getChecks()) {
            if (id.equals(c.getId())) {
                matches.add(c);
            }
        }
        return matches;
    }
}
