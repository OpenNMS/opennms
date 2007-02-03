package org.opennms.netmgt.correlation.drools;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.util.StringUtils;

public class DroolsCorrelationEngineTest extends AbstractDependencyInjectionSpringContextTests {
    
    private static final String WS_OUTAGE_UEI = "uei.opennms.org/correlation/remote/wideSpreadOutage";
    private static final String WS_RESOLVED_UEI = "uei.opennms.org/correlation/remote/wideSpreadOutageResolved";
    private static final String SERVICE_FLAPPING_UEI = "uei.opennms.org/correlation/serviceFlapping";
    
    private MockEventIpcManager m_eventIpcMgr;
	private EventAnticipator m_anticipator;
	private DroolsCorrelationEngine m_engine;      
	private Integer m_anticipatedMemorySize = 0;


    public DroolsCorrelationEngineTest() {
        System.setProperty("opennms.home", "src/test/opennms-home");
        
    }
    
    @Override
    protected ConfigurableApplicationContext loadContextLocations(String[] locations) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Loading context for: " + StringUtils.arrayToCommaDelimitedString(locations));
        }
        return new FileSystemXmlApplicationContext(locations);
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:test-context.xml",
                "classpath:META-INF/opennms/correlation-engine.xml"
        };
    }

    
    public void setEventIpcMgr(MockEventIpcManager eventIpcManager) {
        m_eventIpcMgr = eventIpcManager;
        m_anticipator = m_eventIpcMgr.getEventAnticipator();
    }
    
	public void testWideSpreadLocationMonitorOutage() throws Exception {

		anticipateWideSpreadOutageEvent();
		
        // received outage events for all monitors
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 7));
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 8));
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 9));

        
        // expect memory to contain only the single 'affliction' for this service
        // and the flap tracker for each monitor
        m_anticipatedMemorySize = 4;
        
        verify();
        
        anticipateWideSpreadOutageResolvedEvent();
        
        // received outage events for all monitors
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 7));
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 9));
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 8));
        
        // expect the flap tracker to remain
        m_anticipatedMemorySize = 6;
        

        verify();
        
        // need to time the flap trackers out
        Thread.sleep(1100);

        m_anticipatedMemorySize = 0;
        
        verify();
        
    }
    
    
	
    public void testSingleLocationMonitorOutage() throws Exception {
        
        
    	// recieve outage event for only a single monitor
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 7));
        
        // expect memory to contain only the single 'afflication' for htis service
        m_anticipatedMemorySize = 2;
        
        verify();
        
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 7));
        
        m_anticipatedMemorySize = 2;
        
        verify();
        
        // let flaps time otu
        Thread.sleep(1100);
        
        m_anticipatedMemorySize = 0;
        
        verify();
    }
    
    
    public void testDoubleLocationMonitorOutage() throws Exception {
        
        // recieve outage event for only a single monitor
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 7));
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 8));
        
        // expect memory to contain only the single 'afflication' for this service
        m_anticipatedMemorySize = 3;
        
        verify();
        
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 7));
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 8));
        
        m_anticipatedMemorySize = 4;
        
        verify();
        
        Thread.sleep(1100);

        m_anticipatedMemorySize = 0;
        
        verify();
    }
    
    public void testFlappingMonitor() throws Exception {
        
        anticipateServiceFlappingEvent();
        
        // recieve outage event for only a single monitor
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 7));
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 7));
        
        Thread.sleep(100);
        
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 7));
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 7));
        
        Thread.sleep(100);

        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 7));
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 7));
        
        m_anticipatedMemorySize = 4;
        
        verify();
        
        Thread.sleep(850);

        m_anticipatedMemorySize = 3;
        
        verify();
        
        Thread.sleep(150);
        
        m_anticipatedMemorySize = 0;
        
        verify();
        
        anticipateServiceFlappingEvent();
        
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 7));
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 7));
        
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 7));
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 7));
        
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 7));
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 7));
        
        m_anticipatedMemorySize = 4;
        
        verify();
        
        Thread.sleep(1100);
        
        m_anticipatedMemorySize = 0;
        
        verify();
        

    }
    
    public void testDontFlapWhenOnlyTwoOutages() throws Exception {
        
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "AVAIL", 7));
        
        Thread.sleep(50);
        
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 7));

        Thread.sleep(50);
        
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "AVAIL", 7));
        
        Thread.sleep(50);
        
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 7));
        
        Thread.sleep(50);
        
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "AVAIL", 7));
        
        Thread.sleep(50);
        
        m_engine.correlate(createRemoteNodeLostServiceEvent(1, "192.168.1.1", "HTTP", 7));

        Thread.sleep(50);
        
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "AVAIL", 7));
        
        Thread.sleep(50);
        
        m_engine.correlate(createRemoteNodeRegainedServiceEvent(1, "192.168.1.1", "HTTP", 7));
        
        m_anticipatedMemorySize = 6;
        
        Thread.sleep(100);
        
        verify();
        
        Thread.sleep(1000);
        
        m_anticipatedMemorySize = 0;
        
        verify();

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

    private void anticipateServiceFlappingEvent() {
        EventBuilder bldr = new EventBuilder(SERVICE_FLAPPING_UEI, "Drools");
        bldr.setNodeid(1)
            .setInterface("192.168.1.1")
            .setService("HTTP");
        
        m_anticipator.anticipateEvent(bldr.getEvent());
    }



	private void verify() {
		m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
        if (m_anticipatedMemorySize != null) {
            assertEquals("Unexpected number of objects in working memory: "+m_engine.getMemoryObjects(), m_anticipatedMemorySize.intValue(), m_engine.getMemorySize());
        }
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

    public void setEngine(CorrelationEngine[] engines) {
        m_engine = (DroolsCorrelationEngine) engines[0];
    }

}