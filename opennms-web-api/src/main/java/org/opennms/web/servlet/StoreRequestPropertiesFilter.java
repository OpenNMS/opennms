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
import javax.servlet.http.HttpServletRequest;

/**
 * <p>StoreRequestPropertiesFilter class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class StoreRequestPropertiesFilter implements Filter {

    private String m_servletPathAttribute;
    private String m_relativeServletPathAttribute;

    /** {@inheritDoc} */
    @Override
    public void init(FilterConfig config) throws ServletException {
        m_servletPathAttribute = config.getInitParameter("servletPathAttribute");
        m_relativeServletPathAttribute = config.getInitParameter("relativeServletPathAttribute");
    }

    /** {@inheritDoc} */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (m_servletPathAttribute != null) {
            request.setAttribute(m_servletPathAttribute,
                                 httpRequest.getServletPath());
        }
        if (m_relativeServletPathAttribute != null) {
            String servletPath = httpRequest.getServletPath();
            if (servletPath != null && servletPath.length() > 0 && servletPath.charAt(0) == '/') {
                servletPath = servletPath.substring(1);
            }
            request.setAttribute(m_relativeServletPathAttribute,
                                 servletPath);
        }
        
        chain.doFilter(request, response);
    }

    /**
     * <p>destroy</p>
     */
    @Override
    public void destroy() {
        // Nothing to destroy that a GC won't take care of. :-)
    }

}
