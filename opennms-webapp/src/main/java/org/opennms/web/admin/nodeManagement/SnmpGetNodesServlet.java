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

package org.opennms.web.admin.nodeManagement;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
 * combinations for use in setting up SNMP data collection per interface
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class SnmpGetNodesServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -9025129128736990895L;

    private static final String SNMP_SERVICE_QUERY = "SELECT serviceid FROM service WHERE servicename = 'SNMP'";

    private static final String NODE_QUERY = "SELECT nodeid, nodelabel FROM node WHERE nodeid IN (SELECT nodeid FROM ifservices WHERE serviceid = ? ) AND nodeid IN (SELECT nodeid FROM ipinterface Where ismanaged != 'D') ORDER BY nodelabel, nodeid";

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
            user.setAttribute("listAllnodes.snmpmanage.jsp", getAllNodes(user));
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/snmpmanage.jsp");
        dispatcher.forward(request, response);
    }

    private List<SnmpManagedNode> getAllNodes(HttpSession userSession) throws SQLException {
        Connection connection = null;
        List<SnmpManagedNode> allNodes = new ArrayList<SnmpManagedNode>();
        int lineCount = 0;

        final DBUtils d = new DBUtils(getClass());
        try {
            connection = DataSourceFactory.getInstance().getConnection();
            d.watch(connection);
            int snmpServNum = 0;
            Statement servstmt = connection.createStatement();
            d.watch(servstmt);
            ResultSet snmpserv = servstmt.executeQuery(SNMP_SERVICE_QUERY);
            d.watch(snmpserv);
            if (snmpserv != null) {
                while (snmpserv.next()) {
                    snmpServNum = snmpserv.getInt(1);
                }
            }
            this.log("DEBUG: The SNMP service number is: " + snmpServNum);

            PreparedStatement stmt = connection.prepareStatement(NODE_QUERY);
            d.watch(stmt);
            stmt.setInt(1, snmpServNum);
            ResultSet nodeSet = stmt.executeQuery();
            d.watch(nodeSet);

            if (nodeSet != null) {
                while (nodeSet.next()) {
                    SnmpManagedNode newNode = new SnmpManagedNode();
                    newNode.setNodeID(nodeSet.getInt(1));
                    newNode.setNodeLabel(nodeSet.getString(2));
                    allNodes.add(newNode);

                }
            }
            userSession.setAttribute("lineNodeItems.snmpmanage.jsp", new Integer(lineCount));
        } finally {
            d.cleanUp();
        }

        return allNodes;
    }

}
