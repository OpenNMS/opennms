/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.spring.xml;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class AspectJTest extends AbstractDependencyInjectionSpringContextTests {
    

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:/org/opennms/spring/xml/applicationContext-testAOP.xml"
        };
    }
    
    @Override
    protected void onSetUp() throws Exception {
        m_handler.reset();
        m_interceptor.reset();
    }



    private MockEventIpcManager m_eventIpcManager;
    private AspectJTestEventHandler m_handler;
    private AspectJTestEventHandlerInteceptor m_interceptor;

    public void setEventIpcManager(MockEventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

    public void setHandler(AspectJTestEventHandler handler) {
        m_handler = handler;
    }
    
    public void setInterceptor(AspectJTestEventHandlerInteceptor interceptor) {
        m_interceptor = interceptor;
    }

    public void testAOPProxying() throws Throwable {
        
        assertEquals(0, m_handler.getHandlerCallCount());
        assertEquals(0, m_interceptor.getPreEventCount());
        assertEquals(0, m_interceptor.getPostEventCount());
        
        m_handler.handleAnEvent(createEvent(EventConstants.ADD_INTERFACE_EVENT_UEI));
        
        assertEquals(1, m_handler.getHandlerCallCount());
        assertEquals(1, m_interceptor.getPreEventCount());
        assertEquals(1, m_interceptor.getPostEventCount());
        
    }
    
    public void testEventAdapterOnProxy() {
        
        assertEquals(0, m_handler.getHandlerCallCount());
        assertEquals(0, m_interceptor.getPreEventCount());
        assertEquals(0, m_interceptor.getPostEventCount());
        
        sendEvent(EventConstants.ADD_INTERFACE_EVENT_UEI);
        
        assertEquals(1, m_handler.getHandlerCallCount());
        assertEquals(1, m_interceptor.getPreEventCount());
        assertEquals(1, m_interceptor.getPostEventCount());
        
    }
    
    public void testHandledException() {
        
        assertEquals(0, m_handler.getHandlerCallCount());
        assertEquals(0, m_interceptor.getPreEventCount());
        assertEquals(0, m_interceptor.getPostEventCount());
        assertEquals(0, m_interceptor.getHandledExceptionCount());
        
        m_handler.setThrownException(new RuntimeException("test exception"));
        
        sendEvent(EventConstants.ADD_INTERFACE_EVENT_UEI);
        
        assertEquals(1, m_handler.getHandlerCallCount());
        assertEquals(1, m_interceptor.getPreEventCount());
        assertEquals(0, m_interceptor.getPostEventCount());
        assertEquals(1, m_interceptor.getHandledExceptionCount());
        
    }
    
    public void testUnhandledException() {
        
        assertEquals(0, m_handler.getHandlerCallCount());
        assertEquals(0, m_interceptor.getPreEventCount());
        assertEquals(0, m_interceptor.getPostEventCount());
        assertEquals(0, m_interceptor.getHandledExceptionCount());
        
        m_handler.setThrownException(new Exception("test exception"));
        
        sendEvent(EventConstants.ADD_INTERFACE_EVENT_UEI);
        
        assertEquals(1, m_handler.getHandlerCallCount());
        assertEquals(1, m_interceptor.getPreEventCount());
        assertEquals(0, m_interceptor.getPostEventCount());
        assertEquals(0, m_interceptor.getHandledExceptionCount());
        
    }
    
    private void sendEvent(String uei) {
        m_eventIpcManager.sendNow(createEvent(uei));
    }

    private Event createEvent(String uei) {
        return new EventBuilder(uei, "Test").getEvent();
    }
}
