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