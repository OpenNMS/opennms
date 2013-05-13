/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
 * A filter that adds an HTTP <em>Refresh</em> header to a servlet or JSP's
 * response. The amount of time to wait before refresh is configurable.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @since 1.8.1
 */
public class AddRefreshHeaderFilter extends Object implements Filter {
    protected FilterConfig filterConfig;

    protected String seconds = "108000"; // default is 30 mins

    /**
     * {@inheritDoc}
     *
     * Adds a <em>Refresh</em> HTTP header before processing the request.
     *
     * <p>
     * This is a strange implementation, because intuitively, you would add the
     * header after the content has been produced (in other words, after you had
     * already called {@link FilterChain#doFilter FilterChain.doFilter}.
     * However, the Servlet 2.3 spec (proposed final draft) states (albeitly in
     * an off-handed fashion) that you can only "examine" the response headers
     * after the <code>doFilter</code> call. Evidently this means that you
     * cannot change the headers after the <code>doFilter</code>. If you call
     * <code>setHeader</code> nothing happens.
     * </p>
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ((HttpServletResponse) response).setHeader("Refresh", this.seconds);
        chain.doFilter(request, response);
    }

    /** {@inheritDoc} */
    @Override
    public void init(FilterConfig config) {
        this.filterConfig = config;

        // read the seconds value from the config or use the default if not
        // found
        String seconds = this.filterConfig.getInitParameter("seconds");
        if (seconds != null)
            ;
        {
            this.seconds = seconds;
        }
    }

    /**
     * <p>destroy</p>
     */
    @Override
    public void destroy() {
    }

}
