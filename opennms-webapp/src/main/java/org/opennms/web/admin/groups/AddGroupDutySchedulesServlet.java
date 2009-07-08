/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
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

import org.opennms.netmgt.config.users.DutySchedule;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.web.WebSecurityUtils;

/**
 * A servlet that handles adding new duty schedules to a group
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class AddGroupDutySchedulesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession userSession = request.getSession(true);

        Group group = (Group) userSession.getAttribute("group.modifyGroup.jsp");

        Vector<Object> newSchedule = new Vector<Object>();

        int dutyAddCount = WebSecurityUtils.safeParseInt(request.getParameter("numSchedules"));

        for (int j = 0; j < dutyAddCount; j++) {
            // add 7 false boolean values for each day of the week
            for (int i = 0; i < 7; i++) {
                newSchedule.addElement(new Boolean(false));
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
