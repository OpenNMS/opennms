
package org.opennms.web.eventconf.forwards;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that initializes the data needed to edit forwards
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class ForwardsEditingServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
    
    	if (user != null)
	{
		Event event = (Event)user.getAttribute("event.modify.jsp");
		List forwardsList = event.getForwards();
		
		//make a copy of the forwards and put them into a list where they
		//will be editied
		List editForwardsList = new ArrayList();
		
		for (int i = 0; i < forwardsList.size(); i++)
		{
			editForwardsList.add( (Forward)forwardsList.get(i) );
		}
		
		user.setAttribute("forwards.editForwards.jsp", editForwardsList);
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/eventconf/forwards/editForwards.jsp");
        dispatcher.forward( request, response );
    }
}
