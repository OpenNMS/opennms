/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.web.admin.notification;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventBuilder;

/**
 * A servlet that handles updating the status of the notifications
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class UpdateNotifdStatusServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -841122529212545321L;

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            log().info("Setting notifd status to " + request.getParameter("status") + " for user " + request.getRemoteUser());
            if (request.getParameter("status").equals("on")) {
                NotifdConfigFactory.getInstance().turnNotifdOn();
                sendEvent("uei.opennms.org/internal/notificationsTurnedOn");
            } else {
                NotifdConfigFactory.getInstance().turnNotifdOff();
                sendEvent("uei.opennms.org/internal/notificationsTurnedOff");
            }
        } catch (Throwable e) {
            new ServletException("Could not update notification status: " + e.getMessage(), e);
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/index.jsp");
        dispatcher.forward(request, response);
    }

    protected void sendEvent(String uei) {
        EventBuilder bldr = new EventBuilder(uei, "NotifdConfigFactory");
    
        try {
            EventIpcManagerFactory.getIpcManager().sendNow(bldr.getEvent());
        } catch (Throwable t) {
        }
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
   }
}
