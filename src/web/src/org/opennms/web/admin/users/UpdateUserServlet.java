
package org.opennms.web.admin.users;

import java.io.IOException;
import java.util.*;
import java.text.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.DutySchedule;

/**
 * A servlet that handles saving a user
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class UpdateUserServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession userSession = request.getSession(false);
	
    	if (userSession != null)
	{
		User newUser = (User)userSession.getAttribute("user.modifyUser.jsp");
		UserFactory userFactory;
		try
		{
			UserFactory.init();
		}
		catch(Exception e)
		{
			throw new ServletException("UpdateUserServlet:init Error initialising UserFactory " + e);
		}
		userFactory = UserFactory.getInstance();
		
		//get the rest of the user information from the form
		newUser.setFullName(request.getParameter("fullName"));
		newUser.setUserComments(request.getParameter("comments"));
		
		String password = request.getParameter("password");
		if (password != null && !password.trim().equals(""))
		{
			newUser.setPassword(UserFactory.encryptPassword(password));
		}
		
		String userid = newUser.getUserId();
		String email = request.getParameter("email");
                String pagerEmail = request.getParameter("pemail");
		String numericPage = request.getParameter("numericalService");
		String numericPin = request.getParameter("numericalPin");
		String textPage = request.getParameter("textService");
		String textPin = request.getParameter("textPin");

		newUser.clearContact();

		Contact tmpContact = new Contact();
		tmpContact.setInfo(email);
		tmpContact.setType("email");
		newUser.addContact(tmpContact);

                tmpContact = new Contact();
                tmpContact.setInfo(pagerEmail);
                tmpContact.setType("pagerEmail");
                newUser.addContact(tmpContact);
                
		tmpContact = new Contact();
		tmpContact.setInfo(numericPin);
		tmpContact.setServiceProvider(numericPage);
		tmpContact.setType("numericPage");
		newUser.addContact(tmpContact);

		tmpContact = new Contact();
		tmpContact.setInfo(textPin);
		tmpContact.setServiceProvider(textPage);
		tmpContact.setType("textPage");
		newUser.addContact(tmpContact);

		//build the duty schedule data structure
		Vector newSchedule = new Vector();
		ChoiceFormat days = new ChoiceFormat("0#Mo|1#Tu|2#We|3#Th|4#Fr|5#Sa|6#Su");
		
		Collection dutySchedules = newUser.getDutyScheduleCollection();
		dutySchedules.clear();
		
		int dutyCount = Integer.parseInt(request.getParameter("dutySchedules"));
		for (int duties = 0; duties < dutyCount; duties++)
		{
			newSchedule.clear();
			String deleteFlag = request.getParameter("deleteDuty"+duties);
			//don't save any duties that were marked for deletion
			if (deleteFlag == null)
			{
				for (int i = 0; i < 7; i++)
				{
					String curDayFlag = request.getParameter("duty" + duties + days.format(i));
					if (curDayFlag != null)
					{
						newSchedule.addElement(new Boolean(true));
					}
					else
					{
						newSchedule.addElement(new Boolean(false));
					}
				}
				
				newSchedule.addElement(request.getParameter("duty"+duties+"Begin"));
				newSchedule.addElement(request.getParameter("duty"+duties+"End"));
				
				DutySchedule newDuty = new DutySchedule(newSchedule);
				dutySchedules.add(newDuty.toString());
			}
		}
		userSession.setAttribute("user.modifyUser.jsp", newUser); 
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward( request, response );
    }
}
