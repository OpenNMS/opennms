/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.admin.users;

import java.io.IOException;
import java.text.ChoiceFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager.ContactType;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.DutySchedule;
import org.opennms.netmgt.config.users.Password;
import org.opennms.netmgt.config.users.User;

/**
 * A servlet that handles saving a user
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class UpdateUserServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -945279264373810897L;

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession userSession = request.getSession(false);

        if (userSession != null) {
            User newUser = (User) userSession.getAttribute("user.modifyUser.jsp");
            try {
                UserFactory.init();
            } catch (Throwable e) {
                throw new ServletException("UpdateUserServlet:init Error initialising UserFactory " + e);
            }
            
            // get the rest of the user information from the form
            newUser.setFullName(request.getParameter("fullName"));
            newUser.setUserComments(request.getParameter("userComments"));
            newUser.setReadOnly(false);
            if (request.getParameter("readOnly") != null && (request.getParameter("readOnly").equalsIgnoreCase("true") || request.getParameter("readOnly").equalsIgnoreCase("on"))) {
                newUser.setReadOnly(true);
            }

            String password = request.getParameter("password");
            if (password != null && !password.trim().equals("")) {
                final Password pass = new Password();
                pass.setContent(UserFactory.getInstance().encryptedPassword(password, true));
                pass.setSalt(true);
                newUser.setPassword(pass);
            }
            
            String tuiPin = request.getParameter("tuiPin");
            if (tuiPin != null && !tuiPin.trim().equals("")) {
                newUser.setTuiPin(tuiPin);
            }

            String email = request.getParameter(ContactType.email.toString());
            String pagerEmail = request.getParameter("pemail");
            String xmppAddress = request.getParameter(ContactType.xmppAddress.toString());
            String microblog = request.getParameter(ContactType.microblog.toString());
            String numericPage = request.getParameter("numericalService");
            String numericPin = request.getParameter("numericalPin");
            String textPage = request.getParameter("textService");
            String textPin = request.getParameter("textPin");
            String workPhone = request.getParameter(ContactType.workPhone.toString());
            String mobilePhone = request.getParameter(ContactType.mobilePhone.toString());
            String homePhone = request.getParameter(ContactType.homePhone.toString());

            newUser.removeAllContact();

            Contact tmpContact = new Contact();
            tmpContact.setInfo(email);
            tmpContact.setType(ContactType.email.toString());
            newUser.addContact(tmpContact);

            tmpContact = new Contact();
            tmpContact.setInfo(pagerEmail);
            tmpContact.setType(ContactType.pagerEmail.toString());
            newUser.addContact(tmpContact);

            tmpContact = new Contact();
            tmpContact.setInfo(xmppAddress);
            tmpContact.setType(ContactType.xmppAddress.toString());
            newUser.addContact(tmpContact);
            
            tmpContact = new Contact();
            tmpContact.setInfo(microblog);
            tmpContact.setType(ContactType.microblog.toString());
            newUser.addContact(tmpContact);
            
            tmpContact = new Contact();
            tmpContact.setInfo(numericPin);
            tmpContact.setServiceProvider(numericPage);
            tmpContact.setType(ContactType.numericPage.toString());
            newUser.addContact(tmpContact);

            tmpContact = new Contact();
            tmpContact.setInfo(textPin);
            tmpContact.setServiceProvider(textPage);
            tmpContact.setType(ContactType.textPage.toString());
            newUser.addContact(tmpContact);
            
            tmpContact = new Contact();
            tmpContact.setInfo(workPhone);
            tmpContact.setType(ContactType.workPhone.toString());
            newUser.addContact(tmpContact);
            
            tmpContact = new Contact();
            tmpContact.setInfo(mobilePhone);
            tmpContact.setType(ContactType.mobilePhone.toString());
            newUser.addContact(tmpContact);

            tmpContact = new Contact();
            tmpContact.setInfo(homePhone);
            tmpContact.setType(ContactType.homePhone.toString());
            newUser.addContact(tmpContact);

            // build the duty schedule data structure
            List<Boolean> newSchedule = new ArrayList<Boolean>(7);
            ChoiceFormat days = new ChoiceFormat("0#Mo|1#Tu|2#We|3#Th|4#Fr|5#Sa|6#Su");

            Collection<String> dutySchedules = getDutySchedulesForUser(newUser);
            dutySchedules.clear();

            int dutyCount = WebSecurityUtils.safeParseInt(request.getParameter("dutySchedules"));
            for (int duties = 0; duties < dutyCount; duties++) {
                newSchedule.clear();
                String deleteFlag = request.getParameter("deleteDuty" + duties);
                // don't save any duties that were marked for deletion
                if (deleteFlag == null) {
                    for (int i = 0; i < 7; i++) {
                        String curDayFlag = request.getParameter("duty" + duties + days.format(i));
                        newSchedule.add(new Boolean(curDayFlag != null));
                    }

                    int startTime = WebSecurityUtils.safeParseInt(request.getParameter("duty" + duties + "Begin"));
                    int stopTime = WebSecurityUtils.safeParseInt(request.getParameter("duty" + duties + "End"));

                    DutySchedule newDuty = new DutySchedule(newSchedule, startTime, stopTime);
                    dutySchedules.add(newDuty.toString());
                }
            }

            userSession.setAttribute("user.modifyUser.jsp", newUser);
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward(request, response);
    }

    private List<String> getDutySchedulesForUser(User newUser) {
        return newUser.getDutyScheduleCollection();
    }
    
}
