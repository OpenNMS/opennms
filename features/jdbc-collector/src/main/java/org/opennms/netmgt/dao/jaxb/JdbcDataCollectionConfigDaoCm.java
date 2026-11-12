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
package org.opennms.netmgt.dao.jaxb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.exception.ConfigNotFoundException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.EventType;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.jdbc.JdbcDataCollection;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.dao.JdbcDataCollectionConfigDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC data collection configuration stored in the configuration manager.
 *
 * <p>The schema allows multiple configurations. {@code default} holds what used to be
 * {@code jdbc-datacollection-config.xml}, and every file of {@code jdbc-datacollection.d/} became a configuration
 * named after the file (e.g. {@code postgresql}). The collector sees one merged configuration: {@code default} first,
 * then the others sorted by id, which is the order the files used to be merged in. When a collection name is defined
 * more than once, the first one wins.</p>
 *
 * <p>The merged view is cached and rebuilt after any configuration of the schema is created, updated or deleted.</p>
 */
public class JdbcDataCollectionConfigDaoCm extends AbstractCmJaxbConfigDao<JdbcDataCollectionConfig> implements JdbcDataCollectionConfigDao {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcDataCollectionConfigDaoCm.class);

    public static final String CONFIG_NAME = "jdbc-datacollection-config";

    private final AtomicLong generation = new AtomicLong();
    private volatile JdbcDataCollectionConfig merged;

    public JdbcDataCollectionConfigDaoCm() {
        super(JdbcDataCollectionConfig.class, "JDBC Data Collection Configuration");
    }

    @Override
    public String getConfigName() {
        return CONFIG_NAME;
    }

    /**
     * Registers one handler for all configurations of the schema, so configurations created after startup are
     * tracked too. Replaces the per-configuration handlers of the base class.
     */
    @Override
    @PostConstruct
    public void postConstruct() {
        final ConfigUpdateInfo allConfigs = new ConfigUpdateInfo(CONFIG_NAME, ConfigUpdateInfo.WILDCARD_ID);
        final Consumer<ConfigUpdateInfo> reload = this::onConfigChanged;
        configurationManagerService.registerEventHandler(EventType.CREATE, allConfigs, reload);
        configurationManagerService.registerEventHandler(EventType.UPDATE, allConfigs, reload);
        configurationManagerService.registerEventHandler(EventType.DELETE, allConfigs, info -> invalidate());
    }

    private void onConfigChanged(final ConfigUpdateInfo info) {
        try {
            // refresh the cached entity of this configuration
            loadConfig(info.getConfigId());
        } catch (ConfigNotFoundException e) {
            LOG.debug("Configuration {} vanished while reloading it.", info.getConfigId());
        }
        invalidate();
    }

    private void invalidate() {
        generation.incrementAndGet();
        merged = null;
    }

    /**
     * @return the ids of all stored configurations, in merge order: {@code default} first, then sorted.
     */
    public List<String> getConfigIds() {
        final List<String> ids = new ArrayList<>(configurationManagerService.getConfigIds(CONFIG_NAME));
        ids.sort((a, b) -> {
            if (a.equals(b)) {
                return 0;
            } else if (ConfigDefinition.DEFAULT_CONFIG_ID.equals(a)) {
                return -1;
            } else if (ConfigDefinition.DEFAULT_CONFIG_ID.equals(b)) {
                return 1;
            }
            return a.compareTo(b);
        });
        return ids;
    }

    /**
     * @return all configurations merged into one
     */
    @Override
    public JdbcDataCollectionConfig getConfig() {
        JdbcDataCollectionConfig config = merged;
        if (config != null) {
            return config;
        }
        synchronized (this) {
            config = merged;
            if (config != null) {
                return config;
            }
            final long current = generation.get();
            config = merge();
            if (current == generation.get()) {
                // nothing changed while merging
                merged = config;
            }
            return config;
        }
    }

    private JdbcDataCollectionConfig merge() {
        final JdbcDataCollectionConfig result = new JdbcDataCollectionConfig();
        final Map<String, String> collectionOrigin = new HashMap<>();
        for (final String configId : getConfigIds()) {
            final JdbcDataCollectionConfig config;
            try {
                config = getConfig(configId);
            } catch (ConfigNotFoundException e) {
                LOG.debug("Configuration {} was deleted while merging.", configId);
                continue;
            }
            for (final JdbcDataCollection collection : config.getJdbcDataCollections()) {
                final String previous = collectionOrigin.putIfAbsent(collection.getName(), configId);
                if (previous != null) {
                    LOG.warn("JDBC collection '{}' of configuration '{}' is ignored, configuration '{}' already defines it.",
                            collection.getName(), configId, previous);
                }
            }
            result.merge(config);
        }
        return result;
    }

    /**
     * @return a private copy of a stored configuration, safe to modify; the instance {@link #getConfig(String)}
     *         returns is shared with the collector
     * @throws ConfigNotFoundException when no configuration with that id is stored
     */
    public JdbcDataCollectionConfig copyOfConfig(final String configId) {
        if (!configurationManagerService.getConfigIds(CONFIG_NAME).contains(configId)) {
            throw new ConfigNotFoundException("NOT_FOUND: configName: " + CONFIG_NAME + " configId: " + configId);
        }
        return ConfigConvertUtil.jsonToObject(ConfigConvertUtil.objectToJson(getConfig(configId)), JdbcDataCollectionConfig.class);
    }

    /**
     * Stores a new configuration.
     *
     * @throws org.opennms.features.config.exception.ConfigAlreadyExistsException when the id is taken
     * @throws org.opennms.features.config.exception.ValidationException when the schema rejects it
     */
    public void createConfig(final String configId, final JdbcDataCollectionConfig config) {
        configurationManagerService.registerConfiguration(CONFIG_NAME, configId, new JsonAsString(ConfigConvertUtil.objectToJson(config)));
    }

    /**
     * Replaces a stored configuration in full. Unlike {@link #updateConfig(String, JdbcDataCollectionConfig)}, which
     * merges the top-level keys, this also removes the last collection or the repository.
     *
     * @throws org.opennms.features.config.exception.ValidationException when the schema rejects it
     */
    public void replaceConfig(final String configId, final JdbcDataCollectionConfig config) {
        updateConfig(configId, ConfigConvertUtil.objectToJson(config), true);
    }

    public void deleteConfig(final String configId) {
        configurationManagerService.unregisterConfiguration(CONFIG_NAME, configId);
    }

    @Override
    public JdbcDataCollection getDataCollectionByName(final String name) {
        for (final JdbcDataCollection collection : getConfig().getJdbcDataCollections()) {
            if (Objects.equals(collection.getName(), name)) {
                return collection;
            }
        }
        return null;
    }

    @Override
    public JdbcDataCollection getDataCollectionByIndex(final int idx) {
        return getConfig().getJdbcDataCollections().get(idx);
    }
}
