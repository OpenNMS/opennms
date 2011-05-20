package org.opennms.web.rest;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
import org.opennms.web.rest.support.TimeChunker;


public class ResolutionTest {

    @Test
    public void testResolution() {
        Date startDate = new Date(new Date().getTime() - 300000);
        long startTime = startDate.getTime();
        long endTime = startDate.getTime() + 300000;
        long timeLength = endTime - startTime;
        
        TimeChunker resolution = new TimeChunker(TimeChunker.MINUTE, startTime, timeLength);
        
        assertEquals(1, resolution.getSegmentCount());
        Date startDate1 = resolution.getNextSegment().getStartDate();
        while(resolution.hasNext()) {
            System.err.println("startDate segment1: " + startDate1);
            assertEquals(startDate, startDate1);
        }
    }
}
