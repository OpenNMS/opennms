/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.core.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.junit.Test;

public class CentralizedDateTimeFormatTest {

    @Test
    public void shouldOutputeDateTimeIncludingTimeZone() throws IOException {
        test("yyyy-MM-dd'T'HH:mm:ssxxx", Instant.now());
    }

    @Test
    public void shouldBeResilientAgainstNull() throws IOException {
        assertNull(new CentralizedDateTimeFormat().format((Instant)null, null));
        assertNull(new CentralizedDateTimeFormat().format((Date)null, null));
    }

    @Test
    public void shouldHonorSystemSettings() throws IOException {
        String format = "yyy-MM-dd";
        System.setProperty(CentralizedDateTimeFormat.SYSTEM_PROPERTY_DATE_FORMAT, format);
        test(format, Instant.now());
        System.clearProperty(CentralizedDateTimeFormat.SYSTEM_PROPERTY_DATE_FORMAT);
    }

    public void test(String expectedPattern, Instant time) {

        String output = new CentralizedDateTimeFormat().format(time, ZoneId.systemDefault());
        assertEquals(DateTimeFormatter.ofPattern(expectedPattern).withZone(ZoneId.systemDefault()).format(time), output);
    }
}