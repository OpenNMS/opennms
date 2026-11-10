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
package org.opennms.web.rest.v2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.exception.ConfigAlreadyExistsException;
import org.opennms.features.config.exception.ConfigNotFoundException;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.dao.jaxb.JdbcDataCollectionConfigDaoCm;

/**
 * The JDBC data collection DAO on an in-memory store. Configurations are kept as JSON, as the configuration
 * manager keeps them, and every write is validated against the schema the upgrade registers, so a DTO the
 * mapper turns into something the configuration manager would refuse fails here too.
 */
class FakeJdbcDataCollectionConfigDao extends JdbcDataCollectionConfigDaoCm {

    private static final ConfigDefinition DEFINITION = XsdHelper.buildConfigDefinition(CONFIG_NAME,
            "jdbc-datacollection.xsd", "jdbc-datacollection-config", "/rest/cm", true);

    private final Map<String, String> store = new LinkedHashMap<>();
    private int writes;

    void put(final String configId, final JdbcDataCollectionConfig config) {
        store.put(configId, ConfigConvertUtil.objectToJson(config));
    }

    int getWrites() {
        return writes;
    }

    @Override
    public void postConstruct() {
        // no configuration manager to register with
    }

    @Override
    public List<String> getConfigIds() {
        final List<String> ids = new ArrayList<>(store.keySet());
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

    @Override
    public JdbcDataCollectionConfig getConfig(final String configId) {
        final String json = store.get(configId);
        if (json == null) {
            throw new ConfigNotFoundException("NOT_FOUND: " + configId);
        }
        return ConfigConvertUtil.jsonToObject(json, JdbcDataCollectionConfig.class);
    }

    @Override
    public JdbcDataCollectionConfig copyOfConfig(final String configId) {
        return getConfig(configId);
    }

    @Override
    public JdbcDataCollectionConfig getConfig() {
        final JdbcDataCollectionConfig merged = new JdbcDataCollectionConfig();
        getConfigIds().forEach(id -> merged.merge(getConfig(id)));
        return merged;
    }

    @Override
    public void createConfig(final String configId, final JdbcDataCollectionConfig config) {
        if (store.containsKey(configId)) {
            throw new ConfigAlreadyExistsException("exists: " + configId);
        }
        store.put(configId, validated(config));
        writes++;
    }

    @Override
    public void replaceConfig(final String configId, final JdbcDataCollectionConfig config) {
        if (!store.containsKey(configId)) {
            throw new ConfigNotFoundException("NOT_FOUND: " + configId);
        }
        store.put(configId, validated(config));
        writes++;
    }

    @Override
    public void deleteConfig(final String configId) {
        store.remove(configId);
        writes++;
    }

    private static String validated(final JdbcDataCollectionConfig config) {
        final String json = ConfigConvertUtil.objectToJson(config);
        DEFINITION.validate(json);
        return json;
    }
}
