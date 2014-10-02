/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.accesspointmonitor.poller;

import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.snmp.mock.MockSnmpValueFactory;

import junit.framework.TestCase;

public class TableStrategyTest extends TestCase {

    public void testPhydAddrFromValue() {
        SnmpValueFactory valueFactory = new MockSnmpValueFactory();

        final byte[] ZERO_MAC = new byte[] { 0, 0, 0, 0, 0, 0 };
        SnmpValue value = valueFactory.getOctetString(ZERO_MAC);
        assertEquals("00:00:00:00:00:00", TableStrategy.getPhysAddrFromValue(value));

        final byte[] RANDOM_MAC = new byte[] { 11, 22, 33, 44, (byte) 255, 66 };
        value = valueFactory.getOctetString(RANDOM_MAC);
        assertEquals("0B:16:21:2C:FF:42", TableStrategy.getPhysAddrFromValue(value));

        final byte[] NOT_A_MAC = new byte[] { 11, 22, 33, 44, 55 };
        value = valueFactory.getOctetString(NOT_A_MAC);
        assertEquals(null, TableStrategy.getPhysAddrFromValue(value));
    }
}
