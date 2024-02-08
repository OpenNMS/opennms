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
package org.opennms.features.config.dao.api.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.IOException;

/**
 * OpenAPI have its own deserializer, ObjectMapper may have problem
 */
public class OpenAPIDeserializer extends JsonDeserializer<OpenAPI> {
    private final ObjectMapper mapper;

    public OpenAPIDeserializer() {
        mapper = Json.mapper();
    }

    @Override
    public OpenAPI deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String json = jsonParser.getCodec().readTree(jsonParser).toString();
        return mapper.readValue(json, OpenAPI.class);
    }
}
