/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.adapters.netflow.ipfix;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.DateTimeMicrosecondsValue;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.DateTimeMillisecondsValue;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.DateTimeNanosecondsValue;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Float32Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Float64Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Signed16Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Signed32Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Signed64Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Signed8Value;

public class ValueTest {

    @Test
    public void testBooleanValue() throws Exception {
        final BooleanValue bTrue = (BooleanValue) BooleanValue.parser("booleanName").parse(null, ByteBuffer.wrap(new byte[]{1}));
        Assert.assertTrue(bTrue.value);
        Assert.assertEquals("booleanName", bTrue.getName());

        final BooleanValue bFalse = (BooleanValue) BooleanValue.parser("booleanName").parse(null, ByteBuffer.wrap(new byte[]{2}));
        Assert.assertFalse(bFalse.value);
        Assert.assertEquals("booleanName", bFalse.getName());
    }

    @Test(expected = InvalidPacketException.class)
    public void testBooleanValueInvalid() throws Exception {
        BooleanValue.parser("booleanName").parse(null, ByteBuffer.wrap(new byte[]{3}));
    }

    @Test
    public void testDateTimeMicrosecondsValue() throws Exception {
        final DateTimeMicrosecondsValue dateTimeMicrosecondsValue1 = (DateTimeMicrosecondsValue) DateTimeMicrosecondsValue.parser("dateTimeMicrosecondsName1").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}));
        Assert.assertEquals(0, dateTimeMicrosecondsValue1.seconds);
        Assert.assertEquals(0, dateTimeMicrosecondsValue1.fraction);
        Assert.assertEquals("dateTimeMicrosecondsName1", dateTimeMicrosecondsValue1.getName());

        final DateTimeMicrosecondsValue dateTimeMicrosecondsValue2 = (DateTimeMicrosecondsValue) DateTimeMicrosecondsValue.parser("dateTimeMicrosecondsName2").parse(null, ByteBuffer.wrap(new byte[]{0, 0, (byte) 0xFF, (byte) 0xFF, 0, 0, (byte) 0xFF, (byte) 0xFF}));
        Assert.assertEquals(65535, dateTimeMicrosecondsValue2.seconds);
        Assert.assertEquals(65535 & (0xFFFFFFFF << 11), dateTimeMicrosecondsValue2.fraction);
        Assert.assertEquals("dateTimeMicrosecondsName2", dateTimeMicrosecondsValue2.getName());
    }

    @Test
    public void testDateTimeMillisecondsValue() throws Exception {
        final DateTimeMillisecondsValue dateTimeMillisecondsValue1 = (DateTimeMillisecondsValue) DateTimeMillisecondsValue.parser("dateTimeMillisecondsName1").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}));
        Assert.assertEquals(0, dateTimeMillisecondsValue1.milliseconds.longValue());
        Assert.assertEquals("dateTimeMillisecondsName1", dateTimeMillisecondsValue1.getName());

        final DateTimeMillisecondsValue dateTimeMillisecondsValue2 = (DateTimeMillisecondsValue) DateTimeMillisecondsValue.parser("dateTimeMillisecondsName2").parse(null, ByteBuffer.wrap(new byte[]{0, 0, (byte) 0xFF, (byte) 0xFF, 0, 0, (byte) 0xFF, (byte) 0xFF}));
        Assert.assertEquals((65535 << 32) | 65535, dateTimeMillisecondsValue2.milliseconds.longValue());
        Assert.assertEquals("dateTimeMillisecondsName2", dateTimeMillisecondsValue2.getName());
    }

    @Test
    public void testDateTimeNanosecondsValue() throws Exception {
        final DateTimeNanosecondsValue dateTimeNanosecondsValue1 = (DateTimeNanosecondsValue) DateTimeNanosecondsValue.parser("dateTimeNanosecondsName1").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}));
        Assert.assertEquals(0, dateTimeNanosecondsValue1.seconds);
        Assert.assertEquals(0, dateTimeNanosecondsValue1.fraction);
        Assert.assertEquals("dateTimeNanosecondsName1", dateTimeNanosecondsValue1.getName());

        final DateTimeNanosecondsValue dateTimeNanosecondsValue2 = (DateTimeNanosecondsValue) DateTimeNanosecondsValue.parser("dateTimeNanosecondsName2").parse(null, ByteBuffer.wrap(new byte[]{0, 0, (byte) 0xFF, (byte) 0xFF, 0, 0, (byte) 0xFF, (byte) 0xFF}));
        Assert.assertEquals(65535, dateTimeNanosecondsValue2.seconds);
        Assert.assertEquals(65535, dateTimeNanosecondsValue2.fraction);
        Assert.assertEquals("dateTimeNanosecondsName2", dateTimeNanosecondsValue2.getName());
    }

    @Test
    public void testFloat32Value() throws Exception {
        final Float32Value float32Value1 = (Float32Value) Float32Value.parser("float32Name1").parse(null, (ByteBuffer) ByteBuffer.allocate(4).putFloat(0).flip());
        Assert.assertEquals(0f, float32Value1.value, 0);
        Assert.assertEquals("float32Name1", float32Value1.getName());

        final Float32Value float32Value2 = (Float32Value) Float32Value.parser("float32Name2").parse(null, (ByteBuffer) ByteBuffer.allocate(4).putFloat(123.456f).flip());
        Assert.assertEquals(123.456f, float32Value2.value, 0);
        Assert.assertEquals("float32Name2", float32Value2.getName());

        Assert.assertEquals(1.4E-45f, ((Float32Value) Float32Value.parser("float32Name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 1}))).value, 0);
        Assert.assertEquals(1.4E-45f, ((Float32Value) Float32Value.parser("float32Name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 1}))).value, 0);
        Assert.assertEquals(1.4E-45f, ((Float32Value) Float32Value.parser("float32Name").parse(null, ByteBuffer.wrap(new byte[]{0, 1}))).value, 0);
        Assert.assertEquals(1.4E-45f, ((Float32Value) Float32Value.parser("float32Name").parse(null, ByteBuffer.wrap(new byte[]{1}))).value, 0);
    }

    @Test
    public void testFloat64Value() throws Exception {
        final Float64Value float64Value1 = (Float64Value) Float64Value.parser("float64Name1").parse(null, (ByteBuffer) ByteBuffer.allocate(8).putDouble(0).flip());
        Assert.assertEquals(0f, float64Value1.value, 0);
        Assert.assertEquals("float64Name1", float64Value1.getName());

        final Float64Value float64Value2 = (Float64Value) Float64Value.parser("float64Name2").parse(null, (ByteBuffer) ByteBuffer.allocate(8).putDouble(123.456).flip());
        Assert.assertEquals(123.456, float64Value2.value, 0);
        Assert.assertEquals("float64Name2", float64Value2.getName());

        Assert.assertEquals(4.9E-324, ((Float64Value) Float64Value.parser("float64Name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0, 1}))).value, 0);
        Assert.assertEquals(4.9E-324, ((Float64Value) Float64Value.parser("float64Name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 1}))).value, 0);
        Assert.assertEquals(4.9E-324, ((Float64Value) Float64Value.parser("float64Name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 1}))).value, 0);
        Assert.assertEquals(4.9E-324, ((Float64Value) Float64Value.parser("float64Name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 1}))).value, 0);
        Assert.assertEquals(4.9E-324, ((Float64Value) Float64Value.parser("float64Name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 1}))).value, 0);
        Assert.assertEquals(4.9E-324, ((Float64Value) Float64Value.parser("float64Name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 1}))).value, 0);
        Assert.assertEquals(4.9E-324, ((Float64Value) Float64Value.parser("float64Name").parse(null, ByteBuffer.wrap(new byte[]{0, 1}))).value, 0);
        Assert.assertEquals(4.9E-324, ((Float64Value) Float64Value.parser("float64Name").parse(null, ByteBuffer.wrap(new byte[]{1}))).value, 0);
    }

    @Test
    public void testIPv4AddressValue() throws Exception {
        final IPv4AddressValue ipv4AddressValue = (IPv4AddressValue) IPv4AddressValue.parser("ipv4AddressName").parse(null, ByteBuffer.wrap(InetAddress.getByName("127.0.0.1").getAddress()));
        Assert.assertEquals("ipv4AddressName", ipv4AddressValue.getName());
        Assert.assertEquals("127.0.0.1", ipv4AddressValue.inet4Address.getHostAddress());
    }

    @Test
    public void testIPv6AddressValue() throws Exception {
        final IPv6AddressValue ipv6AddressValue = (IPv6AddressValue) IPv6AddressValue.parser("ipv6AddressName").parse(null, ByteBuffer.wrap(InetAddress.getByName("2001:638:301:11a0:d498:3253:ca5f:3777").getAddress()));
        Assert.assertEquals("ipv6AddressName", ipv6AddressValue.getName());
        Assert.assertEquals("2001:638:301:11a0:d498:3253:ca5f:3777", ipv6AddressValue.inet6Address.getHostAddress());
    }

    @Test
    public void testMacAddressValue() throws Exception {
        final MacAddressValue macAddressValue = (MacAddressValue) MacAddressValue.parser("macAddressName").parse(null, ByteBuffer.wrap(new byte[]{1, 2, 4, 8, 16, 32}));
        Assert.assertEquals("macAddressName", macAddressValue.getName());
        Assert.assertArrayEquals(new byte[]{1, 2, 4, 8, 16, 32}, macAddressValue.macAddressOctets);
    }

    @Test
    public void testOctetArrayValue() throws Exception {
        final OctetArrayValue octetArrayValue = (OctetArrayValue) OctetArrayValue.parser("octetArrayName").parse(null, ByteBuffer.wrap(new byte[]{1, 2, 4, 8, 16}));
        Assert.assertEquals("octetArrayName", octetArrayValue.getName());
        Assert.assertArrayEquals(new byte[]{1, 2, 4, 8, 16}, octetArrayValue.data);
    }

    @Test
    public void testSigned64Value() throws Exception {
        final Signed64Value v1 = (Signed64Value) Signed64Value.parser("name1").parse(null, (ByteBuffer) ByteBuffer.allocate(8).putLong(0).flip());
        Assert.assertEquals(0, v1.value);
        Assert.assertEquals("name1", v1.getName());

        final Signed64Value v2 = (Signed64Value) Signed64Value.parser("name2").parse(null, (ByteBuffer) ByteBuffer.allocate(8).putLong(42).flip());
        Assert.assertEquals(42, v2.value);
        Assert.assertEquals("name2", v2.getName());

        final Signed64Value v3 = (Signed64Value) Signed64Value.parser("name3").parse(null, (ByteBuffer) ByteBuffer.allocate(8).putLong(-42).flip());
        Assert.assertEquals(-42, v3.value);
        Assert.assertEquals("name3", v3.getName());

        Assert.assertEquals(1, ((Signed64Value) Signed64Value.parser("name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0, 1}))).value, 0);
        Assert.assertEquals(1, ((Signed64Value) Signed64Value.parser("name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 1}))).value, 0);
        Assert.assertEquals(1, ((Signed64Value) Signed64Value.parser("name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 1}))).value, 0);
        Assert.assertEquals(1, ((Signed64Value) Signed64Value.parser("name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 1}))).value, 0);
        Assert.assertEquals(1, ((Signed64Value) Signed64Value.parser("name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 1}))).value, 0);
        Assert.assertEquals(1, ((Signed64Value) Signed64Value.parser("name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 1}))).value, 0);
        Assert.assertEquals(1, ((Signed64Value) Signed64Value.parser("name").parse(null, ByteBuffer.wrap(new byte[]{0, 1}))).value, 0);
        Assert.assertEquals(1, ((Signed64Value) Signed64Value.parser("name").parse(null, ByteBuffer.wrap(new byte[]{1}))).value, 0);
    }

    @Test
    public void testSigned32Value() throws Exception {
        final Signed32Value v1 = (Signed32Value) Signed32Value.parser("name1").parse(null, (ByteBuffer) ByteBuffer.allocate(4).putInt(0).flip());
        Assert.assertEquals(0, v1.value);
        Assert.assertEquals("name1", v1.getName());

        final Signed32Value v2 = (Signed32Value) Signed32Value.parser("name2").parse(null, (ByteBuffer) ByteBuffer.allocate(4).putInt(42).flip());
        Assert.assertEquals(42, v2.value);
        Assert.assertEquals("name2", v2.getName());

        final Signed32Value v3 = (Signed32Value) Signed32Value.parser("name3").parse(null, (ByteBuffer) ByteBuffer.allocate(4).putInt(-42).flip());
        Assert.assertEquals(-42, v3.value);
        Assert.assertEquals("name3", v3.getName());

        Assert.assertEquals(1, ((Signed32Value) Signed32Value.parser("name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 1}))).value, 0);
        Assert.assertEquals(1, ((Signed32Value) Signed32Value.parser("name").parse(null, ByteBuffer.wrap(new byte[]{0, 0, 1}))).value, 0);
        Assert.assertEquals(1, ((Signed32Value) Signed32Value.parser("name").parse(null, ByteBuffer.wrap(new byte[]{0, 1}))).value, 0);
        Assert.assertEquals(1, ((Signed32Value) Signed32Value.parser("name").parse(null, ByteBuffer.wrap(new byte[]{1}))).value, 0);
    }

    @Test
    public void testSigned16Value() throws Exception {
        final Signed16Value v1 = (Signed16Value) Signed16Value.parser("name1").parse(null, (ByteBuffer) ByteBuffer.allocate(2).putShort((short) 0).flip());
        Assert.assertEquals(0, v1.value);
        Assert.assertEquals("name1", v1.getName());

        final Signed16Value v2 = (Signed16Value) Signed16Value.parser("name2").parse(null, (ByteBuffer) ByteBuffer.allocate(2).putShort((short) 42).flip());
        Assert.assertEquals(42, v2.value);
        Assert.assertEquals("name2", v2.getName());

        final Signed16Value v3 = (Signed16Value) Signed16Value.parser("name3").parse(null, (ByteBuffer) ByteBuffer.allocate(2).putShort((short) -42).flip());
        Assert.assertEquals(-42, v3.value);
        Assert.assertEquals("name3", v3.getName());

        Assert.assertEquals(1, ((Signed16Value) Signed16Value.parser("name").parse(null, ByteBuffer.wrap(new byte[]{0, 1}))).value, 0);
        Assert.assertEquals(1, ((Signed16Value) Signed16Value.parser("name").parse(null, ByteBuffer.wrap(new byte[]{1}))).value, 0);
    }

    @Test
    public void testSigned8Value() throws Exception {
        final Signed8Value v1 = (Signed8Value) Signed8Value.parser("name1").parse(null, ByteBuffer.wrap(new byte[]{0}));
        Assert.assertEquals(0, v1.value);
        Assert.assertEquals("name1", v1.getName());

        final Signed8Value v2 = (Signed8Value) Signed8Value.parser("name2").parse(null, ByteBuffer.wrap(new byte[]{42}));
        Assert.assertEquals(42, v2.value);
        Assert.assertEquals("name2", v2.getName());

        final Signed8Value v3 = (Signed8Value) Signed8Value.parser("name3").parse(null, ByteBuffer.wrap(new byte[]{-42}));
        Assert.assertEquals(-42, v3.value);
        Assert.assertEquals("name3", v3.getName());
    }
}
