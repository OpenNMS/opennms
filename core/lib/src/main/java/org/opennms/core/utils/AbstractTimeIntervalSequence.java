/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


// TODO make this implement Collection
/**
 * <p>TimeIntervalSequence class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractTimeIntervalSequence<T extends TimeInterval> {
    
    private static class TimeIntervalSeqIter<T extends TimeInterval> implements Iterator<T> {

        private AbstractTimeIntervalSequence<T> m_current;
        
        public TimeIntervalSeqIter(AbstractTimeIntervalSequence<T> seq) {
            m_current = seq;
        }

        @Override
        public boolean hasNext() {
            return m_current != null && m_current.m_interval != null;
        }

        @Override
        public T next() {
            T interval = m_current.m_interval;
            m_current = m_current.m_tail;
            return interval;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("not implemented yet");
        }

    }

    private T m_interval;
    private AbstractTimeIntervalSequence<T> m_tail;
    
    /**
     * <p>Constructor for TimeIntervalSequence.</p>
     */
    public AbstractTimeIntervalSequence() {
        this(null, null);
    }

    /**
     * <p>Constructor for TimeIntervalSequence.</p>
     *
     * @param interval a {@link org.opennms.core.utils.TimeInterval} object.
     */
    public AbstractTimeIntervalSequence(T interval) {
        this(interval, null);
    }
    
    private AbstractTimeIntervalSequence(T interval, AbstractTimeIntervalSequence<T> tail) {
        m_interval = interval;
        m_tail = tail;
    }
    
    /**
     * <p>iterator</p>
     *
     * @return a {@link java.util.Iterator} object.
     */
    public Iterator<T> iterator() {
        return new TimeIntervalSeqIter<T>(this);
    }
    
    Date min(Date a, Date b) {
       return (a.before(b) ? a : b); 
    }
    
    Date max(Date a, Date b) {
        return (b.before(a) ? a: b);
    }

    /**
     * <p>addInterval</p>
     *
     * @param interval a {@link org.opennms.core.utils.TimeInterval} object.
     */
    public void addInterval(T interval) {
        if (m_interval == null) {
            m_interval = interval;
        } else if (m_interval.preceeds(interval)) {
            addPreceedingInterval(interval);
        } else if (m_interval.follows(interval)) {
            addSucceedingInterval(interval);
        } else if (m_interval.overlaps(interval)) {
            addOverlappingInterval(interval);
        }
        
    }

    private void addOverlappingInterval(T newInterval) {
        // overlapping intervals
        Collection<T> newIntervals = combineIntervals(m_interval, newInterval);
        
        // remove the current interval since we are replacing it with the new ones
        removeCurrent();
        
        // now add the new intervals
        addAll(newIntervals);
        
        
    }

    /**
     * <p>combineIntervals</p>
     *
     * @param currentInterval a {@link org.opennms.core.utils.TimeInterval} object.
     * @param newInterval a {@link org.opennms.core.utils.TimeInterval} object.
     * @return a {@link java.util.Collection} object.
     */
    protected Collection<T> combineIntervals(T currentInterval, T newInterval) {
        List<T> newIntervals = new ArrayList<T>(3);
        
        // overlapping intervals get divided into three non-overlapping segments
        // that are bounded by the below
        Date first = min(currentInterval.getStart(), newInterval.getStart());
        Date second = max(currentInterval.getStart(), newInterval.getStart());
        Date third = min(currentInterval.getEnd(), newInterval.getEnd());
        Date fourth = max(currentInterval.getEnd(), newInterval.getEnd());
        
        // Construct up to three non-overlapping intervals that can be added to the list
        if (first.equals(second)) { 
            // if the first segment is empty then the second segment because head of the list
            // the second segment is not empty because intervals can't be empty
            newIntervals.add(createInterval(first, third));
        } else {
            // first segment is not empty make it head of the list and add the 
            // second the the tail
            newIntervals.add(createInterval(first, second));
            newIntervals.add(createInterval(second, third));
        }
        
        
        if (!third.equals(fourth)) {
            // if the third segment no empty add it to the tail as well.
            // Note: this segment may overlap with the original lists next interval
            newIntervals.add(createInterval(third, fourth));
        }
        return newIntervals;
    }

    private void addSucceedingInterval(T interval) {
        // new interval is earlier than current interval
        // replace current with new and add current to the tail
        AbstractTimeIntervalSequence<T> oldTail = m_tail;
        m_tail = createTail(m_interval);
        m_tail.m_tail = oldTail;
        m_interval = interval;
    }

    private void addPreceedingInterval(T interval) {
        // new interval is later than current interval so add it to the tail
        addToTail(interval);
    }

    private void addToTail(T interval) {
        if (m_tail == null) {
            m_tail = createTail(interval);
        } else {
            m_tail.addInterval(interval);
        }
    }
    
    /**
     * <p>createInterval</p>
     *
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     * @return a {@link org.opennms.core.utils.TimeInterval} object.
     */
    protected abstract T createInterval(Date start, Date end);
    
    /**
     * <p>createTail</p>
     *
     * @param interval a {@link org.opennms.core.utils.TimeInterval} object.
     * @return a {@link org.opennms.core.utils.AbstractTimeIntervalSequence} object.
     */
    protected abstract AbstractTimeIntervalSequence<T> createTail(T interval);
    
    private void removeCurrent() {
        if (m_tail == null) {
            m_interval = null;
        } else {
            m_interval = m_tail.m_interval;
            m_tail = m_tail.m_tail;
        }
    }

    /**
     * <p>removeInterval</p>
     *
     * @param removedInterval a {@link org.opennms.core.utils.TimeInterval} object.
     */
    public void removeInterval(T removedInterval) {
        if (m_interval == null) {
            return;
        }
        
        if (m_interval.preceeds(removedInterval)) {
            removeFromTail(removedInterval);
        } else if (m_interval.follows(removedInterval)) {
            // no need to do anything because the entire remove interval is before this
            return; 
        } else if (m_interval.overlaps(removedInterval)) {
            
            T origInterval = m_interval;

            // remove the region from the tail of sequence
            removeFromTail(removedInterval);
            
            // remove the current element
            removeCurrent();
            
            
            // add back any part of the original interval that follows the remove interval
            Collection<T> newIntervals = separateIntervals(origInterval, removedInterval);
            
            addAll(newIntervals);
        }
    }

    /**
     * <p>separateIntervals</p>
     *
     * @param origInterval a {@link org.opennms.core.utils.TimeInterval} object.
     * @param removedInterval a {@link org.opennms.core.utils.TimeInterval} object.
     * @return a {@link java.util.Collection} object.
     */
    protected Collection<T> separateIntervals(T origInterval, T removedInterval) {
        List<T> newIntervals = new ArrayList<T>(2);
        if (removedInterval.getEnd().before(origInterval.getEnd())) {
            newIntervals.add(createInterval(removedInterval.getEnd(), origInterval.getEnd()));
        }
        
        // add back any part of the original interval the preceeded the remove interval
        if (origInterval.getStart().before(removedInterval.getStart())) {
            newIntervals.add(createInterval(origInterval.getStart(), removedInterval.getStart()));
        }
        return newIntervals;
    }

    private void removeFromTail(T interval) {
        if (m_tail == null) {
            return;
        }
        
        m_tail.removeInterval(interval);
        if (m_tail.m_interval == null) {
            m_tail = null;
        }
    }
    
    /**
     * <p>bound</p>
     *
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     */
    public void bound(Date start, Date end) {
        removeInterval(createInterval(new Date(0), start));
        removeInterval(createInterval(end, new Date(Long.MAX_VALUE)));
    }

    /**
     * <p>bound</p>
     *
     * @param interval a {@link org.opennms.core.utils.TimeInterval} object.
     */
    public void bound(T interval) {
        bound(interval.getStart(), interval.getEnd());
    }
    
    /**
     * <p>getStart</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getStart() {
        if (m_interval == null) return null;
        return m_interval.getStart();
    }
    
    /**
     * <p>getEnd</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getEnd() {
        if (m_interval == null) return null;
        if (m_tail == null) return m_interval.getEnd();
        return m_tail.getEnd();
    }
    
    /**
     * <p>getBounds</p>
     *
     * @return a {@link org.opennms.core.utils.TimeInterval} object.
     */
    public TimeInterval getBounds() {
        Date start = getStart();
        Date end = getEnd();
        return (start == null || end == null ? null : new TimeInterval(start, end));
    }

    /**
     * <p>addAll</p>
     *
     * @param intervals a {@link org.opennms.core.utils.AbstractTimeIntervalSequence} object.
     */
    public void addAll(AbstractTimeIntervalSequence<T> intervals) {
        for (Iterator<T> it = intervals.iterator(); it.hasNext();) {
            T interval = it.next();
            addInterval(interval);
        }
    }
    
    /**
     * <p>addAll</p>
     *
     * @param intervals a {@link java.util.Collection} object.
     */
    public void addAll(Collection<T> intervals) {
        for (Iterator<T> it = intervals.iterator(); it.hasNext();) {
            T interval = it.next();
            addInterval(interval);
        }
    }
    
    /**
     * <p>removeAll</p>
     *
     * @param intervals a {@link org.opennms.core.utils.AbstractTimeIntervalSequence} object.
     */
    public void removeAll(AbstractTimeIntervalSequence<T> intervals) {
        for (Iterator<T> it = intervals.iterator(); it.hasNext();) {
            T interval = it.next();
            removeInterval(interval);
        }
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("[");
        boolean first = true;
        for (Iterator<? extends TimeInterval> it = this.iterator(); it.hasNext();) {
            TimeInterval interval = (TimeInterval) it.next();
            if (first) {
                first = false;
            } else {
                buf.append(",");
            }
            
            buf.append(interval);
        }
        buf.append(']');
        return buf.toString();
    }

}
