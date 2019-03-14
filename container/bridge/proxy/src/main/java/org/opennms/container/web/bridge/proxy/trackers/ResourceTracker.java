/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
