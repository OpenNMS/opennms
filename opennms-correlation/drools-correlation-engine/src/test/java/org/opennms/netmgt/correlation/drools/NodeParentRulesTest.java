package org.opennms.netmgt.correlation.drools;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.expect;

import org.easymock.EasyMock;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.netmgt.xml.event.Event;


public class NodeParentRulesTest extends CorrelationRulesTestCase {
    
    List<Object> mocks = new ArrayList<Object>();
    
    public <T> T createMock(Class<T> mockClass) {
        T mock = EasyMock.createMock(mockClass);
        mocks.add(mock);
        return mock;
    }
    
    public void replay() {
        EasyMock.replay(mocks.toArray());
    }
    
    public void verify(DroolsCorrelationEngine engine) {
        EasyMock.verify(mocks.toArray());
        EasyMock.reset(mocks.toArray());
        super.verify(engine);
    }
    
    public void testParentNodeDown() throws Exception {
        
        //anticipate(createRootCauseEvent(1, 1));
        
        NodeService nodeService = createMock(NodeService.class);
        
        expect(nodeService.getParentNode(1L)).andReturn(null);
        
        replay();
        
        DroolsCorrelationEngine engine = findEngineByName("nodeParentRules");
        engine.setGlobal("nodeService", nodeService);
        
        engine.correlate(createNodeDownEvent(1));

        // event + root cause
        m_anticipatedMemorySize = 2;
        
        verify(engine);
        
        anticipate(createRootCauseResolvedEvent(1, 1));
        
    }
    
    private Event createRootCauseResolvedEvent(int symptom, int cause) {
        return new EventBuilder(createNodeEvent("rootCauseResolved", cause)).getEvent();

    }

    private Event createRootCauseEvent(int symptom, int cause) {
        return new EventBuilder(createNodeEvent("rootCauseEvent", cause)).getEvent();
    }


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