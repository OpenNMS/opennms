package org.opennms.web.account.selfService;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.User;

/**
 * A servlet that retrieves a user's password in preparation for changing the password
 * 
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class NewPasswordEntryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession userSession = request.getSession(false);
		
		try {
            UserFactory.init();
        } catch (Exception e) {
            throw new ServletException("NewPasswordEntryServlet: Error initialising user factory." + e);
        }
        UserManager userFactory = UserFactory.getInstance();
        
        if (userSession != null) {
            String userid = request.getRemoteUser();
            try {
                User user = userFactory.getUser(userid);
                userSession.setAttribute("user.newPassword.jsp", user);
            }
            catch (Exception e) {
                throw new ServletException("Couldn't initialize UserFactory", e);
            }
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/account/selfService/newPassword.jsp");
            dispatcher.forward(request, response);
        }
	}
}
