/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
