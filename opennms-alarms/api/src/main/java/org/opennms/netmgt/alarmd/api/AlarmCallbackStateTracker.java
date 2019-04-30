/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
