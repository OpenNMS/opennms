package org.opennms.netmgt.poller.monitors;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.utils.ParameterMap;

public class TimeoutTracker {

    private int m_retry;
    private long m_timeoutInNanos;
    private long m_timeoutInMillis;
    private long m_timeoutInSeconds;
    private boolean m_strictTimeouts;
    
    private int m_attempt = 0;
    private long m_nextRetryTimeNanos = -1L;
    private long m_attemptStartTimeNanos = -1L;

    public TimeoutTracker(Map parameters, int defaultRetry, int defaultTimeout) {
        m_retry = ParameterMap.getKeyedInteger(parameters, "retry", defaultRetry);

        // make sure the timeout is a least 10 millis
        m_timeoutInMillis = Math.max(10L, ParameterMap.getKeyedInteger(parameters, "timeout", defaultTimeout));
        m_timeoutInNanos = Math.max(10000000L, TimeUnit.NANOSECONDS.convert(m_timeoutInMillis, TimeUnit.MILLISECONDS));
        m_timeoutInSeconds = Math.max(1L, TimeUnit.SECONDS.convert(m_timeoutInMillis, TimeUnit.SECONDS));


        m_strictTimeouts = ParameterMap.getKeyedBoolean(parameters, "strict-timeout", false);
        
        resetAttemptStartTime();

    }

    public boolean shouldRetry() {
        return m_attempt <= m_retry;
    }
    
    public long getTimeoutInMillis() {
        return m_timeoutInMillis;
    }
    
    public long getTimeoutInSeconds() {
        return m_timeoutInSeconds;
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
        return convertFromNanos(elapsedTimeNanos(), TimeUnit.MILLISECONDS);
    }
    
    public long elapsedTimeNanos() {
        long nanoTime = System.nanoTime();
        assertStarted();
        return nanoTime - m_attemptStartTimeNanos;
    }
    
    public double elapsedTime(TimeUnit unit) {
        return convertFromNanos(elapsedTimeNanos(), unit);
    }

    private double convertFromNanos(double nanos, TimeUnit unit) {
        double nanosPerUnit = TimeUnit.NANOSECONDS.convert(1, unit);
        return nanos/nanosPerUnit;
    }
    
    @Override
    public String toString() {
        return new StringBuilder(64)
            .append("timeout: ").append(getTimeoutInMillis()).append("ms")
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
