
package org.opennms.web.admin.users;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.UserFactory;

/**
 * A servlet that handles adding a new user
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class AddNewUserServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
	try
	{
		UserFactory.init();
	}
	catch(Exception e)
	{
		throw new ServletException ("AddNewUserServlet: Error initialising user factory." + e);
	}
	UserFactory userFactory = UserFactory.getInstance();
	
       	String userID = request.getParameter("userID");
	String password = request.getParameter("pass1");
	
        boolean hasUser = false;
        try {
                hasUser = userFactory.hasUser(userID);
        } catch (Exception e)
        {
                throw new ServletException("can't determine if user " + userID + " already exists in users.xml.", e);
        }
        
        if (hasUser)
        {
                RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/users/newUser.jsp?action=redo");
                dispatcher.forward( request, response );
        }
        else
        {
	User newUser = new User();
	newUser.setUserId(userID);
	newUser.setPassword(UserFactory.encryptPassword(password));
	
	HttpSession userSession = request.getSession(false);
	userSession.setAttribute("user.modifyUser.jsp", newUser);
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/users/modifyUser.jsp");
        dispatcher.forward( request, response );
        }
    }
}
