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
package org.opennms.web.springframework.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.web.api.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.util.StringUtils;

@SuppressWarnings("java:S2068")
public class OpenNMSAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    // username/password combination that triggers the password gate, which prompts
    // user to change the default "admin" password

    public static final String PASSWORD_GATE_USERNAME = "admin";
    public static final String PASSWORD_GATE_PASSWORD = "admin";

    protected final Logger logger = LoggerFactory.getLogger(OpenNMSAuthSuccessHandler.class);
    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        // changing JSESSIONID to prevent Session Fixation attacks, see NMS-15310
        request.changeSessionId();

        // login.jsp posts the URL fragment, which the browser never sends to the server
        final String fragment = request.getParameter(LoginRedirects.FRAGMENT_PARAMETER);

        // check for admin/admin
        boolean defaultAdminLogin = isDefaultAdminLogin(request.getParameter("j_username"), request.getParameter("j_password"));

        if (defaultAdminLogin) {
            handleDefaultAdminLogin(request, response, fragment);
        } else {
            // a login after an admin/admin login in the same session must not inherit its password gate
            final HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(LoginRedirects.PASSWORD_GATE_PENDING_ATTRIBUTE);
                session.removeAttribute(LoginRedirects.FRAGMENT_SESSION_ATTRIBUTE);
            }

            // null if there is no saved request, or it is for an asset or REST call (not a page to go back to)
            final String savedPath = LoginRedirects.savedRequestPath(this.requestCache.getRequest(request, response));
            final String targetUrlParameter = this.getTargetUrlParameter();

            final boolean useSavedRequest = savedPath != null && !this.isAlwaysUseDefaultTargetUrl() &&
                (targetUrlParameter == null || !StringUtils.hasText(request.getParameter(targetUrlParameter)));

            final String targetUrl = useSavedRequest
                ? LoginRedirects.buildTargetUrl(request, savedPath, fragment)
                : createTargetURL(request, response);

            this.requestCache.removeRequest(request, response);
            this.clearAuthenticationAttributes(request);
            this.logger.debug("Redirecting to target URL after login: {}", targetUrl);
            this.getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }

    /**
     * 'admin' user is logged in successfully, but with default "admin" password, redirect to password gate page.
     * The saved request and the fragment stay in the session, so that PasswordGateActionServlet can send
     * the user on to the page they asked for.
     */
    private void handleDefaultAdminLogin(HttpServletRequest request, HttpServletResponse response, String fragment) throws IOException {
        final HttpSession session = request.getSession();
        session.setAttribute(LoginRedirects.PASSWORD_GATE_PENDING_ATTRIBUTE, Boolean.TRUE);

        final String safeFragment = LoginRedirects.sanitizeFragment(fragment);
        if (safeFragment != null) {
            session.setAttribute(LoginRedirects.FRAGMENT_SESSION_ATTRIBUTE, safeFragment);
        } else {
            session.removeAttribute(LoginRedirects.FRAGMENT_SESSION_ATTRIBUTE);
        }

        final String targetUrl = Util.calculateUrlBase(request, "/account/selfService/passwordGate.jsp");
        this.logger.debug("User used default admin password. Redirecting to Password Gate, url: " + targetUrl);
        super.clearAuthenticationAttributes(request);
        this.getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String createTargetURL(HttpServletRequest request, HttpServletResponse response) {
        return Util.calculateUrlBase(request, determineTargetUrl(request, response));
    }

    private boolean isDefaultAdminLogin(String username, String password) {
        return username != null && username.equals(PASSWORD_GATE_USERNAME) &&
            password != null && password.equals(PASSWORD_GATE_PASSWORD);
    }
}
