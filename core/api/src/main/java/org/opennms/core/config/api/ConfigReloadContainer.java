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
package org.opennms.core.config.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.RegistrationListener;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A container for managing configuration that sources the configuration from one or more {@link ConfigurationProvider}.
 *
 * @param <V> type of configuration bean
 */
public class ConfigReloadContainer<V> implements ReloadingContainer<V>, RegistrationListener<ConfigurationProvider> {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigReloadContainer.class);

    private static final long DEFAULT_RELOAD_CHECK_INTERVAL_MS = 5000;

    private static final ServiceRegistry REGISTRY = DefaultServiceRegistry.INSTANCE;

    public static class Builder<V> {
        private final Class<V> clazz;
        private V initialConfig;
        private Date lastUpdate;
        private Long reloadCheckIntervalInMs = DEFAULT_RELOAD_CHECK_INTERVAL_MS;
        private List<ConfigurationProvider> providers = new ArrayList<>();
        private BinaryOperator<V> folder;

        public Builder(Class<V> clazz) {
            this.clazz = Objects.requireNonNull(clazz);
        }

        public Builder<V> withInitialConfig(V config) {
            this.initialConfig = config;
            return this;
        }

        public Builder<V> withLastUpdate(Date lastUpdate) {
            this.lastUpdate = lastUpdate;
            return this;
        }

        public Builder<V> withProvider(ConfigurationProvider provider) {
            providers.add(provider);
            return this;
        }

        /**
         * Note: this method assumes the object being managed by this container is mutable and can be used for
         * accumulation. This simplifies the usage of clients by allowing them to provide a consumer for accumulation
         * purposes rather than a function. Should this container ever need to manage immutable objects an additional
         * method should be provided which accepts a {@link BinaryOperator} rather than a {@link BiConsumer}.
         *
         * @param folder a consumer which given an accumulator and the next value, folds the next value into the
         *               accumulator such that the accumulator now represents the merger of both objects
         */
        public Builder<V> withFolder(BiConsumer<V, V> folder) {
            this.folder = (a, b) -> {
                folder.accept(a, b);
                return a;
            };
            return this;
        }

        public Builder<V> withReloadCheckInterval(Long reloadCheckIntervalInMs) {
            this.reloadCheckIntervalInMs = reloadCheckIntervalInMs;
            return this;
        }

        public ConfigReloadContainer<V> build() {
            if (lastUpdate != null) {
                if (initialConfig == null) {
                    throw new IllegalArgumentException("Cannot set last update when no initial configuration is supplied.");
                } else if (lastUpdate.compareTo(new Date()) > 0) {
                    throw new IllegalArgumentException("Last update date cannot be in the future!");
                }
            }
            return new ConfigReloadContainer<>(this);
        }
    }

    // Config
    private final Class<V> clazz;
    private long reloadCheckIntervalInMs;
    private BinaryOperator<V> folder;

    // State
    private V object;
    private long lastUpdate = -1;
    private long lastReloadCheck = 0;
    private boolean forceReload = false;
    private final Set<ConfigurationProviderState<V>> providers = new LinkedHashSet<>();

    private ConfigReloadContainer(Builder<V> builder) {
        clazz = builder.clazz;
        object = builder.initialConfig;

        if (object != null) {
            // We have some initial state
            if (builder.lastUpdate != null) {
                // The user specified an initial date, use it
                lastUpdate = builder.lastUpdate.getTime();
            } else {
                // We have some object, but no date was specified, default to "now"
                lastUpdate = System.currentTimeMillis();
            }
            // We were provided with some object, so defer the reload check until
            // the specified interval has passed
            lastReloadCheck = System.currentTimeMillis();
        }

        if (builder.reloadCheckIntervalInMs == null) {
            reloadCheckIntervalInMs = DEFAULT_RELOAD_CHECK_INTERVAL_MS;
        } else if (builder.reloadCheckIntervalInMs <= 0) {
            reloadCheckIntervalInMs = -1;
        } else {
            reloadCheckIntervalInMs = builder.reloadCheckIntervalInMs;
        }
        builder.providers.forEach(p -> {
            providers.add(new ConfigurationProviderState<V>(p));
        });
        folder = builder.folder;
        REGISTRY.addListener(ConfigurationProvider.class, this, true);
    }

    @Override
    public V getObject() {
        checkForUpdates();
        return object;
    }

    @Override
    public void reload() {
        if (providers.isEmpty()) {
            object = null;
        } else if (providers.size() == 1) {
            object = providers.iterator().next().load();
        } else {
            object = providers.stream()
                    .map(ConfigurationProviderState::load)
                    .filter(Objects::nonNull)
                    .reduce(folder)
                    .orElse(null);
        }
        LOG.debug("reloaded - conf object: {}", object);
    }

    @Override
    public void setReloadCheckInterval(Long reloadCheckInterval) {
        if (reloadCheckInterval == null) {
            this.reloadCheckIntervalInMs = DEFAULT_RELOAD_CHECK_INTERVAL_MS;
        } else if (reloadCheckInterval <= 0) {
            this.reloadCheckIntervalInMs = -1;
        } else {
            this.reloadCheckIntervalInMs = reloadCheckInterval;
        }
    }

    @Override
    public Long getLastUpdate() {
        return lastUpdate;
    }

    private synchronized void checkForUpdates() {
        if (!forceReload && (reloadCheckIntervalInMs < 0 || System.currentTimeMillis() < (lastReloadCheck + reloadCheckIntervalInMs))) {
            // Reload checking is disabled, or the time hasn't elapsed since the last check
            LOG.debug("reload not forced and reload check disabled or check interval not yet elapsed");
            return;
        }
        // Reset the timer
        lastReloadCheck = System.currentTimeMillis();

        if (forceReload || providers.isEmpty() || providers.stream().anyMatch(ConfigurationProviderState::shouldReload)) {
            reload();
            forceReload = false;
        }
    }

    @Override
    public void providerRegistered(Registration registration, ConfigurationProvider provider) {
        boolean added;
        // must be synchronized for two reasons:
        // 1. the providers collection must not be modified concurrently
        // 2. forceReload changes must made visible to other threads
        synchronized (this) {
            if (clazz.equals(registration.getProvider(ConfigurationProvider.class).getType())
                && providers.add(new ConfigurationProviderState<>(provider))) {
                LOG.debug("Registered configuration provider {} for {}.", provider, clazz.getCanonicalName());
                // Force a check on the next get
                forceReload = true;
                added = true;
            } else {
                added = false;
            }
        }
        if (added) {
            provider.registeredToConfigReloadContainer();
        }
    }

    @Override
    public void providerUnregistered(Registration registration, ConfigurationProvider provider) {
        boolean removed;
        // must be synchronized for two reasons (see above)
        synchronized (this) {
            if (providers.remove(new ConfigurationProviderState(provider))) {
                LOG.debug("Unregistered configuration provider {} for {}.", provider, clazz.getCanonicalName());
                // Force a check on the next get
                forceReload = true;
                removed = true;
            } else {
                removed = false;
            }
        }
        if (removed) {
            provider.deregisteredFromConfigReloadContainer();
        }
    }

    private static class ConfigurationProviderState<V> {
        private final ConfigurationProvider provider;
        private long lastLoad;

        private ConfigurationProviderState(ConfigurationProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        private V load() {
            final V object = (V)provider.getObject();
            lastLoad = System.currentTimeMillis();
            return object;
        }

        private boolean shouldReload() {
            return lastLoad <= provider.getLastUpdate();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConfigurationProviderState that = (ConfigurationProviderState) o;
            return Objects.equals(provider, that.provider);
        }

        @Override
        public int hashCode() {
            return Objects.hash(provider);
        }

    }
}
