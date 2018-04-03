/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventHandler;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.ThreadAwareEventListener;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.test.ThreadLocker;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;

import junit.framework.TestCase;

/**
 * @author Seth
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class EventIpcManagerDefaultImplTest extends TestCase {

    private static final Logger LOG = LoggerFactory.getLogger(EventIpcManagerDefaultImplTest.class);

    private static final int SLOW_EVENT_OPERATION_DELAY = 200;

    private EasyMockUtils m_mocks = new EasyMockUtils();
    private EventIpcManagerDefaultImpl m_manager;
    private EventHandler m_eventHandler = m_mocks.createMock(EventHandler.class);
    private MockEventListener m_listener = new MockEventListener();
    private Throwable m_caughtThrowable = null;
    private Thread m_caughtThrowableThread = null;
    private MetricRegistry m_registry = new MetricRegistry();

    @Override
    public void setUp() throws Exception {
        m_manager = new EventIpcManagerDefaultImpl(m_registry);
        m_manager.setEventHandler(m_eventHandler);
        m_manager.setHandlerPoolSize(5);
        m_manager.afterPropertiesSet();

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
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

        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl(m_registry);
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

        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl(m_registry);
        manager.setHandlerPoolSize(5);

        try {
            manager.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testInit() throws Exception {
        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl(m_registry);
        manager.setEventHandler(m_eventHandler);
        manager.setHandlerPoolSize(5);
        manager.afterPropertiesSet();
    }
    
    public void testBroadcastWithNoListeners() throws Exception {
        EventBuilder bldr = new EventBuilder(null, "testBroadcastWithNoListeners");

        m_mocks.replayAll();

        m_manager.broadcastNow(bldr.getEvent(), false);
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
        EventBuilder bldr = new EventBuilder(null, "testAddEventListenerAndBroadcast");
        Event event = bldr.getEvent();

        m_mocks.replayAll();

        m_manager.addEventListener(m_listener);
        m_manager.broadcastNow(event, false);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(event));
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
        EventBuilder bldr = new EventBuilder("uei.opennms.org/foo", "testAddEventListenerTwoArgumentStringAndBroadcast");
        Event e = bldr.getEvent();
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, e.getUei());
        m_manager.broadcastNow(e, false);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    
    public void testAddEventListenerTwoArgumentStringWithUeiPartAndBroadcast() throws Exception {
        EventBuilder bldr = new EventBuilder("uei.opennms.org/foo", "testAddEventListenerTwoArgumentStringWithUeiPartAndBroadcast");
        Event e = bldr.getEvent();

        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, "uei.opennms.org/");
        m_manager.broadcastNow(e, false);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    
    public void testAddEventListenerTwoArgumentStringWithUeiPartMultipleTrimAndBroadcast() throws Exception {
        EventBuilder bldr = new EventBuilder("uei.opennms.org/foo", "testAddEventListenerTwoArgumentStringWithUeiPartMultipleTrimAndBroadcast");
        Event e = bldr.getEvent();
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, "uei.opennms.org/");
        m_manager.broadcastNow(e, false);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    
    public void testAddEventListenerTwoArgumentStringWithUeiPartTooLittleAndBroadcast() throws Exception {
        EventBuilder bldr = new EventBuilder("uei.opennms.org/foo", "testAddEventListenerTwoArgumentStringWithUeiPartTooLittleAndBroadcast");
        Event e = bldr.getEvent();
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, "uei.opennms.org");
        m_manager.broadcastNow(e, false);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
    }
    
    public void testAddEventListenerTwoArgumentStringWithUeiPartTooMuchAndBroadcast() throws Exception {
        EventBuilder bldr = new EventBuilder("uei.opennms.org/foo", "testAddEventListenerTwoArgumentStringWithUeiPartTooMuchAndBroadcast");
        Event e = bldr.getEvent();
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, "uei.opennms.org/*");
        m_manager.broadcastNow(e, false);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
    }

    public void testAddEventListenerWithUeiAndSubUeiMatchAndBroadcast() throws Exception {
        EventBuilder bldr = new EventBuilder("uei.opennms.org/foo", "testAddEventListenerWithUeiAndSubUeiMatchAndBroadcast");
        Event e = bldr.getEvent();
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, "uei.opennms.org/foo");
        m_manager.addEventListener(m_listener, "uei.opennms.org/");
        m_manager.broadcastNow(e, false);
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
        EventBuilder bldr = new EventBuilder("uei.opennms.org/foo", "testAddEventListenerThenAddEventListenerWithUeiAndBroadcast");
        Event e = bldr.getEvent();
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener);
        m_manager.addEventListener(m_listener, e.getUei());
        m_manager.broadcastNow(e, false);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    
    public void testAddEventListenerWithUeiAndBroadcastThenAddEventListener() throws Exception {
        EventBuilder bldr = new EventBuilder("uei.opennms.org/foo", "testAddEventListenerWithUeiAndBroadcastThenAddEventListener");
        Event e = bldr.getEvent();
        
        m_mocks.replayAll();

        m_manager.addEventListener(m_listener, e.getUei());
        m_manager.addEventListener(m_listener);
        m_manager.broadcastNow(e, false);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
        
        assertTrue("could not remove broadcasted event--did it make it?", m_listener.getEvents().remove(e));
    }
    

    /**
     * This is the type of exception we want to catch.
     * 
     * 2006-05-28 18:30:12,532 WARN  [EventHandlerPool-fiber0] OpenNMS.Xmlrpcd.org.opennms.netmgt.eventd.EventHandler: Unknown exception processing event
     * java.lang.NullPointerException
     *    at java.text.SimpleDateFormat.parse(SimpleDateFormat.java:1076)
     *    at java.text.DateFormat.parse(DateFormat.java:333)
     *    at org.opennms.netmgt.EventConstants.parseToDate(EventConstants.java:744)
     *    at org.opennms.netmgt.eventd.Persist.getEventTime(Persist.java:801)
     *    at org.opennms.netmgt.eventd.Persist.insertEvent(Persist.java:581)
     *    at org.opennms.netmgt.eventd.EventWriter.persistEvent(EventWriter.java:131)
     *    at org.opennms.netmgt.eventd.EventHandler.run(EventHandler.java:154)
     *    at org.opennms.core.concurrent.RunnableConsumerThreadPool$FiberThreadImpl.run(RunnableConsumerThreadPool.java:412)
     *    at java.lang.Thread.run(Thread.java:613)
     */
    public void testNoDateDate() throws InterruptedException {
        EventBuilder bldr = new EventBuilder(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, "the one true event source");
        bldr.setNodeid(1);
        bldr.setInterface(addr("192.168.1.1"));
        bldr.setService("ICMP");
        Event e = bldr.getEvent();
        m_mocks.replayAll();

        m_manager.broadcastNow(e, false);
        Thread.sleep(100);
        
        m_mocks.verifyAll();
    }

    private static class ThreadRecordingEventHandler implements EventHandler {
        private final AtomicReference<Long> lastThreadId = new AtomicReference<>();
        private CountDownLatch latch;

        /**
         * This {@link EventHandler} is always synchronous so this method just 
         * delegates to {@link #createRunnable(Log)}.
         */
        @Override
        public Runnable createRunnable(Log eventLog, boolean synchronous) {
            return createRunnable(eventLog);
        }

        @Override
        public Runnable createRunnable(Log eventLog) {
            latch = new CountDownLatch(1);
            return new Runnable() {
                @Override
                public void run() {
                    lastThreadId.set(Thread.currentThread().getId());
                    latch.countDown();
                }
            };
        }

        public long getThreadId() {
            return lastThreadId.get();
        }

        public void waitForEvent() throws InterruptedException {
            latch.await();
        }
    }

    public void testAsyncVsSyncSendNow() throws InterruptedException {
        ThreadRecordingEventHandler threadRecordingEventHandler = new ThreadRecordingEventHandler();
        m_manager.setEventHandler(threadRecordingEventHandler);

        EventBuilder bldr = new EventBuilder("uei.opennms.org/foo", "testAsyncVsSyncSendNow");
        Event e = bldr.getEvent();

        // Async: When invoking sendNow, the Runnable should be ran from thread other than the callers
        m_manager.sendNow(e);
        threadRecordingEventHandler.waitForEvent();
        assertNotEquals(Thread.currentThread().getId(), threadRecordingEventHandler.getThreadId());

        // Sync: When invoking sendNowSync, the Runnable should be ran from the callers thread
        m_manager.sendNowSync(e);
        assertEquals(Thread.currentThread().getId(), threadRecordingEventHandler.getThreadId());
    }

    public void testSlowEventHandlerCausesDiscards() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        EventHandler handler = new EventHandler() {
            @Override
            public Runnable createRunnable(Log eventLog, boolean synchronous) {
                return createRunnable(eventLog);
            }

            @Override
            public Runnable createRunnable(Log eventLog) {
                return new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(SLOW_EVENT_OPERATION_DELAY);
                        } catch (InterruptedException e) {
                        }
                        counter.incrementAndGet();
                    }
                };
            }
        };

        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl(m_registry);
        manager.setEventHandler(handler);
        manager.setHandlerPoolSize(1);
        manager.setHandlerQueueLength(5);
        manager.afterPropertiesSet();

        // Send 10 events. The first one will be executed on the handler thread,
        // the next 5 will be enqueued, then the last 4 will be discarded because
        // the queue is full. After the time has expired, the first 6 events will 
        // have passed through the handler.
        //
        for (int i = 0; i < 10; i++) {
            EventBuilder bldr = new EventBuilder("uei.opennms.org/foo/" + i, "testDiscardWhenFullWithSlowEventListener");
            Event event = bldr.getEvent();
            try {
                manager.sendNow(event);
            } catch (RejectedExecutionException e) {
                rejected.incrementAndGet();
            }
        }

        await().pollInterval(1, TimeUnit.SECONDS).untilAtomic(counter, is(equalTo(6)));
        await().pollInterval(1, TimeUnit.SECONDS).untilAtomic(rejected, is(equalTo(4)));
    }

    public void testSlowEventListener() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();

        EventListener slowListener = new EventListener() {
            @Override
            public String getName() {
                return "testSlowEventListener";
            }

            @Override
            public void onEvent(Event event) {
                LOG.info("Hello, here is event: " + event.getUei());
                try {
                    Thread.sleep(SLOW_EVENT_OPERATION_DELAY);
                } catch (InterruptedException e) {
                }
                counter.incrementAndGet();
            }
        };

        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl(m_registry);
        manager.setHandlerPoolSize(1);
        manager.setHandlerQueueLength(5);
        DefaultEventHandlerImpl handler = new DefaultEventHandlerImpl(m_registry);
        manager.setEventHandler(handler);
        manager.afterPropertiesSet();

        manager.addEventListener(slowListener);

        // Send 10 events. The first one will be executed on the listener thread,
        // the next 5 will be enqueued, then the last 4 will be discarded because
        // the queue is full. After the time has expired, the first 6 events will 
        // have passed through the listener.
        //
        for (int i = 0; i < 10; i++) {
            EventBuilder bldr = new EventBuilder("uei.opennms.org/foo/" + i, "testSlowEventListener");
            Event e = bldr.getEvent();
            manager.broadcastNow(e, false);
        }

        await().pollInterval(1, TimeUnit.SECONDS).untilAtomic(counter, is(equalTo(6)));
    }

    /**
     * This test creates two event listeners that both create events as they
     * handle events. This test can be used to detect deadlocks between the
     * listeners.
     * 
     * @throws InterruptedException
     */
    public void testRecursiveEvents() throws InterruptedException {
        final int numberOfEvents = 20;
        CountDownLatch fooCounter = new CountDownLatch(numberOfEvents);
        CountDownLatch barCounter = new CountDownLatch(numberOfEvents);
        CountDownLatch kiwiCounter = new CountDownLatch(numberOfEvents);
        CountDownLatch ulfCounter = new CountDownLatch(numberOfEvents);

        final EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl(m_registry);
        manager.setHandlerPoolSize(1);
        //manager.setHandlerQueueLength(5);
        DefaultEventHandlerImpl handler = new DefaultEventHandlerImpl(m_registry);
        manager.setEventHandler(handler);
        manager.afterPropertiesSet();

        EventListener slowFooBarListener = new EventListener() {
            @Override
            public String getName() {
                return "slowFooBarListener";
            }

            @Override
            public void onEvent(Event event) {
                if ("uei.opennms.org/foo".equals(event.getUei())) {
                    EventBuilder bldr = new EventBuilder("uei.opennms.org/bar", "testRecursiveEvents");
                    Event e = bldr.getEvent();
                    manager.broadcastNow(e, false);
                    fooCounter.countDown();
                } else {
                    try {
                        Thread.sleep(SLOW_EVENT_OPERATION_DELAY);
                    } catch (InterruptedException e) {
                    }
                    barCounter.countDown();
                }
            }
        };

        EventListener slowKiwiUlfListener = new EventListener() {
            @Override
            public String getName() {
                return "slowKiwiUlfListener";
            }

            @Override
            public void onEvent(Event event) {
                if ("uei.opennms.org/foo".equals(event.getUei())) {
                    EventBuilder bldr = new EventBuilder("uei.opennms.org/ulf", "testRecursiveEvents");
                    Event e = bldr.getEvent();
                    manager.broadcastNow(e, false);
                    kiwiCounter.countDown();
                } else {
                    try {
                        Thread.sleep(SLOW_EVENT_OPERATION_DELAY);
                    } catch (InterruptedException e) {
                    }
                    ulfCounter.countDown();
                }
            }
        };

        manager.addEventListener(slowFooBarListener);
        manager.addEventListener(slowKiwiUlfListener);

        // Send ${numberOfEvents} "foo" events. This will trigger a cascade of "bar"
        // and "ulf" events.
        //
        for (int i = 0; i < numberOfEvents; i++) {
            EventBuilder bldr = new EventBuilder("uei.opennms.org/foo", "testRecursiveEvents");
            Event e = bldr.getEvent();
            manager.broadcastNow(e, false);
        }

        assertTrue("foo counter not satisfied: " + fooCounter.getCount(), fooCounter.await(100, TimeUnit.SECONDS));
        assertTrue("bar counter not satisfied: " + barCounter.getCount(), barCounter.await(100, TimeUnit.SECONDS));
        assertTrue("kiwi counter not satisfied: " + kiwiCounter.getCount(), kiwiCounter.await(100, TimeUnit.SECONDS));
        assertTrue("ulf counter not satisfied: " + ulfCounter.getCount(), ulfCounter.await(100, TimeUnit.SECONDS));
    }

    public class MockEventListener implements EventListener {
        private List<Event> m_events = new ArrayList<>();
        
        @Override
        public String getName() {
            return "party on, Wayne";
        }

        @Override
        public void onEvent(Event e) {
            m_events.add(e);
        }
        
        public List<Event> getEvents() {
            return m_events;
        }
    }

    public void testBroadcastNowSync() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        final EventListener slowListener = new EventListener() {
            @Override
            public String getName() {
                return "testBroadcastNowSync";
            }

            @Override
            public void onEvent(Event event) {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(2));
                } catch (InterruptedException e) {
                }
                counter.incrementAndGet();
            }
        };

        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl(m_registry);
        manager.setHandlerPoolSize(5);
        DefaultEventHandlerImpl handler = new DefaultEventHandlerImpl(m_registry);
        manager.setEventHandler(handler);
        manager.afterPropertiesSet();

        manager.addEventListener(slowListener);

        EventBuilder bldr = new EventBuilder("uei.opennms.org/foo", "testBroadcastNowSync");
        Event e = bldr.getEvent();

        // Verify the initial state
        assertEquals(0, counter.get());
        // Broadcast synchronously - this should block until our event listener returns
        manager.broadcastNow(e, true);
        // broadcastNow() returned, so the counter should have been increased
        assertEquals(1, counter.get());
    }

    private static class MultiThreadedEventListener implements ThreadAwareEventListener, EventListener {
        private final ThreadLocker locker;
        private final int numThreads;

        public MultiThreadedEventListener(int numThreads, ThreadLocker locker) {
            this.numThreads = numThreads;
            this.locker = Objects.requireNonNull(locker);
        }

        @Override
        public String getName() {
            return getClass().getCanonicalName();
        }

        @Override
        public void onEvent(Event e) {
            locker.park();
        }

        @Override
        public int getNumThreads() {
            return numThreads;
        }
    }

    /**
     * Verify that an event listener that implements the {@link ThreadAwareEventListener} interface
     * receives event callbacks over mulitple threads.
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testMultiThreadedEventListener() throws ExecutionException, InterruptedException {
        int N = 10;
        ThreadLocker locker = new ThreadLocker();
        MultiThreadedEventListener mtListener = new MultiThreadedEventListener(N, locker);
        m_manager.addEventListener(mtListener);

        // No threads waiting
        CompletableFuture<Integer> lockedFuture = locker.waitForThreads(N);

        // Send 2*N events
        for (int k = 0; k < 2*N; k++) {
            EventBuilder bldr = new EventBuilder("uei.opennms.org/foo", "testMultiThreadedEventListener");
            m_manager.broadcastNow(bldr.getEvent(), false);
        }

        // Wait for N threads to be locked
        lockedFuture.get();

        // Sleep a little longer
        Thread.sleep(500);

        // No extra threads should be waiting
        assertThat(locker.getNumExtraThreadsWaiting(), equalTo(0));

        // Release
        locker.release();
    }
}
