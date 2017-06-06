/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
import java.security.Principal;

import javax.security.auth.Subject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.jetty.server.Authentication.User;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Sets the Jetty specific user identity based on the current principal
 * authenticated by Spring Security.
 *
 * @author jwhite
 * @author nalvarez
 */
public class JettyUserIdentityFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(JettyUserIdentityFilter.class);

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            final Request jettyReq = Request.getBaseRequest(req);
            if (jettyReq == null) {
                LOG.warn("Failed to find org.eclipse.jetty.server.Request from javax.servlet.ServletRequest. No identity will be set.");
            } else {
                jettyReq.setAuthentication(new AuthenticationUserStub(authentication));
            }
        }
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // pass
    }

    @Override
    public void destroy() {
        // pass
    }

    private static class UserIdentityStub implements UserIdentity {
        private final Authentication authentication;

        public UserIdentityStub(Authentication authentication) {
            this.authentication = authentication;
        }

        public Subject getSubject() {
            return new Subject();
        }

        public Principal getUserPrincipal() {
            return authentication;
        }

        public boolean isUserInRole(String role, Scope scope) {
            return false;
        }
    }

    private static class AuthenticationUserStub implements User {
        private final Authentication authentication;

        public AuthenticationUserStub(Authentication authentication) {
            this.authentication = authentication;
        }

        public String getAuthMethod() {
            return null;
        }

        public UserIdentity getUserIdentity() {
            return new UserIdentityStub(authentication);
        }

        public boolean isUserInRole(UserIdentity.Scope scope, String role) {
            return false;
        }

        public void logout() {
            // pass
        }
    }

}
