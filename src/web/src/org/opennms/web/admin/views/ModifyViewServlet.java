
package org.opennms.web.admin.views;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.views.View;
import org.opennms.netmgt.config.ViewFactory;

/**
 * A servlet that handles putting the View object into the 
   request and forwarding on to a particular jsp
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class ModifyViewServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession userSession = request.getSession(true);
	
	try
	{
		ViewFactory.init();
		ViewFactory viewFactory = ViewFactory.getInstance();
		View view = viewFactory.getView(request.getParameter("viewName"));
		userSession.setAttribute("view.modifyView.jsp", view);
	}
	catch (Exception e)
	{
		throw new ServletException("Couldn't initialize ViewFactory", e);
	}
	
        //forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/views/modifyView.jsp");
        dispatcher.forward( request, response );
    }
}
