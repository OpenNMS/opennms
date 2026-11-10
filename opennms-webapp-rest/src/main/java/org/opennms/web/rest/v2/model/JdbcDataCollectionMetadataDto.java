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

@Schema(description = "Everything the JDBC data collection editor needs to populate its selections and defaults.")
public class JdbcDataCollectionMetadataDto {

    @Schema(description = "Selectable column types.")
    private List<String> attributeTypes = new ArrayList<>();

    @Schema(description = "Suggested interface types of a query.")
    private List<String> ifTypes = new ArrayList<>();

    @Schema(description = "Names of the resource types defined on this system.")
    private List<String> resourceTypes = new ArrayList<>();

    @Schema(description = "Repository of a new source.")
    private String defaultRrdRepository;

    @Schema(description = "RRD step of a new collection.")
    private Integer defaultRrdStep;

    @Schema(description = "RRAs of a new collection.")
    private List<String> defaultRras = new ArrayList<>();

    public List<String> getAttributeTypes() {
        return attributeTypes;
    }

    public void setAttributeTypes(final List<String> attributeTypes) {
        this.attributeTypes = attributeTypes;
    }

    public List<String> getIfTypes() {
        return ifTypes;
    }

    public void setIfTypes(final List<String> ifTypes) {
        this.ifTypes = ifTypes;
    }

    public List<String> getResourceTypes() {
        return resourceTypes;
    }

    public void setResourceTypes(final List<String> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    public String getDefaultRrdRepository() {
        return defaultRrdRepository;
    }

    public void setDefaultRrdRepository(final String defaultRrdRepository) {
        this.defaultRrdRepository = defaultRrdRepository;
    }

    public Integer getDefaultRrdStep() {
        return defaultRrdStep;
    }

    public void setDefaultRrdStep(final Integer defaultRrdStep) {
        this.defaultRrdStep = defaultRrdStep;
    }

    public List<String> getDefaultRras() {
        return defaultRras;
    }

    public void setDefaultRras(final List<String> defaultRras) {
        this.defaultRras = defaultRras;
    }
}
