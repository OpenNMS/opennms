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
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.users.DutySchedule;
import org.opennms.netmgt.config.groups.Group;

/**
 * A servlet that handles adding new duty schedules to a group
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class AddGroupDutySchedulesServlet extends HttpServlet {
    private static final long serialVersionUID = -4167184420524735471L;

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession userSession = request.getSession(true);

        Group group = (Group) userSession.getAttribute("group.modifyGroup.jsp");

        Vector<Object> newSchedule = new Vector<Object>();

        int dutyAddCount = WebSecurityUtils.safeParseInt(request.getParameter("numSchedules"));

        for (int j = 0; j < dutyAddCount; j++) {
            // add 7 false boolean values for each day of the week
            for (int i = 0; i < 7; i++) {
                newSchedule.addElement(Boolean.FALSE);
            }

            // add two strings for the begin and end time
            newSchedule.addElement("0");
            newSchedule.addElement("0");

            group.addDutySchedule((new DutySchedule(newSchedule)).toString());
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/groups/modifyGroup.jsp");
        dispatcher.forward(request, response);
    }
}
