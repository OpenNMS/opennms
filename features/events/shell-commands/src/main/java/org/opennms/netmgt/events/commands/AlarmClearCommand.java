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

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.introspection.JexlPermissions;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.integration.api.v1.dao.AlarmDao;
import org.opennms.integration.api.v1.model.Alarm;

import java.util.List;

@Command(scope = "opennms", name = "alarm-clear", description = "Clear alarms either by alarmId or by matching JEXL expression. Use 'opennms:alarm-list' to see alarm details")
@Service
public class AlarmClearCommand implements Action {

    @Reference
    private AlarmDao alarmDao;

    @Option(name = "-e", aliases = "--expression", description = "Clear alarms that match this JEXL expression, e.g. 'alarm.reductionKey =~ \".*dataCollectionFailed.*\"' ", required = false, multiValued = false)
    private String expression;

    @Option(name = "-i", aliases = "--alarm-id", description = "Clear this alarm ID. Can be provided multiple times, e.g. '-i 10 -i 12 -i 13'", required = false, multiValued = true)
    private List<Integer> alarmIds;

    @Option(name = "-v", aliases = "--verbose", description = "Be verbose; display matching alarm objects.", required = false)
    private boolean verbose = false;

    @Override
    public Object execute() {
        JexlEngine jexl = new JexlBuilder().permissions(JexlPermissions.RESTRICTED.compose("org.opennms.integration.api.v1.model.*")).create();
        JexlExpression e = null;

        if (expression == null && (alarmIds == null || alarmIds.isEmpty())) {
            System.out.printf("Either JEXL expression or alarm id must be provided!");
            return null;
        }
        if (expression != null) {
            e = jexl.createExpression(expression);
        }

        int numAlarmsProcessed = 0;
        boolean didMatchAtLeastOneAlarm = false;
        boolean alarmIdMatched = false;
        int matchedAlarmCount = 0;

        for (Alarm alarm : alarmDao.getAlarms()) {
            numAlarmsProcessed++;

            // handle expressions
            if (e != null && testAlarmAgainstExpression(e, alarm)) {
                matchedAlarmCount++;
                didMatchAtLeastOneAlarm = true;
                if (verbose) {
                    System.out.printf("Clearing matched alarm object: '%s'\n", alarm);
                } else {
                    System.out.printf("Clearing alarm with ID '%d' and reduction key: '%s'\n", alarm.getId(), alarm.getReductionKey());
                }
                alarmDao.clear(alarm.getId());
            }
            // handle multiple ids
            if (alarmIds != null) {
                for (Integer alarmId : alarmIds) {
                    if (alarm.getId().equals(alarmId)) {
                        matchedAlarmCount++;
                        alarmIdMatched = true;
                        didMatchAtLeastOneAlarm = true;
                        if (verbose) {
                            System.out.printf("Clearing matched alarm object '%s'", alarm);
                        } else {
                            System.out.printf("Clearing alarm with ID '%d' and reduction key: '%s'\n", alarm.getId(), alarm.getReductionKey());
                        }
                        alarmDao.clear(alarm.getId());
                    }
                }
            }
        }

        if (numAlarmsProcessed < 1) {
            System.out.println("\nNo alarms present.\n");
        } else if (!didMatchAtLeastOneAlarm) {
            System.out.printf("\nNo alarms matched (out of %d alarms.)\n", numAlarmsProcessed);
        } else if (alarmIds != null && !alarmIdMatched) {
            System.out.print("\nNo alarms matching the provided IDs were found!\n");
        } else if (didMatchAtLeastOneAlarm && matchedAlarmCount > 0) {
            System.out.printf("\nMatched and cleared %d alarms (out of %d alarms.)\n", matchedAlarmCount, numAlarmsProcessed);
        }
        return null;
    }

    private static boolean testAlarmAgainstExpression(JexlExpression expression, Alarm alarm) {
        final JexlContext jc = new MapContext();
        jc.set("alarm", alarm);
        return (boolean)expression.evaluate(jc);
    }
}

