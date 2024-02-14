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
