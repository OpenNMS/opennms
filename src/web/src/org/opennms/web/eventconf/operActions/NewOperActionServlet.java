
package org.opennms.web.eventconf.operActions;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that handles adding a new operator action for an event
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class NewOperActionServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
    
    	if (user != null)
	{
		List operActions = (List)user.getAttribute("operActions.editOperActions.jsp");
		
		OperatorAction newAction = new OperatorAction();
		newAction.setOperatorAction("");
		newAction.setMenuText("");
		
		operActions.add(newAction);
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/eventconf/operActions/editOperActions.jsp");
        dispatcher.forward( request, response );
    }
}
