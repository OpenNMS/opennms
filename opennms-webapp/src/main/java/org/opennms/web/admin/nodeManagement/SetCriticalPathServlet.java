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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.db.DataSourceFactory;
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

    private static void deleteCriticalPath(int node) throws SQLException {

        final DBUtils d = new DBUtils(SetCriticalPathServlet.class);

        try {
            Connection conn = DataSourceFactory.getInstance().getConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_CRITICAL_PATH);
            d.watch(stmt);
            stmt.setInt(1, node);
            stmt.executeUpdate();
        } finally {
            d.cleanUp();
        }
    }

    private static void setCriticalPath(int node, String criticalIp, String criticalSvc) throws SQLException {

        deleteCriticalPath(node);

        final DBUtils d = new DBUtils(SetCriticalPathServlet.class);
        try {
            Connection conn = DataSourceFactory.getInstance().getConnection();
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
