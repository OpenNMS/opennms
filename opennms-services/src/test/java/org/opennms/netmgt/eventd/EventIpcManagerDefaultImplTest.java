package org.opennms.netmgt.eventd;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;

public class EventIpcManagerDefaultImplTest extends TestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    private EventIpcManagerDefaultImpl m_manager;
    private EventHandler m_eventHandler = m_mocks.createMock(EventHandler.class);
    private MockEventListener m_listener = new MockEventListener();
    private Throwable m_caughtThrowable = null;
    private Thread m_caughtThrowableThread = null;

    @Override
    public void setUp() throws Exception {
        m_manager = new EventIpcManagerDefaultImpl();
        m_manager.setEventHandler(m_eventHandler);
        m_manager.setHandlerPoolSize(5);
        m_manager.afterPropertiesSet();

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable throwable) {
                m_caughtThrowable = throwable;
                m_caughtThrowableThread = thread;
            }
        });
    }
    
    @Override
    public void runTest() throws Throwable {
        super.runTest();
        
        assertEquals("unprocessed received events", 0, m_listener.getEvents().size());
        
        if (m_caughtThrowable != null) {
            throw new Exception("Thread " + m_caughtThrowableThread + " threw an uncaught exception: " + m_caughtThrowable, m_caughtThrowable);
        }
    }
    
    public void testInitWithNoHandlerPoolSize() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("handlerPoolSize not set"));

        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl();
        manager.setEventHandler(m_eventHandler);
        
        try {
            manager.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testInitWithNoEventHandler() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("eventHandler not set"));

        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl();
        manager.setHandlerPoolSize(5);

        try {
            manager.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testInit() throws Exception {
        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl();
        manager.setEventHandler(m_eventHandler);
        manager.setHandlerPoolSize(5);
        manager.afterPropertiesSet();
    }
    
    public void testBroadcastWithNoListeners() throws Exception {
        Event e = new Event();

        m_mocks.replayAll();

        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
    }
    
    public void testSendNowNullEvent() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("event argument cannot be null"));

        try {
            m_manager.sendNow((Event) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testSendNowNullEventLog() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("eventLog argument cannot be null"));

        try {
            m_manager.sendNow((Log) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testAddEventListenerNullListener() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("listener argument cannot be null"));

        try {
            m_manager.addEventListener((EventListener) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testAddEventListenerAndBroadcast() throws Exception {
        Event e = new Event();

        m_mocks.replayAll();

        m_manager.addEventListener(m_listener);
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }

    public void testAddEventListenerTwoArgumentListNullListener() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("listener argument cannot be null"));

        try {
            m_manager.addEventListener((EventListener) null, new ArrayList<String>(0));
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testAddEventListenerTwoArgumentListNullUeiList() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("ueilist argument cannot be null"));

        try {
            m_manager.addEventListener(m_listener, (List<String>) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testAddEventListenerTwoArgumentStringAndBroadcast() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, e.getUei());
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    
    public void testAddEventListenerTwoArgumentStringWithUeiPartAndBroadcast() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, "uei.opennms.org/");
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    
    public void testAddEventListenerTwoArgumentStringWithUeiPartMultipleTrimAndBroadcast() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo/bar");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, "uei.opennms.org/");
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    
    public void testAddEventListenerTwoArgumentStringWithUeiPartTooLittleAndBroadcast() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, "uei.opennms.org");
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
    }
    
    public void testAddEventListenerTwoArgumentStringWithUeiPartTooMuchAndBroadcast() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, "uei.opennms.org/*");
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
    }

    public void testAddEventListenerWithUeiAndSubUeiMatchAndBroadcast() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, "uei.opennms.org/foo");
        m_manager.addEventListener(m_listener, "uei.opennms.org/");
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    
    public void testAddEventListenerTwoArgumentStringNullListener() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("listener argument cannot be null"));

        try {
            m_manager.addEventListener((EventListener) null, "");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testAddEventListenerTwoArgumentStringNullUeiList() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("uei argument cannot be null"));

        try {
            m_manager.addEventListener(m_listener, (String) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testRemoveEventListenerNullListener() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("listener argument cannot be null"));

        try {
            m_manager.removeEventListener((EventListener) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testRemoveEventListenerTwoArgumentListNullListener() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("listener argument cannot be null"));

        try {
            m_manager.removeEventListener((EventListener) null, new ArrayList<String>(0));
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testRemoveEventListenerTwoArgumentListNullUeiList() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("ueilist argument cannot be null"));

        try {
            m_manager.removeEventListener(m_listener, (List<String>) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testRemoveEventListenerTwoArgumentStringNullListener() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("listener argument cannot be null"));

        try {
            m_manager.removeEventListener((EventListener) null, "");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testRemoveEventListenerTwoArgumentStringNullUeiList() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("uei argument cannot be null"));

        try {
            m_manager.removeEventListener(m_listener, (String) null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testAddEventListenerThenAddEventListenerWithUeiAndBroadcast() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener);
        m_manager.addEventListener(m_listener, e.getUei());
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    
    public void testAddEventListenerWithUeiAndBroadcastThenAddEventListener() throws Exception {
        Event e = new Event();
        e.setUei("uei.opennms.org/foo");
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, e.getUei());
        m_manager.addEventListener(m_listener);
        m_manager.broadcastNow(e);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    
    public class MockEventListener implements EventListener {
        private List<Event> m_events = new ArrayList<Event>();
        
        public String getName() {
            return "party on, Wayne";
        }

        public void onEvent(Event e) {
            m_events.add(e);
        }
        
        public List<Event> getEvents() {
            return m_events;
        }
    }
}
