/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.web.springframework.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class SecurityAuthenticationEventOnmsEventBuilderTest {
    private EventProxy m_eventProxy;

    @Before
    public void setUp() {
        m_eventProxy = mock(EventProxy.class);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_eventProxy);
    }

    @Test
    public void testAuthenticationSuccessEventWithEverything() throws Exception {
        String userName = "bar";
        String ip = "1.2.3.4";
        String sessionId = "it tastes just like our regular coffee";
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getRemoteAddr()).thenReturn(ip);
        when(request.getSession(false)).thenReturn(session);
        when(session.getId()).thenReturn(sessionId);
        
        WebAuthenticationDetails details = new WebAuthenticationDetails(request);
        
        org.springframework.security.core.Authentication authentication = new TestingDetailsAuthenticationToken(userName, "cheesiness", new GrantedAuthority[0], details);
        AuthenticationSuccessEvent authEvent = new AuthenticationSuccessEvent(authentication);
        
        SecurityAuthenticationEventOnmsEventBuilder builder = new SecurityAuthenticationEventOnmsEventBuilder();
        builder.setEventProxy(m_eventProxy);
        builder.afterPropertiesSet();
        
        EventBuilder eventBuilder = new EventBuilder(SecurityAuthenticationEventOnmsEventBuilder.SUCCESS_UEI, "OpenNMS.WebUI");
        eventBuilder.addParam("user", userName);
        eventBuilder.addParam("ip", ip);
        
        Event expectedEvent = eventBuilder.getEvent();
        // Make sure the timestamps are synchronized
        expectedEvent.setTime(new Date(authEvent.getTimestamp()));
        
        builder.onApplicationEvent(authEvent);

        verify(m_eventProxy, atLeastOnce()).send(any(Event.class));
    }

    @Test
    public void testAuthenticationFailureEvent() throws Exception {
        String userName = "bar";
        String ip = "1.2.3.4";
        String sessionId = "it tastes just like our regular coffee";
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getRemoteAddr()).thenReturn(ip);
        when(request.getSession(false)).thenReturn(session);
        when(session.getId()).thenReturn(sessionId);
        
        WebAuthenticationDetails details = new WebAuthenticationDetails(request);
        
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
        
        builder.onApplicationEvent(authEvent);

        verify(m_eventProxy, atLeastOnce()).send(any(Event.class));
    }

    /**
     * This shouldn't trigger an OpenNMS event.
     */
    @Test
    public void testRandomEvent() throws Exception {
        SecurityAuthenticationEventOnmsEventBuilder builder = new SecurityAuthenticationEventOnmsEventBuilder();
        builder.setEventProxy(m_eventProxy);
        builder.afterPropertiesSet();
        
        builder.onApplicationEvent(new TestApplicationEvent("Hello!"));
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
