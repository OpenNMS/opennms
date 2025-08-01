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
