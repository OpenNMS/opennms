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

import java.util.LinkedHashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The body of {@code GET /nodes/{nodeCriteria}/alarmStatus}.
 */
@Schema(name = "NodeAlarmStatus", description = "A summary of one node's unacknowledged problem alarms (severity "
        + "above Normal). Counted on the server, so it costs the same for a node with thousands of alarms as for one "
        + "with none.")
public class NodeAlarmStatusDto {

    @Schema(description = "Highest severity among the unacknowledged problem alarms; `NORMAL` when there are none. "
            + "An acknowledged alarm does not raise this.", example = "MAJOR")
    private String severity;

    @Schema(description = "Problem alarms that are not acknowledged.", example = "2")
    private long unacknowledgedCount;

    @Schema(description = "Unacknowledged problem alarms at each severity above Normal, highest first. Every one "
            + "of `CRITICAL`, `MAJOR`, `MINOR` and `WARNING` is present, with 0 when there are none.",
            example = "{\"CRITICAL\": 0, \"MAJOR\": 1, \"MINOR\": 1, \"WARNING\": 0}")
    private Map<String, Long> unacknowledgedBySeverity = new LinkedHashMap<>();

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(final String severity) {
        this.severity = severity;
    }

    public long getUnacknowledgedCount() {
        return unacknowledgedCount;
    }

    public void setUnacknowledgedCount(final long unacknowledgedCount) {
        this.unacknowledgedCount = unacknowledgedCount;
    }

    public Map<String, Long> getUnacknowledgedBySeverity() {
        return unacknowledgedBySeverity;
    }

    public void setUnacknowledgedBySeverity(final Map<String, Long> unacknowledgedBySeverity) {
        this.unacknowledgedBySeverity = unacknowledgedBySeverity;
    }
}
