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
