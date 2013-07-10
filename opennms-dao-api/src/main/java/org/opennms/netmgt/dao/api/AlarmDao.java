/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

import java.util.List;

import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.alarm.AlarmSummary;

/**
 * <p>AlarmDao interface.</p>
 */
public interface AlarmDao extends OnmsDao<OnmsAlarm, Integer> {

    /**
     * <p>findByReductionKey</p>
     *
     * @param reductionKey a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAlarm} object.
     */
    OnmsAlarm findByReductionKey(String reductionKey);

    /**
     * <p>Get the list of current alarms per node with severity greater than normal,
     * reflecting the max severity, the minimum last event time and alarm count;
     * ordered by the oldest.</p>
     * 
     * @return A list of alarm summaries.
     * @param nodeIds If you want to restrict the NodeAlarmSummaries to specific nodes (optional)
     */
    List<AlarmSummary> getNodeAlarmSummaries(Integer... nodeIds);
    
}
