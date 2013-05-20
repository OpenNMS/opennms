/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.mock;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.snmp.joesnmp.JoeSnmpValueFactory;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValueFactory;

public class SnmpValueTest {
    private static final SnmpValueFactory[] m_factories = new SnmpValueFactory[] { new Snmp4JValueFactory(), new JoeSnmpValueFactory(), new MockSnmpValueFactory() };

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }

    @Test
    public void testParseHex() {
        final String trimmed = "00 24 81 8F 40 17";
        final ByteBuffer bb = ByteBuffer.allocate(trimmed.length());
        for (final String chunk : trimmed.split("[ :]")) {
            short s = Short.valueOf(chunk, 16);
            final byte b = (byte) (s & 0xFF);
            bb.put(b);
        }
        final byte[] parsed = new byte[bb.position()];
        bb.flip();
        bb.get(parsed);

        assertArrayEquals(new byte[] { (byte) 0x0, (byte) 0x24, (byte) 0x81, (byte) 0x8f, (byte) 0x40, (byte) 0x17 }, parsed);
    }

    @Test
    public void testCounter32() {
        for (final SnmpValueFactory factory : m_factories) {
            final String methodName = "Counter32";
            final String stringResult = "42";
            final Long numberResult = 42L;
            final SnmpValue value = factory.getCounter32(numberResult);
            final String className = factory.getClass().getName();

            doNumericCheck(className, methodName, value, stringResult, numberResult);
        }
    }

    @Test
    public void testCounter64() {
        for (final SnmpValueFactory factory : m_factories) {
            final String methodName = "Counter64";
            final String stringResult = "42";
            final Long numberResult = 42L;
            final SnmpValue value = factory.getCounter64(BigInteger.valueOf(numberResult));
            final String className = factory.getClass().getName();

            doNumericCheck(className, methodName, value, stringResult, numberResult);
        }
    }

    @Test
    public void testGauge32() {
        for (final SnmpValueFactory factory : m_factories) {
            final String methodName = "Gauge32";
            final String stringResult = "42";
            final Long numberResult = 42L;
            final SnmpValue value = factory.getGauge32(numberResult);
            final String className = factory.getClass().getName();

            doNumericCheck(className, methodName, value, stringResult, numberResult);
        }
    }

    @Test
    public void testInt32() {
        for (final SnmpValueFactory factory : m_factories) {
            final String methodName = "Int32";
            final String stringResult = "42";
            final Long numberResult = 42L;
            final SnmpValue value = factory.getInt32(numberResult.intValue());
            final String className = factory.getClass().getName();

            doNumericCheck(className, methodName, value, stringResult, numberResult);
        }
    }

    @Test
    public void testNull() {
        for (final SnmpValueFactory factory : m_factories) {
            final SnmpValue value = factory.getNull();
            final String factoryClassName = factory.getClass().getName();
            assertFalse(factoryClassName + ": Null isDisplayable should be false", value.isDisplayable());
            assertTrue(factoryClassName + ": Null isNull should be true", value.isNull());
            assertFalse(factoryClassName + ": Null isEndOfMib should be false", value.isEndOfMib());
            assertFalse(factoryClassName + ": Null isError should be false", value.isError());
        }
    }

    @Test
    public void testInetAddress() {
        for (final SnmpValueFactory factory : m_factories) {
            final InetAddress address = InetAddressUtils.addr("192.168.0.1");
            final SnmpValue value = factory.getIpAddress(address);
            final String className = factory.getClass().getName();

            assertTrue(className + ": getInetAddress isDisplayable should be true", value.isDisplayable());
            assertEquals(className + ": getInetAddress to InetAddress should return 192.168.0.1", address, value.toInetAddress());
            assertEquals(className + ": getInetAddress to String should return 192.168.0.1", "192.168.0.1", value.toString());
            assertEquals(className + ": getInetAddress to DisplayString should return 192.168.0.1", "192.168.0.1", value.toDisplayString());
            try {
                value.toInt();
                fail(className + ": getInetAddress to int should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toLong();
                fail(className + ": getInetAddress to long should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toBigInteger();
                fail(className + ": getInetAddress to BigInteger should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toHexString();
                fail(className + ": getInetAddress to HexString should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toSnmpObjId();
                fail(className + ": getInetAddress to SnmpObjId should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
        }
    }

    @Test
    public void testSnmpObjId() {
        for (final SnmpValueFactory factory : m_factories) {
            final String oid = ".1.3.6.1.4.1.2925.4.5.2.1.1";
            final SnmpObjId id = SnmpObjId.get(oid);
            final SnmpValue value = factory.getObjectId(id);
            final String className = factory.getClass().getName();

            assertTrue(className + ": getInetAddress isDisplayable should be true", value.isDisplayable());
            assertEquals(className + ": getObjectId to SnmpObjId should return " + oid, id, value.toSnmpObjId());
            assertEquals(className + ": getObjectId to String should return " + oid, oid, value.toString());
            assertEquals(className + ": getObjectId to DisplayString should return " + oid, oid, value.toDisplayString());
            try {
                value.toInt();
                fail(className + ": getObjectId to int should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toLong();
                fail(className + ": getObjectId to long should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toBigInteger();
                fail(className + ": getObjectId to BigInteger should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toHexString();
                fail(className + ": getObjectId to HexString should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toInetAddress();
                fail(className + ": getObjectId to InetAddress should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
        }
    }

    @Test
    public void testTimeTicks() {
        for (final SnmpValueFactory factory : m_factories) {
            final String className = factory.getClass().getName();
            final SnmpValue[] values = { factory.getTimeTicks(42) };
            for (final SnmpValue value : values) {
                assertTrue(className + ": getInetAddress isDisplayable should be true", value.isDisplayable());
                assertEquals(className + ": getTimeTicks to int should return " + value.toInt(), 42, value.toInt());
                assertEquals(className + ": getTimeTicks to long should return " + value.toLong(), 42, value.toLong());
                assertEquals(className + ": getTimeTicks to BigInteger should return " + value.toBigInteger(), BigInteger.valueOf(42), value.toBigInteger());
                assertEquals(className + ": getTimeTicks to String should return 42", "42", value.toString());
                assertEquals(className + ": getTimeTicks to DisplayString should return 42", "42", value.toDisplayString());
                try {
                    value.toHexString();
                    fail(className + ": getTimeTicks to HexString should throw an IllegalArgumentException");
                } catch (final IllegalArgumentException e) { /* expected */
                }
                try {
                    value.toInetAddress();
                    fail(className + ": getTimeTicks to InetAddress should throw an IllegalArgumentException");
                } catch (final IllegalArgumentException e) { /* expected */
                }
                try {
                    value.toSnmpObjId();
                    fail(className + ": getTimeTicks to SnmpObjId should throw an IllegalArgumentException");
                } catch (final IllegalArgumentException e) { /* expected */
                }
            }
        }
    }

    @Test
    public void testMacAddressOctetString() {
        for (final SnmpValueFactory factory : m_factories) {
            final String hexString = "005056e7a72f";
            final byte[] rawBytes = { 0, (byte) 0x50, (byte) 0x56, (byte) 0xe7, (byte) 0xa7, (byte) 0x2f };
            final String stringBytes = "." + new String(Arrays.copyOfRange(rawBytes, 1, rawBytes.length));
            final String className = factory.getClass().getName();

            final SnmpValue value = factory.getOctetString(rawBytes);

            assertArrayEquals(className + ": getOctetString bytes should match", rawBytes, value.getBytes());
            assertFalse(className + ": getOctetString displayable should be false", value.isDisplayable());
            assertEquals(className + ": getOctetString to String should return " + stringBytes, stringBytes, value.toString());
            assertEquals(className + ": getOctetString to DisplayString should return " + stringBytes, stringBytes, value.toDisplayString());
            assertEquals(className + ": getOctetString to HexString should return " + hexString, hexString, value.toHexString());
            try {
                value.toInt();
                fail(className + ": getOctetString to int should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toLong();
                fail(className + ": getOctetString to long should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toBigInteger();
                fail(className + ": getOctetString to BigInteger should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
        }
    }

    @Test
    public void testOtherAddressOctetString() {
        for (final SnmpValueFactory factory : m_factories) {
            final String hexString = "abcd";
            final byte[] rawBytes = { (byte) 0xab, (byte) 0xcd };
            final String stringBytes = new String(rawBytes);
            final String className = factory.getClass().getName();

            final SnmpValue value = factory.getOctetString(rawBytes);

            assertArrayEquals(className + ": getOctetString bytes should match", rawBytes, value.getBytes());
            assertFalse(className + ": getOctetString displayable should be false", value.isDisplayable());
            assertEquals(className + ": getOctetString to String should return " + stringBytes, stringBytes, value.toString());
            assertEquals(className + ": getOctetString to DisplayString should return " + stringBytes, stringBytes, value.toDisplayString());
            assertEquals(className + ": getOctetString to HexString should return " + hexString, hexString, value.toHexString());
            try {
                value.toInt();
                fail(className + ": getOctetString to int should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toLong();
                fail(className + ": getOctetString to long should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toBigInteger();
                fail(className + ": getOctetString to BigInteger should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
        }
    }

    @Test
    public void testNullTerminatedASCIIString() {
        for (final SnmpValueFactory factory : m_factories) {
            final String hex = "49206c696b65206368656573652100";
            final String expectedText = "I like cheese!.";
            final byte[] rawBytes = { (byte)0x49, (byte)0x20, (byte)0x6c, (byte)0x69, (byte)0x6b, (byte)0x65, (byte)0x20, (byte)0x63, (byte)0x68, (byte)0x65, (byte)0x65, (byte)0x73, (byte)0x65, (byte)0x21, (byte)0x00 };
            final String className = factory.getClass().getName();

            final SnmpValue value = factory.getOctetString(rawBytes);

            assertArrayEquals(className + ": getOctetString bytes should match", rawBytes, value.getBytes());
            assertTrue(className + ": getOctetString displayable should be true", value.isDisplayable());
            assertEquals(className + ": getOctetString to String should return " + expectedText, expectedText, value.toString());
            assertEquals(className + ": getOctetString to DisplayString should return " + expectedText, expectedText, value.toDisplayString());
            assertEquals(className + ": getOctetString to HexString should return " + hex, hex, value.toHexString());
            try {
                value.toInt();
                fail(className + ": getOctetString to int should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toLong();
                fail(className + ": getOctetString to long should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toBigInteger();
                fail(className + ": getOctetString to BigInteger should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
        }

    }
    @Test
    public void testNormalString() {
        for (final SnmpValueFactory factory : m_factories) {
            final String text = "I like cheese!";
            final String hex = "49206c696b652063686565736521";
            final byte[] rawBytes = text.getBytes();
            final String className = factory.getClass().getName();

            final SnmpValue value = factory.getOctetString(rawBytes);

            assertArrayEquals(className + ": getOctetString bytes should match", rawBytes, value.getBytes());
            assertTrue(className + ": getOctetString displayable should be true", value.isDisplayable());
            assertEquals(className + ": getOctetString to String should return " + text, text, value.toString());
            assertEquals(className + ": getOctetString to DisplayString should return " + text, text, value.toDisplayString());
            assertEquals(className + ": getOctetString to HexString should return " + hex, hex, value.toHexString());
            try {
                value.toInt();
                fail(className + ": getOctetString to int should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toLong();
                fail(className + ": getOctetString to long should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
            try {
                value.toBigInteger();
                fail(className + ": getOctetString to BigInteger should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) { /* expected */
            }
        }
    }

    private void doNumericCheck(final String className, final String methodName, final SnmpValue result, final String expectedResultString, final Long expectedResultNumber) {
        assertTrue(className + ": " + methodName + " isDisplayable should be true", result.isDisplayable());
        assertEquals(className + ": " + methodName + " to int should return " + expectedResultString, expectedResultNumber.intValue(), result.toInt());
        assertEquals(className + ": " + methodName + " to long should return " + expectedResultString, expectedResultNumber.longValue(), result.toLong());
        assertEquals(className + ": " + methodName + " to BigInteger should return " + expectedResultString, BigInteger.valueOf(expectedResultNumber.longValue()), result.toBigInteger());
        assertEquals(className + ": " + methodName + " to String should return " + expectedResultString, expectedResultString, result.toString());
        assertEquals(className + ": " + methodName + " to DisplayString should return " + expectedResultString, expectedResultString, result.toDisplayString());
        assertEquals(className + ": " + methodName + " to bytes should return [" + expectedResultString + "]", "[" + expectedResultString + "]", Arrays.toString(result.getBytes()));
        try {
            result.toHexString();
            fail(className + ": " + methodName + " to HexString should throw an IllegalArgumentException");
        } catch (final IllegalArgumentException e) { /* expected */
        }
        try {
            result.toInetAddress();
            fail(className + ": " + methodName + " to InetAddress should throw an IllegalArgumentException");
        } catch (final IllegalArgumentException e) { /* expected */
        }
        try {
            result.toSnmpObjId();
            fail(className + ": " + methodName + " to SnmpObjId should throw an IllegalArgumentException");
        } catch (final IllegalArgumentException e) { /* expected */
        }
    }

}
