
package org.opennms.web.eventconf.forwards;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;

/**
 * A servlet that handles adding a new forward to an event
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class DeleteForwardServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
    
    	if (user != null)
	{
		List forwards = (List)user.getAttribute("forwards.editForwards.jsp");
		
		int index = Integer.parseInt(request.getParameter("deleteIndex"));
		
		forwards.remove(index);
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/eventconf/forwards/editForwards.jsp");
        dispatcher.forward( request, response );
    }
}
