/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TimeChunker {
    
    public static class TimeChunk {
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

        public boolean contains(Date changeTime) {
            return !changeTime.before(m_startDate) && !m_endDate.before(changeTime);
        }
        
    }
    
    public static class Chunks extends Exception {

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
    
    private Date m_startDate;
    private Date m_endDate;
    
    private List<TimeChunk> m_resolutionSegments = new ArrayList<TimeChunk>();
    private Iterator<TimeChunk> m_itr;
    private long m_resolution;
    
    public TimeChunker(long resolution, Date startDate, Date endDate) {
        m_startDate = startDate;
        m_endDate = endDate;
        m_resolution = resolution;
        createTimeSegments(m_resolutionSegments, resolution, startDate.getTime(), (endDate.getTime() - startDate.getTime()));
    }
    
    private void createTimeSegments(List<TimeChunk> resolutionSegments, long resolution, long startTime, long timeInMilliseconds) {
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
    
    public TimeChunk getAt(int index) {
        return index >= m_resolutionSegments.size() ? null : m_resolutionSegments.get(index);
    }
    
    public long getIndexContaining(Date timestamp) {
        return (long)(timestamp.getTime() - m_startDate.getTime()) / m_resolution;
    }
    
    
    public void throwChunks() throws Chunks {
        throw new Chunks("Ewww gross you just threw chunks");
    }

    public Date getStartDate() {
        return m_startDate;
    }

    public Date getEndDate() {
        return m_endDate;
    }
    
    
}
