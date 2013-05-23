/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.opennms.netmgt.config.groups.Role;

/**
 * <p>MonthlyCalendar class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class MonthlyCalendar extends AbstractWebCalendar {
    
    private GregorianCalendar m_calendar;
    private Role m_role;
    private GroupManager m_groupManager;

    /**
     * <p>Constructor for MonthlyCalendar.</p>
     *
     * @param date a java$util$Date object.
     * @param role a {@link org.opennms.netmgt.config.groups.Role} object.
     * @param groupManager a {@link org.opennms.netmgt.config.GroupManager} object.
     */
    public MonthlyCalendar(Date date, Role role, GroupManager groupManager) {
        m_role = role;
        m_groupManager = groupManager;
        m_calendar = new GregorianCalendar();
        m_calendar.setTime(date);
    }
    
    /**
     * <p>getMonth</p>
     *
     * @return a int.
     */
    public int getMonth() {
        return m_calendar.get(Calendar.MONTH);
    }
    
    private int getYear() {
        return m_calendar.get(Calendar.YEAR);
    }
    
    /**
     * <p>getMonthAndYear</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getMonthAndYear() {
        return new SimpleDateFormat("MMMM yyyy").format(m_calendar.getTime());
    }
    
    private boolean isFirstOfWeek(Calendar cal) {
        return (cal.get(Calendar.DAY_OF_WEEK) == cal.getFirstDayOfWeek());
    }
    
    private Calendar getDateOfFirstWeek() {
        Calendar first = getFirstOfMonth();
        if (!isFirstOfWeek(first)) {
            first.set(Calendar.DAY_OF_WEEK, first.getFirstDayOfWeek());
            first.set(Calendar.DAY_OF_WEEK_IN_MONTH, 0);
        }
        return first;
        
    }

    private Calendar getFirstOfMonth() {
        return new GregorianCalendar(getYear(), getMonth(), 1);
    }

    /**
     * <p>getWeeks</p>
     *
     * @return an array of {@link org.opennms.web.admin.roles.Week} objects.
     */
    @Override
    public Week[] getWeeks() {
        Calendar weekBegin = getDateOfFirstWeek();
        List<Week> weeks = new ArrayList<Week>(6);
        do {
            weeks.add(new Week(weekBegin.getTime(), m_role, m_groupManager));
            weekBegin.add(Calendar.DAY_OF_YEAR, 7);
        } while (isThisMonth(weekBegin));
        
        return weeks.toArray(new Week[weeks.size()]);
    }

    private boolean isThisMonth(Calendar weekBegin) {
        return weekBegin.get(Calendar.MONTH) == getMonth();
    }

    /**
     * <p>getNextMonth</p>
     *
     * @return a java$util$Date object.
     */
    @Override
    public Date getNextMonth() {
        return new GregorianCalendar(getYear(), getMonth()+1, 1).getTime();
    }

    /**
     * <p>getPreviousMonth</p>
     *
     * @return a java$util$Date object.
     */
    @Override
    public Date getPreviousMonth() {
        return new GregorianCalendar(getYear(), getMonth()-1, 1).getTime();
    }

}
