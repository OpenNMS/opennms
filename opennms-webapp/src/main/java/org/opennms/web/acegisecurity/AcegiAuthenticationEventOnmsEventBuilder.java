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
package org.opennms.web.acegisecurity;

import java.util.Date;

import org.acegisecurity.Authentication;
import org.acegisecurity.event.authentication.AuthenticationSuccessEvent;
import org.acegisecurity.ui.WebAuthenticationDetails;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;

public class AcegiAuthenticationEventOnmsEventBuilder implements ApplicationListener, InitializingBean {
    public static final String SUCCESS_UEI = "uei.opennms.org/internal/authentication/successfulLogin";

    private EventProxy m_eventProxy;
    
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof AuthenticationSuccessEvent) {
            AuthenticationSuccessEvent authEvent = (AuthenticationSuccessEvent) event;

            EventBuilder builder = new EventBuilder(SUCCESS_UEI, "OpenNMS.WebUI");
            builder.setTime(new Date(authEvent.getTimestamp()));
            Authentication auth = authEvent.getAuthentication();
            builder.addParam("user", auth.getName());
            if (auth.getDetails() instanceof WebAuthenticationDetails) {
                WebAuthenticationDetails webDetails = (WebAuthenticationDetails) auth.getDetails();
                if (webDetails.getRemoteAddress() != null) {
                    builder.addParam("ip", webDetails.getRemoteAddress());
                }
            }
            sendEvent(builder.getEvent());
        }
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
