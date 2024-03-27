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
