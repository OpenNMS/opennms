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
package org.opennms.features.kafka.producer.shell;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.kafka.producer.datasync.AlarmDataStore;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;

@Command(scope = "opennms", name = "kafka-list-alarms", description = "Enumerates the alarms that are currently in the Kafka data store.")
@Service
public class ListAlarms implements Action {

    @Reference
    private AlarmDataStore alarmDataStore;

    @Option(name = "-k", aliases = "--reduction-key", description = "Lookup the alarm at a specific reduction key.")
    private String reductionKey;

    @Override
    public Object execute() {
        if (!SyncAlarms.waitForAlarmDataStore(alarmDataStore, 15, TimeUnit.SECONDS)) {
            return null;
        }

        // Get
        final Map<String, OpennmsModelProtos.Alarm> alarmsByReductionKey = new LinkedHashMap<>();
        if (reductionKey != null) {
            alarmsByReductionKey.put(reductionKey, alarmDataStore.getAlarm(reductionKey));
        } else {
            alarmsByReductionKey.putAll(alarmDataStore.getAlarms());
        }

        // Dump
        alarmsByReductionKey.forEach(this::printAlarm);

        return null;
    }

    private void printAlarm(String reductionKey, OpennmsModelProtos.Alarm alarm) {
        System.out.printf("%s\n\t%s\n", reductionKey, alarm != null ? alarm.getLastEvent().getLabel() : "(No alarm)");
    }

}
