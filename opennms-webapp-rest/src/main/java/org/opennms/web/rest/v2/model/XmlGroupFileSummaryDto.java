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

@Schema(description = "A shared group file without its groups, for list views.")
public class XmlGroupFileSummaryDto {

    @Schema(description = "Unique id of the group file.", example = "elasticsearch-cluster-stats")
    private String id;

    @Schema(description = "Number of groups in the file.", example = "2")
    private Integer groupCount;

    @Schema(description = "Names of the collections with a source importing this group file.")
    private List<String> usedBy;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Integer getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(final Integer groupCount) {
        this.groupCount = groupCount;
    }

    public List<String> getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(final List<String> usedBy) {
        this.usedBy = usedBy;
    }
}
