/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.events.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Terminal;
import org.apache.karaf.shell.support.table.ShellTable;
import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;

@Command(scope = "opennms", name = "event", description = "Show event details for a given id")
@Service
public class EventCommand implements Action {

    @Reference
    public EventDao eventDao;

    @Argument(name = "id", description = "Event Id to match (exact). If no argument is given, the most recent event will be displayed.", required = false, multiValued = false)
    Integer id;

    @Reference
    Terminal terminal;

    @Override
    public Object execute() {
        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsEvent.class)
                .orderBy("eventTime").desc()
                .alias("node", "node", Alias.JoinType.LEFT_JOIN)
                .alias("alarm", "alarm", Alias.JoinType.LEFT_JOIN)
                .alias("eventParameters", "event_parameters", Alias.JoinType.LEFT_JOIN)
                .alias("serviceType", "serviceType", Alias.JoinType.LEFT_JOIN)
                .orderBy("id", false)
                .limit(1);

        if (id != null) {
            criteriaBuilder = criteriaBuilder.eq("id", id);
        }

        final var onmsEvents = eventDao.findMatching(criteriaBuilder.toCriteria());

        if (onmsEvents.size() == 0) {
            return null;
        }

        final OnmsEvent onmsEvent = onmsEvents.get(0);

        final ShellTable eventPropertyTable = new ShellTable();
        eventPropertyTable.size(terminal.getWidth() - 1);
        eventPropertyTable.column("Property");
        eventPropertyTable.column("Value");

        fillTable(eventPropertyTable, "eventUei", onmsEvent.getEventUei());
        fillTable(eventPropertyTable, "eventTime", onmsEvent.getEventTime());
        fillTable(eventPropertyTable, "eventHost", onmsEvent.getEventHost());
        fillTable(eventPropertyTable, "eventSource", onmsEvent.getEventSource());
        fillTable(eventPropertyTable, "ipAddr", onmsEvent.getIpAddr());
        fillTable(eventPropertyTable, "distPoller", onmsEvent.getDistPoller());
        fillTable(eventPropertyTable, "eventSnmpHost", onmsEvent.getEventSnmpHost());
        fillTable(eventPropertyTable, "serviceType", onmsEvent.getServiceType());
        fillTable(eventPropertyTable, "eventSnmp", onmsEvent.getEventSnmp());
        fillTable(eventPropertyTable, "eventCreateTime", onmsEvent.getEventCreateTime());
        fillTable(eventPropertyTable, "eventDescr", onmsEvent.getEventDescr());
        fillTable(eventPropertyTable, "eventLog", onmsEvent.getEventLog());
        fillTable(eventPropertyTable, "eventLogGroup", onmsEvent.getEventLogGroup());
        fillTable(eventPropertyTable, "eventLogMsg", onmsEvent.getEventLogMsg());
        fillTable(eventPropertyTable, "eventSeverity", onmsEvent.getEventSeverity());
        fillTable(eventPropertyTable, "ifIndex", onmsEvent.getIfIndex());
        fillTable(eventPropertyTable, "eventPathOutage", onmsEvent.getEventPathOutage());
        fillTable(eventPropertyTable, "eventCorrelation", onmsEvent.getEventCorrelation());
        fillTable(eventPropertyTable, "eventSurpressedCount", onmsEvent.getEventSuppressedCount());
        fillTable(eventPropertyTable, "eventOperInstruct", onmsEvent.getEventOperInstruct());
        fillTable(eventPropertyTable, "eventAutoAction", onmsEvent.getEventAutoAction());
        fillTable(eventPropertyTable, "eventOperAction", onmsEvent.getEventOperAction());
        fillTable(eventPropertyTable, "eventOperActionMenuText", onmsEvent.getEventOperActionMenuText());
        fillTable(eventPropertyTable, "eventNotification", onmsEvent.getEventNotification());
        fillTable(eventPropertyTable, "eventTTicket", onmsEvent.getEventTTicket());
        fillTable(eventPropertyTable, "eventTTicketState", onmsEvent.getEventTTicketState());
        fillTable(eventPropertyTable, "eventForward", onmsEvent.getEventForward());
        fillTable(eventPropertyTable, "eventMouseOverText", onmsEvent.getEventMouseOverText());
        fillTable(eventPropertyTable, "eventDisplay", onmsEvent.getEventDisplay());
        fillTable(eventPropertyTable, "eventAckUser", onmsEvent.getEventAckUser());
        fillTable(eventPropertyTable, "eventAckTime", onmsEvent.getEventAckTime());
        fillTable(eventPropertyTable, "alarm", onmsEvent.getAlarm());
        fillTable(eventPropertyTable, "node", onmsEvent.getNode());

        System.out.println("\nEvent Properties for Event #" + onmsEvent.getId() + ":\n");
        eventPropertyTable.print(System.out);

        final ShellTable eventParameterTable = new ShellTable();
        eventParameterTable.size(terminal.getWidth() - 1);
        eventParameterTable.column("Name");
        eventParameterTable.column("Type");
        eventParameterTable.column("Value");

        for (final OnmsEventParameter onmsEventParameter : onmsEvent.getEventParameters()) {
            eventParameterTable.addRow().addContent(onmsEventParameter.getName(), onmsEventParameter.getType(), onmsEventParameter.getValue());
        }

        System.out.println("\nEvent Parameters for Event #" + onmsEvent.getId() + ":\n");
        eventParameterTable.print(System.out);

        return null;
    }

    private void fillTable(final ShellTable table, final String name, final Object object) {
        String value;
        if (object == null) {
            value = "<not set>";
        } else {
            switch (name) {
                case "eventSeverity":
                    value = object + " (" + OnmsSeverity.get((Integer) object) + ")";
                    break;
                case "distPoller":
                    value = String.valueOf(((OnmsDistPoller) object).getId());
                    break;
                case "serviceType":
                    value = String.valueOf(((OnmsServiceType) object).getName());
                    break;
                case "alarm":
                    value = String.valueOf(((OnmsAlarm) object).getId());
                    break;
                case "node":
                    value = String.valueOf(((OnmsNode) object).getId());
                    break;
                default:
                    value = object.toString();
                    break;
            }
        }
        table.addRow().addContent(name, value);
    }
}
