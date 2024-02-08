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
