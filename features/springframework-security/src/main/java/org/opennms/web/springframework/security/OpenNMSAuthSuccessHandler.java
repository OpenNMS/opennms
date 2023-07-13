/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

                if (!this.isAlwaysUseDefaultTargetUrl() && (targetUrlParameter == null || !StringUtils.hasText(request.getParameter(targetUrlParameter)))) {
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
