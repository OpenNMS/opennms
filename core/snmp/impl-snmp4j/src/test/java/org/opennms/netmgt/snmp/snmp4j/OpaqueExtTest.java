/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import java.nio.ByteBuffer;
import java.util.Objects;
import org.junit.Test;
import static org.junit.Assert.*;
import org.snmp4j.asn1.BERInputStream;

public class OpaqueExtTest {
    
    private static byte[] hexStringToByteArray(String s) {
        Objects.requireNonNull(s);
        
        int len = s.length();
        if ((len & 1) == 1) {
            throw new IllegalArgumentException("Require a sting with even length");
        }
        byte[] data = new byte[len / 2];
        int hi = 0;
        int i = 0;
        for (char c : s.toCharArray()) {
            int digit = Character.digit(c, 16);
            if (digit == -1) {
                throw new IllegalArgumentException("HEX-String expected");
            }
            if((i & 1) == 0) {
                hi = digit << 4;
            } else {
                data[i >> 1] = (byte)(hi + digit);
            }
            i++;
        }
        return data;
    }       
    
    private static final byte[] DOUBLE_DATA = hexStringToByteArray("9f780442f60000"); //Double 123.0
    private static final byte[] DOUBLE_DATA_TOO_LONG = hexStringToByteArray("9f780442f6000000"); //Double 123.0
    private static final byte[] DOUBLE_DATA_WITH_OPAQUE_HEADER = hexStringToByteArray("44079f780442f60000"); //Double 123.0
    
    public OpaqueExtTest() {
    }

    @Test
    public void testClone() {
        OpaqueExt instance = new OpaqueExt(DOUBLE_DATA);
        OpaqueExt clone = (OpaqueExt) instance.clone();
                
        assertEquals(instance.getValueType(), clone.getValueType());
        assertArrayEquals(instance.getValue(), clone.getValue());
    }

    @Test
    public void testDoubleCreation() {
        OpaqueExt instance = new OpaqueExt(DOUBLE_DATA);
        assertEquals(OpaqueValueType.DOUBLE, instance.getValueType());
        assertEquals("123.0", instance.toString());
        assertEquals(123.0, instance.getDouble(),0);
        assertEquals(123L, instance.getLong().longValue());
    }

    @Test(expected = NullPointerException.class)
    public void testNull() {
        OpaqueExt instance = new OpaqueExt(null);
    }

    @Test
    public void testEmpty() {
        OpaqueExt instance = new OpaqueExt(new byte[]{});
        assertEquals(OpaqueValueType.ERROR, instance.getValueType());
        assertNull(instance.getLong());
        assertNull(instance.getDouble());
        assertNull(instance.toString());
    }

    @Test
    public void testUnsupportedFormat() {
        OpaqueExt instance = new OpaqueExt(hexStringToByteArray("FF000100"));
        assertEquals(OpaqueValueType.UNSUPPORTED, instance.getValueType());
        assertNull(instance.getLong());
        assertNull(instance.getDouble());
        assertArrayEquals(instance.getValue(), new byte[]{-1,0,1,0});
    }

    @Test
    public void testWrongLength() {
        OpaqueExt instance = new OpaqueExt(DOUBLE_DATA_TOO_LONG);
        assertEquals(OpaqueValueType.ERROR, instance.getValueType());
        assertNull(instance.getLong());
        assertNull(instance.getDouble());
        assertArrayEquals(instance.getValue(), DOUBLE_DATA_TOO_LONG);
    }

    @Test
    public void testSetValue() {
        OpaqueExt instance = new OpaqueExt();
        instance.setValue(DOUBLE_DATA);
        assertEquals(OpaqueValueType.DOUBLE, instance.getValueType());
        assertEquals("123.0", instance.toString());
        assertEquals(123.0, instance.getDouble(),0);
        assertEquals(123L, instance.getLong().longValue());
    }

    @Test
    public void testDecodeBER() throws Exception {
        OpaqueExt instance = new OpaqueExt();
        BERInputStream inputStream = new BERInputStream(ByteBuffer.wrap(DOUBLE_DATA_WITH_OPAQUE_HEADER));
        instance.decodeBER(inputStream);
        assertEquals(OpaqueValueType.DOUBLE, instance.getValueType());
        assertEquals("123.0", instance.toString());
        assertEquals(123.0, instance.getDouble(),0);
        assertEquals(123L, instance.getLong().longValue());
    }
    
}
