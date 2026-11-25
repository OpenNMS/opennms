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

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A resource types configuration without the definitions of its types, for list views.")
public class ResourceTypesConfigSummaryDto {

    @Schema(description = "Unique id of the configuration.", example = "postgresql-resource")
    private String id;

    @Schema(description = "Names of the resource types of the configuration, in order.", example = "[\"pgTableSpace\", \"pgDatabase\"]")
    private List<String> resourceTypeNames = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public List<String> getResourceTypeNames() {
        return resourceTypeNames;
    }

    public void setResourceTypeNames(final List<String> resourceTypeNames) {
        this.resourceTypeNames = resourceTypeNames;
    }
}
