/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
