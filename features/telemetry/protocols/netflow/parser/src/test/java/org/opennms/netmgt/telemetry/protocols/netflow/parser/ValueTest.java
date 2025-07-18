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
package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
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

import io.netty.buffer.Unpooled;

public class ValueTest {

    // TODO: Test BasicList, SubTemplateList, SubTemplateMultiList

    final InformationElementDatabase database = new InformationElementDatabase(new org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.InformationElementProvider(), new org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.InformationElementProvider());

    @Test
    public void testBooleanValue() throws Exception {
        final BooleanValue bTrue = (BooleanValue) BooleanValue.parser(database, "booleanName", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{1}));
        Assert.assertTrue(bTrue.getValue());
        Assert.assertEquals("booleanName", bTrue.getName());

        final BooleanValue bFalse = (BooleanValue) BooleanValue.parser(database, "booleanName", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{2}));
        Assert.assertFalse(bFalse.getValue());
        Assert.assertEquals("booleanName", bFalse.getName());
    }

    @Test(expected = InvalidPacketException.class)
    public void testBooleanValueInvalid() throws Exception {
        BooleanValue.parser(database, "booleanName", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{3}));
    }

    @Test
    public void testDateTimeMicrosecondsValue() throws Exception {
        final DateTimeValue dateTimeMicrosecondsValue1 = (DateTimeValue) DateTimeValue.parserWithMicroseconds(database, "dateTimeMicrosecondsName1", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}));
        Assert.assertEquals(Instant.from(ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)), dateTimeMicrosecondsValue1.getValue());
        Assert.assertEquals("dateTimeMicrosecondsName1", dateTimeMicrosecondsValue1.getName());

        final DateTimeValue dateTimeMicrosecondsName2 = (DateTimeValue) DateTimeValue.parserWithMicroseconds(database, "dateTimeMicrosecondsName2", Optional.empty()).parse(database, null, Unpooled.buffer(8).writeInt((int) (1509533996L + DateTimeValue.SECONDS_TO_EPOCH)).writeInt(0));
        Assert.assertEquals(Instant.from(ZonedDateTime.of(2017, 11, 1, 10, 59, 56, 0, ZoneOffset.UTC)), dateTimeMicrosecondsName2.getValue());
        Assert.assertEquals("dateTimeMicrosecondsName2", dateTimeMicrosecondsName2.getName());


        final DateTimeValue dateTimeMicrosecondsName3 = (DateTimeValue) DateTimeValue.parserWithMicroseconds(database, "dateTimeMicrosecondsName3", Optional.empty()).parse(database, null, Unpooled.buffer(8).writeInt((int) (1509533996L + DateTimeValue.SECONDS_TO_EPOCH)).writeInt(16775168));
        Assert.assertEquals(Instant.from(ZonedDateTime.of(2017, 11, 1, 10, 59, 56, 3905773, ZoneOffset.UTC)), dateTimeMicrosecondsName3.getValue());
        Assert.assertEquals("dateTimeMicrosecondsName3", dateTimeMicrosecondsName3.getName());

        final DateTimeValue dateTimeMicrosecondsName4 = (DateTimeValue) DateTimeValue.parserWithMicroseconds(database, "dateTimeMicrosecondsName4", Optional.empty()).parse(database, null, Unpooled.buffer(8).writeInt((int) (1509533996L + DateTimeValue.SECONDS_TO_EPOCH)).writeInt(16775169));
        Assert.assertEquals(Instant.from(ZonedDateTime.of(2017, 11, 1, 10, 59, 56, 3905773, ZoneOffset.UTC)), dateTimeMicrosecondsName4.getValue());
        Assert.assertEquals("dateTimeMicrosecondsName4", dateTimeMicrosecondsName4.getName());
    }

    @Test
    public void testDateTimeMillisecondsValue() throws Exception {
        final DateTimeValue dateTimeMillisecondsValue1 = (DateTimeValue) DateTimeValue.parserWithMilliseconds(database, "dateTimeMillisecondsName1", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}));
        Assert.assertEquals(Instant.ofEpochMilli(0), dateTimeMillisecondsValue1.getValue());
        Assert.assertEquals("dateTimeMillisecondsName1", dateTimeMillisecondsValue1.getName());

        final DateTimeValue dateTimeMillisecondsValue2 = (DateTimeValue) DateTimeValue.parserWithMilliseconds(database, "dateTimeMillisecondsName2", Optional.empty()).parse(database, null, Unpooled.buffer(8).writeLong(1509532300714L));
        Assert.assertEquals(Instant.ofEpochMilli(1509532300714L), dateTimeMillisecondsValue2.getValue());
        Assert.assertEquals("dateTimeMillisecondsName2", dateTimeMillisecondsValue2.getName());
    }

    @Test
    public void testDateTimeNanosecondsValue() throws Exception {
        final DateTimeValue dateTimeNanosecondsValue1 = (DateTimeValue) DateTimeValue.parserWithNanoseconds(database, "dateTimeNanosecondsName1", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}));
        Assert.assertEquals(Instant.from(ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)), dateTimeNanosecondsValue1.getValue());
        Assert.assertEquals("dateTimeNanosecondsName1", dateTimeNanosecondsValue1.getName());

        final DateTimeValue dateTimeNanosecondsValue2 = (DateTimeValue) DateTimeValue.parserWithNanoseconds(database, "dateTimeNanosecondsName2", Optional.empty()).parse(database, null, Unpooled.buffer(8).writeInt((int) (1509533996L + DateTimeValue.SECONDS_TO_EPOCH)).writeInt(0));
        Assert.assertEquals(Instant.from(ZonedDateTime.of(2017, 11, 1, 10, 59, 56, 0, ZoneOffset.UTC)), dateTimeNanosecondsValue2.getValue());
        Assert.assertEquals("dateTimeNanosecondsName2", dateTimeNanosecondsValue2.getName());

        final DateTimeValue dateTimeNanosecondsValue3 = (DateTimeValue) DateTimeValue.parserWithNanoseconds(database, "dateTimeNanosecondsName3", Optional.empty()).parse(database, null, Unpooled.buffer(8).writeInt((int) (1509533996L + DateTimeValue.SECONDS_TO_EPOCH)).writeInt(34509786));
        Assert.assertEquals(Instant.from(ZonedDateTime.of(2017, 11, 1, 10, 59, 56, 8034935, ZoneOffset.UTC)), dateTimeNanosecondsValue3.getValue());
        Assert.assertEquals("dateTimeNanosecondsName3", dateTimeNanosecondsValue3.getName());
    }

    @Test
    public void testFloat32Value() throws Exception {
        final FloatValue float32Value1 = (FloatValue) FloatValue.parserWith32Bit(database, "float32Name1", Optional.empty()).parse(database, null, Unpooled.buffer(4).writeFloat(0));
        Assert.assertEquals(0f, float32Value1.getValue(), 0);
        Assert.assertEquals("float32Name1", float32Value1.getName());

        final FloatValue float32Value2 = (FloatValue) FloatValue.parserWith32Bit(database, "float32Name2", Optional.empty()).parse(database, null, Unpooled.buffer(4).writeFloat(123.456f));
        Assert.assertEquals(123.456f, float32Value2.getValue(), 0);
        Assert.assertEquals("float32Name2", float32Value2.getName());

        Assert.assertEquals(1.4E-45f, ((FloatValue) FloatValue.parserWith32Bit(database, "float32Name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1.4E-45f, ((FloatValue) FloatValue.parserWith32Bit(database, "float32Name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1.4E-45f, ((FloatValue) FloatValue.parserWith32Bit(database, "float32Name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 1}))).getValue(), 0);
        Assert.assertEquals(1.4E-45f, ((FloatValue) FloatValue.parserWith32Bit(database, "float32Name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{1}))).getValue(), 0);
    }

    @Test
    public void testFloat64Value() throws Exception {
        final FloatValue float64Value1 = (FloatValue) FloatValue.parserWith64Bit(database, "float64Name1", Optional.empty()).parse(database, null, Unpooled.buffer(8).writeDouble(0));
        Assert.assertEquals(0f, float64Value1.getValue(), 0);
        Assert.assertEquals("float64Name1", float64Value1.getName());

        final FloatValue float64Value2 = (FloatValue) FloatValue.parserWith64Bit(database, "float64Name2", Optional.empty()).parse(database, null, Unpooled.buffer(8).writeDouble(123.456));
        Assert.assertEquals(123.456, float64Value2.getValue(), 0);
        Assert.assertEquals("float64Name2", float64Value2.getName());

        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit(database, "float64Name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit(database, "float64Name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit(database, "float64Name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit(database, "float64Name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit(database, "float64Name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit(database, "float64Name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit(database, "float64Name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 1}))).getValue(), 0);
        Assert.assertEquals(4.9E-324, ((FloatValue) FloatValue.parserWith64Bit(database, "float64Name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{1}))).getValue(), 0);
    }

    @Test
    public void testIPv4AddressValue() throws Exception {
        final IPv4AddressValue ipv4AddressValue = (IPv4AddressValue) IPv4AddressValue.parser(database, "ipv4AddressName", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(InetAddress.getByName("127.0.0.1").getAddress()));
        Assert.assertEquals("ipv4AddressName", ipv4AddressValue.getName());
        Assert.assertEquals("127.0.0.1", ipv4AddressValue.getValue().getHostAddress());
    }

    @Test
    public void testIPv6AddressValue() throws Exception {
        final IPv6AddressValue ipv6AddressValue = (IPv6AddressValue) IPv6AddressValue.parser(database, "ipv6AddressName", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(InetAddress.getByName("2001:638:301:11a0:d498:3253:ca5f:3777").getAddress()));
        Assert.assertEquals("ipv6AddressName", ipv6AddressValue.getName());
        Assert.assertEquals("2001:638:301:11a0:d498:3253:ca5f:3777", ipv6AddressValue.getValue().getHostAddress());
    }

    @Test
    public void testMacAddressValue() throws Exception {
        final MacAddressValue macAddressValue = (MacAddressValue) MacAddressValue.parser(database, "macAddressName", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{1, 2, 4, 8, 16, 32}));
        Assert.assertEquals("macAddressName", macAddressValue.getName());
        Assert.assertArrayEquals(new byte[]{1, 2, 4, 8, 16, 32}, macAddressValue.getValue());
    }

    @Test
    public void testOctetArrayValue() throws Exception {
        final OctetArrayValue octetArrayValue = (OctetArrayValue) OctetArrayValue.parser(database, "octetArrayName", Optional.empty()).parse(null, null, Unpooled.wrappedBuffer(new byte[]{1, 2, 4, 8, 16}));
        Assert.assertEquals("octetArrayName", octetArrayValue.getName());
        Assert.assertArrayEquals(new byte[]{1, 2, 4, 8, 16}, octetArrayValue.getValue());
    }

    @Test
    public void testSigned64Value() throws Exception {
        final SignedValue v1 = (SignedValue) SignedValue.parserWith64Bit(database, "name1", Optional.empty()).parse(database, null, Unpooled.buffer(8).writeLong(0));
        Assert.assertEquals(0, v1.getValue(), 0);
        Assert.assertEquals("name1", v1.getName());

        final SignedValue v2 = (SignedValue) SignedValue.parserWith64Bit(database, "name2", Optional.empty()).parse(database, null, Unpooled.buffer(8).writeLong(42));
        Assert.assertEquals(42, v2.getValue(), 0);
        Assert.assertEquals("name2", v2.getName());

        final SignedValue v3 = (SignedValue) SignedValue.parserWith64Bit(database, "name3", Optional.empty()).parse(database, null, Unpooled.buffer(8).writeLong(-42));
        Assert.assertEquals(-42, v3.getValue(), 0);
        Assert.assertEquals("name3", v3.getName());

        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{1}))).getValue(), 0);
    }

    @Test
    public void testSigned32Value() throws Exception {
        final SignedValue v1 = (SignedValue) SignedValue.parserWith32Bit(database, "name1", Optional.empty()).parse(database, null, Unpooled.buffer(4).writeInt(0));
        Assert.assertEquals(0, v1.getValue(), 0);
        Assert.assertEquals("name1", v1.getName());

        final SignedValue v2 = (SignedValue) SignedValue.parserWith32Bit(database, "name2", Optional.empty()).parse(database, null, Unpooled.buffer(4).writeInt(42));
        Assert.assertEquals(42, v2.getValue(), 0);
        Assert.assertEquals("name2", v2.getName());

        final SignedValue v3 = (SignedValue) SignedValue.parserWith32Bit(database, "name3", Optional.empty()).parse(database, null, Unpooled.buffer(4).writeInt(-42));
        Assert.assertEquals(-42, v3.getValue(), 0);
        Assert.assertEquals("name3", v3.getName());

        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith32Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith32Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith32Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith32Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{1}))).getValue(), 0);
    }

    @Test
    public void testSigned16Value() throws Exception {
        final SignedValue v1 = (SignedValue) SignedValue.parserWith16Bit(database, "name1", Optional.empty()).parse(database, null, Unpooled.buffer(2).writeShort((short) 0));
        Assert.assertEquals(0, v1.getValue(), 0);
        Assert.assertEquals("name1", v1.getName());

        final SignedValue v2 = (SignedValue) SignedValue.parserWith16Bit(database, "name2", Optional.empty()).parse(database, null, Unpooled.buffer(2).writeShort((short) 42));
        Assert.assertEquals(42, v2.getValue(), 0);
        Assert.assertEquals("name2", v2.getName());

        final SignedValue v3 = (SignedValue) SignedValue.parserWith16Bit(database, "name3", Optional.empty()).parse(database, null, Unpooled.buffer(2).writeShort((short) -42));
        Assert.assertEquals(-42, v3.getValue(), 0);
        Assert.assertEquals("name3", v3.getName());

        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith16Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 1}))).getValue(), 0);
        Assert.assertEquals(1, ((SignedValue) SignedValue.parserWith16Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{1}))).getValue(), 0);
    }

    @Test
    public void testSigned8Value() throws Exception {
        final SignedValue v1 = (SignedValue) SignedValue.parserWith8Bit(database, "name1", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0}));
        Assert.assertEquals(0, v1.getValue(), 0);
        Assert.assertEquals("name1", v1.getName());

        final SignedValue v2 = (SignedValue) SignedValue.parserWith8Bit(database, "name2", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{42}));
        Assert.assertEquals(42, v2.getValue(), 0);
        Assert.assertEquals("name2", v2.getName());

        final SignedValue v3 = (SignedValue) SignedValue.parserWith8Bit(database, "name3", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{-42}));
        Assert.assertEquals(-42, v3.getValue(), 0);
        Assert.assertEquals("name3", v3.getName());
    }

    @Test
    public void testUnsigned64Value() throws Exception {
        final UnsignedValue v1 = (UnsignedValue) UnsignedValue.parserWith64Bit(database, "name1", Optional.empty()).parse(database, null, Unpooled.buffer(8).writeLong(0));
        Assert.assertEquals(0L, v1.getValue().longValue());
        Assert.assertEquals("name1", v1.getName());

        final UnsignedValue v2 = (UnsignedValue) UnsignedValue.parserWith64Bit(database, "name2", Optional.empty()).parse(database, null, Unpooled.buffer(8).writeLong(42));
        Assert.assertEquals(42L, v2.getValue().longValue());
        Assert.assertEquals("name2", v2.getName());

        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 0, 0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith64Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{1}))).getValue().longValue(), 0);
    }

    @Test
    public void testUnsigned32Value() throws Exception {
        final UnsignedValue v1 = (UnsignedValue) UnsignedValue.parserWith32Bit(database, "name1", Optional.empty()).parse(database, null, Unpooled.buffer(4).writeInt(0));
        Assert.assertEquals(0L, v1.getValue().longValue());
        Assert.assertEquals("name1", v1.getName());

        final UnsignedValue v2 = (UnsignedValue) UnsignedValue.parserWith32Bit(database, "name2", Optional.empty()).parse(database, null, Unpooled.buffer(4).writeInt(42));
        Assert.assertEquals(42L, v2.getValue().longValue());
        Assert.assertEquals("name2", v2.getName());

        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith32Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith32Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith32Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith32Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{1}))).getValue().longValue(), 0);
    }

    @Test
    public void testUnsigned16Value() throws Exception {
        final UnsignedValue v1 = (UnsignedValue) UnsignedValue.parserWith16Bit(database, "name1", Optional.empty()).parse(database, null, Unpooled.buffer(2).writeShort((short) 0));
        Assert.assertEquals(0L, v1.getValue().longValue());
        Assert.assertEquals("name1", v1.getName());

        final UnsignedValue v2 = (UnsignedValue) UnsignedValue.parserWith16Bit(database, "name2", Optional.empty()).parse(database, null, Unpooled.buffer(2).writeShort((short) 42));
        Assert.assertEquals(42L, v2.getValue().longValue());
        Assert.assertEquals("name2", v2.getName());

        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith16Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0, 1}))).getValue().longValue(), 0);
        Assert.assertEquals(1L, ((UnsignedValue) UnsignedValue.parserWith16Bit(database, "name", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{1}))).getValue().longValue(), 0);
    }

    @Test
    public void testUnsigned8Value() throws Exception {
        final UnsignedValue v1 = (UnsignedValue) UnsignedValue.parserWith8Bit(database, "name1", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{0}));
        Assert.assertEquals(0L, v1.getValue().longValue());
        Assert.assertEquals("name1", v1.getName());

        final UnsignedValue v2 = (UnsignedValue) UnsignedValue.parserWith8Bit(database, "name2", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{42}));
        Assert.assertEquals(42L, v2.getValue().longValue());
        Assert.assertEquals("name2", v2.getName());
    }

    @Test
    public void testStringValue() throws Exception {
        final StringValue v1 = (StringValue) StringValue.parser(database, "name1", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer("Hello World".getBytes("UTF-8")));
        Assert.assertEquals("Hello World", v1.getValue());
        Assert.assertEquals("name1", v1.getName());

        final StringValue v2 = (StringValue) StringValue.parser(database, "name2", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer("Foo".getBytes("UTF-8")));
        Assert.assertEquals("Foo", v2.getValue());
        Assert.assertEquals("name2", v2.getName());

        final StringValue v3 = (StringValue) StringValue.parser(database, "name3", Optional.empty()).parse(database, null, Unpooled.wrappedBuffer(new byte[]{}));
        Assert.assertEquals("", v3.getValue());
        Assert.assertEquals("name3", v3.getName());
    }
}
