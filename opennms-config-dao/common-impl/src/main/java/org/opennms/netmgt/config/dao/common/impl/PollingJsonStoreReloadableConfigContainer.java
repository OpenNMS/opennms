/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.dao.common.impl;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.dao.common.api.ReloadableConfigContainer;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Try;

/**
 * An abstraction to contain a JAXB annotated XML document that has been marshalled to JSON and stored in a
 * {@link JsonStore}.
 * <p>
 * This container caches a copy of the entity and keeps it up to date relative to the copy in the JSON store based on a
 * fixed polling interval. Polling is only done lazily on calls to {@link #getConfig()}.
 */
public class PollingJsonStoreReloadableConfigContainer<T> implements ReloadableConfigContainer<T> {
    private final ObjectMapper mapper = JacksonUtils.createDefaultObjectMapper();
    private final JsonStore jsonStore;
    private final String key;
    private final String context;
    private final Class<T> entityClass;
    private final long pollingIntervalMs;
    private final Supplier<Optional<T>> retryableConfigSupplier;
    private long lastGotTime;
    private long lastCheckedTime;
    private T config;

    public PollingJsonStoreReloadableConfigContainer(JsonStore jsonStore, String key, String context,
                                                     Class<T> entityClass, long pollingIntervalMs, Retry retry) {
        this.jsonStore = Objects.requireNonNull(jsonStore);
        this.key = Objects.requireNonNull(key);
        this.context = Objects.requireNonNull(context);
        this.entityClass = Objects.requireNonNull(entityClass);
        this.pollingIntervalMs = pollingIntervalMs;
        retryableConfigSupplier = Retry.decorateSupplier(Objects.requireNonNull(retry), this::reloadedConfigSupplier);
    }

    public PollingJsonStoreReloadableConfigContainer(JsonStore jsonStore, String key, String context,
                                                     Class<T> entityClass) {
        // Constructor providing a default polling interval and retry strategy
        this(jsonStore, key, context, entityClass, TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES),
                Retry.of("retryReloading", RetryConfig.custom()
                        .maxAttempts(60)
                        .waitDuration(Duration.ofSeconds(5))
                        .build()));
    }

    @Override
    public synchronized void reload() {
        Try.ofSupplier(retryableConfigSupplier).getOrElseThrow(t -> new RuntimeException(t))
                .ifPresent(reloadedConfig -> config = reloadedConfig);
    }

    @Override
    public T getConfig() {
        // Lazily poll for an updated configuration
        if (config == null || System.currentTimeMillis() > lastCheckedTime + pollingIntervalMs) {
            reload();
        }
        return config;
    }

    /**
     * @return an Optional containing the new config if the old config was stale, otherwise containing empty if the old
     * config is still up to date
     */
    private Optional<T> reloadedConfigSupplier() {
        lastCheckedTime = System.currentTimeMillis();
        long lastUpdated = jsonStore.getLastUpdated(key, context)
                .orElseThrow(PollingJsonStoreReloadableConfigContainer::failedToFind);

        if (lastUpdated <= lastGotTime) {
            return Optional.empty();
        }

        // Get JSON and convert to jaxb object
        String jsonDocument = jsonStore.get(key, context)
                .orElseThrow(PollingJsonStoreReloadableConfigContainer::failedToFind);

        try {
            T updatedConfig = mapper.readValue(jsonDocument, entityClass);
            lastGotTime = lastUpdated;
            return Optional.of(updatedConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static RuntimeException failedToFind() {
        throw new RuntimeException("Failed to find the configuration in the JSON store");
    }
}
