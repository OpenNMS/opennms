
package org.opennms.web.eventconf;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that handles rearranging event configurations
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class RearrangeEventsServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	try
	{
		EventConfFactory eventConfFactory = EventConfFactory.getInstance();
		
		List newList = new ArrayList();
		String ueis[] = request.getParameterValues("eventUEIs");
		
		for (int i = 0; i < ueis.length; i++)
		{
			newList.add(eventConfFactory.getEvent(ueis[i]));
		}
		
		eventConfFactory.saveEvents(newList);
	}
	catch (Exception e)
	{
		throw new ServletException("Couldn't initialize EventConfFactory", e);
	}
	
        //forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/eventconf/list.jsp");
        dispatcher.forward( request, response );
    }
}
