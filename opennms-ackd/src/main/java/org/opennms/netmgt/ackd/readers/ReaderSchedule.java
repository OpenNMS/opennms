/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

/**
 * 
 */
package org.opennms.netmgt.ackd.readers;

import java.util.concurrent.TimeUnit;

/**
 * <p>ReaderSchedule class.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class ReaderSchedule {
    
    private long m_initialDelay;
    private long m_interval;
    private long m_attemptsRemaining;
    private TimeUnit m_unit;
    
    /**
     * <p>createSchedule</p>
     *
     * @return a {@link org.opennms.netmgt.ackd.readers.ReaderSchedule} object.
     */
    public static ReaderSchedule createSchedule() {
        return new ReaderSchedule();
    }

    /**
     * <p>createSchedule</p>
     *
     * @param initDelay a long.
     * @param interval a long.
     * @param attempts a int.
     * @param unit a {@link java.util.concurrent.TimeUnit} object.
     * @return a {@link org.opennms.netmgt.ackd.readers.ReaderSchedule} object.
     */
    public static ReaderSchedule createSchedule(long initDelay, long interval, int attempts, TimeUnit unit) {
        return new ReaderSchedule(initDelay, interval, attempts, unit);
    }

    private ReaderSchedule() {
        this(60, 60, 1, TimeUnit.SECONDS);
    }
    
    private ReaderSchedule(long initDelay, long interval, int attempts, TimeUnit unit) {
        m_initialDelay = initDelay;
        m_interval = interval;
        m_attemptsRemaining = attempts;
        m_unit = unit;
    }

    /**
     * <p>getInitialDelay</p>
     *
     * @return a long.
     */
    public long getInitialDelay() {
        return m_initialDelay;
    }

    /**
     * <p>setInitialDelay</p>
     *
     * @param initialDelay a long.
     */
    public void setInitialDelay(long initialDelay) {
        m_initialDelay = initialDelay;
    }

    /**
     * <p>getInterval</p>
     *
     * @return a long.
     */
    public long getInterval() {
        return m_interval;
    }

    /**
     * <p>setInterval</p>
     *
     * @param interval a long.
     */
    public void setInterval(long interval) {
        m_interval = interval;
    }

    /**
     * <p>getAttemptsRemaining</p>
     *
     * @return a long.
     */
    public long getAttemptsRemaining() {
        return m_attemptsRemaining;
    }

    /**
     * <p>setAttemptsRemaining</p>
     *
     * @param attemptsRemaining a long.
     */
    public void setAttemptsRemaining(long attemptsRemaining) {
        m_attemptsRemaining = attemptsRemaining;
    }

    /**
     * <p>getUnit</p>
     *
     * @return a {@link java.util.concurrent.TimeUnit} object.
     */
    public TimeUnit getUnit() {
        return m_unit;
    }

    /**
     * <p>setUnit</p>
     *
     * @param unit a {@link java.util.concurrent.TimeUnit} object.
     */
    public void setUnit(TimeUnit unit) {
        m_unit = unit;
    }

    /**
     * Creates a proper <code>ReaderSchedule</code> based by computing a new interval based on the
     * string representation of units from the ackd-configuration:
     *     hours(h), days(d), minutes(m), seconds(s), milliseconds(ms)
     *
     * note: if the specification in the configuration is seconds (s) or milliseconds (ms), then no computation is made
     * to adjust the interval.
     *
     * @param interval a long.
     * @param unit a {@link java.lang.String} object.
     * @return an adjusted <code>ReaderSchedule</code>
     */
    public static ReaderSchedule createSchedule(long interval, String unit) {
        TimeUnit tu = TimeUnit.SECONDS;
        
        if ("d".equals(unit)) {
            interval = interval * 60*60*24;
            
        } else if ("h".equals(unit)) {
            interval = interval * 60*60;
            
        } else if ("m".equals(unit)) {
            interval = interval * 60;
            
        } else if ("s".equals(unit)) {
            
        } else if ("ms".equals(unit)) {
            tu = TimeUnit.MILLISECONDS;
            
        }
        
        return createSchedule(interval, interval, 0, tu);
    }
    
}
