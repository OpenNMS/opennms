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

@Schema(description = "A threshold group without its definitions, for list views.")
public class ThresholdGroupSummaryDto {

    @Schema(description = "Unique name of the group.", example = "mib2")
    private String name;

    @Schema(description = "Directory holding the RRD files this group thresholds against.",
            example = "/opt/opennms/share/rrd/snmp/")
    private String rrdRepository;

    @Schema(description = "Number of basic thresholds in the group.", example = "4")
    private Integer thresholdCount;

    @Schema(description = "Number of expression-based thresholds in the group.", example = "1")
    private Integer expressionCount;

    @Schema(description = "True when the group is contributed by an OSGi extension and cannot be edited.",
            example = "false")
    private Boolean readOnly;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
    public String getRrdRepository() {
        return rrdRepository;
    }

    public void setRrdRepository(final String rrdRepository) {
        this.rrdRepository = rrdRepository;
    }
    public Integer getThresholdCount() {
        return thresholdCount;
    }

    public void setThresholdCount(final Integer thresholdCount) {
        this.thresholdCount = thresholdCount;
    }
    public Integer getExpressionCount() {
        return expressionCount;
    }

    public void setExpressionCount(final Integer expressionCount) {
        this.expressionCount = expressionCount;
    }
    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(final Boolean readOnly) {
        this.readOnly = readOnly;
    }
}
