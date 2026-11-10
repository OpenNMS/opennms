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

@Schema(description = "Fields shared by basic thresholds and expression-based thresholds.")
public abstract class BaseThresholdDefDto {

    @Schema(description = "Evaluate the threshold even when some of the referenced values are unknown.",
            example = "false", defaultValue = "false")
    private Boolean relaxed;

    @Schema(description = "Free-text description shown in the UI.", example = "High CPU utilization")
    private String description;

    @Schema(description = "Threshold type.", example = "high", required = true,
            allowableValues = {"high", "low", "relativeChange", "absoluteChange", "rearmingAbsoluteChange"})
    private String type;

    @Schema(description = "Datasource type. `node` for node-level data, `if` for interface-level data, or the "
            + "name of a generic resource type from datacollection-config.xml. Node-level thresholds ignore "
            + "resource filters. The valid values for this system are served by GET /thresholding/metadata.",
            example = "node", required = true)
    private String dsType;

    @Schema(description = "Value at which the threshold triggers. A number, or a metadata reference such as "
            + "${scv:key:value}.", example = "90", required = true,
            pattern = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?|\\$\\{(.+:.+)\\}")
    private String value;

    @Schema(description = "Value at which the threshold re-arms. Unused for relativeChange thresholds but "
            + "still required by the schema.", example = "70", required = true,
            pattern = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?|\\$\\{(.+:.+)\\}")
    private String rearm;

    @Schema(description = "Number of consecutive polls that must exceed the value before the threshold "
            + "triggers. A positive integer, or a metadata reference. Not used for relativeChange "
            + "thresholds but still required by the schema.", example = "3", required = true,
            pattern = "[0-9]*[1-9][0-9]*|\\$\\{(.+:.+)\\}")
    private String trigger;

    @Schema(description = "Name of a collected string attribute used to label the resource in generated events.",
            example = "ifName")
    private String dsLabel;

    @Schema(description = "Human-readable label for an expression threshold. Ignored for basic thresholds.",
            example = "CPU total")
    private String exprLabel;

    @Schema(description = "UEI of the event sent when the threshold triggers. Blank uses the standard "
            + "threshold UEI. A UEI that eventconf does not know yet is created automatically, cloned "
            + "from the matching built-in threshold event.",
            example = "uei.opennms.org/example/highCpuThresholdExceeded")
    private String triggeredUEI;

    @Schema(description = "UEI of the event sent when the threshold re-arms. Ignored, and stored as null, "
            + "when the type is relativeChange or absoluteChange.",
            example = "uei.opennms.org/example/highCpuThresholdRearmed")
    private String rearmedUEI;

    @Schema(description = "How multiple resource filters combine. `or` matches any filter, `and` requires all.",
            example = "or", defaultValue = "or", allowableValues = {"and", "or"})
    private String filterOperator;

    @Schema(description = "Resource filters, applied in the order given.")
    private List<ResourceFilterDto> resourceFilters;

    public Boolean getRelaxed() {
        return relaxed;
    }

    public void setRelaxed(final Boolean relaxed) {
        this.relaxed = relaxed;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }
    public String getDsType() {
        return dsType;
    }

    public void setDsType(final String dsType) {
        this.dsType = dsType;
    }
    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
    public String getRearm() {
        return rearm;
    }

    public void setRearm(final String rearm) {
        this.rearm = rearm;
    }
    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(final String trigger) {
        this.trigger = trigger;
    }
    public String getDsLabel() {
        return dsLabel;
    }

    public void setDsLabel(final String dsLabel) {
        this.dsLabel = dsLabel;
    }
    public String getExprLabel() {
        return exprLabel;
    }

    public void setExprLabel(final String exprLabel) {
        this.exprLabel = exprLabel;
    }
    public String getTriggeredUEI() {
        return triggeredUEI;
    }

    public void setTriggeredUEI(final String triggeredUEI) {
        this.triggeredUEI = triggeredUEI;
    }
    public String getRearmedUEI() {
        return rearmedUEI;
    }

    public void setRearmedUEI(final String rearmedUEI) {
        this.rearmedUEI = rearmedUEI;
    }
    public String getFilterOperator() {
        return filterOperator;
    }

    public void setFilterOperator(final String filterOperator) {
        this.filterOperator = filterOperator;
    }
    public List<ResourceFilterDto> getResourceFilters() {
        return resourceFilters;
    }

    public void setResourceFilters(final List<ResourceFilterDto> resourceFilters) {
        this.resourceFilters = resourceFilters;
    }
}
