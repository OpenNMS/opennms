
package org.opennms.web.eventconf;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that handles renaming an event
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class RenameEventServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	Event event = null;
	HttpSession user = request.getSession(true);
	
	try
	{
		EventConfFactory eventConfFactory = EventConfFactory.getInstance();
		event = eventConfFactory.getEvent(request.getParameter("oldEventUEI"));
		eventConfFactory.renameEvent(request.getParameter("newEventUEI"), event);
	}
	catch (Exception e)
	{
		throw new ServletException("Couldn't rename event", e);
	}
	
        //forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/eventconf/list.jsp");
        dispatcher.forward( request, response );
    }
}
