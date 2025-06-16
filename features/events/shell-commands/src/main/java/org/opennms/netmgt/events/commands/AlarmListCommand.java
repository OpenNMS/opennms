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
