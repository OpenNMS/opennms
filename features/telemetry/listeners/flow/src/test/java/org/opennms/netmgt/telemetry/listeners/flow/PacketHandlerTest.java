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

package org.opennms.netmgt.telemetry.listeners.flow;

import java.net.Inet6Address;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.bson.RawBsonDocument;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.telemetry.listeners.flow.ie.RecordProvider;
import org.opennms.netmgt.telemetry.listeners.flow.ie.Semantics;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.FloatValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.ListValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.SignedValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.StringValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.UnsignedValue;

import com.google.common.primitives.UnsignedLong;

public class PacketHandlerTest {

    @Test
    public void test() throws Exception {
        final RecordProvider.Record record = new RecordProvider.Record(0, 0, 3, Arrays.asList(
                new StringValue("name1", Optional.empty(), "my value 1"),
                new UnsignedValue("name2", Optional.of(Semantics.TOTAL_COUNTER), UnsignedLong.valueOf(23)),
                new IPv6AddressValue("name3", Optional.of(Semantics.IDENTIFIER), (Inet6Address) Inet6Address.getByName("::1")),
                new DateTimeValue("name4", Optional.empty(), Instant.ofEpochMilli(12345678)),
                new ListValue("name5", Optional.empty(), ListValue.Semantic.EXACTLY_ONE_OF, Arrays.asList(
                        Arrays.asList(
                                new SignedValue("name5t0e0", Optional.empty(), 5),
                                new SignedValue("name5t0e1", Optional.empty(), 6),
                                new SignedValue("name5t0e2", Optional.empty(), 7)
                        ),
                        Arrays.asList(
                                new SignedValue("name5t1e0", Optional.empty(), 5),
                                new SignedValue("name5t1e1", Optional.empty(), 6),
                                new SignedValue("name5t1e2", Optional.empty(), 7)
                        ),
                        Arrays.asList(
                                new BooleanValue("name5t2e0", Optional.empty(), true),
                                new BooleanValue("name5t2e1", Optional.empty(), false)
                        ),
                        Arrays.asList(
                                new ListValue("name5t3e0", Optional.empty(), ListValue.Semantic.ALL_OF, Arrays.asList(
                                        Arrays.asList(
                                                new FloatValue("name5t3e0t0e0", Optional.empty(), 1.5),
                                                new FloatValue("name5t3e0t0e1", Optional.empty(), 1.0 / 3.0)
                                        )
                                )),
                                new ListValue("name5t3e1", Optional.empty(), ListValue.Semantic.NONE_OF, Arrays.asList())
                        ))
                )
        ));

        final ByteBuffer output = PacketHandler.serialize(Protocol.IPFIX, record);

        final RawBsonDocument bson = new RawBsonDocument(output.array());
        System.out.println(bson.toString());

        Assert.assertEquals("my value 1", bson.getDocument("elements").getDocument("name1").getString("v").getValue());
        Assert.assertEquals(23, bson.getDocument("elements").getDocument("name2").getInt64("v").getValue());
        Assert.assertEquals(Semantics.IDENTIFIER.ordinal(), bson.getDocument("elements").getDocument("name3").getInt32("s").getValue());
    }
}
