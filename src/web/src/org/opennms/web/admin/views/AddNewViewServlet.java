
package org.opennms.web.admin.views;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.web.admin.views.parsers.*;
import org.opennms.web.parsers.*;
import org.opennms.web.admin.views.*;

/**
 * A servlet that handles adding a new view
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class AddNewViewServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	String viewName = request.getParameter("viewName");
	
	View newView = new View();
	newView.setViewName(viewName);
	
	HttpSession userSession = request.getSession(false);
	userSession.setAttribute("view.modifyView.jsp", newView);
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/views/modifyView.jsp");
        dispatcher.forward( request, response );
    }
}
