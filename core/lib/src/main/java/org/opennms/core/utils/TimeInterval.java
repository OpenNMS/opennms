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

import java.util.Date;

/**
 * <p>TimeInterval class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class TimeInterval implements Comparable<TimeInterval> {
    
    private Date m_start;
    private Date m_end;
    

    /**
     * <p>Constructor for TimeInterval.</p>
     *
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     */
    public TimeInterval(Date start, Date end) {
        if (start == null) throw new NullPointerException("start is null");
        if (end == null) throw new NullPointerException("end is null");
        if (start.compareTo(end) >= 0)
            throw new IllegalArgumentException("start ("+start+") must come strictly before end ("+end+")");
        
        m_start = start;
        m_end = end;
            
    }
    
    /**
     * <p>getStart</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getStart() {
        return m_start;
    }
    
    /**
     * <p>getEnd</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getEnd() {
        return m_end;
    }
    
    /**
     * Returns -1, 0, 1 based on how date compares to this interval
     *
     * @param date a {@link java.util.Date} object.
     * @return -1 if the interval is entirely before date,
     *          0 if the interval contains date,
     *           1 if the interface entirely follows date,
     *           for these the starting date is included the ending date excluded
     */
    public int comparesTo(Date date) {
        if (date.before(m_start))
            return 1;
        if (date.after(m_end) || date.equals(m_end) )
            return -1;
        else return 0;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return "["+m_start+" - "+m_end+']';
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (o instanceof TimeInterval) {
            TimeInterval t = (TimeInterval)o;
            return (m_start.equals(t.m_start) && m_end.equals(t.m_end));
        }
        return false;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    @Override
    public int hashCode() {
        return m_start.hashCode() ^ m_end.hashCode();
    }

    // I don't implement Comparable because this relation is not consistent with equals
    /**
     * <p>compareTo</p>
     *
     * @param t a {@link org.opennms.core.utils.TimeInterval} object.
     * @return a int.
     */
    @Override
    public int compareTo(TimeInterval t) {
        if (t.m_end.before(m_start) || t.m_end.equals(m_start))
            return 1;
        if (t.m_start.after(m_end) || t.m_start.equals(m_end))
            return -1;
        else return 0;
    }

    public boolean preceeds(TimeInterval interval) {
        return compareTo(interval) < 0;
    }

    public boolean follows(TimeInterval interval) {
        return compareTo(interval) > 0;
    }

    public boolean overlaps(TimeInterval interval) {
        return compareTo(interval) == 0;
    }
    
    

}
