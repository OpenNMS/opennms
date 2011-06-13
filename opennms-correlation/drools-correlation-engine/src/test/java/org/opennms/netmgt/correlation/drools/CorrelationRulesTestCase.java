package org.opennms.netmgt.correlation.drools;

import static org.junit.Assert.assertEquals;
import static org.opennms.core.utils.InetAddressUtils.addr;

import org.junit.runner.RunWith;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.correlation.CorrelationEngineRegistrar;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.ConfigurationTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:META-INF/opennms/applicationContext-daemon.xml",
        "classpath:META-INF/opennms/mockEventIpcManager.xml",
        "classpath:test-context.xml",
        "classpath:META-INF/opennms/correlation-engine.xml"
})
public class CorrelationRulesTestCase {

    @Autowired
    private MockEventIpcManager m_eventIpcMgr;
    protected Integer m_anticipatedMemorySize = 0;
    
    @Autowired
    private CorrelationEngineRegistrar m_correlator;

    protected CorrelationRulesTestCase() {
        ConfigurationTestUtils.setRelativeHomeDirectory("src/test/opennms-home");
    }

    public void setCorrelationEngineRegistrar(CorrelationEngineRegistrar correlator) {
        m_correlator = correlator;
    }

    protected void verify(DroolsCorrelationEngine engine) {
    	getAnticipator().verifyAnticipated(0, 0, 0, 0, 0);
        if (m_anticipatedMemorySize != null) {
            assertEquals("Unexpected number of objects in working memory: "+engine.getMemoryObjects(), m_anticipatedMemorySize.intValue(), engine.getMemorySize());
        }
    }

    protected DroolsCorrelationEngine findEngineByName(String engineName) {
        return (DroolsCorrelationEngine) m_correlator.findEngineByName(engineName);
    }

    protected void anticipate(Event event) {
        getAnticipator().anticipateEvent(event);
    }

    protected Event createRemoteNodeLostServiceEvent(int nodeId, String ipAddr, String svcName, int locationMonitor) {
    	return createEvent(EventConstants.REMOTE_NODE_LOST_SERVICE_UEI, nodeId, ipAddr, svcName, locationMonitor);
    }

    protected Event createRemoteNodeRegainedServiceEvent(int nodeId, String ipAddr, String svcName, int locationMonitor) {
    	return createEvent(EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI, nodeId, ipAddr, svcName, locationMonitor);
    }

    protected Event createEvent(String uei, int nodeId, String ipAddr, String svcName, int locationMonitor) {
    	return new EventBuilder(uei, "test")
        .setNodeid(nodeId).setInterface(addr(ipAddr))
        	.setService(svcName)
        	.addParam(EventConstants.PARM_LOCATION_MONITOR_ID, locationMonitor)
            .getEvent();
    }

    protected Event createServiceEvent(String uei, int nodeId, String ipAddr, String svcName) {
        return new EventBuilder(uei, "test")
        .setNodeid(nodeId).setInterface(addr("192.168.1.1"))
            .setService("HTTP")
            .getEvent();
    }

    /**
     * @return the anticipator
     */
    protected EventAnticipator getAnticipator() {
        return m_eventIpcMgr.getEventAnticipator();
    }

}
