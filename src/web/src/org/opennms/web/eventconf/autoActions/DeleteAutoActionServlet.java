
package org.opennms.web.eventconf.autoActions;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;

/**
 * A servlet that handles deleting an auto action from an event
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class DeleteAutoActionServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
    
    	if (user != null)
	{
		List autoActions = (List)user.getAttribute("autoActions.editAutoActions.jsp");
		
		int index = Integer.parseInt(request.getParameter("deleteIndex"));
		
		autoActions.remove(index);
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/eventconf/autoActions/editAutoActions.jsp");
        dispatcher.forward( request, response );
    }
}
