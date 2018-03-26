/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest.index;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

public class IndexStrategyTest {

    @Test
    public void verifyGetIndex() {
        final String[] expectedValues = new String[] { "2018", "2018-03", "2018-03-01", "2018-03-01-18" };

        // Ensure that we actually verify all strategies and don't miss one
        assertEquals(expectedValues.length, IndexStrategy.values().length);

        // Prepare test input
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2018);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 18);

        // Verify each strategy
        final IndexStrategy[] strategies = IndexStrategy.values();
        for (int i=0; i<expectedValues.length; i++) {
            final String actualValue = strategies[i].getIndex("opennms", cal.getTime());
            final String expectedValue = "opennms-" + expectedValues[i];
            assertEquals(expectedValue, actualValue);
        }
    }
}