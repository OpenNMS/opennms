
package org.opennms.web.eventconf.autoActions;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that handles saving the auto actions of an event
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class SaveAutoActionsServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
	
    	if (user != null)
	{
		Event event = (Event)user.getAttribute("event.modify.jsp");
		
		event.clearAutoActions();
		
		int rows = Integer.parseInt(request.getParameter("rows"));
		
		for (int i = 0; i < rows; i++)
		{
			AutoAction newAction = new AutoAction();
			
			newAction.setAutoAction(request.getParameter("action"+i));
			newAction.setState(request.getParameter("state"+i));
			
			event.addAutoAction(newAction);
		}
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward( request, response );
    }
}
