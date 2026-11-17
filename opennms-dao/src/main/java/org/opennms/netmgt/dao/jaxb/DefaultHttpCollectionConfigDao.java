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

import java.util.function.Consumer;

import org.json.JSONObject;
import org.opennms.features.config.exception.ConfigConversionException;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.httpdatacollection.HttpDatacollectionConfig;
import org.opennms.netmgt.dao.api.HttpCollectionConfigDao;

/**
 * Reads and writes the HTTP collector configuration through Config Management.
 *
 * <p>Updates only refresh the cached config (the default update callback). No daemon reload is
 * needed: the HttpCollector resolves its collection from this DAO on every collection attempt.</p>
 */
public class DefaultHttpCollectionConfigDao extends AbstractCmJaxbConfigDao<HttpDatacollectionConfig> implements HttpCollectionConfigDao {

    public DefaultHttpCollectionConfigDao() {
        super(HttpDatacollectionConfig.class, "HTTP Data Collection Config");
    }

    @Override
    public String getConfigName() {
        return HttpDatacollectionConfig.CM_CONFIG_NAME;
    }

    /**
     * Rejects configs that pass the schema but can't be read back, e.g. attribute types such as
     * {@code timeticks} that the schema allows but the collector doesn't support.
     */
    @Override
    public Consumer<ConfigUpdateInfo> getValidationCallback() {
        return configUpdateInfo -> {
            final JSONObject json = configUpdateInfo.getConfigJson();
            if (json == null) {
                throw new ValidationException(String.format("%s config is empty.", configUpdateInfo.getConfigName()));
            }
            try {
                ConfigConvertUtil.jsonToObject(json.toString(), HttpDatacollectionConfig.class);
            } catch (final ConfigConversionException e) {
                throw new ValidationException(rootCauseMessage(e));
            }
        };
    }

    private static String rootCauseMessage(final Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage() != null ? cause.getMessage() : t.getMessage();
    }

    @Override
    public HttpDatacollectionConfig getConfig() {
        return this.getConfig(this.getDefaultConfigId());
    }

    @Override
    public void replaceConfig(final HttpDatacollectionConfig config) {
        if (config == null) {
            return;
        }
        this.updateConfig(this.getDefaultConfigId(), ConfigConvertUtil.objectToJson(config), true);
    }
}
