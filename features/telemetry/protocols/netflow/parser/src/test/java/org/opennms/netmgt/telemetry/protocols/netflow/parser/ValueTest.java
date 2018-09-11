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

package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.FloatValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.SignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.StringValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;

public class ValueTest {

    // TODO: Test BasicList, SubTemplateList, SubTemplateMultiList

    @Test
    public void testBooleanValue() throws Exception {
        final BooleanValue bTrue = (BooleanValue) BooleanValue.parser("booleanName", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{1}));
        Assert.assertTrue(bTrue.getValue());
        Assert.assertEquals("booleanName", bTrue.getName());

        final BooleanValue bFalse = (BooleanValue) BooleanValue.parser("booleanName", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{2}));
        Assert.assertFalse(bFalse.getValue());
        Assert.assertEquals("booleanName", bFalse.getName());
    }

    @Test(expected = InvalidPacketException.class)
    public void testBooleanValueInvalid() throws Exception {
        BooleanValue.parser("booleanName", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{3}));
    }

    @Test
    public void testDateTimeMicrosecondsValue() throws Exception {
        final DateTimeValue dateTimeMicrosecondsValue1 = (DateTimeValue) DateTimeValue.parserWithMicroseconds("dateTimeMicrosecondsName1", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}));
        Assert.assertEquals(Instant.from(ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)), dateTimeMicrosecondsValue1.getValue());
        Assert.assertEquals("dateTimeMicrosecondsName1", dateTimeMicrosecondsValue1.getName());

        final DateTimeValue dateTimeMicrosecondsName2 = (DateTimeValue) DateTimeValue.parserWithMicroseconds("dateTimeMicrosecondsName2", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(8).putInt((int)(1509533996L + DateTimeValue.SECONDS_TO_EPOCH)).putInt(0).flip());
        Assert.assertEquals(Instant.from(ZonedDateTime.of(2017, 11, 1, 10, 59, 56, 0, ZoneOffset.UTC)), dateTimeMicrosecondsName2.getValue());
        Assert.assertEquals("dateTimeMicrosecondsName2", dateTimeMicrosecondsName2.getName());


        final DateTimeValue dateTimeMicrosecondsName3 = (DateTimeValue) DateTimeValue.parserWithMicroseconds("dateTimeMicrosecondsName3", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(8).putInt((int)(1509533996L + DateTimeValue.SECONDS_TO_EPOCH)).putInt(16775168).flip());
        Assert.assertEquals(Instant.from(ZonedDateTime.of(2017, 11, 1, 10, 59, 56, 3905773, ZoneOffset.UTC)), dateTimeMicrosecondsName3.getValue());
        Assert.assertEquals("dateTimeMicrosecondsName3", dateTimeMicrosecondsName3.getName());

        final DateTimeValue dateTimeMicrosecondsName4 = (DateTimeValue) DateTimeValue.parserWithMicroseconds("dateTimeMicrosecondsName4", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(8).putInt((int)(1509533996L + DateTimeValue.SECONDS_TO_EPOCH)).putInt(16775169).flip());
        Assert.assertEquals(Instant.from(ZonedDateTime.of(2017, 11, 1, 10, 59, 56, 3905773, ZoneOffset.UTC)), dateTimeMicrosecondsName4.getValue());
        Assert.assertEquals("dateTimeMicrosecondsName4", dateTimeMicrosecondsName4.getName());
    }

    @Test
    public void testDateTimeMillisecondsValue() throws Exception {
        final DateTimeValue dateTimeMillisecondsValue1 = (DateTimeValue) DateTimeValue.parserWithMilliseconds("dateTimeMillisecondsName1", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}));
        Assert.assertEquals(Instant.ofEpochMilli(0), dateTimeMillisecondsValue1.getValue());
        Assert.assertEquals("dateTimeMillisecondsName1", dateTimeMillisecondsValue1.getName());

        final DateTimeValue dateTimeMillisecondsValue2 = (DateTimeValue) DateTimeValue.parserWithMilliseconds("dateTimeMillisecondsName2", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(8).putLong(1509532300714L).flip());
        Assert.assertEquals(Instant.ofEpochMilli(1509532300714L), dateTimeMillisecondsValue2.getValue());
        Assert.assertEquals("dateTimeMillisecondsName2", dateTimeMillisecondsValue2.getName());
    }

    @Test
    public void testDateTimeNanosecondsValue() throws Exception {
        final DateTimeValue dateTimeNanosecondsValue1 = (DateTimeValue) DateTimeValue.parserWithNanoseconds("dateTimeNanosecondsName1", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}));
        Assert.assertEquals(Instant.from(ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)), dateTimeNanosecondsValue1.getValue());
        Assert.assertEquals("dateTimeNanosecondsName1", dateTimeNanosecondsValue1.getName());

        final DateTimeValue dateTimeNanosecondsValue2 = (DateTimeValue) DateTimeValue.parserWithNanoseconds("dateTimeNanosecondsName2", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(8).putInt((int)(1509533996L + DateTimeValue.SECONDS_TO_EPOCH)).putInt(0).flip());
        Assert.assertEquals(Instant.from(ZonedDateTime.of(2017, 11, 1, 10, 59, 56, 0, ZoneOffset.UTC)), dateTimeNanosecondsValue2.getValue());
        Assert.assertEquals("dateTimeNanosecondsName2", dateTimeNanosecondsValue2.getName());

        final DateTimeValue dateTimeNanosecondsValue3 = (DateTimeValue) DateTimeValue.parserWithNanoseconds("dateTimeNanosecondsName3", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(8).putInt((int)(1509533996L + DateTimeValue.SECONDS_TO_EPOCH)).putInt(34509786).flip());
        Assert.assertEquals(Instant.from(ZonedDateTime.of(2017, 11, 1, 10, 59, 56, 8034935, ZoneOffset.UTC)), dateTimeNanosecondsValue3.getValue());
        Assert.assertEquals("dateTimeNanosecondsName3", dateTimeNanosecondsValue3.getName());
    }

    @Test
    public void testFloat32Value() throws Exception {
        final FloatValue float32Value1 = (FloatValue) FloatValue.parserWith32Bit("float32Name1", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(4).putFloat(0).flip());
        Assert.assertEquals(0f, float32Value1.getValue(), 0);
        Assert.assertEquals("float32Name1", float32Value1.getName());

        final FloatValue float32Value2 = (FloatValue) FloatValue.parserWith32Bit("float32Name2", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(4).putFloat(123.456f).flip());
        Assert.assertEquals(123.456f, float32Value2.getValue(), 0);
        Assert.assertEquals("float32Name2", float32Value2.getName());

        Assert.assertEquals(1.4E-45f, ((FloatValue) FloatValue.parserWith32Bit("float32Name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1.4E-45f, ((FloatValue) FloatValue.parserWith32Bit("float32Name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1.4E-45f, ((FloatValue) FloatValue.parserWith32Bit("float32Name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 1}))).getValue(), 0);
        Assert.assertEquals(1.4E-45f, ((FloatValue) FloatValue.parserWith32Bit("float32Name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{1}))).getValue(), 0);
    }

    @Test
    public void testFloat64Value() throws Exception {
        final FloatValue float64Value1 = (FloatValue) FloatValue.parserWith64Bit("float64Name1", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(8).putDouble(0).flip());
        Assert.assertEquals(0f, float64Value1.getValue(), 0);
        Assert.assertEquals("float64Name1", float64Value1.getName());

        final FloatValue float64Value2 = (FloatValue) FloatValue.parserWith64Bit("float64Name2", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(8).putDouble(123.456).flip());
        Assert.assertEquals(123.456, float64Value2.getValue(), 0);
        Assert.assertEquals("float64Name2", float64Value2.getName());

        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit("float64Name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit("float64Name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit("float64Name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit("float64Name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit("float64Name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit("float64Name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit("float64Name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 1}))).getValue(), 0);
        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit("float64Name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{1}))).getValue(), 0);
    }

    @Test
    public void testIPv4AddressValue() throws Exception {
        final IPv4AddressValue ipv4AddressValue = (IPv4AddressValue) IPv4AddressValue.parser("ipv4AddressName", Optional.empty()).parse(null, ByteBuffer.wrap(InetAddress.getByName("127.0.0.1").getAddress()));
        Assert.assertEquals("ipv4AddressName", ipv4AddressValue.getName());
        Assert.assertEquals("127.0.0.1", ipv4AddressValue.getValue().getHostAddress());
    }

    @Test
    public void testIPv6AddressValue() throws Exception {
        final IPv6AddressValue ipv6AddressValue = (IPv6AddressValue) IPv6AddressValue.parser("ipv6AddressName", Optional.empty()).parse(null, ByteBuffer.wrap(InetAddress.getByName("2001:638:301:11a0:d498:3253:ca5f:3777").getAddress()));
        Assert.assertEquals("ipv6AddressName", ipv6AddressValue.getName());
        Assert.assertEquals("2001:638:301:11a0:d498:3253:ca5f:3777", ipv6AddressValue.getValue().getHostAddress());
    }

    @Test
    public void testMacAddressValue() throws Exception {
        final MacAddressValue macAddressValue = (MacAddressValue) MacAddressValue.parser("macAddressName", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{1, 2, 4, 8, 16, 32}));
        Assert.assertEquals("macAddressName", macAddressValue.getName());
        Assert.assertArrayEquals(new byte[]{1, 2, 4, 8, 16, 32}, macAddressValue.getValue());
    }

    @Test
    public void testOctetArrayValue() throws Exception {
        final OctetArrayValue octetArrayValue = (OctetArrayValue) OctetArrayValue.parser("octetArrayName", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{1, 2, 4, 8, 16}));
        Assert.assertEquals("octetArrayName", octetArrayValue.getName());
        Assert.assertArrayEquals(new byte[]{1, 2, 4, 8, 16}, octetArrayValue.getValue());
    }

    @Test
    public void testSigned64Value() throws Exception {
        final SignedValue v1 = (SignedValue) SignedValue.parserWith64Bit("name1", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(8).putLong(0).flip());
        Assert.assertEquals(0, v1.getValue(), 0);
        Assert.assertEquals("name1", v1.getName());

        final SignedValue v2 = (SignedValue) SignedValue.parserWith64Bit("name2", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(8).putLong(42).flip());
        Assert.assertEquals(42, v2.getValue(), 0);
        Assert.assertEquals("name2", v2.getName());

        final SignedValue v3 = (SignedValue) SignedValue.parserWith64Bit("name3", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(8).putLong(-42).flip());
        Assert.assertEquals(-42, v3.getValue(), 0);
        Assert.assertEquals("name3", v3.getName());

        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{1}))).getValue(), 0);
    }

    @Test
    public void testSigned32Value() throws Exception {
        final SignedValue v1 = (SignedValue) SignedValue.parserWith32Bit("name1", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(4).putInt(0).flip());
        Assert.assertEquals(0, v1.getValue(), 0);
        Assert.assertEquals("name1", v1.getName());

        final SignedValue v2 = (SignedValue) SignedValue.parserWith32Bit("name2", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(4).putInt(42).flip());
        Assert.assertEquals(42, v2.getValue(), 0);
        Assert.assertEquals("name2", v2.getName());

        final SignedValue v3 = (SignedValue) SignedValue.parserWith32Bit("name3", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(4).putInt(-42).flip());
        Assert.assertEquals(-42, v3.getValue(), 0);
        Assert.assertEquals("name3", v3.getName());

        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith32Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith32Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith32Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith32Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{1}))).getValue(), 0);
    }

    @Test
    public void testSigned16Value() throws Exception {
        final SignedValue v1 = (SignedValue) SignedValue.parserWith16Bit("name1", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(2).putShort((short) 0).flip());
        Assert.assertEquals(0, v1.getValue(), 0);
        Assert.assertEquals("name1", v1.getName());

        final SignedValue v2 = (SignedValue) SignedValue.parserWith16Bit("name2", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(2).putShort((short) 42).flip());
        Assert.assertEquals(42, v2.getValue(), 0);
        Assert.assertEquals("name2", v2.getName());

        final SignedValue v3 = (SignedValue) SignedValue.parserWith16Bit("name3", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(2).putShort((short) -42).flip());
        Assert.assertEquals(-42, v3.getValue(), 0);
        Assert.assertEquals("name3", v3.getName());

        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith16Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith16Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{1}))).getValue(), 0);
    }

    @Test
    public void testSigned8Value() throws Exception {
        final SignedValue v1 = (SignedValue) SignedValue.parserWith8Bit("name1", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0}));
        Assert.assertEquals(0, v1.getValue(), 0);
        Assert.assertEquals("name1", v1.getName());

        final SignedValue v2 = (SignedValue) SignedValue.parserWith8Bit("name2", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{42}));
        Assert.assertEquals(42, v2.getValue(), 0);
        Assert.assertEquals("name2", v2.getName());

        final SignedValue v3 = (SignedValue) SignedValue.parserWith8Bit("name3", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{-42}));
        Assert.assertEquals(-42, v3.getValue(), 0);
        Assert.assertEquals("name3", v3.getName());
    }

    @Test
    public void testUnsigned64Value() throws Exception {
        final UnsignedValue v1 = (UnsignedValue) UnsignedValue.parserWith64Bit("name1", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(8).putLong(0).flip());
        Assert.assertEquals(0L, v1.getValue().longValue());
        Assert.assertEquals("name1", v1.getName());

        final UnsignedValue v2 = (UnsignedValue) UnsignedValue.parserWith64Bit("name2", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(8).putLong(42).flip());
        Assert.assertEquals(42L, v2.getValue().longValue());
        Assert.assertEquals("name2", v2.getName());

        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{1}))).getValue().longValue(), 0);
    }

    @Test
    public void testUnsigned32Value() throws Exception {
        final UnsignedValue v1 = (UnsignedValue) UnsignedValue.parserWith32Bit("name1", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(4).putInt(0).flip());
        Assert.assertEquals(0L, v1.getValue().longValue());
        Assert.assertEquals("name1", v1.getName());

        final UnsignedValue v2 = (UnsignedValue) UnsignedValue.parserWith32Bit("name2", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(4).putInt(42).flip());
        Assert.assertEquals(42L, v2.getValue().longValue());
        Assert.assertEquals("name2", v2.getName());

        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith32Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith32Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith32Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith32Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{1}))).getValue().longValue(), 0);
    }

    @Test
    public void testUnsigned16Value() throws Exception {
        final UnsignedValue v1 = (UnsignedValue) UnsignedValue.parserWith16Bit("name1", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(2).putShort((short) 0).flip());
        Assert.assertEquals(0L, v1.getValue().longValue());
        Assert.assertEquals("name1", v1.getName());

        final UnsignedValue v2 = (UnsignedValue) UnsignedValue.parserWith16Bit("name2", Optional.empty()).parse(null, (ByteBuffer) ByteBuffer.allocate(2).putShort((short) 42).flip());
        Assert.assertEquals(42L, v2.getValue().longValue());
        Assert.assertEquals("name2", v2.getName());

        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith16Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith16Bit("name", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{1}))).getValue().longValue(), 0);
    }

    @Test
    public void testUnsigned8Value() throws Exception {
        final UnsignedValue v1 = (UnsignedValue) UnsignedValue.parserWith8Bit("name1", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{0}));
        Assert.assertEquals(0L, v1.getValue().longValue());
        Assert.assertEquals("name1", v1.getName());

        final UnsignedValue v2 = (UnsignedValue) UnsignedValue.parserWith8Bit("name2", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{42}));
        Assert.assertEquals(42L, v2.getValue().longValue());
        Assert.assertEquals("name2", v2.getName());
    }

    @Test
    public void testStringValue() throws Exception {
        final StringValue v1 = (StringValue) StringValue.parser("name1", Optional.empty()).parse(null, ByteBuffer.wrap("Hello World".getBytes("UTF-8")));
        Assert.assertEquals("Hello World", v1.getValue());
        Assert.assertEquals("name1", v1.getName());

        final StringValue v2 = (StringValue) StringValue.parser("name2", Optional.empty()).parse(null, ByteBuffer.wrap("Foo".getBytes("UTF-8")));
        Assert.assertEquals("Foo", v2.getValue());
        Assert.assertEquals("name2", v2.getName());

        final StringValue v3 = (StringValue) StringValue.parser("name3", Optional.empty()).parse(null, ByteBuffer.wrap(new byte[]{}));
        Assert.assertEquals("", v3.getValue());
        Assert.assertEquals("name3", v3.getName());
    }
}
