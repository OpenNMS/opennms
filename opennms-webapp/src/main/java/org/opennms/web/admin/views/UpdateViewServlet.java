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

package org.opennms.web.admin.views;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.netmgt.config.views.Member;
import org.opennms.netmgt.config.views.Membership;
import org.opennms.netmgt.config.views.View;

/**
 * A servlet that handles saving a view
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class UpdateViewServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -29922796393463161L;

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession userSession = request.getSession(false);

        if (userSession != null) {
            View newView = (View) userSession.getAttribute("view.modifyView.jsp");

            newView.setMembership(null);

            String users[] = request.getParameterValues("selectedUsers");
            Membership membership = new Membership();
            if (users != null) {
                for (int i = 0; i < users.length; i++) {
                    Member member = new Member();
                    member.setType("user");
                    member.setContent(users[i]);
                    membership.addMember(member);
                }
            }

            String groups[] = request.getParameterValues("selectedGroups");
            if (groups != null) {
                for (int i = 0; i < groups.length; i++) {
                    Member member = new Member();
                    member.setType("group");
                    member.setContent(groups[i]);
                    membership.addMember(member);
                }
            }
            if (membership.getMemberCount() > 0)
                newView.setMembership(membership);
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward(request, response);
    }
}
