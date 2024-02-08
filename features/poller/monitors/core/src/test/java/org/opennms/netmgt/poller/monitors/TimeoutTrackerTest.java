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
package org.opennms.netmgt.poller.monitors;

import java.util.Collections;
import java.util.Map;

import org.opennms.core.utils.TimeoutTracker;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class TimeoutTrackerTest extends TestCase {
    
    public void testShouldRetry() {
        
        int retries = 2;
        
        Map<String,?> emptyMap = Collections.emptyMap();
        TimeoutTracker tracker = new TimeoutTracker(emptyMap, retries, 3000);
        
        int count = 0;
        for(tracker.reset(); tracker.shouldRetry(); tracker.nextAttempt()) {
            tracker.startAttempt();
            count++;
            assertTrue(tracker.elapsedTimeInMillis() < 100);
        }
        
        assertEquals("expected one try and 2 retries", 3, count);
    }
    
    
    public void testElapsedTimeButNoStartAttempt() {
        
        Map<String,?> emptyMap = Collections.emptyMap();
        TimeoutTracker tracker = new TimeoutTracker(emptyMap, 0, 3000);
        
        try {
            tracker.elapsedTimeInMillis();
            fail("expected an exception since no startAttempt is called");
        } catch(IllegalStateException e) {
            // w00t.. 
        }
        
    }
    
    public void testElapsedTime() throws InterruptedException {
        
        long sleepTime = 200L;
        
        Map<String,?> emptyMap = Collections.emptyMap();
        TimeoutTracker tracker = new TimeoutTracker(emptyMap, 0, 3000);

        tracker.startAttempt();
        
        Thread.sleep(sleepTime, 0);
        
        double elapsedTimeInMillis = tracker.elapsedTimeInMillis();
        
        long minTime = sleepTime;
        long maxTime = 2 * sleepTime;
        assertTrue("Expected value for elapsedTimeInMillis should be greater than " + minTime, elapsedTimeInMillis > (minTime - 1));
        assertTrue("Expected value for elapsedTimeInMillis should be less than " + maxTime, elapsedTimeInMillis < (maxTime + 1));
    }

}
