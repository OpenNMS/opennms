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
// 2002 Nov 12: Added the ability to delete data dirs when deleting nodes.
// 2002 Nov 10: Removed "http://" from UEIs and references to bluebird.
// 2002 Oct 22: Removed the need for a restart.
// 2002 Sep 19: Added delete node page based on manage/unmanage node page.
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
import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.core.resource.Vault;
import org.opennms.netmgt.config.*;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.TcpEventProxy;
import org.opennms.netmgt.xml.event.*;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.netmgt.config.EventconfFactory;

import org.opennms.netmgt.EventConstants;

/**
 * A servlet that handles deleting nodes from the database
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class DeleteNodesServlet extends HttpServlet
{

	public void init() 
		throws ServletException
	{
		try
		{
                        DatabaseConnectionFactory.init();
		}
		catch(Exception e)
		{
			throw new ServletException("Could not initialize database factory: " + e.getMessage(), e);
		}


	}
    	public static final String RRDTOOL_SNMP_GRAPH_PROPERTIES_FILENAME = "/etc/snmp-graph.properties";
    	public static final String RRDTOOL_RT_GRAPH_PROPERTIES_FILENAME = "/etc/response-graph.properties";

    	protected Properties snmpProps;
    	protected File snmpRrdDirectory;
    	protected Properties rtProps;
    	protected File rtRrdDirectory;


        public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
        {
                HttpSession userSession = request.getSession(false);

                //the list of all nodes marked for deletion
                java.util.List nodeList = getList(request.getParameterValues("nodeCheck"));
                java.util.List nodeDataList = getList(request.getParameterValues("nodeData"));

		//get the directories storing the response time and SNMP data
        	this.snmpProps = new java.util.Properties();
        	this.snmpProps.load( new FileInputStream( Vault.getHomeDir() + RRDTOOL_SNMP_GRAPH_PROPERTIES_FILENAME ));

        	this.snmpRrdDirectory = new File( this.snmpProps.getProperty( "command.input.dir" ));

                this.rtProps = new java.util.Properties();
                this.rtProps.load( new FileInputStream( Vault.getHomeDir() + RRDTOOL_RT_GRAPH_PROPERTIES_FILENAME ));

                this.rtRrdDirectory = new File( this.rtProps.getProperty( "command.input.dir" ));


		//delete data directories if desired
                for (int j = 0; j < nodeDataList.size(); j++)
                {
			//SNMP RRD directory
        		File nodeDir = new File(this.snmpRrdDirectory, (String)nodeDataList.get(j));

        		if(nodeDir.exists() && nodeDir.isDirectory()) 
			{
                        	this.log("DEBUG: Attempting to Delete Node Data Directory: " + nodeDir.getAbsolutePath());
				if(deleteDir(nodeDir))
                        		this.log("DEBUG: Node Data Directory Deleted Successfully");
			}
        		StringBuffer select = new StringBuffer("SELECT DISTINCT ipaddr FROM ipinterface WHERE nodeid=");

        		select.append((String)nodeDataList.get(j));
			
			try
			{
        			Connection conn = Vault.getDbConnection();
        			ArrayList intfs = new ArrayList();

        			try
        			{
            				Statement stmt = conn.createStatement();
            				ResultSet rs = stmt.executeQuery(select.toString());

            				while( rs.next() )
            				{
                				String ipAddr = rs.getString("ipaddr");
						//Response Time RRD directory
                				File intfDir = new File(this.rtRrdDirectory, ipAddr);

                				if(intfDir.exists() && intfDir.isDirectory())
                				{
                                			this.log("DEBUG: Attempting to Delete Node Response Time Data Directory: " + intfDir.getAbsolutePath());
                                			if(deleteDir(intfDir))
                                        			this.log("DEBUG: Node Response Time Data Directory Deleted Successfully");
                				}
            				}
            				rs.close();
            				stmt.close();
        			}
        			finally
        			{
            				Vault.releaseDbConnection(conn);
        			}
        		}
                	catch (SQLException e)
                	{
                       		throw new ServletException("There was a problem with the database connection: " + e.getMessage(), e);
                	}
		}

		// Now, Delete the node from the database
                try
                {
                        Connection connection = Vault.getDbConnection();
                        try
                        {
                                connection.setAutoCommit(false);

				for ( int s = 0; s < nodeList.size(); s++)
				{
					int nodeid = Integer.parseInt((String)nodeList.get(s));
					StringBuffer squery = new StringBuffer("SELECT ifservices.ipaddr, service.servicename FROM ifservices, service WHERE nodeID = ");
					squery.append((String)nodeList.get(s));
					squery.append(" AND ifservices.serviceid=service.serviceid");
					Statement supdate = connection.createStatement();
					ResultSet rs = supdate.executeQuery(squery.toString());
					while (rs.next())
					{
						String iface = rs.getString(1);
						String svcname = rs.getString(2);
						sendServiceDeletedEvent(nodeid, iface, svcname);
					}
				}							

                                for (int j = 0; j < nodeList.size(); j++)
					{
                                        this.log("DEBUG: Starting Delete of Node Number: " + nodeList.get(j));


                			StringBuffer query1 = new StringBuffer("DELETE from usersNotified where notifyID in (select notifyID from notifications where nodeID = ");
                			query1.append((String)nodeList.get(j));
                			query1.append(")");
                			Statement update1 = connection.createStatement();
                			update1.executeUpdate(query1.toString());
                			update1.close();
                			StringBuffer query2 = new StringBuffer("DELETE from notifications where nodeID = ");
                			query2.append((String)nodeList.get(j));
                			Statement update2 = connection.createStatement();
                			update2.executeUpdate(query2.toString());
                			update2.close();
                			StringBuffer query3 = new StringBuffer("DELETE from outages where nodeID = ");
                			query3.append((String)nodeList.get(j));
                			Statement update3 = connection.createStatement();
                			update3.executeUpdate(query3.toString());
                			update3.close();
                			StringBuffer query4 = new StringBuffer("DELETE from events where nodeID = ");
                			query4.append((String)nodeList.get(j));
                			Statement update4 = connection.createStatement();
                			update4.executeUpdate(query4.toString());
                			update4.close();
                			StringBuffer query5 = new StringBuffer("DELETE from ifServices where nodeID = ");
                			query5.append((String)nodeList.get(j));
                			Statement update5 = connection.createStatement();
                			update5.executeUpdate(query5.toString());
                			update5.close();
                			StringBuffer query6 = new StringBuffer("DELETE from ipInterface where nodeID = ");
                			query6.append((String)nodeList.get(j));
                			Statement update6 = connection.createStatement();
                			update6.executeUpdate(query6.toString());
                			update6.close();
                			StringBuffer query7 = new StringBuffer("DELETE from snmpInterface where nodeID = ");
                			query7.append((String)nodeList.get(j));
                			Statement update7 = connection.createStatement();
                			update7.executeUpdate(query7.toString());
                			update7.close();
                			StringBuffer query8 = new StringBuffer("DELETE from assets where nodeID = ");
                			query8.append((String)nodeList.get(j));
                			Statement update8 = connection.createStatement();
                			update8.executeUpdate(query8.toString());
                			update8.close();
                			StringBuffer query9 = new StringBuffer("DELETE from node where nodeID = ");
                			query9.append((String)nodeList.get(j));
                			Statement update9 = connection.createStatement();
                			update9.executeUpdate(query9.toString());
                			update9.close();

					int currNodeId = Integer.parseInt((String)nodeList.get(j));
					sendNodeDeletedEvent(currNodeId);

                                        this.log("DEBUG: End of Delete of Node Number: " + nodeList.get(j));

                        		} //end j loop
				connection.commit();
			}
                        finally
                        {       //close off the db connection
                                connection.setAutoCommit(true);
                                Vault.releaseDbConnection(connection);
                        }
		}
                catch (SQLException e)
                {
			throw new ServletException("There was a problem with the database connection: " + e.getMessage(), e);
                }


                //forward the request for proper display
                RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/deleteNodesFinish.jsp");
                dispatcher.forward( request, response );

        }

        private void sendNodeDeletedEvent(int node)
                throws ServletException
        {
                Event nodeDeleted = new Event();
                nodeDeleted.setUei("uei.opennms.org/nodes/nodeDeleted");
                nodeDeleted.setSource("web ui");
                nodeDeleted.setNodeid(node);
                nodeDeleted.setTime(EventConstants.formatToString(new java.util.Date()));

                sendEvent(nodeDeleted);
        }

        private void sendServiceDeletedEvent(int node, String iface, String svcname)
                throws ServletException
        {
                Event serviceDeleted = new Event();
                serviceDeleted.setUei("uei.opennms.org/nodes/deleteService");
                serviceDeleted.setSource("web ui");
                serviceDeleted.setNodeid(node);
                serviceDeleted.setInterface(iface);
                serviceDeleted.setService(svcname);
                serviceDeleted.setTime(EventConstants.formatToString(new java.util.Date()));

                sendEvent(serviceDeleted);
        }

        private void sendEvent(Event event)
                throws ServletException
        {
                try
                {
                        EventProxy eventProxy = new TcpEventProxy();
                                eventProxy.send(event);
                }
                catch(Exception e)
                {
                        throw new ServletException("Could not send event " + event.getUei(), e);
                }
        }



	private java.util.List getList(String array[])
	{
		java.util.List newList = new ArrayList();
		
		if (array != null)
		{
			for (int i = 0; i < array.length; i++)
			{
				newList.add(array[i]);
			}
		}
		
		return newList;
	}
    	// Deletes all files and subdirectories under dir.
    	// Returns true if all deletions were successful.
    	// If a deletion fails, the method stops attempting to delete and returns false.
    	public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
        	String[] children = dir.list();
            	for (int i=0; i<children.length; i++) {
                	boolean success = deleteDir(new File(dir, children[i]));
                	if (!success) {
                    		return false;
                	}
            	}
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }

	
}
