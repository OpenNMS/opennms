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

/**
 * A servlet that handles querying the database for node information
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class DeleteGetNodesServlet extends HttpServlet {
    private static final long serialVersionUID = -34219022712246261L;

    private static final String NODE_QUERY =
    // "SELECT nodeid, nodelabel FROM node ORDER BY nodelabel, nodeid";
    "SELECT nodeid, nodelabel FROM node WHERE nodetype != 'D' ORDER BY nodelabel, nodeid";

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
            user.setAttribute("listAll.delete.jsp", getAllNodes(user));
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/delete.jsp");
        dispatcher.forward(request, response);
    }

    /**
     */
    private List<ManagedNode> getAllNodes(HttpSession userSession) throws SQLException {
        Connection connection = null;
        List<ManagedNode> allNodes = new ArrayList<ManagedNode>();
        int lineCount = 0;

        try {
            connection = DataSourceFactory.getInstance().getConnection();

            Statement stmt = connection.createStatement();
            ResultSet nodeSet = stmt.executeQuery(NODE_QUERY);

            if (nodeSet != null) {
                while (nodeSet.next()) {
                    ManagedNode newNode = new ManagedNode();
                    newNode.setNodeID(nodeSet.getInt(1));
                    newNode.setNodeLabel(nodeSet.getString(2));
                    allNodes.add(newNode);

                }
            }
            // FIXME: linecount never modified???
            userSession.setAttribute("lineItems.delete.jsp", Integer.valueOf(lineCount));

            nodeSet.close();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }

        return allNodes;
    }
}
