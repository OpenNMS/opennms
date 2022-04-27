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

package org.opennms.features.config.service.impl.callback;

import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;

import java.util.function.Consumer;

/**
 * It is default update notifier for AbstractCmJaxbConfigDao.
 *`
 * @param <E> entity class
 */
public class DefaultAbstractCmJaxbConfigDaoUpdateCallback<E> implements Consumer<ConfigUpdateInfo> {
    private AbstractCmJaxbConfigDao<E> abstractCmJaxbConfigDao;

    public DefaultAbstractCmJaxbConfigDaoUpdateCallback(AbstractCmJaxbConfigDao<E> abstractCmJaxbConfigDao) {
        this.abstractCmJaxbConfigDao = abstractCmJaxbConfigDao;
    }

    @Override
    public void accept(ConfigUpdateInfo configUpdateInfo) {
        // trigger to reload, which will replace the entity in lastKnownEntityMap
        abstractCmJaxbConfigDao.loadConfig(configUpdateInfo.getConfigId());
    }
}