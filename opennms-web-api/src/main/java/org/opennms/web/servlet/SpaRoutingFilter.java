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
package org.opennms.web.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

@WebFilter(asyncSupported = true, urlPatterns = "/*")
public class SpaRoutingFilter implements Filter {

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final String uri = httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length());
        if (isClientPath(uri)) {
            chain.doFilter(new HttpServletRequestWrapper(httpServletRequest) {
                @Override
                public String getServletPath() {
                    return "/ui/index.html";
                }
            }, response);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isClientPath(final String uri) {
        return uri.startsWith("/ui/")
                && !uri.startsWith("/ui/assets/")
                && !uri.endsWith(".svg");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // pass
    }

    @Override
    public void destroy() {
        // pass
    }
}
