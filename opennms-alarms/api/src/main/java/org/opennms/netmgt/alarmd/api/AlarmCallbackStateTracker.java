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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class can be used to help track callbacks issued via the {@link AlarmLifecycleListener}
 * in order to help simplify possible synchronization logic in {@link AlarmLifecycleListener#handleAlarmSnapshot(List, long)}.
 *
 * Calls to updates and deletes should be tracked using the track* methods.
 * expireEntriesBefore should be called when handling a snapshot to evict older state.
 * The was* methods can then be used to determine whehter or not a particular callback occured
 * since the snapshot was taken.
 *
 * @author jwhite
 */
public class AlarmCallbackStateTracker {

    private final Map<Integer, Long> alarmsUpdatesById = new ConcurrentHashMap<>();
    private final Map<String, Long> alarmsUpdatesByReductionKey = new ConcurrentHashMap<>();

    private final Map<Integer, Long> deletedAlarmsByAlarmId = new ConcurrentHashMap<>();
    private final Map<String, Long> deletedAlarmsByReductionKey = new ConcurrentHashMap<>();

    private final List<Map<?, Long>> maps = Arrays.asList(alarmsUpdatesById, alarmsUpdatesByReductionKey,
            deletedAlarmsByAlarmId, deletedAlarmsByReductionKey);

    public void trackNewOrUpdatedAlarm(int alarmId, String reductionKey) {
        final long now = System.currentTimeMillis();
        alarmsUpdatesById.put(alarmId, now);
        alarmsUpdatesByReductionKey.put(reductionKey, now);
    }

    public void trackDeletedAlarm(int alarmId, String reductionKey) {
        final long now = System.currentTimeMillis();
        deletedAlarmsByAlarmId.put(alarmId, now);
        deletedAlarmsByReductionKey.put(reductionKey, now);
    }

    public void expireEntriesBefore(long systemMillis) {
        // Remove any entries from all of the maps that happened before the given time
        maps.forEach(map -> map.values().removeIf(deletedAlarmAt -> deletedAlarmAt < systemMillis));
    }

    // By ID

    public boolean wasAlarmWithIdUpdatedOnOrAfter(int alarmId, long systemMillis) {
        return isOnOrAfter(alarmsUpdatesById.get(alarmId), systemMillis);
    }

    public boolean wasAlarmWithIdDeletedOnOrAfter(int alarmId, long systemMillis) {
        return isOnOrAfter(deletedAlarmsByAlarmId.get(alarmId), systemMillis);
    }

    // By reduction key

    public boolean wasAlarmWithReductionKeyUpdatedOnOrAfter(String reductionKey, long systemMillis) {
        return isOnOrAfter(alarmsUpdatesByReductionKey.get(reductionKey), systemMillis);
    }

    public boolean wasAlarmWithReductionKeyDeletedOnOrAfter(String reductionKey, long systemMillis) {
        return isOnOrAfter(deletedAlarmsByReductionKey.get(reductionKey), systemMillis);
    }

    private boolean isOnOrAfter(Long t1, long t2) {
        return t1 != null && t1 > t2;
    }

}
