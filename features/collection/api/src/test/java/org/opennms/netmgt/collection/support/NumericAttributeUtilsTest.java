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
package org.opennms.netmgt.collection.support;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NumericAttributeUtilsTest {

    private static final double delta = 0.00001;

    @Test
    public void canParseNumericValuesFromStrings() {
        // Standard Numeric Value
        assertEquals(4.6, NumericAttributeUtils.parseNumericValue("4.6"), delta);

        // Percentage Value
        assertEquals(4.6, NumericAttributeUtils.parseNumericValue("4.6%"), delta);

        // Value with Units - Model 1
        assertEquals(4.6, NumericAttributeUtils.parseNumericValue("4.6Bps"), delta);

        // Value with Units - Model 2
        assertEquals(4.6, NumericAttributeUtils.parseNumericValue("4.6 bps"), delta);

        // Negative value
        assertEquals(-42, NumericAttributeUtils.parseNumericValue("-42"), delta);

        // Negative Value with Units
        assertEquals(-32, NumericAttributeUtils.parseNumericValue("-32 celcius"), delta);

        // Value in scientific notation - Model 1
        assertEquals(420.0, NumericAttributeUtils.parseNumericValue("4.2E2"), delta);

        // Value in scientific notation - Model 2
        assertEquals(-0.04, NumericAttributeUtils.parseNumericValue("-4e-2"), delta);
    }
}
