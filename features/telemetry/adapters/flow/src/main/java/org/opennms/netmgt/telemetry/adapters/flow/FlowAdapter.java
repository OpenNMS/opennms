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

package org.opennms.netmgt.telemetry.adapters.flow;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.RawBsonDocument;
import org.opennms.netmgt.flows.api.FlowType;
import org.opennms.netmgt.flows.api.NetflowDocument;
import org.opennms.netmgt.telemetry.adapters.AbstractFlowAdapter;
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessage;

public class FlowAdapter extends AbstractFlowAdapter<BsonDocument> {

    private static Optional<BsonValue> get(final BsonDocument doc, final String... path) {
        BsonValue value = doc;
        for (final String p : path) {
            value = value.asDocument().get(p);
            if (value == null) {
                return Optional.empty();
            }
        }

        return Optional.of(value);
    }

    @Override
    protected BsonDocument parse(final TelemetryMessage message) {
        return new RawBsonDocument(message.getByteArray());
    }

    @Override
    protected List<NetflowDocument> convert(final BsonDocument packet) {
        final NetflowDocument document = new NetflowDocument();

        switch (get(packet, "version").get().asInt32().getValue()) {
            case 5:
                document.setFlowType(FlowType.NETFLOW_5);
                // TODO: Implement
                break;

            case 9:
                document.setFlowType(FlowType.NETFLOW_9);
                convertNetflow9(packet, document);
                break;

            case 10:
                document.setFlowType(FlowType.IPFIX);
                convertIpfix(packet, document);
                break;

            default:
                throw new RuntimeException("Illegal flow version");
        }

        document.setTimestamp(get(packet, "exportTime").get().asTimestamp().getTime() * 1000);
        document.setVersion(get(packet, "version").get().asInt32().getValue());

        return Collections.singletonList(document);
    }

    private void convertNetflow9(final BsonDocument packet, final NetflowDocument document) {

//        boolean swapped = get(packet, "elements", "DIRECTION", "v").map(BsonValue::asInt32).map(BsonInt32::getValue).orElse(0) == 1;

        // TODO: The nr of records in pkt is totally irrelevant
//        document.setFlowRecords();

        // TODO: This is contained in v9 header and not transported right now
//            document.setSysUptime(document.getTimestamp() - v.asInt32().getValue());

        // TODO: The seq nr is totally irrelevant
//        document.setFlowSequenceNumber(netflowPacket.getFlowSequence());

        // TODO: ENGINE_ID
        get(packet, "elements", "ENGINE_TYPE", "v").ifPresent(v -> {
            document.setEngineType((int) v.asInt64().getValue());
        });

        // TODO: v5 samplingInterval is { method:2, interval:14} for real - SAMPLING_ALGORITHM
        get(packet, "elements", "SAMPLING_INTERVAL", "v").ifPresent(v -> {
            // TODO: also use samplingMethod
            document.setSamplingInterval((int) v.asInt64().getValue());
        });

        // TODO: IPV6_SRC_ADDR
        get(packet, "elements", "IPV4_SRC_ADDR", "v").ifPresent(v -> {
            document.setIpv4SourceAddress(v.asString().getValue());
        });
        get(packet, "elements", "L4_SRC_PORT", "v").ifPresent(v -> {
            document.setSourcePort((int) v.asInt64().getValue());
        });
        get(packet, "elements", "IPV4_DST_ADDR", "v").ifPresent(v -> {
            document.setIpv4DestAddress(v.asString().getValue());
        });
        get(packet, "elements", "L4_DST_PORT", "v").ifPresent(v -> {
            document.setSourcePort((int) v.asInt64().getValue());
        });

        // TODO: Also BGP_IPV4_NEXT_HOP, IPV6_NEXT_HOP, BPG_IPV6_NEXT_HOP
        // TODO: Next Hop and switched is strange?
        get(packet, "elements", "IPV4_NEXT_HOP", "v").ifPresent(v -> {
            document.setIpv4NextHopAddress(v.asString().getValue());
        });

        get(packet, "elements", "INPUT_SNMP", "v").ifPresent(v -> {
            document.setInputSnmpInterfaceIndex((int) v.asInt64().getValue());
        });
        get(packet, "elements", "OUTPUT_SNMP", "v").ifPresent(v -> {
            document.setOutputSnmpInterfaceIndex((int) v.asInt64().getValue());
        });
        get(packet, "elements", "IN_BYTES", "v").ifPresent(v -> {
            document.setInBytes(v.asInt64().getValue());
        });
        get(packet, "elements", "IN_PKTS", "v").ifPresent(v -> {
            document.setInPackets(v.asInt64().getValue());
        });

        // TODO: By flowStartSysUpTime, flowStart*, flowStartDelta*, flowDuration*, minFlowStart*,
//        document.setFirst(record.getFirst());
//        document.setLast(record.getLast());

        get(packet, "elements", "TCP_FLAGS", "v").ifPresent(v -> {
            document.setTcpFlags((int) v.asInt64().getValue());
        });

        get(packet, "elements", "PROTOCOL", "v").ifPresent(v -> {
            document.setIpProtocol((int) v.asInt64().getValue());
        });

        get(packet, "elements", "SRC_TOS", "v").ifPresent(v -> {
            document.setTos(v.asInt32().getValue());
        });

        get(packet, "elements", "SRC_AS", "v").ifPresent(v -> {
            document.setSourceAutonomousSystemNumber((int) v.asInt64().getValue());
        });
        get(packet, "elements", "DST_AS", "v").ifPresent(v -> {
            document.setDestAutonomousSystemNumber((int) v.asInt64().getValue());
        });

        // TODO: IPV6_SRC_MASK
        get(packet, "elements", "SRC_MASK", "v").ifPresent(v -> {
            document.setSourceMask((int) v.asInt64().getValue());
        });
        // TODO: IPV6_DST_MASK
        get(packet, "elements", "DST_MASK", "v").ifPresent(v -> {
            document.setDestMask((int) v.asInt64().getValue());
        });
    }

    private void convertIpfix(final BsonDocument packet, final NetflowDocument document) {

//        boolean swapped = get(packet, "elements", "flowDirection", "v").map(BsonValue::asInt32).map(BsonInt32::getValue).orElse(0) == 1;

        // TODO: The nr of records in pkt is totally irrelevant
//        document.setFlowRecords();

        get(packet, "elements", "systemInitTimeMilliseconds", "v", "epoch").ifPresent(v -> {
            document.setSysUptime(document.getTimestamp() - v.asInt64().getValue());
        });

        // TODO: The seq nr is totally irrelevant
//        document.setFlowSequenceNumber(netflowPacket.getFlowSequence());

        get(packet, "elements", "engineType", "v").ifPresent(v -> {
            document.setEngineType((int) v.asInt64().getValue());
        });

        // TODO: v5 samplingInterval is { method:2, interval:14} for real
        get(packet, "elements", "samplingInterval", "v").ifPresent(v -> {
            // TODO: also use samplingMethod
            document.setSamplingInterval((int) v.asInt64().getValue());
        });

        get(packet, "elements", "sourceIPv4Address", "v").ifPresent(v -> {
            document.setIpv4SourceAddress(v.asString().getValue());
        });
        get(packet, "elements", "sourceTransportPort", "v").ifPresent(v -> {
            document.setSourcePort((int) v.asInt64().getValue());
        });
        get(packet, "elements", "destinationIPv4Address", "v").ifPresent(v -> {
            document.setIpv4DestAddress(v.asString().getValue());
        });
        get(packet, "elements", "destinationTransportPort", "v").ifPresent(v -> {
            document.setDestPort((int) v.asInt64().getValue());
        });

        // TODO: Also bgpNextHopIPv4Address
        // TODO: Next Hop and switched is strange? On A -> B -> C, we are B - should we switch A and C?
        get(packet, "elements", "ipNextHopIPv4Address", "v").ifPresent(v -> {
            document.setIpv4NextHopAddress(v.asString().getValue());
        });

        get(packet, "elements", "ingressInterface", "v").ifPresent(v -> {
            document.setInputSnmpInterfaceIndex((int) v.asInt64().getValue());
        });
        get(packet, "elements", "egressInterface", "v").ifPresent(v -> {
            document.setOutputSnmpInterfaceIndex((int) v.asInt64().getValue());
        });

        // TODO: octetTotalCount, octetDeltaSumOfSquares, octetTotalSumOfSquares, initiatorOctets, responderOctets, layer2OctetDeltaCount, layer2OctetTotalCount, flowSelectedOctetDeltaCount, transportOctetDeltaCount
        get(packet, "elements", "octetDeltaCount", "v").ifPresent(v -> {
            document.setInBytes(v.asInt64().getValue());
        });
        // TODO: packetTotalCount, initiatorPackets, responderPackets, flowSelectedPacketDeltaCount, transportPacketDeltaCount
        get(packet, "elements", "packetDeltaCount", "v").ifPresent(v -> {
            document.setInPackets(v.asInt64().getValue());
        });

        // TODO: By flowStartSysUpTime, flowStart*, flowStartDelta*, flowDuration*, minFlowStart*,
//        document.setFirst(record.getFirst());
//        document.setLast(record.getLast());

        get(packet, "elements", "tcpControlBits", "v").ifPresent(v -> {
            document.setTcpFlags((int) v.asInt64().getValue());
        });

        get(packet, "elements", "protocolIdentifier", "v").ifPresent(v -> {
            document.setIpProtocol((int) v.asInt64().getValue());
        });

        get(packet, "elements", "ipClassOfService", "v").ifPresent(v -> {
            document.setTos((int) v.asInt64().getValue());
        });

        get(packet, "elements", "bgpSourceAsNumber", "v").ifPresent(v -> {
            document.setSourceAutonomousSystemNumber((int) v.asInt64().getValue());
        });
        get(packet, "elements", "bgpDestinationAsNumber", "v").ifPresent(v -> {
            document.setDestAutonomousSystemNumber((int) v.asInt64().getValue());
        });

        get(packet, "elements", "sourceIPv4PrefixLength", "v").ifPresent(v -> {
            document.setSourceMask((int) v.asInt64().getValue());
        });
        get(packet, "elements", "destinationIPv4PrefixLength", "v").ifPresent(v -> {
            document.setDestMask((int) v.asInt64().getValue());
        });
    }
}
