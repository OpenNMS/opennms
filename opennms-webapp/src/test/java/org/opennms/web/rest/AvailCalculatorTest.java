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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.web.rest.support.TimeChunker;


public class AvailCalculatorTest {

    private OnmsLocationMonitor m_locationMon;
    private OnmsMonitoredService m_svc;

    @Before
    public void setUp() {
        m_locationMon = new OnmsLocationMonitor();
        m_locationMon.setDefinitionName("IPv6");
        m_locationMon.setStatus(MonitorStatus.STARTED);
        
        m_svc = new OnmsMonitoredService();
    }
    
    
    @Test
    public void testGetAvailabilityOneStatus() {
        Date endTime = new Date(System.currentTimeMillis());
        Date startTime = new Date(endTime.getTime() - 100);
       
        PollStatus pollStatus = PollStatus.unavailable();
        Date timestamp = new Date(endTime.getTime() - 50);
        OnmsLocationSpecificStatus statusChange = createStatusChange(pollStatus, timestamp);
        TimeChunker chunker = new TimeChunker((int)(endTime.getTime() - startTime.getTime()), startTime, endTime);
        AvailCalculator calculator = new AvailCalculator(chunker);
        
        calculator.onStatusChange(statusChange);
        
        chunker.getNextSegment();
        double uptimePercent = calculator.getAvailabilityFor(getServices(), 0);
        assertEquals(0.5, uptimePercent, 0.00);
    }


    private OnmsLocationSpecificStatus createStatusChange(PollStatus pollStatus, Date timestamp) {
        pollStatus.setTimestamp(timestamp);
        OnmsLocationSpecificStatus statusChange = new OnmsLocationSpecificStatus();
        statusChange.setLocationMonitor(m_locationMon);
        statusChange.setMonitoredService(m_svc);
        statusChange.setPollResult(pollStatus);
        return statusChange;
    }


    private Collection<OnmsMonitoredService> getServices() {
        Collection<OnmsMonitoredService> svcs = new ArrayList<OnmsMonitoredService>();
        svcs.add(m_svc);
        return svcs;
    }
    
    @Test
    public void testGetAvailabilityStatusFlipFlop() {
        Date endTime = new Date(System.currentTimeMillis());
        Date startTime = new Date(endTime.getTime() - 100);
        TimeChunker chunker = new TimeChunker((int)(endTime.getTime() - startTime.getTime()), startTime, endTime);
        AvailCalculator calculator = new AvailCalculator(chunker);
        
        calculator.onStatusChange(createStatusChange(PollStatus.unavailable(), new Date(endTime.getTime() - 90)));
        calculator.onStatusChange(createStatusChange(PollStatus.available(), new Date(endTime.getTime() - 70)));
        calculator.onStatusChange(createStatusChange(PollStatus.unavailable(), new Date(endTime.getTime() - 50)));
        calculator.onStatusChange(createStatusChange(PollStatus.available(), new Date(endTime.getTime() - 20)));
        
        Collection<OnmsMonitoredService> svcs = getServices();
        
        
        double uptimePercent = calculator.getAvailabilityFor(svcs, 0);
        assertEquals(0.5, uptimePercent, 0.00);
    }
    
    @Test
    public void testGetAvailabilityUnavailableBefore() {
        Date endTime = new Date(System.currentTimeMillis());
        Date startTime = new Date(endTime.getTime() - 100);
        TimeChunker chunker = new TimeChunker((int)(endTime.getTime() - startTime.getTime()), startTime, endTime);
        AvailCalculator calculator = new AvailCalculator(chunker);
        
        calculator.onStatusChange(createStatusChange(PollStatus.unavailable(), new Date(endTime.getTime() - 150)));
        calculator.onStatusChange(createStatusChange(PollStatus.available(), new Date(endTime.getTime() - 50)));
        
        Collection<OnmsMonitoredService> svcs = getServices();
        
        
        double uptimePercent = calculator.getAvailabilityFor(svcs, 0);
        assertEquals(0.5, uptimePercent, 0.00);
    }
    
    @Test
    public void testNotAvailabileDuringTimeChunk() {
        Date endTime = new Date(System.currentTimeMillis());
        Date startTime = new Date(endTime.getTime() - 100);
        TimeChunker chunker = new TimeChunker((int)(endTime.getTime() - startTime.getTime()), startTime, endTime);
        AvailCalculator calculator = new AvailCalculator(chunker);
        
        calculator.onStatusChange(createStatusChange(PollStatus.unavailable(), new Date(endTime.getTime() - 150)));
        calculator.onStatusChange(createStatusChange(PollStatus.available(), new Date(endTime.getTime() + 50)));
        
        Collection<OnmsMonitoredService> svcs = getServices();
        
        
        double uptimePercent = calculator.getAvailabilityFor(svcs, 0);
        assertEquals(0.0, uptimePercent, 0.00);
    }
    
}
