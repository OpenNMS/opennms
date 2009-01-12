package org.opennms.netmgt.snmpinterfacepoller.pollable;

import org.opennms.netmgt.scheduler.ScheduleInterval;
import org.opennms.netmgt.scheduler.Timer;

public class PollableSnmpInterfaceConfig implements ScheduleInterval {

    private Timer m_timer;
    private long interval;
    
    public long getInterval() {
        return interval;
    }

    public boolean scheduledSuspension() {
        return false;
    }

    public long getCurrentTime() {
        return m_timer.getCurrentTime();
    }

    public PollableSnmpInterfaceConfig(Timer timer, long interval) {
        super();
        m_timer = timer;
        this.interval = interval;
    }

}
