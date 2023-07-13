/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.User;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

public class PasswordGateActionServlet extends AbstractBasePasswordChangeActionServlet {
    private static final long serialVersionUID = 1L;

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        initUserFactory("PasswordGateActionServlet");

        final String skip = request.getParameter("skip");

        if (skip != null && skip.equals("1")) {
            // skip was clicked, redirect either back to originally requested page or to the main page
            response.sendRedirect(getRedirectPath(request, response));
            return;
        }

        final UserManager userFactory = UserFactory.getInstance();
        final String userid = request.getRemoteUser();
        final User user = userFactory.getUser(userid);

        readonlyUserCheck(request, user);

        final HttpSession userSession = request.getSession(false);
        final String currentPassword = request.getParameter("currentPassword");
        final String newPassword = request.getParameter("newPassword");

        verifyAndChangePassword(userFactory, userSession, user,
            request, response, currentPassword, newPassword,
            "/account/selfService/passwordGate.jsp?action=redo");
    }

    private String getRedirectPath(HttpServletRequest request, HttpServletResponse response) {
        final RequestCache requestCache = new HttpSessionRequestCache();
        final DefaultSavedRequest savedRequest = (DefaultSavedRequest) requestCache.getRequest(request, response);
        final String servletPath = savedRequest != null ? savedRequest.getServletPath() : null;
        final String path = servletPath != null && !servletPath.isEmpty() ? servletPath : "/index.jsp";

        return request.getContextPath() + path;
    }
}
