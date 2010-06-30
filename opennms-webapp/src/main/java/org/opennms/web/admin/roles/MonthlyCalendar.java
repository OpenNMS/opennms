//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Java 5 generics. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.web.admin.roles;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.groups.Role;

/**
 * <p>MonthlyCalendar class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class MonthlyCalendar extends AbstractWebCalendar {
    
    private GregorianCalendar m_calendar;
    private Role m_role;
    private GroupManager m_groupManager;

    /**
     * <p>Constructor for MonthlyCalendar.</p>
     *
     * @param date a {@link java.util.Date} object.
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
     * @return a {@link java.util.Date} object.
     */
    public Date getNextMonth() {
        return new GregorianCalendar(getYear(), getMonth()+1, 1).getTime();
    }

    /**
     * <p>getPreviousMonth</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getPreviousMonth() {
        return new GregorianCalendar(getYear(), getMonth()-1, 1).getTime();
    }

}
