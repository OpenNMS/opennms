/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
            "/account/selfService/passwordGate.jsp?action=redo",
            getRedirectPath(request, response),
            true);
    }

    private String getRedirectPath(HttpServletRequest request, HttpServletResponse response) {
        final RequestCache requestCache = new HttpSessionRequestCache();
        final DefaultSavedRequest savedRequest = (DefaultSavedRequest) requestCache.getRequest(request, response);
        final String servletPath = savedRequest != null ? savedRequest.getServletPath() : null;
        final String path = servletPath != null && !servletPath.isEmpty() &&
                !servletPath.endsWith("/ui-components/assets/index.js")
                ? servletPath : "/index.jsp";

        return request.getContextPath() + path;
    }
}
