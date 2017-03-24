/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
