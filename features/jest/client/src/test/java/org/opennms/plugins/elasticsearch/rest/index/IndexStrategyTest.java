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

import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;

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
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Verify each strategy
        final IndexStrategy[] strategies = IndexStrategy.values();
        for (int i=0; i<expectedValues.length; i++) {
            final String actualValue = strategies[i].getIndex("opennms", cal.toInstant());
            final String expectedValue = "opennms-" + expectedValues[i];
            assertEquals(expectedValue, actualValue);
        }
    }

    // See HZN-1278
    @Test
    public void verifyIsUsingUTC() {
        // First set time zone to EST, to ensure the IndexStrategy is using UTC instead of local time
        TimeZone.setDefault(TimeZone.getTimeZone("EST"));

        // Set date to "Wednesday, March 28, 2018 2:55:05 AM UTC"
        // This is "Tuesday March 27, 2018 21:55:05 EST"
        final Instant instant = Instant.ofEpochMilli(1522205705000L);
        assertEquals("opennms-2018-03-28", IndexStrategy.DAILY.getIndex("opennms", instant));
        assertEquals("opennms-2018-03-28-02", IndexStrategy.HOURLY.getIndex("opennms", instant));
    }
}