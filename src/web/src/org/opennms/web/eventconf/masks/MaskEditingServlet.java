
package org.opennms.web.eventconf.masks;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that initializes the data needed to edit masks and
 * forwards browser onto the mask editing jsp.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class MaskEditingServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
    
    	if (user != null)
	{
		Event event = (Event)user.getAttribute("event.modify.jsp");
		List elementsList = event.getMask();
		
		//make a copy of the mask elements and put them into a list where they
		//will be editied
		List editElementsList = new ArrayList();
		
		for (int i = 0; i < elementsList.size(); i++)
		{
			editElementsList.add( (MaskElement)elementsList.get(i) );
		}
		
		user.setAttribute("maskElements.editMask.jsp", editElementsList);
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/eventconf/masks/editMask.jsp");
        dispatcher.forward( request, response );
    }
}
