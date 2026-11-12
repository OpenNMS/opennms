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

@Schema(description = "A datasource type that can be selected for a threshold.")
public class DsTypeDto {

    @Schema(description = "Value to store in a threshold's dsType.", example = "hrStorageIndex")
    private String name;

    @Schema(description = "Human-readable label.", example = "Storage (SNMP)")
    private String label;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public static DsTypeDto of(final String name, final String label) {
        final DsTypeDto dto = new DsTypeDto();
        dto.setName(name);
        dto.setLabel(label);
        return dto;
    }
}
