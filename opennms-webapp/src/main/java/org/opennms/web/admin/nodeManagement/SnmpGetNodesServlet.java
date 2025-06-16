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
 */
public class SnmpGetNodesServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -9025129128736990895L;

    private static final String SNMP_SERVICE_QUERY = "SELECT serviceid FROM service WHERE servicename = 'SNMP'";

    private static final String NODE_QUERY = "SELECT DISTINCT node.nodeid, node.nodelabel FROM node, ipinterface, ifservices WHERE node.nodeid = ipinterface.nodeid AND ipinterface.id = ifservices.ipinterfaceid AND ifservices.serviceid = ? AND ipinterface.ismanaged != 'D' ORDER BY node.nodelabel, node.nodeid";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    /** {@inheritDoc} */
    @Override
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
        List<SnmpManagedNode> allNodes = new ArrayList<>();
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
            userSession.setAttribute("lineNodeItems.snmpmanage.jsp", Integer.valueOf(lineCount));
        } finally {
            d.cleanUp();
        }

        return allNodes;
    }

}
