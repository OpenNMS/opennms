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
 * A servlet that handles managing or unmanaging interfaces and services on a node
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class SnmpManageNodesServlet extends HttpServlet
{
	private static final String UPDATE_INTERFACE = "UPDATE ipinterface SET issnmpprimary = ? WHERE nodeid = ? AND ifindex = ?";
	
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
		
		try 
		{
			NotificationFactory.init();
		}
		catch (Exception e)
		{
			throw new ServletException("Could not initialize notification factory: " + e.getMessage(), e);
		}
	}
	
	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
	{
		HttpSession userSession = request.getSession(false);
		java.util.List allInterfaces = null;
		
		if (userSession != null)
		{
		  	allInterfaces = (java.util.List)userSession.getAttribute("listAllinterfaces.snmpmanage.jsp");
		}
		
		//the list of all interfaces marked as managed
		java.util.List interfaceList = getList(request.getParameterValues("collTypeCheck"));
		
		//the node being modified
        	String nodeIdString = request.getParameter("node");
		int currNodeId = Integer.parseInt(nodeIdString);

		
		// date to set on events sent out
		String curDate = EventConstants.formatToString(new java.util.Date());

		String primeInt = null;

		for (int k = 0; k < allInterfaces.size(); k++)
		{
			SnmpManagedInterface testInterface = (SnmpManagedInterface)allInterfaces.get(k);
			if (testInterface.getNodeid() == currNodeId && testInterface.getStatus().equals("P"))
				primeInt = testInterface.getAddress();
		} 
	
		
		try 
		{
			Connection connection = Vault.getDbConnection();
			try 
			{
				connection.setAutoCommit(false);
				PreparedStatement stmt = connection.prepareStatement(UPDATE_INTERFACE);
                        
				for (int j = 0; j < allInterfaces.size(); j++)
                        	{
					SnmpManagedInterface curInterface = (SnmpManagedInterface)allInterfaces.get(j);
					String intKey = curInterface.getNodeid()+"+"+curInterface.getIfIndex();
                                
				
					//determine what is managed and unmanged
					if (interfaceList.contains(intKey) && curInterface.getStatus().equals("N"))
					{
                                           	stmt.setString(1, "C");
                                                stmt.setInt(2, curInterface.getNodeid());
                                                stmt.setInt(3, curInterface.getIfIndex());
                                                this.log("DEBUG: executing SNMP Collection Type update to C for nodeid: " + curInterface.getNodeid() + " ifIndex: " + curInterface.getIfIndex());
                                                stmt.executeUpdate();
					}
					else if (!interfaceList.contains(intKey) && curInterface.getNodeid() == currNodeId && curInterface.getStatus().equals("C"))
					{
                                                stmt.setString(1, "N");
                                                stmt.setInt(2, curInterface.getNodeid());
                                                stmt.setInt(3, curInterface.getIfIndex());
                                                this.log("DEBUG: executing SNMP Collection Type update to N for nodeid: " + curInterface.getNodeid() + " ifIndex: " + curInterface.getIfIndex());
                                                stmt.executeUpdate();
                                        }

                                }
			        
				connection.commit();
			}
			finally
			{	//close off the db connection
				connection.setAutoCommit(true);
				Vault.releaseDbConnection(connection);
			}
		}
		catch (SQLException e)
		{
			throw new ServletException(e);
		}
		
		//send the event to restart SNMP Collection
		if (primeInt != null)
		sendSNMPRestartEvent(currNodeId, primeInt);
		
		//forward the request for proper display
		RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/index.jsp");
		dispatcher.forward( request, response );
	}
	
	/**
	*/
	private void sendSNMPRestartEvent(int nodeid, String primeInt)
		throws ServletException
	{
		Event snmpRestart = new Event();
		snmpRestart.setUei("http://uei.opennms.org/products/bluebird/nodes/reinitializePrimarySnmpInterface");
		snmpRestart.setNodeid(nodeid);
		snmpRestart.setInterface(primeInt);
		snmpRestart.setSource("web ui");
		snmpRestart.setTime(EventConstants.formatToString(new java.util.Date()));
	
		sendEvent(snmpRestart);
	}
	
	
	/**
	*/
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
        /**
        */
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
