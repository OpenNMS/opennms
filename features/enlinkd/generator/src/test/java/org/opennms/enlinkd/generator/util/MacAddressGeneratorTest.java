/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.enlinkd.generator.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MacAddressGeneratorTest {

    @Test
    public void shouldProduceStreamOfMacAddresses() {
        MacAddressGenerator gen = new MacAddressGenerator();
        assertEquals("00:00:00:00:00:00", gen.next());
        assertEquals("00:00:00:00:00:01", gen.next());
        assertEquals("00:00:00:00:00:02", gen.next());
        assertEquals("00:00:00:00:00:03", gen.next());
        assertEquals("00:00:00:00:00:04", gen.next());
        assertEquals("00:00:00:00:00:05", gen.next());
        assertEquals("00:00:00:00:00:06", gen.next());
        assertEquals("00:00:00:00:00:07", gen.next());
        assertEquals("00:00:00:00:00:08", gen.next());
        assertEquals("00:00:00:00:00:09", gen.next());
        assertEquals("00:00:00:00:00:0a", gen.next());
        assertEquals("00:00:00:00:00:0b", gen.next());
        assertEquals("00:00:00:00:00:0c", gen.next());
        assertEquals("00:00:00:00:00:0d", gen.next());
        assertEquals("00:00:00:00:00:0e", gen.next());
        assertEquals("00:00:00:00:00:0f", gen.next());

        assertEquals("00:00:00:00:00:10", gen.next());
        assertEquals("00:00:00:00:00:11", gen.next());
        assertEquals("00:00:00:00:00:12", gen.next());
        assertEquals("00:00:00:00:00:13", gen.next());
        assertEquals("00:00:00:00:00:14", gen.next());
        assertEquals("00:00:00:00:00:15", gen.next());
        assertEquals("00:00:00:00:00:16", gen.next());
        assertEquals("00:00:00:00:00:17", gen.next());
        assertEquals("00:00:00:00:00:18", gen.next());
        assertEquals("00:00:00:00:00:19", gen.next());
        assertEquals("00:00:00:00:00:1a", gen.next());
        assertEquals("00:00:00:00:00:1b", gen.next());
        assertEquals("00:00:00:00:00:1c", gen.next());
        assertEquals("00:00:00:00:00:1d", gen.next());
        assertEquals("00:00:00:00:00:1e", gen.next());
        assertEquals("00:00:00:00:00:1f", gen.next());

    }
}
