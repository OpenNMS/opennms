/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022-2023 The OpenNMS Group, Inc.
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

package org.opennms.web.springframework.security;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
        final String loginForm = determineUrlToUseForThisRequest(request, response, authException);
        if (UrlUtils.isAbsoluteUrl(loginForm)) {
            return loginForm;
        }

        final URL url;

        try {
            url = new URL(Util.calculateUrlBase(request, loginForm));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        final RedirectUrlBuilder urlBuilder = new RedirectUrlBuilder();
        urlBuilder.setScheme(url.getProtocol());
        urlBuilder.setServerName(url.getHost());
        urlBuilder.setPort(url.getPort() == -1 ? url.getDefaultPort() : url.getPort());
        urlBuilder.setContextPath(url.getPath());
        urlBuilder.setPathInfo(url.getQuery());
        return urlBuilder.getUrl();
    }

    protected boolean isHttpsEnabled(HttpServletRequest request) {
        return Util.calculateUrlBase(request).startsWith("https:");
    }
}
