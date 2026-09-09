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
import org.opennms.web.svclayer.model.SnmpInfo;
import org.opennms.netmgt.config.snmp.SnmpProfile;

@Schema(description = """
        An SNMP profile expressed with the flatter `SnmpInfo` field names, plus the profile's label and
        filter. `toSnmpProfile` converts it to the `SnmpProfile` bean that `POST /snmp-config/profile`
        consumes.""")
public class SnmpConfigProfileDto extends SnmpInfo {
    @Schema(description = "Profile label. Identifies the profile for update and delete.",
            example = "edge-switches", required = true)
    protected String label;

    @Schema(description = "Filter expression deciding which interfaces the profile is tried against. "
            + "Empty means every interface.",
            example = "iphostname LIKE '%edge%'")
    protected String filterExpression;

    public String getLabel() {
        return label;
    }

    public void setLabel(String s) {
        label = s;
    }

    public String getFilterExpression() {
        return filterExpression;
    }

    public void setFilterExpression(String s) {
        filterExpression = s;
    }

    public static SnmpProfile toSnmpProfile(SnmpConfigProfileDto dto) {
        return new SnmpProfile(
                dto.getPort(),
                dto.getRetries(),
                dto.getTimeout(),
                dto.getReadCommunity(),
                dto.getWriteCommunity(),
                dto.getProxyHost(),
                dto.getVersion(),
                dto.getMaxVarsPerPdu(),
                dto.getMaxRepetitions(),
                dto.getMaxRequestSize(),
                dto.getSecurityName(),
                dto.getSecurityLevel(),
                dto.getAuthPassPhrase(),
                dto.getAuthProtocol(),
                dto.getEngineId(),
                dto.getContextEngineId(),
                dto.getContextName(),
                dto.getPrivPassPhrase(),
                dto.getPrivProtocol(),
                dto.getEnterpriseId(),
                dto.getLabel(),
                dto.getFilterExpression()
        );
    }
}
