
package org.opennms.web.admin.users;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;

//import org.opennms.web.admin.users.*;
//import org.opennms.web.admin.users.parsers.*;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.users.User;

/**
 * A servlet that handles putting the User object into the 
   request and forwarding on to a particular jsp
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class ModifyUserServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession userSession = request.getSession(true);
	
	try
	{
		UserFactory.init();
		UserFactory userFactory = UserFactory.getInstance();
		User user = userFactory.getUser(request.getParameter("userID"));
		userSession.setAttribute("user.modifyUser.jsp", user);
	}
	catch (Exception e)
	{
		throw new ServletException("Couldn't initialize UserFactory", e);
	}
	
        //forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/users/modifyUser.jsp");
        dispatcher.forward( request, response );
    }
}
