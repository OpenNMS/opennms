/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.mock;

import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.scheduler.ScheduleInterval;
import org.opennms.netmgt.scheduler.Timer;


public class MockInterval implements ScheduleInterval {
    
    private Timer m_timer;
    private long m_interval;
    private List<Suspension> m_suspensions = new LinkedList<Suspension>();
    
    /**
     * @param l
     */
    public MockInterval(Timer timer, long interval) {
        m_timer = timer;
        m_interval = interval;
    }
    
    @Override
    public long getInterval() {
        return m_interval;
    }

    public void setInterval(long interval) {
        m_interval = interval;
    }
    
    class Suspension {
        private long m_start;
        private long m_end;

        Suspension(long start, long end) {
            m_start = start;
            m_end = end;
        }
        
        public boolean contains(long time) {
            return m_start <= time && time <= m_end;
        }
    }

    public void addSuspension(long start, long end) {
        m_suspensions.add(new Suspension(start, end));
    }
    
    @Override
    public boolean scheduledSuspension() {
        for (Suspension suspension : m_suspensions) {
            if (suspension.contains(m_timer.getCurrentTime())) {
                return (suspension.m_end - m_timer.getCurrentTime()) > 0;
            }
        }
        return false;
    }
}