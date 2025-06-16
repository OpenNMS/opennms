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
package org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow9;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.IllegalFlowException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.Netflow9MessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.InvalidProtocolBufferException;

public class FlowTimeoutTest {

    @Test
    public void testWithoutTimeout() throws InvalidProtocolBufferException, IllegalFlowException {

        Iterable<Value<?>> values = ImmutableList.<Value<?>>builder()
                .add(new UnsignedValue("@unixSecs", 0))
                .add(new UnsignedValue("@sysUpTime", 0))
                .add(new UnsignedValue("FIRST_SWITCHED", 123000))
                .add(new UnsignedValue("LAST_SWITCHED", 987000)).build();
        Netflow9MessageBuilder netflow9MessageBuilder = new Netflow9MessageBuilder();

        FlowMessage flowMessage = netflow9MessageBuilder.buildMessage(values, (address -> Optional.empty())).build();
        assertThat(flowMessage.getFirstSwitched().getValue(), is(123000L));
        assertThat(flowMessage.getDeltaSwitched().getValue(), is(123000L));
        assertThat(flowMessage.getLastSwitched().getValue(), is(987000L));

    }

    @Test
    public void testWithActiveTimeout() throws InvalidProtocolBufferException, IllegalFlowException {

        Iterable<Value<?>> values = ImmutableList.<Value<?>>builder()
                .add(new UnsignedValue("@unixSecs", 0))
                .add(new UnsignedValue("@sysUpTime", 0))
                .add(new UnsignedValue("FIRST_SWITCHED", 123000))
                .add(new UnsignedValue("LAST_SWITCHED", 987000))
                .add(new UnsignedValue("IN_BYTES", 10))
                .add(new UnsignedValue("IN_PKTS", 10))
                .add(new UnsignedValue("FLOW_ACTIVE_TIMEOUT", 10))
                .add(new UnsignedValue("FLOW_INACTIVE_TIMEOUT", 300))
                .build();

        Netflow9MessageBuilder netflow9MessageBuilder = new Netflow9MessageBuilder();

        FlowMessage flowMessage = netflow9MessageBuilder.buildMessage(values, (address -> Optional.empty())).build();
        assertThat(flowMessage.getFirstSwitched().getValue(), is(123000L));
        assertThat(flowMessage.getDeltaSwitched().getValue(),  is(987000L - 10000L));
        assertThat(flowMessage.getLastSwitched().getValue(), is(987000L));
    }

    @Test
    public void testWithInactiveTimeout() throws InvalidProtocolBufferException, IllegalFlowException {

        Iterable<Value<?>> values = ImmutableList.<Value<?>>builder()
                .add(new UnsignedValue("@unixSecs", 0))
                .add(new UnsignedValue("@sysUpTime", 0))
                .add(new UnsignedValue("FIRST_SWITCHED", 123000))
                .add(new UnsignedValue("LAST_SWITCHED", 987000))
                .add(new UnsignedValue("IN_BYTES", 0))
                .add(new UnsignedValue("IN_PKTS", 0))
                .add(new UnsignedValue("FLOW_ACTIVE_TIMEOUT", 10))
                .add(new UnsignedValue("FLOW_INACTIVE_TIMEOUT", 300))
                .build();

        Netflow9MessageBuilder netflow9MessageBuilder = new Netflow9MessageBuilder();

        FlowMessage flowMessage = netflow9MessageBuilder.buildMessage(values, (address -> Optional.empty())).build();

        assertThat(flowMessage.getFirstSwitched().getValue(), is(123000L));
        assertThat(flowMessage.getDeltaSwitched().getValue(), is(987000L - 300000L));
        assertThat(flowMessage.getLastSwitched().getValue(), is(987000L));
    }
}
