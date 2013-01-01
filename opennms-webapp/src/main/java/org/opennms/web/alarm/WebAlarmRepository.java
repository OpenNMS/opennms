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

package org.opennms.web.alarm;

import java.util.Date;
import java.util.List;

import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.web.alarm.filter.AlarmCriteria;

/*
 * WebAlarmRepository @author brozow
 */
/**
 * <p>WebAlarmRepository interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface WebAlarmRepository {

    /**
     * <p>countMatchingAlarms</p>
     *
     * @param criteria a {@link org.opennms.web.alarm.filter.AlarmCriteria}
     * object.
     * @return a int.
     */
    public abstract int countMatchingAlarms(AlarmCriteria criteria);

    /**
     * <p>countMatchingAlarmsBySeverity</p>
     *
     * @param criteria a {@link org.opennms.web.alarm.filter.AlarmCriteria}
     * object.
     * @return an array of int.
     */
    public abstract int[] countMatchingAlarmsBySeverity(AlarmCriteria criteria);

    /**
     * <p>getAlarm</p>
     *
     * @param alarmId a int.
     * @return a {@link org.opennms.web.alarm.Alarm} object.
     */
    public abstract Alarm getAlarm(int alarmId);

    /**
     * <p>getMatchingAlarms</p>
     *
     * @param criteria a {@link org.opennms.web.alarm.filter.AlarmCriteria}
     * object.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     */
    public abstract Alarm[] getMatchingAlarms(AlarmCriteria criteria);

    /**
     * <p>acknowledgeMatchingAlarms</p>
     *
     * @param user a {@link java.lang.String} object.
     * @param timestamp a java$util$Date object.
     * @param criteria a {@link org.opennms.web.alarm.filter.AlarmCriteria}
     * object.
     */
    public abstract void acknowledgeMatchingAlarms(String user,
            Date timestamp, AlarmCriteria criteria);

    /**
     * <p>acknowledgeAlarms</p>
     *
     * @param alarmIds an array of int.
     * @param user a {@link java.lang.String} object.
     * @param timestamp a java$util$Date object.
     */
    public void acknowledgeAlarms(int[] alarmIds, String user, Date timestamp);

    /**
     * <p>acknowledgeAll</p>
     *
     * @param user a {@link java.lang.String} object.
     * @param timestamp a java$util$Date object.
     */
    public abstract void acknowledgeAll(String user, Date timestamp);

    /**
     * <p>unacknowledgeMatchingAlarms</p>
     *
     * @param criteria a {@link org.opennms.web.alarm.filter.AlarmCriteria}
     * object.
     * @param user a {@link java.lang.String} object.
     */
    public abstract void unacknowledgeMatchingAlarms(AlarmCriteria criteria, String user);

    /**
     * <p>unacknowledgeAlarms</p>
     *
     * @param alarmIds an array of int.
     * @param user a {@link java.lang.String} object.
     */
    public void unacknowledgeAlarms(int[] alarmIds, String user);

    /**
     * <p>unacknowledgeAll</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public abstract void unacknowledgeAll(String user);

    /**
     * <p>escalateAlarms</p>
     *
     * @param alarmIds an array of int.
     * @param user a {@link java.lang.String} object.
     * @param timestamp a java$util$Date object.
     */
    public abstract void escalateAlarms(int[] alarmIds, String user, Date timestamp);

    /**
     * <p>clearAlarms</p>
     *
     * @param alamrIds an array of int.
     * @param user a {@link java.lang.String} object.
     * @param timestamp a java$util$Date object.
     */
    public abstract void clearAlarms(int[] alamrIds, String user, Date timestamp);

    /**
     * Updates the StickyMemo of the alarm to persistence
     * @param alarmId the alarmId of the alarm where the StickyMemo has to be persisted.
     */
    public void updateStickyMemo(Integer alarmId, String body, String user);

    /**
     * Updates the ReductionKeyMemo of the alarm to persistence.
     * ReductionKeyMemo aka JournalMemo
     */
    public void updateReductionKeyMemo(Integer alarmId, String body, String user);

    public void removeStickyMemo(Integer alarmId);

    public void removeReductionKeyMemo(int alarmId);
    
    public List<OnmsAcknowledgment> getAcknowledgments(int alarmId);
}
