/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
import org.opennms.netmgt.collection.api.TimeKeeper;

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
