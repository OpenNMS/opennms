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
// Modifications:
//
// 2008 Apr 25: Don't save a user if they're read-only and you're not admin - ranger@opennms.org
// 2007 Jul 24: Add serialVersionUID. - dj@opennms.org
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

package org.opennms.web.account.selfService;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.User;
import org.opennms.web.springframework.security.Authentication;

/**
 * A servlet that handles changing a user's password
 * 
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class NewPasswordActionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            UserFactory.init();
        } catch (Exception e) {
            throw new ServletException("NewPasswordActionServlet: Error initialising user factory." + e);
        }
        HttpSession userSession = request.getSession(false);
        UserManager userFactory = UserFactory.getInstance();

        User user = (User) userSession.getAttribute("user.newPassword.jsp");
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");

        if (!request.isUserInRole(Authentication.ADMIN_ROLE) && user.isReadOnly()) {
            throw new ServletException("User " + user.getUserId() + " is read-only");
        }

        if (! userFactory.comparePasswords(user.getUserId(), currentPassword)) {
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/account/selfService/newPassword.jsp?action=redo");
            dispatcher.forward(request, response);
        } else {
            user.setPassword(UserFactory.getInstance().encryptedPassword(newPassword));

            userSession.setAttribute("user.newPassword.jsp", user);
            try {
            	userFactory.saveUser(user.getUserId(), user);
            }
            catch (Exception e) {
            	throw new ServletException("Error saving user " + user.getUserId(), e);
            }

            // forward the request for proper display
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/account/selfService/passwordChanged.jsp");
            dispatcher.forward(request, response);
        }
    }
}
