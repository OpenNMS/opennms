
package org.opennms.web.admin.users;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.web.admin.users.parsers.*;

/**
 * A servlet that handles removing duties from a users notification information
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class RemoveDutySchedulesServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession userSession = request.getSession(true);
	
	User user = (User)userSession.getAttribute("user.modifyUser.jsp");
	NotificationInfo notif = user.getNotificationInfo();
	
	List dutySchedules = notif.getDutySchedules();
	
	int dutyCount = Integer.parseInt(request.getParameter("dutySchedules"));
	for (int i = 0; i < dutyCount; i++)
	{
		String curDuty = request.getParameter("deleteDuty"+i);
		if (curDuty != null)
		{
			dutySchedules.remove(i);
		}
	}
	
        //forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/users/modifyUser.jsp");
        dispatcher.forward( request, response );
    }
}
