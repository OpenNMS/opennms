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
package org.opennms.web.admin.nodeManagement;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.Util;

/**
 * <p>DeleteInterfaceServlet class.</p>
 *
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 * @since 1.8.1
 */
public class DeleteInterfaceServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -6492975646540210281L;

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

        long nodeId = WebSecurityUtils.safeParseLong(request.getParameter("node"));
        String ipAddr = request.getParameter("intf");
        int ifIndex = -1;
        if (request.getParameter("ifindex") != null && request.getParameter("ifindex").length() != 0) {
            ifIndex = WebSecurityUtils.safeParseInt(request.getParameter("ifindex"));
        }

        Event e = EventUtils.createDeleteInterfaceEvent("OpenNMS.WebUI", nodeId, ipAddr, ifIndex, -1L);
        sendEvent(e);

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/interfaceDeleted.jsp");
        dispatcher.forward(request, response);

    }

    private void sendEvent(Event event) throws ServletException {
        try {
            Util.createEventProxy().send(event);
        } catch (Throwable e) {
            throw new ServletException("Could not send event " + event.getUei(), e);
        }
    }

    /**
     * <p>checkParameters</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     */
    public void checkParameters(HttpServletRequest request) {
        String nodeIdString = request.getParameter("node");
        String ipAddr = request.getParameter("intf");
        String ifIndex = request.getParameter("ifindex");

        if (nodeIdString == null) {
            throw new org.opennms.web.servlet.MissingParameterException("node", new String[] { "node", "intf or ifindex" });
        }

        if (ipAddr == null && ifIndex == null) {
            throw new org.opennms.web.servlet.MissingParameterException("intf or ifindex", new String[] { "node", "intf or ifindex" });
        }

    }

}
