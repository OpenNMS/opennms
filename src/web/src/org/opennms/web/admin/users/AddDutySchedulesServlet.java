
package org.opennms.web.admin.users;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.DutySchedule;

/**
 * A servlet that handles adding new duty schedules to a users notification info
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class AddDutySchedulesServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession userSession = request.getSession(true);
	
	User user = (User)userSession.getAttribute("user.modifyUser.jsp");
//	NotificationInfo notif = user.getNotificationInfo();
	
	Vector newSchedule = new Vector();
	
	int dutyAddCount = Integer.parseInt(request.getParameter("numSchedules"));
	
	for (int j = 0; j < dutyAddCount; j++)
	{
		//add 7 false boolean values for each day of the week
		for (int i = 0; i < 7; i++)
		{
			newSchedule.addElement(new Boolean(false));
		}
		
		//add two strings for the begin and end time
		newSchedule.addElement("0");
		newSchedule.addElement("0");
		
		user.addDutySchedule((new DutySchedule(newSchedule)).toString());
	}
	
        //forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/users/modifyUser.jsp");
        dispatcher.forward( request, response );
    }
}
