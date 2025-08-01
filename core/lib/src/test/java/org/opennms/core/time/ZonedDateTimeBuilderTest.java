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
package org.opennms.core.time;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;

import org.junit.Test;

/**
 * @author Seth
 */
public class ZonedDateTimeBuilderTest {

    @Test
    public void testParseTimeZone() {
        LocalDateTime time = LocalDateTime.of(2017, Month.JANUARY, 1, 1, 1, 1, 999000000);
        assertEquals(1483228861999L, ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("+01:00")).toInstant().toEpochMilli());

        assertEquals(1483228861999L + (2 * 3600 * 1000), ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("-01:00")).toInstant().toEpochMilli());
        assertEquals(1483228861999L + (1 * 3600 * 1000), ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("-00:00")).toInstant().toEpochMilli());
        assertEquals(1483228861999L + (1 * 3600 * 1000), ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("+00:00")).toInstant().toEpochMilli());
        assertEquals(1483228861999L + (1 * 3600 * 1000), ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("UTC")).toInstant().toEpochMilli());
        assertEquals(1483228861999L + (1 * 3600 * 1000), ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("Z")).toInstant().toEpochMilli());

        // Test common United States offset abbreviations
        assertEquals(1483228861999L + (6 * 3600 * 1000), ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("EST")).toInstant().toEpochMilli());
        assertEquals(1483228861999L + (5 * 3600 * 1000), ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("EDT")).toInstant().toEpochMilli());
        assertEquals(1483228861999L + (7 * 3600 * 1000), ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("CST")).toInstant().toEpochMilli());
        assertEquals(1483228861999L + (6 * 3600 * 1000), ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("CDT")).toInstant().toEpochMilli());
        assertEquals(1483228861999L + (8 * 3600 * 1000), ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("MST")).toInstant().toEpochMilli());
        assertEquals(1483228861999L + (7 * 3600 * 1000), ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("MDT")).toInstant().toEpochMilli());
        assertEquals(1483228861999L + (9 * 3600 * 1000), ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("PST")).toInstant().toEpochMilli());
        assertEquals(1483228861999L + (8 * 3600 * 1000), ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("PDT")).toInstant().toEpochMilli());

        // Test common European offset abbreviations
        assertEquals(1483228861999L + (-1 * 3600 * 1000), ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("CEST")).toInstant().toEpochMilli());
        assertEquals(1483228861999L + (0 * 3600 * 1000), ZonedDateTime.of(time, ZonedDateTimeBuilder.parseZoneId("CET")).toInstant().toEpochMilli());
    }
}
