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
        public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
        {
                HttpSession userSession = request.getSession(false);

                //the list of all nodes marked for deletion
                java.util.List nodeList = getList(request.getParameterValues("nodeCheck"));

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
                nodeDeleted.setUei("http://uei.opennms.org/products/bluebird/nodes/nodeDeleted");
                nodeDeleted.setSource("web ui");
                nodeDeleted.setNodeid(node);
                nodeDeleted.setTime(EventConstants.formatToString(new java.util.Date()));

                sendEvent(nodeDeleted);
        }

        private void sendServiceDeletedEvent(int node, String iface, String svcname)
                throws ServletException
        {
                Event serviceDeleted = new Event();
                serviceDeleted.setUei("http://uei.opennms.org/products/bluebird/nodes/deleteService");
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
	
}
