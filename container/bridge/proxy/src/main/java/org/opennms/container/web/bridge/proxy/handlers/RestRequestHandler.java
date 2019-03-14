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

package org.opennms.container.web.bridge.proxy.handlers;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opennms.container.web.bridge.api.RestEndpointRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class RestRequestHandler implements RequestHandler {

    private final BundleContext bundleContext;

    public RestRequestHandler(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public boolean canHandle(String requestedPath) {
        final List<String> knownPatterns = getPatterns();
        for (String eachPattern : knownPatterns) {
            if (requestedPath.startsWith(eachPattern)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getPatterns() {
        final ServiceReference<RestEndpointRegistry> serviceReference = bundleContext.getServiceReference(RestEndpointRegistry.class);
        if (serviceReference != null) {
            try {
                final RestEndpointRegistry restEndpointRegistry = bundleContext.getService(serviceReference);
                return restEndpointRegistry.getRestEndpoints();
            } finally {
                bundleContext.ungetService(serviceReference);
            }
        }
        return Collections.emptyList();
    }
}
