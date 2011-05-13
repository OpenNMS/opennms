/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: November 2, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
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
