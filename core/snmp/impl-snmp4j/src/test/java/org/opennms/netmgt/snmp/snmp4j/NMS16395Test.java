/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2025 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2025 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.snmp.snmp4j;

import static org.junit.Assert.assertEquals;

import java.util.function.Function;

import org.junit.Test;
import org.opennms.netmgt.snmp.AbstractSnmpValue;
import org.snmp4j.smi.OctetString;

public class NMS16395Test {
    private final static Function<String, AbstractSnmpValue> FUNCTION = string -> new Snmp4JValue(new OctetString(string));

    @Test
    public void testWithoutAdditionalPrintableCharacters() {
        System.setProperty(AbstractSnmpValue.ADDITIONAL_PRINTABLE_CHARACTERS_PROPERTY, "");
        AbstractSnmpValue.invalidateAdditionalCharacters();
        assertEquals("A.B.C.D.E.F.", FUNCTION.apply("A\u0011B\u0011C\nD\u0011E\u0011F\n").toDisplayString());
    }

    @Test
    public void testAdditionalPrintableCharacters() {
        System.setProperty(AbstractSnmpValue.ADDITIONAL_PRINTABLE_CHARACTERS_PROPERTY, "0x0a,0x11");
        AbstractSnmpValue.invalidateAdditionalCharacters();
        assertEquals("A\u0011B\u0011C\nD\u0011E\u0011F\n", FUNCTION.apply("A\u0011B\u0011C\nD\u0011E\u0011F\n").toDisplayString());
    }

    @Test
    public void testMappedCharacters() {
        System.setProperty(AbstractSnmpValue.ADDITIONAL_PRINTABLE_CHARACTERS_PROPERTY, "0x0a:0x20,0x11,0x45:0x58");
        AbstractSnmpValue.invalidateAdditionalCharacters();
        assertEquals("A\u0011B\u0011C D\u0011X\u0011F ", FUNCTION.apply("A\u0011B\u0011C\nD\u0011E\u0011F\n").toDisplayString());
    }
}
