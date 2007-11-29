//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 May 06: Moved plugin management and database synchronization out
//              of CapsdConfigFactory, use RrdTestUtils to setup RRD
//              subsystem, and move configuration files out of embedded
//              strings into src/test/resources. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8
package org.opennms.spring.xml;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.utils.EventBuilder;
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
