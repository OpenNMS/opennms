/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.events.api;

import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.test.LocaleProviderUtils;

public class LegacyDatetimeFormatterTest {
    private TimeZone oldZone;

    @BeforeClass
    public static void beforeClass() throws Exception {
        LocaleProviderUtils.compat();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        LocaleProviderUtils.reset();
    }

    @Before
    public void setUp() {
        oldZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Etc/UTC")));
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(oldZone);
    }

    @Test
    public void testFormat() throws Exception {
        final LegacyDatetimeFormatter formatter = new LegacyDatetimeFormatter();
        final String date = formatter.format(new Date(0));
        assertEquals("1970-01-01T00:00:00Z", date);
    }

    @Test
    public void testParse() throws Exception {
        final LegacyDatetimeFormatter formatter = new LegacyDatetimeFormatter();

        // "default" format (DateFormat.FULL + DateFormat.LONG)
        Date date = formatter.parse("Thursday, January 1, 1970 12:00:03 AM GMT");
        assertEquals(new Date(3000), date);

        // "custom" format (old XML format)
        date = formatter.parse("Thursday, 1 January 1970 00:00:03 o'clock GMT");
        assertEquals(new Date(3000), date);

        // "full" format
        date = formatter.parse("Thursday, January 1, 1970 12:00:03 AM GMT");
        assertEquals(new Date(3000), date);

        date = formatter.parse("1970-01-01T00:00:03+00:00");
        assertEquals(new Date(3000), date);
    }
}

