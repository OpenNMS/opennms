/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.config.service.util;

import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.features.config.service.api.ConfigUpdateInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * It is default update notifier for AbstractCmJaxbConfigDao.
 *
 * @param <ENTITY_CLASS>
 */
public class DefaultUpdateNotifier<ENTITY_CLASS> implements Consumer<ConfigUpdateInfo> {
    private AbstractCmJaxbConfigDao<ENTITY_CLASS> abstractCmJaxbConfigDao;

    public DefaultUpdateNotifier(AbstractCmJaxbConfigDao<ENTITY_CLASS> abstractCmJaxbConfigDao) {
        this.abstractCmJaxbConfigDao = abstractCmJaxbConfigDao;
    }

    @Override
    public void accept(ConfigUpdateInfo configUpdateInfo) {
        // trigger to reload, which will replace the entity in lastKnownEntityMap
        abstractCmJaxbConfigDao.loadConfig(configUpdateInfo.getConfigId());
    }
}