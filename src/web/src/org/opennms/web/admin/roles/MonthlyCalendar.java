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
import java.util.GregorianCalendar;
import java.util.List;

public class MonthlyCalendar extends AbstractWebCalendar {
    
    private GregorianCalendar m_calendar;

    public MonthlyCalendar() {
        this(new GregorianCalendar());
    }

    public MonthlyCalendar(Date date) {
        m_calendar = new GregorianCalendar();
        m_calendar.setTime(date);
    }
    
    public MonthlyCalendar(GregorianCalendar calendar) {
        m_calendar = calendar;
    }
    
    public int getMonth() {
        return m_calendar.get(Calendar.MONTH);
    }
    
    private int getYear() {
        return m_calendar.get(Calendar.YEAR);
    }
    
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

    public Week[] getWeeks() {
        Calendar weekBegin = getDateOfFirstWeek();
        List weeks = new ArrayList(6);
        do {
            weeks.add(new Week(weekBegin.getTime()));
            weekBegin.add(Calendar.DAY_OF_YEAR, 7);
        } while (isThisMonth(weekBegin));
        
        return (Week[]) weeks.toArray(new Week[weeks.size()]);
    }

    private boolean isThisMonth(Calendar weekBegin) {
        return weekBegin.get(Calendar.MONTH) == getMonth();
    }

    public Date getNextMonth() {
        return new GregorianCalendar(getYear(), getMonth()+1, 1).getTime();
    }

    public Date getPreviousMonth() {
        return new GregorianCalendar(getYear(), getMonth()-1, 1).getTime();
    }

}
