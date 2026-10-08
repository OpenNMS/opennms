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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ScheduleTest {

    private static class RecordingTimer implements ScheduleTimer {
        final List<ReadyRunnable> scheduled = new ArrayList<>();

        @Override
        public void schedule(long interval, ReadyRunnable schedule) {
            scheduled.add(schedule);
        }

        @Override
        public long getCurrentTime() {
            return 0;
        }
    }

    private static final ScheduleInterval EVERY_SECOND = new ScheduleInterval() {
        @Override
        public long getInterval() {
            return 1000;
        }

        @Override
        public boolean scheduledSuspension() {
            return false;
        }
    };

    @Test
    public void reschedulesAfterRunnableThrows() {
        final RecordingTimer timer = new RecordingTimer();
        final ReadyRunnable failing = new ReadyRunnable() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void run() {
                throw new IllegalStateException("poll failed");
            }
        };

        final Schedule schedule = new Schedule(failing, EVERY_SECOND, timer);
        schedule.schedule();
        assertEquals(1, timer.scheduled.size());

        timer.scheduled.get(0).run();
        assertEquals("a failed run must still be rescheduled", 2, timer.scheduled.size());
    }
}
