
package org.opennms.web.eventconf;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that handles putting the Event conf object into the 
   request and forwarding on to a particular jsp
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class ModifyParamServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	Event event = null;
	HttpSession user = request.getSession(true);
	
	try
	{
		EventConfFactory eventConfFactory = EventConfFactory.getInstance();
		event = eventConfFactory.getEvent(request.getParameter("oldEventUEI"));
	}
	catch (Exception e)
	{
		throw new ServletException("Couldn't initialize EventConfFactory", e);
	}
	
        user.setAttribute("event.modify.jsp", event);
	
        //forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/eventconf/modify.jsp");
        dispatcher.forward( request, response );
    }
}
