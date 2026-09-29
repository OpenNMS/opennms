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
package org.opennms.web.rest.v1.config;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Describes the shape of the ad-hoc map returned by
 * {@code GET /rest/config/datacollection/status}. It is a documentation-only type: the handler
 * builds a {@link java.util.LinkedHashMap} rather than an instance of this class.
 */
@Schema(name = "DataCollectionStatusResponse", description = "Summary of the SNMP data collection configuration currently held in memory.")
public class DataCollectionStatusResponse {

    @Schema(description = "Names of the datacollection-group elements loaded from datacollection-config.xml and the datacollection.d include directory.", example = "[\"MIB2\",\"Cisco\",\"Net-SNMP\"]")
    private List<String> availableCollectionGroups;

    @Schema(description = "Number of systemDef elements across all loaded groups.", example = "216")
    private int availableSystemDefs;

    @Schema(description = "Number of MIB groups across all loaded groups.", example = "328")
    private int availableMibGroups;

    @Schema(description = "Number of resourceType elements across all loaded groups.", example = "195")
    private int configuredResourceTypes;

    // Serialized as epoch milliseconds, not as the ISO-8601 string a Date would otherwise imply.
    @Schema(description = "When the configuration was last (re)loaded, as epoch milliseconds. Null if it has not been loaded from a file.", type = "integer", format = "int64", example = "1787685418848")
    private Long lastUpdate;

    @Schema(description = "One entry per snmp-collection, excluding the internal __resource_type_collection entry.")
    private List<SnmpCollectionSummary> snmpCollections;

    public List<String> getAvailableCollectionGroups() {
        return availableCollectionGroups;
    }

    public int getAvailableSystemDefs() {
        return availableSystemDefs;
    }

    public int getAvailableMibGroups() {
        return availableMibGroups;
    }

    public int getConfiguredResourceTypes() {
        return configuredResourceTypes;
    }

    public Long getLastUpdate() {
        return lastUpdate;
    }

    public List<SnmpCollectionSummary> getSnmpCollections() {
        return snmpCollections;
    }

    @Schema(name = "SnmpCollectionSummary", description = "Counts for one snmp-collection.")
    public static class SnmpCollectionSummary {

        @Schema(description = "The snmp-collection name, as referenced by the collectd collection parameter.", example = "default")
        private String name;

        @Schema(description = "Value of snmpStorageFlag for the collection.", example = "select", allowableValues = {"select", "primary", "all"})
        private String storageFlag;

        @Schema(description = "RRD step in seconds, or null when the collection declares no rrd element.", example = "300")
        private Integer rrdStep;

        @Schema(description = "Number of groups referenced by the collection.", example = "324")
        private int groups;

        @Schema(description = "Number of systemDefs referenced by the collection.", example = "215")
        private int systemDefs;

        @Schema(description = "Number of resourceTypes referenced by the collection.", example = "194")
        private int resourceTypes;

        public String getName() {
            return name;
        }

        public String getStorageFlag() {
            return storageFlag;
        }

        public Integer getRrdStep() {
            return rrdStep;
        }

        public int getGroups() {
            return groups;
        }

        public int getSystemDefs() {
            return systemDefs;
        }

        public int getResourceTypes() {
            return resourceTypes;
        }
    }
}
