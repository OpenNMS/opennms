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

package org.opennms.web.element;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.web.api.Util;
import org.opennms.web.servlet.MissingParameterException;

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
    @Override
    public void init() throws ServletException {
        try {
            this.proxy = Util.createEventProxy();
        } catch (Throwable e) {
            throw new ServletException("Exception", e);
        }
    }

    /** {@inheritDoc} */
    @Override
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

            EventBuilder bldr = new EventBuilder(EventConstants.FORCE_RESCAN_EVENT_UEI, "NodeRescanServlet");
            bldr.setNodeid(nodeId);
            bldr.setHost("host");

            // send the event
            this.proxy.send(bldr.getEvent());

            // redirect the request for display
            response.sendRedirect(Util.calculateUrlBase(request, returnUrl));
        } catch (Throwable e) {
            throw new ServletException("Exception sending node rescan event", e);
        }
    }

}
