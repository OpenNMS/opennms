/**
 * 
 */
package org.opennms.netmgt.ackd.readers;

import java.util.concurrent.TimeUnit;

class ReaderSchedule {
    
    private long m_initialDelay;
    private long m_interval;
    private long m_attemptsRemaining;
    private TimeUnit m_unit;
    
    public static ReaderSchedule createSchedule() {
        return new ReaderSchedule();
    }

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

    public long getInitialDelay() {
        return m_initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        m_initialDelay = initialDelay;
    }

    public long getInterval() {
        return m_interval;
    }

    public void setInterval(long interval) {
        m_interval = interval;
    }

    public long getAttemptsRemaining() {
        return m_attemptsRemaining;
    }

    public void setAttemptsRemaining(long attemptsRemaining) {
        m_attemptsRemaining = attemptsRemaining;
    }

    public TimeUnit getUnit() {
        return m_unit;
    }

    public void setUnit(TimeUnit unit) {
        m_unit = unit;
    }
    
}