
package org.opennms.web.admin.groups;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.web.admin.groups.parsers.*;
import org.opennms.web.parsers.*;
import org.opennms.web.admin.groups.*;

/**
 * A servlet that handles adding a new group
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class AddNewGroupServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	String groupName = request.getParameter("groupName");
	
	Group newGroup = new Group();
	newGroup.setGroupName(groupName);
	
	HttpSession userSession = request.getSession(false);
	userSession.setAttribute("group.modifyGroup.jsp", newGroup);
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/groups/modifyGroup.jsp");
        dispatcher.forward( request, response );
    }
}
