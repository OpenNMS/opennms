
package org.opennms.web.eventconf;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * 
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class NewEventServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	Event event = new Event();
	event.setUei(request.getParameter("newEventUEI"));
	event.setSeverity("Normal");
	event.setLogMessage("");
	event.setDescription("");
	
	HttpSession user = request.getSession(true);
	
	user.setAttribute("event.modify.jsp", event);
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/eventconf/modify.jsp");
        dispatcher.forward( request, response );
    }
}
