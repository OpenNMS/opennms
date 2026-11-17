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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.OnCallScheduleExpander.Entry;
import org.opennms.netmgt.config.OnCallScheduleExpander.OnCallNow;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;

public class OnCallScheduleExpanderTest {
    private static final int ALL_DAY_INDEX = 1;

    private static final String[] SCHEDULES = {
        "<schedule name=\"alice\" type=\"daily\"><time begins=\"08:00:00\" ends=\"17:00:00\"/></schedule>",
        "<schedule name=\"carol\" type=\"daily\"><time begins=\"00:00:00\" ends=\"23:59:59\"/></schedule>",
        "<schedule name=\"bob\" type=\"weekly\">"
            + "<time day=\"monday\" begins=\"22:00:00\" ends=\"23:59:59\"/>"
            + "<time day=\"tuesday\" begins=\"00:00:00\" ends=\"06:00:00\"/></schedule>",
        "<schedule name=\"dave\" type=\"monthly\"><time day=\"31\" begins=\"09:00:00\" ends=\"10:00:00\"/></schedule>",
        "<schedule name=\"erin\" type=\"monthly\"><time day=\"30\" begins=\"00:00:00\" ends=\"23:59:59\"/></schedule>",
        "<schedule name=\"frank\" type=\"specific\"><time begins=\"07-Mar-2026 22:00:00\" ends=\"09-Mar-2026 03:30:00\"/></schedule>",
        "<schedule name=\"gina\" type=\"weekly\"><time begins=\"12:00:00\" ends=\"13:00:00\"/></schedule>",
        "<schedule name=\"hank\" type=\"specific\"><time begins=\"11:00:00\" ends=\"11:30:00\"/></schedule>",
        "<schedule name=\"ivy\" type=\"daily\"><time begins=\"18:00:00\" ends=\"07:00:00\"/></schedule>",
        // unreadable times: notifd compares against the instant itself, so a bad end reaches to the end of the day
        // and a bad begin starts at midnight
        "<schedule name=\"jon\" type=\"daily\"><time begins=\"23:00:00\" ends=\"14-Xyz-2026 20:00:00\"/></schedule>",
        "<schedule name=\"kim\" type=\"daily\"><time begins=\"01-Xyz-2026 00:00:00\" ends=\"00:30:00\"/></schedule>",
        // lee: a 21-character begin (German month, see role()) is not read; the valid end then covers everything
        // up to 2 March
        "<schedule name=\"lee\" type=\"specific\"><time begins=\"14-Mar-2026 18:00:00\" ends=\"02-Mar-2026 20:00:00\"/></schedule>",
    };

    private TimeZone m_zone;
    private Locale m_locale;

    @Before
    public void setUp() {
        m_zone = TimeZone.getDefault();
        m_locale = Locale.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
        Locale.setDefault(Locale.ENGLISH);
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(m_zone);
        Locale.setDefault(m_locale);
    }

    private static Role role(boolean withAllDay) {
        final Role role = new Role();
        role.setName("test");
        role.setSupervisor("sup");
        for (int i = 0; i < SCHEDULES.length; i++) {
            if (!withAllDay && i == ALL_DAY_INDEX) {
                continue;
            }
            final Schedule schedule = JaxbUtils.unmarshal(Schedule.class, SCHEDULES[i]);
            if ("lee".equals(schedule.getName())) {
                // the schema only allows 3-letter months, so a 21-character time cannot be unmarshalled
                schedule.getTimes().get(0).setBegins("14-M\u00e4rz-2026 18:00:00");
            }
            role.addSchedule(schedule);
        }
        return role;
    }

    private static Date at(int year, int month, int day, int hour, int minute, int second) {
        final Calendar cal = new GregorianCalendar(year, month - 1, day, hour, minute, second);
        return cal.getTime();
    }

    private static Set<String> expected(Role role, Date instant) {
        final Set<String> users = new TreeSet<>();
        for (Schedule sched : role.getSchedules()) {
            if (BasicScheduleUtils.isTimeInSchedule(instant, BasicScheduleUtils.getGroupSchedule(sched))) {
                users.add(sched.getName());
            }
        }
        if (users.isEmpty()) {
            users.add(role.getSupervisor());
        }
        return users;
    }

    private static void check(Role role, List<Entry> entries, Date instant, Date start, Date end) {
        if (instant.before(start) || !instant.before(end)) {
            return;
        }
        final Set<String> actual = new TreeSet<>();
        boolean fallback = false;
        boolean scheduled = false;
        for (Entry e : entries) {
            if (!instant.before(e.start()) && instant.before(e.end())) {
                actual.add(e.user());
                if (e.scheduleIndex() == null) {
                    fallback = true;
                } else {
                    scheduled = true;
                }
            }
        }
        assertEquals("users at " + instant, expected(role, instant), actual);
        assertFalse("scheduled and fallback at " + instant, fallback && scheduled);
        assertTrue("neither at " + instant, fallback || scheduled);
    }

    @Test
    public void testAgreesWithIsTimeInSchedule() {
        assertAgrees(new int[][] { { 2026, 3 }, { 2026, 4 }, { 2026, 11 } });
    }

    /**
     * DST starts at local midnight in Havana, so 00:00 does not exist on that day (8 March 2026), and ends at 01:00, so
     * 00:00 to 01:00 happens twice on that day (1 November 2026, the first day of a month).
     */
    @Test
    public void testAgreesWithIsTimeInScheduleWhenDstChangesAtMidnight() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Havana"));
        assertAgrees(new int[][] { { 2026, 3 }, { 2026, 10 }, { 2026, 11 } });
    }

    /**
     * The first 00:00 to 01:00 of 1 November 2026 in Havana is on 1 November, whose times notifd reads onto the second
     * 00:00, so even an all-day schedule does not cover it and the supervisor is on call; the November month starts
     * with it.
     */
    @Test
    public void testFirstOfTwoMidnightHoursIsInTheMonthAndFallsBackToTheSupervisor() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Havana"));
        final Role role = new Role();
        role.setName("test");
        role.setSupervisor("sup");
        role.addSchedule(JaxbUtils.unmarshal(Schedule.class, SCHEDULES[ALL_DAY_INDEX]));
        final Date start = monthStart(2026, 11);
        final Date firstHour = new Date(start.getTime() + 30 * 60 * 1000L);
        assertEquals(Set.of("sup"), expected(role, firstHour));

        final List<Entry> entries = OnCallScheduleExpander.entries(role, start, monthStart(2026, 12));
        assertEquals(new Entry(start, new Date(start.getTime() + 60 * 60 * 1000L), "sup", null), entries.get(0));
        check(role, entries, firstHour, start, monthStart(2026, 12));
        // October ends where November starts
        final List<Entry> october = OnCallScheduleExpander.entries(role, monthStart(2026, 10), start);
        assertEquals(start, october.get(october.size() - 1).end());
    }

    /** The first instant of a month (13 is January of the next year), as the calendar endpoint takes it. */
    private static Date monthStart(final int year, final int month) {
        return Date.from(YearMonth.of(year, 1).plusMonths(month - 1).atDay(1).atStartOfDay(TimeZone.getDefault().toZoneId()).toInstant());
    }

    private static void assertAgrees(final int[][] months) {
        for (boolean withAllDay : new boolean[] { true, false }) {
            final Role role = role(withAllDay);
            for (int[] ym : months) {
                final Date start = monthStart(ym[0], ym[1]);
                final Date end = monthStart(ym[0], ym[1] + 1);
                final List<Entry> entries = OnCallScheduleExpander.entries(role, start, end);

                final Set<Long> samples = new LinkedHashSet<>();
                for (long t = start.getTime(); t < end.getTime(); t += 7 * 60 * 1000L) {
                    samples.add(t);
                }
                for (Entry e : entries) {
                    samples.add(e.start().getTime() - 1000);
                    samples.add(e.start().getTime());
                    samples.add(e.end().getTime() - 1000);
                    samples.add(e.end().getTime());
                }
                for (long t : samples) {
                    check(role, entries, new Date(t), start, end);
                }
            }
        }
    }

    @Test
    public void testOnCallAt() {
        final Role role = role(false);

        OnCallNow now = OnCallScheduleExpander.onCallAt(role, at(2026, 3, 10, 5, 0, 0));
        assertEquals(List.of("bob"), now.users());
        assertFalse(now.supervisorFallback());

        now = OnCallScheduleExpander.onCallAt(role, at(2026, 4, 2, 17, 30, 0));
        assertEquals(List.of("sup"), now.users());
        assertTrue(now.supervisorFallback());

        // alice (schedule 0) and gina (schedule 5) at noon on a day nobody else covers
        now = OnCallScheduleExpander.onCallAt(role, at(2026, 4, 2, 12, 30, 0));
        assertEquals(List.of("alice", "gina"), now.users());
        assertFalse(now.supervisorFallback());
    }

    /** Without a supervisor, nobody covers the times nobody is scheduled: no fallback entries, and nobody on call. */
    @Test
    public void testARoleWithoutASupervisorHasNoFallback() {
        final Role role = role(false);
        role.setSupervisor(null);
        final Role supervised = role(false);

        final List<Entry> entries = OnCallScheduleExpander.entries(role, monthStart(2026, 4), monthStart(2026, 5));
        final List<Entry> scheduled = new ArrayList<>();
        for (final Entry e : OnCallScheduleExpander.entries(supervised, monthStart(2026, 4), monthStart(2026, 5))) {
            if (e.scheduleIndex() != null) {
                scheduled.add(e);
            }
        }
        assertFalse(scheduled.isEmpty());
        assertEquals(scheduled, entries);

        OnCallNow now = OnCallScheduleExpander.onCallAt(role, at(2026, 4, 2, 17, 30, 0));
        assertEquals(List.of(), now.users());
        assertFalse(now.supervisorFallback());

        now = OnCallScheduleExpander.onCallAt(role, at(2026, 3, 10, 5, 0, 0));
        assertEquals(List.of("bob"), now.users());
        assertFalse(now.supervisorFallback());
    }

    @Test
    public void testOnCallAtListsAUserOnceWhenTwoSchedulesCoverTheInstant() {
        final Role role = new Role();
        role.setName("test");
        role.setSupervisor("sup");
        role.addSchedule(JaxbUtils.unmarshal(Schedule.class, SCHEDULES[0]));
        role.addSchedule(JaxbUtils.unmarshal(Schedule.class,
                "<schedule name=\"alice\" type=\"daily\"><time begins=\"10:00:00\" ends=\"14:00:00\"/></schedule>"));

        final OnCallNow now = OnCallScheduleExpander.onCallAt(role, at(2026, 4, 2, 12, 30, 0));
        assertEquals(List.of("alice"), now.users());
        assertFalse(now.supervisorFallback());
    }

    @Test
    public void testEntriesSplitAtMidnightAndClipToWindow() {
        final Role role = role(true);
        final int frankIndex = 5;
        final List<Entry> frank = new ArrayList<>();
        for (Entry e : OnCallScheduleExpander.entries(role, at(2026, 3, 1, 0, 0, 0), at(2026, 4, 1, 0, 0, 0))) {
            if (Integer.valueOf(frankIndex).equals(e.scheduleIndex())) {
                frank.add(e);
            }
        }
        assertEquals(3, frank.size());
        assertEquals(at(2026, 3, 7, 22, 0, 0), frank.get(0).start());
        assertEquals(at(2026, 3, 8, 0, 0, 0), frank.get(0).end());
        assertEquals(at(2026, 3, 8, 0, 0, 0), frank.get(1).start());
        assertEquals(at(2026, 3, 9, 0, 0, 0), frank.get(1).end());
        assertEquals(23 * 3600 * 1000L, frank.get(1).end().getTime() - frank.get(1).start().getTime());
        assertEquals(at(2026, 3, 9, 0, 0, 0), frank.get(2).start());
        assertEquals(at(2026, 3, 9, 3, 30, 1), frank.get(2).end());

        for (Entry e : OnCallScheduleExpander.entries(role, at(2026, 4, 1, 0, 0, 0), at(2026, 5, 1, 0, 0, 0))) {
            assertFalse("day-31 entry in April", "dave".equals(e.user()));
        }

        // clipped to the window
        final List<Entry> clipped = OnCallScheduleExpander.entries(role, at(2026, 3, 8, 12, 0, 0), at(2026, 3, 8, 13, 0, 0));
        for (Entry e : clipped) {
            assertFalse(e.start().before(at(2026, 3, 8, 12, 0, 0)));
            assertFalse(e.end().after(at(2026, 3, 8, 13, 0, 0)));
        }
        assertFalse(clipped.isEmpty());
    }
}
