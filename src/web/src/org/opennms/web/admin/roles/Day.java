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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.OwnedInterval;
import org.opennms.netmgt.config.OwnedIntervalSequence;
import org.opennms.netmgt.config.Owner;
import org.opennms.netmgt.config.groups.Role;

public class Day {
    
    private Calendar m_calendar;
    private Role m_role;
    private GroupManager m_groupManager;

    public Day(Date date, Role role, GroupManager groupManager) {
        m_role = role;
        m_groupManager = groupManager;
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
    
    public CalendarEntry[] getEntries() {
        List entries = new ArrayList();
        
        OwnedIntervalSequence schedEntries = m_groupManager.getRoleScheduleEntries(m_role.getName(), getTime(0,0), getTime(24,0));
        
        for (Iterator it = schedEntries.iterator(); it.hasNext();) {
            OwnedInterval schedEntry = (OwnedInterval) it.next();
            CalendarEntry entry = new CalendarEntry(schedEntry.getStart(), schedEntry.getEnd(), ownerString(schedEntry.getOwners()));
            entries.add(entry);
        }
        
        return (CalendarEntry[]) entries.toArray(new CalendarEntry[entries.size()]);
    }

    private String ownerString(List owners) {
        boolean first = true;
        StringBuffer buf = new StringBuffer();
        for (Iterator it = owners.iterator(); it.hasNext();) {
            Owner owner = (Owner) it.next();
            if (first)
                first = false;
            else 
                buf.append(", ");
            buf.append(owner.getUser());
        }
        return buf.toString();
    }

}
