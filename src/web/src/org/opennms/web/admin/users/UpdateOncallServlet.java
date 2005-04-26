//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.admin.users;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.netmgt.config.common.Time;
import org.opennms.netmgt.config.users.OncallSchedule;
import org.opennms.netmgt.config.users.User;

/**
 * A servlet that handles adding new duty schedules to a users notification info
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class UpdateOncallServlet extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession userSession = request.getSession(true);

        User user = (User) userSession.getAttribute("user.modifyUser.jsp");
        
        
        String action = request.getParameter("schedAction");
        if ("deleteTime".equals(action)) {
            int schedIndex = Integer.parseInt(request.getParameter("schedIndex"));
            int timeIndex = Integer.parseInt(request.getParameter("schedTimeIndex"));
            
            user.getOncallSchedule(schedIndex).getTimeCollection().remove(timeIndex);
            
        } else if ("addTime".equals(action)) {
            int schedIndex = Integer.parseInt(request.getParameter("schedIndex"));
            
            Time time = new Time();
            time.setDay("sunday");
            time.setBegins("00:00:00");
            time.setEnds("00:00:00");
            
            user.getOncallSchedule(schedIndex).addTime(time);
        } else if ("addSchedule".equals(action)) {
            String newType = request.getParameter("addOncallType");
            
            OncallSchedule sched = new OncallSchedule();
            sched.setName("new");
            sched.setType(newType);
            Time time = new Time();
            if ("weekly".equals(newType)) {
                time.setDay("sunday");
                time.setBegins("00:00:00");
                time.setEnds("00:00:00");
            } else if ("monthly".equals(newType)) {
                time.setDay("1");
                time.setBegins("00:00:00");
                time.setEnds("00:00:00");
            } else {
                Date date = new Date();
                SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy 00:00:00");
                String day = format.format(date);
                time.setBegins(day);
                time.setEnds(day);
            }
            sched.addTime(time);
            user.addOncallSchedule(sched);
        } else if ("deleteSchedule".equals(action)) {
            int schedIndex = Integer.parseInt(request.getParameter("schedIndex"));
            user.getOncallScheduleCollection().remove(schedIndex);
        } else {
            throw new ServletException("Unrecognized Action: "+action);
        }


        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/users/modifyUser.jsp");
        dispatcher.forward(request, response);
    }
}
