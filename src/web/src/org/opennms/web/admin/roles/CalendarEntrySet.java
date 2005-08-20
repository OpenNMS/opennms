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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.config.BasicScheduleUtils;
import org.opennms.netmgt.config.common.Time;
import org.opennms.netmgt.config.groups.Schedule;

public class CalendarEntrySet {
    private String m_defaultLabel;
    private Date m_start;
    private Date m_end;
    private List m_entries;
    
    class DefaultCalendarEntry extends CalendarEntrySet {
        
        boolean m_default = true;

        public DefaultCalendarEntry(Date start, Date end, String defaultLabel, boolean deflt) {
            super(start, end, defaultLabel);
            m_default = deflt;
        }
        
        public DefaultCalendarEntry(Date start, Date end, String defaultLabel) {
            this(start, end, defaultLabel, false);
        }        
        
        public boolean isDefault() {
            return m_default;
        }
        
        public void setDefault(boolean deflt) {
            m_default = deflt;
        }
        
        public boolean isTimeWithin(Date time) {
            return m_start.before(time) && m_end.after(time);
        }
        
    }

    public CalendarEntrySet(Date start, Date end, String defaultLabel) {
        m_defaultLabel = defaultLabel;
        m_start = start;
        m_end = end;
        m_entries = new ArrayList();
        m_entries.add(new DefaultCalendarEntry(m_start, m_end, m_defaultLabel, true));
    }

    public Collection getEntries() {
        return m_entries;
    }

    public void addSchedule(Schedule schedule) {
        
        
    }

}
