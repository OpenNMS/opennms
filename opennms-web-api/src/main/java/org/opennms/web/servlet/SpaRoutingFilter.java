/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
