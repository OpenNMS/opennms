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
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.WebSecurityUtils;

/**
 * A servlet that manages the pathOutage table in the DB
 *
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class SetCriticalPathServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -5363097208855224954L;
    private static final String SQL_SET_CRITICAL_PATH = "INSERT INTO pathoutage (nodeid, criticalpathip, criticalpathservicename) VALUES (?, ?, ?)";
    private static final String SQL_DELETE_CRITICAL_PATH = "DELETE FROM pathoutage WHERE nodeid=?";

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nodeString = request.getParameter("node");
        String criticalIp = InetAddressUtils.normalize(request.getParameter("criticalIp"));
        String criticalSvc = request.getParameter("criticalSvc");
        String task = request.getParameter("task");
        int node = -1;
        try {
            node = WebSecurityUtils.safeParseInt(nodeString);
        } catch (NumberFormatException numE)  {
            throw new ServletException(numE);
        }

        if (task.equals("Delete")) {
            try {
                deleteCriticalPath(node);
            } catch (SQLException e) {
                throw new ServletException("SetCriticalPathServlet: Error writing to database." + e);
            }
        } else if (task.equals("Submit")) {
            try {
            setCriticalPath(node, criticalIp, criticalSvc);
            } catch (SQLException e) {
                throw new ServletException("SetCriticalPathServlet: Error writing to database." + e);
            }
        } else {
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/nodemanagement/setPathOutage.jsp?node=" + node + "&task=Requested operation " + task + " not understood.");
            dispatcher.forward(request, response);
            return;
        }
	   
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/nodemanagement/index.jsp?node=" + node);
        dispatcher.forward(request, response);
    }

    private void deleteCriticalPath(int node) throws SQLException {

        final DBUtils d = new DBUtils(getClass());

        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_CRITICAL_PATH);
            d.watch(stmt);
            stmt.setInt(1, node);
            stmt.executeUpdate();
        } finally {
            d.cleanUp();
        }
    }

    private void setCriticalPath(int node, String criticalIp, String criticalSvc) throws SQLException {

        deleteCriticalPath(node);

        final DBUtils d = new DBUtils(getClass());
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement(SQL_SET_CRITICAL_PATH);
            d.watch(stmt);
            stmt.setInt(1, node);
            stmt.setString(2, criticalIp);
            stmt.setString(3, criticalSvc);
            stmt.executeUpdate();
        } finally {
            d.cleanUp();
        }
    }
}
