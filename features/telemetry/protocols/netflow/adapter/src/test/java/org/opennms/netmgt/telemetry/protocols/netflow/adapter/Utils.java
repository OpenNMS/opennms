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

import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.telemetry.protocols.netflow.adapter.common.NetflowMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

public class Utils {
    public static List<Flow> getJsonFlowFromResources(final Instant receivedAt, String... resources) {
        final List<Flow> flows = new ArrayList<>();

        for (String resource : resources) {
            URL resourceURL = Utils.class.getResource(resource);
            try (FileReader reader = new FileReader(resourceURL.toURI().getPath())) {
                JsonReader jsonReader = new JsonReader(reader);
                jsonReader.setLenient(false);
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(jsonReader);

                if(jsonElement instanceof JsonArray) {
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    for(JsonElement json: jsonArray) {
                        try {
                            FlowMessage.Builder builder = FlowMessage.newBuilder();
                            JsonFormat.parser().merge(json.toString(), builder);
                            flows.add(new NetflowMessage(builder.build(), receivedAt));
                        } catch (InvalidProtocolBufferException e) {
                            //Ignore.
                        }
                    }
                }
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        return flows;
    }
}
