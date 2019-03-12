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
        assertEquals("000000000001", gen.next());
        assertEquals("000000000002", gen.next());
        assertEquals("000000000003", gen.next());
        assertEquals("000000000004", gen.next());
        assertEquals("000000000005", gen.next());
        assertEquals("000000000006", gen.next());
        assertEquals("000000000007", gen.next());
        assertEquals("000000000008", gen.next());
        assertEquals("000000000009", gen.next());
        assertEquals("00000000000a", gen.next());
        assertEquals("00000000000b", gen.next());
        assertEquals("00000000000c", gen.next());
        assertEquals("00000000000d", gen.next());
        assertEquals("00000000000e", gen.next());
        assertEquals("00000000000f", gen.next());

        assertEquals("000000000010", gen.next());
        assertEquals("000000000011", gen.next());
        assertEquals("000000000012", gen.next());
        assertEquals("000000000013", gen.next());
        assertEquals("000000000014", gen.next());
        assertEquals("000000000015", gen.next());
        assertEquals("000000000016", gen.next());
        assertEquals("000000000017", gen.next());
        assertEquals("000000000018", gen.next());
        assertEquals("000000000019", gen.next());
        assertEquals("00000000001a", gen.next());
        assertEquals("00000000001b", gen.next());
        assertEquals("00000000001c", gen.next());
        assertEquals("00000000001d", gen.next());
        assertEquals("00000000001e", gen.next());
        assertEquals("00000000001f", gen.next());

    }
}
