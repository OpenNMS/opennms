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

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Selects the unauthenticated requests that the request cache saves as the post-login target.
 *
 * Only a browser navigation to a page qualifies: a GET that accepts HTML and is not an XHR.
 * Without this, any later unauthenticated GET (an image, font, script or XHR, e.g. from
 * another tab) replaces the saved page, and the user lands on the default page after login.
 */
public class PageNavigationRequestMatcher implements RequestMatcher {
    @Override
    public boolean matches(final HttpServletRequest request) {
        if (!"GET".equals(request.getMethod())) {
            return false;
        }
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return false;
        }
        final String accept = request.getHeader("Accept");
        if (accept == null || !accept.toLowerCase(Locale.ROOT).contains("text/html")) {
            return false;
        }
        final String path = (request.getServletPath() == null ? "" : request.getServletPath())
            + (request.getPathInfo() == null ? "" : request.getPathInfo());
        return !LoginModuleUtils.isInvalidSavedRequestUrl(path);
    }
}
