package org.opennms.netmgt.correlation.drools;

import static org.easymock.EasyMock.expect;

import org.junit.Test;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.EasyMockUtils;


public class NodeParentRulesTest extends CorrelationRulesTestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    
    @Test
    public void testParentNodeDown() throws Exception {
        
        //anticipate(createRootCauseEvent(1, 1));
        
        NodeService nodeService = m_mocks.createMock(NodeService.class);
        
        expect(nodeService.getParentNode(1L)).andReturn(null).atLeastOnce();
        
        m_mocks.replayAll();
        
        DroolsCorrelationEngine engine = findEngineByName("nodeParentRules");
        engine.setGlobal("nodeService", nodeService);
        
        engine.correlate(createNodeDownEvent(1));

        // event + root cause
        m_anticipatedMemorySize = 2;
        
        m_mocks.verifyAll();
        verify(engine);
        
        anticipate(createRootCauseResolvedEvent(1, 1));
        
    }
    
    private Event createRootCauseResolvedEvent(int symptom, int cause) {
        return new EventBuilder(createNodeEvent("rootCauseResolved", cause)).getEvent();

    }

    // Currently unused
//    private Event createRootCauseEvent(int symptom, int cause) {
//        return new EventBuilder(createNodeEvent("rootCauseEvent", cause)).getEvent();
//    }


    public Event createNodeDownEvent(int nodeid) {
        return createNodeEvent(EventConstants.NODE_DOWN_EVENT_UEI, nodeid);
    }
    
    public Event createNodeUpEvent(int nodeid) {
        return createNodeEvent(EventConstants.NODE_UP_EVENT_UEI, nodeid);
    }

    private Event createNodeEvent(String uei, int nodeid) {
        return new EventBuilder(uei, "test")
            .setNodeid(nodeid)
            .getEvent();
    }
    



}