//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web;

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
    public void init(FilterConfig config) throws ServletException {
        m_servletPathAttribute = config.getInitParameter("servletPathAttribute");
        m_relativeServletPathAttribute = config.getInitParameter("relativeServletPathAttribute");
    }

    /** {@inheritDoc} */
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
    public void destroy() {
        // Nothing to destroy that a GC won't take care of. :-)
    }

}
