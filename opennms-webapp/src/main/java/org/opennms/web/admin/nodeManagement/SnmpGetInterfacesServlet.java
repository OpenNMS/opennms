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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.WebSecurityUtils;

/**
 * A servlet that handles querying the database for node, interface, service
 * combinations for use in setting up SNMP data collection per interface
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class SnmpGetInterfacesServlet extends HttpServlet {

    private static final long serialVersionUID = -5538965497977581230L;

    private static final String INTERFACE_QUERY = "SELECT " +
        "snmpinterface.nodeid, " +
        "snmpinterface.snmpifindex, " +
        "ipinterface.iphostname, " +
        "ipinterface.issnmpprimary, " +
        "snmpinterface.snmpifdescr, " +
        "snmpinterface.snmpiftype, " +
        "snmpinterface.snmpifname, " +
        "snmpinterface.snmpifalias, " +
        "snmpinterface.snmpcollect, " +
        "snmpinterface.id " +
        "FROM snmpinterface LEFT JOIN ipinterface " +
        "ON ipinterface.snmpinterfaceid=snmpinterface.id " +
        "WHERE snmpinterface.nodeid=?";

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
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession user = request.getSession(true);

        String nodeIdString = request.getParameter( "node" );

        if( nodeIdString == null ) {
            throw new org.opennms.web.servlet.MissingParameterException( "node" );
        }

        int nodeid = WebSecurityUtils.safeParseInt( nodeIdString );

        try {
            user.setAttribute("listInterfacesForNode.snmpselect.jsp", getNodeInterfaces(user,nodeid));
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/snmpselect.jsp");
        dispatcher.forward(request, response);
    }

    private List<SnmpManagedInterface> getNodeInterfaces(HttpSession userSession, int nodeid) throws SQLException {
        Connection connection = null;
        List<SnmpManagedInterface> nodeInterfaces = new ArrayList<SnmpManagedInterface>();

        final DBUtils d = new DBUtils(getClass());
        try {
            connection = DataSourceFactory.getInstance().getConnection();
            d.watch(connection);

            PreparedStatement interfaceSelect = connection.prepareStatement(INTERFACE_QUERY);
            d.watch(interfaceSelect);
            interfaceSelect.setInt(1, nodeid);

            ResultSet interfaceSet = interfaceSelect.executeQuery();
            d.watch(interfaceSet);

            if (interfaceSet != null) {
                while (interfaceSet.next()) {
                    SnmpManagedInterface newInterface = new SnmpManagedInterface();
                    nodeInterfaces.add(newInterface);
                    newInterface.setNodeid(interfaceSet.getInt(1));
                    newInterface.setIfIndex(interfaceSet.getInt(2));
                    newInterface.setIpHostname(interfaceSet.getString(3));
                    newInterface.setStatus(interfaceSet.getString(4));
                    newInterface.setIfDescr(interfaceSet.getString(5));
                    newInterface.setIfType(interfaceSet.getInt(6));
                    newInterface.setIfName(interfaceSet.getString(7));
                    newInterface.setIfAlias(interfaceSet.getString(8));
                    newInterface.setCollectFlag(interfaceSet.getString(9));
                    newInterface.setSnmpInterfaceId(interfaceSet.getInt(10));
                }
            }
        } finally {
            d.cleanUp();
        }

        Collections.sort(nodeInterfaces);
        return nodeInterfaces;

    }
}
