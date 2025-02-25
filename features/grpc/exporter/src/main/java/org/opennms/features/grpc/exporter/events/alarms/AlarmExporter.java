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

package org.opennms.features.grpc.exporter.events.alarms;

import org.opennms.features.grpc.exporter.alarms.AlarmService;
import org.opennms.netmgt.alarmd.api.AlarmLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Objects;

public class AlarmExporter implements AlarmLifecycleListener {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmExporter.class);

    private final AlarmService alarmService;

    public AlarmExporter(final AlarmService alarmService) {
        this.alarmService = Objects.requireNonNull(alarmService);
    }

    @Override
    public void handleAlarmSnapshot(List<org.opennms.netmgt.model.OnmsAlarm> alarms) {
            this.alarmService.sendAlarmsSnapshot(alarms);
    }

    @Override
    public void preHandleAlarmSnapshot() {
    }

    @Override
    public void postHandleAlarmSnapshot() {
    }

    @Override
    public void handleNewOrUpdatedAlarm(org.opennms.netmgt.model.OnmsAlarm alarm) {
        this.alarmService.sendAddUpdateAlarms(List.of(alarm));
    }

    @Override
    public void handleDeletedAlarm(int alarmId, String reductionKey) {
    }

}
