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

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.exolab.castor.xml.Unmarshaller;
import org.opennms.netmgt.config.common.Time;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.mock.MockLogAppender;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

public class CalendarEntryTest extends TestCase {

    private Schedule m_brozowOnCall;
    private Schedule m_adminOnCall;
    private Schedule m_davidOnCall;
    private Schedule m_upUserOnCall;
    private SimpleDateFormat m_dateFormat;
    private Date m_dayStart;
    private Date m_dayEnd;
    private CalendarEntrySet m_entrySet;
    private String m_supervisor;
    private Schedule m_simpleSchedule;

    protected void setUp() throws Exception {
        super.setUp();
        MockLogAppender.setupLogging();
        
        m_dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

        m_dayStart = m_dateFormat.parse("21-08-2005 00:00:00");
        m_dayEnd = m_dateFormat.parse("22-08-2005 00:00:00");
        m_brozowOnCall = (Schedule)Unmarshaller.unmarshal(Schedule.class, new StringReader(
                "      <schedule name=\"brozow\" type=\"weekly\">\n" + 
                "         <time day=\"sunday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
                "         <time day=\"monday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
                "         <time day=\"wednesday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
                "         <time day=\"friday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
                "      </schedule>\n"
                ));
        m_adminOnCall = (Schedule)Unmarshaller.unmarshal(Schedule.class, new StringReader(
                "      <schedule name=\"admin\" type=\"weekly\">\n" + 
                "         <time day=\"sunday\" begins=\"00:00:00\" ends=\"23:59:59\"/>\n" + 
                "         <time day=\"tuesday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
                "         <time day=\"thursday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
                "         <time day=\"saturday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
                "      </schedule>\n"
                ));
        m_davidOnCall = (Schedule)Unmarshaller.unmarshal(Schedule.class, new StringReader(
                "      <schedule name=\"david\" type=\"weekly\">\n" + 
                "         <time day=\"sunday\"    begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
                "         <time day=\"sunday\"    begins=\"17:00:00\" ends=\"23:59:59\"/>\n" + 
                "         <time day=\"monday\"    begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
                "         <time day=\"monday\"    begins=\"17:00:00\" ends=\"23:59:59\"/>\n" + 
                "         <time day=\"tuesday\"   begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
                "         <time day=\"tuesday\"   begins=\"17:00:00\" ends=\"23:59:59\"/>\n" + 
                "         <time day=\"wednesday\" begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
                "         <time day=\"wednesday\" begins=\"17:00:00\" ends=\"23:59:59\"/>\n" + 
                "         <time day=\"thursday\"  begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
                "         <time day=\"thursday\"  begins=\"17:00:00\" ends=\"23:59:59\"/>\n" + 
                "         <time day=\"friday\"    begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
                "         <time day=\"friday\"    begins=\"17:00:00\" ends=\"23:59:59\"/>\n" + 
                "         <time day=\"saturday\"  begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
                "         <time day=\"saturday\"  begins=\"17:00:00\" ends=\"23:59:59\"/>\n" + 
                "      </schedule>\n"
                ));
        m_upUserOnCall = (Schedule)Unmarshaller.unmarshal(Schedule.class, new StringReader(
                "           <schedule name=\"upUser\" type=\"weekly\">" +
                "               <time day=\"sunday\" begins=\"00:00:00\" ends=\"23:59:59\"/>\n" + 
                "               <time day=\"monday\" begins=\"00:00:00\" ends=\"23:59:59\"/>\n" + 
                "               <time day=\"tuesday\" begins=\"00:00:00\" ends=\"23:59:59\"/>\n" + 
                "               <time day=\"wednesday\" begins=\"00:00:00\" ends=\"23:59:59\"/>\n" + 
                "               <time day=\"thursday\" begins=\"00:00:00\" ends=\"23:59:59\"/>\n" + 
                "               <time day=\"friday\" begins=\"00:00:00\" ends=\"23:59:59\"/>\n" + 
                "               <time day=\"saturday\" begins=\"00:00:00\" ends=\"23:59:59\"/>\n" + 
                "           </schedule>"
                ));
        
        m_simpleSchedule = (Schedule)Unmarshaller.unmarshal(Schedule.class, new StringReader(
                "           <schedule name=\"simple\" type=\"specific\">" +
                "               <time begins=\"21-Aug-2005 13:00:00\" ends=\"21-Aug-2005 14:00:00\"/>\n" + 
                "           </schedule>"
        ));

        m_supervisor = "supervisor";
        m_entrySet = new CalendarEntrySet(m_dayStart, m_dayEnd, m_supervisor);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        MockLogAppender.assertNoWarningsOrGreater();
    }
    
    public void testNoSchedules() {
        Collection entries = m_entrySet.getEntries();
        assertNotNull(entries);
        // should contain a single entry for the supervisor
        assertEquals(1, entries.size());
        CalendarEntry entry = (CalendarEntry)entries.iterator().next();
        assertEquals(m_supervisor, entry.getDescription());
        assertEquals(m_dayStart, entry.getStartTime());
        assertEquals(m_dayEnd, entry.getEndTime());
    }
    
    public void testSimpleSchedule() {

        m_entrySet.addSchedule(m_simpleSchedule);
        
        Collection entries = m_entrySet.getEntries();
        assertNotNull(entries);
        
        // share have a supervisor entry, then the entry from the schedule, then supervisor again
        assertEquals(3, entries.size());
    }
    
    

}
