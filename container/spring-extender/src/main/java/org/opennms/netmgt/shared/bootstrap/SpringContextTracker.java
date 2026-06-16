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
import java.util.concurrent.atomic.AtomicInteger;

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

    private final NamespaceProviderRegistry namespaceProviderRegistry;
    private final Map<Long, ConfigurableApplicationContext> contexts = new ConcurrentHashMap<>();
    private final AtomicInteger threadIndex = new AtomicInteger();
    private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
        final Thread thread = new Thread(runnable, "spring-context-extender-" + threadIndex.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    });

    SpringContextTracker(BundleContext context, NamespaceProviderRegistry namespaceProviderRegistry) {
        super(context, Bundle.ACTIVE, null);
        this.namespaceProviderRegistry = namespaceProviderRegistry;
    }

    @Override
    public Bundle addingBundle(Bundle bundle, BundleEvent event) {
        final List<String> configLocations = getConfigLocations(bundle);
        if (configLocations.isEmpty()) {
            return null;
        }
        LOG.info("Creating Spring application context for bundle {} from {}", bundle.getSymbolicName(), configLocations);
        executor.execute(() -> createApplicationContext(bundle, configLocations));
        return bundle;
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, Bundle object) {
        closeContext(bundle.getBundleId());
    }

    @Override
    public void close() {
        super.close();
        executor.shutdownNow();
        for (final Long bundleId : new ArrayList<>(contexts.keySet())) {
            closeContext(bundleId);
        }
    }

    private void closeContext(long bundleId) {
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

    private void createApplicationContext(Bundle bundle, List<String> configLocations) {
        if (bundle.getState() != Bundle.ACTIVE) {
            LOG.info("Bundle {} was stopped before its Spring application context was created, skipping",
                    bundle.getSymbolicName());
            return;
        }
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
            contexts.put(bundle.getBundleId(), context);
            // The bundle may have been stopped while we were blocked in refresh (e.g.
            // waiting on an osgi:reference); removedBundle saw an empty map then, so
            // re-check and tear the context down ourselves
            if (bundle.getState() != Bundle.ACTIVE) {
                LOG.info("Bundle {} was stopped while its Spring application context was starting, closing it",
                        bundle.getSymbolicName());
                closeContext(bundle.getBundleId());
                return;
            }
            LOG.info("Started Spring application context for bundle {} ({} bean definitions)",
                    bundle.getSymbolicName(), context.getBeanDefinitionCount());
        } catch (Throwable t) {
            LOG.error("Failed to start Spring application context for bundle {}", bundle.getSymbolicName(), t);
        } finally {
            thread.setContextClassLoader(oldTccl);
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
