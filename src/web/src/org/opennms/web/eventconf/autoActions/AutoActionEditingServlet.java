
package org.opennms.web.eventconf.autoActions;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that initializes the data needed to edit auto actions
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class AutoActionEditingServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
    
    	if (user != null)
	{
		Event event = (Event)user.getAttribute("event.modify.jsp");
		List actionsList = event.getAutoActions();
		
		//make a copy of the mask elements and put them into a list where they
		//will be editied
		List editAutoActionsList = new ArrayList();
		
		for (int i = 0; i < actionsList.size(); i++)
		{
			editAutoActionsList.add( (AutoAction)actionsList.get(i) );
		}
		
		user.setAttribute("autoActions.editAutoActions.jsp", editAutoActionsList);
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/eventconf/autoActions/editAutoActions.jsp");
        dispatcher.forward( request, response );
    }
}
