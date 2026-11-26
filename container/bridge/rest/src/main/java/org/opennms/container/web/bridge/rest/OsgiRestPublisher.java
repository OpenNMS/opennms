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
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
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
 * <p>
 * The publisher registers the applications only with the HTTP service of the HTTP bridge.
 * The web application forwards requests to that HTTP service and to no other.
 */
@Component(name = "restEndpointRegistry", immediate = true, service = RestEndpointRegistry.class)
public class OsgiRestPublisher implements RestEndpointRegistry, ServiceTrackerCustomizer<Object, OsgiRestPublisher.Published> {

    static final String APPLICATION_PATH = "application-path";
    static final String DEFAULT_APPLICATION_PATH = "/services";

    private static final Logger LOG = LoggerFactory.getLogger(OsgiRestPublisher.class);
    private static final String BRIDGE_BUNDLE = "org.apache.felix.http.bridge";
    private static final Filter ALL_SERVICES_FILTER = allServices();

    private final Object lock = new Object();
    private final Map<String, RestApplication> applications = new HashMap<>();
    private final List<Object> providers = new ArrayList<>();
    private final Object jsonProvider = new JacksonJaxbJsonProvider();

    private BundleContext bundleContext;
    private ServiceTracker<HttpService, HttpService> httpServiceTracker;
    private HttpService httpService;
    private ServiceTracker<Object, Published> tracker;
    private boolean publishSuspended;
    // Request threads read this list without the lock.
    private volatile List<String> endpoints = List.of();

    @Activate
    public void activate(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        // Bundles call the JAX-RS API from threads that cannot find the implementation.
        // The API keeps the first implementation it finds.
        CxfClassLoader.run(RuntimeDelegate::getInstance);
        httpServiceTracker = new ServiceTracker<>(bundleContext, HttpService.class, new BridgeHttpServiceCustomizer());
        httpServiceTracker.open();
    }

    @Deactivate
    public void deactivate() {
        httpServiceTracker.close();
    }

    private void start(final HttpService bridgeHttpService) {
        synchronized (lock) {
            httpService = bridgeHttpService;
            publishSuspended = true;
        }
        tracker = new ServiceTracker<>(bundleContext, ALL_SERVICES_FILTER, this);
        try {
            tracker.open();
        } finally {
            // One publish operation serves all services that the tracker found.
            synchronized (lock) {
                publishSuspended = false;
                republishAll();
                updateEndpoints();
            }
        }
    }

    private void stop() {
        synchronized (lock) {
            publishSuspended = true;
        }
        tracker.close();
        synchronized (lock) {
            applications.values().forEach(RestApplication::close);
            applications.clear();
            providers.clear();
            httpService = null;
            publishSuspended = false;
            updateEndpoints();
        }
    }

    @Override
    public List<String> getRestEndpoints() {
        return endpoints;
    }

    private void updateEndpoints() {
        final List<String> current = new ArrayList<>();
        for (RestApplication application : applications.values()) {
            current.addAll(application.getEndpoints());
        }
        endpoints = Collections.unmodifiableList(current);
    }

    @Override
    public Published addingService(final ServiceReference<Object> reference) {
        if (bundleContext.getBundle().equals(reference.getBundle())) {
            return null;
        }
        try {
            return publish(reference);
        } catch (Exception | LinkageError e) {
            // An error from one service must not stop the publisher.
            LOG.warn("The publisher ignores a service of bundle {} because of an error.",
                    reference.getBundle().getSymbolicName(), e);
            return null;
        }
    }

    private Published publish(final ServiceReference<Object> reference) {
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
                if (application.isEmpty()) {
                    applications.remove(published.applicationPath);
                }
            }
            updateEndpoints();
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
                    if (application.isEmpty()) {
                        application.close();
                        applications.remove(published.applicationPath);
                    } else {
                        republish(application);
                    }
                }
            }
            updateEndpoints();
        }
    }

    private void republishAll() {
        new ArrayList<>(applications.values()).forEach(this::republish);
    }

    private void republish(final RestApplication application) {
        if (publishSuspended) {
            return;
        }
        final List<Object> allProviders = new ArrayList<>();
        allProviders.add(jsonProvider);
        allProviders.add(new ClientErrorMapper());
        allProviders.addAll(providers);
        try {
            application.publish(allProviders);
        } catch (Exception e) {
            LOG.error("The ReST application with path {} did not start.", application.getPath(), e);
        }
    }

    private static Filter allServices() {
        try {
            return FrameworkUtil.createFilter("(objectClass=*)");
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Accepts only the HTTP service of the HTTP bridge. */
    private final class BridgeHttpServiceCustomizer implements ServiceTrackerCustomizer<HttpService, HttpService> {

        @Override
        public HttpService addingService(final ServiceReference<HttpService> reference) {
            if (!BRIDGE_BUNDLE.equals(reference.getBundle().getSymbolicName())) {
                LOG.debug("The publisher ignores the HTTP service of bundle {}.", reference.getBundle().getSymbolicName());
                return null;
            }
            final HttpService bridgeHttpService = bundleContext.getService(reference);
            if (bridgeHttpService != null) {
                start(bridgeHttpService);
            }
            return bridgeHttpService;
        }

        @Override
        public void modifiedService(final ServiceReference<HttpService> reference, final HttpService service) {
            // The service properties have no effect on the publisher.
        }

        @Override
        public void removedService(final ServiceReference<HttpService> reference, final HttpService service) {
            stop();
            bundleContext.ungetService(reference);
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
        final Class<? extends Annotation> foreign = findForeignAnnotation(service.getClass());
        if (foreign != null) {
            LOG.warn("The publisher ignores the service {} of bundle {}. A different class loader loaded its {} annotation: {}",
                    service.getClass().getName(), reference.getBundle().getSymbolicName(),
                    foreign.getName(), foreign.getClassLoader());
        }
        return null;
    }

    /**
     * Finds a {@link Path} or {@link Provider} annotation that a different class loader loaded.
     * The publisher cannot use a service with such an annotation.
     */
    static Class<? extends Annotation> findForeignAnnotation(final Class<?> type) {
        if (type == null) {
            return null;
        }
        for (Annotation annotation : type.getAnnotations()) {
            final Class<? extends Annotation> annotationType = annotation.annotationType();
            final String name = annotationType.getName();
            if ((name.equals(Path.class.getName()) && annotationType != Path.class)
                    || (name.equals(Provider.class.getName()) && annotationType != Provider.class)) {
                return annotationType;
            }
        }
        Class<? extends Annotation> found = findForeignAnnotation(type.getSuperclass());
        for (int i = 0; found == null && i < type.getInterfaces().length; i++) {
            found = findForeignAnnotation(type.getInterfaces()[i]);
        }
        return found;
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
