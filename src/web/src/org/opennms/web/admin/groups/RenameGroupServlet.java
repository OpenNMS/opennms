
package org.opennms.web.admin.groups;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.*;

/**
 * A servlet that handles renaming an existing group
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class RenameGroupServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	String groupName = request.getParameter("groupName");
	String newName = request.getParameter("newName");
	
	//now save to the xml file
	try 
	{
		GroupFactory.init();
                GroupFactory groupFactory = GroupFactory.getInstance();
		groupFactory.renameGroup(groupName, newName);
	}
	catch( Exception e) 
	{
		throw new ServletException( "Error renaming group " + groupName + " to " + newName, e );
	}
	
	
	response.sendRedirect( "list.jsp" );
    }
}
