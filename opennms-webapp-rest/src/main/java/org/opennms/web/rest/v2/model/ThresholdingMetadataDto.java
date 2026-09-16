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
import java.util.List;

@Schema(description = """
        Everything the threshold editor needs to populate its dropdowns. The datasource types depend on
        the resource types configured on this system, so they cannot be hardcoded by a client.""")
public class ThresholdingMetadataDto {

    @Schema(description = "Selectable datasource types: node, if, and every generic index resource type.")
    private List<DsTypeDto> dsTypes;

    @Schema(description = "Selectable threshold types.")
    private List<String> thresholdTypes;

    @Schema(description = "Selectable resource filter operators.")
    private List<String> filterOperators;

    public List<DsTypeDto> getDsTypes() {
        return dsTypes;
    }

    public void setDsTypes(final List<DsTypeDto> dsTypes) {
        this.dsTypes = dsTypes;
    }
    public List<String> getThresholdTypes() {
        return thresholdTypes;
    }

    public void setThresholdTypes(final List<String> thresholdTypes) {
        this.thresholdTypes = thresholdTypes;
    }
    public List<String> getFilterOperators() {
        return filterOperators;
    }

    public void setFilterOperators(final List<String> filterOperators) {
        this.filterOperators = filterOperators;
    }
}
