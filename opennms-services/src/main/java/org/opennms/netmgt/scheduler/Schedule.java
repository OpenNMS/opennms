//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.scheduler;

import java.util.Random;

import org.opennms.core.utils.ThreadCategory;


/**
 * Represents a Schedule
 *
 * @author brozow
 * @version $Id: $
 */
public class Schedule {

	/** Constant <code>random</code> */
	public static final Random random = new Random();
	
    private final ReadyRunnable m_schedulable;
    private final ScheduleInterval m_interval;
    private final ScheduleTimer m_timer;
    private volatile int m_currentExpirationCode;
    private volatile long m_currentInterval;
    private volatile boolean m_scheduled = false;
	
    
    class ScheduleEntry implements ReadyRunnable {
        private final int m_expirationCode;

        public ScheduleEntry(int expirationCode) {
            m_expirationCode = expirationCode;
        }
        
        /**
         * @return
         */
        private boolean isExpired() {
            return m_expirationCode < m_currentExpirationCode;
        }
        
        public boolean isReady() {
            return isExpired() || m_schedulable.isReady();
        }

        public void run() {
            if (isExpired()) {
                ThreadCategory.getInstance(getClass()).debug("Schedule "+this+" expired.  No need to run.");
                return;
            }
            
            if (!m_interval.scheduledSuspension()) {
                try {
                    Schedule.this.run();
                } catch (PostponeNecessary e) {
				   // Chose a random number of seconds between 5 and 14 to wait before trying again
                    m_timer.schedule(random.nextInt(10)*1000+5000, this);
                    return;
                }
            }
                

            // if it is expired by the current run then don't reschedule
            if (isExpired()) {
                ThreadCategory.getInstance(getClass()).debug("Schedule "+this+" expired.  No need to reschedule.");
                return;
            }
            
            long interval = m_interval.getInterval();
            if (interval >= 0 && m_scheduled)
                m_timer.schedule(interval, this);

        }
        
        public String toString() { return "ScheduleEntry[expCode="+m_expirationCode+"] for "+m_schedulable; }
    }

    /**
     * <p>Constructor for Schedule.</p>
     *
     * @param interval a {@link org.opennms.netmgt.scheduler.ScheduleInterval} object.
     * @param timer a {@link org.opennms.netmgt.scheduler.ScheduleTimer} object.
     * @param schedulable a {@link org.opennms.netmgt.scheduler.ReadyRunnable} object.
     */
    public Schedule(ReadyRunnable schedulable, ScheduleInterval interval, ScheduleTimer timer) {
        m_schedulable = schedulable;
        m_interval = interval;
        m_timer = timer;
        m_currentExpirationCode = 0;
    }

    /**
     * <p>schedule</p>
     */
    public void schedule() {
        m_scheduled = true;
        schedule(0);
    }

    private void schedule(long interval) {
        if (interval >= 0 && m_scheduled)
            m_timer.schedule(interval, new ScheduleEntry(++m_currentExpirationCode));
    }

    /**
     * <p>run</p>
     */
    public void run() {
        m_schedulable.run();
    }

    /**
     * <p>adjustSchedule</p>
     */
    public void adjustSchedule() {
        schedule(m_interval.getInterval());
    }

    /**
     * <p>unschedule</p>
     */
    public void unschedule() {
        m_scheduled = false;
        m_currentExpirationCode++;
    }

}
