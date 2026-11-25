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

@Schema(description = "Choices and context for editing resource types.")
public class ResourceTypesMetadataDto {

    @Schema(description = "Class names of the storage strategies shipped with OpenNMS. Other classes are accepted as well.")
    private List<String> storageStrategies = new ArrayList<>();

    @Schema(description = "Class names of the persistence selector strategies shipped with OpenNMS. Other classes are accepted as well.")
    private List<String> persistenceSelectorStrategies = new ArrayList<>();

    @Schema(description = "Names of the resource types defined by the SNMP data collection configuration. A stored "
            + "resource type of the same name overrides them.")
    private List<String> dataCollectionTypeNames = new ArrayList<>();

    @Schema(description = "Names of the resource types provided by extensions. They override a stored resource type "
            + "of the same name.")
    private List<String> extensionTypeNames = new ArrayList<>();

    public List<String> getStorageStrategies() {
        return storageStrategies;
    }

    public void setStorageStrategies(final List<String> storageStrategies) {
        this.storageStrategies = storageStrategies;
    }

    public List<String> getPersistenceSelectorStrategies() {
        return persistenceSelectorStrategies;
    }

    public void setPersistenceSelectorStrategies(final List<String> persistenceSelectorStrategies) {
        this.persistenceSelectorStrategies = persistenceSelectorStrategies;
    }

    public List<String> getDataCollectionTypeNames() {
        return dataCollectionTypeNames;
    }

    public void setDataCollectionTypeNames(final List<String> dataCollectionTypeNames) {
        this.dataCollectionTypeNames = dataCollectionTypeNames;
    }

    public List<String> getExtensionTypeNames() {
        return extensionTypeNames;
    }

    public void setExtensionTypeNames(final List<String> extensionTypeNames) {
        this.extensionTypeNames = extensionTypeNames;
    }
}
