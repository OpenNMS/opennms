
package org.opennms.web.admin.views;

import java.io.IOException;
import java.util.*;
import java.text.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.*;
import org.opennms.netmgt.config.views.*;

/**
 * A servlet that handles saving a view
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class UpdateViewServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession userSession = request.getSession(false);
	
    	if (userSession != null)
	{
		View newView = (View)userSession.getAttribute("view.modifyView.jsp");
		
		newView.setMembership(null);
		
		String users[] = request.getParameterValues("selectedUsers");
		Membership membership = new Membership();
		if (users != null)
		{
			for (int i = 0; i < users.length; i++)
			{	
				Member member = new Member();
				member.setType("user");
				member.setContent(users[i]);
				membership.addMember(member);
			}
		}
		
		String groups[] = request.getParameterValues("selectedGroups");
		if (groups != null)
		{
			for (int i = 0; i < groups.length; i++)
			{
				Member member = new Member();
				member.setType("group");
				member.setContent(groups[i]);
				membership.addMember(member);
			}
		}
                if (membership.getMemberCount()>0)
                        newView.setMembership(membership);
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward( request, response );
    }
}
