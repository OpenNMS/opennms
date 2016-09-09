/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
