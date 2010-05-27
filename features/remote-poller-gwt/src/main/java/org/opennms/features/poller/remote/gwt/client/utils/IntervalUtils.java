package org.opennms.features.poller.remote.gwt.client.utils;

import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.poller.remote.gwt.client.utils.Interval;

public class IntervalUtils {

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

    public static Set<Interval> getIntervalSet() {
        return new TreeSet<Interval>(new Comparator<Interval>() {
            public int compare(Interval o1, Interval o2) {
                return new CompareToBuilder()
                    .append(o1.getStartMillis(), o2.getStartMillis())
                    .append(o1.getEndMillis(), o2.getEndMillis())
                    .toComparison();
            }
        });
    }

}
