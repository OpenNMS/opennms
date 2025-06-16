/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.admin.users;

import java.io.IOException;
import java.text.ChoiceFormat;
import java.time.ZoneId;
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
import org.opennms.netmgt.config.api.UserConfig.ContactType;
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

            String password = request.getParameter("password");
            if (password != null && !password.trim().equals("")) {
                final Password pass = new Password();
                pass.setEncryptedPassword(UserFactory.getInstance().encryptedPassword(password, true));
                pass.setSalt(true);
                newUser.setPassword(pass);
            }
            
            String tuiPin = request.getParameter("tuiPin");
            if (tuiPin != null && !tuiPin.trim().equals("")) {
                newUser.setTuiPin(tuiPin);
            }

            String timeZoneId = request.getParameter("timeZoneId");
            if (timeZoneId != null && !timeZoneId.trim().equals("")) {
                newUser.setTimeZoneId(timeZoneId);
            } else {
                newUser.setTimeZoneId((ZoneId) null);
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

            newUser.clearContacts();

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
                        newSchedule.add(Boolean.valueOf(curDayFlag != null));
                    }

                    int startTime = WebSecurityUtils.safeParseInt(request.getParameter("duty" + duties + "Begin"));
                    int stopTime = WebSecurityUtils.safeParseInt(request.getParameter("duty" + duties + "End"));

                    DutySchedule newDuty = new DutySchedule(newSchedule, startTime, stopTime);
                    dutySchedules.add(newDuty.toString());
                }
            }

            // The new list of roles will override the existing one.
            // If the new list is empty or null, that means the user should not have roles, and the existing ones should be removed.
            newUser.getRoles().clear();
            String[] configuredRoles = request.getParameterValues("configuredRoles");
            if (configuredRoles != null && configuredRoles.length > 0) {
                newUser.getRoles().clear();
                for (String role : configuredRoles) {
                    newUser.addRole(role);
                }
            }

            userSession.setAttribute("user.modifyUser.jsp", newUser);
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward(request, response);
    }

    private List<String> getDutySchedulesForUser(User newUser) {
        return newUser.getDutySchedules();
    }
    
}
