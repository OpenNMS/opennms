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
import org.opennms.web.springframework.security.LoginRedirects;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

public class PasswordGateActionServlet extends AbstractBasePasswordChangeActionServlet {
    private static final long serialVersionUID = 1L;

    private final transient RequestCache requestCache = new HttpSessionRequestCache();

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final HttpSession userSession = request.getSession(false);

        // The gate is only for a login with the default admin password (see OpenNMSAuthSuccessHandler).
        // At any other time, do nothing and go to the main page.
        if (!LoginRedirects.isPasswordGatePending(userSession)) {
            response.sendRedirect(LoginRedirects.buildTargetUrl(request, null, null));
            return;
        }

        // after the gate, go to the page the user asked for before login, or to the main page
        final String targetUrl = LoginRedirects.buildTargetUrl(request,
            LoginRedirects.savedRequestPath(requestCache.getRequest(request, response)),
            (String) userSession.getAttribute(LoginRedirects.FRAGMENT_SESSION_ATTRIBUTE));

        final String skip = request.getParameter("skip");

        if (skip != null && skip.equals("1")) {
            finishGate(request, response, userSession);
            response.sendRedirect(targetUrl);
            return;
        }

        final UserManager userFactory = getUserManager();
        final String userid = request.getRemoteUser();
        final User user = userFactory.getUser(userid);

        readonlyUserCheck(request, user);

        final String currentPassword = request.getParameter("currentPassword");
        final String newPassword = request.getParameter("newPassword");

        final boolean changed = verifyAndChangePassword(userFactory, userSession, user,
            request, response, currentPassword, newPassword,
            "/account/selfService/passwordGate.jsp?action=redo",
            targetUrl,
            true);

        if (changed) {
            finishGate(request, response, userSession);
        }
    }

    protected UserManager getUserManager() throws ServletException {
        initUserFactory("PasswordGateActionServlet");
        return UserFactory.getInstance();
    }

    private void finishGate(HttpServletRequest request, HttpServletResponse response, HttpSession userSession) {
        userSession.removeAttribute(LoginRedirects.PASSWORD_GATE_PENDING_ATTRIBUTE);
        userSession.removeAttribute(LoginRedirects.FRAGMENT_SESSION_ATTRIBUTE);
        requestCache.removeRequest(request, response);
    }
}
