/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class OriginHeaderFilter implements Filter {

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            final HttpServletRequest req = (HttpServletRequest)request;
            final String header = req.getHeader("Origin");
            if (header != null && header.startsWith("file://")) {
                /* 
                 * file://* is technically an invalid Origin: for CORS, but it appears Cordova
                 * sometimes sends it so we need to filter it out.
                 */
                final List<String> headerNames = new ArrayList<>(Collections.list(req.getHeaderNames()));
                headerNames.remove("Origin");
                final HttpServletRequestWrapper newReq = new HttpServletRequestWrapper(req) {
                    @Override public Enumeration<String> getHeaderNames() {
                        return Collections.enumeration(headerNames);
                    }

                    @Override public Enumeration<String> getHeaders(final String name) {
                        if ("origin".equalsIgnoreCase(name)) {
                            return Collections.emptyEnumeration();
                        } else {
                            return super.getHeaders(name);
                        }
                    }

                    @Override public String getHeader(final String name) {
                        if ("origin".equalsIgnoreCase(name)) {
                            return null;
                        } else {
                            return super.getHeader(name);
                        }
                    }
                };
                chain.doFilter(newReq, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
