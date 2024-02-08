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
     * However, the Servlet 2.3 spec (proposed final draft) states (albeit in
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
        if (seconds != null) {
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
