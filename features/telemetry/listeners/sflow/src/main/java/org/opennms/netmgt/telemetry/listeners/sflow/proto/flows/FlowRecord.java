/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.nio.ByteBuffer;
import java.util.Map;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.Opaque;

import com.google.common.collect.ImmutableMap;

// struct flow_record {
//    data_format flow_format;         /* The format of sflow_data */
//    opaque flow_data<>;              /* Flow data uniquely defined
//                                        by the flow_format. */
// };

public class FlowRecord extends Record<FlowData> {
    private static Map<DataFormat, Opaque.Parser<FlowData>> flowDataFormats = ImmutableMap.<DataFormat, Opaque.Parser<FlowData>>builder()
            .put(DataFormat.from(0, 1), SampledHeader::new)
            .put(DataFormat.from(0, 2), SampledEthernet::new)
            .put(DataFormat.from(0, 3), SampledIpv4::new)
            .put(DataFormat.from(0, 4), SampledIpv6::new)
            .put(DataFormat.from(0, 1001), ExtendedSwitch::new)
            .put(DataFormat.from(0, 1002), ExtendedRouter::new)
            .put(DataFormat.from(0, 1003), ExtendedGateway::new)
            .put(DataFormat.from(0, 1004), ExtendedUser::new)
            .put(DataFormat.from(0, 1005), ExtendedUrl::new)
            .put(DataFormat.from(0, 1006), ExtendedMpls::new)
            .put(DataFormat.from(0, 1007), ExtendedNat::new)
            .put(DataFormat.from(0, 1008), ExtendedMplsTunnel::new)
            .put(DataFormat.from(0, 1009), ExtendedMplsVc::new)
            .put(DataFormat.from(0, 1010), ExtendedMplsFtn::new)
            .put(DataFormat.from(0, 1011), ExtendedMplsLdpFec::new)
            .put(DataFormat.from(0, 1012), ExtendedVlantunnel::new)
            .put(DataFormat.from(0, 1013), Extended80211Payload::new)
            .put(DataFormat.from(0, 1014), Extended80211Rx::new)
            .put(DataFormat.from(0, 1015), Extended80211Tx::new)
            .put(DataFormat.from(0, 1016), Extended80211Aggregation::new)
            .put(DataFormat.from(0, 1021), ExtendedL2TunnelEgress::new)
            .put(DataFormat.from(0, 1022), ExtendedL2TunnelIngress::new)
            .put(DataFormat.from(0, 1023), ExtendedIpv4TunnelEgress::new)
            .put(DataFormat.from(0, 1024), ExtendedIpv4TunnelIngress::new)
            .put(DataFormat.from(0, 1025), ExtendedIpv6TunnelEgress::new)
            .put(DataFormat.from(0, 1026), ExtendedIpv6TunnelIngress::new)
            .put(DataFormat.from(0, 1027), ExtendedDecapsulateEgress::new)
            .put(DataFormat.from(0, 1028), ExtendedDecapsulateIngress::new)
            .put(DataFormat.from(0, 1029), ExtendedVniEgress::new)
            .put(DataFormat.from(0, 1030), ExtendedVniIngress::new)
            .put(DataFormat.from(4413, 1), ExtendedBstEgressQueue::new)
            .put(DataFormat.from(0, 2100), ExtendedSocketIpv4::new)
            .put(DataFormat.from(0, 2101), ExtendedSocketIpv6::new)
            .put(DataFormat.from(0, 2206), HttpRequest::new)
            .put(DataFormat.from(0, 2207), ExtendedProxyRequest::new)
            .put(DataFormat.from(0, 2102), ExtendedProxySocketIpv4::new)
            .put(DataFormat.from(0, 2103), ExtendedProxySocketIpv6::new)
            .put(DataFormat.from(0, 2202), AppOperation::new)
            .put(DataFormat.from(0, 2203), AppParentContent::new)
            .put(DataFormat.from(0, 2204), AppInitiator::new)
            .put(DataFormat.from(0, 2205), AppTarget::new)
            .build();

    public FlowRecord(final ByteBuffer buffer) throws InvalidPacketException {
        super(buffer, flowDataFormats);
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        if (data.value != null) {
            this.data.value.writeBson(bsonWriter);
        } else {
            bsonWriter.writeNull();
        }
    }
}
