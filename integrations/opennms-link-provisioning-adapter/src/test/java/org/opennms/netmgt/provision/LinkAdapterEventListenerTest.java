package org.opennms.netmgt.provision;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.EasyMockUtils;

public class LinkAdapterEventListenerTest {
    

    private Event m_failedEvent = new EventBuilder(EventConstants.DATA_LINK_FAILED_EVENT_UEI, "Test").getEvent();
    private Event m_regainedEvent = new EventBuilder(EventConstants.DATA_LINK_RESTORED_EVENT_UEI, "Test").getEvent();

    public class TestLinkEventHandler implements LinkEventHandler {
        public void receiveEvent(Event e) {
//            if (m_correlator.isLinkUp(e)) {
//                getEventForwarder().sendNow(new EventBuilder(m_regainedEvent).addParam("endPoint1", "pittsboro-1").addParam("endPoint2", "pittsboro-2").getEvent());
//            } else {
//                getEventForwarder().sendNow(new EventBuilder(m_failedEvent).addParam("endPoint1", "pittsboro-1").addParam("endPoint2", "pittsboro-2").getEvent());
//            }
            m_correlator.updateLinkStatus(e);
        }

    }
    
    public interface State {
        public static final State UP_STATE = new Up();
        public static final State BOTH_DOWN_STATE = new BothDown();
        public static final State END_POINT_1_DOWN_STATE = new EndPoint1Down();
        public static final State END_POINT_2_DOWN_STATE = new EndPoint2Down();
        public State endPoint1Down();
        public State endPoint2Down();
        public State endPoint1Up();
        public State endPoint2Up();
    }
    
    public static abstract class AbstractState implements State {
        
        public static final String DATALINK_FAILED_EVENT = "datalinkFailed";
        public static final String DATALINK_RESTORE_EVENT = "datalinkRestored";
        
        public State endPoint1Down() {
            return this;
        }

        public State endPoint1Up() {
            return this;
        }

        public State endPoint2Down() {
            return this;
        }

        public State endPoint2Up() {
            return this;
        }
        
        protected void sendEvent(String event) {
            if(event.equalsIgnoreCase(DATALINK_FAILED_EVENT)) {
                
            }else if(event.equalsIgnoreCase(DATALINK_RESTORE_EVENT)) {
                
            }
        }

    }
    
    public static class BothDown extends AbstractState{

        @Override
        public State endPoint1Up() {
            return State.END_POINT_2_DOWN_STATE;
        }

        @Override
        public State endPoint2Up() {
            return State.END_POINT_1_DOWN_STATE;
        }
        
    }
    
    public static class EndPoint1Down extends AbstractState{

        @Override
        public State endPoint1Up() {
            sendEvent("datalinkRestored");
            return State.UP_STATE;
        }

        @Override
        public State endPoint2Down() {
            return State.BOTH_DOWN_STATE;
        }
        
    }
    
    public static class EndPoint2Down extends AbstractState{

        @Override
        public State endPoint1Down() {
            return State.BOTH_DOWN_STATE;
        }

        @Override
        public State endPoint2Up() {
            return State.UP_STATE;
        }
        //TODO: Store initial state by scanning.
        
    }
    
    public static class Up extends AbstractState {
        
        public State endPoint1Down() {
            sendEvent("datalinkFailed");
            return State.END_POINT_1_DOWN_STATE;
        }

        public State endPoint2Down() {
            sendEvent("datalinkFailed");
            return State.END_POINT_2_DOWN_STATE;
        }
        
    }
    
    
    public interface LinkEventHandler {
        public void receiveEvent(Event e);
    }

    public interface EventCorrelator {
        public boolean isLinkUp(Event e);
        public void updateLinkStatus(Event e);
    }
    
    public class DefaultEventCorrelator implements EventCorrelator{
        
        public boolean isLinkUp(Event e) {
            return false;
        }

        public void updateLinkStatus(Event e) {
            
        }
        
    }
    
    public class LinkStatus{
        private int m_datalinkId;
        private String m_endPoint1;
        private String m_endPoint2;
        private State m_state;
    }
        
    TestLinkEventHandler m_linkEventHandler;
    EventCorrelator m_correlator;
    
    EasyMockUtils m_easyMock = new EasyMockUtils();
    MockEventUtil m_eventUtil = new MockEventUtil();
    private MockNetwork m_network;
    private MockNode m_node1;
    private MockNode m_node2;
    private MockEventIpcManager m_eventIpcManager;
    private EventAnticipator m_anticipator;

    @Before
    public void setUp() {
        
        m_eventIpcManager = new MockEventIpcManager();
        m_anticipator = m_eventIpcManager.getEventAnticipator();
        
        m_linkEventHandler = new TestLinkEventHandler();

        m_correlator = createMock(EventCorrelator.class);
        m_network = new MockNetwork();
        m_node1 = new MockNode(m_network, 1, "pittsboro-1");
        m_node2 = new MockNode(m_network, 2, "pittsboro-2");
    }
    
    @Test
    public void testNodeDownEvent() {
        Event e = m_node1.createDownEvent();
        
        m_correlator.updateLinkStatus(e);
        
        m_anticipator.anticipateEvent(m_failedEvent);
        
        replay();

        m_linkEventHandler.receiveEvent(e);

        //verify that the event was successful 
        m_eventIpcManager.finishProcessingEvents();
        assertEquals(0, m_anticipator.waitForAnticipated(0).size());
        assertEquals(0, m_anticipator.unanticipatedEvents().size());

        verify();
    }

    @Test
    @Ignore
    public void test2NodesDownEvent() {
        Event e1 = m_node1.createDownEvent();
        Event e2 = m_node2.createDownEvent();
        
        expect(m_correlator.isLinkUp(e1)).andStubReturn(false);
        expect(m_correlator.isLinkUp(e2)).andStubReturn(false);
        m_anticipator.anticipateEvent(m_failedEvent);
        
        replay();
        
        m_linkEventHandler.receiveEvent(e1);
        m_linkEventHandler.receiveEvent(e2);
        m_eventIpcManager.finishProcessingEvents();
        assertEquals(0, m_anticipator.waitForAnticipated(0).size());
        assertEquals(0, m_anticipator.unanticipatedEvents().size());
        
        verify();
    }

    public <T> T createMock(Class<T> clazz){
        return m_easyMock.createMock(clazz);
    }
    
    public void verify(){
        m_easyMock.verifyAll();
    }
    
    public void replay(){
        m_easyMock.replayAll();
    }

    private EventForwarder getEventForwarder() {
        return m_eventIpcManager;
    }
}
