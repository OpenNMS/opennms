/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

import org.opennms.core.xml.AbstractMergingJaxbConfigDao;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.ResourceTypes;

public class DefaultResourceTypesDao extends AbstractMergingJaxbConfigDao<ResourceTypes, ResourceTypes> implements ResourceTypesDao {

    public DefaultResourceTypesDao() {
        super(ResourceTypes.class, "Resource Type Definitions",
                Paths.get("etc", "resource-types.d"));
    }

    @Override
    public Map<String, ResourceType> getResourceTypes() {
        final Map<String, ResourceType> resourceTypesByName = new HashMap<>();
        resourceTypesByName.putAll(DataCollectionConfigFactory.getInstance().getConfiguredResourceTypes());
        final ResourceTypes configuredResourceTypes = getObject();
        if (configuredResourceTypes != null) {
            configuredResourceTypes.getResourceTypes().stream().forEach(r -> resourceTypesByName.put(r.getName(), r));
        }
        return resourceTypesByName;
    }

    @Override
    public ResourceType getResourceTypeByName(String name) {
        return getResourceTypes().get(name);
    }

    @Override
    public ResourceTypes translateConfig(ResourceTypes config) {
        return config;
    }

    @Override
    public ResourceTypes mergeConfigs(ResourceTypes source, ResourceTypes target) {
        if (target == null) {
            target = new ResourceTypes();
        }
        if (source != null) {
            target.getResourceTypes().addAll(source.getResourceTypes());
        }
        return target;
    }

    @Override
    public Date getLastUpdate() {
        final Date lastUpdateOfDcConfig = DataCollectionConfigFactory.getInstance().getLastUpdate();
        final Date lastUpdateOfResourceTypes = super.getLastUpdate();

        if (lastUpdateOfDcConfig.after(lastUpdateOfResourceTypes)) {
            // The data-collection configuration has been updated more recently
            // than the resource types configuration, so use that date instead
            return lastUpdateOfDcConfig;
        } else {
            return lastUpdateOfResourceTypes;
        }
    }
}
