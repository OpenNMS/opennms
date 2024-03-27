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
package org.opennms.core.test;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.opennms.core.utils.AbstractTimeIntervalSequence;
import org.opennms.core.utils.OwnedInterval;
import org.opennms.core.utils.Owner;
import org.opennms.core.utils.TimeInterval;

public class IntervalTestCase {

    DateFormat m_dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }

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
