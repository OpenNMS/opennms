/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.api;

import java.util.Date;
import java.util.List;

import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.alarm.AlarmSummary;

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

}
