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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.api.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.RedirectUrlBuilder;
import org.springframework.security.web.util.UrlUtils;

public class OpenNMSLoginUrlAuthEntryPoint extends LoginUrlAuthenticationEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(OpenNMSLoginUrlAuthEntryPoint.class);
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public OpenNMSLoginUrlAuthEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String redirectUrl = null;
        if (isUseForward()) {
            if (isHttpsEnabled(request) && "http".equals(request.getScheme())) {
                // First redirect the current request to HTTPS.
                // When that request is received, the forward to the login page will be used.
                redirectUrl = buildHttpsRedirectUrlForRequest(request);
            }
            if (redirectUrl == null) {
                String loginForm = determineUrlToUseForThisRequest(request, response, authException);
                if (logger.isDebugEnabled()) {
                    logger.debug("Server side forward to: {}", loginForm);
                }
                RequestDispatcher dispatcher = request.getRequestDispatcher(loginForm);
                dispatcher.forward(request, response);
                return;
            }
        } else {
            // redirect to login page. Use https if forceHttps true
            redirectUrl = buildRedirectUrlToLoginPage(request, response, authException);
        }
        redirectStrategy.sendRedirect(request, response, redirectUrl);
    }

    @Override
    protected String buildRedirectUrlToLoginPage(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        String loginForm = determineUrlToUseForThisRequest(request, response, authException);
        if (UrlUtils.isAbsoluteUrl(loginForm)) {
            return loginForm;
        }
        int serverPort = getPortResolver().getServerPort(request);
        String scheme = request.getScheme();
        RedirectUrlBuilder urlBuilder = new RedirectUrlBuilder();
        urlBuilder.setScheme(scheme);
        urlBuilder.setServerName(request.getServerName());
        urlBuilder.setPort(serverPort);
        urlBuilder.setContextPath(request.getContextPath());
        urlBuilder.setPathInfo(loginForm);
        if (isHttpsEnabled(request) && "http".equals(scheme)) {
            Integer httpsPort = getPortMapper().lookupHttpsPort(serverPort);
            if (httpsPort != null) {
                // Overwrite scheme and port in the redirect URL
                urlBuilder.setScheme("https");
                urlBuilder.setPort(httpsPort);
            } else {
                logger.warn("Unable to redirect to HTTPS as no port mapping found for HTTP port {}", serverPort);
            }
        }
        return urlBuilder.getUrl();
    }

    protected boolean isHttpsEnabled(HttpServletRequest request) {
        return Util.calculateUrlBase(request).startsWith("https:");
    }
}
