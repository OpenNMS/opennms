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
package org.opennms.protocols.xml.dao.cm;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.EventType;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlDataCollectionConfig;
import org.opennms.protocols.xml.config.XmlGroups;
import org.opennms.protocols.xml.config.XmlSource;
import org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Reads the XML collector configuration from the configuration manager.
 *
 * <p>It is stored as two configurations:</p>
 * <ul>
 *     <li>{@value #CONFIG_NAME}: the former {@code etc/xml-datacollection-config.xml}, one configuration only.</li>
 *     <li>{@value #GROUPS_CONFIG_NAME}: the former {@code etc/xml-datacollection/*.xml} group files, one
 *     configuration per file. The configId is the file name without {@code .xml}.</li>
 * </ul>
 *
 * <p>An {@code <import-groups>} entry of a source names a group configuration. Both the configId
 * ({@code elasticsearch-cluster-stats}) and the former file path ({@code xml-datacollection/elasticsearch-cluster-stats.xml})
 * are accepted, so configurations imported from {@code etc} keep working unchanged. The groups are appended to the
 * source's own groups, as they were when they were read from files.</p>
 *
 * <p>The resolved configuration is cached and dropped whenever either configuration changes, so edits take effect
 * with the next collection, without a restart. This also covers changes to a group alone, which the file based DAO
 * never noticed.</p>
 */
public class XmlDataCollectionConfigDaoCm implements XmlDataCollectionConfigDao {

    private static final Logger LOG = LoggerFactory.getLogger(XmlDataCollectionConfigDaoCm.class);

    public static final String CONFIG_NAME = "xml-datacollection-config";
    public static final String GROUPS_CONFIG_NAME = "xml-datacollection-groups";

    private static final String XML_EXTENSION = ".xml";

    private ConfigurationManagerService configurationManagerService;

    private final AtomicBoolean handlersRegistered = new AtomicBoolean(false);

    private volatile XmlDataCollectionConfig config;

    public XmlDataCollectionConfigDaoCm() {
    }

    public XmlDataCollectionConfigDaoCm(final ConfigurationManagerService configurationManagerService) {
        setConfigurationManagerService(configurationManagerService);
    }

    @Autowired(required = false)
    public void setConfigurationManagerService(final ConfigurationManagerService configurationManagerService) {
        this.configurationManagerService = Objects.requireNonNull(configurationManagerService);
    }

    /**
     * The configId of the group configuration an {@code <import-groups>} entry refers to.
     *
     * @param importGroup a configId or a path relative to {@code etc}, e.g. {@code xml-datacollection/foo.xml}
     * @return the configId, e.g. {@code foo}
     */
    public static String toGroupsConfigId(final String importGroup) {
        String id = importGroup.trim().replace('\\', '/');
        id = id.substring(id.lastIndexOf('/') + 1);
        if (id.toLowerCase().endsWith(XML_EXTENSION)) {
            id = id.substring(0, id.length() - XML_EXTENSION.length());
        }
        return id;
    }

    @Override
    public XmlDataCollectionConfig getConfig() {
        XmlDataCollectionConfig current = config;
        if (current == null) {
            synchronized (this) {
                current = config;
                if (current == null) {
                    current = load();
                    config = current;
                }
            }
        }
        return current;
    }

    @Override
    public XmlDataCollection getDataCollectionByName(final String name) {
        return getConfig().getDataCollectionByName(name);
    }

    @Override
    public XmlDataCollection getDataCollectionByIndex(final int idx) {
        return getConfig().getXmlDataCollections().get(idx);
    }

    /**
     * Drops the cached configuration, the next access reads it again from the configuration manager.
     */
    public void reload() {
        LOG.debug("XML data collection configuration changed, reloading it with the next access.");
        config = null;
    }

    private XmlDataCollectionConfig load() {
        registerHandlers();
        final long start = System.currentTimeMillis();
        final Optional<XmlDataCollectionConfig> json = read(CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID, XmlDataCollectionConfig.class);
        if (json.isEmpty()) {
            LOG.warn("No configuration {}/{} found, no XML data collections are defined.", CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID);
            return new XmlDataCollectionConfig();
        }
        final XmlDataCollectionConfig loaded = json.get();
        for (final XmlDataCollection collection : loaded.getXmlDataCollections()) {
            for (final XmlSource source : collection.getXmlSources()) {
                importGroups(collection, source);
            }
        }
        LOG.info("Loaded XML data collection configuration with {} collections in {} ms.",
                loaded.getXmlDataCollections().size(), System.currentTimeMillis() - start);
        return loaded;
    }

    private void importGroups(final XmlDataCollection collection, final XmlSource source) {
        if (!source.hasImportGroups()) {
            return;
        }
        for (final String importGroup : source.getImportGroupsList()) {
            final String groupsId = toGroupsConfigId(importGroup);
            final Optional<XmlGroups> groups = read(GROUPS_CONFIG_NAME, groupsId, XmlGroups.class);
            if (groups.isPresent()) {
                LOG.debug("Adding {} groups of {}/{} to source {} of collection {}.",
                        groups.get().getXmlGroups().size(), GROUPS_CONFIG_NAME, groupsId, source.getUrl(), collection.getName());
                source.getXmlGroups().addAll(groups.get().getXmlGroups());
            } else {
                LOG.warn("Collection {}, source {}: imported groups '{}' not found (configuration {}/{}).",
                        collection.getName(), source.getUrl(), importGroup, GROUPS_CONFIG_NAME, groupsId);
            }
        }
    }

    private <T> Optional<T> read(final String configName, final String configId, final Class<T> type) {
        return getConfigurationManagerService().getJSONConfiguration(configName, configId)
                .map(json -> ConfigConvertUtil.jsonToObject(withoutEmptyContainers(json).toString(), type));
    }

    /**
     * Removes empty objects and arrays. The configuration manager fills in an empty object for every optional child
     * missing in a stored configuration, e.g. {@code "request":{"parameter":[],"header":[]}} for a source without
     * request. Read as is, such a source would get a {@link org.opennms.protocols.xml.config.Request} where the XML
     * configuration had none.
     */
    static JSONObject withoutEmptyContainers(final JSONObject json) {
        for (final String key : new ArrayList<>(json.keySet())) {
            final Object value = json.get(key);
            if (value instanceof JSONObject) {
                withoutEmptyContainers((JSONObject) value);
                if (((JSONObject) value).isEmpty()) {
                    json.remove(key);
                }
            } else if (value instanceof JSONArray) {
                final JSONArray array = (JSONArray) value;
                for (final Object item : array) {
                    if (item instanceof JSONObject) {
                        withoutEmptyContainers((JSONObject) item);
                    }
                }
                if (array.isEmpty()) {
                    json.remove(key);
                }
            }
        }
        return json;
    }

    private void registerHandlers() {
        if (!handlersRegistered.compareAndSet(false, true)) {
            return;
        }
        final ConfigurationManagerService cms = getConfigurationManagerService();
        for (final String configName : new String[]{CONFIG_NAME, GROUPS_CONFIG_NAME}) {
            final ConfigUpdateInfo anyId = new ConfigUpdateInfo(configName, ConfigUpdateInfo.WILDCARD_ID);
            for (final EventType type : new EventType[]{EventType.CREATE, EventType.UPDATE, EventType.DELETE}) {
                cms.registerEventHandler(type, anyId, info -> reload());
            }
        }
    }

    private ConfigurationManagerService getConfigurationManagerService() {
        if (configurationManagerService == null) {
            throw new IllegalStateException("No ConfigurationManagerService available to read the XML data collection configuration.");
        }
        return configurationManagerService;
    }
}
