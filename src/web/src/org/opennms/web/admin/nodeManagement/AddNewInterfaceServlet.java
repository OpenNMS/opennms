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
// ///07/2003 Use EventProxy to send event.
// 11/07/2003 Changed the new suspect event source.
// 11/28/2003 Created.
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
//      http://www.blast.com/
//

package org.opennms.web.admin.nodeManagement;

import java.io.IOException;
import java.util.Date;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.xml.event.Event;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.TcpEventProxy;

/**
 * A servlet that handles adding a new interface
 *
 * @author <A HREF="mailto:jamesz@blast.com">James Zuo</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class AddNewInterfaceServlet extends HttpServlet
{
        private static final String SQL_INTERFACE_EXIST = "SELECT nodeid FROM ipinterface WHERE ipaddr = ? "
                                                        + "AND ismanaged in ('M', 'A', 'U', 'F')"; 
        /**
         * The value used as the source of the event
         */
        final static String EVENT_SOURCE_VALUE = "Web UI";

        public void init() throws ServletException
        {
	        try
	        {
		        DatabaseConnectionFactory.init();
	        }
	        catch(Exception e)
	        {
		        throw new ServletException ("AddNewInterfaceServlet: Error initialising database connection factory." + e);
	        }

        }

        public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
        {
                int nodeId = -1;
       	        String ipAddress = request.getParameter("ipAddress");
	
                try 
                {
                        nodeId = getNodeId(ipAddress);
                } catch (SQLException sqlE)
                {
                        throw new ServletException("AddInterfaceServlet: failed to query if the ipaddress already exists", sqlE);
                }
        
                if (nodeId != -1)
                {
                        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/newInterface.jsp?action=redo");
                        dispatcher.forward( request, response);
                }
                else
                {
                        createAndSendNewSuspectInterfaceEvent(ipAddress);
	
	                //forward the request for proper display
                        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/interfaceAdded.jsp");
                        dispatcher.forward( request, response);
                }
        }

        private void createAndSendNewSuspectInterfaceEvent(String ipaddr)
                throws ServletException
        {
                Event event = new Event();
                event.setSource(EVENT_SOURCE_VALUE);
                event.setUei(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
                event.setInterface(ipaddr);

                try
                {
                        event.setHost(InetAddress.getLocalHost().getHostName());
                }
                catch (UnknownHostException uhE)
                {
                        event.setHost("unresolved.host");
                }

                event.setTime(EventConstants.formatToString(new java.util.Date()));
                
                try
                {
                        EventProxy eventProxy = new TcpEventProxy();
                        eventProxy.send(event);
                }
                catch (Exception e)
                {
                        throw new ServletException("Could not send event " + event.getUei(), e);
                }
        }

        private int getNodeId(String ipaddr) throws SQLException
        {
                int nodeId = -1;

                Connection conn = null;
                PreparedStatement stmt = null;

                try
                {
                        conn = DatabaseConnectionFactory.getInstance().getConnection();
                        stmt = conn.prepareStatement(SQL_INTERFACE_EXIST);
                        stmt.setString(1, ipaddr);
                        
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next())
                        {
                                nodeId = rs.getInt(1);
                        }
                }
                finally {
                        if (stmt != null)
                        {
                                try 
                                {
                                        stmt.close();
                                } catch (SQLException e1) {}
                        }
                        if (conn != null)
                        {
                                try 
                                {
                                        conn.close();
                                } catch (SQLException e2) {}
                        }
                        return nodeId;
                }
        }
    
}
