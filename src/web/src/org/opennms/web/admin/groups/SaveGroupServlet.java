
package org.opennms.web.admin.groups;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.GroupFactory;

/**
 * A servlet that handles saving the group stored in the web users http session.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class SaveGroupServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
	
    	if (user != null)
	{
		Group newGroup = (Group)user.getAttribute("group.modifyGroup.jsp");
		
		if(newGroup != null)
		{
			//now save to the xml file
			try 
			{
				GroupFactory groupFactory = GroupFactory.getInstance();
				groupFactory.saveGroup(newGroup.getName(), newGroup);
			}
			catch( Exception e) 
			{
				throw new ServletException( "Error saving group " + newGroup.getName(), e);
			}
		}
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/groups/list.jsp");
        dispatcher.forward( request, response );
    }
}
