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
package org.opennms.netmgt.shared.bootstrap;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.core.sysprops.SystemProperties;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.BundleTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.AntPathMatcher;

/**
 * Tracks active bundles with a {@code Spring-Context} manifest header and starts
 * one Spring application context per bundle, the way the Gemini extender used to.
 *
 * Contexts are created on dedicated threads, so an {@code osgi:reference} in one
 * bundle's context can block until another bundle's context (or an Aries blueprint)
 * publishes the service it needs.
 */
class SpringContextTracker extends BundleTracker<Bundle> {

    private static final Logger LOG = LoggerFactory.getLogger(SpringContextTracker.class);

    static final String BUNDLE_CONTEXT_BEAN_NAME = "bundleContext";

    private static final String SPRING_CONTEXT_HEADER = "Spring-Context";
    private static final String WILDCARD = "*";
    private static final String DEFAULT_CONFIG_DIR = "META-INF/spring";

    // A failed context creation (e.g. the database service not appearing within the osgi:reference wait
    // at boot) is retried with capped exponential backoff rather than abandoned: Gemini kept waiting for
    // unsatisfied mandatory dependencies, and a bundle that sits ACTIVE without its context (and without
    // its services) until someone manually restarts it is a silent outage.
    private static final long RETRY_DELAY_MS = SystemProperties.getLong(
            "org.opennms.spring.extender.contextRetryDelayMillis", TimeUnit.SECONDS.toMillis(30));
    private static final long MAX_RETRY_DELAY_MS = SystemProperties.getLong(
            "org.opennms.spring.extender.contextRetryMaxDelayMillis", TimeUnit.MINUTES.toMillis(5));

    private final NamespaceProviderRegistry namespaceProviderRegistry;
    private final Map<Long, ConfigurableApplicationContext> contexts = new ConcurrentHashMap<>();
    // Per-bundle "generation" token identifying the latest context-creation task for a bundle id. A task only
    // installs its context if it is still the current generation; a stop (removedBundle clears the token) or a
    // restart (addingBundle replaces it) invalidates an in-flight task so it cannot overwrite a newer context.
    private final Map<Long, Object> generations = new ConcurrentHashMap<>();
    // Guards the compound check-then-act on contexts/generations. NOT held across refresh() (which can block on
    // an osgi:reference), only across the short install/close critical sections, so it cannot deadlock a task.
    private final Object lifecycleLock = new Object();
    private final AtomicInteger threadIndex = new AtomicInteger();
    private final ExecutorService executor;
    private final ScheduledExecutorService retryScheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        final Thread thread = new Thread(runnable, "spring-context-extender-retry");
        thread.setDaemon(true);
        return thread;
    });

    SpringContextTracker(BundleContext context, NamespaceProviderRegistry namespaceProviderRegistry) {
        this(context, namespaceProviderRegistry, null);
    }

    /** Test seam: a caller may supply a controllable executor (e.g. run-on-demand) instead of the default pool. */
    SpringContextTracker(BundleContext context, NamespaceProviderRegistry namespaceProviderRegistry, ExecutorService executor) {
        super(context, Bundle.ACTIVE, null);
        this.namespaceProviderRegistry = namespaceProviderRegistry;
        this.executor = executor != null ? executor : Executors.newCachedThreadPool(runnable -> {
            final Thread thread = new Thread(runnable, "spring-context-extender-" + threadIndex.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public Bundle addingBundle(Bundle bundle, BundleEvent event) {
        final List<String> configLocations = getConfigLocations(bundle);
        if (configLocations.isEmpty()) {
            return null;
        }
        LOG.info("Creating Spring application context for bundle {} from {}", bundle.getSymbolicName(), configLocations);
        final Object token = new Object();
        generations.put(bundle.getBundleId(), token);
        executor.execute(() -> createApplicationContext(bundle, configLocations, token, 1));
        return bundle;
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, Bundle object) {
        synchronized (lifecycleLock) {
            // Invalidate any in-flight creation task for this bundle so it cannot install a stale context,
            // then tear down whatever context is already installed.
            generations.remove(bundle.getBundleId());
            closeContextLocked(bundle.getBundleId());
        }
    }

    @Override
    public void close() {
        super.close();
        retryScheduler.shutdownNow();
        executor.shutdownNow();
        synchronized (lifecycleLock) {
            generations.clear();
            for (final Long bundleId : new ArrayList<>(contexts.keySet())) {
                closeContextLocked(bundleId);
            }
        }
    }

    /** Removes and closes the installed context for a bundle id. Caller must hold {@link #lifecycleLock}. */
    private void closeContextLocked(long bundleId) {
        final ConfigurableApplicationContext context = contexts.remove(bundleId);
        if (context != null) {
            try {
                context.close();
            } catch (Throwable t) {
                LOG.warn("Failed to close Spring application context {}", context.getDisplayName(), t);
            }
        }
    }

    private List<String> getConfigLocations(Bundle bundle) {
        final String header = bundle.getHeaders().get(SPRING_CONTEXT_HEADER);
        if (header == null) {
            return List.of();
        }
        final List<String> locations = new ArrayList<>();
        for (final String token : header.split(",")) {
            // strip directives such as ";public-context:=false"
            final String location = token.split(";")[0].trim();
            if (location.isEmpty()) {
                continue;
            }
            if (WILDCARD.equals(location)) {
                final Enumeration<URL> entries = bundle.findEntries(DEFAULT_CONFIG_DIR, "*.xml", false);
                for (final URL entry : entries == null ? Collections.<URL>emptyList() : Collections.list(entries)) {
                    final String path = entry.getPath();
                    locations.add(path.startsWith("/") ? path.substring(1) : path);
                }
            } else {
                locations.add(location);
            }
        }
        return locations;
    }

    private void createApplicationContext(Bundle bundle, List<String> configLocations, Object token, int attempt) {
        if (generations.get(bundle.getBundleId()) != token) {
            LOG.info("Bundle {} was stopped/restarted before its Spring application context was created, skipping",
                    bundle.getSymbolicName());
            return;
        }
        final ConfigurableApplicationContext context;
        try {
            // buildContext() can block in refresh() (e.g. waiting on an osgi:reference); it must run WITHOUT
            // holding lifecycleLock so removedBundle/close can proceed concurrently.
            context = buildContext(bundle, configLocations);
        } catch (Throwable t) {
            final long delay = retryDelayMillis(attempt);
            LOG.error("Failed to start Spring application context for bundle {} (attempt {}); retrying in {}ms",
                    bundle.getSymbolicName(), attempt, delay, t);
            scheduleRetry(() -> createApplicationContext(bundle, configLocations, token, attempt + 1), delay);
            return;
        }
        installContext(bundle, token, context);
    }

    private static long retryDelayMillis(int attempt) {
        final long delay = RETRY_DELAY_MS << Math.min(attempt - 1, 20);
        return delay < 0 || delay > MAX_RETRY_DELAY_MS ? MAX_RETRY_DELAY_MS : delay;
    }

    /**
     * Re-dispatches a failed creation attempt to {@link #executor} after a delay (the scheduler thread must
     * never run buildContext itself — it can block for minutes). Stale retries are discarded by the
     * generation-token check at the top of createApplicationContext, exactly like first attempts.
     * Package-private seam so tests can capture the retry instead of sleeping.
     */
    void scheduleRetry(final Runnable retry, final long delayMillis) {
        try {
            retryScheduler.schedule(() -> {
                try {
                    executor.execute(retry);
                } catch (RejectedExecutionException e) {
                    // the extender is shutting down
                }
            }, delayMillis, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            // the extender is shutting down
        }
    }

    /**
     * Builds and refreshes the Spring context for a bundle. Package-private seam so tests can supply a
     * controllable/mock context (and simulate a slow refresh) without a live OSGi framework.
     */
    ConfigurableApplicationContext buildContext(Bundle bundle, List<String> configLocations) {
        // Bean classes resolve through the bundle first; Spring infrastructure classes
        // the bundle does not import (tx/aop interceptors, ...) fall back to this
        // bundle's loader, which dynamically imports org.springframework.*
        final ClassLoader classLoader = new ChainedClassLoader(
                bundle.adapt(BundleWiring.class).getClassLoader(), getClass().getClassLoader());
        final Thread thread = Thread.currentThread();
        final ClassLoader oldTccl = thread.getContextClassLoader();
        thread.setContextClassLoader(classLoader);
        try {
            final GenericApplicationContext context = new OsgiApplicationContext(bundle);
            context.setDisplayName("OSGi Spring context for bundle " + bundle.getSymbolicName());
            context.setClassLoader(classLoader);
            context.getBeanFactory().registerSingleton(BUNDLE_CONTEXT_BEAN_NAME, bundle.getBundleContext());

            final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
            reader.setBeanClassLoader(classLoader);
            reader.setNamespaceHandlerResolver(namespaceProviderRegistry.getNamespaceHandlerResolver());
            reader.setEntityResolver(namespaceProviderRegistry.getEntityResolver());
            for (final String configLocation : configLocations) {
                reader.loadBeanDefinitions(new ClassPathResource(configLocation, classLoader));
            }

            context.refresh();
            return context;
        } finally {
            thread.setContextClassLoader(oldTccl);
        }
    }

    /**
     * Installs a freshly-built context under {@link #lifecycleLock}, but only if it is still the current
     * generation for the bundle id. The bundle may have been stopped, or stopped and restarted, while we were
     * blocked in {@link #buildContext} (removedBundle could not cancel us because our context was not yet in
     * the map). If so, close our now-orphaned context rather than overwrite a newer one or leak its services.
     */
    private void installContext(Bundle bundle, Object token, ConfigurableApplicationContext context) {
        synchronized (lifecycleLock) {
            if (generations.get(bundle.getBundleId()) != token) {
                LOG.info("Bundle {} was stopped/restarted while its Spring application context was starting, closing it",
                        bundle.getSymbolicName());
                closeQuietly(context);
                return;
            }
            // Defensively replace any previously installed context for this id before installing ours.
            closeContextLocked(bundle.getBundleId());
            contexts.put(bundle.getBundleId(), context);
        }
        LOG.info("Started Spring application context for bundle {} ({} bean definitions)",
                bundle.getSymbolicName(), context.getBeanDefinitionCount());
    }

    private static void closeQuietly(final ConfigurableApplicationContext context) {
        try {
            context.close();
        } catch (Throwable t) {
            LOG.warn("Failed to close stale Spring application context {}", context.getDisplayName(), t);
        }
    }

    /**
     * Spring's PathMatchingResourcePatternResolver cannot enumerate the
     * {@code bundle:} URLs a bundle class loader returns, so wildcard
     * {@code classpath*:} lookups (hibernate packagesToScan, component-scan)
     * silently come up empty. Resolve them through the bundle wiring instead,
     * the way the Gemini resource pattern resolver used to.
     */
    private static final class OsgiApplicationContext extends GenericApplicationContext {
        private final Bundle bundle;
        private final AntPathMatcher pathMatcher = new AntPathMatcher();

        OsgiApplicationContext(Bundle bundle) {
            this.bundle = bundle;
        }

        @Override
        public Resource[] getResources(String locationPattern) throws IOException {
            if (locationPattern.startsWith(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX)) {
                final String pattern = locationPattern.substring(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX.length());
                if (pathMatcher.isPattern(pattern)) {
                    return findBundleResources(pattern);
                }
            }
            return super.getResources(locationPattern);
        }

        private Resource[] findBundleResources(String pattern) {
            final int firstWildcard = indexOfFirstWildcard(pattern);
            final int rootEnd = pattern.lastIndexOf('/', firstWildcard);
            final String rootPath = rootEnd > 0 ? pattern.substring(0, rootEnd) : "/";
            final String filePattern = pattern.substring(pattern.lastIndexOf('/') + 1);

            final BundleWiring wiring = bundle.adapt(BundleWiring.class);
            final Collection<String> names = wiring.listResources(rootPath, filePattern, BundleWiring.LISTRESOURCES_RECURSE);
            final List<Resource> resources = new ArrayList<>();
            for (final String name : names) {
                if (pathMatcher.match(pattern, name)) {
                    final URL url = wiring.getClassLoader().getResource(name);
                    if (url != null) {
                        resources.add(new UrlResource(url));
                    }
                }
            }
            return resources.toArray(new Resource[0]);
        }

        private static int indexOfFirstWildcard(String pattern) {
            final int star = pattern.indexOf('*');
            final int question = pattern.indexOf('?');
            if (star < 0) {
                return question;
            }
            return question < 0 ? star : Math.min(star, question);
        }
    }

    /**
     * Tries each delegate in order. No parent: the delegates are bundle class
     * loaders that handle java.* delegation themselves.
     */
    private static final class ChainedClassLoader extends ClassLoader {
        private final ClassLoader[] delegates;

        ChainedClassLoader(ClassLoader... delegates) {
            super(null);
            this.delegates = delegates;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            for (final ClassLoader delegate : delegates) {
                try {
                    return delegate.loadClass(name);
                } catch (ClassNotFoundException ignored) {
                    // try the next delegate
                }
            }
            throw new ClassNotFoundException(name);
        }

        @Override
        protected URL findResource(String name) {
            for (final ClassLoader delegate : delegates) {
                final URL resource = delegate.getResource(name);
                if (resource != null) {
                    return resource;
                }
            }
            return null;
        }

        @Override
        protected Enumeration<URL> findResources(String name) throws IOException {
            final List<URL> resources = new ArrayList<>();
            for (final ClassLoader delegate : delegates) {
                resources.addAll(Collections.list(delegate.getResources(name)));
            }
            return Collections.enumeration(resources);
        }
    }
}
