/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.statsd;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.utils.TimeKeeper;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@RunWith(Parameterized.class)
public class RelativeTimeTest {
    private TimeZone m_timeZone;
    private int m_offset;
    private int m_startYear;
    private int m_startMonth;
    private int m_startDay;
    private int m_endYear;
    private int m_endMonth;
    private int m_endDay;

    public RelativeTimeTest(final String timeZone, final int offset, final int startYear, final int startMonth, final int startDay, final int endYear, final int endMonth, final int endDay) {
        m_timeZone = TimeZone.getTimeZone(timeZone);
        m_offset = offset * 60 * 60 * 1000;
        m_startYear = startYear;
        m_startMonth = startMonth;
        m_startDay = startDay;
        m_endYear = endYear;
        m_endMonth = endMonth;
        m_endDay = endDay;
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
                { "America/Chicago",       -6, 2006, Calendar.APRIL,      3, 2006, Calendar.OCTOBER, 30 },
                { "America/New_York",      -5, 2006, Calendar.APRIL,      3, 2006, Calendar.OCTOBER, 30 },
                { "Europe/Berlin",          1, 2006, Calendar.MARCH,     27, 2006, Calendar.OCTOBER, 30 },
                { "Europe/Rome",            1, 2006, Calendar.MARCH,     27, 2006, Calendar.OCTOBER, 30 },
                { "Antarctica/South_Pole", 12, 2006, Calendar.OCTOBER,    2, 2006, Calendar.MARCH,   20 },
                { "Pacific/Auckland",      12, 2006, Calendar.OCTOBER,    2, 2006, Calendar.MARCH,   20 }
        });
    }

    @Test
    public void testYesterdayBeginningDST() {
        RelativeTime yesterday = RelativeTime.YESTERDAY;
        yesterday.setTimeKeeper(new TimeKeeper() {

            @Override
            public Date getCurrentDate() {
                Calendar cal = new GregorianCalendar(m_timeZone, Locale.ENGLISH);
                cal.set(m_startYear, m_startMonth, m_startDay, 10, 0, 0);
                return cal.getTime();
            }

            @Override
            public long getCurrentTime() {
                return getCurrentDate().getTime();
            }

            @Override
            public TimeZone getTimeZone() {
                return m_timeZone;
            }
            
        });
        
        Date start = yesterday.getStart();
        Date end = yesterday.getEnd();
        
        System.err.println("start = " + start);
        System.err.println("end = " + end);

        Calendar c = new GregorianCalendar(m_timeZone, Locale.ENGLISH);
        c.setTime(start);

        assertEquals(m_offset, c.get(Calendar.ZONE_OFFSET));
        assertEquals(m_startYear, c.get(Calendar.YEAR));
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(Calendar.SUNDAY, c.get(Calendar.DAY_OF_WEEK));
        assertEquals(m_startDay - 1, c.get(Calendar.DAY_OF_MONTH));

        c.setTime(end);

        assertEquals(m_offset, c.get(Calendar.ZONE_OFFSET));
        assertEquals(m_startYear, c.get(Calendar.YEAR));
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(Calendar.MONDAY, c.get(Calendar.DAY_OF_WEEK));
        assertEquals(m_startDay, c.get(Calendar.DAY_OF_MONTH));

        assertEquals("end date - start date", 82800000, end.getTime() - start.getTime());
    }
    
    @Test
    public void testYesterdayEndingDST() {
        RelativeTime yesterday = RelativeTime.YESTERDAY;
        yesterday.setTimeKeeper(new TimeKeeper() {

            @Override
            public Date getCurrentDate() {
                Calendar cal = new GregorianCalendar(m_timeZone, Locale.ENGLISH);
                cal.set(m_endYear, m_endMonth, m_endDay, 10, 0, 0);
                return cal.getTime();
            }

            @Override
            public long getCurrentTime() {
                return getCurrentDate().getTime();
            }

            @Override
            public TimeZone getTimeZone() {
                return m_timeZone;
            }
            
        });
        
        Date start = yesterday.getStart();
        Date end = yesterday.getEnd();

        Calendar c = new GregorianCalendar(m_timeZone, Locale.ENGLISH);
        c.setTime(start);

        assertEquals(m_offset, c.get(Calendar.ZONE_OFFSET));
        assertEquals(m_endYear, c.get(Calendar.YEAR));
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(Calendar.SUNDAY, c.get(Calendar.DAY_OF_WEEK));
        assertEquals(m_endDay - 1, c.get(Calendar.DAY_OF_MONTH));

        c.setTime(end);

        assertEquals(m_offset, c.get(Calendar.ZONE_OFFSET));
        assertEquals(m_endYear, c.get(Calendar.YEAR));
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(Calendar.MONDAY, c.get(Calendar.DAY_OF_WEEK));
        assertEquals(m_endDay, c.get(Calendar.DAY_OF_MONTH));

        assertEquals("end date - start date", 90000000, end.getTime() - start.getTime());
    }
    
}
