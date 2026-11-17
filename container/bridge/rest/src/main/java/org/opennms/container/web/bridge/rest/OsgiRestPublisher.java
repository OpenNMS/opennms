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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.Feature;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.RuntimeDelegate;

import org.opennms.container.web.bridge.api.RestEndpointRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * Publishes OSGi services as JAX-RS resources.
 * <p>
 * A service is a resource when its class, a superclass or an interface has the {@link Path} annotation.
 * The <code>application-path</code> service property selects the application that serves the resource.
 * A service is a provider when it has the {@link Provider} annotation or implements {@link Feature}.
 * Each application uses all providers.
 */
@Component(name = "restEndpointRegistry", immediate = true)
public class OsgiRestPublisher implements RestEndpointRegistry, ServiceTrackerCustomizer<Object, OsgiRestPublisher.Published> {

    static final String APPLICATION_PATH = "application-path";
    static final String DEFAULT_APPLICATION_PATH = "/services";

    private static final Logger LOG = LoggerFactory.getLogger(OsgiRestPublisher.class);
    private static final String ALL_SERVICES = "(objectClass=*)";

    private final Object lock = new Object();
    private final Map<String, RestApplication> applications = new HashMap<>();
    private final List<Object> providers = new ArrayList<>();
    private final Object jsonProvider = new JacksonJaxbJsonProvider();

    private HttpService httpService;
    private BundleContext bundleContext;
    private ServiceTracker<Object, Published> tracker;

    @Reference
    public void setHttpService(final HttpService httpService) {
        this.httpService = httpService;
    }

    @Activate
    public void activate(final BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;
        // Bundles call the JAX-RS API from threads that cannot find the implementation.
        // The API keeps the first implementation it finds.
        CxfClassLoader.run(RuntimeDelegate::getInstance);
        tracker = new ServiceTracker<>(bundleContext, bundleContext.createFilter(ALL_SERVICES), this);
        tracker.open();
    }

    @Deactivate
    public void deactivate() {
        tracker.close();
        synchronized (lock) {
            applications.values().forEach(RestApplication::close);
            applications.clear();
            providers.clear();
        }
    }

    @Override
    public List<String> getRestEndpoints() {
        final List<String> endpoints = new ArrayList<>();
        synchronized (lock) {
            for (RestApplication application : applications.values()) {
                endpoints.addAll(application.getEndpoints());
            }
        }
        return Collections.unmodifiableList(endpoints);
    }

    @Override
    public Published addingService(final ServiceReference<Object> reference) {
        if (bundleContext.getBundle().equals(reference.getBundle())) {
            return null;
        }
        final Object service = bundleContext.getService(reference);
        final Published published = toPublished(reference, service);
        if (published == null) {
            if (service != null) {
                bundleContext.ungetService(reference);
            }
            return null;
        }
        add(published);
        return published;
    }

    @Override
    public void modifiedService(final ServiceReference<Object> reference, final Published published) {
        final String applicationPath = getApplicationPath(reference);
        if (published.applicationPath != null && !published.applicationPath.equals(applicationPath)) {
            remove(published);
            published.applicationPath = applicationPath;
            add(published);
        }
    }

    @Override
    public void removedService(final ServiceReference<Object> reference, final Published published) {
        remove(published);
        bundleContext.ungetService(reference);
    }

    private void add(final Published published) {
        synchronized (lock) {
            if (published.applicationPath == null) {
                providers.add(published.service);
                republishAll();
            } else {
                final RestApplication application = applications.computeIfAbsent(published.applicationPath,
                        path -> new RestApplication(path, httpService));
                application.addResource(published.service);
                republish(application);
            }
        }
    }

    private void remove(final Published published) {
        synchronized (lock) {
            if (published.applicationPath == null) {
                providers.remove(published.service);
                republishAll();
            } else {
                final RestApplication application = applications.get(published.applicationPath);
                if (application != null) {
                    application.removeResource(published.service);
                    republish(application);
                    if (application.isEmpty()) {
                        applications.remove(published.applicationPath);
                    }
                }
            }
        }
    }

    private void republishAll() {
        applications.values().forEach(this::republish);
    }

    private void republish(final RestApplication application) {
        final List<Object> allProviders = new ArrayList<>();
        allProviders.add(jsonProvider);
        allProviders.add(new ClientErrorMapper());
        allProviders.addAll(providers);
        try {
            application.publish(allProviders);
        } catch (Exception e) {
            LOG.error("Failed to publish the ReST application with path {}", application.getPath(), e);
        }
    }

    private static Published toPublished(final ServiceReference<Object> reference, final Object service) {
        if (service == null) {
            return null;
        }
        if (hasAnnotation(service.getClass(), Provider.class) || service instanceof Feature) {
            return new Published(service, null);
        }
        if (hasAnnotation(service.getClass(), Path.class)) {
            return new Published(service, getApplicationPath(reference));
        }
        return null;
    }

    private static String getApplicationPath(final ServiceReference<Object> reference) {
        final Object value = reference.getProperty(APPLICATION_PATH);
        final String path = value == null ? DEFAULT_APPLICATION_PATH : value.toString();
        return path.startsWith("/") ? path : "/" + path;
    }

    static boolean hasAnnotation(final Class<?> type, final Class<? extends Annotation> annotation) {
        return findAnnotation(type, annotation) != null;
    }

    static <A extends Annotation> A findAnnotation(final Class<?> type, final Class<A> annotation) {
        if (type == null) {
            return null;
        }
        A found = type.getAnnotation(annotation);
        if (found == null) {
            found = findAnnotation(type.getSuperclass(), annotation);
        }
        for (int i = 0; found == null && i < type.getInterfaces().length; i++) {
            found = findAnnotation(type.getInterfaces()[i], annotation);
        }
        return found;
    }

    /** A service that this publisher uses. A provider has no application path. */
    static final class Published {
        private final Object service;
        private String applicationPath;

        private Published(final Object service, final String applicationPath) {
            this.service = service;
            this.applicationPath = applicationPath;
        }
    }
}
