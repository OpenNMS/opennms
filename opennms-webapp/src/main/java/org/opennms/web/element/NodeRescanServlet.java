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

package org.opennms.web.element;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.api.Util;

/**
 * <p>NodeRescanServlet class.</p>
 *
 * @author <A HREF="larry@opennms.org">Larry Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="larry@opennms.org">Larry Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class NodeRescanServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -3183139374532183137L;
    protected EventProxy proxy;

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    public void init() throws ServletException {
        try {
            this.proxy = Util.createEventProxy();
        } catch (Exception e) {
            throw new ServletException("Exception", e);
        }
    }

    /** {@inheritDoc} */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // required parameters
        String nodeIdString = request.getParameter("node");
        String returnUrl = request.getParameter("returnUrl");

        if (nodeIdString == null) {
            throw new MissingParameterException("node", new String[] { "node", "returnUrl" });
        }
        if (returnUrl == null) {
            throw new MissingParameterException("returnUrl", new String[] { "node", "returnUrl" });
        }

        try {
            int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);

            // prepare the event
            Event outEvent = new Event();
            outEvent.setSource("NodeRescanServlet");
            outEvent.setUei(EventConstants.FORCE_RESCAN_EVENT_UEI);
            outEvent.setNodeid(nodeId);
            outEvent.setHost("host");
            outEvent.setTime(EventConstants.formatToString(new java.util.Date()));

            // send the event
            this.proxy.send(outEvent);

            // redirect the request for display
            response.sendRedirect(Util.calculateUrlBase(request) + "/" + returnUrl);
        } catch (Exception e) {
            throw new ServletException("Exception sending node rescan event", e);
        }
    }

}
