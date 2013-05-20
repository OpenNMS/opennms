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
import org.opennms.netmgt.EventConstants;
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
    final static String EVENT_SOURCE_VALUE = "Web UI";

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    @Override
    public void init() throws ServletException {
        try {
            DataSourceFactory.init();
        } catch (Throwable e) {
            throw new ServletException("AddNewInterfaceServlet: Error initialising database connection factory." + e);
        }

    }

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
