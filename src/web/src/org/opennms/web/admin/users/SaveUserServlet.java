
package org.opennms.web.admin.users;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.UserFactory;

/**
 * A servlet that handles saving the user stored in the web users http session.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class SaveUserServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
	
    	if (user != null)
	{
		User newUser = (User)user.getAttribute("user.modifyUser.jsp");
		
		//now save to the xml file
		try 
		{
			UserFactory userFactory = UserFactory.getInstance();
			userFactory.saveUser(newUser.getUserId(), newUser);
		}
		catch( Exception e) 
		{
			throw new ServletException( "Error saving user " + newUser.getUserId(), e);
		}
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/users/list.jsp");
        dispatcher.forward( request, response );
    }
}
