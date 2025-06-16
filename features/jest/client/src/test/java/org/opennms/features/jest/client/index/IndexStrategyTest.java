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
package org.opennms.features.jest.client.index;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;
import org.opennms.features.jest.client.template.IndexSettings;

public class IndexStrategyTest {

    IndexSettings indexSettings = new IndexSettings();

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
            final String actualValue = strategies[i].getIndex(indexSettings,"opennms", cal.toInstant());
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
        assertEquals("opennms-2018-03-28", IndexStrategy.DAILY.getIndex(indexSettings,"opennms", instant));
        assertEquals("opennms-2018-03-28-02", IndexStrategy.HOURLY.getIndex(indexSettings,"opennms", instant));
    }
}