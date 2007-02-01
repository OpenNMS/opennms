package org.opennms.netmgt.correlation.drools;

import junit.framework.TestCase;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

public class DroolsCorrelationEngineTest extends TestCase {
    
    private static final String WS_OUTAGE_UEI = "uei.opennms.org/correlation/locationMonitors/wideSpreadOutage";
    private static final String WS_RESOLVED_UEI = "uei.opennms.org/correlation/locationMonitors/wideSpreadOutageResolved";
    private static final String ISO_OUTAGE_UEI = "uei.opennms.org/correlation/locationMonitors/isolatedOutage";
    private static final String ISO_RESOLVED_UEI = "uei.opennms.org/correlation/locationMonitors/isolatedOutageResolved";
    
    private MockEventIpcManager m_eventIpcMgr;
	private EventAnticipator m_anticipator;
	private DroolsCorrelationEngine m_engine;      
	private Integer m_anticipatedMemorySize = 0;

    public DroolsCorrelationEngineTest() {
        System.setProperty("opennms.home", "src/test/opennms-home");
        
        m_eventIpcMgr = new MockEventIpcManager();
        EventIpcManagerFactory.setIpcManager(m_eventIpcMgr);
    }
    

    
    @Override
	protected void setUp() throws Exception {
		super.setUp();
		
    	m_anticipator = m_eventIpcMgr.getEventAnticipator();
        
        m_engine = new DroolsCorrelationEngine();
		m_engine.setEventIpcManager(m_eventIpcMgr);
        m_engine.afterPropertiesSet();
		
	}



    @Override
	protected void runTest() throws Throwable {
		super.runTest();
		verify();
	}

	public void testMultipleLocationMonitorOutage() throws Exception {

		anticipateWideSpreadOutageEvent();
		
        // received outage events for all monitors
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 7));
        
        Thread.sleep(5000);
        
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 8));
        
        // expect memory to contain only the single 'affliction' for this service
        m_anticipatedMemorySize = 1;
        
        verify();
        
        anticipateWideSpreadOutageResolvedEvent();
        
        // received outage events for all monitors
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 7));
        
        Thread.sleep(5000);
        
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 8));
        
        m_anticipatedMemorySize = 0;
        
        verify();
        
        
    }
    
    
	
    public void testSingleLocationMonitorOutage() throws Exception {
        
        anticipateIsolatedOutageEvent();
        
    	// recieve outage event for only a single monitor
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 7));
        
        Thread.sleep(31000);
    	
        // expect memory to contain only the single 'afflication' for htis service
        m_anticipatedMemorySize = 1;
        
        verify();
        
        anticipateIsolatedOutageResolvedEvent();
        
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 7));
        
        m_anticipatedMemorySize = 0;
        
        verify();
    }
    
    public void testOutageEscalation() {
        // we detect an isolated outage from a single monitor
        // anticipateIsolated(mon 7)
        
        // sometime later the outage is detected from additional monitors (becomes wide spread)
        // anticipateWideSpread
        
        // 
    }
    
    public void testFlappingMonitor() {
    	// receive three non alertable outages in 10 minutes
    	
    	// send a flapping event
    }

	private void anticipateWideSpreadOutageEvent() {
		EventBuilder bldr = new EventBuilder(WS_OUTAGE_UEI, "Drools");
		bldr.setNodeid(1)
			.setInterface("192.168.1.1")
			.setService("HTTP");
		
		m_anticipator.anticipateEvent(bldr.getEvent());
	}

    private void anticipateWideSpreadOutageResolvedEvent() {
        EventBuilder bldr = new EventBuilder(WS_RESOLVED_UEI, "Drools");
        bldr.setNodeid(1)
            .setInterface("192.168.1.1")
            .setService("HTTP");
        
        m_anticipator.anticipateEvent(bldr.getEvent());
    }

    private void anticipateIsolatedOutageResolvedEvent() {
        EventBuilder bldr = new EventBuilder(ISO_RESOLVED_UEI, "Drools");
        bldr.setNodeid(1)
            .setInterface("192.168.1.1")
            .setService("HTTP");
        
        m_anticipator.anticipateEvent(bldr.getEvent());
    }

    private void anticipateIsolatedOutageEvent() {
        EventBuilder bldr = new EventBuilder(ISO_OUTAGE_UEI, "Drools");
        bldr.setNodeid(1)
            .setInterface("192.168.1.1")
            .setService("HTTP");
        
        m_anticipator.anticipateEvent(bldr.getEvent());
    }



	private void verify() {
		m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
		assertEquals("Unexpected number of objects in working memory", m_anticipatedMemorySize.intValue(), m_engine.getMemorySize());
	}
    

	private Event createRemoteNodeLostServiceEvent(int nodeId, String ipAddr, String svcName, int locationMonitor) {
		return createEvent(EventConstants.REMOTE_NODE_LOST_SERVICE_UEI, nodeId, ipAddr, svcName, locationMonitor);
	}
	
	private Event createRemoteNodeRegainedServiceEvent(int nodeId, String ipAddr, String svcName, int locationMonitor) {
		return createEvent(EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI, nodeId, ipAddr, svcName, locationMonitor);
	}
	

	private Event createEvent(String uei, int nodeId, String ipAddr, String svcName, int locationMonitor) {
		EventBuilder bldr = new EventBuilder(uei, "test");
        bldr.setNodeid(nodeId)
        	.setInterface(ipAddr)
        	.setService(svcName)
        	.addParam(EventConstants.PARM_LOCATION_MONITOR_ID, locationMonitor);
        
        Event event = bldr.getEvent();
		return event;
	}
    

}
