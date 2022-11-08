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
