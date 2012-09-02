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

package org.opennms.core.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import junit.framework.TestCase;

import org.opennms.core.utils.AbstractTimeIntervalSequence;
import org.opennms.core.utils.OwnedInterval;
import org.opennms.core.utils.Owner;
import org.opennms.core.utils.TimeInterval;

public class IntervalTestCase extends TestCase {

    DateFormat m_dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    protected Date date(String dateStr) throws Exception {
        return m_dateFormat.parse(dateStr);
    }
    
    protected TimeInterval interval(int startMonth, int startDay, int startHour, int endMonth, int endDay, int endHour) throws Exception {
        return new TimeInterval(date(startDay+"-"+startMonth+"-2005 "+startHour+":00:00"), date(endDay+"-"+endMonth+"-2005 "+endHour+":00:00"));
    }

    protected TimeInterval jun(int startDay, int startHour, int endDay, int endHour) throws Exception {
        return interval(6, startDay, startHour, 6, endDay, endHour);
    }

    protected TimeInterval jul(int startDay, int startHour, int endDay, int endHour) throws Exception {
        return interval(7, startDay, startHour, 7, endDay, endHour);
    }

    protected TimeInterval aug(int startDay, int startHour, int endDay, int endHour) throws Exception {
        return interval(8, startDay, startHour, 8, endDay, endHour);
    }

    protected TimeInterval sep(int startDay, int startHour, int endDay, int endHour) throws Exception {
        return interval(9, startDay, startHour, 9, endDay, endHour);
    }

    protected TimeInterval oct(int startDay, int startHour, int endDay, int endHour) throws Exception {
        return interval(10, startDay, startHour, 10, endDay, endHour);
    }

    protected TimeInterval nov(int startDay, int startHour, int endDay, int endHour) throws Exception {
        return interval(11, startDay, startHour, 11, endDay, endHour);
    }

    protected TimeInterval dec(int startDay, int startHour, int endDay, int endHour) throws Exception {
        return interval(12, startDay, startHour, 12, endDay, endHour);
    }

    protected TimeInterval jun(int day, int beginHour, int endHour) throws Exception {
        return jun(day, beginHour, day, endHour);
    }
    
    protected TimeInterval jul(int day, int beginHour, int endHour) throws Exception {
        return jul(day, beginHour, day, endHour);
    }

    protected TimeInterval aug(int day, int beginHour, int endHour) throws Exception {
        return aug(day, beginHour, day, endHour);
    }

    protected TimeInterval sep(int day, int beginHour, int endHour) throws Exception {
        return sep(day, beginHour, day, endHour);
    }

    protected TimeInterval oct(int day, int beginHour, int endHour) throws Exception {
        return oct(day, beginHour, day, endHour);
    }

    protected TimeInterval nov(int day, int beginHour, int endHour) throws Exception {
        return nov(day, beginHour, day, endHour);
    }

    protected TimeInterval dec(int day, int beginHour, int endHour) throws Exception {
        return dec(day, beginHour, day, endHour);
    }

    
    protected TimeInterval aug(int day) throws Exception {
        return aug(day, 0, 24);
    }

    protected <T extends TimeInterval> void assertTimeIntervalSequence(T[] intervals, AbstractTimeIntervalSequence<T> seq) {
        int count  = 0;
        for (Iterator<T> iter = seq.iterator(); iter.hasNext();) {
            T interval = iter.next();
            assertInterval(intervals[count], interval);
            count++;
        }
        assertEquals(intervals.length, count);
        
    }

    protected void assertInterval(TimeInterval expected, TimeInterval actual) {
        assertEquals(expected, actual);
    }

    protected OwnedInterval owned(Owner owner, TimeInterval interval) {
        return (owner == null ? new OwnedInterval(interval) : new OwnedInterval(owner, interval));
    }

    protected OwnedInterval owned(TimeInterval interval) {
        return owned(null, interval);
    }

    protected OwnedInterval ownedOne(TimeInterval interval) {
        return owned(new Owner("one", "one"), interval);
    }

    protected OwnedInterval ownedTwo(TimeInterval interval) {
        return owned(new Owner("two", "two"), interval);
    }

    protected OwnedInterval ownedOneAndTwo(TimeInterval interval) {
        Owner[] owners = new Owner[] { new Owner("one", "one"), new Owner("two", "two") };
        return new OwnedInterval(Arrays.asList(owners), interval);
    }

}
