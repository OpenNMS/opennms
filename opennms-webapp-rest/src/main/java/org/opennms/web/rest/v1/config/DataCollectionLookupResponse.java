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
 * {@code GET /rest/config/datacollection/lookup}. It is a documentation-only type: the handler
 * builds a {@link java.util.LinkedHashMap} rather than an instance of this class.
 */
@Schema(name = "DataCollectionLookupResponse", description = "MIB objects that SNMP collection would gather for a given sysObjectID, address, collection and ifType.")
public class DataCollectionLookupResponse {

    @Schema(description = "The sysObjectID the lookup was performed for, echoed back from the request.", example = ".1.3.6.1.4.1.8072.3.2.10")
    private String sysoid;

    @Schema(description = "The interface address the lookup was performed for, echoed back from the request.", example = "127.0.0.1")
    private String address;

    @Schema(description = "The SNMP collection name the lookup was performed against, echoed back from the request.", example = "default")
    private String collection;

    @Schema(description = "The ifType the lookup was performed for, echoed back from the request. -1 means node-level objects only.", example = "-1")
    private int ifType;

    @Schema(description = "Number of entries in objects.", example = "191")
    private int matchedObjectCount;

    @Schema(description = "The matched MIB objects, in the order the data collection config returned them.")
    private List<MibObjectSummary> objects;

    public String getSysoid() {
        return sysoid;
    }

    public String getAddress() {
        return address;
    }

    public String getCollection() {
        return collection;
    }

    public int getIfType() {
        return ifType;
    }

    public int getMatchedObjectCount() {
        return matchedObjectCount;
    }

    public List<MibObjectSummary> getObjects() {
        return objects;
    }

    @Schema(name = "MibObjectSummary", description = "One MIB object from the resolved collection.")
    public static class MibObjectSummary {

        @Schema(description = "Name of the MIB group the object belongs to.", example = "mib2-tcp")
        private String group;

        @Schema(description = "Object identifier, without the instance part.", example = ".1.3.6.1.2.1.6.5")
        private String oid;

        @Schema(description = "Data source alias the value is stored under. RRD data source names are limited to 19 characters.", example = "tcpActiveOpens")
        private String alias;

        @Schema(description = "Declared type of the object, as spelled in datacollection-config.xml.", example = "Counter32")
        private String type;

        @Schema(description = "Instance selector: a literal sub-identifier for scalars, or a resource type name for tabular objects.", example = "0")
        private String instance;

        public String getGroup() {
            return group;
        }

        public String getOid() {
            return oid;
        }

        public String getAlias() {
            return alias;
        }

        public String getType() {
            return type;
        }

        public String getInstance() {
            return instance;
        }
    }
}
