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

    @Override
    public void removedService(ServiceReference<Servlet> reference, Servlet service) {
        super.removedService(reference, service);
        final RequestHandler removedRequestHandler = requestHandlerMap.remove(reference);
        proxyFilter.removeRequestHandler(removedRequestHandler);
    }
}
