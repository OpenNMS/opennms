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
package org.opennms.web.rest.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opennms.netmgt.collection.api.Parameter;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.MibObjProperty;

import java.util.ArrayList;
import java.util.List;

public final class DatacollectionJsonHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DatacollectionJsonHelper() {
        // utility class
    }

    public static String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON serialization failed", e);
        }
    }

    public static IpList fromJsonToIpList(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, IpList.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse IpList JSON: " + json, e);
        }
    }


    public static List<Parameter> fromJsonToParameters(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            List<KeyValueDto> dtos = OBJECT_MAPPER.readValue(json, new TypeReference<List<KeyValueDto>>() {});

            List<Parameter> out = new ArrayList<>(dtos.size());
            for (KeyValueDto dto : dtos) {
                // IMPORTANT: instantiate the JAXB/config Parameter class
                org.opennms.netmgt.config.datacollection.Parameter p =
                        new org.opennms.netmgt.config.datacollection.Parameter();
                p.setKey(dto.key);
                p.setValue(dto.value);
                out.add(p); // upcast to API interface is fine
            }
            return out;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse parameters JSON: " + json, e);
        }
    }

    private static final class KeyValueDto {
        public String key;
        public String value;
    }

    public static List<MibObj> fromJsonToMibObjs(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse MIB objects JSON: " + json, e);
        }
    }

    public static List<MibObjProperty> fromJsonToProperties(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<MibObjProperty>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse properties JSON: " + json, e);
        }
    }

    public static <T> T fromJson(final String json, final TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeRef);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JSON: " + json, e);
        }
    }
}
