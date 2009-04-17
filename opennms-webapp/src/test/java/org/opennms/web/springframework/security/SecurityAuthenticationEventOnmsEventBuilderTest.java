//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 May 10: Add support for failed events. - dj@opennms.org
//
// Copyright (C) 2008 Daniel J. Gregor, Jr..  All Rights Reserved.
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
package org.opennms.web.springframework.security;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.opennms.netmgt.mock.EventWrapper;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.Authentication;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.event.authentication.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.event.authentication.AuthenticationSuccessEvent;
import org.springframework.security.providers.TestingAuthenticationToken;
import org.springframework.security.ui.WebAuthenticationDetails;

public class SecurityAuthenticationEventOnmsEventBuilderTest extends TestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    private EventProxy m_eventProxy = m_mocks.createMock(EventProxy.class);
    
    public void testAuthenticationSuccessEventWithEverything() throws Exception {
        String userName = "bar";
        String ip = "1.2.3.4";
        String sessionId = "it tastes just like our regular coffee";
        
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpSession session = createMock(HttpSession.class);
        expect(request.getRemoteAddr()).andReturn(ip);
        expect(request.getSession(false)).andReturn(session);
        expect(session.getId()).andReturn(sessionId);
        
        replay(request, session);
        WebAuthenticationDetails details = new WebAuthenticationDetails(request);
        verify(request, session);
        
        Authentication authentication = new TestingDetailsAuthenticationToken(userName, "cheesiness", new GrantedAuthority[0], details);
        AuthenticationSuccessEvent authEvent = new AuthenticationSuccessEvent(authentication);
        
        SecurityAuthenticationEventOnmsEventBuilder builder = new SecurityAuthenticationEventOnmsEventBuilder();
        builder.setEventProxy(m_eventProxy);
        builder.afterPropertiesSet();
        
        EventBuilder eventBuilder = new EventBuilder(SecurityAuthenticationEventOnmsEventBuilder.SUCCESS_UEI, "OpenNMS.WebUI");
        eventBuilder.addParam("user", userName);
        eventBuilder.addParam("ip", ip);
        
        m_eventProxy.send(EventEquals.eqEvent(eventBuilder.getEvent()));
        
        m_mocks.replayAll();
        builder.onApplicationEvent(authEvent);
        m_mocks.verifyAll();
    }
    
    public void testAuthenticationFailureEvent() throws Exception {
        String userName = "bar";
        String ip = "1.2.3.4";
        String sessionId = "it tastes just like our regular coffee";
        
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpSession session = createMock(HttpSession.class);
        expect(request.getRemoteAddr()).andReturn(ip);
        expect(request.getSession(false)).andReturn(session);
        expect(session.getId()).andReturn(sessionId);
        
        replay(request, session);
        WebAuthenticationDetails details = new WebAuthenticationDetails(request);
        verify(request, session);
        
        Authentication authentication = new TestingDetailsAuthenticationToken(userName, "cheesiness", new GrantedAuthority[0], details);
        AuthenticationFailureBadCredentialsEvent authEvent = new AuthenticationFailureBadCredentialsEvent(authentication, new BadCredentialsException("you are bad!"));
        
        SecurityAuthenticationEventOnmsEventBuilder builder = new SecurityAuthenticationEventOnmsEventBuilder();
        builder.setEventProxy(m_eventProxy);
        builder.afterPropertiesSet();
        
        EventBuilder eventBuilder = new EventBuilder(SecurityAuthenticationEventOnmsEventBuilder.FAILURE_UEI, "OpenNMS.WebUI");
        eventBuilder.addParam("user", userName);
        eventBuilder.addParam("ip", ip);
        eventBuilder.addParam("exceptionName", authEvent.getException().getClass().getSimpleName());
        eventBuilder.addParam("exceptionMessage", authEvent.getException().getMessage());
        
        m_eventProxy.send(EventEquals.eqEvent(eventBuilder.getEvent()));
        
        m_mocks.replayAll();
        builder.onApplicationEvent(authEvent);
        m_mocks.verifyAll();
    }

    /**
     * This shouldn't trigger an OpenNMS event.
     */
    public void testRandomEvent() throws Exception {
        SecurityAuthenticationEventOnmsEventBuilder builder = new SecurityAuthenticationEventOnmsEventBuilder();
        builder.setEventProxy(m_eventProxy);
        builder.afterPropertiesSet();
        
        m_mocks.replayAll();
        builder.onApplicationEvent(new TestApplicationEvent("Hello!"));
        m_mocks.verifyAll();
    }
    
    public static class EventEquals implements IArgumentMatcher {
        private Event m_expected;

        public EventEquals(Event expected) {
            m_expected = expected;
        }

        public boolean matches(Object actual) {
            if (!(actual instanceof Event)) {
                return false;
            }
            
            Event actualEvent = (Event) actual;
            return MockEventUtil.eventsMatchDeep(m_expected, actualEvent);
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("eqEvent(");
            buffer.append(new EventWrapper(m_expected));
            buffer.append(")");
        }
        
        public static Event eqEvent(Event in) {
            EasyMock.reportMatcher(new EventEquals(in));
            return null;
        }
    }
    
    public static class TestingDetailsAuthenticationToken extends TestingAuthenticationToken {
        private static final long serialVersionUID = 1L;
        private Object m_details;

        public TestingDetailsAuthenticationToken(Object name, Object principal, GrantedAuthority[] authorities, Object details) {
            super(name, principal, authorities);
            m_details = details;
        }
        
        @Override
        public Object getDetails() {
            return m_details;
        }
    }
    
    public static class TestApplicationEvent extends ApplicationEvent {
        private static final long serialVersionUID = 1L;

        public TestApplicationEvent(Object obj) {
            super(obj);
        }
    }
}
