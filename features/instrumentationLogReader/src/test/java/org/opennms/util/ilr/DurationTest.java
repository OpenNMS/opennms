/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.util.ilr;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.runner.RunWith;
import org.opennms.util.ilr.BaseLogMessage;
import org.opennms.util.ilr.Duration;


/**
 * Duration
 *
 * @author brozow
 */

@RunWith(Theories.class)
public class DurationTest {
    
    static Date timestamp(String dateString) {
        return BaseLogMessage.parseTimestamp(dateString);
    }
    
    @DataPoints
    public static Date[] dataPoints = new Date[]{
        timestamp("2010-05-26 12:12:40,000"),
        timestamp("2010-05-26 12:12:40,001"),
        // duplicate on purpuse
        timestamp("2010-05-26 12:12:40,001"),
        timestamp("2010-05-26 12:12:40,010"),
        timestamp("2010-05-26 12:12:40,100"),
        timestamp("2010-05-26 12:12:41,000"),
        timestamp("2010-05-26 12:12:50,000"),
        timestamp("2010-05-26 12:13:40,000"),
        timestamp("2010-05-26 12:22:40,000"),
        timestamp("2010-05-26 13:12:40,000"),
        timestamp("2010-05-26 22:12:40,000"),
        timestamp("2010-05-27 12:12:40,000"),
        timestamp("2010-06-26 12:12:40,000"),
        timestamp("2011-05-26 12:12:40,000"),
        timestamp("2020-05-26 12:12:40,000"),
        timestamp("2110-05-26 12:12:40,000"),
        timestamp("3010-05-26 12:12:40,000"),
    };
    
    public static class ToStringData {
        long m_duration;
        TimeUnit m_units;
        String m_expectedString;
        
        public ToStringData(long duration, TimeUnit units, String expectedString) {
            m_duration = duration;
            m_units = units;
            m_expectedString = expectedString;
        }
        
        public long duration() { return m_duration; }
        public TimeUnit units() { return m_units; }
        public String expectedString() { return m_expectedString; }
    }
    
    @DataPoints
    public static ToStringData[] toStringData = new ToStringData[]{
        new ToStringData(0, TimeUnit.MILLISECONDS, "0ms"),
        new ToStringData(1, TimeUnit.MILLISECONDS, "1ms"),
        new ToStringData(1, TimeUnit.SECONDS, "1s"),
        new ToStringData(60, TimeUnit.SECONDS, "1m"),
        new ToStringData(3600, TimeUnit.SECONDS, "1h"),
        new ToStringData(3600*24, TimeUnit.SECONDS, "1d"),
        new ToStringData(62, TimeUnit.SECONDS, "1m2s"),
        new ToStringData(62000, TimeUnit.MILLISECONDS, "1m2s"),
        new ToStringData(62003, TimeUnit.MILLISECONDS, "1m2s3ms"),
        new ToStringData(60003, TimeUnit.MILLISECONDS, "1m0s3ms"),
        new ToStringData(3600*24*3+1, TimeUnit.SECONDS, "3d0h0m1s"),
    };
    
    
    @Theory
    public void testToString(ToStringData data) {
        Duration d = new Duration(data.duration(), data.units());
        assertThat(d.toString(), is(data.expectedString()));
    }

    @Theory
    public void testStartAndEndSame(Date time) {
        Duration d = new Duration(time, time);
        
        assertThat(d.millis(), is(0L));
    }
    
    @Test
    public void testSimpleCompare() {
        Duration d1 = new Duration(100);
        Duration d2 = new Duration(200);
        
        
        assertThat(sign(d1.compareTo(d2)), is(-1));
        assertThat(sign(d2.compareTo(d1)), is(1));
        assertThat(d1.compareTo(d1), is(0));
        assertThat(d2.compareTo(d2), is(0));
    }
    
    @Theory
    public void testCompareTo(Date s1, Date e1, Date s2, Date e2) {
        assumeThat(!s1.after(e1), is(true));
        assumeThat(!s2.after(e2), is(true));
        
        Duration d1 = new Duration(s1, e1);
        Duration d2 = new Duration(s2, e2);
        
        assertThat(d1.compareTo(d2)<0, is(d1.millis()<d2.millis()));
        assertThat(d1.compareTo(d2)==0, is(d1.millis()==d2.millis()));
        assertThat(d1.compareTo(d2)>0, is(d1.millis()>d2.millis()));
        
    }
    

    
    @Theory
    public void testCreate(Date startTime, Date endTime) {
        assumeThat(startTime.before(endTime), is(true));

        Duration d = new Duration(startTime, endTime);
        assertThat(d.millis(), is(endTime.getTime() - startTime.getTime()));
        
    }
    
    @Theory
    public void testCreateWithInvalidDates(Date startTime, Date endTime) {
        assumeThat(startTime.after(endTime), is(true));
        
        try {
            new Duration(startTime, endTime);
            fail("Expected an exception");
        } catch (IllegalArgumentException e) {
            
        } catch (Throwable t) {
            fail("Unexpected Throwable "+t);
        }
        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testWithNullStartDate() {
        new Duration(null, new Date());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testWithNullEndDate() {
        new Duration(new Date(), null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testWithNullTimeUnit() {
        new Duration(1, null);
    }
    
    Matcher<Integer> isLessThan(final int val) {
        return new TypeSafeMatcher<Integer>() {

            @Override
            public boolean matchesSafely(Integer item) {
                return item < val;
            }

            @Override
            public void describeTo(Description descr) {
                descr.appendText("an integer less than ").appendValue(val);
            }
        };
    }
    Matcher<Integer> isGreaterThan(final int val) {
        return new TypeSafeMatcher<Integer>() {

            @Override
            public boolean matchesSafely(Integer item) {
                return item > val;
            }

            @Override
            public void describeTo(Description descr) {
                descr.appendText("an integer greater than ").appendValue(val);
            }
        };
        
    }
    
    int sign(long num) {
        return num < 0 ? -1 : num > 0 ? 1 : 0;
    }
    
    
    
}
