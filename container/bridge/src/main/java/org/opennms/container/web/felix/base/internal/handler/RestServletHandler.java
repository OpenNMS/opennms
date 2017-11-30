/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.container.web.felix.base.internal.handler;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;

import org.opennms.container.web.felix.base.internal.context.ExtServletContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.eclipsesource.jaxrs.publisher.api.ApplicationRegistry;

public class RestServletHandler extends ServletHandler {
    private final BundleContext bundleContext;

    public RestServletHandler(ExtServletContext context, Servlet servlet, String alias, BundleContext bundleContext) {
        super(context, servlet, alias);
        this.bundleContext = bundleContext;
    }

    @Override
    public boolean matches(String uri) {
        boolean matches = super.matches(uri);
        if (matches) {
            return !getEndpoints(uri).isEmpty();
        }
        return matches;
    }

    @Override
    protected String getUri(HttpServletRequest req) {
        if (req.getPathInfo() != null && !req.getPathInfo().isEmpty()) {
            return req.getServletPath() + req.getPathInfo();
        }
        return super.getUri(req);
    }

    private List<String> getEndpoints(String uri) {
        final ServiceReference<ApplicationRegistry> serviceReference = bundleContext.getServiceReference(ApplicationRegistry.class);
        try {
            final ApplicationRegistry applicationRegistry = bundleContext.getService(serviceReference);
            return applicationRegistry.getEndpoints().stream()
                    .filter(endpoint -> uri.equals(endpoint) || uri.startsWith(endpoint)).collect(Collectors.toList());
        } finally {
            bundleContext.ungetService(serviceReference);
        }
    }
}
