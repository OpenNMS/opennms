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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.container.web.bridge.proxy.handlers.RequestHandler;
import org.opennms.container.web.bridge.proxy.handlers.RequestHandlerRegistry;
import org.opennms.container.web.bridge.proxy.handlers.RestRequestHandler;
import org.opennms.container.web.bridge.proxy.trackers.ResourceTracker;
import org.opennms.container.web.bridge.proxy.trackers.ServletTracker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
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
public class ProxyFilter implements Filter, RequestHandlerRegistry {
    private BundleContext bundleContext;
    private DispatcherTracker dispatcherTracker;
    /**
     * Used to synchronize access to the list of request handlers.
     * We expect a large number of reads with infrequent writes, so we use
     * a ReadWriteLock as opposed to just using synchronized.
     */
    private final ReadWriteLock handlerRwLock = new ReentrantReadWriteLock();
    private final List<RequestHandler> handlers = new ArrayList<>();
    private ServiceTracker<Servlet, Servlet> servletTracker;
    private ServiceTracker resourceTracker;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        bundleContext = getBundleContext(filterConfig.getServletContext());
        try {
            dispatcherTracker = createDispatcherTracker(filterConfig);
            servletTracker = new ServletTracker(bundleContext, filterConfig.getServletContext(), this);
            resourceTracker = new ResourceTracker(bundleContext, filterConfig.getServletContext(), this);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
        servletTracker.open();
        resourceTracker.open();
        dispatcherTracker.open();

        // By default we register a handler for all rest endpoints, as they are already
        // known by the ApplicationRegistry (jax-rs-connector project)
        addRequestHandler(new RestRequestHandler(bundleContext));
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
        final String requestedPath = path;
        final Optional<RequestHandler> handler;
        handlerRwLock.readLock().lock();
        try {
            handler = handlers.stream().filter(eachHandler -> eachHandler.canHandle(requestedPath)).findAny();
        } finally {
            handlerRwLock.readLock().unlock();
        }
        return handler.isPresent();
    }

    @Override
    public void destroy() {
        servletTracker.close();
        resourceTracker.close();
        handlerRwLock.writeLock().lock();
        try {
            handlers.clear();
        } finally {
            handlerRwLock.writeLock().unlock();
        }
    }

    private DispatcherTracker createDispatcherTracker(FilterConfig filterConfig) {
        try {
            return new DispatcherTracker(bundleContext, filterConfig);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addRequestHandler(RequestHandler requestHandler) {
        handlerRwLock.writeLock().lock();
        try {
            for(RequestHandler eachHandler : handlers) {
                for (String eachPattern : requestHandler.getPatterns()) {
                    if (eachHandler.getPatterns().contains(eachPattern)) {
                        throw new IllegalArgumentException("Cannot add request handler as another handler already handles these requestes");
                    }
                }
            }
            handlers.add(requestHandler);
        } finally {
            handlerRwLock.writeLock().unlock();
        }
    }

    @Override
    public void removeRequestHandler(RequestHandler requestHandler) {
        handlerRwLock.writeLock().lock();
        try {
            handlers.remove(requestHandler);
        } finally {
            handlerRwLock.writeLock().unlock();
        }
    }

    private static BundleContext getBundleContext(final ServletContext servletContext) throws ServletException {
        final Object context = servletContext.getAttribute(BundleContext.class.getName());
        if (context instanceof BundleContext) {
            return (BundleContext)context;
        }
        throw new ServletException("Bundle context attribute [" + BundleContext.class.getName() + "] not set in servlet context");
    }
}