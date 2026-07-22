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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Aggregates {@code META-INF/spring.handlers} and {@code META-INF/spring.schemas}
 * mappings from all installed bundles, so XML namespaces (context, tx, cache,
 * onmsgi, ...) resolve when parsing a Spring context inside OSGi. This replaces
 * Gemini's NamespaceHandlerActivator.
 *
 * Providers are preferred in descending bundle-version order and a handler is only
 * accepted if it is type-compatible with this bundle's Spring class space, so when
 * multiple Spring versions are installed (4.3 for Camel, 5.3 for OpenNMS) the
 * handlers wired to 5.3 win.
 */
class NamespaceProviderRegistry extends BundleTracker<Bundle> {

    private static final Logger LOG = LoggerFactory.getLogger(NamespaceProviderRegistry.class);

    private static final String HANDLER_MAPPINGS = "META-INF/spring.handlers";
    private static final String SCHEMA_MAPPINGS = "META-INF/spring.schemas";

    /** The (Gemini era) spring-osgi namespace, handled by the extender itself. */
    static final String OSGI_NAMESPACE_URI = "http://www.springframework.org/schema/osgi";
    private static final String OSGI_SCHEMA_MARKER = "/schema/osgi/spring-osgi";
    private static final String OSGI_SCHEMA_RESOURCE = "/META-INF/spring-osgi.xsd";

    private static final class Provider {
        final Bundle bundle;
        final String value;

        Provider(Bundle bundle, String value) {
            this.bundle = bundle;
            this.value = value;
        }
    }

    private final Map<String, List<Provider>> handlers = new ConcurrentHashMap<>();
    private final Map<String, List<Provider>> schemas = new ConcurrentHashMap<>();
    private final Map<String, NamespaceHandler> resolvedHandlers = new ConcurrentHashMap<>();

    NamespaceProviderRegistry(BundleContext context) {
        super(context, Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING, null);
    }

    @Override
    public Bundle addingBundle(Bundle bundle, BundleEvent event) {
        addMappings(bundle, HANDLER_MAPPINGS, handlers);
        addMappings(bundle, SCHEMA_MAPPINGS, schemas);
        return bundle;
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, Bundle object) {
        removeMappings(bundle, handlers);
        removeMappings(bundle, schemas);
        resolvedHandlers.clear();
    }

    private void addMappings(Bundle bundle, String resource, Map<String, List<Provider>> target) {
        final Properties mappings = loadProperties(bundle, resource);
        if (mappings == null) {
            return;
        }
        for (final String key : mappings.stringPropertyNames()) {
            final List<Provider> providers = target.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
            providers.add(new Provider(bundle, mappings.getProperty(key)));
            providers.sort(Comparator.comparing((Provider p) -> p.bundle.getVersion()).reversed());
        }
        resolvedHandlers.clear();
    }

    private void removeMappings(Bundle bundle, Map<String, List<Provider>> target) {
        for (final List<Provider> providers : target.values()) {
            providers.removeIf(p -> p.bundle.getBundleId() == bundle.getBundleId());
        }
    }

    private Properties loadProperties(Bundle bundle, String resource) {
        final URL url = bundle.getEntry(resource);
        if (url == null) {
            return null;
        }
        try (InputStream is = url.openStream()) {
            final Properties properties = new Properties();
            properties.load(is);
            return properties;
        } catch (IOException e) {
            LOG.warn("Failed to read {} from bundle {}", resource, bundle.getSymbolicName(), e);
            return null;
        }
    }

    NamespaceHandlerResolver getNamespaceHandlerResolver() {
        return namespaceUri -> resolvedHandlers.computeIfAbsent(namespaceUri, this::createHandler);
    }

    private NamespaceHandler createHandler(String namespaceUri) {
        if (OSGI_NAMESPACE_URI.equals(namespaceUri)) {
            final NamespaceHandler handler = new OsgiCompatNamespaceHandler();
            handler.init();
            return handler;
        }
        for (final Provider provider : handlers.getOrDefault(namespaceUri, List.of())) {
            try {
                final Object instance = provider.bundle.loadClass(provider.value).getDeclaredConstructor().newInstance();
                if (!(instance instanceof NamespaceHandler)) {
                    LOG.warn("Namespace handler {} for {} from bundle {} belongs to a different Spring class space, skipping",
                            provider.value, namespaceUri, provider.bundle.getSymbolicName());
                    continue;
                }
                final NamespaceHandler handler = (NamespaceHandler) instance;
                handler.init();
                LOG.debug("Resolved namespace {} to {} from bundle {}", namespaceUri, provider.value, provider.bundle.getSymbolicName());
                return handler;
            } catch (Throwable t) {
                LOG.warn("Failed to instantiate namespace handler {} for {} from bundle {}",
                        provider.value, namespaceUri, provider.bundle.getSymbolicName(), t);
            }
        }
        // Returning null makes Spring fail with a clear "Unable to locate NamespaceHandler" message
        LOG.error("No namespace handler found for {}", namespaceUri);
        return null;
    }

    EntityResolver getEntityResolver() {
        return this::resolveEntity;
    }

    private InputSource resolveEntity(String publicId, String systemId) throws IOException {
        if (systemId == null) {
            return null;
        }
        if (systemId.contains(OSGI_SCHEMA_MARKER)) {
            return toInputSource(getClass().getResource(OSGI_SCHEMA_RESOURCE), publicId, systemId);
        }
        for (final Provider provider : schemas.getOrDefault(systemId, List.of())) {
            URL url = provider.bundle.getEntry(provider.value);
            if (url == null) {
                url = provider.bundle.getResource(provider.value);
            }
            if (url != null) {
                return toInputSource(url, publicId, systemId);
            }
        }
        LOG.warn("Unable to resolve schema {} from any installed bundle", systemId);
        return null;
    }

    private InputSource toInputSource(URL url, String publicId, String systemId) throws IOException {
        if (url == null) {
            return null;
        }
        final InputSource source = new InputSource(url.openStream());
        source.setPublicId(publicId);
        source.setSystemId(systemId);
        return source;
    }

    /** Visible for {@link SpringContextTracker} logging. */
    List<String> knownNamespaces() {
        return new ArrayList<>(handlers.keySet());
    }
}
