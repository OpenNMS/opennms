/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.model.events;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.events.api.AnnotationBasedEventListenerAdapter;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.annotations.EventExceptionHandler;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.annotations.EventPostProcessor;
import org.opennms.netmgt.events.api.annotations.EventPreProcessor;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.xml.event.Event;

/**
 * AnnotationBasedEventListenerAdapterTest
 *
 * @author brozow
 */
public class AnnotationBasedEventListenerAdapterTest {
    
    private static final String ANNOTATED_NAME = "AnotatedListenerName";
    private static final String OVERRIDEN_NAME = "OverriddenName";
    
    private AnnotatedListener m_annotatedListener;
    private AnnotationBasedEventListenerAdapter m_adapter;
    private EventSubscriptionService m_eventIpcMgr;
    private Set<String> m_subscriptions;
    
    @EventListener(name=ANNOTATED_NAME)
    public static class AnnotatedListener {
        
        public int preProcessedEvents = 0;
        public int receivedEventCount = 0;
        public int postProcessedEvents = 0;
        public int illegalArgsHandled = 0;
        public int genExceptionsHandled = 0;
        
        @EventHandler(uei=EventConstants.NODE_DOWN_EVENT_UEI)
        public void handleAnEvent(IEvent e) {
            receivedEventCount++;
        }
        
        @EventHandler(uei=EventConstants.NODE_LOST_SERVICE_EVENT_UEI)
        public void handleAnotherEvent(IEvent e) {
            throw new IllegalArgumentException("test generated exception");
        }
        
        @EventHandler(uei=EventConstants.ADD_NODE_EVENT_UEI)
        public void handleYetAnotherEvent(IEvent e) {
            throw new IllegalStateException("test generated state exception");
        }
        
        @EventPreProcessor()
        public void preProcess(IEvent e) {
            preProcessedEvents++;
        }
        
        @EventPostProcessor
        public void postProcess(IEvent e) {
            postProcessedEvents++;
        }
        
        @EventExceptionHandler
        public void handleException(IEvent e, IllegalArgumentException ex) {
            illegalArgsHandled++;
        }
        
        @EventExceptionHandler
        public void handleException(IEvent e, Exception ex) {
            genExceptionsHandled++;
        }
        
    }
    
    
    private static class DerivedListener extends AnnotatedListener {
        
    }
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        m_eventIpcMgr = mock(EventSubscriptionService.class);

        m_annotatedListener = new AnnotatedListener();
        m_adapter = new AnnotationBasedEventListenerAdapter();
        m_adapter.setAnnotatedListener(m_annotatedListener);
        m_adapter.setEventSubscriptionService(m_eventIpcMgr);
        
        m_subscriptions = new HashSet<>();
        
        Collections.addAll(m_subscriptions, 
                EventConstants.NODE_DOWN_EVENT_UEI, 
                EventConstants.ADD_NODE_EVENT_UEI,
                EventConstants.NODE_LOST_SERVICE_EVENT_UEI
                );
        
        m_eventIpcMgr.addEventListener(m_adapter, m_subscriptions);
    }

    @After
    public void tearDown() throws Exception {
        verify(m_eventIpcMgr, atLeastOnce()).addEventListener(m_adapter, m_subscriptions);
        verifyNoMoreInteractions(m_eventIpcMgr);
    }

    @Test
    public void testDerivedClass() throws Exception {
        
        AnnotationBasedEventListenerAdapter adapter = new AnnotationBasedEventListenerAdapter();

        // expect a subscription for the new adapter
        m_eventIpcMgr.addEventListener(adapter, m_subscriptions);
        
        // finish expectations for the old adapter
        m_adapter.afterPropertiesSet();

        
        // setup the derivied listener
        DerivedListener derivedListener = new DerivedListener();
        
        adapter.setAnnotatedListener(derivedListener);
        adapter.setEventSubscriptionService(m_eventIpcMgr);
        adapter.afterPropertiesSet();
        

        assertEquals(0, derivedListener.preProcessedEvents);
        assertEquals(0, derivedListener.receivedEventCount);
        assertEquals(0, derivedListener.postProcessedEvents);
        
        adapter.onEvent(ImmutableMapper.fromMutableEvent(createEvent(EventConstants.NODE_DOWN_EVENT_UEI)));
        
        assertEquals(1, derivedListener.preProcessedEvents);
        assertEquals(1, derivedListener.receivedEventCount);
        assertEquals(1, derivedListener.postProcessedEvents);
        
        verify(m_eventIpcMgr, atLeastOnce()).addEventListener(eq(adapter), eq(m_subscriptions));
    }
    
    @Test
    public void testGetNameFromAnnotation() throws Exception {
        m_adapter.afterPropertiesSet();
        assertEquals(ANNOTATED_NAME, m_adapter.getName());
    }
    
    @Test
    public void testOverriddenName() throws Exception {
        m_adapter.setName(OVERRIDEN_NAME);
        m_adapter.afterPropertiesSet();
        
        assertEquals(OVERRIDEN_NAME, m_adapter.getName());
    }
    
    @Test
    public void testSendMatchingEvent() {
        m_adapter.afterPropertiesSet();
        
        assertEquals(0, m_annotatedListener.preProcessedEvents);
        assertEquals(0, m_annotatedListener.receivedEventCount);
        assertEquals(0, m_annotatedListener.postProcessedEvents);
        
        m_adapter.onEvent(ImmutableMapper.fromMutableEvent(createEvent(EventConstants.NODE_DOWN_EVENT_UEI)));
        
        assertEquals(1, m_annotatedListener.preProcessedEvents);
        assertEquals(1, m_annotatedListener.receivedEventCount);
        assertEquals(1, m_annotatedListener.postProcessedEvents);
        
    }
    
    @Test
    public void testProcessingException() {
        m_adapter.afterPropertiesSet();
        
        assertEquals(0, m_annotatedListener.illegalArgsHandled);
        assertEquals(0, m_annotatedListener.genExceptionsHandled);

        m_adapter.onEvent(ImmutableMapper.fromMutableEvent(createEvent(EventConstants.NODE_LOST_SERVICE_EVENT_UEI)));
        
        assertEquals(1, m_annotatedListener.illegalArgsHandled);
        assertEquals(0, m_annotatedListener.genExceptionsHandled);
        
        m_adapter.onEvent(ImmutableMapper.fromMutableEvent(createEvent(EventConstants.ADD_NODE_EVENT_UEI)));
        
        assertEquals(1, m_annotatedListener.illegalArgsHandled);
        assertEquals(1, m_annotatedListener.genExceptionsHandled);
    }

    private Event createEvent(String uei) {
        return new EventBuilder(uei, "Test").getEvent();
    }

}
