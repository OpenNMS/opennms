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

public class RangedValueTest {
    @Test
    public void verifyRangedValues() {
        // Verify simple range
        RangedValue value = new RangedValue("80-100");
        RangedValue value2 = new RangedValue(new StringValue("80-100"));
        RangedValue value3 = new RangedValue(80, 100);
        for (int i=80; i<=100; i++) {
            assertThat(value.isInRange(i), is(true));
            assertThat(value2.isInRange(i), is(true));
            assertThat(value3.isInRange(i), is(true));
        }
        for (int i=-1000; i<=1000; i++) {
            if (i >= 80 && i <= 100) continue; // skip for in range
            assertThat(value.isInRange(i), is(false));
            assertThat(value2.isInRange(i), is(false));
            assertThat(value3.isInRange(i), is(false));
        }

        // Verify if single value
        RangedValue singleValue = new RangedValue("80");
        assertThat(singleValue.isInRange(80), is(true));
        assertThat(singleValue.isInRange(79), is(false));
        assertThat(singleValue.isInRange(81), is(false));

        // Verify multi range
        RangedValue rangedValue = new RangedValue("80-100-200");
        for(int i=80; i<=100; i++) {
            assertThat(rangedValue.isInRange(i), is(true));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyNullRange() {
        new RangedValue((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyEmptyRange() {
        new RangedValue("");
    }
}
