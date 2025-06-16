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

        @Override
        public String getAuthMethod() {
            return null;
        }

        @Override
        public UserIdentity getUserIdentity() {
            return new UserIdentityStub(authentication);
        }

        @Override
        public boolean isUserInRole(UserIdentity.Scope scope, String role) {
            return false;
        }

        @Override
        public void logout() {
            // pass
        }

        @Override
        public org.eclipse.jetty.server.Authentication logout(final ServletRequest request) {
            return null;
        }
    }

}
