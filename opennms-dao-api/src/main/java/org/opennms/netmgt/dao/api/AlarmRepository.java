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
package org.opennms.netmgt.dao.api;

import java.util.Date;
import java.util.List;

import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.netmgt.model.alarm.SituationSummary;

/**
 * WebAlarmRepository @author brozow
 */
public interface AlarmRepository {

    /**
     * Count alarms matching a criteria.
     *
     * @param criteria the criteria
     * @return the amount of alarms
     */
    public abstract int countMatchingAlarms(OnmsCriteria criteria);

    /**
     * Count alarms by severity matching a specific criteria.
     *
     * @param criteria the criteria
     * @return an array with the amount of alarms per severity.
     */
    public abstract int[] countMatchingAlarmsBySeverity(OnmsCriteria criteria);

    /**
     * Gets and alarm.
     *
     * @param alarmId the alarm id
     * @return the alarm object
     */
    public abstract OnmsAlarm getAlarm(int alarmId);

    /**
     * Gets alarms matching a specific criteria.
     *
     * @param criteria the criteria
     * @return a array with matching alarms
     */
    public abstract OnmsAlarm[] getMatchingAlarms(OnmsCriteria criteria);

    /**
     * Acknowledge alarms matching a specific criteria.
     *
     * @param user the user
     * @param timestamp the timestamp
     * @param criteria the criteria
     */
    public abstract void acknowledgeMatchingAlarms(String user,
            Date timestamp, OnmsCriteria criteria);

    /**
     * Acknowledge Alarms.
     *
     * @param alarmIds an array of alarms ID
     * @param user the user
     * @param timestamp the timestamp
     */
    public void acknowledgeAlarms(int[] alarmIds, String user, Date timestamp);

    /**
     * Acknowledge all the alarms.
     *
     * @param user the user
     * @param timestamp the timestamp
     */
    public abstract void acknowledgeAll(String user, Date timestamp);

    /**
     * Unacknowledge Matching Alarms</p>.
     *
     * @param criteria the criteria
     * @param user the user
     */
    public abstract void unacknowledgeMatchingAlarms(OnmsCriteria criteria, String user);

    /**
     * Unacknowledge Alarms
     *
     * @param alarmIds an array of alarms ID
     * @param user the user
     */
    public void unacknowledgeAlarms(int[] alarmIds, String user);

    /**
     * Unacknowledge all the alarms.
     *
     * @param user the user
     */
    public abstract void unacknowledgeAll(String user);

    /**
     * Escalate Alarms.
     *
     * @param alarmIds an array of alarms ID
     * @param user the user
     * @param timestamp the timestamp
     */
    public abstract void escalateAlarms(int[] alarmIds, String user, Date timestamp);

    /**
     * Clear Alarms
     *
     * @param alarmIds an array of alarms ID
     * @param user the user
     * @param timestamp the timestamp
     */
    public abstract void clearAlarms(int[] alarmIds, String user, Date timestamp);

    /**
     * Updates the StickyMemo of the alarm to persistence.
     * 
     * @param alarmId the alarmId of the alarm where the StickyMemo has to be persisted.
     */
    public abstract void updateStickyMemo(Integer alarmId, String body, String user);

    /**
     * Updates the ReductionKeyMemo of the alarm to persistence.
     * ReductionKeyMemo aka JournalMemo.
     *
     * @param alarmId the alarm id
     * @param body the body
     * @param user the user
     */
    public abstract void updateReductionKeyMemo(Integer alarmId, String body, String user);

    /**
     * Removes the sticky memo.
     *
     * @param alarmId the alarm id
     */
    public abstract void removeStickyMemo(Integer alarmId);

    /**
     * Removes the reduction key memo.
     *
     * @param alarmId the alarm id
     */
    public abstract void removeReductionKeyMemo(int alarmId);

    /**
     * Gets the acknowledgments.
     *
     * @param alarmId the alarm id
     * @return the acknowledgments
     */
    public abstract List<OnmsAcknowledgment> getAcknowledgments(int alarmId);

    /**
     * Gets the current node alarm summaries.
     *
     * @return the current node alarm summaries
     */
    public abstract List<AlarmSummary> getCurrentNodeAlarmSummaries();

    /**
     * Gets the current node alarm summaries.
     *
     * @return the current node alarm summaries
     */
    List<SituationSummary> getCurrentSituationSummaries();
}
