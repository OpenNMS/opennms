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

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.opennms.web.api.Util;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.SavedRequest;

/**
 * Builds the URL a user is sent to after login, or after the admin password gate.
 *
 * The target is the page the user asked for before login (the Spring Security saved
 * request), with its query string and path info. The browser never sends the URL
 * fragment to the server, so login.jsp posts it in {@link #FRAGMENT_PARAMETER}; it is
 * added back only for the Vue SPA, whose routes live in the fragment ("#/node/1").
 */
public final class LoginRedirects {
    public static final String FRAGMENT_PARAMETER = "onms_redirect_fragment";
    public static final String FRAGMENT_SESSION_ATTRIBUTE = "org.opennms.web.login.REDIRECT_FRAGMENT";
    public static final String PASSWORD_GATE_PENDING_ATTRIBUTE = "org.opennms.web.login.PASSWORD_GATE_PENDING";
    public static final String DEFAULT_TARGET_PATH = "/index.jsp";

    // A Vue hash route: "#/" followed by RFC 3986 fragment characters only (no '#', whitespace,
    // control characters, or HTML metacharacters), so it cannot inject headers or markup.
    private static final Pattern FRAGMENT_PATTERN = Pattern.compile("^#/[A-Za-z0-9\\-._~!$&'()*+,;=:@/?%]{0,2047}$");

    private static final String SPA_PATH = "/ui";

    private LoginRedirects() {}

    /**
     * @return the fragment if it is a safe Vue hash route, otherwise null
     */
    public static String sanitizeFragment(final String fragment) {
        if (fragment == null || fragment.startsWith("#//") || !FRAGMENT_PATTERN.matcher(fragment).matches()) {
            return null;
        }
        return fragment;
    }

    public static boolean isSpaPath(final String path) {
        return path != null && (path.equals(SPA_PATH) || path.startsWith(SPA_PATH + "/"));
    }

    /**
     * @return the context-relative path and query of the saved request, or null if there is
     *     none or it is not a page (see {@link LoginModuleUtils#isInvalidSavedRequestUrl})
     */
    public static String savedRequestPath(final SavedRequest saved) {
        if (!(saved instanceof DefaultSavedRequest)) {
            return null;
        }
        final DefaultSavedRequest request = (DefaultSavedRequest) saved;
        final String path = (request.getServletPath() == null ? "" : request.getServletPath())
            + (request.getPathInfo() == null ? "" : request.getPathInfo());
        if (LoginModuleUtils.isInvalidSavedRequestUrl(path)) {
            return null;
        }
        return request.getQueryString() == null ? path : path + "?" + request.getQueryString();
    }

    /**
     * @return the absolute URL to redirect to: the saved request (or {@link #DEFAULT_TARGET_PATH}),
     *     plus the fragment when the target is the Vue SPA and the fragment is safe
     */
    public static String buildTargetUrl(final HttpServletRequest request, final SavedRequest saved, final String fragment) {
        final String savedPath = savedRequestPath(saved);
        final String path = savedPath == null ? DEFAULT_TARGET_PATH : savedPath;

        final int query = path.indexOf('?');
        final String safeFragment = sanitizeFragment(fragment);
        final boolean addFragment = safeFragment != null && isSpaPath(query < 0 ? path : path.substring(0, query));

        return Util.calculateUrlBase(request, addFragment ? path + safeFragment : path);
    }
}
