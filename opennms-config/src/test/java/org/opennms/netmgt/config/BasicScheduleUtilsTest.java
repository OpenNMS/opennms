/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opennms.core.test.IntervalTestCase;
import org.opennms.core.utils.OwnedInterval;
import org.opennms.core.utils.OwnedIntervalSequence;
import org.opennms.core.utils.Owner;
import org.opennms.core.utils.TimeInterval;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.groups.Time;
import org.opennms.netmgt.config.poller.outages.Outage;

public class BasicScheduleUtilsTest extends IntervalTestCase {

    @Test
    public void testSimpleScheduleExcluded() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"specific\">" +
            "               <time begins=\"20-Aug-2005 13:00:00\" ends=\"20-Aug-2005 14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = JaxbUtils.unmarshal(Schedule.class, schedSpec);
        
        Owner owner = new Owner("unnamed", "simple", 0);
        OwnedIntervalSequence intervals = BasicScheduleUtils.getIntervalsCovering(aug(18), BasicScheduleUtils.getGroupSchedule(simpleSchedule), owner);
        assertNotNull(intervals);
        assertTimeIntervalSequence(new OwnedInterval[0], intervals);

    }

    @Test
    public void testSimpleScheduleIncluded() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"specific\">" +
            "               <time begins=\"18-Aug-2005 13:00:00\" ends=\"18-Aug-2005 14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = JaxbUtils.unmarshal(Schedule.class, schedSpec);
        
        Owner owner = new Owner("unnamed", "simple", 0);
        OwnedIntervalSequence intervals = BasicScheduleUtils.getIntervalsCovering(aug(18), BasicScheduleUtils.getGroupSchedule(simpleSchedule), owner);
        assertNotNull(intervals);
        assertTimeIntervalSequence(new OwnedInterval[] { owned(owner, aug(18, 13, 14)) }, intervals);

    }
    
    @Test
    public void testDoubleScheduleIncluded() throws Exception {
        String schedSpec = 
            "           <schedule name=\"double\" type=\"specific\">" +
            "               <time begins=\"18-Aug-2005 13:00:00\" ends=\"18-Aug-2005 14:00:00\"/>\n" + 
            "               <time begins=\"18-Aug-2005 16:00:00\" ends=\"18-Aug-2005 17:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = JaxbUtils.unmarshal(Schedule.class, schedSpec);
        
        Owner owner = new Owner("unnamed", "double", 0);
        OwnedIntervalSequence intervals = BasicScheduleUtils.getIntervalsCovering(aug(18), BasicScheduleUtils.getGroupSchedule(simpleSchedule), owner);
        assertNotNull(intervals);
        assertTimeIntervalSequence(new OwnedInterval[] { owned(owner, aug(18, 13, 14)), owned(owner, aug(18, 16, 17)) }, intervals);

    }
    
    @Test
    public void testComplexScheduleIncluded() throws Exception {
        String schedSpec = 
            "           <schedule name=\"complex\" type=\"specific\">" +
            "               <time day=\"thursday\" begins=\"13:00:00\" ends=\"14:00:00\"/>\n" + 
            "               <time day=\"friday\" begins=\"07:00:00\" ends=\"08:00:00\"/>\n" + 
            "               <time day=\"18\" begins=\"19:00:00\" ends=\"20:00:00\"/>\n" + 
            "               <time day=\"19\" begins=\"09:00:00\" ends=\"10:00:00\"/>\n" + 
            "               <time begins=\"18-Aug-2005 16:00:00\" ends=\"18-Aug-2005 17:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = JaxbUtils.unmarshal(Schedule.class, schedSpec);
        
        Owner owner = new Owner("unnamed", "complex", 0);
       OwnedIntervalSequence intervals = BasicScheduleUtils.getIntervalsCovering(aug(18), BasicScheduleUtils.getGroupSchedule(simpleSchedule), owner);
        assertNotNull(intervals);

        OwnedInterval[] expected = {
                owned(owner, aug(18, 13, 14)),
                owned(owner, aug(18, 16, 17)),
                owned(owner, aug(18, 19, 20)),
        };
        
        assertTimeIntervalSequence(expected, intervals);

    }
    
    @Test
    public void testSpecificInterval() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"specific\">" +
            "               <time begins=\"18-Aug-2005 13:00:00\" ends=\"18-Aug-2005 14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = JaxbUtils.unmarshal(Schedule.class, schedSpec);
        BasicSchedule basicSchedule = BasicScheduleUtils.getGroupSchedule(simpleSchedule);

        Owner owner = new Owner("unnamed", "simple", 0, 0);
        TimeInterval interval = BasicScheduleUtils.getInterval(new Date(), basicSchedule.getTime(0), owner);
        assertNotNull(interval);
        assertInterval(owned(owner, aug(18, 13, 14)), interval);
        
    }
    
    @Test
    public void testMonthlyInterval() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"specific\">" +
            "               <time day=\"18\" begins=\"13:00:00\" ends=\"14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = JaxbUtils.unmarshal(Schedule.class, schedSpec);
        BasicSchedule basicSchedule = BasicScheduleUtils.getGroupSchedule(simpleSchedule);
        
        Owner owner = new Owner("unnamed", "simple", 0, 0);
        TimeInterval interval = BasicScheduleUtils.getInterval(aug(18).getStart(), basicSchedule.getTime(0), owner);
        assertNotNull(interval);
        assertInterval(owned(owner, aug(18, 13, 14)), interval);
        
    }

    @Test
    public void testWeeklyInterval() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"specific\">" +
            "               <time day=\"thursday\" begins=\"13:00:00\" ends=\"14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = JaxbUtils.unmarshal(Schedule.class, schedSpec);
        BasicSchedule basicSchedule = BasicScheduleUtils.getGroupSchedule(simpleSchedule);
        
        Owner owner = new Owner("unnamed", "simple", 0, 0);
        TimeInterval interval = BasicScheduleUtils.getInterval(aug(18).getStart(), basicSchedule.getTime(0), owner);
        assertNotNull(interval);
        assertInterval(owned(owner, aug(18, 13, 14)), interval);
        
    }

    @Test
    public void testGetIntervalsWeekly() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"weekly\">" +
            "               <time day=\"thursday\" begins=\"11:00:00\" ends=\"14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = JaxbUtils.unmarshal(Schedule.class, schedSpec);
        BasicSchedule basicSchedule = BasicScheduleUtils.getGroupSchedule(simpleSchedule);
        
        Owner owner = new Owner("unnamed", "simple", 0, 0);
        OwnedIntervalSequence intervals = BasicScheduleUtils.getIntervals(aug(4, 13, 25, 12), basicSchedule.getTime(0), owner);
        assertNotNull(intervals);
        
        OwnedInterval[] expected = {
                owned(owner, aug(4, 13, 14)), // start of requested interval overlaps this one
                owned(owner, aug(11, 11, 14)),
                owned(owner, aug(18, 11, 14)),
                owned(owner, aug(25, 11, 12)) // end of requested interval overlaps this one
        };
        
        assertTimeIntervalSequence(expected, intervals);
    }

    @Test
    public void testGetIntervalsMonthly() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"monthly\">" +
            "               <time day=\"7\" begins=\"11:00:00\" ends=\"14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = JaxbUtils.unmarshal(Schedule.class, schedSpec);
        BasicSchedule basicSchedule = BasicScheduleUtils.getGroupSchedule(simpleSchedule);
        
        Owner owner = new Owner("unnamed", "simple", 0, 0);
        OwnedIntervalSequence intervals = BasicScheduleUtils.getIntervals(interval(6 /* june */, 7, 13, 11 /* nov */, 7, 12), basicSchedule.getTime(0), owner);
        assertNotNull(intervals);
        
        OwnedInterval[] expected = {
                owned(owner, jun(7, 13, 14)),
                owned(owner, jul(7, 11, 14)),
                owned(owner, aug(7, 11, 14)),
                owned(owner, sep(7, 11, 14)),
                owned(owner, oct(7, 11, 14)),
                owned(owner, nov(7, 11, 12)),
        };
        
        assertTimeIntervalSequence(expected, intervals);
    }
    
    @Test
    public void testGetIntervalsDaily() throws Exception {
        String schedSpec = 
            "           <schedule name=\"simple\" type=\"daily\">" +
            "               <time begins=\"11:00:00\" ends=\"14:00:00\"/>\n" + 
            "           </schedule>";
        Schedule simpleSchedule = JaxbUtils.unmarshal(Schedule.class, schedSpec);
        assertEquals("simple", simpleSchedule.getName());
        assertEquals("daily", simpleSchedule.getType());
        assertEquals(1, simpleSchedule.getTimes().size());

        final Time simpleScheduleTime = simpleSchedule.getTimes().get(0);
        assertEquals("11:00:00", simpleScheduleTime.getBegins());
        assertEquals("14:00:00", simpleScheduleTime.getEnds());
        assertEquals(false, simpleScheduleTime.getDay().isPresent());
        assertEquals(false, simpleScheduleTime.getId().isPresent());

        BasicSchedule basicSchedule = BasicScheduleUtils.getGroupSchedule(simpleSchedule);
        
        Owner owner = new Owner("unnamed", "simple", 0, 0);
        OwnedIntervalSequence intervals = BasicScheduleUtils.getIntervals(interval(6, 1, 0, 7, 1, 0), basicSchedule.getTime(0), owner);
        assertNotNull(intervals);

        List<OwnedInterval> expected = new ArrayList<OwnedInterval>();
        for (int i = 1; i < 31; i++) {
        	expected.add(owned(owner, jun(i, 11, i, 14)));
        }
        
        assertTimeIntervalSequence(expected.toArray(new OwnedInterval[]{}), intervals);
    }
    
    @Test
    public void testNms6013IsTimeInScheduleWithDay() throws Exception {
        String schedSpec = "" +
                "<outage name=\"debtoct\" type=\"weekly\"> \n" + 
                "    <time day=\"monday\" begins=\"00:00:00\" ends=\"08:30:00\"/> \n" + 
                "    <time day=\"monday\" begins=\"16:15:00\" ends=\"23:59:59\"/> \n" + 
                "    <time day=\"tuesday\" begins=\"00:00:00\" ends=\"08:30:00\"/> \n" + 
                "    <time day=\"tuesday\" begins=\"16:15:00\" ends=\"23:59:59\"/> \n" + 
                "    <time day=\"wednesday\" begins=\"00:00:00\" ends=\"08:30:00\"/> \n" + 
                "    <time day=\"wednesday\" begins=\"16:15:00\" ends=\"23:59:59\"/> \n" + 
                "    <time day=\"thursday\" begins=\"00:00:00\" ends=\"08:30:00\"/> \n" + 
                "    <time day=\"thursday\" begins=\"16:15:00\" ends=\"23:59:59\"/> \n" + 
                "    <time day=\"friday\" begins=\"00:00:00\" ends=\"08:30:00\"/> \n" + 
                "    <time day=\"friday\" begins=\"16:15:00\" ends=\"23:59:59\"/> \n" + 
                "    <time day=\"saturday\" begins=\"00:00:00\" ends=\"23:59:59\"/> \n" + 
                "    <time day=\"sunday\" begins=\"00:00:00\" ends=\"23:59:59\"/> \n" + 
                "    <interface address=\"10.85.34.61\"/> \n" +
                "</outage> \n";
        final Outage out = JaxbUtils.unmarshal(Outage.class, schedSpec);
        final BasicSchedule schedule = BasicScheduleUtils.getBasicOutageSchedule(out);

        final Map<Calendar,Boolean> daySchedules = new HashMap<Calendar,Boolean>();
        daySchedules.put(new GregorianCalendar(2013, 7, 5, 9, 0),  false); // monday, august 5, 9:00am
        daySchedules.put(new GregorianCalendar(2013, 7, 5, 17, 0), true);  // monday, august 5, 17:00pm
        daySchedules.put(new GregorianCalendar(2013, 7, 6, 1, 0),  true);  // tuesday, august 6, 1:00am
        daySchedules.put(new GregorianCalendar(2013, 7, 6, 10, 0), false); // tuesday, august 6, 10:00am
        daySchedules.put(new GregorianCalendar(2013, 7, 7, 1, 0),  true);  // wednesday, august 7, 1:00am
        daySchedules.put(new GregorianCalendar(2013, 7, 7, 13, 0),  false); // wednesday, august 7, 1:00pm
        daySchedules.put(new GregorianCalendar(2013, 7, 7, 23, 0),  true);  // wednesday, august 7, 11:004m

        for (final Map.Entry<Calendar,Boolean> entry : daySchedules.entrySet()) {
            if (entry.getValue()) {
                assertTrue(entry.getKey().getTime() + " should be in the schedule", BasicScheduleUtils.isTimeInSchedule(entry.getKey(), schedule));
            } else {
                assertFalse(entry.getKey().getTime() + " should not be in the schedule", BasicScheduleUtils.isTimeInSchedule(entry.getKey(), schedule));
            }
        }
    }

}
 
