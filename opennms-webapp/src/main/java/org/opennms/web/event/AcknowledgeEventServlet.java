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
// 2007 Jul 24: Add serialVersionUID. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.event;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.acegisecurity.Authentication;

/**
 * This servlet receives an HTTP POST with a list of events to acknowledge or
 * unacknowledge, and then it redirects the client to a URL for display. The
 * target URL is configurable in the servlet config (web.xml file).
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.6.12
 */
public class AcknowledgeEventServlet extends BaseAcknowledgeServlet {
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     *
     * Acknowledge the events specified in the POST and then redirect the client
     * to an appropriate URL for display.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	if(request.isUserInRole( Authentication.READONLY_ROLE)) {
    		//ERROR: unauthorised user trying to access this servlet by surreptitious means
    		throw new ServletException("Unauthorized access to this servlet");
    	}
    	// required parameter
        String[] eventIdStrings = request.getParameterValues("event");
        String action = request.getParameter("action");

        if (eventIdStrings == null) {
            throw new MissingParameterException("event", new String[] { "event", "action" });
        }

        if (action == null) {
            throw new MissingParameterException("action", new String[] { "event", "action" });
        }

        // convert the event id strings to ints
        int[] eventIds = new int[eventIdStrings.length];
        for (int i = 0; i < eventIds.length; i++) {
            eventIds[i] = WebSecurityUtils.safeParseInt(eventIdStrings[i]);
        }

        try {
            if (action.equals(ACKNOWLEDGE_ACTION)) {
                EventFactory.acknowledge(eventIds, request.getRemoteUser());
            } else if (action.equals(UNACKNOWLEDGE_ACTION)) {
                EventFactory.unacknowledge(eventIds);
            } else {
                throw new ServletException("Unknown acknowledge action: " + action);
            }

            response.sendRedirect(this.getRedirectString(request));
        } catch (SQLException e) {
            throw new ServletException("Database exception", e);
        }
    }

}
