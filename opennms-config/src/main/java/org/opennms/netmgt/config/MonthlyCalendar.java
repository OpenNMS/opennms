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
public class MonthlyCalendar implements WebCalendar {
    
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
