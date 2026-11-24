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
package org.opennms.smoketest.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class KarafShellTest {
    private static final String FEATURE = "opennms-plugins-example";
    private static final String HIGHLIGHTED_FEATURE = "\u001B[31;1m" + FEATURE + "\u001B[0m";

    private static String row(String name, String version, String state) {
        return String.join("\t", name, version, "x", state, HIGHLIGHTED_FEATURE + "-features", "Example") + "\n";
    }

    @Test
    public void findsTheStateOfTheFeature() {
        final String output = "admin@opennms()> feature:list --no-format | grep " + FEATURE + "\n"
                + "\u001B[?1l\u001B>\u001B[?2004l" + row(HIGHLIGHTED_FEATURE, "1.0.0", "Started");

        assertThat(KarafShell.hasFeatureState(output, FEATURE, "Started"), is(true));
        assertThat(KarafShell.hasFeatureState(output, FEATURE, "Uninstalled"), is(false));
    }

    @Test
    public void ignoresOtherFeaturesFromTheSameRepository() {
        final String output = row(HIGHLIGHTED_FEATURE, "1.0.0", "Uninstalled")
                + row("example-dependency", "2.0.0", "Started");

        assertThat(KarafShell.hasFeatureState(output, FEATURE, "Started"), is(false));
    }

    @Test
    public void ignoresTheStateWordInOtherColumns() {
        final String output = String.join("\t", HIGHLIGHTED_FEATURE, "1.0.0", "x", "Uninstalled", "Started", "Started");

        assertThat(KarafShell.hasFeatureState(output, FEATURE, "Started"), is(false));
    }
}
