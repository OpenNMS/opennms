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
// 2007 Jun 24: Use Java 5 generics. - dj@opennms.org
// 2004 Jan 16: Order interface list by nodeid and ipaddress.
// 2004 Jan 06: Added support for STATUS_SUSPEND and STATUS_RESUME
// 2003 Feb 05: Added ORDER BY to SQL statement.
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

import org.opennms.netmgt.config.DataSourceFactory;

/**
 * A servlet that handles querying the database for node, interface, service
 * combinations
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class GetNodesServlet extends HttpServlet {

    private static final long serialVersionUID = 9083494959783285766L;

    private static final String INTERFACE_QUERY = "SELECT nodeid, ipaddr, isManaged FROM ipinterface WHERE ismanaged in ('M','A','U','F') AND ipaddr <> '0.0.0.0' ORDER BY nodeid, inet(ipaddr)";

    private static final String SERVICE_QUERY = "SELECT ifservices.serviceid, servicename, status FROM ifservices, service WHERE nodeid=? AND ipaddr=? AND status in ('A','U','F', 'S', 'R') AND ifservices.serviceid = service.serviceid ORDER BY servicename";

    public void init() throws ServletException {
        try {
            DataSourceFactory.init();
        } catch (Exception e) {
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession user = request.getSession(true);

        try {
            user.setAttribute("listAll.manage.jsp", getAllNodes(user));
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/manage.jsp");
        dispatcher.forward(request, response);
    }

    /**
     */
    private List<ManagedInterface> getAllNodes(HttpSession userSession) throws SQLException {
        Connection connection = null;
        List<ManagedInterface> allNodes = new ArrayList<ManagedInterface>();
        int lineCount = 0;

        try {
            connection = DataSourceFactory.getInstance().getConnection();
            PreparedStatement ifaceStmt = connection.prepareStatement(INTERFACE_QUERY);
            ResultSet ifaceResults = ifaceStmt.executeQuery();

            if (ifaceResults != null) {
                while (ifaceResults.next()) {
                    lineCount++;
                    ManagedInterface newInterface = new ManagedInterface();
                    allNodes.add(newInterface);
                    newInterface.setNodeid(ifaceResults.getInt(1));
                    newInterface.setAddress(ifaceResults.getString(2));

                    newInterface.setStatus(ifaceResults.getString(3));

                    PreparedStatement svcStmt = connection.prepareStatement(SERVICE_QUERY);
                    svcStmt.setInt(1, newInterface.getNodeid());
                    svcStmt.setString(2, newInterface.getAddress());

                    ResultSet svcResults = svcStmt.executeQuery();

                    if (svcResults != null) {
                        while (svcResults.next()) {
                            lineCount++;
                            ManagedService newService = new ManagedService();
                            newService.setId(svcResults.getInt(1));
                            newService.setName(svcResults.getString(2));
                            newService.setStatus(svcResults.getString(3));
                            newInterface.addService(newService);
                        }
                    }
                    svcResults.close();
                    svcStmt.close();
                }
            }
            ifaceResults.close();
            ifaceStmt.close();
            userSession.setAttribute("lineItems.manage.jsp", new Integer(lineCount));
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        return allNodes;
    }
}
