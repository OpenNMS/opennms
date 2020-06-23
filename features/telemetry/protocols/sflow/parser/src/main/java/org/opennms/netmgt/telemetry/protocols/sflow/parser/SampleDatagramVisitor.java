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

package org.opennms.netmgt.telemetry.protocols.sflow.parser;

import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.Address;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.AppInitiator;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.AppOperation;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.AppParentContent;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.AppTarget;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.CounterRecord;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.CountersSample;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.CountersSampleExpanded;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.Extended80211Aggregation;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.Extended80211Payload;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.Extended80211Rx;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.Extended80211Tx;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedBstEgressQueue;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedDecapsulateEgress;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedDecapsulateIngress;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedGateway;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedIpv4TunnelEgress;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedIpv4TunnelIngress;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedIpv6TunnelEgress;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedIpv6TunnelIngress;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedL2TunnelEgress;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedL2TunnelIngress;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedMpls;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedMplsFtn;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedMplsLdpFec;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedMplsTunnel;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedMplsVc;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedNat;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedProxyRequest;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedProxySocketIpv4;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedProxySocketIpv6;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedRouter;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedSocketIpv4;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedSocketIpv6;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedSwitch;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedUrl;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedUser;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedVlantunnel;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedVniEgress;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.ExtendedVniIngress;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.FlowRecord;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.FlowSample;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.FlowSampleExpanded;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.HttpRequest;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.IpV4;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.IpV6;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.NextHop;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.SampleDatagram;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.SampleDatagramType;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.SampleDatagramV5;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.SampleRecord;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.SampledEthernet;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.SampledHeader;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.SampledIpv4;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.SampledIpv6;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.headers.Inet4Header;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.headers.Inet6Header;

public interface SampleDatagramVisitor {
    default void accept(SampleDatagram datagram) { }

    default void accept(SampleDatagramType sampleDatagramType) { }

    default void accept(SampleDatagramV5 sampleDatagramV5) { }

    default void accept(SampleRecord sampleRecord) { }

    default void accept(FlowRecord flowRecord) { }

    default void accept(CounterRecord counterRecord) { }

    default void accept(FlowSample flowSample) { }

    default void accept(SampledHeader sampledHeader) { }

    default void accept(SampledEthernet sampledEthernet) { }

    default void accept(SampledIpv4 sampledIpv4) { }

    default void accept(SampledIpv6 sampledIpv6) { }

    default void accept(Inet4Header inet4Header) { }

    default void accept(Inet6Header inet6Header) { }

    default void accept(AppInitiator appInitiator) { }

    default void accept(AppOperation appOperation) { }

    default void accept(AppParentContent appParentContent) { }

    default void accept(AppTarget appTarget) { }

    default void accept(Extended80211Aggregation extended80211Aggregation) { }

    default void accept(Extended80211Payload extended80211Payload) { }

    default void accept(Extended80211Rx extended80211Rx) { }

    default void accept(Extended80211Tx extended80211Tx) { }

    default void accept(IpV4 ipV4) { }

    default void accept(IpV6 ipV6) { }

    default void accept(Address address) { }

    default void accept(NextHop nextHop) { }

    default void accept(ExtendedGateway extendedGateway) { }

    default void accept(ExtendedMpls extendedMpls) { }

    default void accept(ExtendedRouter extendedRouter) { }

    default void accept(ExtendedNat extendedNat) { }

    default void accept(ExtendedIpv4TunnelEgress extendedIpv4TunnelEgress) { }

    default void accept(ExtendedSocketIpv6 extendedSocketIpv6) { }

    default void accept(ExtendedSwitch extendedSwitch) { }

    default void accept(ExtendedL2TunnelEgress extendedL2TunnelEgress) { }

    default void accept(ExtendedVniIngress extendedVniIngress) { }

    default void accept(ExtendedVniEgress extendedVniEgress) { }

    default void accept(ExtendedSocketIpv4 extendedSocketIpv4) { }

    default void accept(ExtendedMplsFtn extendedMplsFtn) { }

    default void accept(ExtendedUser extendedUser) { }

    default void accept(ExtendedDecapsulateEgress extendedDecapsulateEgress) { }

    default void accept(ExtendedDecapsulateIngress extendedDecapsulateIngress) { }

    default void accept(ExtendedIpv6TunnelIngress extendedIpv6TunnelIngress) { }

    default void accept(ExtendedIpv6TunnelEgress extendedIpv6TunnelEgress) { }

    default void accept(ExtendedIpv4TunnelIngress extendedIpv4TunnelIngress) { }

    default void accept(ExtendedMplsLdpFec extendedMplsLdpFec) { }

    default void accept(ExtendedMplsTunnel extendedMplsTunnel) { }

    default void accept(ExtendedMplsVc extendedMplsVc) { }

    default void accept(FlowSampleExpanded flowSampleExpanded) { }

    default void accept(ExtendedVlantunnel extendedVlantunnel) { }

    default void accept(CountersSampleExpanded countersSampleExpanded) { }

    default void accept(ExtendedProxySocketIpv6 extendedProxySocketIpv6) { }

    default void accept(ExtendedProxySocketIpv4 extendedProxySocketIpv4) { }

    default void accept(ExtendedProxyRequest extendedProxyRequest) { }

    default void accept(HttpRequest httpRequest) { }

    default void accept(ExtendedL2TunnelIngress extendedL2TunnelIngress) { }

    default void accept(CountersSample countersSample) { }

    default void accept(ExtendedUrl extendedUrl) { }

    default void accept(ExtendedBstEgressQueue extendedBstEgressQueue) { }
}
