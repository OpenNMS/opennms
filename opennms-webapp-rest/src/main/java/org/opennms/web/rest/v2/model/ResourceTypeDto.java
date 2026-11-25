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

@Schema(description = "A resource type: how collected data of one kind of resource is named, labelled and stored.")
public class ResourceTypeDto {

    @Schema(description = "Unique name of the resource type, referenced by data collection groups and thresholds.",
            example = "pgTableSpace", required = true)
    private String name;

    @Schema(description = "Label of the resource type shown in the web UI.", example = "PostgreSQL Tablespace", required = true)
    private String label;

    @Schema(description = "Expression for the label of each resource of this type.", example = "${spcname}")
    private String resourceLabel;

    @Schema(description = "Decides which resources of this type are persisted.", required = true)
    private ResourceTypeStrategyDto persistenceSelectorStrategy;

    @Schema(description = "Decides where the data of a resource of this type is stored.", required = true)
    private ResourceTypeStrategyDto storageStrategy;

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

    public String getResourceLabel() {
        return resourceLabel;
    }

    public void setResourceLabel(final String resourceLabel) {
        this.resourceLabel = resourceLabel;
    }

    public ResourceTypeStrategyDto getPersistenceSelectorStrategy() {
        return persistenceSelectorStrategy;
    }

    public void setPersistenceSelectorStrategy(final ResourceTypeStrategyDto persistenceSelectorStrategy) {
        this.persistenceSelectorStrategy = persistenceSelectorStrategy;
    }

    public ResourceTypeStrategyDto getStorageStrategy() {
        return storageStrategy;
    }

    public void setStorageStrategy(final ResourceTypeStrategyDto storageStrategy) {
        this.storageStrategy = storageStrategy;
    }
}
