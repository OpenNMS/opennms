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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Day {
    
    private Calendar m_calendar;

    public Day(Calendar calendar) {
        m_calendar = calendar;
    }
    
    public Day(Date date) {
        m_calendar = Calendar.getInstance();
        m_calendar.setTime(date);
    }
    
    public Date getDate() {
        return m_calendar.getTime();
    }
    
    public int getMonth() { return m_calendar.get(Calendar.MONTH); }

    public int getDayOfMonth() { return m_calendar.get(Calendar.DAY_OF_MONTH); }
    
    public int getDayOfYear() { return m_calendar.get(Calendar.DAY_OF_YEAR); }
    
    public String getDayOfWeek() {
        return new SimpleDateFormat("EEEE").format(m_calendar.getTime());
    }
    
    public Date getTime(int hours, int minutes) {
        Calendar time = Calendar.getInstance();
        time.set(m_calendar.get(Calendar.YEAR), m_calendar.get(Calendar.MONTH), m_calendar.get(Calendar.DAY_OF_MONTH), hours, minutes);
        return time.getTime();
    }
    
    public ScheduleEntry[] getEntries() {
        ScheduleEntry[] entries = new ScheduleEntry[3];
        entries[0] = new ScheduleEntry(getTime(0, 0), getTime(9, 0), "defaultUser2");
        entries[1] = new ScheduleEntry(getTime(9, 0), getTime(15, 0), "mhuot");
        entries[2] = new ScheduleEntry(getTime(15, 0), getTime(24, 0), "defaultUser2");
        return entries;
    }

}
