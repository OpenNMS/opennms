
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
 * A servlet that handles querying the database for node information
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class DeleteGetNodesServlet extends HttpServlet
{
	private static final String NODE_QUERY = 
		"SELECT nodeid, nodelabel FROM node ORDER BY nodelabel, nodeid";
	
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
                        user.setAttribute("listAll.delete.jsp", getAllNodes(user));
		} catch (SQLException e) {
                        throw new ServletException(e);
                }
                
		//forward the request for proper display
		RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/delete.jsp");
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
			
			Statement stmt = connection.createStatement();
			ResultSet nodeSet = stmt.executeQuery(NODE_QUERY);
			
			if (nodeSet != null)
			{
				while (nodeSet.next ())
				{
					ManagedNode newNode = new ManagedNode();
                                        newNode.setNodeID(nodeSet.getInt(1));
                                        newNode.setNodeLabel(nodeSet.getString(2));
                                        allNodes.add(newNode);
                                        
			    	}
                        }
                        userSession.setAttribute("lineItems.delete.jsp", new Integer(lineCount));

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
}
