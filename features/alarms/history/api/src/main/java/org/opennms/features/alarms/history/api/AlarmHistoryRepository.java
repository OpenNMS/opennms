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

package org.opennms.features.alarms.history.api;

import java.util.List;

public interface AlarmHistoryRepository {

    /**
     * Retrieves the last known state of the alarm with the given database id, at or before the given time.
     *
     * If the alarm was deleted at (or before) this time, the returned document will only
     * include mininimal information. To retrieve the complete state prior to the delete
     * you can perform another call to this this function with a time less than {@link AlarmState#getDeletedTime()}.
     *
     * @param id database id of the alarm to query
     * @param time timestamps in milliseconds
     * @return the last known state of the alarm, or {@code null} if none was found
     */
    AlarmState getAlarmWithDbIdAt(long id, long time);

    /**
     * Similar to {@link #getAlarmWithDbIdAt(long, long)}, except the lookup is performed using the reduction key.
     *
     * @param reductionKey reduction key of the alarm to query
     * @param time timestamps in milliseconds
     * @return the last known state of the alarm, or {@code null} if none was found
     */
    AlarmState getAlarmWithReductionKeyIdAt(String reductionKey, long time);

    /**
     * Retrieves all the known states for the alarm with the given database id.
     *
     * @param id database id of the alarm to query
     * @return the last known state of the alarm, or {@code null} if none was found
     */
    List<AlarmState> getStatesForAlarmWithDbId(long id);

    /**
     * Retrieves all the known states for the alarm with the given database id.
     *
     * @param reductionKey reduction key of the alarm to query
     * @return the last known state of the alarm, or {@code null} if none was found
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
     * Retrieves the last known state of all alarms which was recorded
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
