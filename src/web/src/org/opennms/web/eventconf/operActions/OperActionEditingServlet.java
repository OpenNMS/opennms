
package org.opennms.web.eventconf.operActions;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that initializes the data needed to edit operator actions
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class OperActionEditingServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
    
    	if (user != null)
	{
		Event event = (Event)user.getAttribute("event.modify.jsp");
		List actionsList = event.getOperatorActions();
		
		//make a copy of the operator actions and put them into a list where they
		//will be edited
		List editOperActionsList = new ArrayList();
		
		for (int i = 0; i < actionsList.size(); i++)
		{
			editOperActionsList.add( (OperatorAction)actionsList.get(i) );
		}
		
		user.setAttribute("operActions.editOperActions.jsp", editOperActionsList);
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/eventconf/operActions/editOperActions.jsp");
        dispatcher.forward( request, response );
    }
}
