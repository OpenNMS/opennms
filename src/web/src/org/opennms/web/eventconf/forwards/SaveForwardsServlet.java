
package org.opennms.web.eventconf.forwards;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that handles saving forwards
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class SaveForwardsServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
	
    	if (user != null)
	{
		Event event = (Event)user.getAttribute("event.modify.jsp");
		
		int rows = Integer.parseInt(request.getParameter("rows"));
		
		event.clearForwards();
		for (int i = 0; i < rows; i++)
		{
			Forward newForward = new Forward();
			
			newForward.setState(request.getParameter("state"+i));
			newForward.setMechanism(request.getParameter("mechanism"+i));
			newForward.setForward(request.getParameter("forward"+i));
			
			event.addForward(newForward);
		}
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward( request, response );
    }
}
