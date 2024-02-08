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
package org.opennms.netmgt.scheduler;


import junit.framework.TestCase;

import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.scheduler.mock.MockInterval;
import org.opennms.netmgt.scheduler.mock.MockScheduler;

/**
 * Represents a ScheduleTest 
 *
 * @author brozow
 */
public class ScheduleTest extends TestCase {

    private MockSchedulable m_schedulable;
    private MockInterval m_interval;
    private MockScheduler m_scheduler;
    private Schedule m_sched;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ScheduleTest.class);
    }
    
    class MockSchedulable implements ReadyRunnable {
        private volatile int runCount = 0;
        private volatile boolean m_callingAdjustSchedule;
        
        @Override
        public boolean isReady() {
            return true;
        }
    
        @Override
        public void run() {
            runCount++;
            if (isCallingAdjustSchedule())
                m_sched.adjustSchedule();
        }
        
        public int getRunCount() {
            return runCount;
        }
        
        public void setCallingAdjustSchedule(boolean callingAdjustSchedule) {
            m_callingAdjustSchedule = callingAdjustSchedule;
        }
        
        public boolean isCallingAdjustSchedule() {
            return m_callingAdjustSchedule;
        }
        
    }
    
    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLogAppender.setupLogging();
        m_schedulable = new MockSchedulable();
        m_scheduler = new MockScheduler();
        m_interval = new MockInterval(m_scheduler, 1000L);
        m_sched = new Schedule(m_schedulable, m_interval, m_scheduler);        
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
        super.tearDown();
    }
    
    public void testSchedule() {
        m_sched.schedule();
        
        assertRunAndScheduled(0, 0, 0, 1);


        m_scheduler.next();

        assertRunAndScheduled(0, 1000, 1, 1);

        m_scheduler.next();
        
        assertRunAndScheduled(1000, 1000, 2, 1);
    }
    
    public void testAdjustSchedule() {
        
        m_sched.schedule();
        
        assertRunAndScheduled(0, 0, 0, 1);
        
        m_scheduler.next();
        
        m_interval.setInterval(900);
        m_sched.adjustSchedule();
        
        assertRunAndScheduled(0, 900, 1, 2);
        
        m_scheduler.next();
        
        assertRunAndScheduled(900, 900, 2, 2);

        // jump to the expired entry
        m_scheduler.next();
        
        // note that we don't increase the run count
        assertRunAndScheduled(1000, 800, 2, 1);
        
        m_scheduler.next();

        assertRunAndScheduled(1800, 900, 3, 1);
        
        m_scheduler.next();
        
        assertRunAndScheduled(2700, 900, 4, 1);
        
        m_interval.setInterval(1000);
        m_sched.adjustSchedule();
        
        // jump to the expired entry
        m_scheduler.next();
        
        assertRunAndScheduled(3600, 100, 4, 1);
        
        m_scheduler.next();

        assertRunAndScheduled(3700, 1000, 5, 1);
        
    }
    
    public void testUnschedule() {
        m_sched.schedule();
        
        assertRunAndScheduled(0, 0, 0, 1);

        m_scheduler.next();

        assertRunAndScheduled(0, 1000, 1, 1);

        m_scheduler.next();
        
        assertRunAndScheduled(1000, 1000, 2, 1);
        
        m_sched.unschedule();
        
        // jump to the expired entry
        m_scheduler.next();
        
        assertRunAndScheduled(2000, -1, 2, 0);
    }
    
    public void testTemporarilySuspend() {
        m_interval.addSuspension(1500, 2500);
        
        m_sched.schedule();
        
        assertRunAndScheduled(0, 0, 0, 1);

        m_scheduler.next();

        assertRunAndScheduled(0, 1000, 1, 1);

        m_scheduler.next();
        
        assertRunAndScheduled(1000, 1000, 2, 1);

        // this is the suspended entry
        m_scheduler.next();
        
        // assert that the entry has not run
        assertRunAndScheduled(2000, 1000, 2, 1);
        
        m_scheduler.next();
        
        assertRunAndScheduled(3000, 1000, 3, 1);
    }
    
    public void testAdjustScheduleWithinRun() {
        m_schedulable.setCallingAdjustSchedule(true);
        
        m_sched.schedule();
        
        assertRunAndScheduled(0, 0, 0, 1);

        m_scheduler.next();

        assertRunAndScheduled(0, 1000, 1, 1);

        m_scheduler.next();
        
        assertRunAndScheduled(1000, 1000, 2, 1);
    }
    
    private void assertRunAndScheduled(long currentTime, long interval, int count, int entryCount) {
        assertEquals(count, m_schedulable.getRunCount());
        assertEquals(currentTime, m_scheduler.getCurrentTime());
        assertEquals(entryCount, m_scheduler.getEntryCount());
        if (entryCount > 0)
            assertNotNull(m_scheduler.getEntries().get(Long.valueOf(currentTime+interval)));
        
    }


}
