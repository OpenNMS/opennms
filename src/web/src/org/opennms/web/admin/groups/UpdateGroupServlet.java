
package org.opennms.web.admin.groups;

import java.io.IOException;
import java.util.*;
import java.text.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.GroupFactory;

/**
 * A servlet that handles saving a group
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class UpdateGroupServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession userSession = request.getSession(false);
	
    	if (userSession != null)
	{
		Group newGroup = (Group)userSession.getAttribute("group.modifyGroup.jsp");
		
		//get the rest of the group information from the form
		newGroup.clearUser();
		
		String users[] = request.getParameterValues("selectedUsers");
		
		if (users != null)
		{
			for (int i = 0; i < users.length; i++)
			{
				newGroup.addUser(users[i]);
			}
		}
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward( request, response );
    }
}
