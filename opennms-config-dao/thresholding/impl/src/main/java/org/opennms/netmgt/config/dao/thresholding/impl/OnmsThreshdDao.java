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
package org.opennms.netmgt.config.dao.thresholding.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.config.api.ConfigReloadContainer;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.EventType;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.dao.common.api.ConfigDaoConstants;
import org.opennms.netmgt.config.dao.thresholding.api.WriteableThreshdDao;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;

/**
 * Threshd configuration stored in the configuration manager.
 *
 * <p>The published configuration ({@link #getReadOnlyConfig()}, {@link #getWriteableConfig()}) is never
 * mutated; it is only ever replaced. Readers therefore need no lock: whatever they got stays consistent while
 * they iterate it. Writers get a private draft through {@link #withWriteLock}, and {@link #saveConfig()}
 * persists that draft; the published configuration only changes once the configuration manager has accepted
 * it. Every replacement happens under the write lock, so a reload can no longer slip in between a writer's
 * edits and its save.</p>
 */
public class OnmsThreshdDao extends AbstractThreshdDao implements WriteableThreshdDao {
    public static final String CONFIG_NAME = "threshd-config";

    private final ConfigReloadContainer<ThreshdConfiguration> extContainer;
    private final ObjectMapper objectMapper = JacksonUtils.createDefaultObjectMapper();
    private volatile ThreshdConfiguration dbConfig;
    private volatile ConfigurationManagerService cms;
    private volatile boolean initialized;
    /** Set when a configuration manager update arrived while the write lock was held; see {@link #onCmsUpdate()}. */
    private volatile boolean reloadPending;

    // The fields below are only accessed while holding the write lock.
    /** True while the first load runs, so the reentrant IPMap lookup does not load again. */
    private boolean loading;
    /** The draft handed to the current {@link #withWriteLock} consumer, if any. */
    private ThreshdConfiguration pendingDraft;
    /** Incremented by every successful reload, so a save can tell whether its update event reloaded. */
    private long reloadCount;

    public OnmsThreshdDao(JsonStore jsonStore) {
        super(jsonStore);
        extContainer = new ConfigReloadContainer.Builder<>(ThreshdConfiguration.class)
                .withFolder((accumulator, next) -> accumulator.getPackages().addAll(next.getPackages()))
                .build();
        // No I/O in the constructor — CMS is looked up lazily on first access (matches SnmpPeerFactory).
    }

    /** Test hook: uses the given CMS instead of looking it up in the Spring context. */
    OnmsThreshdDao(JsonStore jsonStore, ConfigurationManagerService cms) {
        this(jsonStore);
        this.cms = registerWith(cms);
    }

    private ConfigurationManagerService getCms() {
        if (cms == null) {
            synchronized (this) {
                if (cms == null) {
                    cms = registerWith(BeanUtils.getBean("daoContext", "configurationManagerService", ConfigurationManagerService.class));
                }
            }
        }
        return cms;
    }

    private ConfigurationManagerService registerWith(ConfigurationManagerService cms) {
        cms.registerEventHandler(EventType.UPDATE,
                new ConfigUpdateInfo(CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID),
                info -> onCmsUpdate());
        return cms;
    }

    private void ensureInitialized() {
        if (initialized) {
            return;
        }
        final Lock lock = getWriteLock();
        lock.lock();
        try {
            // reload() → super.reload() → IPMap → getReadOnlyConfig() comes back here on the same thread;
            // dbConfig is already set by then, so just let it through. initialized is only set by a
            // successful reload(), so a failed first load is retried on the next access.
            if (!initialized && !loading) {
                loading = true;
                try {
                    reload();
                } finally {
                    loading = false;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return the merged configuration consisting of the database configuration and any configuration provided by
     * extensions
     */
    @Override
    public ThreshdConfiguration getReadOnlyConfig() {
        ensureInitialized();
        return getMergedConfig();
    }

    /**
     * @return just the configuration from the database since configuration provided by extensions is read only.
     * The returned instance is shared with every other reader and must not be modified; use
     * {@link #withWriteLock} to make changes.
     */
    @Override
    public ThreshdConfiguration getWriteableConfig() {
        ensureInitialized();
        return dbConfig;
    }

    /**
     * Hands the consumer a private copy of the database configuration (or {@code null} if there is none) while
     * holding the write lock. Changes become visible to readers only once {@link #saveConfig()} succeeds.
     */
    @Override
    public void withWriteLock(Consumer<ThreshdConfiguration> consumerWithLock) {
        final Lock lock = getWriteLock();
        lock.lock();
        final ThreshdConfiguration previousDraft = pendingDraft;
        try {
            ensureInitialized();
            if (pendingDraft == null) {
                pendingDraft = copyOf(dbConfig);
            }
            consumerWithLock.accept(pendingDraft);
        } finally {
            pendingDraft = previousDraft;
            lock.unlock();
        }
        drainPendingReload();
    }

    @Override
    public void reload() {
        final Lock lock = getWriteLock();
        lock.lock();
        try {
            reloadPending = false;
            dbConfig = loadFromDb();
            super.reload();
            publishMergedConfig();
            initialized = true;
            reloadCount++;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Persists the draft of the enclosing {@link #withWriteLock} call, or the published configuration when called
     * outside of one. If the configuration manager rejects it, the published configuration is left as it was.
     */
    @Override
    public void saveConfig() {
        final Lock lock = getWriteLock();
        lock.lock();
        try {
            final ThreshdConfiguration config = pendingDraft != null ? pendingDraft : dbConfig;
            if (config == null) {
                throw new IllegalStateException("No threshd configuration loaded; cannot save");
            }
            final long reloadsBefore = reloadCount;
            getCms().updateConfiguration(CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID,
                    new JsonAsString(ConfigConvertUtil.objectToJson(config)), true);
            // The update event normally reloads synchronously on this thread. If it did not (no handler, or the
            // handler failed and the configuration manager only logged it), publish what was stored ourselves.
            if (reloadCount == reloadsBefore) {
                reload();
            }
        } finally {
            lock.unlock();
        }
        drainPendingReload();
    }

    @Override
    public void onConfigChanged() {
        reload();
    }

    /**
     * Update handler registered with the configuration manager. It runs while the configuration manager holds a
     * lock of its own (its handler map entry), and a writer holding our write lock may be waiting for that very
     * lock in {@code updateConfiguration()}. Blocking here would deadlock, so if the write lock is taken the
     * reload is left to whoever releases it.
     */
    private void onCmsUpdate() {
        reloadPending = true;
        drainPendingReload();
    }

    private void drainPendingReload() {
        final Lock lock = getWriteLock();
        while (reloadPending && lock.tryLock()) {
            try {
                if (reloadPending) {
                    reload();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private ThreshdConfiguration loadFromDb() {
        return getCms().getJSONStrConfiguration(CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID)
                .map(json -> ConfigConvertUtil.jsonToObject(json, ThreshdConfiguration.class))
                .orElse(null);
    }

    private static ThreshdConfiguration copyOf(ThreshdConfiguration config) {
        return config == null ? null
                : ConfigConvertUtil.jsonToObject(ConfigConvertUtil.objectToJson(config), ThreshdConfiguration.class);
    }

    private ThreshdConfiguration getMergedConfig() {
        final ThreshdConfiguration databaseConfig = dbConfig;
        final ThreshdConfiguration externalConfig = extContainer.getObject();

        if (databaseConfig == null && externalConfig == null) {
            return null;
        } else if (externalConfig == null) {
            return databaseConfig;
        } else if (databaseConfig == null) {
            return externalConfig;
        }

        // Create a merged config by combining the config from the database and the external config provided by extensions
        ThreshdConfiguration mergedConfig = new ThreshdConfiguration();

        List<Package> mergedPackages = new ArrayList<>();
        mergedPackages.addAll(databaseConfig.getPackages());
        mergedPackages.addAll(externalConfig.getPackages());
        mergedConfig.setPackages(Collections.unmodifiableList(mergedPackages));

        return mergedConfig;
    }

    /** Called with the write lock held. */
    private void publishMergedConfig() {
        ThreshdConfiguration merged = getMergedConfig();
        if (merged == null) {
            return;
        }
        try {
            jsonStore.put(JSON_STORE_KEY, objectMapper.writeValueAsString(merged),
                    ConfigDaoConstants.JSON_KEY_STORE_CONTEXT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
