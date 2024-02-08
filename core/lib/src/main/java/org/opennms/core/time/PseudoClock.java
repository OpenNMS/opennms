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
package org.opennms.core.time;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Singleton used to track and control time independently of the system clock.
 *
 * This class is currently shared by implementations and test APIs  to help share
 * the same reference point to time.
 *
 * @author jwhite
 */
public class PseudoClock {

    private static PseudoClock instance = new PseudoClock();

    private AtomicLong time = new AtomicLong();

    private PseudoClock() {
        reset();
    }

    public static PseudoClock getInstance() {
        return instance;
    }

    /**
     * Resets the clock time to zero.
     */
    public void reset() {
        time.set(0);
    }

    /**
     * Retrieves the current time.
     *
     * @return current timestamp in ms
     */
    public long getTime() {
        return time.get();
    }

    /**
     * Advances the clock time
     *
     * @param duration duration of time in the given units
     * @param unit time unit
     * @return the absolute timestamp of the clock, following the advance
     */
    public long advanceTime(long duration, TimeUnit unit) {
        return time.addAndGet(unit.toMillis(duration));
    }
}
