/*
 * This file is part of the OpenNMS(R) Application. OpenNMS(R) is Copyright
 * (C) 2009 The OpenNMS Group, Inc. All rights reserved. OpenNMS(R) is a
 * derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights
 * for modified and included code are below. OpenNMS(R) is a registered
 * trademark of The OpenNMS Group, Inc. Original code base Copyright (C)
 * 1999-2001 Oculan Corp. All rights reserved. This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version
 * 2 of the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place - Suite 330, Boston, MA 02111-1307, USA. For more information
 * contact: OpenNMS Licensing <license@opennms.org> http://www.opennms.org/
 * http://www.opennms.com/
 */
package org.opennms.web.alarm;

import java.util.Date;

import org.opennms.web.alarm.filter.AlarmCriteria;

/*
 * WebAlarmRepository
 * @author brozow
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
     * @param criteria a {@link org.opennms.web.alarm.filter.AlarmCriteria} object.
     * @return a int.
     */
    public abstract int countMatchingAlarms(AlarmCriteria criteria);

    /**
     * <p>countMatchingAlarmsBySeverity</p>
     *
     * @param criteria a {@link org.opennms.web.alarm.filter.AlarmCriteria} object.
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
     * @param criteria a {@link org.opennms.web.alarm.filter.AlarmCriteria} object.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     */
    public abstract Alarm[] getMatchingAlarms(AlarmCriteria criteria);

    /**
     * <p>acknowledgeMatchingAlarms</p>
     *
     * @param user a {@link java.lang.String} object.
     * @param timestamp a java$util$Date object.
     * @param criteria a {@link org.opennms.web.alarm.filter.AlarmCriteria} object.
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
     * @param criteria a {@link org.opennms.web.alarm.filter.AlarmCriteria} object.
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

}
