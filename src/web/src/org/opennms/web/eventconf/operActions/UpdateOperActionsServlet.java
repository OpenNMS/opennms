
package org.opennms.web.eventconf.operActions;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that handles updating the temporary copy of operator actions
 * currently being edited
 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class UpdateOperActionsServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
	
    	if (user != null)
	{
		List operActions = (List)user.getAttribute("operActions.editOperActions.jsp");
		
		int rows = Integer.parseInt(request.getParameter("rows"));
		
		operActions.clear();
		for (int i = 0; i < rows; i++)
		{
			OperatorAction newAction = new OperatorAction();
			
			newAction.setOperatorAction(request.getParameter("action"+i));
			newAction.setState(request.getParameter("state"+i));
			newAction.setMenuText(request.getParameter("menu"+i));
			
			operActions.add(newAction);
		}
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward( request, response );
    }
}
