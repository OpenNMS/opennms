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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    /** {@inheritDoc} */
    @Override
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
        List<ManagedNode> allNodes = new ArrayList<>();
        int lineCount = 0;

        try (
            final Connection connection = DataSourceFactory.getInstance().getConnection();
            final Statement stmt = connection.createStatement();
            final ResultSet nodeSet = stmt.executeQuery(NODE_QUERY);
        ) {
            while (nodeSet.next()) {
                ManagedNode newNode = new ManagedNode();
                newNode.setNodeID(nodeSet.getInt(1));
                newNode.setNodeLabel(nodeSet.getString(2));
                allNodes.add(newNode);
                lineCount++;
            }
            userSession.setAttribute("lineItems.delete.jsp", Integer.valueOf(lineCount));
        }

        return allNodes;
    }
}
