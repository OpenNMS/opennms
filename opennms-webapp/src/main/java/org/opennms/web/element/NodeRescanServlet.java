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
package org.opennms.web.element;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;
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
