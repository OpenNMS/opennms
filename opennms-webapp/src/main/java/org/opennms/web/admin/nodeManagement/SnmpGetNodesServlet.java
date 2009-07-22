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
// 2007 Jun 25: Add serialVersionUID and use Java 5 generics. - dj@opennms.org
// 2002 Sep 24: Added the ability to select SNMP interfaces for collection.
//              Code based on original manage/unmanage code.
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
import java.sql.Statement;
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

/**
 * A servlet that handles querying the database for node, interface, service
 * combinations for use in setting up SNMP data collection per interface
 * 
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class SnmpGetNodesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String SNMP_SERVICE_QUERY = "SELECT serviceid FROM service WHERE servicename = 'SNMP'";

    private static final String NODE_QUERY = "SELECT nodeid, nodelabel FROM node WHERE nodeid IN (SELECT nodeid FROM ifservices WHERE serviceid = ? ) AND nodeid IN (SELECT nodeid FROM ipinterface Where ismanaged != 'D') ORDER BY nodelabel, nodeid";

    public void init() throws ServletException {
        try {
            DataSourceFactory.init();
        } catch (Exception e) {
        }
    }

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

    private List getAllNodes(HttpSession userSession) throws SQLException {
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
