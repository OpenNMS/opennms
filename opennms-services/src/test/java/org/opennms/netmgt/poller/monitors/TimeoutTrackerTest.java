package org.opennms.netmgt.poller.monitors;

import java.util.Collections;

import junit.framework.TestCase;

public class TimeoutTrackerTest extends TestCase {
    
    public void testShouldRetry() {
        
        int retries = 2;
        
        TimeoutTracker tracker = new TimeoutTracker(Collections.emptyMap(), retries, 3000);
        
        int count = 0;
        for(tracker.reset(); tracker.shouldRetry(); tracker.nextAttempt()) {
            count++;
        }
        
        assertEquals("expected one try and 2 retries", 3, count);
    }
    
    
    public void testElapsedTimeButNoStartAttempt() {
        
        TimeoutTracker tracker = new TimeoutTracker(Collections.emptyMap(), 0, 3000);
        
        try {
            tracker.elapsedTimeInMillis();
            fail("expected an exception since no startAttempt is called");
        } catch(IllegalStateException e) {
            // w00t.. 
        }
        
    }
    
    public void testElapsedTime() throws InterruptedException {
        
        long sleepTime = 100L;
        
        TimeoutTracker tracker = new TimeoutTracker(Collections.emptyMap(), 0, 3000);

        tracker.startAttempt();
        
        Thread.sleep(sleepTime);
        
        double elapsedTimeInMillis = tracker.elapsedTimeInMillis();
        assertTrue("Unexpected value for elapsedTimeInMillis: "+elapsedTimeInMillis, elapsedTimeInMillis > sleepTime);
        assertTrue("Unexpected value for elapsedTimeInMillis: "+elapsedTimeInMillis, elapsedTimeInMillis < 2 * sleepTime);
        
    }

}
