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

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.opennms.netmgt.flows.api.Converter;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.telemetry.protocols.netflow.adapter.common.NetflowMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.IllegalFlowException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.RecordEnrichment;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.IpFixMessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.Netflow5MessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.Netflow9MessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

public class Utils {


    public static FlowMessage.Builder buildAndSerialize(Protocol protocol, Iterable<Value<?>> record) throws IllegalFlowException {
        RecordEnrichment enrichment = (address -> Optional.empty());
        if (protocol.equals(Protocol.NETFLOW5)) {
            Netflow5MessageBuilder builder = new Netflow5MessageBuilder();
            return builder.buildMessage(record, enrichment);
        } else if (protocol.equals(Protocol.NETFLOW9)) {
            Netflow9MessageBuilder builder = new Netflow9MessageBuilder();
            return builder.buildMessage(record, enrichment);
        } else if (protocol.equals(Protocol.IPFIX)) {
            IpFixMessageBuilder builder = new IpFixMessageBuilder();
            return builder.buildMessage(record, enrichment);
        }
        return null;
    }

    public static class JsonConverter implements Converter<List<String>> {


        public List<String> getJsonStringFromResources(String... resources) {
            final List<String> jsonStrings = new ArrayList<>();
            for (String resource : resources) {
                URL resourceURL = getClass().getResource(resource);
                try (FileReader reader = new FileReader(resourceURL.toURI().getPath())) {
                    JsonReader jsonReader = new JsonReader(reader);
                    jsonReader.setLenient(false);
                    JsonParser jsonParser = new JsonParser();
                    JsonElement jsonElement = jsonParser.parse(jsonReader);

                    if(jsonElement instanceof JsonArray) {
                        JsonArray jsonArray = jsonElement.getAsJsonArray();
                        for(JsonElement json: jsonArray) {
                            jsonStrings.add(json.toString());
                        }
                    }
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            return jsonStrings;
        }

        @Override
        public List<Flow> convert(List<String> resources, final Instant receivedAt) {

            List<Flow> flows = new ArrayList<>();

            resources.forEach(resource -> {
                try {
                    FlowMessage.Builder builder = FlowMessage.newBuilder();
                    JsonFormat.parser().merge(resource, builder);
                    flows.add(new NetflowMessage(builder.build(), receivedAt));
                } catch (InvalidProtocolBufferException e) {
                    //Ignore.
                }
            });
            return flows;
        }
    }

}
