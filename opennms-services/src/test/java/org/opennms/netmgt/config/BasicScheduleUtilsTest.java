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
package org.opennms.netmgt.config;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.test.mock.MockLogAppender;

public class BasicScheduleUtilsTest extends IntervalTestCase {
    
    protected void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }


    protected void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    public void testSimpleScheduleExcluded() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"specific\">" +
            "               <time begins=\"20-Aug-2005 13:00:00\" ends=\"20-Aug-2005 14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = CastorUtils.unmarshal(Schedule.class, new ByteArrayInputStream(schedSpec.getBytes()), CastorUtils.PRESERVE_WHITESPACE);
        
        Owner owner = new Owner("unnamed", "simple", 0);
        OwnedIntervalSequence intervals = BasicScheduleUtils.getIntervalsCovering(aug(18), simpleSchedule, owner);
        assertNotNull(intervals);
        assertTimeIntervalSequence(new OwnedInterval[0], intervals);

    }

    public void testSimpleScheduleIncluded() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"specific\">" +
            "               <time begins=\"18-Aug-2005 13:00:00\" ends=\"18-Aug-2005 14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = CastorUtils.unmarshal(Schedule.class, new ByteArrayInputStream(schedSpec.getBytes()), CastorUtils.PRESERVE_WHITESPACE);
        
        Owner owner = new Owner("unnamed", "simple", 0);
        OwnedIntervalSequence intervals = BasicScheduleUtils.getIntervalsCovering(aug(18), simpleSchedule, owner);
        assertNotNull(intervals);
        assertTimeIntervalSequence(new OwnedInterval[] { owned(owner, aug(18, 13, 14)) }, intervals);

    }
    
    public void testDoubleScheduleIncluded() throws Exception {
        String schedSpec = 
            "           <schedule name=\"double\" type=\"specific\">" +
            "               <time begins=\"18-Aug-2005 13:00:00\" ends=\"18-Aug-2005 14:00:00\"/>\n" + 
            "               <time begins=\"18-Aug-2005 16:00:00\" ends=\"18-Aug-2005 17:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = CastorUtils.unmarshal(Schedule.class, new ByteArrayInputStream(schedSpec.getBytes()), CastorUtils.PRESERVE_WHITESPACE);
        
        Owner owner = new Owner("unnamed", "double", 0);
        TimeIntervalSequence intervals = BasicScheduleUtils.getIntervalsCovering(aug(18), simpleSchedule, owner);
        assertNotNull(intervals);
        assertTimeIntervalSequence(new TimeInterval[] { owned(owner, aug(18, 13, 14)), owned(owner, aug(18, 16, 17)) }, intervals);

    }
    
    public void testComplexScheduleIncluded() throws Exception {
        String schedSpec = 
            "           <schedule name=\"complex\" type=\"specific\">" +
            "               <time day=\"thursday\" begins=\"13:00:00\" ends=\"14:00:00\"/>\n" + 
            "               <time day=\"friday\" begins=\"07:00:00\" ends=\"08:00:00\"/>\n" + 
            "               <time day=\"18\" begins=\"19:00:00\" ends=\"20:00:00\"/>\n" + 
            "               <time day=\"19\" begins=\"09:00:00\" ends=\"10:00:00\"/>\n" + 
            "               <time begins=\"18-Aug-2005 16:00:00\" ends=\"18-Aug-2005 17:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = CastorUtils.unmarshal(Schedule.class, new ByteArrayInputStream(schedSpec.getBytes()), CastorUtils.PRESERVE_WHITESPACE);
        
        Owner owner = new Owner("unnamed", "complex", 0);
       TimeIntervalSequence intervals = BasicScheduleUtils.getIntervalsCovering(aug(18), simpleSchedule, owner);
        assertNotNull(intervals);

        TimeInterval[] expected = {
                owned(owner, aug(18, 13, 14)),
                owned(owner, aug(18, 16, 17)),
                owned(owner, aug(18, 19, 20)),
        };
        
        assertTimeIntervalSequence(expected, intervals);

    }
    
    public void testSpecificInterval() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"specific\">" +
            "               <time begins=\"18-Aug-2005 13:00:00\" ends=\"18-Aug-2005 14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = CastorUtils.unmarshal(Schedule.class, new ByteArrayInputStream(schedSpec.getBytes()), CastorUtils.PRESERVE_WHITESPACE);
        
        Owner owner = new Owner("unnamed", "simple", 0, 0);
        TimeInterval interval = BasicScheduleUtils.getInterval(new Date(), simpleSchedule.getTime(0), owner);
        assertNotNull(interval);
        assertInterval(owned(owner, aug(18, 13, 14)), interval);
        
    }
    
    public void testMonthlyInterval() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"specific\">" +
            "               <time day=\"18\" begins=\"13:00:00\" ends=\"14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = CastorUtils.unmarshal(Schedule.class, new ByteArrayInputStream(schedSpec.getBytes()), CastorUtils.PRESERVE_WHITESPACE);
        
        Owner owner = new Owner("unnamed", "simple", 0, 0);
        TimeInterval interval = BasicScheduleUtils.getInterval(aug(18).getStart(), simpleSchedule.getTime(0), owner);
        assertNotNull(interval);
        assertInterval(owned(owner, aug(18, 13, 14)), interval);
        
    }

    public void testWeeklyInterval() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"specific\">" +
            "               <time day=\"thursday\" begins=\"13:00:00\" ends=\"14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = CastorUtils.unmarshal(Schedule.class, new ByteArrayInputStream(schedSpec.getBytes()), CastorUtils.PRESERVE_WHITESPACE);
        
        Owner owner = new Owner("unnamed", "simple", 0, 0);
        TimeInterval interval = BasicScheduleUtils.getInterval(aug(18).getStart(), simpleSchedule.getTime(0), owner);
        assertNotNull(interval);
        assertInterval(owned(owner, aug(18, 13, 14)), interval);
        
    }

    public void testGetIntervalsWeekly() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"weekly\">" +
            "               <time day=\"thursday\" begins=\"11:00:00\" ends=\"14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = CastorUtils.unmarshal(Schedule.class, new ByteArrayInputStream(schedSpec.getBytes()), CastorUtils.PRESERVE_WHITESPACE);
        
        Owner owner = new Owner("unnamed", "simple", 0, 0);
        TimeIntervalSequence intervals = BasicScheduleUtils.getIntervals(aug(4, 13, 25, 12), simpleSchedule.getTime(0), owner);
        assertNotNull(intervals);
        
        TimeInterval[] expected = {
                owned(owner, aug(4, 13, 14)), // start of requested interval overlaps this one
                owned(owner, aug(11, 11, 14)),
                owned(owner, aug(18, 11, 14)),
                owned(owner, aug(25, 11, 12)) // end of requested interval overlaps this one
        };
        
        assertTimeIntervalSequence(expected, intervals);
    }

    public void testGetIntervalsMonthly() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"monthly\">" +
            "               <time day=\"7\" begins=\"11:00:00\" ends=\"14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = CastorUtils.unmarshal(Schedule.class, new ByteArrayInputStream(schedSpec.getBytes()), CastorUtils.PRESERVE_WHITESPACE);
        
        Owner owner = new Owner("unnamed", "simple", 0, 0);
        TimeIntervalSequence intervals = BasicScheduleUtils.getIntervals(interval(6 /* june */, 7, 13, 11 /* nov */, 7, 12), simpleSchedule.getTime(0), owner);
        assertNotNull(intervals);
        
        TimeInterval[] expected = {
                owned(owner, jun(7, 13, 14)),
                owned(owner, jul(7, 11, 14)),
                owned(owner, aug(7, 11, 14)),
                owned(owner, sep(7, 11, 14)),
                owned(owner, oct(7, 11, 14)),
                owned(owner, nov(7, 11, 12)),
        };
        
        assertTimeIntervalSequence(expected, intervals);
    }
    
    public void testGetIntervalsDaily() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"daily\">" +
            "               <time begins=\"11:00:00\" ends=\"14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = CastorUtils.unmarshal(Schedule.class, new ByteArrayInputStream(schedSpec.getBytes()), CastorUtils.PRESERVE_WHITESPACE);
        
        Owner owner = new Owner("unnamed", "simple", 0, 0);
        TimeIntervalSequence intervals = BasicScheduleUtils.getIntervals(interval(6, 1, 0, 7, 1, 0), simpleSchedule.getTime(0), owner);
        assertNotNull(intervals);

        List<TimeInterval> expected = new ArrayList<TimeInterval>();
        for (int i = 1; i < 31; i++) {
        	expected.add(owned(owner, jun(i, 11, i, 14)));
        }
        
        assertTimeIntervalSequence(expected.toArray(new TimeInterval[]{}), intervals);
    }

}
 
