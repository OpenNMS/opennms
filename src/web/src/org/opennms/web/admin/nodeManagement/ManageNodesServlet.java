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
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class ManageNodesServlet extends HttpServlet
{
	private static final String UPDATE_INTERFACE = "UPDATE ipinterface SET isManaged = ? WHERE ipaddr IN (?)";
	private static final String UPDATE_SERVICE = "UPDATE ifservices SET status = ? WHERE ipaddr = ? AND nodeID = ? AND serviceid = ?";
	
	private static final String INCLUDE_FILE_NAME = "include";
	
	public static final String GAINED_SERVICE_UEI   = "uei.opennms.org/nodes/nodeGainedService";
	public static final String GAINED_INTERFACE_UEI = "uei.opennms.org/nodes/nodeGainedInterface";
	public static final String NOTICE_NAME          = "Email-Reporting";
	private static final String NOTICE_COMMAND = "/opt/OpenNMS/bin/notify.sh ";
	
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
		java.util.List allNodes = null;
		
		if (userSession != null)
		{
		  	allNodes = (java.util.List)userSession.getAttribute("listAll.manage.jsp");
		}
		
		//the list of all interfaces marked as managed
		java.util.List interfaceList = getList(request.getParameterValues("interfaceCheck"));
		
		//the list of all services marked as managed
		java.util.List serviceList = getList(request.getParameterValues("serviceCheck"));
		
		//the list of interfaces that need to be put into the URL file
		java.util.List addToURL = new ArrayList();

		// date to set on events sent out
		String curDate = EventConstants.formatToString(new java.util.Date());
		
		
		List unmanageInterfacesList = new ArrayList();
		List manageInterfacesList = new ArrayList();
		
		try 
		{
			Connection connection = Vault.getDbConnection();
			try 
			{
				connection.setAutoCommit(false);
				PreparedStatement stmt = connection.prepareStatement(UPDATE_SERVICE);
                        
				for (int j = 0; j < allNodes.size(); j++)
                        {
					ManagedInterface curInterface = (ManagedInterface)allNodes.get(j);
					String intKey = curInterface.getNodeid()+"-"+curInterface.getAddress();
                                
                                //see if this interface needs added to the url list
					if (interfaceList.contains(intKey))
				{
					addToURL.add(curInterface.getAddress());
				}
				
					//determine what is managed and unmanged
					if (interfaceList.contains(intKey) && curInterface.getStatus().equals("unmanaged"))
				{
						//Event newEvent = new Event();
						//newEvent.setUei("uei.opennms.org/internal/interfaceManaged");
						//newEvent.setSource("web ui");
						//newEvent.setNodeid(curNode.getNodeID());
						//newEvent.setInterface(curInterface.getAddress());
						//newEvent.setTime(curDate);
		
						//updateInterface(curInterface.getNodeid(), curInterface.getAddress(), new Event(), "M");
						manageInterfacesList.add(curInterface.getAddress());
					}
					else if (!interfaceList.contains(intKey) && curInterface.getStatus().equals("managed"))
					{
						//Event newEvent = new Event();
						//newEvent.setUei("uei.opennms.org/internal/interfaceUnmanaged");
						//newEvent.setSource("web ui");
						//newEvent.setNodeid(curNode.getNodeID());
						//newEvent.setInterface(curInterface.getAddress());
						//newEvent.setTime(curDate);
                    
						//updateInterface(curInterface.getNodeid(), curInterface.getAddress(), new Event(), "F");
						unmanageInterfacesList.add(curInterface.getAddress());
                                }
			        
                                List interfaceServices = curInterface.getServices();
                                
                                for (int k = 0; k < interfaceServices.size(); k++)
                                {
                                        ManagedService curService = (ManagedService)interfaceServices.get(k);
						String serviceKey = intKey + "-" + curService.getId();
                                        
						if (serviceList.contains(serviceKey) && curService.getStatus().equals("unmanaged"))
                                        {
							//Event newEvent = new Event();
							//newEvent.setUei("uei.opennms.org/internal/serviceManaged");
							//newEvent.setSource("web ui");
							//newEvent.setNodeid(curNode.getNodeID());
							//newEvent.setInterface(curInterface.getAddress());
							//newEvent.setService(curService.getName());
							//newEvent.setTime(curDate);
							
							stmt.setString(1, "A");
							stmt.setString(2, curInterface.getAddress());
							stmt.setInt(3, curInterface.getNodeid());
							stmt.setInt(4, curService.getId());
							this.log("DEBUG: executing manage service update for " + curInterface.getAddress() + " " + curService.getName());
							stmt.executeUpdate();
						}
						else if (!serviceList.contains(serviceKey) && curService.getStatus().equals("managed"))
						{
							//Event newEvent = new Event();
							//newEvent.setUei("uei.opennms.org/internal/serviceUnmanaged");
							//newEvent.setSource("web ui");
							//newEvent.setNodeid(curNode.getNodeID());
							//newEvent.setInterface(curInterface.getAddress());
							//newEvent.setService(curService.getName());
							//newEvent.setTime(curDate);
							
							stmt.setString(1, "F");
							stmt.setString(2, curInterface.getAddress());
							stmt.setInt(3, curInterface.getNodeid());
							stmt.setInt(4, curService.getId());
							this.log("DEBUG: executing unmanage service update for " + curInterface.getAddress() + " " + curService.getName());
							stmt.executeUpdate();
                                        }
                                } //end k loop
                        } //end j loop
					
				if (manageInterfacesList.size() > 0) manageInterfaces(manageInterfacesList, connection);
				if (unmanageInterfacesList.size() > 0) unmanageInterfaces(unmanageInterfacesList, connection);
		
		//update the packages url file
		writeURLFile(addToURL);
		
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
		
		//send the event to restart SCM
		sendSCMRestartEvent();
		
		//forward the request for proper display
		RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/manageNodesFinish.jsp");
		dispatcher.forward( request, response );
	}
	
	/**
	*/
	private void manageInterfaces(List interfaces, Connection connection)
		throws SQLException
	{
		StringBuffer query = new StringBuffer("UPDATE ipinterface SET isManaged = ");
		query.append("'M'").append(" WHERE ipaddr IN (");
			
		for (int i = 0; i < interfaces.size(); i++)
			{
			query.append("'").append((String)interfaces.get(i)).append("'");
			
			if (i < interfaces.size()-1) query.append(",");
		}
		query.append(")");
		
		this.log("DEBUG: " + query.toString());
		Statement update = connection.createStatement();
		update.executeUpdate(query.toString());
		update.close();
	}
	
	/**
	*/
	private void unmanageInterfaces(List interfaces, Connection connection)
		throws SQLException
	{
		StringBuffer query = new StringBuffer("UPDATE ipinterface SET isManaged = ");
		query.append("'F'").append(" WHERE ipaddr IN (");
		
		for (int i = 0; i < interfaces.size(); i++)
	{
			query.append("'").append((String)interfaces.get(i)).append("'");
		
			if (i < interfaces.size()-1) query.append(",");
		}
		query.append(")");
		
		this.log("DEBUG: " + query.toString());
		Statement update = connection.createStatement();
		update.executeUpdate(query.toString());
		update.close();
	}
	
	/**
	*/
	private void sendSCMRestartEvent()
		throws ServletException
	{
		Event scmRestart = new Event();
		scmRestart.setUei("uei.opennms.org/internal/restartSCM");
		scmRestart.setSource("web ui");
		scmRestart.setTime(EventConstants.formatToString(new java.util.Date()));
	
		sendEvent(scmRestart);
	}
	
	/**
	*/
	private void writeURLFile(java.util.List interfaceList)
		throws ServletException
	{
		String path = System.getProperty("opennms.home") + File.separator + "etc" + File.separator;
		
		if (path != null)
		{
			String fileName = path + INCLUDE_FILE_NAME;
			
			try
			{
				FileWriter fileWriter = new FileWriter(fileName);
				
				for (int i = 0; i < interfaceList.size(); i++)
				{
					fileWriter.write((String)interfaceList.get(i) + System.getProperty("line.separator"));
				}
				
				//write out the file and close
				fileWriter.flush();
				fileWriter.close();
			}
			catch (IOException e)
			{
				throw new ServletException("Error writing the include url file " + fileName + ": " + e.getMessage(), e);
			}
		}
		else
		{
			throw new ServletException("The path to the package URL include directory is null.");
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
}
