/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.opennms.netmgt.flows.api.Converter;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.telemetry.protocols.netflow.adapter.common.NetFlowMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.RecordEnrichment;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.IpFixMessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.NetFlow9MessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.Netflow5MessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

public class Utils {


    public static byte[] buildAndSerialize(Protocol protocol, Iterable<Value<?>> record) {
        RecordEnrichment enrichment = new RecordEnrichment() {
            @Override
            public Optional<String> getHostnameFor(InetAddress srcAddress) {
                return Optional.empty();
            }
        };
        if (protocol.equals(Protocol.NETFLOW5)) {
            Netflow5MessageBuilder builder = new Netflow5MessageBuilder(record, enrichment);
            return builder.buildData();
        } else if (protocol.equals(Protocol.NETFLOW9)) {
            NetFlow9MessageBuilder builder = new NetFlow9MessageBuilder(record, enrichment);
            return builder.buildData();
        } else if (protocol.equals(Protocol.IPFIX)) {
            IpFixMessageBuilder builder = new IpFixMessageBuilder(record, enrichment);
            return builder.buildData();
        }
        return null;
    }

    public static class JsonConverter implements Converter<String[]> {

        @Override
        public List<Flow> convert(String[] packets) {
            List<Flow> flows = new ArrayList<>();
            for(String packet : packets) {
                FlowMessage.Builder builder = FlowMessage.newBuilder();
                try {
                    JsonFormat.parser().merge(packet, builder);
                    flows.add(new NetFlowMessage(builder.build()));
                } catch (InvalidProtocolBufferException e) {
                    //pass
                }
            }
            return flows;
        }
    }

}
