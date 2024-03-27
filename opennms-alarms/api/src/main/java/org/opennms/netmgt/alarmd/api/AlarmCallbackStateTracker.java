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
package org.opennms.netmgt.alarmd.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * This class can be used to help track callbacks issued via the {@link AlarmLifecycleListener}
 * in order to help simplify possible synchronization logic in {@link AlarmLifecycleListener#handleAlarmSnapshot(List)}.
 *
 * @author jwhite
 */
public class AlarmCallbackStateTracker {

    private final Set<Integer> alarmsUpdatesById = new HashSet<>();
    private final Set<String> alarmsUpdatesByReductionKey = new HashSet<>();

    private final Set<Integer> deletedAlarmsByAlarmId = new HashSet<>();
    private final Set<String> deletedAlarmsByReductionKey = new HashSet<>();

    private final List<Set<?>> sets = Arrays.asList(alarmsUpdatesById, alarmsUpdatesByReductionKey,
            deletedAlarmsByAlarmId, deletedAlarmsByReductionKey);

    private boolean trackAlarms = false;

    public synchronized void startTrackingAlarms() {
        trackAlarms = true;
    }

    public synchronized void trackNewOrUpdatedAlarm(int alarmId, String reductionKey) {
        if (!trackAlarms) {
            return;
        }
        alarmsUpdatesById.add(alarmId);
        alarmsUpdatesByReductionKey.add(reductionKey);
    }

    public synchronized void trackDeletedAlarm(int alarmId, String reductionKey) {
        if (!trackAlarms) {
            return;
        }
        deletedAlarmsByAlarmId.add(alarmId);
        deletedAlarmsByReductionKey.add(reductionKey);
    }

    public synchronized void resetStateAndStopTrackingAlarms() {
        trackAlarms = false;
        sets.forEach(Set::clear);
    }

    // By ID

    public synchronized boolean wasAlarmWithIdUpdated(int alarmId) {
        return alarmsUpdatesById.contains(alarmId);
    }

    public synchronized boolean wasAlarmWithIdDeleted(int alarmId) {
        return deletedAlarmsByAlarmId.contains(alarmId);
    }

    // By reduction key

    public synchronized boolean wasAlarmWithReductionKeyUpdated(String reductionKey) {
        return alarmsUpdatesByReductionKey.contains(reductionKey);
    }

    public synchronized boolean wasAlarmWithReductionKeyDeleted(String reductionKey) {
        return deletedAlarmsByReductionKey.contains(reductionKey);
    }

    public Set<Integer> getUpdatedAlarmIds() {
        return ImmutableSet.copyOf(alarmsUpdatesById);
    }
}
