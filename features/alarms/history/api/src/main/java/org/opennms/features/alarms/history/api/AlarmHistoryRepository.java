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
package org.opennms.features.alarms.history.api;

import java.util.List;
import java.util.Optional;

/**
 * Provides methods for querying the alarm history.
 */
public interface AlarmHistoryRepository {

    /**
     * Retrieves the last known state of the alarm with the given database id, at or before the given time.
     *
     * If the alarm was deleted at (or before) this time, the returned document will only
     * include minimal information. To retrieve the complete state prior to the delete
     * you can perform another call to this this function with a time less than {@link AlarmState#getDeletedTime()}.
     *
     * @param id database id of the alarm to query
     * @param time timestamps in milliseconds
     * @return the last known state of the alarm, or an empty {@code Optional} if none was found
     */
    Optional<AlarmState> getAlarmWithDbIdAt(long id, long time);

    /**
     * Similar to {@link #getAlarmWithDbIdAt(long, long)}, except the lookup is performed using the reduction key.
     *
     * @param reductionKey reduction key of the alarm to query
     * @param time timestamps in milliseconds
     * @return the last known state of the alarm, or an empty {@code Optional} if none was found
     */
    Optional<AlarmState> getAlarmWithReductionKeyIdAt(String reductionKey, long time);

    /**
     * Retrieves all the known states for the alarm with the given database id.
     *
     * @param id database id of the alarm to query
     * @return all the known states for the alarm, or an empty list if none were found
     */
    List<AlarmState> getStatesForAlarmWithDbId(long id);

    /**
     * Retrieves all the known states for the alarm with the given database reduction key.
     *
     * @param reductionKey reduction key of the alarm to query
     * @return all the known states for the alarm, or an empty list if none were found
     */
    List<AlarmState> getStatesForAlarmWithReductionKey(String reductionKey);

    /**
     * Retrieves the last known state of alarms which were active (and not yet deleted) at the given time.
     *
     * @param time timestamp in milliseconds
     * @return list of alarms
     */
    List<AlarmState> getActiveAlarmsAt(long time);

    /**
     * Retrieves the last known state of all alarms which were recorded
     * in the given time period.
     *
     * @param start timestamp in milliseconds (inclusive)
     * @param end timestamp in milliseconds (inclusive)
     * @return list of alarms
     */
    List<AlarmState> getLastStateOfAllAlarms(long start, long end);

    /**
     * Retrieves the number of alarms which were active at the given time.
     *
     * @param time timestamp in milliseconds
     * @return number of active alarms
     */
    long getNumActiveAlarmsAt(long time);

    /**
     * Retrieves the last known state of alarms which are currently active (and not yet deleted).
     *
     * @return list of alarms
     */
    List<AlarmState> getActiveAlarmsNow();

    /**
     * Retrieves the number of alarms which are currently active.
     *
     * @return number of active alarms
     */
    long getNumActiveAlarmsNow();
}
