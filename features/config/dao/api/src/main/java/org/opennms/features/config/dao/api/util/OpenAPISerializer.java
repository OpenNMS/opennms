/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
 ******************************************************************************/
package org.opennms.features.config.dao.api.util;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.IOException;
import java.util.Map;

public class OpenAPISerializer extends JsonSerializer<OpenAPI> {

    @Override
    public void serialize(OpenAPI o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        jsonGenerator.writeStartObject();
        mapper.writeValue(jsonGenerator, o);
//        jsonGenerator.writeEndObject();
//        if (o != null) {
//            jsonGenerator.writeStartObject();
//            jsonGenerator.writeStringField("openapi", o.getOpenapi());
//            if(o.getInfo() != null) {
//                jsonGenerator.writeObjectField("info", o.getInfo());
//            }
//            if(o.getExternalDocs() != null) {
//                jsonGenerator.writeObjectField("externalDocs", o.getExternalDocs());
//            }
//            if(o.getServers() != null) {
//                jsonGenerator.writeObjectField("servers", o.getServers());
//            }
//            if(o.getSecurity() != null) {
//                jsonGenerator.writeObjectField("security", o.getSecurity());
//            }
//            if(o.getTags() != null) {
//                jsonGenerator.writeObjectField("tags", o.getTags());
//            }
//            if(o.getPaths() != null) {
//                jsonGenerator.writeObjectField("paths", o.getPaths());
//            }
//            if(o.getComponents() != null) {
//                jsonGenerator.writeObjectField("components", o.getComponents());
//            }
//            if(o.getExtensions() != null) {
//                for (Map.Entry<String, Object> e : o.getExtensions().entrySet()) {
//                    jsonGenerator.writeObjectField(e.getKey(), e.getValue());
//                }
//            }
//            jsonGenerator.writeEndObject();
//        }
    }


}
