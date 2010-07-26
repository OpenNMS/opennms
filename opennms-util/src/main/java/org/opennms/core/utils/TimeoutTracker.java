/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.core.utils;

import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Helper class used for tracking retires and timeouts for ServiceMonitors.
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class TimeoutTracker {

    private final int m_retry;
    private final long m_timeoutInNanos;
    private final long m_timeoutInMillis;
    private final long m_timeoutInSeconds;
    private final boolean m_strictTimeouts;
    
    private int m_attempt = 0;
    private long m_nextRetryTimeNanos = -1L;
    private long m_attemptStartTimeNanos = -1L;

    /**
     * <p>Constructor for TimeoutTracker.</p>
     *
     * @param parameters a {@link java.util.Map} object.
     * @param defaultRetry a int.
     * @param defaultTimeout a int.
     */
    public TimeoutTracker(Map parameters, int defaultRetry, int defaultTimeout) {
        m_retry = ParameterMap.getKeyedInteger(parameters, "retry", defaultRetry);

        // make sure the timeout is a least 10 millis
        m_timeoutInMillis = Math.max(10L, ParameterMap.getKeyedInteger(parameters, "timeout", defaultTimeout));
        m_timeoutInNanos = Math.max(10000000L, TimeUnit.NANOSECONDS.convert(m_timeoutInMillis, TimeUnit.MILLISECONDS));
        m_timeoutInSeconds = Math.max(1L, TimeUnit.SECONDS.convert(m_timeoutInMillis, TimeUnit.MILLISECONDS));


        m_strictTimeouts = ParameterMap.getKeyedBoolean(parameters, "strict-timeout", false);
        
        resetAttemptStartTime();

    }

    /**
     * <p>shouldRetry</p>
     *
     * @return a boolean.
     */
    public boolean shouldRetry() {
        return m_attempt <= m_retry;
    }
    
    /**
     * <p>getTimeoutInMillis</p>
     *
     * @return a long.
     */
    public long getTimeoutInMillis() {
        return m_timeoutInMillis;
    }
    
    /**
     * <p>getTimeoutInSeconds</p>
     *
     * @return a long.
     */
    public long getTimeoutInSeconds() {
        return m_timeoutInSeconds;
    }
    

    /**
     * <p>reset</p>
     */
    public void reset() {
        m_attempt = 0;
        resetAttemptStartTime();
    }

    private void resetAttemptStartTime() {
        m_attemptStartTimeNanos = -1L;
    }

    /**
     * <p>nextAttempt</p>
     */
    public void nextAttempt() {
        m_attempt++;
        resetAttemptStartTime();
    }

    /**
     * <p>getAttempt</p>
     *
     * @return a int.
     */
    public int getAttempt() {
        return m_attempt;
    }

    /**
     * <p>startAttempt</p>
     */
    public void startAttempt() {
        long now = System.nanoTime();
        while (m_strictTimeouts && now < m_nextRetryTimeNanos) {
            sleep(m_nextRetryTimeNanos - now);
            now = System.nanoTime();
        }

        m_attemptStartTimeNanos = System.nanoTime();
        m_nextRetryTimeNanos = m_attemptStartTimeNanos + m_timeoutInNanos;

    }

    private void sleep(long nanos) {
        long millis = nanos / 1000000L;
        int remainingNanos = (int)(nanos % 1000000L);

        try { Thread.sleep(millis, remainingNanos); } catch (InterruptedException e) {
            // we ignore InterruptedExceptions
        }
    }

    private void assertStarted() {
        if (m_attemptStartTimeNanos < 0) {
            throw new IllegalStateException("Failed to call startAttempt before requesting elapsedTime.. This is most likely a bug");
        }
    }
    
    /**
     * <p>elapsedTimeInMillis</p>
     *
     * @return a double.
     */
    public double elapsedTimeInMillis() {
        return convertFromNanos(elapsedTimeNanos(), TimeUnit.MILLISECONDS);
    }
    
    /**
     * <p>elapsedTimeNanos</p>
     *
     * @return a long.
     */
    public long elapsedTimeNanos() {
        long nanoTime = System.nanoTime();
        assertStarted();
        return nanoTime - m_attemptStartTimeNanos;
    }
    
    /**
     * <p>elapsedTime</p>
     *
     * @param unit a {@link java.util.concurrent.TimeUnit} object.
     * @return a double.
     */
    public double elapsedTime(TimeUnit unit) {
        return convertFromNanos(elapsedTimeNanos(), unit);
    }

    private double convertFromNanos(double nanos, TimeUnit unit) {
        double nanosPerUnit = TimeUnit.NANOSECONDS.convert(1, unit);
        return nanos/nanosPerUnit;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new StringBuilder(64)
            .append("timeout: ").append(getTimeoutInMillis()).append("ms")
            .append(" retry: ").append(m_attempt).append(" of ").append(m_retry)
            .toString();

    }

    /**
     * <p>getSoTimeout</p>
     *
     * @return a int.
     */
    public int getSoTimeout() {
        return (int)getTimeoutInMillis();
    }
    
    /**
     * <p>getConnectionTimeout</p>
     *
     * @return a int.
     */
    public int getConnectionTimeout() {
        return (int)getTimeoutInMillis();
    }
    


}
