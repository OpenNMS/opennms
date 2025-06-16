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
package org.opennms.netmgt.scheduler.mock;

import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.scheduler.ScheduleInterval;
import org.opennms.netmgt.scheduler.Timer;


public class MockInterval implements ScheduleInterval {
    
    private Timer m_timer;
    private long m_interval;
    private List<Suspension> m_suspensions = new LinkedList<>();
    
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