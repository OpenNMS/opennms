//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Mar 10: Created this file. - dj@opennms.org
//
// Copyright (C) 2008 Daniel J. Gregor, Jr..  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.eventd;

import org.easymock.EasyMock;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;

import junit.framework.TestCase;

public class EventIpcManagerProxyTest extends TestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    
    private EventIpcManagerProxy m_proxy = new EventIpcManagerProxy();
    private EventListener m_eventListener = m_mocks.createMock(EventListener.class);

    public void testAddEventListenerNoProxySet() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        
        ta.anticipate(new IllegalStateException("property delegate not set; has the event daemon successfully started?"));
        try {
            m_proxy.addEventListener(m_eventListener);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }
    
    public void testAddEventListenerWithProxySet() {
        EventIpcManager delegate = EasyMock.createMock(EventIpcManager.class);
        
        m_proxy.setDelegate(delegate);
        
        delegate.addEventListener(m_eventListener);
        
        m_mocks.replayAll();
        m_proxy.addEventListener(m_eventListener);
        m_mocks.verifyAll();
    }
}
