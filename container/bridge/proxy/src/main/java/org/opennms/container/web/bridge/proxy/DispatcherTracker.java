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

import java.util.Enumeration;
import java.util.Objects;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * In order to dispatch requests to the Apache Felix Http Bridge, we have to listen for the HttpServlet the bridge module
 * is exposing.
 *
 * @author mvrueden
 */
public final class DispatcherTracker extends ServiceTracker<HttpServlet, HttpServlet> {
    private final ServletConfig config;
    private HttpServlet dispatcher;

    public DispatcherTracker(BundleContext context, FilterConfig filterConfig) throws InvalidSyntaxException {
        this(context, convert(filterConfig));
    }

    private DispatcherTracker(BundleContext context, ServletConfig servletConfig) throws InvalidSyntaxException {
        super(context, context.createFilter("(&(objectClass=javax.servlet.http.HttpServlet)(http.felix.dispatcher=*))"), null);
        this.config = Objects.requireNonNull(servletConfig);
    }

    public HttpServlet getDispatcher() {
        return this.dispatcher;
    }

    @Override
    public HttpServlet addingService(ServiceReference ref) {
        HttpServlet service = super.addingService(ref);
        setDispatcher(service);
        return service;
    }

    @Override
    public void removedService(ServiceReference<HttpServlet> reference, HttpServlet service) {
        setDispatcher(null);
        super.removedService(reference, service);
    }

    private void setDispatcher(HttpServlet dispatcher) {
        destroyDispatcher();
        this.dispatcher = dispatcher;
        initDispatcher();
    }

    private void destroyDispatcher() {
        if (this.dispatcher != null) {
            this.dispatcher.destroy();
        }
    }

    private void initDispatcher() {
        if (this.dispatcher != null) {
            try {
                this.dispatcher.init(config);
            } catch (Exception e) {
                config.getServletContext().log("Failed to initialize dispatcher", e);
            }
        }
    }

    private static ServletConfig convert(FilterConfig filterConfig) {
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
        return servletConfig;
    }
}