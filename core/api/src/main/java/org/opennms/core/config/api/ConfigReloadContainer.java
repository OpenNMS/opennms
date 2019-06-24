/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.config.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.RegistrationListener;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A container for managing configuation that sources the configuration from one or more {@link ConfigurationProvider}.
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
        private BiFunction<V, V, V> merger;

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

        public Builder<V> withMerger(BiFunction<V, V, V> merger) {
            this.merger = merger;
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
    private BiFunction<V, V, V> merger;

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
        merger = builder.merger;
        REGISTRY.addListener(ConfigurationProvider.class, this, true);
    }

    @Override
    public V getObject() {
        checkForUpdates();
        return object;
    }

    @Override
    public void reload() {
        // Load the objects
        List<V> loadedObjects = providers.stream()
                .map(ConfigurationProviderState::load)
                .collect(Collectors.toList());

        if (loadedObjects.size() <= 0) {
            // No object
            object = null;
        } else if (loadedObjects.size() == 1) {
            // A single object
            object = loadedObjects.get(0);
        } else {
            // Many objects
            boolean first = true;
            V mergedObject = null;
            for (V loadedObject : loadedObjects) {
                if (first) {
                    mergedObject = loadedObject;
                    first = false;
                    continue;
                }
                mergedObject = merger.apply(loadedObject, mergedObject);
            }
            object = mergedObject;
        }
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
            return;
        }
        // Reset the timer
        lastReloadCheck = System.currentTimeMillis();

        if (providers.size() < 1) {
            // No resource to load
            return;
        }

        if (forceReload || providers.stream().anyMatch(ConfigurationProviderState::shouldReload)) {
            reload();
        }
    }

    @Override
    public void providerRegistered(Registration registration, ConfigurationProvider provider) {
        if (clazz.equals(registration.getProvider(ConfigurationProvider.class).getType())
            && providers.add(new ConfigurationProviderState<>(provider))) {
            LOG.debug("Registered configuration provider {} for {}.", provider, clazz.getCanonicalName());
            // Force a check on the next get
            forceReload = true;
        }
    }

    @Override
    public void providerUnregistered(Registration registration, ConfigurationProvider provider) {
        if (providers.remove(new ConfigurationProviderState(provider))) {
            LOG.debug("Unregistered configuration provider {} for {}.", provider, clazz.getCanonicalName());
            // Force a check on the next get
            forceReload = true;
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
