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
package org.opennms.web.rest.v2.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A value extracted from a regular expression group and stored as a metric or string attribute.")
public class HttpAttribDto {

    @Schema(description = "Name the value is stored under. At most 19 characters (the RRD data source name limit).", example = "documentCount", required = true)
    private String alias;

    @Schema(description = "Number of the `matches` regular expression group holding the value, starting at 1.", example = "1", required = true)
    private Integer matchGroup;

    @Schema(description = "Attribute type: `gauge`, `counter` or `string`. SNMP-style aliases such as `counter32` are accepted and stored as their base type.", example = "gauge", required = true)
    private String type;

    public String getAlias() {
        return alias;
    }

    public void setAlias(final String alias) {
        this.alias = alias;
    }

    public Integer getMatchGroup() {
        return matchGroup;
    }

    public void setMatchGroup(final Integer matchGroup) {
        this.matchGroup = matchGroup;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }
}
