/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.admin.nodeManagement;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.Util;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.element.Service;

/**
 * <p>DeleteServiceServlet class.</p>
 *
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 * @since 1.8.1
 */
public class DeleteServiceServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -8169359759001371089L;

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

            checkParameters(request);

            int nodeId = WebSecurityUtils.safeParseInt(request.getParameter("node"));
            String ipAddr = request.getParameter("intf");
            int serviceId = WebSecurityUtils.safeParseInt(request.getParameter("service"));

            Service service_db = NetworkElementFactory.getInstance(getServletContext()).getService(nodeId, ipAddr, serviceId);

            if (service_db == null) {
                // handle this WAY better, very awful
                throw new ServletException("No such service in database");
            }

            Event e = EventUtils.createDeleteServiceEvent("OpenNMS.WebUI", nodeId, ipAddr, service_db.getServiceName(), -1L);
            sendEvent(e);

            // forward the request for proper display
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/serviceDeleted.jsp");
            dispatcher.forward(request, response);

    }

    /**
     * <p>checkParameters</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     */
    public void checkParameters(HttpServletRequest request) {
        String nodeIdString = request.getParameter("node");
        String ipAddr = request.getParameter("intf");
        String serviceId = request.getParameter("service");

        if (nodeIdString == null) {
            throw new org.opennms.web.servlet.MissingParameterException("node", new String[] { "node", "intf", "service", "ifindex?" });
        }

        if (ipAddr == null) {
            throw new org.opennms.web.servlet.MissingParameterException("intf", new String[] { "node", "intf", "service", "ifindex?" });
        }

        if (serviceId == null) {
            throw new org.opennms.web.servlet.MissingParameterException("service", new String[] { "node", "intf", "service", "ifindex?" });
        }

    }

    private void sendEvent(Event event) throws ServletException {
        try {
            Util.createEventProxy().send(event);
        } catch (Throwable e) {
            throw new ServletException("Could not send event " + event.getUei(), e);
        }
    }

}
