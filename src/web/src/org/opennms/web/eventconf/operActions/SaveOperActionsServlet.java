
package org.opennms.web.eventconf.operActions;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that handles saving the operator actions of an event
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class SaveOperActionsServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
	
    	if (user != null)
	{
		Event event = (Event)user.getAttribute("event.modify.jsp");
		
		event.clearOperatorActions();
		
		int rows = Integer.parseInt(request.getParameter("rows"));
		
		for (int i = 0; i < rows; i++)
		{
			OperatorAction newAction = new OperatorAction();
			
			newAction.setOperatorAction(request.getParameter("action"+i));
			newAction.setState(request.getParameter("state"+i));
			newAction.setMenuText(request.getParameter("menu"+i));
			
			event.addOperatorAction(newAction);
		}
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward( request, response );
    }
}
