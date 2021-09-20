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
