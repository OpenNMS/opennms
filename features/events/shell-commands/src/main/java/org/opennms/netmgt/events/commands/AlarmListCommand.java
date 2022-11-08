/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAlarmCollection;

import com.google.common.base.Strings;

@Command(scope = "opennms", name = "alarm-list", description = "Lists current alarms meeting certain criteria")
@Service
public class AlarmListCommand implements Action {
    @Reference
    AlarmDao alarmDao;
    
    @Option(name="-l", aliases="--limit", description="Limit the number of alarms that are shown.")
    int limit = 10;

    @Argument(name="uei", description="Event UEI to match (exact).", required = false, multiValued = false)
    @Completion(EventUeiCompleter.class)
    String eventUeiMatch;

    @Override
    public Object execute() {
        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsAlarm.class)
                .orderBy("lastEventTime").desc()
                .limit(limit)
                .alias("node",  "node", JoinType.LEFT_JOIN);
        
        if (! Strings.isNullOrEmpty(eventUeiMatch)) {
            criteriaBuilder.eq("uei", eventUeiMatch);
        }

        final OnmsAlarmCollection alarmCollection = new OnmsAlarmCollection(alarmDao.findMatching(criteriaBuilder.toCriteria()));
        alarmCollection.setTotalCount(alarmDao.countMatching(criteriaBuilder.toCriteria()));
        System.out.println(String.format("Found %d alarms, showing %d:", alarmCollection.getTotalCount(), alarmCollection.size()));
        try {
            ShellTable table = new ShellTable();
            table.column("ID");
            table.column("Count");
            table.column("UEI");
            table.column("Severity");
            table.column("Last Event Time");
            table.column("Node Label");
            table.column("Ack User");
            table.column("Ack Time");
            for (OnmsAlarm alarm : alarmCollection) {
                table.addRow().addContent(alarm.getId(), alarm.getCounter(), alarm.getUei(), alarm.getSeverityLabel(), alarm.getLastEventTime(), alarm.getNodeLabel(), alarm.getAlarmAckUser(), alarm.getAlarmAckTime());
            }
            table.print(System.out);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }

        return null;
    }
}
