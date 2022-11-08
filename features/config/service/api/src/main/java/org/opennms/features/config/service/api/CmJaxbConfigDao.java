/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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