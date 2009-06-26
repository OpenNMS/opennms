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

import java.util.Calendar;
import java.util.Date;

import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.groups.Role;

public class Week {
    
    private Calendar m_calendar;
    private Role m_role;
    private GroupManager m_groupManager;

    public Week(Calendar weekBegin) {
        m_calendar = weekBegin;
    }
    
    public Week(Date date, Role role, GroupManager groupManager) {
        m_role = role;
        m_groupManager = groupManager;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        m_calendar = calendar;
    }

    public int getWeekOfYear() { return m_calendar.get(Calendar.WEEK_OF_YEAR); }
    
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
