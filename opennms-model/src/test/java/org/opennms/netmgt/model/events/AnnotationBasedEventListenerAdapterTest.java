/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.model.events;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.annotations.EventExceptionHandler;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.model.events.annotations.EventPostProcessor;
import org.opennms.netmgt.model.events.annotations.EventPreProcessor;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.EasyMockUtils;

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
    private EasyMockUtils m_mockUtils;
    private EventSubscriptionService m_eventIpcMgr;
    private Set<String> m_subscriptions;
    
    @EventListener(name=ANNOTATED_NAME)
    private static class AnnotatedListener {
        
        public int preProcessedEvents = 0;
        public int receivedEventCount = 0;
        public int postProcessedEvents = 0;
        public int illegalArgsHandled = 0;
        public int genExceptionsHandled = 0;
        
        @SuppressWarnings("unused")
        @EventHandler(uei=EventConstants.NODE_DOWN_EVENT_UEI)
        public void handleAnEvent(Event e) {
            receivedEventCount++;
        }
        
        @SuppressWarnings("unused")
        @EventHandler(uei=EventConstants.ADD_INTERFACE_EVENT_UEI)
        public void handleAnotherEvent(Event e) {
            throw new IllegalArgumentException("test generated exception");
        }
        
        @SuppressWarnings("unused")
        @EventHandler(uei=EventConstants.ADD_NODE_EVENT_UEI)
        public void handleYetAnotherEvent(Event e) {
            throw new IllegalStateException("test generated state exception");
        }
        
        @SuppressWarnings("unused")
        @EventPreProcessor()
        public void preProcess(Event e) {
            preProcessedEvents++;
        }
        
        @SuppressWarnings("unused")
        @EventPostProcessor
        public void postProcess(Event e) {
            postProcessedEvents++;
        }
        
        @SuppressWarnings("unused")
        @EventExceptionHandler
        public void handleException(Event e, IllegalArgumentException ex) {
            illegalArgsHandled++;
        }
        
        @SuppressWarnings("unused")
        @EventExceptionHandler
        public void handleException(Event e, Exception ex) {
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
        
        m_mockUtils = new EasyMockUtils();
        
        m_eventIpcMgr = m_mockUtils.createMock(EventSubscriptionService.class);

        m_annotatedListener = new AnnotatedListener();
        m_adapter = new AnnotationBasedEventListenerAdapter();
        m_adapter.setAnnotatedListener(m_annotatedListener);
        m_adapter.setEventSubscriptionService(m_eventIpcMgr);
        
        m_subscriptions = new HashSet<String>();
        
        Collections.addAll(m_subscriptions, 
                EventConstants.NODE_DOWN_EVENT_UEI, 
                EventConstants.ADD_NODE_EVENT_UEI,
                EventConstants.ADD_INTERFACE_EVENT_UEI
                );
        
        m_eventIpcMgr.addEventListener(m_adapter, m_subscriptions);
    }

    @Test
    public void testDerivedClass() throws Exception {
        
        AnnotationBasedEventListenerAdapter adapter = new AnnotationBasedEventListenerAdapter();

        // expect a subscription for the new adapter
        m_eventIpcMgr.addEventListener(adapter, m_subscriptions);
        
        m_mockUtils.replayAll();

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
        
        adapter.onEvent(createEvent(EventConstants.NODE_DOWN_EVENT_UEI));
        
        assertEquals(1, derivedListener.preProcessedEvents);
        assertEquals(1, derivedListener.receivedEventCount);
        assertEquals(1, derivedListener.postProcessedEvents);
        
        m_mockUtils.verifyAll();
    }
    
    @Test
    public void testGetNameFromAnnotation() throws Exception {
        m_mockUtils.replayAll();
        
        m_adapter.afterPropertiesSet();
        assertEquals(ANNOTATED_NAME, m_adapter.getName());
        
        m_mockUtils.verifyAll();
    }
    
    @Test
    public void testOverriddenName() throws Exception {
        m_mockUtils.replayAll();

        m_adapter.setName(OVERRIDEN_NAME);
        m_adapter.afterPropertiesSet();
        
        assertEquals(OVERRIDEN_NAME, m_adapter.getName());
        
        m_mockUtils.verifyAll();
    }
    
    @Test
    public void testSendMatchingEvent() {
        
        m_mockUtils.replayAll();

        m_adapter.afterPropertiesSet();
        
        assertEquals(0, m_annotatedListener.preProcessedEvents);
        assertEquals(0, m_annotatedListener.receivedEventCount);
        assertEquals(0, m_annotatedListener.postProcessedEvents);
        
        m_adapter.onEvent(createEvent(EventConstants.NODE_DOWN_EVENT_UEI));
        
        assertEquals(1, m_annotatedListener.preProcessedEvents);
        assertEquals(1, m_annotatedListener.receivedEventCount);
        assertEquals(1, m_annotatedListener.postProcessedEvents);
        
        m_mockUtils.verifyAll();
        
    }
    
    @Test
    public void testProcessingException() {
        
        m_mockUtils.replayAll();

        m_adapter.afterPropertiesSet();
        
        assertEquals(0, m_annotatedListener.illegalArgsHandled);
        assertEquals(0, m_annotatedListener.genExceptionsHandled);

        m_adapter.onEvent(createEvent(EventConstants.ADD_INTERFACE_EVENT_UEI));
        
        assertEquals(1, m_annotatedListener.illegalArgsHandled);
        assertEquals(0, m_annotatedListener.genExceptionsHandled);
        
        m_adapter.onEvent(createEvent(EventConstants.ADD_NODE_EVENT_UEI));
        
        assertEquals(1, m_annotatedListener.illegalArgsHandled);
        assertEquals(1, m_annotatedListener.genExceptionsHandled);

        m_mockUtils.verifyAll();
        
    }

    private Event createEvent(String uei) {
        return new EventBuilder(uei, "Test").getEvent();
    }

}
