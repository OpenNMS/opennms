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
// 2004 Jan 16: Created.
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.netmgt.config.DatabaseConnectionFactory;

/**
 * A servlet that handles querying the database for node, interface, service combinations
 *
 * @author <A HREF="mailto:jamesz@blast.com">James Zuo</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class GetInterfacesServlet extends HttpServlet
{
        private static final String INTERFACE_QUERY = 
                "SELECT ipaddr, isManaged FROM ipinterface " +
                "WHERE nodeid=? " +
                "AND ismanaged IN ('M','A','U','F') " +
                "AND ipaddr <> '0.0.0.0' " +
                "ORDER BY inet(ipaddr)";
        
        private static final String SERVICE_QUERY =
                "SELECT ifservices.serviceid, servicename, status FROM ifservices, service " +
                "WHERE nodeid=? AND ipaddr=? AND status IN ('A','U','F', 'S', 'R') " +
                "AND ifservices.serviceid = service.serviceid ORDER BY servicename";
	
        public void init() throws ServletException 
        {
                try {
                        DatabaseConnectionFactory.init();
                }
                catch( Exception e ) 
                {
                        throw new ServletException(e);
                }
        }
        
        public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
	{
                int nodeId = -1;
                String nodeIdString = request.getParameter("node");

                if ( nodeIdString == null)
                        throw new ServletException("Missing node Id.");
                        
		HttpSession user = request.getSession(true);
		
                try
                {
                        nodeId = Integer.parseInt(nodeIdString);
                }
                catch (NumberFormatException numE)
                {
                        throw new ServletException(numE.getMessage());
                }
                
                if (nodeId < 0)
                        throw new ServletException("Invalid node ID.");
                
                try 
                {
                        user.setAttribute("interfaces.nodemanagement", getInterfaces(user, nodeId));
		} catch (SQLException sqlE) {
                        throw new ServletException(sqlE);
                } catch (IllegalStateException illE) {
                        throw new ServletException(illE);
                }                
                
		//forward the request for proper display
		RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/nodemanagement/managenode.jsp");
		dispatcher.forward( request, response );
	}
	
	/**
         * <p>Retrieve all the interfaces and services from the database, and keep them in
         * the user session.
         *
         * @param userSession   Current user working session
         * @param nodeId        Id of the node to manage
         */
	private List getInterfaces(HttpSession userSession, int nodeId)	throws SQLException
	{
		Connection connection = null;
		List allInterfaces = new ArrayList();
                int lineCount = 0;
                
		try
		{
			connection = DatabaseConnectionFactory.getInstance().getConnection();
			
                        PreparedStatement interfaceSelect = connection.prepareStatement(INTERFACE_QUERY);
                        interfaceSelect.setInt(1, nodeId);
                        
                        ResultSet interfaceSet = interfaceSelect.executeQuery();
                        while(interfaceSet.next())
                        {
                                lineCount++;
                                ManagedInterface newInterface = new ManagedInterface();
                                allInterfaces.add(newInterface);
                                newInterface.setNodeid(nodeId);
                                newInterface.setAddress(interfaceSet.getString(1));
                                newInterface.setStatus(interfaceSet.getString(2));
                                
                                PreparedStatement serviceSelect = connection.prepareStatement(SERVICE_QUERY);
				serviceSelect.setInt(1, nodeId);
                                serviceSelect.setString(2, newInterface.getAddress());
                                
                                ResultSet serviceSet = serviceSelect.executeQuery();
                                while(serviceSet.next())
                                {
                                        lineCount++;
                                        ManagedService newService = new ManagedService();
                                        newService.setId(serviceSet.getInt(1));
						newService.setName(serviceSet.getString(2));
                                        newService.setStatus(serviceSet.getString(3));
                                        newInterface.addService(newService);
                                }
                                serviceSelect.close();
                        }
                        interfaceSelect.close();
                        userSession.setAttribute("lineItems.nodemanagement", new Integer(lineCount));
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
