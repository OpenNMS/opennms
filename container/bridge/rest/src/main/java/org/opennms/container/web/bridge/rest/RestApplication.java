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
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.Path;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.osgi.service.http.HttpService;

/**
 * Serves the JAX-RS resources that share one application path.
 * The application has one servlet, which the {@link HttpService} maps to the application path.
 */
final class RestApplication {

    private final String path;
    private final HttpService httpService;
    private final List<Object> resources = new ArrayList<>();

    private Bus bus;
    private Server server;

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

    List<String> getEndpoints() {
        final String prefix = path.endsWith("/") ? path : path + "/";
        final List<String> endpoints = new ArrayList<>();
        for (Object resource : resources) {
            final Path annotation = OsgiRestPublisher.findAnnotation(resource.getClass(), Path.class);
            final String value = annotation == null ? "" : annotation.value().trim();
            if (!value.isEmpty()) {
                endpoints.add(prefix + (value.startsWith("/") ? value.substring(1) : value));
            }
        }
        return endpoints;
    }

    /** Starts a server for the current resources. Stops the application when it has no resources. */
    void publish(final List<Object> providers) throws Exception {
        CxfClassLoader.call(() -> {
            stopServer();
            if (resources.isEmpty()) {
                unregisterServlet();
                return null;
            }
            registerServlet();
            final JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
            factory.setBus(bus);
            factory.setAddress("/");
            factory.setServiceBeans(new ArrayList<>(resources));
            factory.setProviders(providers);
            server = factory.create();
            return null;
        });
    }

    void close() {
        CxfClassLoader.run(() -> {
            stopServer();
            unregisterServlet();
        });
    }

    private void registerServlet() throws Exception {
        if (bus != null) {
            return;
        }
        final ExtensionManagerBus newBus = new ExtensionManagerBus(null, null, Bus.class.getClassLoader());
        newBus.initialize();
        final CXFNonSpringServlet servlet = new CXFNonSpringServlet();
        servlet.setBus(newBus);
        final Hashtable<String, String> initParameters = new Hashtable<>();
        initParameters.put("hide-service-list-page", "true");
        httpService.registerServlet(path, servlet, initParameters, null);
        bus = newBus;
    }

    private void unregisterServlet() {
        if (bus == null) {
            return;
        }
        httpService.unregister(path);
        bus.shutdown(true);
        bus = null;
    }

    private void stopServer() {
        if (server != null) {
            server.destroy();
            server = null;
        }
    }
}
