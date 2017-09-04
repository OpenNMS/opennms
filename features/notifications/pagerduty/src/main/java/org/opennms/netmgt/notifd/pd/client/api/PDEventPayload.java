/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.notifd.pd.client.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PDEventPayload {

    protected static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'.'SS+0000";

    /**
     * A brief text summary of the event, used to generate the summaries/titles of any associated alerts.
     */
    @JsonProperty("summary")
    private String summary;

    /**
     * The unique location of the affected system, preferably a hostname or FQDN.
     */
    @JsonProperty("source")
    private String source;

    /**
     * The perceived severity of the status the event is describing with respect to the affected system.
     */
    @JsonProperty("severity")
    @JsonSerialize(using = PDEventSeveritySerializer.class)
    private PDEventSeverity severity;

    /**
     * The time at which the emitting tool detected or generated the event.
     */
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
    private Date timestamp;

    /**
     * Component of the source machine that is responsible for the event, for example mysql or eth0.
     */
    @JsonProperty("component")
    private String component;

    /**
     * Logical grouping of components of a service, for example app-stack.
     */
    @JsonProperty("group")
    private String group;

    /**
     * The class/type of the event, for example ping failure or cpu load
     */
    @JsonProperty("class")
    private String eventClass;

    /**
     * Additional details about the event and affected system.
     */
    @JsonProperty("custom_details")
    private Map<String, Object> customDetails = new LinkedHashMap<>();

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public PDEventSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(PDEventSeverity severity) {
        this.severity = severity;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getEventClass() {
        return eventClass;
    }

    public void setEventClass(String eventClass) {
        this.eventClass = eventClass;
    }

    public Map<String, Object> getCustomDetails() {
        return customDetails;
    }

    public void setCustomDetails(Map<String, Object> customDetails) {
        this.customDetails = customDetails;
    }
}
