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
        
        final StringBuilder buf = new StringBuilder();

        long millis = millis();
        millis = appendUnit(buf, millis, 24L * 60L * 60L * 1000L, "d");
        millis = appendUnit(buf, millis, 60L * 60L * 1000L, "h");
        millis = appendUnit(buf, millis, 60L * 1000L, "m");
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
