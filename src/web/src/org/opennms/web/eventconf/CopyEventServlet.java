
package org.opennms.web.eventconf;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;
import org.opennms.web.parsers.*;

/**
 * 
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class CopyEventServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	String originalEventUEI = request.getParameter("oldEventUEI");
	String clonedEventUEI = request.getParameter("newEventUEI");
	
	try
	{
		EventConfFactory factory = EventConfFactory.getInstance();
		
		Event event = factory.getEvent(originalEventUEI);
		Event clonedEvent = (Event)event.clone();
		clonedEvent.setUei(clonedEventUEI);
		
		factory.saveEvent(clonedEvent);
	}
	catch (XMLWriteException e)
	{
		throw new ServletException("could not save copied event " + clonedEventUEI + ": " + e.getMessage(), e);
	}
	catch (Exception e)
	{
		throw new ServletException("could not copy event " + originalEventUEI + ": " + e.getMessage(), e); 
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/eventconf/list.jsp");
        dispatcher.forward( request, response );
    }
}
