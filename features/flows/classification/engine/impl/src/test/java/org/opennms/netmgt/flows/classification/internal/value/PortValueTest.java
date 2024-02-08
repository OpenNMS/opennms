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
package org.opennms.netmgt.flows.classification.internal.value;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PortValueTest {

    @Test
    public void verifySingleValue() {
        final PortValue portValue = PortValue.of("5");
        assertThat(portValue.matches(5), is(true));
        assertThat(portValue.matches(1), is(false));
    }

    @Test
    public void verifyMultipleValues() {
        final PortValue portValue = PortValue.of("1,2,3");
        assertThat(portValue.matches(1), is(true));
        assertThat(portValue.matches(2), is(true));
        assertThat(portValue.matches(3), is(true));
        assertThat(portValue.matches(4), is(false));
        assertThat(portValue.matches(5), is(false));
    }

    @Test
    public void verifyRange() {
        final PortValue portValue = PortValue.of("10-13");
        assertThat(portValue.matches(10), is(true));
        assertThat(portValue.matches(11), is(true));
        assertThat(portValue.matches(12), is(true));
        assertThat(portValue.matches(13), is(true));
        assertThat(portValue.matches(1), is(false));
        assertThat(portValue.matches(2), is(false));
        assertThat(portValue.matches(3), is(false));
    }
}
