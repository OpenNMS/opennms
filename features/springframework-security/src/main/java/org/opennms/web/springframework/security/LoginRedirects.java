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

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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

    // A Vue hash route keeps only RFC 3986 fragment characters (and '%' of an existing escape); every
    // other character is percent-encoded, so the result cannot inject headers or markup.
    private static final String FRAGMENT_CHARACTERS =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~!$&'()*+,;=:@/?%";
    private static final int MAX_FRAGMENT_LENGTH = 2049;
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    // ";name=value" path parameters in a path segment, e.g. ";jsessionid=..."
    private static final Pattern PATH_PARAMETERS = Pattern.compile(";[^/]*");

    private static final String SPA_PATH = "/ui";

    private LoginRedirects() {}

    /**
     * vue-router leaves some characters unencoded in the hash, e.g. | [ ] { } ^ and, in query values,
     * `. A user-entered name in the query can contain them. Percent-encode them, and any other
     * character outside {@link #FRAGMENT_CHARACTERS}: vue-router decodes the escapes, so the route
     * stays the same.
     *
     * @return the fragment as a safe Vue hash route ("#/..."), or null if it is not one: no "#/"
     *     at the start, "#//" (looks like a host), control characters, or too long
     */
    public static String sanitizeFragment(final String fragment) {
        if (fragment == null || !fragment.startsWith("#/") || fragment.startsWith("#//") || fragment.length() > MAX_FRAGMENT_LENGTH) {
            return null;
        }
        final StringBuilder safe = new StringBuilder(fragment.length() + 16).append("#/");
        for (final byte b : fragment.substring(2).getBytes(StandardCharsets.UTF_8)) {
            final int c = b & 0xff;
            if (c < 0x20 || c == 0x7f) {
                // a control character (e.g. CR, LF) is never part of a route
                return null;
            }
            if (c < 0x80 && FRAGMENT_CHARACTERS.indexOf(c) >= 0) {
                safe.append((char) c);
            } else {
                safe.append('%').append(HEX[c >> 4]).append(HEX[c & 0x0f]);
            }
        }
        return safe.toString();
    }

    public static boolean isSpaPath(final String path) {
        return path != null && (path.equals(SPA_PATH) || path.startsWith(SPA_PATH + "/"));
    }

    /**
     * @return the servlet path and path info joined, as the servlet container decoded them (never null)
     */
    public static String decodedPath(final String servletPath, final String pathInfo) {
        return (servletPath == null ? "" : servletPath) + (pathInfo == null ? "" : pathInfo);
    }

    /**
     * @return the context-relative path and query of the saved request, as the client sent them
     *     (still encoded, without path parameters such as ";jsessionid="), or null if there is none
     *     or it is not a page (see {@link LoginModuleUtils#isInvalidSavedRequestUrl})
     */
    public static String savedRequestPath(final SavedRequest saved) {
        if (!(saved instanceof DefaultSavedRequest)) {
            return null;
        }
        final DefaultSavedRequest request = (DefaultSavedRequest) saved;
        // check the decoded path, so that an encoded name cannot hide an asset or REST path
        final String decoded = decodedPath(request.getServletPath(), request.getPathInfo());
        if (LoginModuleUtils.isInvalidSavedRequestUrl(decoded)) {
            return null;
        }
        final String path = encodedPath(request, decoded);
        return request.getQueryString() == null ? path : path + "?" + request.getQueryString();
    }

    // The request URI is not decoded, so "%3F", "%23" or "%20" in the path keep their meaning in the redirect.
    private static String encodedPath(final DefaultSavedRequest request, final String decoded) {
        final String uri = request.getRequestURI();
        final String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        if (uri == null || !uri.startsWith(contextPath)) {
            return decoded;
        }
        return PATH_PARAMETERS.matcher(uri.substring(contextPath.length())).replaceAll("");
    }

    public static boolean isPasswordGatePending(final HttpSession session) {
        return session != null && session.getAttribute(PASSWORD_GATE_PENDING_ATTRIBUTE) != null;
    }

    /**
     * @param savedPath the result of {@link #savedRequestPath}, or null for {@link #DEFAULT_TARGET_PATH}
     * @return the absolute URL to redirect to, plus the fragment when the target is the Vue SPA and
     *     the fragment is safe
     */
    public static String buildTargetUrl(final HttpServletRequest request, final String savedPath, final String fragment) {
        final String path = savedPath == null ? DEFAULT_TARGET_PATH : savedPath;

        final int query = path.indexOf('?');
        final String safeFragment = sanitizeFragment(fragment);
        final boolean addFragment = safeFragment != null && isSpaPath(query < 0 ? path : path.substring(0, query));

        return Util.calculateUrlBase(request, addFragment ? path + safeFragment : path);
    }
}
