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

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.api.Util;

/**
 * A servlet that handles adding a new interface
 *
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class AddNewInterfaceServlet extends HttpServlet {
    private static final long serialVersionUID = 8246413657214969476L;

    private static final String SQL_INTERFACE_EXIST = "SELECT nodeid FROM ipinterface WHERE ipaddr = ? " + "AND ismanaged in ('M', 'A', 'U', 'F')";

    /**
     * The value used as the source of the event
     */
    static final String EVENT_SOURCE_VALUE = "Web UI";

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int nodeId = -1;
        String ipAddress = request.getParameter("ipAddress");

        try {
            nodeId = getNodeId(ipAddress);
        } catch (SQLException sqlE) {
            throw new ServletException("AddInterfaceServlet: failed to query if the ipaddress already exists", sqlE);
        }

        if (nodeId != -1) {
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/newInterface.jsp?action=redo");
            dispatcher.forward(request, response);
        } else {
            createAndSendNewSuspectInterfaceEvent(ipAddress);

            // forward the request for proper display
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/interfaceAdded.jsp");
            dispatcher.forward(request, response);
        }
    }

    private void createAndSendNewSuspectInterfaceEvent(String ipaddr) throws ServletException {
        EventBuilder bldr = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, EVENT_SOURCE_VALUE);
        bldr.setInterface(addr(ipaddr));
        bldr.setHost(InetAddressUtils.getLocalHostName());


        try {
            Util.createEventProxy().send(bldr.getEvent());
        } catch (Throwable e) {
            throw new ServletException("Could not send event " + bldr.getEvent().getUei(), e);
        }
    }

    private int getNodeId(String ipaddr) throws SQLException {
        int nodeId = -1;

        Connection conn = null;
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());

        try {
            conn = DataSourceFactory.getInstance().getConnection();
            d.watch(conn);
            stmt = conn.prepareStatement(SQL_INTERFACE_EXIST);
            d.watch(stmt);
            stmt.setString(1, ipaddr);

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            if (rs.next()) {
                nodeId = rs.getInt(1);
            }
            return nodeId;
        } finally {
            d.cleanUp();
        }
    }

}
