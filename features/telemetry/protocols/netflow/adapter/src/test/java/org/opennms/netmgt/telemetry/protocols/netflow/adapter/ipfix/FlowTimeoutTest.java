/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.ipfix;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.Instant;
import java.util.Optional;

import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.IllegalFlowException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.IpFixMessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.InvalidProtocolBufferException;

public class FlowTimeoutTest {

    @Test
    public void testWithoutTimeout() throws InvalidProtocolBufferException, IllegalFlowException {

        Iterable<Value<?>> values = ImmutableList.<Value<?>>builder()
                .add(new DateTimeValue("flowStartSeconds", Instant.ofEpochSecond(123)))
                .add(new DateTimeValue("flowEndSeconds", Instant.ofEpochSecond(987)))
                .build();

        final IpFixMessageBuilder ipFixMessageBuilder = new IpFixMessageBuilder();
        FlowMessage flowMessage = ipFixMessageBuilder.buildMessage(values, (address -> Optional.empty())).build();

        assertThat(flowMessage.getFirstSwitched().getValue(), is(123000L));
        assertThat(flowMessage.getDeltaSwitched().getValue(), is(123000L)); // Timeout is same as first
        assertThat(flowMessage.getLastSwitched().getValue(), is(987000L));
    }

    @Test
    public void testWithActiveTimeout() throws InvalidProtocolBufferException, IllegalFlowException {

        Iterable<Value<?>> values = ImmutableList.<Value<?>>builder()
                .add(new DateTimeValue("flowStartSeconds", Instant.ofEpochSecond(123)))
                .add(new DateTimeValue("flowEndSeconds", Instant.ofEpochSecond(987)))
                .add(new UnsignedValue("octetDeltaCount", 10))
                .add(new UnsignedValue("packetDeltaCount", 10))
                .add(new UnsignedValue("flowActiveTimeout", 10))
                .add(new UnsignedValue("flowInactiveTimeout", 300))
                .build();


        final IpFixMessageBuilder ipFixMessageBuilder = new IpFixMessageBuilder();
        FlowMessage flowMessage = ipFixMessageBuilder.buildMessage(values, (address -> Optional.empty())).build();

        assertThat(flowMessage.getFirstSwitched().getValue(), is(123000L));
        assertThat(flowMessage.getDeltaSwitched().getValue(), is(987000L - 10000L));
        assertThat(flowMessage.getLastSwitched().getValue(), is(987000L));
    }

    @Test
    public void testWithInactiveTimeout() throws InvalidProtocolBufferException, IllegalFlowException {

        Iterable<Value<?>> values = ImmutableList.<Value<?>>builder()
                .add(new DateTimeValue("flowStartSeconds", Instant.ofEpochSecond(123)))
                .add(new DateTimeValue("flowEndSeconds", Instant.ofEpochSecond(987)))
                .add(new UnsignedValue("octetDeltaCount", 0))
                .add(new UnsignedValue("packetDeltaCount", 0))
                .add(new UnsignedValue("flowActiveTimeout", 10))
                .add(new UnsignedValue("flowInactiveTimeout", 300))
                .build();
        final IpFixMessageBuilder ipFixMessageBuilder = new IpFixMessageBuilder();
        FlowMessage flowMessage = ipFixMessageBuilder.buildMessage(values, (address -> Optional.empty())).build();


        assertThat(flowMessage.getFirstSwitched().getValue(), is(123000L));
        assertThat(flowMessage.getDeltaSwitched().getValue(), is(987000L - 300000L));
        assertThat(flowMessage.getLastSwitched().getValue(), is(987000L));
    }


    @Test
    public void testFirstLastSwitchedValues() throws InvalidProtocolBufferException, IllegalFlowException {

        Iterable<Value<?>> values = ImmutableList.<Value<?>>builder()
                .add(new DateTimeValue("flowStartSeconds", Instant.ofEpochSecond(123)))
                .add(new DateTimeValue("flowEndSeconds", Instant.ofEpochSecond(987)))
                .build();

        IpFixMessageBuilder ipFixMessageBuilder = new IpFixMessageBuilder();
        FlowMessage flowMessage = ipFixMessageBuilder.buildMessage(values, (address -> Optional.empty())).build();

        assertThat(flowMessage.getFirstSwitched().getValue(), is(123000L));
        assertThat(flowMessage.getDeltaSwitched().getValue(), is(123000L));
        assertThat(flowMessage.getLastSwitched().getValue(), is(987000L));

        values = ImmutableList.<Value<?>>builder()
                .add(new DateTimeValue("systemInitTimeMilliseconds", Instant.ofEpochMilli(100000)))
                .add(new UnsignedValue("flowStartSysUpTime", 2000000))
                .add(new UnsignedValue("flowEndSysUpTime", 4000000))
                .build();
        ipFixMessageBuilder = new IpFixMessageBuilder();
        flowMessage = ipFixMessageBuilder.buildMessage(values, (address -> Optional.empty())).build();

        assertThat(flowMessage.getFirstSwitched().getValue(), is(2000000L + 100000L));
        assertThat(flowMessage.getDeltaSwitched().getValue(), is(2000000L + 100000L));
        assertThat(flowMessage.getLastSwitched().getValue(), is(4100000L));
    }
}
