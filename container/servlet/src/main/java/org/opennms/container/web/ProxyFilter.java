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

package org.opennms.container.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.container.web.pattern.PatternMatcher;
import org.opennms.container.web.pattern.PatternMatcherFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The Apache Felix Http Bridge requires a Http Proxy on the SErvlet Container (Jetty) Side in order to work properly.
 * The default implementation uses the {@link org.apache.felix.http.proxy.ProxyServlet}.
 * However, this only forwards requests to a certain context, e.g. /osgi.
 * This breaks with a lot of components, e.g. Vaadin is required to be exposed to /VAADIN
 * In order to compensate, a Filter is used instead, which forwards to the HttpServlet of the Apache Felix Http Bridge.
 * To only forward/dispatch requests which can actually be handled by OSGi-registered Servlets, all registered Servlet's are persisted here.
 *
 * @author mvrueden
 */
public class ProxyFilter implements Filter {
    private BundleContext bundleContext;
    private Map<ServiceReference<Servlet>, ServletInfo> servletInfoMap = new HashMap<>();
    private ServiceTracker<Servlet, Servlet> serviceTracker;
    private DispatcherTracker dispatcherTracker;
    private RestRequestDetector restRequestDetector = new RestRequestDetector();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        bundleContext = getBundleContext(filterConfig.getServletContext());
        dispatcherTracker = createDispatcherTracker(filterConfig);
        serviceTracker = new ServiceTracker<Servlet, Servlet>(bundleContext, Servlet.class, null) {
            @Override
            public Servlet addingService(ServiceReference reference) {
                final Servlet servlet = super.addingService(reference);
                final ServletInfo servletInfo = new ServletInfo(reference);

                if (servletInfo.hasAlias()) {
                    filterConfig.getServletContext().log("alias is no longer supported. Please use osgi.http.whiteboard.servlet.pattern instead");
                }
                if (!servletInfo.isValid()) {
                    filterConfig.getServletContext().log("Servlet is not valid. Probably no url pattern defined");
                } else {
                    servletInfoMap.put(reference, servletInfo);
                }
                return servlet;
            }

            @Override
            public void removedService(ServiceReference<Servlet> reference, Servlet service) {
                super.removedService(reference, service);
                servletInfoMap.remove(reference);
            }
        };
        serviceTracker.open();
        dispatcherTracker.open();
    }

    private DispatcherTracker createDispatcherTracker(FilterConfig filterConfig) {
        // Convert FilterConfig to ServletConfig
        final ServletConfig servletConfig = new ServletConfig() {

            @Override
            public String getServletName() {
                return "opennms-http-osgi-bridge";
            }

            @Override
            public ServletContext getServletContext() {
                return filterConfig.getServletContext();
            }

            @Override
            public String getInitParameter(String name) {
                return filterConfig.getInitParameter(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return filterConfig.getInitParameterNames();
            }
        };

        try {
            return new DispatcherTracker(bundleContext, servletConfig);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // We try to see if any OSGi servlet's are able to handle the request
        // If so, we forward the request accordingly, otherwise we don't
        if (dispatcherTracker.getDispatcher() != null
                && request instanceof HttpServletRequest
                && response instanceof HttpServletResponse
                && canHandle((HttpServletRequest) request)) {
            dispatcherTracker.getDispatcher().service(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean canHandle(HttpServletRequest request) {
        String path = request.getServletPath();
        if (request.getPathInfo() != null) {
            path += request.getPathInfo();
        }
        final String finalPath = path;

        // TODO MVR for now hardcoded
        if (finalPath.startsWith("/rest/classifications") || finalPath.startsWith("/rest/flows") || finalPath.startsWith("/rest/datachoices")) {
            return true;
        }

        final Optional<ServletInfo> info = servletInfoMap.values().stream().filter(servletInfo -> servletInfo.canHandle(finalPath)).findAny();
        return info.isPresent();
    }

    @Override
    public void destroy() {
        serviceTracker.close();
        servletInfoMap.clear();
    }

    private static BundleContext getBundleContext(final ServletContext servletContext) throws ServletException {
        final Object context = servletContext.getAttribute(BundleContext.class.getName());
        if (context instanceof BundleContext) {
            return (BundleContext)context;
        }
        throw new ServletException("Bundle context attribute [" + BundleContext.class.getName() + "] not set in servlet context");
    }

    // Info object for servlets
    private static class ServletInfo {
        private String alias;
        private String name;
        private final List<String> patterns;
        private final List<PatternMatcher> patternMatchers;

        public ServletInfo(ServiceReference reference) {
            this.name = getStringProperty(reference, "osgi.http.whiteboard.servlet.name");
            this.patterns = getListProperty(reference, "osgi.http.whiteboard.servlet.pattern");
            this.alias = getStringProperty(reference, "alias");
            this.patternMatchers = determinePatternMatcher(this.patterns);
        }

        private static List<PatternMatcher> determinePatternMatcher(List<String> patterns) {
            return patterns.stream().map(pattern -> createPatternMatcher(pattern)).collect(Collectors.toList());
        }

        private static PatternMatcher createPatternMatcher(String pattern) {
            return PatternMatcherFactory.createPatternMatcher(pattern);
        }

        public boolean canHandle(String path) {
            final Optional<PatternMatcher> any = patternMatchers.stream().filter(pm -> pm.matches(path)).findAny();
            return any.isPresent();
        }

        private List<String> getListProperty(ServiceReference reference, String key) {
            final List<String> returnList = new ArrayList<>();
            final Object property = reference.getProperty(key);
            if (property instanceof String) {
                final String value = ((String) property).trim();
                if (value != null && !"".equals(property)) {
                    returnList.add(value);
                }
            }
            return returnList;
        }

        private static String getStringProperty(ServiceReference reference, String key) {
            final Object property = reference.getProperty(key);
            if (property instanceof String) {
                return ((String) property).trim();
            }
            return null;
        }

        public boolean isValid() {
            return !patterns.isEmpty() && !patternMatchers.isEmpty();
        }

        public boolean hasAlias() {
            return alias != null;
        }
    }
}