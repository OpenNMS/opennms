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

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.LogDestType;
import org.opennms.netmgt.xml.eventconf.Logmsg;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The part of an event definition the Event Logs tab edits for a mapping's UEI:
 * label, description, log message, severity and whether it raises an alarm.
 * Everything else in a definition that already exists (mask, varbinds, actions)
 * is kept as it is.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WsmanEventLogDefinitionDto {

    public static final String DEFAULT_REDUCTION_KEY = "%uei%:%dpname%:%nodeid%";
    public static final int ALARM_TYPE_PROBLEM = 1;
    public static final int ALARM_TYPE_RESOLUTION = 2;
    public static final int ALARM_TYPE_PROBLEM_WITHOUT_RESOLUTION = 3;

    public String uei;
    public boolean exists;
    /** Whether the Event Logs tab may write this definition: it is missing or lives in the daemon's own source. */
    public boolean editable;
    public String sourceName;
    public Long sourceId;
    public Long eventId;
    public String label;
    public String description;
    public String logMessage;
    public String severity;
    public boolean alarm;
    public Integer alarmType;
    public String reductionKey;

    public static class Rows {
        public List<WsmanEventLogDefinitionDto> rows = new ArrayList<>();
    }

    public static WsmanEventLogDefinitionDto missing(final String uei) {
        final WsmanEventLogDefinitionDto dto = new WsmanEventLogDefinitionDto();
        dto.uei = uei;
        dto.exists = false;
        dto.editable = true;
        return dto;
    }

    public static WsmanEventLogDefinitionDto from(final EventConfEvent row, final Event event, final String editableSource) {
        final WsmanEventLogDefinitionDto dto = new WsmanEventLogDefinitionDto();
        dto.uei = row.getUei();
        dto.exists = true;
        dto.sourceName = row.getSource() != null ? row.getSource().getName() : null;
        dto.editable = editableSource != null && editableSource.equals(dto.sourceName);
        dto.sourceId = row.getSource() != null ? row.getSource().getId() : null;
        dto.eventId = row.getId();
        dto.label = event.getEventLabel();
        dto.description = event.getDescr();
        dto.logMessage = event.getLogmsg() != null ? event.getLogmsg().getContent() : null;
        dto.severity = event.getSeverity();
        final AlarmData alarmData = event.getAlarmData();
        dto.alarm = alarmData != null;
        if (alarmData != null) {
            dto.alarmType = alarmData.getAlarmType();
            dto.reductionKey = alarmData.getReductionKey();
        }
        return dto;
    }

    /** Applies the editable fields to {@code event}, which is either new or the parsed existing definition. */
    public void applyTo(final Event event) {
        event.setUei(uei.trim());
        event.setEventLabel(label.trim());
        event.setDescr(description == null || description.trim().isEmpty() ? label.trim() : description.trim());
        final Logmsg logmsg = event.getLogmsg() != null ? event.getLogmsg() : new Logmsg();
        logmsg.setContent(logMessage == null || logMessage.trim().isEmpty() ? label.trim() : logMessage.trim());
        if (logmsg.getDest() == null) {
            logmsg.setDest(LogDestType.LOGNDISPLAY);
        }
        event.setLogmsg(logmsg);
        event.setSeverity(severity.trim());
        if (!alarm) {
            event.setAlarmData(null);
            return;
        }
        final AlarmData alarmData = event.getAlarmData() != null ? event.getAlarmData() : new AlarmData();
        alarmData.setAlarmType(alarmType != null ? alarmType : ALARM_TYPE_PROBLEM_WITHOUT_RESOLUTION);
        alarmData.setReductionKey(reductionKey == null || reductionKey.trim().isEmpty() ? DEFAULT_REDUCTION_KEY : reductionKey.trim());
        if (alarmData.getAutoClean() == null) {
            alarmData.setAutoClean(false);
        }
        event.setAlarmData(alarmData);
    }
}
