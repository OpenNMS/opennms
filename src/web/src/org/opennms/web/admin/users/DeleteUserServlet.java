
package org.opennms.web.admin.users;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.UserFactory;

/**
 * A servlet that handles deleting an existing user
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class DeleteUserServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	String userID = request.getParameter("userID");
	
	//now save to the xml file
	try 
	{
		UserFactory.init();
		UserFactory userFactory = UserFactory.getInstance();
		userFactory.deleteUser(userID);
	}
	catch( Exception e) 
	{
		throw new ServletException( "Error deleting user " + userID, e );
	}
	
	response.sendRedirect( "list.jsp" );
    }
}
