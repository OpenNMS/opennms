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
package org.opennms.features.config.service.api;

import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.callback.DefaultCmJaxbConfigDaoUpdateCallback;

import java.util.function.Consumer;

public interface CmJaxbConfigDao<E> {
    /**
     * ConfigName use for the Config and Schema
     *
     * @return ConfigName
     */
    String getConfigName();

    /**
     * It will provide the callback for all update, you can simply
     *
     * @return Consumer
     */
    default Consumer<ConfigUpdateInfo> getUpdateCallback() {
        return new DefaultCmJaxbConfigDaoUpdateCallback(this);
    }

    /**
     * It will return null validation callback for all ConfigDao.
     * By default, it is no needed. Validation will rely on openAPI schema. (xsd)
     * Override if you need to do extra validation logic. e.g. quartz expression is not possible to validate by pattern.
     * @return Consumer
     */
    default Consumer<E> getValidationCallback() {
        return null;
    }

    /**
     * The default configId when getConfig without passing configId
     *
     * @return configId
     */
    default String getDefaultConfigId() {
        return ConfigDefinition.DEFAULT_CONFIG_ID;
    }

    /**
     * It will load the default config
     *
     * @return ConfigObject
     */
    E loadConfig();

    /**
     * loadConfig from database by CM. If it is already in cache, it will update the cache.
     *
     * @param configId
     * @return ConfigObject
     */
    E loadConfig(final String configId);

    /**
     * It will the config in cache, if nothing found it will load from db.
     * <b>Please notice that, config can be different in db.</b>
     *
     * @param configId
     * @return config
     */
    E getConfig(String configId);

    void updateConfig(String configId, String jsonConfigString, boolean isReplace) throws ValidationException;
}