
package org.opennms.web.eventconf.autoActions;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that handles adding a new auto action for an event
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class NewAutoActionServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
    
    	if (user != null)
	{
		List autoActions = (List)user.getAttribute("autoActions.editAutoActions.jsp");
		
		AutoAction newAction = new AutoAction();
		newAction.setAutoAction("");
		
		autoActions.add(newAction);
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/eventconf/autoActions/editAutoActions.jsp");
        dispatcher.forward( request, response );
    }
}
