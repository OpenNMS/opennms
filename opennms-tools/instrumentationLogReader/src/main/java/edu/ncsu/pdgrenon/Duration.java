/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package edu.ncsu.pdgrenon;

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
