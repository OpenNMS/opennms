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

package org.opennms.container.web.bridge.proxy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

public class RestRequestDetector {

    private final Set<String> aliases;

    public RestRequestDetector() {
        this(System.getProperty("org.opennms.features.osgi.bridge.restAliases", "/rest,/api/v2"));
    }

    public RestRequestDetector(String restAliasString) {
        this.aliases = parseAliases(restAliasString);
    }

    public boolean isRestRequest(HttpServletRequest request) {
        for (String eachAlias : aliases) {
            if (request.getServletPath().startsWith(eachAlias)) {
                return true;
            }
        }
        return false;
    }

//    private String getUri(HttpServletRequest req) {
//        if (req.getPathInfo() != null && !req.getPathInfo().isEmpty()) {
//            return req.getServletPath() + req.getPathInfo();
//        }
//        return req.getServletPath();
//    }

    // TODO MVR implement me properly
//    private List<String> getEndpoints(String uri) {
//        final ServiceReference<ApplicationRegistry> serviceReference = bundleContext.getServiceReference(ApplicationRegistry.class);
//        try {
//            final ApplicationRegistry applicationRegistry = bundleContext.getService(serviceReference);
//            return applicationRegistry.getEndpoints().stream()
//                    .filter(endpoint -> uri.equals(endpoint) || uri.startsWith(endpoint)).collect(Collectors.toList());
//        } finally {
//            bundleContext.ungetService(serviceReference);
//        }
//    }

    private static Set<String> parseAliases(String aliases) {
        if (aliases == null) {
            return new HashSet<>();
        }
        return Arrays.stream(aliases.split(","))
                .filter(alias -> alias != null && !alias.trim().isEmpty())
                .map(alias -> {
                    alias = alias.trim();
                    if (!alias.startsWith("/")) {
                        alias = "/" + alias;
                    }
                    if (!"/".equals(alias) && alias.endsWith("/")) {
                        alias = alias.substring(0, alias.lastIndexOf("/"));
                    }
                    return alias;
                })
                .collect(Collectors.toSet());
    }
}
