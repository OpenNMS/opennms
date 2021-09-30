/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
