
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
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class GetNodesServlet extends HttpServlet
{
	//private static final String NODE_QUERY = 
	//	"SELECT nodeid, nodelabel FROM node ORDER BY nodelabel, nodeid";
	
        private static final String INTERFACE_QUERY = 
                "SELECT nodeid, ipaddr, isManaged FROM ipinterface WHERE ismanaged in ('M','A','U','F') AND ipaddr <> '0.0.0.0' ORDER BY ipaddr";
        
        private static final String SERVICE_QUERY =
                "SELECT ifservices.serviceid, servicename, status FROM ifservices, service WHERE nodeid=? AND ipaddr=? AND status in ('A','U','F') AND ifservices.serviceid = service.serviceid ORDER BY servicename";
	
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
                        user.setAttribute("listAll.manage.jsp", getAllNodes(user));
		} catch (SQLException e) {
                        throw new ServletException(e);
                }
                
		//forward the request for proper display
		RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/manage.jsp");
		dispatcher.forward( request, response );
	}
	
	/**
	*/
	private List getAllNodes(HttpSession userSession)
		throws SQLException
	{
		Connection connection = null;
		List allNodes = new ArrayList();
                int lineCount = 0;
                
		try
		{
			connection = DatabaseConnectionFactory.getInstance().getConnection();
			
			//Statement stmt = connection.createStatement();
			//ResultSet nodeSet = stmt.executeQuery(NODE_QUERY);
			
			//if (nodeSet != null)
			//{
				// Iterate through the result and build the array list
			//	while (nodeSet.next ())
			//	{
			//		ManagedNode newNode = new ManagedNode();
                        //                newNode.setNodeID(nodeSet.getInt(1));
                        //                newNode.setNodeLabel(nodeSet.getString(2));
                        //                allNodes.add(newNode);
                                        
                                        PreparedStatement interfaceSelect = connection.prepareStatement(INTERFACE_QUERY);
                                        //interfaceSelect.setInt(1, newNode.getNodeID());
                                        
                                        ResultSet interfaceSet = interfaceSelect.executeQuery();
                                        
                                        if (interfaceSet != null)
                                        {
                                                while(interfaceSet.next())
                                                {
                                                        lineCount++;
                                                        ManagedInterface newInterface = new ManagedInterface();
                                                        allNodes.add(newInterface);
                                                        newInterface.setNodeid(interfaceSet.getInt(1));
                                                        newInterface.setAddress(interfaceSet.getString(2));
                                                        
                                                        
                                                        newInterface.setStatus(interfaceSet.getString(3));
                                                        //newNode.addInterface(newInterface);
                                                        
                                                        PreparedStatement serviceSelect = connection.prepareStatement(SERVICE_QUERY);
							serviceSelect.setInt(1, newInterface.getNodeid());
                                                        serviceSelect.setString(2, newInterface.getAddress());
                                                        
                                                        ResultSet serviceSet = serviceSelect.executeQuery();
                                                        
                                                        if (serviceSet != null)
                                                        {
                                                                while(serviceSet.next())
                                                                {
                                                                        lineCount++;
                                                                        ManagedService newService = new ManagedService();
                                                                        newService.setId(serviceSet.getInt(1));
									newService.setName(serviceSet.getString(2));
                                                                        newService.setStatus(serviceSet.getString(3));
                                                                        newInterface.addService(newService);
                                                                }
                                                        }
                                                        serviceSelect.close();
                                                }
                                        }
                                        interfaceSelect.close();
			    	//}
                        //}
                        userSession.setAttribute("lineItems.manage.jsp", new Integer(lineCount));
			//nodeSet.close();
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
}
