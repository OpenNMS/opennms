/**
 * 
 */
package org.opennms.netmgt.ackd.readers;

import java.util.concurrent.TimeUnit;

public class ReaderSchedule {
    
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

    /**
     * Creates a proper <code>ReaderSchedule</code> based by computing a new interval based on the
     * string representation of units from the ackd-configuration:
     *     hours(h), days(d), minutes(m), seconds(s), milliseconds(ms)
     * 
     * note: if the specification in the configuration is seconds (s) or milliseconds (ms), then no computation is made 
     * to adjust the interval.
     * 
     * @param interval
     * @param unit
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