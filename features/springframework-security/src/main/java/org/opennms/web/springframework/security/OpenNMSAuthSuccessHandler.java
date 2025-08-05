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

import org.opennms.web.api.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
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

        // check for admin/admin
        boolean defaultAdminLogin = isDefaultAdminLogin(request.getParameter("j_username"), request.getParameter("j_password"));

        if (defaultAdminLogin) {
            handleDefaultAdminLogin(request, response);
        } else {
            final DefaultSavedRequest savedRequest = (DefaultSavedRequest) this.requestCache.getRequest(request, response);

            if (savedRequest == null) {
                super.clearAuthenticationAttributes(request);
                this.getRedirectStrategy().sendRedirect(request, response, createTargetURL(request, response));
            } else {
                String targetUrlParameter = this.getTargetUrlParameter();

                boolean useSavedRequest = !this.isAlwaysUseDefaultTargetUrl() &&
                    (targetUrlParameter == null || !StringUtils.hasText(request.getParameter(targetUrlParameter)));

                // make sure we are redirecting to an actual page, not e.g. a URL to an asset
                // TODO: Determine why assets are getting saved in the requestCache
                if (useSavedRequest) {
                    final String servletPathLower = savedRequest.getServletPath().toLowerCase();

                    if (LoginModuleUtils.isInvalidSavedRequestUrl(servletPathLower)) {
                        useSavedRequest = false;
                    }
                }

                if (useSavedRequest) {
                    this.clearAuthenticationAttributes(request);
                    final String targetUrl = Util.calculateUrlBase(request, savedRequest.getServletPath() + (savedRequest.getQueryString() == null ? "" : "?" + savedRequest.getQueryString()));
                    this.logger.debug("Redirecting to DefaultSavedRequest Url: " + targetUrl);
                    this.getRedirectStrategy().sendRedirect(request, response, targetUrl);
                } else {
                    this.requestCache.removeRequest(request, response);
                    this.getRedirectStrategy().sendRedirect(request, response, createTargetURL(request, response));
                }
            }
        }
    }

    /**
     * 'admin' user is logged in successfully, but with default "admin" password, redirect to password gate page.
     */
    private void handleDefaultAdminLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
