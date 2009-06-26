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

import java.util.Date;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.Authentication;
import org.springframework.security.event.authentication.AbstractAuthenticationEvent;
import org.springframework.security.event.authentication.AbstractAuthenticationFailureEvent;
import org.springframework.security.event.authentication.AuthenticationSuccessEvent;
import org.springframework.security.ui.WebAuthenticationDetails;
import org.springframework.util.Assert;

public class SecurityAuthenticationEventOnmsEventBuilder implements ApplicationListener, InitializingBean {
    public static final String SUCCESS_UEI = "uei.opennms.org/internal/authentication/successfulLogin";
    public static final String FAILURE_UEI = "uei.opennms.org/internal/authentication/failure";

    private EventProxy m_eventProxy;
    
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof AuthenticationSuccessEvent) {
            AuthenticationSuccessEvent authEvent = (AuthenticationSuccessEvent) event;

            EventBuilder builder = createEvent(SUCCESS_UEI, authEvent);
            sendEvent(builder.getEvent());
        }
        
        if (event instanceof AbstractAuthenticationFailureEvent) {
            AbstractAuthenticationFailureEvent authEvent = (AbstractAuthenticationFailureEvent) event;

            EventBuilder builder = createEvent(FAILURE_UEI, authEvent);
            builder.addParam("exceptionName", authEvent.getException().getClass().getSimpleName());
            builder.addParam("exceptionMessage", authEvent.getException().getMessage());
            sendEvent(builder.getEvent());
        }
    }

    private EventBuilder createEvent(String uei, AbstractAuthenticationEvent authEvent) {
        EventBuilder builder = new EventBuilder(uei, "OpenNMS.WebUI");
        builder.setTime(new Date(authEvent.getTimestamp()));
        Authentication auth = authEvent.getAuthentication();
        if (auth != null && auth.getName() != null) {
            builder.addParam("user", auth.getName());
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
            log().error("Failed to send OpenNMS event to event proxy (" + m_eventProxy + "): " + e, e);
        }
    }
    
    public void setEventProxy(EventProxy eventProxy) {
        m_eventProxy = eventProxy;
    }

    public void afterPropertiesSet() {
        Assert.notNull(m_eventProxy, "property eventProxy must be set");
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
