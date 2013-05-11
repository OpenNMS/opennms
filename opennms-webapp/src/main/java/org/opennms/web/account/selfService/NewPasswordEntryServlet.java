/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class NewPasswordEntryServlet extends HttpServlet {
    private static final long serialVersionUID = 8979500859478109256L;

    /** {@inheritDoc} */
    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession userSession = request.getSession(false);
		
		try {
            UserFactory.init();
        } catch (Throwable e) {
            throw new ServletException("NewPasswordEntryServlet: Error initialising user factory." + e);
        }
        UserManager userFactory = UserFactory.getInstance();
        
        if (userSession != null) {
            String userid = request.getRemoteUser();
            try {
                User user = userFactory.getUser(userid);
                userSession.setAttribute("user.newPassword.jsp", user);
            }
            catch (Throwable e) {
                throw new ServletException("Couldn't initialize UserFactory", e);
            }
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/account/selfService/newPassword.jsp");
            dispatcher.forward(request, response);
        }
	}
}
