/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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

/**
 * A servlet that retrieves a user's password in preparation for changing the password
 * 
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class NewPasswordEntryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession userSession = request.getSession(false);
		
		try {
            UserFactory.init();
        } catch (Exception e) {
            throw new ServletException("NewPasswordEntryServlet: Error initialising user factory." + e);
        }
        UserManager userFactory = UserFactory.getInstance();
        
        if (userSession != null) {
            String userid = request.getRemoteUser();
            try {
                User user = userFactory.getUser(userid);
                userSession.setAttribute("user.newPassword.jsp", user);
            }
            catch (Exception e) {
                throw new ServletException("Couldn't initialize UserFactory", e);
            }
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/account/selfService/newPassword.jsp");
            dispatcher.forward(request, response);
        }
	}
}
