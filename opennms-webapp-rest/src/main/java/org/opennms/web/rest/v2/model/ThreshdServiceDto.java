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
        A service thresholded within a package. The `thresholding-group` parameter binds the service to a
        threshold group from the thresholding configuration.""")
public class ThreshdServiceDto {

    @Schema(description = "Service name.", example = "SNMP", required = true)
    private String name;

    @Schema(description = "Milliseconds between threshold evaluations. Must be greater than 0.",
            example = "300000", required = true, minimum = "1")
    private Long interval;

    @Schema(description = "Whether the service was added by a user.", example = "false")
    private Boolean userDefined;

    @Schema(description = "Whether thresholding is active for this service.",
            example = "on", allowableValues = {"on", "off"})
    private String status;

    @Schema(description = "Service parameters. `thresholding-group` names the threshold group to apply.")
    private List<ParameterDto> parameters;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
    public Long getInterval() {
        return interval;
    }

    public void setInterval(final Long interval) {
        this.interval = interval;
    }
    public Boolean getUserDefined() {
        return userDefined;
    }

    public void setUserDefined(final Boolean userDefined) {
        this.userDefined = userDefined;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }
    public List<ParameterDto> getParameters() {
        return parameters;
    }

    public void setParameters(final List<ParameterDto> parameters) {
        this.parameters = parameters;
    }
}
