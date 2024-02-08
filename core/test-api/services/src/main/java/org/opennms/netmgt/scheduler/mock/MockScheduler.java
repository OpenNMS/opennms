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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;


public class MockScheduler implements Scheduler {
    
    private MockTimer m_timer;
    /*
     * TODO: Use it or loose it.
     * Commented out because it is not currently used in this monitor
     */
    //private long m_currentTime = 0;
    private SortedMap<Long, List<ReadyRunnable>> m_scheduleEntries = new TreeMap<Long, List<ReadyRunnable>>();

    /**
     * Used to keep track of the number of tasks that have been executed.
     */
    private long m_numTasksExecuted = 0;

    public MockScheduler() {
        this(new MockTimer());
    }
    
    public MockScheduler(MockTimer timer) {
        m_timer = timer;
    }

    
    @Override
    public void schedule(long interval, ReadyRunnable schedule) {
        Long nextTime = Long.valueOf(getCurrentTime()+interval);
        //MockUtil.println("Scheduled "+schedule+" for "+nextTime);
        List<ReadyRunnable> entries = m_scheduleEntries.get(nextTime);
        if (entries == null) {
            entries = new LinkedList<ReadyRunnable>();
            m_scheduleEntries.put(nextTime, entries);
        }
            
        entries.add(schedule);
    }
    
    public int getEntryCount() {
        return m_scheduleEntries.size();
    }
    
    public Map<Long, List<ReadyRunnable>> getEntries() {
        return m_scheduleEntries;
    }
    
    public long getNextTime() {
        if (m_scheduleEntries.isEmpty()) {
            throw new IllegalStateException("Nothing scheduled");
        }

        Long nextTime = m_scheduleEntries.firstKey();
        return nextTime.longValue();
    }
    
    public long next() {
        if (m_scheduleEntries.isEmpty()) {
            throw new IllegalStateException("Nothing scheduled");
        }
        
        Long nextTime = m_scheduleEntries.firstKey();
        List<ReadyRunnable> entries = m_scheduleEntries.get(nextTime);
        Runnable runnable = entries.get(0);
        m_timer.setCurrentTime(nextTime.longValue());
        entries.remove(0);
        if (entries.isEmpty()) {
            m_scheduleEntries.remove(nextTime);
        }
        runnable.run();
        m_numTasksExecuted++;
        return getCurrentTime();
    }
    
    public long tick(int step) {
        if (m_scheduleEntries.isEmpty()) {
            throw new IllegalStateException("Nothing scheduled");
        }
        
        long endTime = getCurrentTime()+step;
        while (getNextTime() <= endTime) {
            next();
        }
        
        m_timer.setCurrentTime(endTime);
        return getCurrentTime();
    }

    @Override
    public long getCurrentTime() {
        return m_timer.getCurrentTime();
    }

    @Override
	public void start() {
		
	}

    @Override
	public void stop() {
	}

    @Override
	public void pause() {
	}

    @Override
	public void resume() {
	}

    @Override
	public int getStatus() {
		return 0;
	}

    @Override
    public long getNumTasksExecuted() {
        return m_numTasksExecuted;
    }
}
