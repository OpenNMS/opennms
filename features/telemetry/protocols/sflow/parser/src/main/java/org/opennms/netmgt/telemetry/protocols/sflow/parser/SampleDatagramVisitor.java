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
