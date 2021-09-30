/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.web.security;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

/** Sets the header "cache-control: no-cache" if the response contains the jsessionid. This is done for security reasons;
 * Web servers use session cookies to identify the active user sessions.  Disclosing these session cookies to an
 * attacker can result in a session hijacking attack. With this in mind, session cookies should be treated as sensitive
 * data and should be well protected. With a secure cache-control policy in place, session cookies are typically stored
 * in the browser memory instead of flushed to the hard drive. When a browsing session is terminated, the corresponding
 * session cookies are deleted from the client machine.  However, session cookies can also be kept in a cached web page.
 * For example, when a user login in a web site, the returned web page may contain session cookies in the “set-cookie”
 * response headers.  If the “cache-control” header in this page is defined as “public”, all proxy servers and gateways
 * between the client and server are allowed to cache this page. Thus, the risk of exposing these sensitive session
 * cookies to an attacker is significantly increased. It’s recommended to change the cache-control header to secure
 * values, e.g., no-cache.    Similar to a password, if an attacker steals the session cookies that represent a valid
 * user session, they can then use them to masquerade as victim users and access their personal data. If a victim user
 * has administrative privileges, then the security of entire web site is at risk.
 */
public class JSessionIdNoCacheFilter implements Filter {

    private final static String PARAM_SESSION_ID_NAME = "sessionIdName";
    private final static String DEFAULT_SESSION_ID_NAME = "JSESSIONID";

    private String sessionIdName = DEFAULT_SESSION_ID_NAME;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(request, response);

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            addHeaderIfRequired((HttpServletRequest)request, (HttpServletResponse)response);
        }
    }

    private void addHeaderIfRequired(final HttpServletRequest request, final HttpServletResponse response) {
        boolean cacheControlHeaderNeeded = false;

        if (request.getSession(false) != null) {
            cacheControlHeaderNeeded = true;
        }

        if (!cacheControlHeaderNeeded) {
            final Collection<String> cookies = response.getHeaders("Set-Cookie");

            // check for session id in cookies
            for(final String cookie : cookies) {
                if(cookie.contains(sessionIdName)){
                    cacheControlHeaderNeeded = true;
                    break;
                }
            }
        }

        if (!cacheControlHeaderNeeded) {
            // check for session id in location url
            String location = response.getHeader("Location");
            if(location != null && location.contains(sessionIdName.toLowerCase())) {
                cacheControlHeaderNeeded = true;
            }
        }

        if(cacheControlHeaderNeeded) {
            response.addHeader("Cache-Control", "no-cache");
        }
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }

    @Override
    public void init(FilterConfig filterConfig) {
        String value = filterConfig.getInitParameter(PARAM_SESSION_ID_NAME);
        if(!Strings.isNullOrEmpty(value)) {
            this.sessionIdName = value;
        }
    }
}
