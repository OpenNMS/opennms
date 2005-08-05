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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

public class RolesTest extends TestCase {


    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testRoles() {
        
    }
    
    public void testWeekCount() throws Exception {
        Date aug3 = getDate("2005-08-03");
        MonthlyCalendar calendar = new MonthlyCalendar(aug3);
        assertEquals(5, calendar.getWeeks().length);
        
        Date july17 = getDate("2005-07-17");
        calendar = new MonthlyCalendar(july17);
        assertEquals(6, calendar.getWeeks().length);
        
        Date may27 = getDate("2005-05-27");
        calendar = new MonthlyCalendar(may27);
        assertEquals(5, calendar.getWeeks().length);
        
        Date feb14_04 = getDate("2004-02-14");
        calendar = new MonthlyCalendar(feb14_04);
        assertEquals(5, calendar.getWeeks().length);
        
        Date feb7_09 = getDate("2009-02-09");
        calendar = new MonthlyCalendar(feb7_09);
        assertEquals(4, calendar.getWeeks().length);
        
    }
    
    private Date getDate(String date) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(date);
    }

}
