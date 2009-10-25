package org.opennms.netmgt.provision;

import org.jivesoftware.smackx.workgroup.util.ModelUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.EventWrapper;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.EasyMockUtils;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.not;
import static org.easymock.EasyMock.or;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LinkAdapterEventListenerTest {
    private Event m_failedEvent = new EventBuilder(EventConstants.DATA_LINK_FAILED_EVENT_UEI, "Test").getEvent();
    private Event m_regainedEvent = new EventBuilder(EventConstants.DATA_LINK_RESTORED_EVENT_UEI, "Test").getEvent();

    public class TestLinkEventHandler implements LinkEventHandler {
        public void receiveEvent(Event e) {
            if (m_correlator.isLinkUp(e)) {
                m_eventForwarder.sendNow(new EventBuilder(m_regainedEvent).addParam("endPoint1", "pittsboro-1").addParam("endPoint2", "pittsboro-2").getEvent());
            } else {
                m_eventForwarder.sendNow(new EventBuilder(m_failedEvent).addParam("endPoint1", "pittsboro-1").addParam("endPoint2", "pittsboro-2").getEvent());
            }
        }

    }
    
    public interface LinkEventHandler {
        public void receiveEvent(Event e);
    }

    public interface EventCorrelator {
        public boolean isLinkUp(Event e);
    }
    
    TestLinkEventHandler m_linkEventHandler;
    EventCorrelator m_correlator;
    
    EasyMockUtils m_easyMock = new EasyMockUtils();
    MockEventUtil m_eventUtil = new MockEventUtil();
    private MockNetwork m_network;
    private MockNode m_node1;
    private MockNode m_node2;
    private MockEventIpcManager m_eventForwarder;
    private EventAnticipator m_anticipator;

    @Before
    public void setUp() {
        m_anticipator = new EventAnticipator();
        
        m_eventForwarder = new MockEventIpcManager();
        m_eventForwarder.setEventAnticipator(m_anticipator);
        
        m_linkEventHandler = new TestLinkEventHandler();

        m_correlator = createMock(EventCorrelator.class);
        m_network = new MockNetwork();
        m_node1 = new MockNode(m_network, 1, "pittsboro-1");
        m_node2 = new MockNode(m_network, 2, "pittsboro-2");
    }
    
    @Test
    public void testNodeDownEvent() {
        Event e = MockEventUtil.createNodeDownEvent("Test", m_node1);
        
        expect(m_correlator.isLinkUp(e)).andStubReturn(false);
        m_anticipator.anticipateEvent(m_failedEvent);
        
        replay();

        m_linkEventHandler.receiveEvent(e);
        m_eventForwarder.finishProcessingEvents();
        assertEquals(0, m_anticipator.waitForAnticipated(0).size());

        verify();
    }

    @Test
    @Ignore
    public void test2NodesDownEvent() {
        Event e1 = MockEventUtil.createNodeDownEvent("Test", m_node1);
        Event e2 = MockEventUtil.createNodeDownEvent("Test", m_node2);
        
        expect(m_correlator.isLinkUp(e1)).andStubReturn(false);
        expect(m_correlator.isLinkUp(e2)).andStubReturn(false);
        m_anticipator.anticipateEvent(m_failedEvent);
        
        replay();
        
        m_linkEventHandler.receiveEvent(e1);
        m_linkEventHandler.receiveEvent(e2);
        m_eventForwarder.finishProcessingEvents();
        assertEquals(0, m_anticipator.waitForAnticipated(0).size());
        assertEquals(1, m_anticipator.getAnticipatedEventsRecieved().size());
        
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
}
