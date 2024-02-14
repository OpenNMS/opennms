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

import javax.servlet.ServletContext;

import org.opennms.container.web.bridge.proxy.ProxyFilter;
import org.opennms.container.web.bridge.proxy.handlers.RequestHandler;
import org.opennms.container.web.bridge.proxy.handlers.ResourceInfo;
import org.opennms.container.web.bridge.proxy.handlers.ResourceRequestHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class ResourceTracker extends ServiceTracker {
    private final ProxyFilter proxyFilter;
    private final ServletContext servletContext;
    private Map<ServiceReference, RequestHandler> requestHandlerMap = new HashMap<>();

    public ResourceTracker(BundleContext context, ServletContext servletContext, ProxyFilter proxyFilter) throws InvalidSyntaxException {
        super(context, context.createFilter(String.format("(&(%s=*)(%s=*))", "osgi.http.whiteboard.resource.pattern", "osgi.http.whiteboard.resource.prefix")), null);
        this.proxyFilter = Objects.requireNonNull(proxyFilter);
        this.servletContext = Objects.requireNonNull(servletContext);
    }

    @Override
    public Object addingService(ServiceReference reference) {
        final Object resource = super.addingService(reference);
        final ResourceInfo resourceInfo = new ResourceInfo(reference);
        if (!resourceInfo.isValid()) { // if invalid, we bail
            servletContext.log(String.format("Resource is not valid. Property '%s' and '%s' must be defined", "osgi.http.whiteboard.resource.pattern", "osgi.http.whiteboard.resource.prefix"));
            return resource;
        }
        final RequestHandler resourceRequestHandler = new ResourceRequestHandler(resourceInfo);
        requestHandlerMap.put(reference, resourceRequestHandler);
        proxyFilter.addRequestHandler(resourceRequestHandler);
        return resource;
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        super.removedService(reference, service);
        final RequestHandler removedRequestHandler = requestHandlerMap.remove(reference);
        proxyFilter.removeRequestHandler(removedRequestHandler);
    }
}
