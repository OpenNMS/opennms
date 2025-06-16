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
