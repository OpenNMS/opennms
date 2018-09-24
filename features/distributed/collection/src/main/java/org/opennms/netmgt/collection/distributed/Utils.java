/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.distributed;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.opennms.netmgt.collection.api.ResourceTypeMapper;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.api.ResourceTypesDao;

public class Utils {

    public static Path getSentinelHome() {
        return Paths.get(System.getProperty("karaf.home", "/opt/sentinel"));
    }

    public static ResourceTypeMapper createReseourceTypeMapper(ResourceTypesDao resourceTypesDao) {
        Objects.requireNonNull(resourceTypesDao);
        ResourceTypeMapper.getInstance().setResourceTypeMapper(
                (type) -> resourceTypesDao.getResourceTypeByName(type));
        return ResourceTypeMapper.getInstance();
    }

    public static DataCollectionConfigFactory createDataCollectionConfigFactory(DataCollectionConfigDao configDao) {
        DataCollectionConfigFactory.setInstance(configDao);
        return new DataCollectionConfigFactory() {};
    }
}
