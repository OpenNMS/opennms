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

import java.util.List;

import org.opennms.netmgt.model.OnmsAlarm;

/**
 * A simplified version of the {@link org.opennms.netmgt.dao.api.AlarmEntityListener} interface with support
 * for periodic snapshots.
 *
 * @author jwhite
 */
public interface AlarmLifecycleListener {

    /**
     * Called periodically with a complete set of alarms as present in the database
     * at the given timestamp.
     *
     * This should be used to synchronize any state to ensure it matches what is currently
     * in the database.
     *
     * Note that it is possible that the *current* state of alarms is different from the state
     * at the time at which the snapshot was taken. Implementations should take this in consideration
     * when performing any state synchronization.
     *
     * This method will be called while the related session & transaction that created
     * the alarm are still open.
     *
     * All of the listeners are invoked serially, so the implementors should avoid
     * blocking when possible.
     *
     * @param alarms canonical set of alarms in the database
     */
    void handleAlarmSnapshot(List<OnmsAlarm> alarms);

    /**
     * Called before the transaction is opened and the alarms are read for subsequent
     * calls to {@link #handleAlarmSnapshot}.
     *
     * This can be used to trigger any necessary state tracking to accurately handle
     * the snapshot results.
     */
    void preHandleAlarmSnapshot();

    /**
     * Called after {@link #handleAlarmSnapshot} has been called on all the listeners, and
     * after the session & transaction used to perform the snapshot has been closed.
     *
     * This can be used to trigger any necessary post-processing of the results once
     * the related session has been closed.
     *
     * This function may be called immediately after a call to {@link #preHandleAlarmSnapshot} if
     * an error occurred while preparing the snapshot i.e. when opening the transaction.
     */
    void postHandleAlarmSnapshot();

    /**
     * Called when an alarm has been created or updated.
     *
     * This method will be called while the related session & transaction that created
     * the alarm are still open.
     *
     * All of the listeners are invoked serially, so the implementors should avoid
     * blocking when possible.
     *
     * @param alarm a newly created or updated alarm
     */
    void handleNewOrUpdatedAlarm(OnmsAlarm alarm);

    /**
     * Called when an alarm has been deleted.
     *
     * This method will be called while the related session & transaction that created
     * the alarm are still open.
     *
     * All of the listeners are invoked serially, so the implementors should avoid
     * blocking when possible.
     *
     * @param alarmId id of the alarm that was deleted
     * @param reductionKey reduction key of the alarm that was deleted
     */
    void handleDeletedAlarm(int alarmId, String reductionKey);

}
