/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.AlarmSeverity;


public class AlarmSeverityUpdatedEvent extends OpenNMSEvent<AlarmSeverityUpdatedEventHandler> {
    public static Type<AlarmSeverityUpdatedEventHandler> TYPE = new Type<>();
    private final AlarmSeverity m_severity;

    public AlarmSeverityUpdatedEvent(final AlarmSeverity severity) {
        m_severity = severity;
    }

    public AlarmSeverity getSeverity() {
        return m_severity;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<AlarmSeverityUpdatedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    public void dispatch(final AlarmSeverityUpdatedEventHandler handler) {
        handler.onAlarmSeverityUpdated(this);
    }

    @Override
    public String toDebugString() {
        return "event: AlarmSeverityUpdatedEvent: severity=" + m_severity;
    }
}
