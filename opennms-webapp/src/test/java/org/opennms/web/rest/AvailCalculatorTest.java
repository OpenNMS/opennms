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
import org.opennms.netmgt.model.PollStatus;
import org.opennms.web.rest.support.TimeChunker;
import org.opennms.web.rest.support.TimeChunker.TimeChunk;


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
        
        
        TimeChunk chunk = chunker.getNextSegment();
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
