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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One JAX-RS application, i.e. one base URI such as {@code /rest} together with the resources and
 * providers published under it.
 *
 * The JAX-RS runtime used here is the Apache CXF of the OpenNMS classpath, which the container
 * exports from the system bundle. That is deliberate: it is the very same CXF - and therefore the
 * same class space - that serves the ReST API of the web application, so no second JAX-RS runtime
 * is introduced into the container.
 */
class JaxRsApplication {

    private static final Logger LOG = LoggerFactory.getLogger(JaxRsApplication.class);

    private final BundleContext bundleContext;
    private final String name;
    private final String base;

    /** Insertion ordered so that the published endpoint list is stable across rebuilds. */
    private final Set<Object> resources = new LinkedHashSet<>();
    private final Set<Object> providers = new LinkedHashSet<>();

    private ServiceRegistration<Servlet> servletRegistration;
    private Server server;

    JaxRsApplication(final BundleContext bundleContext, final String name, final String base) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
        this.name = Objects.requireNonNull(name);
        this.base = JaxRsPaths.normalize(base);
    }

    String getName() {
        return name;
    }

    String getBase() {
        return base;
    }

    /**
     * Replaces the content of this application.
     *
     * @return whether anything actually changed, so that the caller can avoid tearing down and
     *         rebuilding an endpoint that would come back identical - during startup the trackers
     *         see many services that belong to other applications.
     */
    boolean setContents(final Set<Object> newResources, final Set<Object> newProviders) {
        if (resources.equals(newResources) && providers.equals(newProviders)) {
            return false;
        }
        resources.clear();
        resources.addAll(newResources);
        providers.clear();
        providers.addAll(newProviders);
        return true;
    }

    boolean isEmpty() {
        return resources.isEmpty();
    }

    /**
     * The endpoints of this application, e.g. {@code /rest/scv}. Resources without a {@code @Path}
     * of their own are skipped, see {@link JaxRsPaths#endpoint(String, Class)}.
     */
    List<String> getEndpoints() {
        final Set<String> endpoints = new LinkedHashSet<>();
        for (Object resource : resources) {
            final String endpoint = JaxRsPaths.endpoint(base, resource.getClass());
            if (endpoint.isEmpty()) {
                LOG.warn("ReST resource {} has no @Path of its own and is therefore not reachable "
                        + "through the web bridge; it would have claimed all of {}.",
                        resource.getClass().getName(), base.isEmpty() ? "/" : base);
                continue;
            }
            endpoints.add(endpoint);
        }
        return new ArrayList<>(endpoints);
    }

    /**
     * (Re)creates the CXF endpoint and publishes it as a servlet through the OSGi Http Whiteboard.
     * Called whenever the set of resources or providers changed.
     */
    void republish() {
        unpublish();
        if (resources.isEmpty()) {
            LOG.debug("JAX-RS application '{}' has no resources, not publishing {}.", name, base);
            return;
        }
        final List<Object> serviceBeans = new ArrayList<>(resources);
        final List<Object> serviceProviders = new ArrayList<>(providers);

        final CXFNonSpringServlet servlet = new CXFNonSpringServlet() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void loadBus(final ServletConfig servletConfig) {
                super.loadBus(servletConfig);
                final JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
                factory.setBus(getBus());
                // The base URI is contributed by the servlet pattern, so the endpoint is at the root
                // of this servlet.
                factory.setAddress("/");
                factory.setServiceBeans(serviceBeans);
                factory.setProviders(serviceProviders);
                server = factory.create();
            }
        };

        final Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("osgi.http.whiteboard.servlet.pattern", base + "/*");
        properties.put("osgi.http.whiteboard.servlet.name", "opennms-jaxrs-" + name);
        properties.put("osgi.http.whiteboard.servlet.asyncSupported", Boolean.TRUE);

        servletRegistration = bundleContext.registerService(Servlet.class, servlet, properties);
        LOG.info("Published JAX-RS application '{}' at {} with {} resource(s) and {} provider(s).",
                name, base, serviceBeans.size(), serviceProviders.size());
    }

    void unpublish() {
        if (servletRegistration != null) {
            try {
                servletRegistration.unregister();
            } catch (IllegalStateException e) {
                // already unregistered, e.g. because the bundle is stopping
            }
            servletRegistration = null;
        }
        if (server != null) {
            server.destroy();
            server = null;
        }
    }
}
