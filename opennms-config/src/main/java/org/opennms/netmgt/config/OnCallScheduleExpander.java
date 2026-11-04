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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.groups.Time;

/**
 * Expands the schedules of an on-call {@link Role} into intervals, and answers who is on call at an instant, in
 * exactly the way notifd decides it ({@link BasicScheduleUtils#isTimeInSchedule(Date, org.opennms.netmgt.config.poller.outages.common.BasicSchedule)}).
 *
 * <p>notifd evaluates a time against the local calendar day of the instant being tested: the begin and end are
 * read onto that day, and a weekday or day-of-month restriction must match that day. An instant {@code t} is
 * therefore covered by a time iff {@code t} is in {@code [b(D), e(D))} where {@code D} is {@code t}'s local day.
 * This class evaluates that per local day, so every entry lies within one local day.</p>
 *
 * <p>The default time zone and locale are used, as they are by notifd.</p>
 */
public final class OnCallScheduleExpander {

    /**
     * An interval during which {@code user} is on call.
     *
     * @param start inclusive start
     * @param end exclusive end
     * @param user the user id
     * @param scheduleIndex index of the schedule in the role, or null if this is the supervisor fallback
     */
    public record Entry(Date start, Date end, String user, Integer scheduleIndex) {
    }

    /**
     * @param users the users on call, in schedule order and distinct
     * @param supervisorFallback true if nobody is scheduled and the supervisor is on call by default
     */
    public record OnCallNow(List<String> users, boolean supervisorFallback) {
    }

    private OnCallScheduleExpander() {
    }

    /**
     * @return the entries intersecting {@code [start, end)}, sorted by start then schedule index (the supervisor
     *         fallback, which has no index, sorts last)
     */
    public static List<Entry> entries(final Role role, final Date start, final Date end) {
        final List<Entry> result = new ArrayList<>();
        final long windowStart = start.getTime();
        final long windowEnd = end.getTime();
        if (windowEnd <= windowStart) {
            return result;
        }

        final Calendar day = new GregorianCalendar();
        day.setTimeInMillis(windowStart);
        startOfDay(day);
        while (day.getTimeInMillis() < windowEnd) {
            final long dayStart = day.getTimeInMillis();
            final Calendar next = (Calendar) day.clone();
            next.add(Calendar.DAY_OF_MONTH, 1);
            final long dayEnd = next.getTimeInMillis();
            final long from = Math.max(dayStart, windowStart);
            final long to = Math.min(dayEnd, windowEnd);

            final List<long[]> covered = new ArrayList<>();
            final List<Schedule> schedules = role.getSchedules();
            for (int i = 0; i < schedules.size(); i++) {
                final Schedule sched = schedules.get(i);
                final List<long[]> own = new ArrayList<>();
                for (final Time time : sched.getTimes()) {
                    if (!appliesTo(time, day)) {
                        continue;
                    }
                    final long b = Math.max(from, timeOfDay(day, time.getBegins(), false));
                    final long e = Math.min(to, timeOfDay(day, time.getEnds(), true));
                    if (b < e) {
                        own.add(new long[] { b, e });
                    }
                }
                for (final long[] iv : merge(own)) {
                    result.add(new Entry(new Date(iv[0]), new Date(iv[1]), sched.getName(), i));
                    covered.add(iv);
                }
            }

            long cursor = from;
            for (final long[] iv : merge(covered)) {
                if (iv[0] > cursor) {
                    result.add(new Entry(new Date(cursor), new Date(iv[0]), role.getSupervisor(), null));
                }
                cursor = Math.max(cursor, iv[1]);
            }
            if (cursor < to) {
                result.add(new Entry(new Date(cursor), new Date(to), role.getSupervisor(), null));
            }

            day.setTimeInMillis(dayEnd);
        }

        result.sort(Comparator.comparing(Entry::start)
                .thenComparing(Entry::scheduleIndex, Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    /**
     * @return who is on call at {@code instant}, evaluated with notifd's own per-schedule rule
     */
    public static OnCallNow onCallAt(final Role role, final Date instant) {
        final Set<String> users = new LinkedHashSet<>();
        for (final Schedule sched : role.getSchedules()) {
            if (BasicScheduleUtils.isTimeInSchedule(instant, BasicScheduleUtils.getGroupSchedule(sched))) {
                users.add(sched.getName());
            }
        }
        if (!users.isEmpty()) {
            return new OnCallNow(new ArrayList<>(users), false);
        }
        final List<String> fallback = new ArrayList<>();
        if (role.getSupervisor() != null) {
            fallback.add(role.getSupervisor());
        }
        return new OnCallNow(fallback, true);
    }

    private static void startOfDay(final Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    /** Whether the day restriction of {@code time}, if any, matches the local day {@code day}. */
    private static boolean appliesTo(final Time time, final Calendar day) {
        final String restriction = time.getDay().orElse(null);
        if (restriction == null) {
            return true;
        }
        final Integer weekday = BasicScheduleUtils.getDayOfWeekIndex(restriction);
        if (weekday != null) {
            return day.get(Calendar.DAY_OF_WEEK) == weekday;
        }
        try {
            return day.get(Calendar.DAY_OF_MONTH) == Integer.parseInt(restriction);
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    /**
     * Reads a begin or end the way notifd does on the day {@code day} (00:00), floored to the second; an end is
     * exclusive, so one second is added to it.
     */
    private static long timeOfDay(final Calendar day, final String text, final boolean isEnd) {
        final Calendar cal = (Calendar) day.clone();
        BasicScheduleUtils.setOutCalTime(cal, text);
        final long floored = cal.getTimeInMillis() / 1000 * 1000;
        return isEnd ? floored + 1000 : floored;
    }

    /** Sorts and merges touching or overlapping {start, end} pairs. */
    private static List<long[]> merge(final List<long[]> intervals) {
        final List<long[]> sorted = new ArrayList<>(intervals);
        sorted.sort(Comparator.comparingLong((long[] iv) -> iv[0]));
        final List<long[]> merged = new ArrayList<>();
        for (final long[] iv : sorted) {
            if (!merged.isEmpty() && iv[0] <= merged.get(merged.size() - 1)[1]) {
                final long[] last = merged.get(merged.size() - 1);
                last[1] = Math.max(last[1], iv[1]);
            } else {
                merged.add(new long[] { iv[0], iv[1] });
            }
        }
        return merged;
    }
}
