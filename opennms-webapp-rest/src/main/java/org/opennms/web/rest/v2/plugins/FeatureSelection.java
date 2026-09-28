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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.opennms.web.rest.v2.plugins.KarInspection.Check;
import org.opennms.web.rest.v2.plugins.KarInspection.Level;

/**
 * Decides which of a KAR's top-level features the page pre-selects for the boot
 * file: the catalog's list when the KAR came from a catalog entry, otherwise the
 * single top-level feature or the one named after the KAR.
 */
final class FeatureSelection {

    static final String CHECK_BOOT_FEATURES = "boot-features";
    private static final List<String> NAME_SUFFIXES = Arrays.asList("-plugin", "-standalone");

    private FeatureSelection() {
    }

    /**
     * Stores the suggestion on the inspection and adds a WARN check when a catalog
     * feature is not declared by the KAR.
     */
    static List<String> suggest(final KarInspection inspection, final List<String> catalogBootFeatures) {
        final List<String> topLevel = inspection.topLevelFeatureNames();
        final List<String> suggested = new ArrayList<>();
        if (catalogBootFeatures != null && !catalogBootFeatures.isEmpty()) {
            final List<String> missing = new ArrayList<>();
            for (final String feature : catalogBootFeatures) {
                if (topLevel.contains(feature)) {
                    suggested.add(feature);
                } else {
                    missing.add(feature);
                }
            }
            if (!missing.isEmpty()) {
                inspection.getChecks().add(new Check(CHECK_BOOT_FEATURES, Level.WARN,
                        "The catalog expects these features, which this KAR does not declare as top-level features: " + String.join(", ", missing)
                                + (topLevel.isEmpty() ? "" : "; it declares " + String.join(", ", topLevel))));
            }
        }
        if (suggested.isEmpty()) {
            suggested.addAll(byName(inspection.getKarName(), topLevel));
        }
        inspection.setSuggestedFeatures(suggested);
        return suggested;
    }

    private static List<String> byName(final String karName, final List<String> topLevel) {
        final List<String> result = new ArrayList<>();
        if (topLevel.size() == 1) {
            result.add(topLevel.get(0));
            return result;
        }
        if (karName == null) {
            return result;
        }
        final Set<String> candidates = new LinkedHashSet<>();
        candidates.add(karName);
        for (final String suffix : NAME_SUFFIXES) {
            candidates.add(karName + suffix);
            if (karName.endsWith(suffix)) {
                candidates.add(karName.substring(0, karName.length() - suffix.length()));
            }
        }
        for (final String feature : topLevel) {
            if (candidates.contains(feature)) {
                result.add(feature);
                return result;
            }
        }
        return result;
    }
}
