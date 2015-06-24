/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.admin.notification;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet that handles updating the status of the notifications
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class UpdateNotifdStatusServlet extends HttpServlet {
	
	private static final Logger LOG = LoggerFactory.getLogger(UpdateNotifdStatusServlet.class);

    /**
     * 
     */
    private static final long serialVersionUID = -841122529212545321L;

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            LOG.info("Setting notifd status to {} for user {}", request.getParameter("status"), request.getRemoteUser());
            if (request.getParameter("status").equals("on")) {
                NotifdConfigFactory.getInstance().turnNotifdOn();
                sendEvent("uei.opennms.org/internal/notificationsTurnedOn", request);
            } else {
                NotifdConfigFactory.getInstance().turnNotifdOff();
                sendEvent("uei.opennms.org/internal/notificationsTurnedOff", request);
            }
        } catch (Throwable e) {
            throw new ServletException("Could not update notification status: " + e.getMessage(), e);
        }

        // Redirect to admin/index.jsp
        response.sendRedirect("index.jsp");
    }

    protected void sendEvent(String uei, HttpServletRequest request) {
        EventBuilder bldr = new EventBuilder(uei, "NotifdConfigFactory");
        bldr.addParam("remoteUser", request.getRemoteUser());
        bldr.addParam("remoteHost", request.getRemoteHost());
        bldr.addParam("remoteAddr", request.getRemoteAddr());
    
        try {
            EventIpcManagerFactory.getIpcManager().sendNow(bldr.getEvent());
        } catch (Throwable t) {
        }
    }

   
}
