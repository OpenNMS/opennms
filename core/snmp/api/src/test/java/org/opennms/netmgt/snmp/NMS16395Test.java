/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2024 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.snmp;

import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;
import java.util.Base64;

import org.junit.Test;

public class NMS16395Test {

    @Test
    public void testAdditionalPrintableCharactersFail() {
        System.setProperty(AbstractSnmpValue.ADDITIONAL_PRINTABLE_CHARACTERS_PROPERTY, "");
        AbstractSnmpValue.ADDITIONAL_PRINTABLE_CHARACTERS = null;
        assertEquals("A\u0011B\u0011C\nD\u0011E\u0011F\n", checkHexString("41114211430a44114511460a", false));
        assertEquals("A\u0011B\u0011C\nD\u0011E\u0011F\n", checkBase64String("QRFCEUMKRBFFEUYK", false));
    }

    @Test
    public void testAdditionalPrintableCharacters() {
        System.setProperty(AbstractSnmpValue.ADDITIONAL_PRINTABLE_CHARACTERS_PROPERTY, "0x0a,0x11");
        AbstractSnmpValue.ADDITIONAL_PRINTABLE_CHARACTERS = null;
        assertEquals("A\u0011B\u0011C\nD\u0011E\u0011F\n", checkHexString("41114211430a44114511460a", true));
        assertEquals("A\u0011B\u0011C\nD\u0011E\u0011F\n", checkBase64String("QRFCEUMKRBFFEUYK", true));
    }

    private String checkHexString(final String hexString, final boolean expected) {
        byte[] bytes = javax.xml.bind.DatatypeConverter.parseHexBinary(hexString);
        assertEquals(expected, AbstractSnmpValue.allBytesPlainAscii(bytes));
        return new String(bytes, Charset.defaultCharset());
    }

    private String checkBase64String(final String base64String, final boolean expected) {
        byte[] bytes = Base64.getDecoder().decode(base64String);
        assertEquals(expected, AbstractSnmpValue.allBytesPlainAscii(bytes));
        return new String(bytes, Charset.defaultCharset());
    }
}
