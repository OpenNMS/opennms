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
package org.opennms.web.rest.v1.support;

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
    
    private List<TimeChunk> m_resolutionSegments = new ArrayList<>();
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
