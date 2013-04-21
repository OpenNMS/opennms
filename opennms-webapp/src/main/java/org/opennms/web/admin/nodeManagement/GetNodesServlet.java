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

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;

/**
 * A servlet that handles querying the database for node, interface, service
 * combinations
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class GetNodesServlet extends HttpServlet {

    private static final long serialVersionUID = 9083494959783285766L;

    // @ipv6 The regex in this statement is not IPv6-clean
    private static final String INTERFACE_QUERY = "SELECT nodeid, ipaddr, isManaged FROM ipinterface WHERE ismanaged in ('M','A','U','F') AND ipaddr <> '0.0.0.0' ORDER BY nodeid, case when (ipaddr ~ E'^([0-9]{1,3}\\.){3}[0-9]{1,3}$') then inet(ipaddr) else null end, ipaddr";

    private static final String SERVICE_QUERY = "SELECT ifservices.serviceid, servicename, status FROM ifservices, service WHERE nodeid=? AND ipaddr=? AND status in ('A','U','F', 'S', 'R') AND ifservices.serviceid = service.serviceid ORDER BY servicename";

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    public void init() throws ServletException {
        try {
            DataSourceFactory.init();
        } catch (Throwable e) {
        }
    }

    /** {@inheritDoc} */
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

        final DBUtils d = new DBUtils(getClass());
        try {
            connection = DataSourceFactory.getInstance().getConnection();
            d.watch(connection);
            PreparedStatement ifaceStmt = connection.prepareStatement(INTERFACE_QUERY);
            d.watch(ifaceStmt);
            ResultSet ifaceResults = ifaceStmt.executeQuery();
            d.watch(ifaceResults);

            if (ifaceResults != null) {
                while (ifaceResults.next()) {
                    lineCount++;
                    ManagedInterface newInterface = new ManagedInterface();
                    allNodes.add(newInterface);
                    newInterface.setNodeid(ifaceResults.getInt(1));
                    newInterface.setAddress(ifaceResults.getString(2));

                    newInterface.setStatus(ifaceResults.getString(3));

                    PreparedStatement svcStmt = connection.prepareStatement(SERVICE_QUERY);
                    d.watch(svcStmt);
                    svcStmt.setInt(1, newInterface.getNodeid());
                    svcStmt.setString(2, newInterface.getAddress());

                    ResultSet svcResults = svcStmt.executeQuery();
                    d.watch(svcResults);

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
                }
            }
            userSession.setAttribute("lineItems.manage.jsp", Integer.valueOf(lineCount));
        } finally {
            d.cleanUp();
        }

        return allNodes;
    }
}
