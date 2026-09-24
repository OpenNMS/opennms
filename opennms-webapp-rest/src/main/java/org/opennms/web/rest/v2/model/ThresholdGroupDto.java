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
        A threshold group with all of its threshold and expression definitions. Writes replace the whole
        group rather than patching it, so a definition left out of the body is deleted.""")
public class ThresholdGroupDto {

    @Schema(description = "Unique name of the group. Referenced by a threshd package service through its "
            + "`thresholding-group` parameter.", example = "mib2", required = true)
    private String name;

    @Schema(description = "Directory holding the RRD files this group thresholds against.",
            example = "/opt/opennms/share/rrd/snmp/", required = true)
    private String rrdRepository;

    @Schema(description = "Basic thresholds, in evaluation order.")
    private List<ThresholdDto> thresholds;

    @Schema(description = "Expression-based thresholds, in evaluation order.")
    private List<ExpressionDto> expressions;

    @Schema(description = "True when the group is contributed by an OSGi extension rather than stored in "
            + "the database, in which case it cannot be edited.",
            example = "false", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean readOnly;

    @Schema(description = "Opaque version of the group, also returned as the ETag header. Send it back as "
            + "If-Match on a write to be rejected with a 412 instead of overwriting someone else's change.",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String version;

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
    public List<ThresholdDto> getThresholds() {
        return thresholds;
    }

    public void setThresholds(final List<ThresholdDto> thresholds) {
        this.thresholds = thresholds;
    }
    public List<ExpressionDto> getExpressions() {
        return expressions;
    }

    public void setExpressions(final List<ExpressionDto> expressions) {
        this.expressions = expressions;
    }
    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(final Boolean readOnly) {
        this.readOnly = readOnly;
    }
    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }
}
