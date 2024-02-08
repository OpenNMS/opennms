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
package org.opennms.web.springframework.security;

import java.net.ConnectException;
import java.util.Date;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.access.event.AuthorizedEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.util.Assert;
import org.springframework.web.context.support.ServletRequestHandledEvent;

/**
 * <p>SecurityAuthenticationEventOnmsEventBuilder class.</p>
 */
public class SecurityAuthenticationEventOnmsEventBuilder implements ApplicationListener<ApplicationEvent>, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityAuthenticationEventOnmsEventBuilder.class);
    /** Constant <code>SUCCESS_UEI="uei.opennms.org/internal/authentication"{trunked}</code> */
    public static final String SUCCESS_UEI = "uei.opennms.org/internal/authentication/successfulLogin";
    /** Constant <code>FAILURE_UEI="uei.opennms.org/internal/authentication"{trunked}</code> */
    public static final String FAILURE_UEI = "uei.opennms.org/internal/authentication/failure";

    private EventProxy m_eventProxy;
    
    /** {@inheritDoc} */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        LOG.debug("Received ApplicationEvent {}", event.getClass());
        if (event instanceof AuthenticationSuccessEvent) {
            AuthenticationSuccessEvent authEvent = (AuthenticationSuccessEvent) event;

            EventBuilder builder = createEvent(SUCCESS_UEI, authEvent);
            // Sync the timestamp
            builder.setTime(new Date(event.getTimestamp()));
            if (!"true".equalsIgnoreCase(System.getProperty("org.opennms.security.disableLoginSuccessEvent"))) {
                sendEvent(builder.getEvent());
            }
        }
        
        if (event instanceof AbstractAuthenticationFailureEvent) {
            AbstractAuthenticationFailureEvent authEvent = (AbstractAuthenticationFailureEvent) event;
            
            LOG.debug("AbstractAuthenticationFailureEvent was received, exception message - {}", authEvent.getException().getMessage());
            EventBuilder builder = createEvent(FAILURE_UEI, authEvent);
            // Sync the timestamp
            builder.setTime(new Date(event.getTimestamp()));
            builder.addParam("exceptionName", authEvent.getException().getClass().getSimpleName());
            builder.addParam("exceptionMessage", authEvent.getException().getMessage());
            sendEvent(builder.getEvent());
        }
        
        if (event instanceof AuthorizedEvent) {
            AuthorizedEvent authEvent = (AuthorizedEvent) event;
            LOG.debug("AuthorizedEvent received - \n  Details - {}\n  Principal - {}", authEvent.getAuthentication().getDetails(), authEvent.getAuthentication().getPrincipal());
        }
        if (event instanceof AuthorizationFailureEvent) {
            AuthorizationFailureEvent authEvent = (AuthorizationFailureEvent) event;
            LOG.debug("AuthorizationFailureEvent received  -\n   Details - {}\n  Principal - {}", authEvent.getAuthentication().getDetails(), authEvent.getAuthentication().getPrincipal());
        }
        if (event instanceof InteractiveAuthenticationSuccessEvent) {
            InteractiveAuthenticationSuccessEvent authEvent = (InteractiveAuthenticationSuccessEvent) event;
            LOG.debug("InteractiveAuthenticationSuccessEvent received - \n  Details - {}\n  Principal - {}", authEvent.getAuthentication().getDetails(), authEvent.getAuthentication().getPrincipal());
            
        }
        if (event instanceof ServletRequestHandledEvent) {
            ServletRequestHandledEvent authEvent = (ServletRequestHandledEvent) event;
            LOG.debug("ServletRequestHandledEvent received - {}\n  Servlet - {}\n  URL - {}", authEvent.getDescription(), authEvent.getServletName(), authEvent.getRequestUrl());
            LOG.info("{} requested from {} by user {}", authEvent.getRequestUrl(), authEvent.getClientAddress(), authEvent.getUserName());
        }
        
    }

    private EventBuilder createEvent(String uei, AbstractAuthenticationEvent authEvent) {
        EventBuilder builder = new EventBuilder(uei, "OpenNMS.WebUI");
        builder.setTime(new Date(authEvent.getTimestamp()));
        org.springframework.security.core.Authentication auth = authEvent.getAuthentication();
        if (auth != null && auth.getName() != null) {
            builder.addParam("user", WebSecurityUtils.sanitizeString(auth.getName()));
        }
        if (auth != null && auth.getDetails() != null && auth.getDetails() instanceof WebAuthenticationDetails) {
            WebAuthenticationDetails webDetails = (WebAuthenticationDetails) auth.getDetails();
            if (webDetails.getRemoteAddress() != null) {
                builder.addParam("ip", webDetails.getRemoteAddress());
            }
        }
        return builder;
    }
    
    private void sendEvent(Event onmsEvent) {
        try {
            m_eventProxy.send(onmsEvent);
        } catch (EventProxyException e) {
            if (ExceptionUtils.getRootCause(e) instanceof ConnectException) {
                LOG.error("Failed to send OpenNMS event to event proxy ( {} )", m_eventProxy, e);
            } else {
                LOG.error("Failed to send OpenNMS event to event proxy ( {} )", m_eventProxy, e);
            }
        }
    }
    
    /**
     * <p>setEventProxy</p>
     *
     * @param eventProxy a {@link org.opennms.netmgt.events.api.EventProxy} object.
     */
    public void setEventProxy(EventProxy eventProxy) {
        m_eventProxy = eventProxy;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(m_eventProxy, "property eventProxy must be set");
    }
}
