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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

import org.acegisecurity.context.HttpSessionContextIntegrationFilter;
import org.acegisecurity.context.SecurityContext;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Listens for HTTP session attribute changes and sends events when the Acegi
 * security context object is removed from a users session.  It is assumed that
 * this is a precursor to the user logging out or having their session destroyed
 * (e.g.: because of a timeout).  Cooperates with SessionNotatingLogoutHandler
 * to determine if the user clicked the "log out" link in the UI.  Different
 * events are sent for a user-initiated log out vs. a session removal for
 * another reason.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class AcegiSessionAttributeListenerOnmsEventBuilder implements HttpSessionAttributeListener {
    /** Constant <code>LOGGED_OUT_EVENT="uei.opennms.org/internal/authentication"{trunked}</code> */
    public final static String LOGGED_OUT_EVENT = "uei.opennms.org/internal/authentication/loggedOut";
    /** Constant <code>SESSION_REMOVED_EVENT="uei.opennms.org/internal/authentication"{trunked}</code> */
    public final static String SESSION_REMOVED_EVENT = "uei.opennms.org/internal/authentication/sessionRemoved";

    private Set<String> m_sessionIdsLoggedOut = Collections.synchronizedSet(new HashSet<String>());
    
    /** {@inheritDoc} */
    public void attributeAdded(HttpSessionBindingEvent event) {
        if (event.getName().equals(SessionNotatingLogoutHandler.SESSION_ATTRIBUTE)) {
            m_sessionIdsLoggedOut.add(event.getSession().getId());
        }
    }

    /** {@inheritDoc} */
    public void attributeRemoved(HttpSessionBindingEvent event) {
        if (event.getName().equals(HttpSessionContextIntegrationFilter.ACEGI_SECURITY_CONTEXT_KEY)) {
            SecurityContext securityContext = (SecurityContext) event.getValue();
            
            WebApplicationContext appContext = WebApplicationContextUtils.getRequiredWebApplicationContext(event.getSession().getServletContext());
            EventProxy eventProxy = (EventProxy) appContext.getBean("eventProxy", EventProxy.class);

            String user = securityContext.getAuthentication().getName();
            if (m_sessionIdsLoggedOut.contains(event.getSession().getId())) {
                m_sessionIdsLoggedOut.remove(event.getSession().getId());
                createEvent(user, eventProxy, LOGGED_OUT_EVENT);
            } else {
                createEvent(user, eventProxy, SESSION_REMOVED_EVENT);
            }
        }
    }

    /** {@inheritDoc} */
    public void attributeReplaced(HttpSessionBindingEvent event) {
    }

    private void createEvent(String user, EventProxy eventProxy, String uei) {
        EventBuilder builder = new EventBuilder(uei, "OpenNMS.WebUI");
        builder.addParam("user", user);
        try {
            eventProxy.send(builder.getEvent());
        } catch (EventProxyException e) {
            log().error("Could not send event: " + e, e);
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}
