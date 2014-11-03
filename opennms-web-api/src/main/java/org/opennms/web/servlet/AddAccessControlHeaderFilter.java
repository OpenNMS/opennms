/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * A filter that adds an HTTP <em>Access-Control-Allow-Origin</em> header to a servlet or JSP's response.
 *
 * @since 1.9.90
 */
public class AddAccessControlHeaderFilter implements Filter {
	private String m_origin = null;

        @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
    	final HttpServletResponse httpResponse = (HttpServletResponse) response;
    	if (m_origin != null && !httpResponse.containsHeader("Access-Control-Allow-Origin")) {
    		httpResponse.setHeader("Access-Control-Allow-Origin", m_origin);
    	}
        chain.doFilter(request, httpResponse);
    }

    /** {@inheritDoc} */
        @Override
    public void init(final FilterConfig config) {
    	m_origin = config.getInitParameter("origin");
    }

    /**
     * <p>destroy</p>
     */
        @Override
    public void destroy() {
    }

}
