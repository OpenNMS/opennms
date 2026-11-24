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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Answers an unauthenticated request for a protected static asset (the Vue SPA and menu bundles)
 * with a plain, uncacheable 401.
 *
 * A redirect to login.jsp would render the login page for a script or stylesheet request: that
 * creates a new session (for its CSRF token), which can replace the session of a login page open at
 * the same time, so its login fails. Browsers can also cache the redirect as the asset (NMS-20174).
 * No WWW-Authenticate header is sent, so the browser shows no login dialog.
 */
public class NoStoreUnauthorizedEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
