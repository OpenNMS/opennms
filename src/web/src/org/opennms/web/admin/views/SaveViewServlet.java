
package org.opennms.web.admin.views;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.views.View;
import org.opennms.netmgt.config.ViewFactory;

/**
 * A servlet that handles saving the views stored in the web users http session.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class SaveViewServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
	
    	if (user != null)
	{
		View newView = (View)user.getAttribute("view.modifyView.jsp");
		
		//now save to the xml file
		try 
		{
			ViewFactory viewFactory = ViewFactory.getInstance();
			viewFactory.saveView(newView.getName(), newView);
		}
		catch( Exception e) 
		{
			throw new ServletException( "Error saving view " + newView.getName(), e);
		}
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/views/list.jsp");
        dispatcher.forward( request, response );
    }
}
