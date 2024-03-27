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
