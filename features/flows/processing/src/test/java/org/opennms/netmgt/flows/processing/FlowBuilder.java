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
package org.opennms.netmgt.flows.processing;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.opennms.integration.api.v1.flows.Flow.Direction;

import org.opennms.netmgt.flows.api.Flow;

public class FlowBuilder {

    private final List<Flow> flows = new ArrayList<>();

    private Integer snmpInterfaceId;
    private Integer inputSnmpInterfaceId;
    private Integer outputSnmpInterfaceId;
    private String application = null;
    private Direction direction = Direction.INGRESS;
    private String srcHostname = null;
    private String dstHostname = null;
    private Integer tos = null;

    public FlowBuilder withSnmpInterfaceId(Integer snmpInterfaceId) {
        this.snmpInterfaceId = snmpInterfaceId;
        return this;
    }

    public FlowBuilder withInputSnmpInterfaceId(int inputSnmpInterfaceId) {
        this.inputSnmpInterfaceId = inputSnmpInterfaceId;
        return this;
    }

    public FlowBuilder withOutputSnmpInterfaceId(int outputSnmpInterfaceId) {
        this.outputSnmpInterfaceId = outputSnmpInterfaceId;
        return this;
    }

    public FlowBuilder withApplication(String application) {
        this.application = application;
        return this;
    }

    public FlowBuilder withDirection(Direction direction) {
        this.direction = Objects.requireNonNull(direction);
        return this;
    }

    public FlowBuilder withHostnames(final String srcHostname, final String dstHostname) {
        this.srcHostname = srcHostname;
        this.dstHostname = dstHostname;
        return this;
    }

    public FlowBuilder withTos(final Integer tos) {
        this.tos = tos;
        return this;
    }

    public FlowBuilder withFlow(Instant firstSwitched, Instant lastSwitched, String sourceIp, int sourcePort, String destIp, int destPort, long numBytes) {
        return withFlow(firstSwitched, firstSwitched, lastSwitched, sourceIp, sourcePort, destIp, destPort, numBytes);
    }

    public FlowBuilder withFlow(Instant firstSwitched, Instant deltaSwitched, Instant lastSwitched, String sourceIp, int sourcePort, String destIp, int destPort, long numBytes) {
        final TestFlow flow = new TestFlow();
        flow.setTimestamp(lastSwitched);
        flow.setFirstSwitched(firstSwitched);
        flow.setDeltaSwitched(deltaSwitched);
        flow.setLastSwitched(lastSwitched);
        flow.setSrcAddr(sourceIp);
        flow.setSrcPort(sourcePort);
        if (this.srcHostname != null) {
            flow.setSrcAddrHostname(this.srcHostname);
        }
        flow.setDstAddr(destIp);
        flow.setDstPort(destPort);
        if (this.dstHostname != null) {
            flow.setDstAddrHostname(this.dstHostname);
        };
        flow.setBytes(numBytes);
        flow.setProtocol(6); // TCP
        if (direction == Direction.INGRESS) {
            flow.setInputSnmp(snmpInterfaceId);
        } else if (direction == Direction.EGRESS) {
            flow.setOutputSnmp(snmpInterfaceId);
        } else if (direction == Direction.UNKNOWN) {
            flow.setInputSnmp(inputSnmpInterfaceId);
            flow.setOutputSnmp(outputSnmpInterfaceId);
        }
        flow.setDirection(direction);
        flow.setTos(tos);
        flows.add(flow);
        return this;
    }

    public List<Flow> build() {
        return flows;
    }

}
