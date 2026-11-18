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
package org.opennms.container.web.bridge.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves the JAX-RS resources that share one application path.
 * <p>
 * The {@link HttpService} maps one dispatcher servlet to the application path.
 * The dispatcher sends each request to the current deployment.
 * A deployment is a CXF server with a fixed set of resources and providers.
 * A new deployment replaces the current deployment only after it starts.
 */
final class RestApplication {

    private static final Logger LOG = LoggerFactory.getLogger(RestApplication.class);

    private final String path;
    private final HttpService httpService;
    private final List<Object> resources = new ArrayList<>();
    private final Dispatcher dispatcher = new Dispatcher();

    private boolean registered;
    private Deployment deployment;

    RestApplication(final String path, final HttpService httpService) {
        this.path = Objects.requireNonNull(path);
        this.httpService = Objects.requireNonNull(httpService);
    }

    String getPath() {
        return path;
    }

    void addResource(final Object resource) {
        resources.add(resource);
    }

    void removeResource(final Object resource) {
        resources.remove(resource);
    }

    boolean isEmpty() {
        return resources.isEmpty();
    }

    /** Returns the endpoints of the resources that the current deployment serves. */
    List<String> getEndpoints() {
        final String prefix = path.endsWith("/") ? path : path + "/";
        final List<String> endpoints = new ArrayList<>();
        final List<Object> served = deployment == null ? List.of() : deployment.resources;
        for (Object resource : served) {
            final Path annotation = OsgiRestPublisher.findAnnotation(resource.getClass(), Path.class);
            final String value = annotation == null ? "" : annotation.value().trim();
            if (!value.isEmpty()) {
                endpoints.add(prefix + (value.startsWith("/") ? value.substring(1) : value));
            }
        }
        return endpoints;
    }

    /**
     * Starts a deployment for the current resources and then stops the previous deployment.
     * Stops the application when it has no resources.
     * <p>
     * When the deployment does not start, the application rejects each resource that prevents the start.
     */
    void publish(final List<Object> providers) throws Exception {
        CxfClassLoader.call(() -> {
            if (resources.isEmpty()) {
                closeInternal();
                return null;
            }
            registerDispatcher();
            Deployment next;
            try {
                next = new Deployment(resources, providers);
            } catch (Exception e) {
                LOG.warn("The ReST application with path {} did not start. The application now tests each new resource.", path, e);
                next = deployWithoutBadResources(providers);
            }
            if (next != null) {
                replaceDeployment(next);
            } else if (resources.isEmpty()) {
                closeInternal();
            }
            return null;
        });
    }

    void close() {
        CxfClassLoader.run(this::closeInternal);
    }

    /**
     * Adds the resources one at a time to the resources that the current deployment serves.
     * Removes each resource that prevents the start of a deployment.
     *
     * @return the deployment with all accepted resources, or null when no deployment starts
     */
    private Deployment deployWithoutBadResources(final List<Object> providers) {
        final List<Object> accepted = new ArrayList<>();
        final List<Object> candidates = new ArrayList<>();
        for (Object resource : resources) {
            if (deployment != null && deployment.resources.contains(resource)) {
                accepted.add(resource);
            } else {
                candidates.add(resource);
            }
        }
        Deployment next = null;
        if (!accepted.isEmpty()) {
            try {
                next = new Deployment(accepted, providers);
            } catch (Exception e) {
                LOG.error("The ReST application with path {} did not start. The previous resources stay active.", path, e);
                return null;
            }
        }
        for (Object candidate : candidates) {
            final List<Object> attempt = new ArrayList<>(accepted);
            attempt.add(candidate);
            try {
                final Deployment started = new Deployment(attempt, providers);
                if (next != null) {
                    next.stop();
                }
                next = started;
                accepted.add(candidate);
            } catch (Exception e) {
                resources.remove(candidate);
                LOG.error("The application path {} rejected the ReST resource {}.", path, candidate.getClass().getName(), e);
            }
        }
        return next;
    }

    private void replaceDeployment(final Deployment next) {
        final Deployment previous = deployment;
        deployment = next;
        dispatcher.delegate = next.servlet;
        if (previous != null) {
            previous.stop();
        }
    }

    private void registerDispatcher() throws Exception {
        if (registered) {
            return;
        }
        final Hashtable<String, String> initParameters = new Hashtable<>();
        initParameters.put("hide-service-list-page", "true");
        httpService.registerServlet(path, dispatcher, initParameters, null);
        registered = true;
    }

    private void closeInternal() {
        if (registered) {
            httpService.unregister(path);
            registered = false;
        }
        dispatcher.delegate = null;
        if (deployment != null) {
            deployment.stop();
            deployment = null;
        }
    }

    /** Sends each request to the servlet of the current deployment. */
    private static final class Dispatcher extends HttpServlet {
        private static final long serialVersionUID = 1L;

        private transient volatile HttpServlet delegate;

        @Override
        protected void service(final HttpServletRequest request, final HttpServletResponse response)
                throws ServletException, IOException {
            final HttpServlet current = delegate;
            if (current == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                current.service(request, response);
            }
        }
    }

    /** A CXF server and its servlet. Each deployment has its own bus. */
    private final class Deployment {
        private final List<Object> resources;
        private final ExtensionManagerBus bus;
        private final CXFNonSpringServlet servlet;
        private final Server server;

        private Deployment(final List<Object> resources, final List<Object> providers) throws Exception {
            this.resources = List.copyOf(resources);
            bus = new ExtensionManagerBus(null, null, Bus.class.getClassLoader());
            try {
                bus.initialize();
                servlet = new CXFNonSpringServlet();
                servlet.setBus(bus);
                servlet.init(dispatcher.getServletConfig());
                final JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
                factory.setBus(bus);
                factory.setAddress("/");
                factory.setServiceBeans(new ArrayList<>(this.resources));
                factory.setProviders(providers);
                server = factory.create();
            } catch (Exception | LinkageError e) {
                bus.shutdown(true);
                throw e instanceof Exception ? (Exception) e : new IllegalStateException(e);
            }
        }

        private void stop() {
            server.destroy();
            servlet.destroy();
            bus.shutdown(true);
        }
    }
}
