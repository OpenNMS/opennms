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

package org.opennms.netmgt.events.commands;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
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

    @Option(name = "-v", aliases = "--verbose", description = "Be verbose; display matching alarm objects", required = false)
    private boolean verbose = false;

    @Option(name = "-d", aliases = "--dry-run", description = "Show matches, but don't actually clear anything", required = false)
    private boolean dryrun = false;

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
                if (!dryrun) {
                    alarmDao.clear(alarm.getId());
                }
            }
            // handle multiple ids
            if (alarmIds != null) {
                for (Integer alarmId : alarmIds) {
                    if (alarm.getId().equals(alarmId)) {
                        matchedAlarmCount++;
                        alarmIdMatched = true;
                        didMatchAtLeastOneAlarm = true;
                        if (verbose) {
                            System.out.printf("Clearing matched alarm object '%s'\n", alarm);
                        } else {
                            System.out.printf("Clearing alarm with ID '%d' and reduction key: '%s'\n", alarm.getId(), alarm.getReductionKey());
                        }
                        if (!dryrun) {
                            alarmDao.clear(alarm.getId());
                        }
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
        Object result;
        jc.set("alarm", alarm);
        try {
            result = expression.evaluate(jc);
            if (result instanceof Boolean ) {
                return (boolean)result;
            }
        } catch (JexlException ex) {
            System.out.printf("Error evaluating expression: %s \n", ex);
        }
        return false;
    }
}

