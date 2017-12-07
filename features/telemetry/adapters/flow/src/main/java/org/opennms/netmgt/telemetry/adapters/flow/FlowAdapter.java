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
        // TODO: The nr of records in pkt is totally irrelevant
//        document.setFlowRecords();

        // TODO: This is contained in v9 header and not transported right now
//            document.setSysUptime(document.getTimestamp() - v.asInt32().getValue());

        // TODO: The seq nr is totally irrelevant
//        document.setFlowSequenceNumber(netflowPacket.getFlowSequence());

//        get(packet, "elements", "engineType").ifPresent(v -> {
//            document.setEngineType(v.asInt32().getValue());
//        });
//
//        // TODO: v5 samplingInterval is { method:2, interval:14} for real
//        get(packet, "elements", "samplingInterval").ifPresent(v -> {
//            // TODO: also use samplingMethod
//            document.setSamplingInterval(v.asInt32().getValue());
//        });
//
//        get(packet, "elements", "sourceIPv4Address").ifPresent(v -> {
//            document.setIpv4SourceAddress(v.asString().getValue());
//        });
//        get(packet, "elements", "sourceTransportPort").ifPresent(v -> {
//            document.setSourcePort(v.asInt32().getValue());
//        });
//
//        get(packet, "elements", "destinationIPv4Address").ifPresent(v -> {
//            document.setIpv4DestAddress(v.asString().getValue());
//        });
//        get(packet, "elements", "destinationTransportPort").ifPresent(v -> {
//            document.setDestPort(v.asInt32().getValue());
//        });
//
//        // TODO: Also bgpNextHopIPv4Address
//        get(packet, "elements", "ipNextHopIPv4Address").ifPresent(v -> {
//            document.setIpv4NextHopAddress(v.asString().getValue());
//        });
//
//        get(packet, "elements", "ingressInterface").ifPresent(v -> {
//            document.setInputSnmpInterfaceIndex(v.asInt32().getValue());
//        });
//        get(packet, "elements", "egressInterface").ifPresent(v -> {
//            document.setOutputSnmpInterfaceIndex(v.asInt32().getValue());
//        });
//
//        get(packet, "elements", "octetDeltaCount").ifPresent(v -> {
//            document.setInBytes(v.asInt32().getValue());
//        });
//        get(packet, "elements", "packetDeltaCount").ifPresent(v -> {
//            document.setInPackets(v.asInt32().getValue());
//        });
//
//        // TODO: By flowStartSysUpTime, flowStart*, flowStartDelta*, flowDuration*, minFlowStart*,
////        document.setFirst(record.getFirst());
////        document.setLast(record.getLast());
//
//        get(packet, "elements", "tcpControlBits").ifPresent(v -> {
//            document.setTcpFlags(v.asInt32().getValue());
//        });
//
//        get(packet, "elements", "protocolIdentifier").ifPresent(v -> {
//            document.setIpProtocol(v.asInt32().getValue());
//        });
//
//        get(packet, "elements", "ipClassOfService").ifPresent(v -> {
//            document.setTos(v.asInt32().getValue());
//        });
//
//        get(packet, "elements", "bgpSourceAsNumber").ifPresent(v -> {
//            document.setSourceAutonomousSystemNumber(v.asInt32().getValue());
//        });
//        get(packet, "elements", "bgpDestinationAsNumber").ifPresent(v -> {
//            document.setDestAutonomousSystemNumber(v.asInt32().getValue());
//        });
//
//        get(packet, "elements", "sourceIPv4PrefixLength").ifPresent(v -> {
//            document.setSourceMask(v.asInt32().getValue());
//        });
//        get(packet, "elements", "destinationIPv4PrefixLength").ifPresent(v -> {
//            document.setDestMask(v.asInt32().getValue());
//        });
    }

    private void convertIpfix(final BsonDocument packet, final NetflowDocument document) {

        // TODO: The nr of records in pkt is totally irrelevant
//        document.setFlowRecords();

        get(packet, "elements", "systemInitTimeMilliseconds").ifPresent(v -> {
            document.setSysUptime(document.getTimestamp() - v.asInt32().getValue());
        });

        // TODO: The seq nr is totally irrelevant
//        document.setFlowSequenceNumber(netflowPacket.getFlowSequence());

        get(packet, "elements", "engineType").ifPresent(v -> {
            document.setEngineType(v.asInt32().getValue());
        });

        // TODO: v5 samplingInterval is { method:2, interval:14} for real
        get(packet, "elements", "samplingInterval").ifPresent(v -> {
            // TODO: also use samplingMethod
            document.setSamplingInterval(v.asInt32().getValue());
        });

        get(packet, "elements", "sourceIPv4Address").ifPresent(v -> {
            document.setIpv4SourceAddress(v.asString().getValue());
        });
        get(packet, "elements", "sourceTransportPort").ifPresent(v -> {
            document.setSourcePort(v.asInt32().getValue());
        });

        get(packet, "elements", "destinationIPv4Address").ifPresent(v -> {
            document.setIpv4DestAddress(v.asString().getValue());
        });
        get(packet, "elements", "destinationTransportPort").ifPresent(v -> {
            document.setDestPort(v.asInt32().getValue());
        });

        // TODO: Also bgpNextHopIPv4Address
        get(packet, "elements", "ipNextHopIPv4Address").ifPresent(v -> {
            document.setIpv4NextHopAddress(v.asString().getValue());
        });

        get(packet, "elements", "ingressInterface").ifPresent(v -> {
            document.setInputSnmpInterfaceIndex(v.asInt32().getValue());
        });
        get(packet, "elements", "egressInterface").ifPresent(v -> {
            document.setOutputSnmpInterfaceIndex(v.asInt32().getValue());
        });

        get(packet, "elements", "octetDeltaCount").ifPresent(v -> {
            document.setInBytes(v.asInt32().getValue());
        });
        get(packet, "elements", "packetDeltaCount").ifPresent(v -> {
            document.setInPackets(v.asInt32().getValue());
        });

        // TODO: By flowStartSysUpTime, flowStart*, flowStartDelta*, flowDuration*, minFlowStart*,
//        document.setFirst(record.getFirst());
//        document.setLast(record.getLast());

        get(packet, "elements", "tcpControlBits").ifPresent(v -> {
            document.setTcpFlags(v.asInt32().getValue());
        });

        get(packet, "elements", "protocolIdentifier").ifPresent(v -> {
            document.setIpProtocol(v.asInt32().getValue());
        });

        get(packet, "elements", "ipClassOfService").ifPresent(v -> {
            document.setTos(v.asInt32().getValue());
        });

        get(packet, "elements", "bgpSourceAsNumber").ifPresent(v -> {
            document.setSourceAutonomousSystemNumber(v.asInt32().getValue());
        });
        get(packet, "elements", "bgpDestinationAsNumber").ifPresent(v -> {
            document.setDestAutonomousSystemNumber(v.asInt32().getValue());
        });

        get(packet, "elements", "sourceIPv4PrefixLength").ifPresent(v -> {
            document.setSourceMask(v.asInt32().getValue());
        });
        get(packet, "elements", "destinationIPv4PrefixLength").ifPresent(v -> {
            document.setDestMask(v.asInt32().getValue());
        });
    }
}
