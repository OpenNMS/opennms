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
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.resource.Vault;
import org.opennms.web.api.Util;

public class AddStrictTransportSecurityHeaderFilter implements Filter {
    private String maxAge;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.maxAge = filterConfig.getInitParameter("maxAge");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final String urlBase = Util.calculateUrlBase(httpServletRequest);
        boolean httpsEnabledByJetty = Vault.getProperty("org.opennms.netmgt.jetty.https-port") != null;

        if (urlBase.startsWith("https://") && httpsEnabledByJetty) {
            httpServletResponse.setHeader("Strict-Transport-Security", "max-age=" + maxAge + "; includeSubDomains");
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}