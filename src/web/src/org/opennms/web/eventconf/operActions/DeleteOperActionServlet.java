
package org.opennms.web.eventconf.operActions;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;

/**
 * A servlet that handles deleting an operator action from an event
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class DeleteOperActionServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
    
    	if (user != null)
	{
		List operActions = (List)user.getAttribute("operActions.editOperActions.jsp");
		
		int index = Integer.parseInt(request.getParameter("deleteIndex"));
		
		operActions.remove(index);
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/eventconf/operActions/editOperActions.jsp");
        dispatcher.forward( request, response );
    }
}
