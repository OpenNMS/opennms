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

package org.opennms.features.poller.remote.gwt.client.utils;

import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.poller.remote.gwt.client.utils.Interval;

/**
 * <p>IntervalUtils class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class IntervalUtils {

    /**
     * <p>invert</p>
     *
     * @param beginning a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     * @param intervals a {@link java.util.Set} object.
     * @return a {@link java.util.Set} object.
     */
    public static Set<Interval> invert(final Date beginning, final Date end, final Set<Interval> intervals) {
        if (intervals.size() == 0) {
            final Set<Interval> newIntervals = getIntervalSet();
            newIntervals.add(new Interval(beginning.getTime(), end.getTime()));
            return newIntervals;
        } else if (intervals.size() == 1) {
            final Interval interval = intervals.iterator().next();
            if (interval.getStartMillis() == beginning.getTime() && interval.getEndMillis() == end.getTime()) {
                return getIntervalSet();
            }
        }
        final Set<Interval> preIntervals = getIntervalSet();
        preIntervals.addAll(intervals);
        final Set<Interval> invertedIntervals = getIntervalSet();

        Interval lastInterval = null;
        for (final Interval interval : preIntervals) {
            if (lastInterval == null) {
                if (interval.getStartMillis() > beginning.getTime()) {
                    final Interval i = new Interval(beginning.getTime(), interval.getStartMillis());
//                    System.err.println("adding " + i);
                    invertedIntervals.add(i);
                }
                lastInterval = new Interval(interval.getEndMillis(), interval.getEndMillis());
            } else if (interval.getStartMillis() > lastInterval.getEndMillis()) {
                final Interval i = new Interval(lastInterval.getStartMillis(), interval.getStartMillis());
//                System.err.println("adding " + i);
                invertedIntervals.add(i);
                lastInterval = new Interval(interval.getEndMillis(), interval.getEndMillis());
            }
        }
        if (lastInterval != null && lastInterval.getEndMillis() < end.getTime()) {
            final Interval i = new Interval(lastInterval.getEndMillis(), end.getTime());
//            System.err.println("adding " + i);
            invertedIntervals.add(i);
        } else if (lastInterval == null) {
            invertedIntervals.add(new Interval(beginning.getTime(), end.getTime()));
        }
        return invertedIntervals;
    }

    /**
     * <p>normalize</p>
     *
     * @param intervals a {@link java.util.Set} object.
     * @return a {@link java.util.Set} object.
     */
    public static Set<Interval> normalize(final Set<Interval> intervals) {
        Interval lastInterval = null;

        final Set<Interval> preIntervals = getIntervalSet();
        preIntervals.addAll(intervals);
        final Set<Interval> combinedIntervals = getIntervalSet();
        for (final Interval interval : preIntervals) {
            if (lastInterval == null) {
                lastInterval = interval;
            } else {
                if (interval.overlaps(lastInterval)) {
                    lastInterval = new Interval(
                        Math.min(lastInterval.getStartMillis(), interval.getStartMillis()),
                        Math.max(lastInterval.getEndMillis(), interval.getEndMillis())
                    );
                } else {
                    combinedIntervals.add(lastInterval);
                    lastInterval = interval;
                }
            }
        }
        if (lastInterval != null) {
            combinedIntervals.add(lastInterval);
        }
        return combinedIntervals;
    }

    /**
     * <p>getIntervalSet</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public static Set<Interval> getIntervalSet() {
        return new TreeSet<Interval>(new Comparator<Interval>() {
            @Override
            public int compare(Interval o1, Interval o2) {
                return new CompareToBuilder()
                    .append(o1.getStartMillis(), o2.getStartMillis())
                    .append(o1.getEndMillis(), o2.getEndMillis())
                    .toComparison();
            }
        });
    }

}
