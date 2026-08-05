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
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes the ReST endpoints provided by OSGi bundles, replacing the unmaintained
 * osgi-jax-rs-connector.
 *
 * A bundle exports a service whose type carries the JAX-RS annotations and marks it with the
 * service properties of the OSGi JAX-RS Whiteboard specification (OSGi cmpn R7, chapter 151):
 *
 * <ul>
 *   <li>{@code osgi.jaxrs.resource=true} - the service is a ReST resource</li>
 *   <li>{@code osgi.jaxrs.extension=true} - the service is a provider, e.g. an ExceptionMapper</li>
 *   <li>{@code osgi.jaxrs.name} - a name, unique within the application</li>
 *   <li>{@code osgi.jaxrs.application.select} - an LDAP filter selecting the application(s)</li>
 * </ul>
 *
 * The applications themselves are {@code javax.ws.rs.core.Application} services carrying
 * {@code osgi.jaxrs.name} and {@code osgi.jaxrs.application.base}; OpenNMS declares {@code /rest}
 * and {@code /api/v2} in the {@code org.opennms.features.rest-provider} bundle.
 *
 * Note that this is deliberately <em>not</em> an implementation of the whiteboard specification: it
 * only understands the subset of it that OpenNMS uses, and it builds on the Apache CXF of the
 * OpenNMS classpath (exported from the system bundle) instead of requiring CXF to be present as
 * OSGi bundles - which is what makes the specification's reference implementation unusable in this
 * container.
 */
public class JaxRsPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(JaxRsPublisher.class);

    static final String APPLICATION_BASE = "osgi.jaxrs.application.base";
    static final String APPLICATION_SELECT = "osgi.jaxrs.application.select";
    static final String NAME = "osgi.jaxrs.name";
    static final String RESOURCE = "osgi.jaxrs.resource";
    static final String EXTENSION = "osgi.jaxrs.extension";

    /**
     * Services arrive in bursts, in particular while the container starts. Rebuilding an endpoint is
     * not free, so changes are coalesced for a moment before the endpoints are (re)published.
     */
    private static final long PUBLISH_DELAY_MS = 250;

    private final BundleContext bundleContext;
    private final ScheduledExecutorService executor;

    private ServiceTracker<Object, Object> applicationTracker;
    private ServiceTracker<Object, Object> componentTracker;

    /** Guards the applications map, the trackers' views and the scheduled rebuild. */
    private final Object lock = new Object();
    private final Map<String, JaxRsApplication> applications = new LinkedHashMap<>();
    private ScheduledFuture<?> pendingRebuild;

    public JaxRsPublisher(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.executor = Executors.newSingleThreadScheduledExecutor(
                r -> new Thread(r, "opennms-jaxrs-publisher"));
    }

    public void start() throws InvalidSyntaxException {
        applicationTracker = new ServiceTracker<>(bundleContext,
                bundleContext.createFilter("(&(objectClass=javax.ws.rs.core.Application)(" + APPLICATION_BASE + "=*))"),
                new RebuildOnChange());
        componentTracker = new ServiceTracker<>(bundleContext,
                bundleContext.createFilter("(|(" + RESOURCE + "=true)(" + EXTENSION + "=true))"),
                new RebuildOnChange());
        applicationTracker.open();
        componentTracker.open();
        LOG.debug("JAX-RS publisher started.");
    }

    public void stop() {
        executor.shutdownNow();
        if (componentTracker != null) {
            componentTracker.close();
        }
        if (applicationTracker != null) {
            applicationTracker.close();
        }
        synchronized (lock) {
            applications.values().forEach(JaxRsApplication::unpublish);
            applications.clear();
        }
        LOG.debug("JAX-RS publisher stopped.");
    }

    /**
     * The endpoints of every published application, e.g. {@code /rest/scv}, in the granularity the
     * web bridge needs to decide which requests belong to the OSGi container.
     */
    public List<String> getEndpoints() {
        synchronized (lock) {
            final Set<String> endpoints = new LinkedHashSet<>();
            applications.values().forEach(a -> endpoints.addAll(a.getEndpoints()));
            return Collections.unmodifiableList(new ArrayList<>(endpoints));
        }
    }

    private void scheduleRebuild() {
        synchronized (lock) {
            if (pendingRebuild != null) {
                pendingRebuild.cancel(false);
            }
            if (executor.isShutdown()) {
                return;
            }
            pendingRebuild = executor.schedule(this::rebuild, PUBLISH_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void rebuild() {
        synchronized (lock) {
            final Map<String, JaxRsApplication> current = new LinkedHashMap<>();
            for (ServiceReference<Object> reference : safeReferences(applicationTracker)) {
                final String name = stringProperty(reference, NAME);
                final String base = stringProperty(reference, APPLICATION_BASE);
                if (name == null || base == null) {
                    continue;
                }
                // Reuse the existing instance so a rebuild does not tear down an unchanged endpoint.
                final JaxRsApplication application = applications.containsKey(name)
                        ? applications.get(name)
                        : new JaxRsApplication(bundleContext, name, base);
                current.put(name, application);
            }

            // Applications that went away
            for (Map.Entry<String, JaxRsApplication> entry : applications.entrySet()) {
                if (!current.containsKey(entry.getKey())) {
                    entry.getValue().unpublish();
                }
            }
            applications.clear();
            applications.putAll(current);

            for (JaxRsApplication application : applications.values()) {
                final Set<Object> resources = new LinkedHashSet<>();
                final Set<Object> providers = new LinkedHashSet<>();
                for (ServiceReference<Object> reference : safeReferences(componentTracker)) {
                    if (!selects(reference, application)) {
                        continue;
                    }
                    final Object service = componentTracker.getService(reference);
                    if (service == null) {
                        continue;
                    }
                    if (isTrue(reference, RESOURCE)) {
                        resources.add(service);
                    } else if (isTrue(reference, EXTENSION)) {
                        providers.add(service);
                    }
                }
                if (application.setContents(resources, providers)) {
                    application.republish();
                }
            }
        }
    }

    /**
     * Whether a resource or extension selects the given application. A component without a
     * selection filter is not published at all: unlike the whiteboard specification this publisher
     * has no default application, precisely because an application at the root would make the web
     * bridge claim every request of the web application.
     */
    private boolean selects(final ServiceReference<Object> reference, final JaxRsApplication application) {
        final String select = stringProperty(reference, APPLICATION_SELECT);
        if (select == null) {
            LOG.warn("Service {} is marked as a JAX-RS resource or extension but has no '{}' "
                    + "property and is therefore not published.",
                    reference.getProperty("objectClass"), APPLICATION_SELECT);
            return false;
        }
        try {
            final Filter filter = bundleContext.createFilter(select);
            final Hashtable<String, Object> applicationProperties = new Hashtable<>();
            applicationProperties.put(NAME, application.getName());
            applicationProperties.put(APPLICATION_BASE, application.getBase());
            return filter.match(applicationProperties);
        } catch (InvalidSyntaxException e) {
            LOG.warn("Service {} has an invalid '{}' filter '{}' and is not published.",
                    reference.getProperty("objectClass"), APPLICATION_SELECT, select, e);
            return false;
        }
    }

    private static boolean isTrue(final ServiceReference<?> reference, final String property) {
        return Boolean.parseBoolean(String.valueOf(reference.getProperty(property)));
    }

    private static String stringProperty(final ServiceReference<?> reference, final String property) {
        final Object value = reference.getProperty(property);
        return value == null ? null : value.toString();
    }

    private static List<ServiceReference<Object>> safeReferences(final ServiceTracker<Object, Object> tracker) {
        final ServiceReference<Object>[] references = tracker.getServiceReferences();
        return references == null ? Collections.emptyList() : java.util.Arrays.asList(references);
    }

    /** Any change to the tracked services triggers a coalesced rebuild. */
    private final class RebuildOnChange implements org.osgi.util.tracker.ServiceTrackerCustomizer<Object, Object> {
        @Override
        public Object addingService(final ServiceReference<Object> reference) {
            final Object service = bundleContext.getService(reference);
            scheduleRebuild();
            return service;
        }

        @Override
        public void modifiedService(final ServiceReference<Object> reference, final Object service) {
            scheduleRebuild();
        }

        @Override
        public void removedService(final ServiceReference<Object> reference, final Object service) {
            bundleContext.ungetService(reference);
            scheduleRebuild();
        }
    }
}
