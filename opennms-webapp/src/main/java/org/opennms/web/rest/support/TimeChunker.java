package org.opennms.web.rest.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TimeChunker {
    
    public class TimeChunk {
        private Date m_startDate;
        private Date m_endDate;
        
        public TimeChunk(Date startDate, Date endDate) {
            setStartDate(startDate);
            setEndDate(endDate);
        }
        
        public void setStartDate(Date startDate) {
            m_startDate = startDate;
        }
        public Date getStartDate() {
            return m_startDate;
        }
        public void setEndDate(Date endDate) {
            m_endDate = endDate;
        }
        public Date getEndDate() {
            return m_endDate;
        }
        
    }
    
    public class Chunks extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        public Chunks(String message) {
            super(message);
        }
        
    }

    public static final int MINUTE = 300000;
    public static final int HOURLY = 3600000;
    public static final int DAILY = 86400000;
    
    private List<TimeChunk> m_resolutionSegments = new ArrayList<TimeChunk>();
    private Iterator<TimeChunk> m_itr;
    
    public TimeChunker(int resolution, long startTime, long timeLengthInMilliseconds) {
        createTimeSegments(m_resolutionSegments, resolution, startTime, timeLengthInMilliseconds);
    }
    
    private void createTimeSegments(List<TimeChunk> resolutionSegments, int resolution, long startTime, long timeInMilliseconds) {
        for(long i = 0; i < timeInMilliseconds; i+=resolution) {
            Date startDate = new Date(startTime + i);
            Date endDate = new Date(startTime + i + resolution);
            TimeChunk segment = new TimeChunk(startDate, endDate);
            m_resolutionSegments.add(segment);
        }
        m_itr = m_resolutionSegments.iterator();
    }

    public int getSegmentCount() {
        return m_resolutionSegments.size();
    }
    
    public boolean hasNext() {
        return m_itr.hasNext();
    }
    
    public TimeChunk getNextSegment() {
        return m_itr.next(); 
    }
    
    
    public void throwChunks() throws Chunks {
        throw new Chunks("Ewww gross you just threw chunks");
    }
    
    
}
