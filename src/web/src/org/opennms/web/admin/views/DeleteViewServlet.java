
package org.opennms.web.admin.views;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.*;

/**
 * A servlet that handles deleting an existing view
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class DeleteViewServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	String viewName = request.getParameter("viewName");
	
	//now save to the xml file
	try 
	{
		ViewFactory viewFactory = ViewFactory.getInstance();
		viewFactory.deleteView(viewName);
	}
	catch( Exception e) 
	{
		throw new ServletException( "Error deleting view " + viewName, e );
	}
	
	
	response.sendRedirect( "list.jsp" );
    }
}
