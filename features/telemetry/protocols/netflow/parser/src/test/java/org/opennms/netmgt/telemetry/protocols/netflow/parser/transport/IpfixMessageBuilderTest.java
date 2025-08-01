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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.transport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ParserBase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.RecordEnrichment;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Semantics;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.FloatValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.ListValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.NullValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.SignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.StringValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UndeclaredValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.google.protobuf.DoubleValue;

public class IpfixMessageBuilderTest {

    /**
     * Validate that the direction on the flow message is set to UNKNOWN if there is no value present
     */
    @Test
    public void verifyDefaultDirectionIsUnknown() {
        RecordEnrichment recordEnrichment = mock(RecordEnrichment.class);
        IpFixMessageBuilder ipFixMessageBuilder = new IpFixMessageBuilder();
        FlowMessage.Builder builder = ipFixMessageBuilder.buildMessage(Collections.emptyList(), recordEnrichment);
        assertThat(builder.getDirection(), equalTo(Direction.UNKNOWN));
    }

    @Test
    public void verifyRawMessage() throws UnknownHostException {
        final RecordEnrichment recordEnrichment = mock(RecordEnrichment.class);
        final IpFixMessageBuilder ipFixMessageBuilder = new IpFixMessageBuilder();
        final Instant now = Instant.now();

        final List<List<Value<?>>> listOfLists = new ArrayList<>();
        listOfLists.add(Lists.newArrayList(new FloatValue("float", 2), new FloatValue("float", 4), new FloatValue("float", 6)));
        listOfLists.add(Lists.newArrayList(new FloatValue("float", 1), new FloatValue("float", 3), new FloatValue("float", 8)));

        final List<Value<?>> values = new ArrayList<>();
        values.add(new BooleanValue("boolean", true));
        values.add(new DateTimeValue("datetime", now));
        values.add(new FloatValue("float", 12.34));
        values.add(new IPv4AddressValue("ipv4address", (Inet4Address) Inet4Address.getByName("10.20.30.40")));
        values.add(new IPv6AddressValue("ipv6address", (Inet6Address) Inet6Address.getByName("::1")));
        values.add(new ListValue("list", Optional.empty(), ListValue.Semantic.ALL_OF, listOfLists));
        values.add(new MacAddressValue("macaddress", new byte[]{11,22,33,44,55,66}));
        values.add(new NullValue("null"));
        values.add(new OctetArrayValue("octetarray", Optional.empty(), new byte[]{11,22,33,44,55,66}));
        values.add(new SignedValue("signed", -1234));
        values.add(new StringValue("string", "string"));
        values.add(new UndeclaredValue(1, new byte[]{11,22,33,44,55,66}));
        values.add(new UnsignedValue("unsigned", 1234));

        final FlowMessage.Builder builder = ipFixMessageBuilder.buildMessage(values, recordEnrichment);
        for (final Value<?> value : values) {
            final TransportValueVisitor transportValueVisitor = new TransportValueVisitor();
            value.visit(transportValueVisitor);
            builder.addRawMessage(transportValueVisitor.build());
        }
        var list = builder.getRawMessageList();

        Assert.assertEquals(true, list.stream()
                .filter(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::hasBoolean)
                .findFirst()
                .get()
                .getBoolean()
                .getBool()
                .getValue());

        Assert.assertEquals(now.toEpochMilli(), list.stream()
                .filter(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::hasDatetime)
                .findFirst()
                .get()
                .getDatetime()
                .getUint64()
                .getValue());

        Assert.assertEquals(12.34D, list.stream()
                .filter(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::hasFloat)
                .findFirst()
                .get()
                .getFloat()
                .getDouble()
                .getValue(), 0.0001);

        Assert.assertEquals("10.20.30.40", list.stream()
                .filter(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::hasIpv4Address)
                .findFirst()
                .get()
                .getIpv4Address()
                .getString()
                .getValue());

        Assert.assertEquals("0000:0000:0000:0000:0000:0000:0000:0001", list.stream()
                .filter(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::hasIpv6Address)
                .findFirst()
                .get()
                .getIpv6Address()
                .getString()
                .getValue());

        Assert.assertEquals("0b16212c3742", list.stream()
                .filter(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::hasMacaddress)
                .findFirst()
                .get()
                .getMacaddress()
                .getString()
                .getValue());

        Assert.assertEquals(org.opennms.netmgt.telemetry.protocols.netflow.transport.NullValue.newBuilder().build(), list.stream()
                .filter(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::hasNull)
                .findFirst()
                .get()
                .getNull());

        Assert.assertEquals(ByteString.copyFrom(new byte[]{11,22,33,44,55,66}), list.stream()
                .filter(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::hasOctetarray)
                .findFirst()
                .get()
                .getOctetarray()
                .getBytes()
                .getValue());

        Assert.assertEquals(-1234, list.stream()
                .filter(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::hasSigned)
                .findFirst()
                .get()
                .getSigned()
                .getInt64()
                .getValue());

        Assert.assertEquals("string", list.stream()
                .filter(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::hasString)
                .findFirst()
                .get()
                .getString()
                .getString()
                .getValue());

        Assert.assertEquals(ByteString.copyFrom(new byte[]{11,22,33,44,55,66}), list.stream()
                .filter(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::hasUndeclared)
                .findFirst()
                .get()
                .getUndeclared()
                .getBytes()
                .getValue());

        Assert.assertEquals(1234, list.stream()
                .filter(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::hasUnsigned)
                .findFirst()
                .get()
                .getUnsigned()
                .getUint64()
                .getValue());

        Assert.assertEquals(2, list.stream()
                .filter(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::hasList)
                .findFirst().get().getList().getListList().size());

        Assert.assertEquals(12D, list.stream()
                .filter(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::hasList)
                .findFirst()
                .get()
                .getList()
                .getListList()
                .get(0)
                .getValueList()
                .stream()
                .map(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::getFloat)
                .map(org.opennms.netmgt.telemetry.protocols.netflow.transport.FloatValue::getDouble)
                .mapToDouble(DoubleValue::getValue).sum(), 0.0001);

        Assert.assertEquals(12D, list.stream()
                .filter(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::hasList)
                .findFirst()
                .get()
                .getList()
                .getListList()
                .get(1)
                .getValueList()
                .stream()
                .map(org.opennms.netmgt.telemetry.protocols.netflow.transport.Value::getFloat)
                .map(org.opennms.netmgt.telemetry.protocols.netflow.transport.FloatValue::getDouble)
                .mapToDouble(DoubleValue::getValue).sum(), 0.0001);
    }

}
