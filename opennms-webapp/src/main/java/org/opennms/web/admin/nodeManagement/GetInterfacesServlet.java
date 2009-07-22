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
// 2007 Jun 24: Add serialVersionUID and use Java 5 generics. - dj@opennms.org
//
// Modifications:
//
// 2004 Jan 16: Created.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.web.WebSecurityUtils;

/**
 * A servlet that handles querying the database for node, interface, service
 * combinations
 * 
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class GetInterfacesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String INTERFACE_QUERY = "SELECT ipaddr, isManaged FROM ipinterface " + "WHERE nodeid=? " + "AND ismanaged IN ('M','A','U','F') " + "AND ipaddr <> '0.0.0.0' " + "ORDER BY inet(ipaddr)";

    private static final String SERVICE_QUERY = "SELECT ifservices.serviceid, servicename, status FROM ifservices, service " + "WHERE nodeid=? AND ipaddr=? AND status IN ('A','U','F', 'S', 'R') " + "AND ifservices.serviceid = service.serviceid ORDER BY servicename";

    public void init() throws ServletException {
        try {
            DataSourceFactory.init();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int nodeId = -1;
        String nodeIdString = request.getParameter("node");

        if (nodeIdString == null)
            throw new ServletException("Missing node Id.");

        HttpSession user = request.getSession(true);

        try {
            nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
        } catch (NumberFormatException numE) {
            throw new ServletException(numE.getMessage());
        }

        if (nodeId < 0)
            throw new ServletException("Invalid node ID.");

        try {
            user.setAttribute("interfaces.nodemanagement", getInterfaces(user, nodeId));
        } catch (SQLException sqlE) {
            throw new ServletException(sqlE);
        } catch (IllegalStateException illE) {
            throw new ServletException(illE);
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/nodemanagement/managenode.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * <p>
     * Retrieve all the interfaces and services from the database, and keep them
     * in the user session.
     * 
     * @param userSession
     *            Current user working session
     * @param nodeId
     *            Id of the node to manage
     */
    private List<ManagedInterface> getInterfaces(HttpSession userSession, int nodeId) throws SQLException {
        Connection connection = null;
        List<ManagedInterface> allInterfaces = new ArrayList<ManagedInterface>();
        int lineCount = 0;

        final DBUtils d = new DBUtils(getClass());
        try {
            connection = DataSourceFactory.getInstance().getConnection();
            d.watch(connection);

            PreparedStatement ifaceStmt = connection.prepareStatement(INTERFACE_QUERY);
            d.watch(ifaceStmt);
            ifaceStmt.setInt(1, nodeId);

            ResultSet ifaceResults = ifaceStmt.executeQuery();
            d.watch(ifaceResults);
            while (ifaceResults.next()) {
                lineCount++;
                ManagedInterface newInterface = new ManagedInterface();
                newInterface.setNodeid(nodeId);
                newInterface.setAddress(ifaceResults.getString(1));
                newInterface.setStatus(ifaceResults.getString(2));
                allInterfaces.add(newInterface);

                PreparedStatement svcStmt = connection.prepareStatement(SERVICE_QUERY);
                d.watch(svcStmt);
                svcStmt.setInt(1, nodeId);
                svcStmt.setString(2, newInterface.getAddress());

                ResultSet svcResults = svcStmt.executeQuery();
                d.watch(svcResults);
                while (svcResults.next()) {
                    lineCount++;
                    ManagedService newService = new ManagedService();
                    newService.setId(svcResults.getInt(1));
                    newService.setName(svcResults.getString(2));
                    newService.setStatus(svcResults.getString(3));
                    newInterface.addService(newService);
                }
            }
            userSession.setAttribute("lineItems.nodemanagement", new Integer(lineCount));
        } finally {
            d.cleanUp();
        }
        return allInterfaces;
    }
}
