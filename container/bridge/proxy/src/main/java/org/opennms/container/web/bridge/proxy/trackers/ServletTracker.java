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
package org.opennms.container.web.bridge.proxy.trackers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.opennms.container.web.bridge.proxy.ProxyFilter;
import org.opennms.container.web.bridge.proxy.handlers.RequestHandler;
import org.opennms.container.web.bridge.proxy.handlers.ServletInfo;
import org.opennms.container.web.bridge.proxy.handlers.ServletRequestHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class ServletTracker extends ServiceTracker<Servlet, Servlet> {

    /**
     * The bundle symbolic name of the OSGi JAX-RS Whiteboard implementation.
     *
     * @see #isJaxRsWhiteboardServlet(ServiceReference)
     */
    private static final String JAX_RS_WHITEBOARD_BSN = "org.apache.aries.jax.rs.whiteboard";

    private final ServletContext servletContext;
    private final ProxyFilter proxyFilter;
    private Map<ServiceReference<Servlet>, RequestHandler> requestHandlerMap = new HashMap<>();

    public ServletTracker(BundleContext context, ServletContext servletContext, ProxyFilter proxyFilter) {
        super(context, Servlet.class, null);
        this.servletContext = Objects.requireNonNull(servletContext);
        this.proxyFilter = Objects.requireNonNull(proxyFilter);
    }

    @Override
    public Servlet addingService(ServiceReference reference) {
        final Servlet servlet = super.addingService(reference);
        if (isJaxRsWhiteboardServlet(reference)) {
            return servlet;
        }
        final ServletInfo servletInfo = new ServletInfo(reference);
        if (servletInfo.hasAlias()) {
            servletContext.log("Property 'alias' is no longer supported. " +
                    "Please use 'osgi.http.whiteboard.servlet.pattern' instead.");
        }
        // If invalid, we bail
        if (!servletInfo.isValid()) {
            servletContext.log("Servlet is not valid. Probably no url pattern defined");
            return servlet;
        }
        final ServletRequestHandler servletRequestHandler = new ServletRequestHandler(servletInfo);
        requestHandlerMap.put(reference, servletRequestHandler);
        proxyFilter.addRequestHandler(servletRequestHandler);
        return servlet;
    }

    /**
     * The JAX-RS Whiteboard registers one servlet per JAX-RS application, and it puts the
     * application base (e.g. {@code /rest}) on a separate ServletContextHelper via
     * {@code osgi.http.whiteboard.context.path} - the servlet itself is registered with the
     * pattern {@code /*}. Taken at face value that pattern would make the proxy forward every
     * single request of the web application into the OSGi container.
     *
     * Prefixing the pattern with the context path would not help either: that yields
     * {@code /rest/*}, but {@code /rest} is shared - most of it is served by the ReST servlet of
     * the web application itself, only individual resources come from OSGi. Which ones is exactly
     * what the {@link org.opennms.container.web.bridge.api.RestEndpointRegistry} knows, and the
     * {@link org.opennms.container.web.bridge.proxy.handlers.RestRequestHandler} already asks it,
     * so these servlets must simply be left alone here.
     */
    private static boolean isJaxRsWhiteboardServlet(ServiceReference<?> reference) {
        return reference.getBundle() != null
                && JAX_RS_WHITEBOARD_BSN.equals(reference.getBundle().getSymbolicName());
    }

    @Override
    public void removedService(ServiceReference<Servlet> reference, Servlet service) {
        super.removedService(reference, service);
        final RequestHandler removedRequestHandler = requestHandlerMap.remove(reference);
        proxyFilter.removeRequestHandler(removedRequestHandler);
    }
}
