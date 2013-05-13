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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.Password;
import org.opennms.netmgt.config.users.User;

/**
 * A servlet that handles adding a new user
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class AddNewUserServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 9221831285444697701L;

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            UserFactory.init();
        } catch (Throwable e) {
            throw new ServletException("AddNewUserServlet: Error initialising user factory." + e);
        }
        UserManager userFactory = UserFactory.getInstance();

        String userID = request.getParameter("userID");
        String password = request.getParameter("pass1");

        boolean hasUser = false;
        try {
            hasUser = userFactory.hasUser(userID);
        } catch (Throwable e) {
            throw new ServletException("can't determine if user " + userID + " already exists in users.xml.", e);
        }

        if (hasUser) {
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/users/newUser.jsp?action=redo");
            dispatcher.forward(request, response);
        } else {
            final Password pass = new Password();
            pass.setContent(UserFactory.getInstance().encryptedPassword(password, true));
            pass.setSalt(true);

            final User newUser = new User();
            newUser.setUserId(userID);
            newUser.setPassword(pass);

            final HttpSession userSession = request.getSession(false);
            userSession.setAttribute("user.modifyUser.jsp", newUser);

            // forward the request for proper display
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/users/modifyUser.jsp");
            dispatcher.forward(request, response);
        }
    }
}
