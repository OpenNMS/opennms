//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
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
//      http://www.blast.com/
//

package org.opennms.web.admin.nodeManagement;

import java.io.IOException;
import java.util.*;
import java.sql.*;
import java.sql.Connection;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.web.element.NetworkElementFactory;
import org.opennms.netmgt.config.*;

/**
 * A servlet that handles querying the database for node, interface, service combinations
 * for use in setting up SNMP data collection per interface
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class SnmpGetNodesServlet extends HttpServlet
{
	private static final String SNMP_SERVICE_QUERY = 
		"SELECT serviceid FROM service WHERE servicename = 'SNMP'";
	
	private static final String NODE_QUERY = 
		"SELECT nodeid, nodelabel FROM node WHERE nodeid IN (SELECT nodeid FROM ifservices WHERE serviceid = ? ) ORDER BY nodelabel, nodeid";
	
        private static final String INTERFACE_QUERY = 
                "SELECT ipinterface.nodeid, ipinterface.ipaddr, ipinterface.ifindex, ipinterface.iphostname, ipinterface.issnmpprimary, snmpinterface.snmpifdescr, snmpinterface.snmpiftype, snmpinterface.snmpifname FROM ipinterface, snmpinterface WHERE ipinterface.nodeid IN (SELECT ifservices.nodeid FROM ifservices WHERE ifservices.serviceid = ? ) AND snmpinterface.nodeid = ipinterface.nodeid AND snmpinterface.snmpifindex = ipinterface.ifindex"; 
	
        public void init() throws ServletException 
        {
                try {
                        DatabaseConnectionFactory.init();
                }
                catch( Exception e ) {}
        }
        
        public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
	{
		HttpSession user = request.getSession(true);
		
                try {
                        user.setAttribute("listAllnodes.snmpmanage.jsp", getAllNodes(user));
                        user.setAttribute("listAllinterfaces.snmpmanage.jsp", getAllInterfaces(user));
		} catch (SQLException e) {
                        throw new ServletException(e);
                }
                
		//forward the request for proper display
		RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/snmpmanage.jsp");
		dispatcher.forward( request, response );
	}
	
        private List getAllNodes(HttpSession userSession)
                throws SQLException
        {
                Connection connection = null;
                List allNodes = new ArrayList();
                int lineCount = 0;

                try
                {
                        connection = DatabaseConnectionFactory.getInstance().getConnection();
                        int snmpServNum = 0;
                        Statement servstmt = connection.createStatement();
                        ResultSet snmpserv = servstmt.executeQuery(SNMP_SERVICE_QUERY);
                        if (snmpserv != null)
                        {
                                while (snmpserv.next ())
                                {
                                        snmpServNum = snmpserv.getInt(1);
                                }
                        }
                        this.log("DEBUG: The SNMP service number is: " + snmpServNum);


                        PreparedStatement stmt = connection.prepareStatement(NODE_QUERY);
                        stmt.setInt(1, snmpServNum);
                        ResultSet nodeSet = stmt.executeQuery();

                        if (nodeSet != null)
                        {
                                while (nodeSet.next ())
                                {
                                        SnmpManagedNode newNode = new SnmpManagedNode();
                                        newNode.setNodeID(nodeSet.getInt(1));
                                        newNode.setNodeLabel(nodeSet.getString(2));
                                        allNodes.add(newNode);

                                }
                        }
                        userSession.setAttribute("lineNodeItems.snmpmanage.jsp", new Integer(lineCount));

                        nodeSet.close();
                }
                finally
                {
                        if(connection != null)
                        {
                                try {connection.close(); } catch(SQLException e) { }
                        }
                }

                return allNodes;
        }

        private List getAllInterfaces(HttpSession userSession)
                throws SQLException
        {
                Connection connection = null;
                List allInterfaces = new ArrayList();
                int lineCount = 0;

                try
                {
                        connection = DatabaseConnectionFactory.getInstance().getConnection();
                        int snmpServNum = 0;
                        Statement servstmt = connection.createStatement();
                        ResultSet snmpserv = servstmt.executeQuery(SNMP_SERVICE_QUERY);
                        if (snmpserv != null)
                        {
                                while (snmpserv.next ())
                                {
                                        snmpServNum = snmpserv.getInt(1);
                                }
                        }


                        PreparedStatement interfaceSelect = connection.prepareStatement(INTERFACE_QUERY);
                        interfaceSelect.setInt(1, snmpServNum);

                        ResultSet interfaceSet = interfaceSelect.executeQuery();

                        if (interfaceSet != null)
                        {
                                while(interfaceSet.next())
                                {
                                        lineCount++;
                                        SnmpManagedInterface newInterface = new SnmpManagedInterface();
                                        allInterfaces.add(newInterface);
                                        newInterface.setNodeid(interfaceSet.getInt(1));
                                        newInterface.setAddress(interfaceSet.getString(2));
                                        newInterface.setIfIndex(interfaceSet.getInt(3));
                                        newInterface.setIpHostname(interfaceSet.getString(4));
                                        newInterface.setStatus(interfaceSet.getString(5));
                                        newInterface.setIfDescr(interfaceSet.getString(6));
                                        newInterface.setIfType(interfaceSet.getInt(7));
                                        newInterface.setIfName(interfaceSet.getString(8));
                                }
                        }
                        interfaceSelect.close();
        	userSession.setAttribute("lineIntItems.snmpmanage.jsp", new Integer(lineCount));
		}
		finally
		{
        		if(connection != null)
        		{
                		try {connection.close(); } catch(SQLException e) { }
        		}
		}

	return allInterfaces;

	}
}

