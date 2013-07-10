/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
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

package org.opennms.web.springframework.security;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.opennms.netmgt.dao.mock.EventWrapper;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

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
        
        org.springframework.security.core.Authentication authentication = new TestingDetailsAuthenticationToken(userName, "cheesiness", new GrantedAuthority[0], details);
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
        
        org.springframework.security.core.Authentication authentication = new TestingDetailsAuthenticationToken(userName, "cheesiness", new GrantedAuthority[0], details);
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

        @Override
        public boolean matches(Object actual) {
            if (!(actual instanceof Event)) {
                return false;
            }
            
            Event actualEvent = (Event) actual;
            return MockEventUtil.eventsMatchDeep(m_expected, actualEvent);
        }

        @Override
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
    
    public static class TestingDetailsAuthenticationToken extends AbstractAuthenticationToken {
        /**
		 * 
		 */
		private static final long serialVersionUID = -6197934198093164407L;
		private Object m_principal;
        private Object m_credentials;
        private Object m_details;

        public TestingDetailsAuthenticationToken(Object principal, Object credentials, GrantedAuthority[] authorities, Object details) {
            super(Arrays.asList(authorities));
            m_principal = principal;
            m_credentials = credentials;
            m_details = details;
        }
        
        @Override
        public Object getDetails() {
            return m_details;
        }

                @Override
        public Object getCredentials() {
            return m_credentials;
        }

                @Override
        public Object getPrincipal() {
            return m_principal;
        }
    }
    
    public static class TestApplicationEvent extends ApplicationEvent {

        /**
		 * 
		 */
		private static final long serialVersionUID = 7573808524408766331L;

		public TestApplicationEvent(Object obj) {
            super(obj);
        }
    }
}
