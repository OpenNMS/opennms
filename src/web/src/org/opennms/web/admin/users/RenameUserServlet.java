
package org.opennms.web.admin.users;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.UserFactory;

/**
 * A servlet that handles renaming an existing user
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class RenameUserServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	String userID = request.getParameter("userID");
	String newID = request.getParameter("newID");
	
	//now save to the xml file
	try 
	{
		UserFactory userFactory = UserFactory.getInstance();
		userFactory.renameUser(userID, newID);
	}
	catch( Exception e) 
	{
		throw new ServletException( "Error renaming user " + userID + " to " + newID, e );
	}
	
	response.sendRedirect( "list.jsp" );
    }
}
