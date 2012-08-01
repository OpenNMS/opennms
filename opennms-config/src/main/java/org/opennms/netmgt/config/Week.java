/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.util.Calendar;
import java.util.Date;

import org.opennms.netmgt.config.groups.Role;

/**
 * <p>Week class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class Week {
    
    private Calendar m_calendar;
    private Role m_role;
    private GroupManager m_groupManager;

    /**
     * <p>Constructor for Week.</p>
     *
     * @param weekBegin a {@link java.util.Calendar} object.
     */
    public Week(Calendar weekBegin) {
        m_calendar = weekBegin;
    }
    
    /**
     * <p>Constructor for Week.</p>
     *
     * @param date a {@link java.util.Date} object.
     * @param role a {@link org.opennms.netmgt.config.groups.Role} object.
     * @param groupManager a {@link org.opennms.netmgt.config.GroupManager} object.
     */
    public Week(Date date, Role role, GroupManager groupManager) {
        m_role = role;
        m_groupManager = groupManager;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        m_calendar = calendar;
    }

    /**
     * <p>getWeekOfYear</p>
     *
     * @return a int.
     */
    public int getWeekOfYear() { return m_calendar.get(Calendar.WEEK_OF_YEAR); }
    
    /**
     * <p>getDays</p>
     *
     * @return an array of {@link org.opennms.netmgt.config.Day} objects.
     */
    public Day[] getDays() {
        Calendar day = (Calendar)m_calendar.clone();
        Day days[] = new Day[7];
        for(int i = 0; i < 7; i++) {
            days[i] = new Day(day.getTime(), m_role, m_groupManager);
            day.add(Calendar.DAY_OF_YEAR, 1);
        }
        return days;
    }

}
