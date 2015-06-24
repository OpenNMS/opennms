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

package org.opennms.web.rest;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opennms.core.utils.TimeInterval;
import org.opennms.core.utils.TimeIntervalSequence;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.web.rest.support.TimeChunker;
import org.opennms.web.rest.support.TimeChunker.TimeChunk;

public class AvailCalculator {
    
    public static class UptimeCalculator {
        
        public static int count = 0;
        
        private TimeChunker m_timeChunker;
        
        SortedSet<OnmsLocationSpecificStatus> m_statusChanges = new TreeSet<OnmsLocationSpecificStatus>(new Comparator<OnmsLocationSpecificStatus>(){

            @Override
            public int compare(OnmsLocationSpecificStatus o1, OnmsLocationSpecificStatus o2) {
                return o1.getPollResult().getTimestamp().compareTo(o2.getPollResult().getTimestamp());
            }
        
        });

        public UptimeCalculator(TimeChunker timeChunker) {
            m_timeChunker = timeChunker;
//            m_upIntervals = new TimeIntervalSequence[m_timeChunker.getSegmentCount()];
//            
//            for(int i = 0; i < m_timeChunker.getSegmentCount(); i++) {
//                TimeChunk chunk = m_timeChunker.getAt(i);
//                m_upIntervals[i] = new TimeIntervalSequence(new TimeInterval(chunk.getStartDate(), chunk.getEndDate()));
//            }
        }
        
        public Date timestamp(OnmsLocationSpecificStatus status) {
            return new Date(status.getPollResult().getTimestamp().getTime());
        }

        public void onStatusChange(OnmsLocationSpecificStatus statusChange) {
            
//            Date startDate = m_lastChange == null ? new Date(0) : timestamp(m_lastChange);
//            Date endDate = timestamp(statusChange);
//            
//            int startIndex = m_timeChunker.getIndexContaining(startDate);
//            int endIndex = m_timeChunker.getIndexContaining(endDate);
//            
//            if (startIndex < 0) startIndex = 0;
//            if (endIndex >= m_timeChunker.getSegmentCount()) endIndex = m_timeChunker.getSegmentCount()-1;
//            
//            TimeInterval interval = new TimeInterval(startDate, endDate);
//            
//            if (m_lastChange != null && m_lastChange.getPollResult().isDown()) {
//            
//                for(int i = startIndex; i <= endIndex; i++) {
//                    m_upIntervals[i].removeInterval(interval);
//                }
//            }
//            m_lastChange = statusChange;
            m_statusChanges.add(statusChange);
        }

        public double getUptimePercentage(int index) {
            
//            if (m_lastChange != null && m_lastChange.getPollResult().isDown()) {
//                Date start = timestamp(m_lastChange);
//                Date end = m_timeChunker.getEndDate();
//                int startIndex = m_timeChunker.getIndexContaining(start);
//                if (startIndex < 0) startIndex = 0;
//                TimeInterval interval = new TimeInterval(start, end);
//                for(int i = startIndex; i < m_timeChunker.getSegmentCount(); i++) {
//                    m_upIntervals[i].removeInterval(interval);
//                }
//                m_lastChange = null;
//                
//            }
//            return uptime(m_timeChunker.getAt(index), m_upIntervals[index]);
            
            TimeChunk chunk = m_timeChunker.getAt(index);
            TimeIntervalSequence uptime = new TimeIntervalSequence(new TimeInterval(chunk.getStartDate(), chunk.getEndDate()));
            
            OnmsLocationSpecificStatus lastChange = null;
            for(OnmsLocationSpecificStatus status : m_statusChanges) {
                count++;
                Date start = (lastChange == null ? new Date(0) : new Date(lastChange.getPollResult().getTimestamp().getTime()));
                Date end = new Date(status.getPollResult().getTimestamp().getTime());
                if (lastChange != null && lastChange.getPollResult().isDown() ) {
                    if(start.before(end)) {
                        uptime.removeInterval(new TimeInterval(start, end));
                    }
                }
                lastChange = status;
            }
            
            if (lastChange != null && lastChange.getPollResult().isDown() ) {
                Date start = new Date(lastChange.getPollResult().getTimestamp().getTime());
                Date end = new Date(chunk.getEndDate().getTime());
                if(start.before(end)) {
                    uptime.removeInterval(new TimeInterval(start, end));
                }
            }
            
            return uptime(chunk, uptime);
        }

        private double uptime(TimeChunk chunk, TimeIntervalSequence uptime) {
            TimeIntervalSequence sequence = uptime;
            long uptimeMillis = 0;
            for(Iterator<TimeInterval> it = sequence.iterator(); it.hasNext(); ) {
                TimeInterval interval = it.next();
                uptimeMillis += (interval.getEnd().getTime() - interval.getStart().getTime());
            }
            
            long totalMillis = chunk.getEndDate().getTime() - chunk.getStartDate().getTime();
            
            return ((double)uptimeMillis)/((double)totalMillis);
        }
        
    }
    
    public static class ServiceAvailCalculator {
        Map<OnmsLocationMonitor, UptimeCalculator> m_uptimeCalculators = new HashMap<OnmsLocationMonitor, UptimeCalculator>();
        TimeChunker m_timeChunker;
        public ServiceAvailCalculator(TimeChunker timeChunker) {
            m_timeChunker = timeChunker;
        }
        public void onStatusChange(OnmsLocationSpecificStatus statusChange) {
            UptimeCalculator calc = m_uptimeCalculators.get(statusChange.getLocationMonitor());
            if (calc == null) {
                calc = new UptimeCalculator(m_timeChunker);
                m_uptimeCalculators.put(statusChange.getLocationMonitor(), calc);
            }
            
            calc.onStatusChange(statusChange);
        }
        public double getAvailability(int index) {
            double sum = 0.0;
            for(final Map.Entry<OnmsLocationMonitor, UptimeCalculator> entry : m_uptimeCalculators.entrySet()) {
                sum += entry.getValue().getUptimePercentage(index);
            }
            return (m_uptimeCalculators.size() == 0 ? 1.0 : sum / m_uptimeCalculators.size());
        }
        
    }
    /**
     * StatusChange:
     *  - locationMonitor
     *  - service
     *  - status
     *  - timestamp
     *  
     *  service* -- 1application
     *  
     *  statuschange* -- 1service
     *  
     *  statuschange* -- 1monitor
     *  
     *  Trying to calculate overall availability of an application 
     *  
     *  application availability for a monitor for a time period is the average of the 
     *  availability of the individual services of the application for that time period for
     *  that monitor
     *  
     *  service availability for a monitor for a time period is the percent of time in the time period 
     *  that the service is available for that monitor discounting monitor disconnection periods and
     *  stoppages.  A service whose monitor is stopped or disconnected for a time period is counted at 
     *  100% available for that time period
     *  
     *  overall availability of an application is the average of the availability at all monitors.
     *  
     */
    
    
    Map<OnmsMonitoredService, ServiceAvailCalculator> m_svcCalculators = new HashMap<OnmsMonitoredService, ServiceAvailCalculator>();
    TimeChunker m_timeChunker;
    
    public AvailCalculator(TimeChunker timeChunker) {
        m_timeChunker = timeChunker;
    }


    public void onStatusChange(OnmsLocationSpecificStatus statusChange) {
        ServiceAvailCalculator calc = getServiceAvailCalculator(statusChange.getMonitoredService());
        calc.onStatusChange(statusChange);
    }


    private ServiceAvailCalculator getServiceAvailCalculator(OnmsMonitoredService svc) {
        ServiceAvailCalculator calc = m_svcCalculators.get(svc);
        if (calc == null) {
            calc = new ServiceAvailCalculator(m_timeChunker);
            m_svcCalculators.put(svc, calc);
        }
        return calc;
    }
    
    
    public double getAvailabilityFor(final Collection<OnmsMonitoredService> svcs, final int index) {
        double sum = 0.0;
        for(final OnmsMonitoredService svc : svcs) {
            sum += getServiceAvailCalculator(svc).getAvailability(index);
        }
        
        return svcs.size() == 0 ? 1.0 : sum / svcs.size();
    }


}
