
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
public class UpdateForwardsServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
	
    	if (user != null)
	{
		List forwards = (List)user.getAttribute("forwards.editForwards.jsp");
		
		int rows = Integer.parseInt(request.getParameter("rows"));
		
		forwards.clear();
		for (int i = 0; i < rows; i++)
		{
			Forward newForward = new Forward();
			
			newForward.setState(request.getParameter("state"+i));
			newForward.setMechanism(request.getParameter("mechanism"+i));
			newForward.setForward(request.getParameter("forward"+i));
			
			forwards.add(newForward);
		}
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward( request, response );
    }
}
