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
