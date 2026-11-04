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
 * Only a navigation to a page qualifies. Without this, any later unauthenticated GET (an image,
 * font, script or XHR, e.g. from another tab) replaces the saved page, and the user lands on the
 * default page after login. Saving also creates a session, so each saved asset request would
 * create one more.
 *
 * Modern browsers send Fetch Metadata headers: a top-level page navigation has
 * "Sec-Fetch-Mode: navigate" and "Sec-Fetch-Dest: document". Iframe navigations (e.g. embedded
 * Vaadin or Grafana pages) and subresources are not saved. Older clients without these headers
 * are saved when they accept HTML, accept anything ("*&#47;*" alone) or send no Accept header,
 * for kiosk tools and embedded browsers that request pages that way. "Accept: *&#47;*" with
 * Fetch Metadata is a subresource and is not saved.
 *
 * Browsers send Fetch Metadata only to HTTPS and localhost. Over plain HTTP to another host, the
 * Accept rules apply to all browsers too: an iframe navigation (Accept: text/html) is then saved,
 * as before NMS-20178. Scripts, stylesheets, images and fonts are still not saved, because of
 * their Accept header or their file suffix (see LoginModuleUtils).
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
        if (LoginModuleUtils.isInvalidSavedRequestUrl(LoginRedirects.decodedPath(request.getServletPath(), request.getPathInfo()))) {
            return false;
        }
        final String mode = request.getHeader("Sec-Fetch-Mode");
        if (mode != null) {
            final String dest = request.getHeader("Sec-Fetch-Dest");
            return "navigate".equals(mode) && (dest == null || "document".equals(dest));
        }
        final String accept = request.getHeader("Accept");
        return accept == null || accept.isBlank() || "*/*".equals(accept.trim())
            || accept.toLowerCase(Locale.ROOT).contains("text/html");
    }
}
