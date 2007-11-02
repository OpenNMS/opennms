package org.opennms.netmgt.poller.monitors;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.utils.ParameterMap;

public class TimeoutTracker {

    private int m_retry;
    private long m_timeoutInNanos;
    private boolean m_strictTimeouts;
    
    private int m_attempt = 0;
    private long m_nextRetryTimeNanos = -1L;
    private long m_attemptStartTimeNanos = -1L;

    public TimeoutTracker(Map parameters, int defaultRetry, int defaultTimeout) {
        m_retry = ParameterMap.getKeyedInteger(parameters, "retry", defaultRetry);

        // make sure the timeout is a least 10 millis
        long m_timeoutInMillis = Math.max(10, ParameterMap.getKeyedInteger(parameters, "timeout", defaultTimeout));
        
        m_timeoutInNanos = TimeUnit.NANOSECONDS.convert(m_timeoutInMillis, TimeUnit.MILLISECONDS);

        m_strictTimeouts = ParameterMap.getKeyedBoolean(parameters, "strict-timeout", false);
        
        resetAttemptStartTime();

    }

    public boolean shouldRetry() {
        return m_attempt <= m_retry;
    }
    
    public long getTimeoutInMillis() {
        return getTimeout(TimeUnit.MILLISECONDS);
    }
    
    public long getTimeoutInSeconds() {
        return getTimeout(TimeUnit.SECONDS);
    }
    

    public long getTimeout(TimeUnit units) {
        long rounder = TimeUnit.NANOSECONDS.convert(1, units);
        // add rounder so it rounds to the nearest rather than truncating
        long converted = units.convert(m_timeoutInNanos + rounder / 2, TimeUnit.NANOSECONDS);
        
        // never return 0 which often means an infinite timeout
        if (converted == 0) {
            return 1;
        } else {
            return converted;
        }
    }
    
    public void reset() {
        m_attempt = 0;
        resetAttemptStartTime();
    }

    private void resetAttemptStartTime() {
        m_attemptStartTimeNanos = -1L;
    }

    public void nextAttempt() {
        m_attempt++;
        resetAttemptStartTime();
    }

    public int getAttempt() {
        return m_attempt;
    }

    public void startAttempt() {
        long now = System.nanoTime();
        while (m_strictTimeouts && now < m_nextRetryTimeNanos) {
            sleep(m_nextRetryTimeNanos - now);
            now = System.nanoTime();
        }
        // create a connected socket
        //
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
    
    public double elapsedTimeInMillis() {
        return elapsedTime(TimeUnit.MILLISECONDS);
    }
    
    public long elapsedTimeNanos() {
        assertStarted();
        long nanoTime = System.nanoTime();
        return nanoTime - m_attemptStartTimeNanos;
    }
    
    public double elapsedTime(TimeUnit unit) {
        double nanos = elapsedTimeNanos();
        double nanosPerUnit = TimeUnit.NANOSECONDS.convert(1, unit);
        return nanos/nanosPerUnit;
    }

    @Override
    public String toString() {
        return new StringBuilder(64)
            .append("timeout: ").append(elapsedTimeInMillis()).append("ms")
            .append(" retry: ").append(m_attempt).append(" of ").append(m_retry)
            .toString();

    }

    public int getSoTimeout() {
        return (int)getTimeoutInMillis();
    }
    
    public int getConnectionTimeout() {
        return (int)getTimeoutInMillis();
    }
    


}
