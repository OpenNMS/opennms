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
package org.opennms.smoketest.telemetry;

import java.util.List;

import org.opennms.netmgt.flows.elastic.NetflowVersion;

import com.google.common.collect.Lists;

public interface Packets {
    // Flow Packets
    FlowPacket Netflow5 = new FlowPacket(NetflowVersion.V5, Payload.resource("/payloads/flows/netflow5.dat"), 2);
    FlowPacket Netflow9 = new FlowPacket(NetflowVersion.V9, Payload.resource("/payloads/flows/netflow9.dat"), 7);
    FlowPacket Ipfix = new FlowPacket(NetflowVersion.IPFIX, Payload.resource("/payloads/flows/ipfix.dat"), 2);
    FlowPacket SFlow = new FlowPacket(NetflowVersion.SFLOW, Payload.resource("/payloads/flows/sflow.dat"), 5);

    // Other Packets
    Packet NXOS = new Packet(Payload.resource("/payloads/telemetry/cisco-nxos-proto.raw"));
    Packet JTI = new Packet(Payload.resource("/payloads/telemetry/jti-proto.raw"));
    Packet BMP = new Packet(Payload.resource("/payloads/telemetry/bmp-proto.raw"));

    static List<FlowPacket> getFlowPackets() {
        return Lists.newArrayList(Netflow5, Netflow9, Ipfix, SFlow);
    }
}
