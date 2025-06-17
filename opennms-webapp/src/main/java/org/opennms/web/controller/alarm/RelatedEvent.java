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
package org.opennms.web.controller.alarm;

import java.util.Date;

import org.opennms.netmgt.model.OnmsSeverity;

public class RelatedEvent {
    final Integer eventId;
    final Integer alarmId;
    final Date creationTime;
    final OnmsSeverity severity;
    final String uei;
    final String logMessage;

    public RelatedEvent(final Integer eventId, final Integer alarmId, final Date creationTime, final OnmsSeverity severity, final String uei, final String logMessage) {
        this.eventId = eventId;
        this.alarmId = alarmId;
        this.creationTime = creationTime;
        this.severity = severity;
        this.uei = uei;
        this.logMessage = logMessage;
    }

    public Integer getEventId() {
        return eventId;
    }

    public Integer getAlarmId() {
        return alarmId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public OnmsSeverity getSeverity() {
        return severity;
    }

    public String getUei() {
        return uei;
    }

    public String getLogMessage() {
        return logMessage;
    }
}
