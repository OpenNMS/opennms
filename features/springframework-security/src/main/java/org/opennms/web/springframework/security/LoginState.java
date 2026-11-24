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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

/**
 * Tells whether the user of a request is logged in, also where the Spring Security filter does not run.
 *
 * On an error page (an error dispatch, e.g. for a 404 or an ElementNotFoundException), the security
 * filter has already finished, so {@link HttpServletRequest#getRemoteUser()} is null even for a
 * logged-in user. The login is still in the session.
 */
public final class LoginState {
    private LoginState() {}

    public static boolean isLoggedIn(final HttpServletRequest request) {
        if (request.getRemoteUser() != null) {
            return true;
        }
        final HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        final Object context = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (!(context instanceof SecurityContext)) {
            return false;
        }
        final Authentication authentication = ((SecurityContext) context).getAuthentication();
        return authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
