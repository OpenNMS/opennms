
package org.opennms.web.eventconf.masks;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;

/**
 * A servlet that handles adding a new Mask Element to an Event's mask
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class DeleteElementServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
    
    	if (user != null)
	{
		List maskElements = (List)user.getAttribute("maskElements.editMask.jsp");
		
		int index = Integer.parseInt(request.getParameter("deleteIndex"));
		
		maskElements.remove(index);
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/eventconf/masks/editMask.jsp");
        dispatcher.forward( request, response );
    }
}
