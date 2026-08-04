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
package org.opennms.container.web.bridge.proxy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.Test;
import org.opennms.container.web.bridge.proxy.handlers.RequestHandler;

public class ProxyFilterTest {
    @Test
    public void passesRequestsThroughWhenKarafIsUnavailable() throws Exception {
        final ServletContext servletContext = mock(ServletContext.class);
        final FilterConfig filterConfig = mock(FilterConfig.class);
        final ServletRequest request = mock(ServletRequest.class);
        final ServletResponse response = mock(ServletResponse.class);
        final FilterChain chain = mock(FilterChain.class);
        when(filterConfig.getServletContext()).thenReturn(servletContext);

        final ProxyFilter filter = new ProxyFilter();
        filter.init(filterConfig);
        filter.doFilter(request, response, chain);
        filter.destroy();

        // Without a BundleContext the filter must stay out of the way rather than
        // failing the request or the web application startup.
        verify(chain).doFilter(request, response);
    }

    /**
     * Handlers are added from ServiceTracker callbacks, where an exception aborts the tracker and
     * leaves the proxy half wired up. A pattern that is already handled must therefore be skipped
     * rather than rejected - two JAX-RS applications for instance yield two servlets sharing the
     * very same pattern.
     */
    @Test
    public void skipsRequestHandlersWithAnAlreadyHandledPattern() {
        final ProxyFilter filter = new ProxyFilter();
        filter.addRequestHandler(handlerFor("/opennms/example"));
        filter.addRequestHandler(handlerFor("/opennms/example"));
    }

    private static RequestHandler handlerFor(final String pattern) {
        return new RequestHandler() {
            @Override
            public boolean canHandle(String requestedPath) {
                return requestedPath.startsWith(pattern);
            }

            @Override
            public List<String> getPatterns() {
                return Collections.singletonList(pattern);
            }
        };
    }
}
