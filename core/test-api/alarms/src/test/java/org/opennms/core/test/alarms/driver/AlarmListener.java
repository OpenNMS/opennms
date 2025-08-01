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
package org.opennms.core.test.alarms.driver;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.alarmd.api.AlarmLifecycleListener;
import org.opennms.netmgt.model.OnmsAlarm;

public class AlarmListener implements AlarmLifecycleListener {

    private static AlarmListener instance = new AlarmListener();

    private final Set<Integer> allObservedAlarmIds = new LinkedHashSet<>();

    private AlarmListener() {}

    public static AlarmListener getInstance() {
        return instance;
    }

    @Override
    public synchronized void handleAlarmSnapshot(List<OnmsAlarm> alarms) {
        alarms.forEach(a -> allObservedAlarmIds.add(a.getId()));
    }

    @Override
    public synchronized void preHandleAlarmSnapshot() {
        // pass
    }

    @Override
    public synchronized void postHandleAlarmSnapshot() {
        // pass
    }

    @Override
    public synchronized void handleNewOrUpdatedAlarm(OnmsAlarm alarm) {
        allObservedAlarmIds.add(alarm.getId());
    }

    @Override
    public synchronized void handleDeletedAlarm(int alarmId, String reductionKey) {
        allObservedAlarmIds.add(alarmId);
    }

    public synchronized Integer getNumUniqueObserveredAlarmIds() {
        return allObservedAlarmIds.size();
    }
}
