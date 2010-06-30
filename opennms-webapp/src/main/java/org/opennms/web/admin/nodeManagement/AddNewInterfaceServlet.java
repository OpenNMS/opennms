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
// Modifications:
//
// 2007 Jun 24: Add serialVersionUID and organize imports. - dj@opennms.org
// 2003 Nov 07: Use EventProxy to send event.
// 2003 Nov 07: Changed the new suspect event source.
// 2003 Nov 28: Created.
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

package org.opennms.web.admin.nodeManagement;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.Util;

/**
 * A servlet that handles adding a new interface
 *
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.6.12
 */
public class AddNewInterfaceServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

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
    public void init() throws ServletException {
        try {
            DataSourceFactory.init();
        } catch (Exception e) {
            throw new ServletException("AddNewInterfaceServlet: Error initialising database connection factory." + e);
        }

    }

    /** {@inheritDoc} */
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
        Event event = new Event();
        event.setSource(EVENT_SOURCE_VALUE);
        event.setUei(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
        event.setInterface(ipaddr);

        try {
            event.setHost(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException uhE) {
            event.setHost("unresolved.host");
        }

        event.setTime(EventConstants.formatToString(new java.util.Date()));

        try {
            Util.createEventProxy().send(event);
        } catch (Exception e) {
            throw new ServletException("Could not send event " + event.getUei(), e);
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
