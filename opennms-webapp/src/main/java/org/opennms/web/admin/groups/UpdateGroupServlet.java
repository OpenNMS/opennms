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

package org.opennms.web.admin.groups;

import java.io.IOException;
import java.text.ChoiceFormat;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.users.DutySchedule;

/**
 * A servlet that handles saving a group
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class UpdateGroupServlet extends HttpServlet {
    private static final long serialVersionUID = -4328190323404240442L;

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession userSession = request.getSession(false);

        if (userSession != null) {
            //group.modifyGroup.jsp
            Group newGroup = (Group) userSession.getAttribute("group");

            // get the rest of the group information from the form
            newGroup.removeAllUser();

            String users[] = request.getParameterValues("selectedUsers");

            if (users != null) {
                for (int i = 0; i < users.length; i++) {
                    newGroup.addUser(users[i]);
                }
            }

            Vector<Object> newSchedule = new Vector<Object>();
            ChoiceFormat days = new ChoiceFormat("0#Mo|1#Tu|2#We|3#Th|4#Fr|5#Sa|6#Su");

            Collection<String> dutySchedules = getDutySchedulesForGroup(newGroup);
            dutySchedules.clear();

            int dutyCount = WebSecurityUtils.safeParseInt(request.getParameter("dutySchedules"));
            for (int duties = 0; duties < dutyCount; duties++) {
                newSchedule.clear();
                String deleteFlag = request.getParameter("deleteDuty" + duties);
                // don't save any duties that were marked for deletion
                if (deleteFlag == null) {
                    for (int i = 0; i < 7; i++) {
                        String curDayFlag = request.getParameter("duty" + duties + days.format(i));
                        if (curDayFlag != null) {
                            newSchedule.addElement(Boolean.TRUE);
                        } else {
                            newSchedule.addElement(Boolean.FALSE);
                        }
                    }

                    newSchedule.addElement(request.getParameter("duty" + duties + "Begin"));
                    newSchedule.addElement(request.getParameter("duty" + duties + "End"));

                    DutySchedule newDuty = new DutySchedule(newSchedule);
                    dutySchedules.add(newDuty.toString());
                }
            }
            userSession.setAttribute("group", newGroup);
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward(request, response);
    }

    private List<String> getDutySchedulesForGroup(Group group) {
        return (List<String>) group.getDutyScheduleCollection();
    }
}
