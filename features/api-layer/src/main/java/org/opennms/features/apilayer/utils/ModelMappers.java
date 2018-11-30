/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.utils;

import org.opennms.features.apilayer.model.AlarmFeedbackBean;
import org.opennms.features.apilayer.model.AlarmBean;
import org.opennms.features.apilayer.model.DatabaseEventBean;
import org.opennms.features.apilayer.model.InMemoryEventBean;
import org.opennms.features.apilayer.model.NodeBean;
import org.opennms.features.apilayer.model.SnmpInterfaceBean;
import org.opennms.integration.api.v1.model.Alarm;
import org.opennms.integration.api.v1.model.AlarmFeedback;
import org.opennms.integration.api.v1.model.DatabaseEvent;
import org.opennms.integration.api.v1.model.EventParameter;
import org.opennms.integration.api.v1.model.InMemoryEvent;
import org.opennms.integration.api.v1.model.Node;
import org.opennms.integration.api.v1.model.Severity;
import org.opennms.integration.api.v1.model.SnmpInterface;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

/**
 * Utility functions for mapping to/from API types to OpenNMS types
 */
public class ModelMappers {

    public static Alarm toAlarm(OnmsAlarm alarm) {
        return new AlarmBean(alarm);
    }

    public static InMemoryEvent toEvent(Event event) {
        return new InMemoryEventBean(event);
    }

    public static Event toEvent(InMemoryEvent event) {
        final EventBuilder builder = new EventBuilder(event.getUei(), event.getSource());
        if (event.getSeverity() != null) {
            builder.setSeverity(OnmsSeverity.get(event.getSeverity().getId()).getLabel());
        }
        for (EventParameter p : event.getParameters()) {
            builder.setParam(p.getName(), p.getValue());
        }
        return builder.getEvent();
    }

    public static DatabaseEvent toEvent(OnmsEvent event) {
        if (event == null) {
            return null;
        }
        return new DatabaseEventBean(event);
    }

    public static Node toNode(OnmsNode node) {
        if (node == null) {
            return null;
        }
        return new NodeBean(node);
    }

    public static SnmpInterface toSnmpInterface(OnmsSnmpInterface snmpInterface) {
        if (snmpInterface == null) {
            return null;
        }
        return new SnmpInterfaceBean(snmpInterface);
    }

    public static Severity toSeverity(OnmsSeverity severity) {
        if (severity == null) {
            return null;
        }
        switch (severity) {
            case CLEARED:
                return Severity.CLEARED;
            case NORMAL:
                return Severity.NORMAL;
            case WARNING:
                return Severity.WARNING;
            case MINOR:
                return Severity.MINOR;
            case MAJOR:
                return Severity.MAJOR;
            case CRITICAL:
                return Severity.CRITICAL;
        }
        return Severity.INDETERMINATE;
    }
    
    public static AlarmFeedback toFeedback(org.opennms.features.situationfeedback.api.AlarmFeedback feedback) {
        return feedback == null ? null : new AlarmFeedbackBean(feedback);
    }
}
