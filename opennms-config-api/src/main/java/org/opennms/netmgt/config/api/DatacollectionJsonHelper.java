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
package org.opennms.netmgt.config.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opennms.netmgt.collection.api.Parameter;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSON serialization/deserialization helper for SNMP data collection
 * configuration stored in database text columns.
 */
public final class DatacollectionJsonHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DatacollectionJsonHelper.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DatacollectionJsonHelper() {
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

    /**
     * Parse the {@code ip_addresses} column into an {@link IpList}, tolerant
     * of legacy data shapes:
     *
     * <ol>
     *   <li>Canonical {@link IpList} JSON (the shape the migration, the
     *   multipart upload, and the post-fix REST CRUD path all produce).</li>
     *   <li>A bare JSON array of strings — produced by some pre-fix code
     *   paths.</li>
     *   <li>A comma-separated raw string — produced by the pre-fix REST CRUD
     *   path that copied the DTO field through verbatim. Treated as a list
     *   of {@code <ipAddress>} entries.</li>
     * </ol>
     *
     * Returning a structured object regardless of input shape lets the
     * runtime loader and REST GET path keep working when stale rows are
     * still on disk; new writes always go through {@link #toJson} on a real
     * {@link IpList} so the canonical shape becomes the steady state.
     */
    public static IpList fromJsonToIpList(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        // 1) Canonical IpList JSON.
        try {
            return OBJECT_MAPPER.readValue(json, IpList.class);
        } catch (Exception ignored) {
            // fall through
        }
        // 2) Bare JSON array of address strings.
        try {
            List<String> addresses = OBJECT_MAPPER.readValue(json, new TypeReference<List<String>>() {});
            final IpList list = new IpList();
            list.setIpAddresses(addresses != null ? addresses : new ArrayList<>());
            LOG.warn("Recovered legacy bare-array ip_addresses payload (will be re-serialised on next write): {}", json);
            return list;
        } catch (Exception ignored) {
            // fall through
        }
        // 3) Raw / comma-separated string. Split, trim, drop blanks. Surface
        //    the recovery in the log so an operator can re-save the row to
        //    upgrade it to canonical JSON.
        final List<String> entries = Arrays.stream(json.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (entries.isEmpty()) {
            return null;
        }
        final IpList list = new IpList();
        list.setIpAddresses(entries);
        LOG.warn("Recovered legacy comma-separated ip_addresses payload (will be re-serialised on next write): {}", json);
        return list;
    }

    public static List<Parameter> fromJsonToParameters(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            List<KeyValueDto> dtos = OBJECT_MAPPER.readValue(json, new TypeReference<List<KeyValueDto>>() {});
            List<Parameter> out = new ArrayList<>(dtos.size());
            for (KeyValueDto dto : dtos) {
                org.opennms.netmgt.config.datacollection.Parameter p =
                        new org.opennms.netmgt.config.datacollection.Parameter();
                p.setKey(dto.key);
                p.setValue(dto.value);
                out.add(p);
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
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
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
