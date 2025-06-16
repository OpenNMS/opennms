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
package org.opennms.netmgt.graph.api.info;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsSeverity;

public class SeverityTest {

    @Test
    public void verifyIsLessThan() {
        // Unknown
        assertThat(Severity.Unknown.isLessThan(Severity.Unknown), Matchers.is(false));
        assertThat(Severity.Unknown.isLessThan(Severity.Normal), Matchers.is(true));
        assertThat(Severity.Unknown.isLessThan(Severity.Warning), Matchers.is(true));
        assertThat(Severity.Unknown.isLessThan(Severity.Minor), Matchers.is(true));
        assertThat(Severity.Unknown.isLessThan(Severity.Major), Matchers.is(true));
        assertThat(Severity.Unknown.isLessThan(Severity.Critical), Matchers.is(true));

        // Normal
        assertThat(Severity.Normal.isLessThan(Severity.Unknown), Matchers.is(false));
        assertThat(Severity.Normal.isLessThan(Severity.Normal), Matchers.is(false));
        assertThat(Severity.Normal.isLessThan(Severity.Warning), Matchers.is(true));
        assertThat(Severity.Normal.isLessThan(Severity.Minor), Matchers.is(true));
        assertThat(Severity.Normal.isLessThan(Severity.Major), Matchers.is(true));
        assertThat(Severity.Normal.isLessThan(Severity.Critical), Matchers.is(true));

        // Warning
        assertThat(Severity.Warning.isLessThan(Severity.Unknown), Matchers.is(false));
        assertThat(Severity.Warning.isLessThan(Severity.Normal), Matchers.is(false));
        assertThat(Severity.Warning.isLessThan(Severity.Warning), Matchers.is(false));
        assertThat(Severity.Warning.isLessThan(Severity.Minor), Matchers.is(true));
        assertThat(Severity.Warning.isLessThan(Severity.Major), Matchers.is(true));
        assertThat(Severity.Warning.isLessThan(Severity.Critical), Matchers.is(true));

        // Minor
        assertThat(Severity.Minor.isLessThan(Severity.Unknown), Matchers.is(false));
        assertThat(Severity.Minor.isLessThan(Severity.Normal), Matchers.is(false));
        assertThat(Severity.Minor.isLessThan(Severity.Warning), Matchers.is(false));
        assertThat(Severity.Minor.isLessThan(Severity.Minor), Matchers.is(false));
        assertThat(Severity.Minor.isLessThan(Severity.Major), Matchers.is(true));
        assertThat(Severity.Minor.isLessThan(Severity.Critical), Matchers.is(true));

        // Major
        assertThat(Severity.Major.isLessThan(Severity.Unknown), Matchers.is(false));
        assertThat(Severity.Major.isLessThan(Severity.Normal), Matchers.is(false));
        assertThat(Severity.Major.isLessThan(Severity.Warning), Matchers.is(false));
        assertThat(Severity.Major.isLessThan(Severity.Minor), Matchers.is(false));
        assertThat(Severity.Major.isLessThan(Severity.Major), Matchers.is(false));
        assertThat(Severity.Major.isLessThan(Severity.Critical), Matchers.is(true));

        // Critical
        assertThat(Severity.Critical.isLessThan(Severity.Unknown), Matchers.is(false));
        assertThat(Severity.Critical.isLessThan(Severity.Normal), Matchers.is(false));
        assertThat(Severity.Critical.isLessThan(Severity.Warning), Matchers.is(false));
        assertThat(Severity.Critical.isLessThan(Severity.Minor), Matchers.is(false));
        assertThat(Severity.Critical.isLessThan(Severity.Major), Matchers.is(false));
        assertThat(Severity.Critical.isLessThan(Severity.Critical), Matchers.is(false));
    }

    @Test
    public void verifyCreateFrom() {
        assertThat(Severity.createFrom(OnmsSeverity.INDETERMINATE), Matchers.is(Severity.Unknown));
        assertThat(Severity.createFrom(OnmsSeverity.NORMAL), Matchers.is(Severity.Normal));
        assertThat(Severity.createFrom(OnmsSeverity.WARNING), Matchers.is(Severity.Warning));
        assertThat(Severity.createFrom(OnmsSeverity.MINOR), Matchers.is(Severity.Minor));
        assertThat(Severity.createFrom(OnmsSeverity.MAJOR), Matchers.is(Severity.Major));
        assertThat(Severity.createFrom(OnmsSeverity.CRITICAL), Matchers.is(Severity.Critical));
    }
}
