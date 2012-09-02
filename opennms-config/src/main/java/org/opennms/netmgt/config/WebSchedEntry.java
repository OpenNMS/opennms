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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.groups.Time;

/**
 * <p>WebSchedEntry class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class WebSchedEntry {
    
    int m_schedIndex;
    int m_timeIndex;
    String m_user;
    String m_type;
    String m_day;
    String m_begins;
    String m_ends;

    /**
     * <p>Constructor for WebSchedEntry.</p>
     *
     * @param schedIndex a int.
     * @param timeIndex a int.
     * @param user a {@link java.lang.String} object.
     * @param startDate a java$util$Date object.
     * @param endDate a java$util$Date object.
     */
    public WebSchedEntry(int schedIndex, int timeIndex, String user, Date startDate, Date endDate) {
        this(schedIndex, timeIndex, user, new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(startDate), new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(endDate));
    }
    
    /**
     * <p>Constructor for WebSchedEntry.</p>
     *
     * @param schedIndex a int.
     * @param timeIndex a int.
     * @param user a {@link java.lang.String} object.
     * @param begins a {@link java.lang.String} object.
     * @param ends a {@link java.lang.String} object.
     */
    public WebSchedEntry(int schedIndex, int timeIndex, String user, String begins, String ends) {
        // this is a specific entry
        m_schedIndex = schedIndex;
        m_timeIndex = timeIndex;
        m_type = "specific";
        m_day = null;
        m_user = user;
        m_begins = begins;
        m_ends = ends;
    }
    
    /**
     * <p>isNew</p>
     *
     * @return a boolean.
     */
    public boolean isNew() {
        return m_schedIndex == -1 && m_timeIndex == -1;
    }
    
    /**
     * <p>update</p>
     *
     * @param role a {@link org.opennms.netmgt.config.groups.Role} object.
     */
    public void update(Role role) {
        if (isNew()) {
            addToRole(role);
        } else {
            modifyRole(role);
        }
    }

    private void modifyRole(Role role) {
        Schedule sched = role.getSchedule(m_schedIndex);
        Time time = sched.getTime(m_timeIndex);
        sched.setName(m_user);
        sched.setType(m_type);
        time.setDay(m_day);
        time.setBegins(m_begins);
        time.setEnds(m_ends);
    }

    private void addToRole(Role role) {
        Schedule sched = new Schedule();
        sched.setName(m_user);
        sched.setType(m_type);
        Time time = new Time();
        if (m_day != null) time.setDay(m_day);
        time.setBegins(m_begins);
        time.setEnds(m_ends);
        sched.addTime(time);
        role.addSchedule(sched);
    }

}
