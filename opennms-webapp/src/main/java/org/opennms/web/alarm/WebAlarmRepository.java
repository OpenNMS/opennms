/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.alarm;

import java.util.Date;

import org.opennms.web.alarm.filter.AlarmCriteria;

/*
 * WebAlarmRepository
 * @author brozow
 */
public interface WebAlarmRepository {

    public abstract int countMatchingAlarms(AlarmCriteria criteria);

    public abstract int[] countMatchingAlarmsBySeverity(AlarmCriteria criteria);

    public abstract Alarm getAlarm(int alarmId);

    public abstract Alarm[] getMatchingAlarms(AlarmCriteria criteria);

    public abstract void acknowledgeMatchingAlarms(String user,
            Date timestamp, AlarmCriteria criteria);
    
    public void acknowledgeAlarms(int[] alarmIds, String user, Date timestamp);

    public abstract void acknowledgeAll(String user, Date timestamp);

    public abstract void unacknowledgeMatchingAlarms(AlarmCriteria criteria, String user);

    public void unacknowledgeAlarms(int[] alarmIds, String user);

    public abstract void unacknowledgeAll(String user);
    
    public abstract void escalateAlarms(int[] alarmIds, String user, Date timestamp);
    
    public abstract void clearAlarms(int[] alamrIds, String user, Date timestamp);

}