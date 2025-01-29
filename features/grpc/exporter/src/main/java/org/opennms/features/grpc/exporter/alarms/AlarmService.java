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

package org.opennms.features.grpc.exporter.alarms;


import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.features.grpc.exporter.mapper.AlarmMapper;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.opennms.netmgt.model.OnmsAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Objects;

public class AlarmService {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmService.class);

    private final RuntimeInfo runtimeInfo;

    private final AlarmInventoryGrpcClient client;

    public AlarmService(
                        final RuntimeInfo runtimeInfo,
                        final AlarmInventoryGrpcClient client
    ) {
        this.runtimeInfo = Objects.requireNonNull(runtimeInfo);
        this.client = Objects.requireNonNull(client);
    }

    public void sendAlarmsSnapshot(final List<org.opennms.netmgt.model.OnmsAlarm> alarms) {
        final var alarmUpdates = AlarmMapper.INSTANCE.toAlarmUpdatesList(alarms, this.runtimeInfo, SystemInfoUtils.getInstanceId(),true);
        this.client.sendAlarmUpdate(alarmUpdates);
        LOG.info("Sent snapshot for {} alarms.", alarms.stream().count());
    }

    public void sendAddUpdateAlarms(final List<OnmsAlarm> onmsAlarms) {
        final var alarms = AlarmMapper.INSTANCE.toAlarmUpdatesList(onmsAlarms, this.runtimeInfo, SystemInfoUtils.getInstanceId(), false);
        this.client.sendAlarmUpdate(alarms);
    }
}
