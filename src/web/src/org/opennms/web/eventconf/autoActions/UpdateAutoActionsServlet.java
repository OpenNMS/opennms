
package org.opennms.web.eventconf.autoActions;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that handles updating the temporary copy of auto actions
 * currently being edited
 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class UpdateAutoActionsServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
	
    	if (user != null)
	{
		List autoActions = (List)user.getAttribute("autoActions.editAutoActions.jsp");
		
		int rows = Integer.parseInt(request.getParameter("rows"));
		
		autoActions.clear();
		for (int i = 0; i < rows; i++)
		{
			AutoAction newAction = new AutoAction();
			
			newAction.setAutoAction(request.getParameter("action"+i));
			newAction.setState(request.getParameter("state"+i));
			
			autoActions.add(newAction);
		}
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward( request, response );
    }
}
