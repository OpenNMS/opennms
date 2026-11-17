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

/**
 * The body of {@code GET /nodes/{nodeCriteria}/alarmStatus}.
 */
@Schema(name = "NodeAlarmStatus", description = "A summary of one node's problem alarms (severity above "
        + "Normal), as the legacy node page's status box worked it out. Counted on the server, so it costs the "
        + "same for a node with thousands of alarms as for one with none.")
public class NodeAlarmStatusDto {

    @Schema(description = "Highest severity among the unacknowledged problem alarms; `NORMAL` when there are none. "
            + "An acknowledged alarm is counted, but does not raise this.", example = "MAJOR")
    private String severity;

    @Schema(description = "Whether a problem alarm's UEI contains `nodeDown`.", example = "false")
    private boolean nodeDown;

    @Schema(description = "Problem alarms whose UEI contains `interfaceDown`.", example = "0")
    private long interfacesDown;

    @Schema(description = "Problem alarms whose UEI contains `nodeLostService`.", example = "1")
    private long servicesDown;

    @Schema(description = "Problem alarms that are acknowledged.", example = "1")
    private long acknowledgedCount;

    @Schema(description = "Problem alarms that are not acknowledged.", example = "2")
    private long unacknowledgedCount;

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(final String severity) {
        this.severity = severity;
    }

    public boolean isNodeDown() {
        return nodeDown;
    }

    public void setNodeDown(final boolean nodeDown) {
        this.nodeDown = nodeDown;
    }

    public long getInterfacesDown() {
        return interfacesDown;
    }

    public void setInterfacesDown(final long interfacesDown) {
        this.interfacesDown = interfacesDown;
    }

    public long getServicesDown() {
        return servicesDown;
    }

    public void setServicesDown(final long servicesDown) {
        this.servicesDown = servicesDown;
    }

    public long getAcknowledgedCount() {
        return acknowledgedCount;
    }

    public void setAcknowledgedCount(final long acknowledgedCount) {
        this.acknowledgedCount = acknowledgedCount;
    }

    public long getUnacknowledgedCount() {
        return unacknowledgedCount;
    }

    public void setUnacknowledgedCount(final long unacknowledgedCount) {
        this.unacknowledgedCount = unacknowledgedCount;
    }
}
