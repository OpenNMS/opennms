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
import java.util.stream.Collectors;

/**
 * One event of a source, reduced to what an ordering view needs - the XML payload is left out.
 */
@Schema(name = "EventConfEventSummary",
        description = "One event of a source, reduced to what an ordering view needs; the XML definition is left out.")
public class EventConfEventSummaryDto {

    @Schema(description = "Database identifier of the event.", example = "4211")
    private Long id;

    @Schema(description = "Event UEI.", example = "uei.opennms.org/vendor/cisco/syslog/LINK-3-UPDOWN")
    private String uei;

    @Schema(description = "Event label.", example = "Cisco Syslog: LINK-3-UPDOWN")
    private String eventLabel;

    @Schema(description = "Event severity.", example = "Warning")
    private String severity;

    @Schema(description = "Whether the event participates in matching.", example = "true")
    private Boolean enabled;

    @Schema(description = "Evaluation position within the source: 1 = evaluated first.", example = "1")
    private Integer eventOrder;

    public EventConfEventSummaryDto() {
    }

    public EventConfEventSummaryDto(Long id, String uei, String eventLabel, String severity, Boolean enabled, Integer eventOrder) {
        this.id = id;
        this.uei = uei;
        this.eventLabel = eventLabel;
        this.severity = severity;
        this.enabled = enabled;
        this.eventOrder = eventOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUei() { return uei; }
    public void setUei(String uei) { this.uei = uei; }

    public String getEventLabel() { return eventLabel; }
    public void setEventLabel(String eventLabel) { this.eventLabel = eventLabel; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Integer getEventOrder() { return eventOrder; }
    public void setEventOrder(Integer eventOrder) { this.eventOrder = eventOrder; }

    /** Maps the DAO's {@code [id, uei, eventLabel, severity, enabled, eventOrder]} rows. */
    public static List<EventConfEventSummaryDto> fromRows(List<Object[]> rows) {
        return rows.stream()
                .map(row -> new EventConfEventSummaryDto((Long) row[0], (String) row[1], (String) row[2],
                        (String) row[3], (Boolean) row[4], (Integer) row[5]))
                .collect(Collectors.toList());
    }
}
