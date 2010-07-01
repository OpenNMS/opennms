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
// 2007 Jun 24: Add serialVersionUID. - dj@opennms.org
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
import java.lang.String;
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
import org.opennms.web.WebSecurityUtils;

/**
 * A servlet that manages the pathOutage table in the DB
 *
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class SetCriticalPathServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static final String SQL_SET_CRITICAL_PATH = "INSERT INTO pathoutage (nodeid, criticalpathip, criticalpathservicename) VALUES (?, ?, ?)";
    private static final String SQL_DELETE_CRITICAL_PATH = "DELETE FROM pathoutage WHERE nodeid=?";

    /** {@inheritDoc} */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nodeString = request.getParameter("node");
        String criticalIp = request.getParameter("criticalIp");
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
