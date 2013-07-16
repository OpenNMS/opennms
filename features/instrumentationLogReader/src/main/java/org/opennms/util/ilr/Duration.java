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

package org.opennms.util.ilr;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Duration
 *
 * @author brozow
 */
public class Duration implements Comparable<Duration>{
    
    private long m_durationInMillis;
    
    public Duration(Date start, Date end) {
        if (start == null) throw new IllegalArgumentException("startDate may not be null");
        if (end == null) throw new IllegalArgumentException("endDate may not be null");
        if (start.after(end)) {
            throw new IllegalArgumentException("start must preceed end");
        }
        m_durationInMillis = end.getTime() - start.getTime();
    }

    public Duration(long duration, TimeUnit units) {
        if (units == null) throw new IllegalArgumentException("timeUnit may not be null");
        m_durationInMillis = TimeUnit.MILLISECONDS.convert(duration, units);
    }
    
    public Duration(long durationInMillis) {
        m_durationInMillis = durationInMillis;
    }

    public long millis() {
        return m_durationInMillis;
    }
    
    private long appendUnit(StringBuilder buf, long millisRemaining, long millisPerUnit, String unit) {
        long units = millisRemaining / millisPerUnit;
        if (0 < millisRemaining && (0 < units || millisRemaining < millis())) {
            buf.append(units).append(unit);
            millisRemaining -= units * millisPerUnit;
        }
        return millisRemaining;
        
    }
    
    @Override
    public String toString() {
        if (0 == millis()) return "0ms";
        
        StringBuilder buf = new StringBuilder();

        long millis = millis();
        millis = appendUnit(buf, millis, 24*60*60*1000, "d");
        millis = appendUnit(buf, millis, 60*60*1000, "h");
        millis = appendUnit(buf, millis, 60*1000, "m");
        millis = appendUnit(buf, millis, 1000, "s");
        millis = appendUnit(buf, millis, 1, "ms");

        return buf.toString();
    }

    @Override
    public int compareTo(Duration o) {
        long diff = millis()-o.millis();
        return diff < 0 ? -1 : diff > 0 ? 1 : 0;
    }

    @Override
    public int hashCode() {
        return (int) (m_durationInMillis ^ (m_durationInMillis >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Duration) {
            Duration d = (Duration)obj;
            return m_durationInMillis == d.m_durationInMillis;
        }
        return false;
    }
    
    
    
    

}
