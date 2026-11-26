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

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "An XML data collection without its sources, for list views.")
public class XmlCollectionSummaryDto {

    @Schema(description = "Unique name of the collection.", example = "xml-elasticsearch-cluster-stats")
    private String name;

    @Schema(description = "Collection interval in seconds.", example = "300")
    private Integer step;

    @Schema(description = "Number of sources.", example = "1")
    private Integer sourceCount;

    @Schema(description = "Number of groups defined directly in the sources.", example = "0")
    private Integer groupCount;

    @Schema(description = "Ids of the shared group files the sources import.")
    private List<String> importGroups;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(final Integer step) {
        this.step = step;
    }

    public Integer getSourceCount() {
        return sourceCount;
    }

    public void setSourceCount(final Integer sourceCount) {
        this.sourceCount = sourceCount;
    }

    public Integer getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(final Integer groupCount) {
        this.groupCount = groupCount;
    }

    public List<String> getImportGroups() {
        return importGroups;
    }

    public void setImportGroups(final List<String> importGroups) {
        this.importGroups = importGroups;
    }
}
