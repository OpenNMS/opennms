
package org.opennms.web.admin.groups;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.GroupFactory;

/**
 * A servlet that handles putting the Group object into the 
   request and forwarding on to a particular jsp
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class ModifyGroupServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession userSession = request.getSession(true);
	
	try
	{
		GroupFactory.init();
		GroupFactory groupFactory = GroupFactory.getInstance();
		Group group = groupFactory.getGroup(request.getParameter("groupName"));
		userSession.setAttribute("group.modifyGroup.jsp", group);
	}
	catch (Exception e)
	{
		throw new ServletException("Couldn't initialize GroupFactory", e);
	}
	
        //forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/groups/modifyGroup.jsp");
        dispatcher.forward( request, response );
    }
}
