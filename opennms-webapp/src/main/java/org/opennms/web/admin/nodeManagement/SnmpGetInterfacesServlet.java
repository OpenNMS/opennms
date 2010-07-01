//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Feb 27: Updated to be aware of the snmpInterface snmpCollect column
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.web.WebSecurityUtils;

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
public class SnmpGetInterfacesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static final String INTERFACE_QUERY = "SELECT " +
        "snmpinterface.nodeid, " +
        "snmpinterface.ipaddr, " +
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
        } catch (Exception e) {
        }
    }
    
    /** {@inheritDoc} */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession user = request.getSession(true);

        String nodeIdString = request.getParameter( "node" );

        if( nodeIdString == null ) {
            throw new org.opennms.web.MissingParameterException( "node" );
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
        int lineCount = 0;

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
                    lineCount++;
                    SnmpManagedInterface newInterface = new SnmpManagedInterface();
                    nodeInterfaces.add(newInterface);
                    newInterface.setNodeid(interfaceSet.getInt(1));
                    newInterface.setAddress(interfaceSet.getString(2));
                    newInterface.setIfIndex(interfaceSet.getInt(3));
                    newInterface.setIpHostname(interfaceSet.getString(4));
                    newInterface.setStatus(interfaceSet.getString(5));
                    newInterface.setIfDescr(interfaceSet.getString(6));
                    newInterface.setIfType(interfaceSet.getInt(7));
                    newInterface.setIfName(interfaceSet.getString(8));
                    newInterface.setIfAlias(interfaceSet.getString(9));
                    newInterface.setCollectFlag(interfaceSet.getString(10));
                    newInterface.setSnmpInterfaceId(interfaceSet.getInt(11));
                }
            }
        } finally {
            d.cleanUp();
        }

        Collections.sort(nodeInterfaces);
        return nodeInterfaces;

    }
}
