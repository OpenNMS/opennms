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
        
        TimeChunker resolution = new TimeChunker(TimeChunker.MINUTE, startDate, new Date(endTime));
        
        assertEquals(1, resolution.getSegmentCount());
        Date startDate1 = resolution.getNextSegment().getStartDate();
        while(resolution.hasNext()) {
            System.err.println("startDate segment1: " + startDate1);
            assertEquals(startDate, startDate1);
        }
    }
    
    @Test
    public void testGetTimeIndex() {
        Date startDate = new Date(new Date().getTime() - 300000);
        long endTime = startDate.getTime() + 300000;
        
        TimeChunker resolution = new TimeChunker(60000, startDate, new Date(endTime));
        Date date = new Date(startDate.getTime() + 150000);
        assertEquals(2, resolution.getIndexContaining(date));
        
    }
}
