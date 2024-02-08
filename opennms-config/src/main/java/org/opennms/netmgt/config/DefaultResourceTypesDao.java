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
package org.opennms.netmgt.config;

import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.config.api.ConfigReloadContainer;
import org.opennms.core.xml.AbstractMergingJaxbConfigDao;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.ResourceTypes;

public class DefaultResourceTypesDao extends AbstractMergingJaxbConfigDao<ResourceTypes, ResourceTypes> implements ResourceTypesDao {

    private ConfigReloadContainer<ResourceTypes> m_extContainer;

    public DefaultResourceTypesDao() {
        super(ResourceTypes.class, "Resource Type Definitions",
                Paths.get("etc", "resource-types.d"));
        initExtensions();
    }

    @Override
    public Map<String, ResourceType> getResourceTypes() {
        final Map<String, ResourceType> resourceTypesByName = new HashMap<>();
        resourceTypesByName.putAll(DataCollectionConfigFactory.getInstance().getConfiguredResourceTypes());
        final ResourceTypes configuredResourceTypes = getObject();
        if (configuredResourceTypes != null) {
            configuredResourceTypes.getResourceTypes().stream().forEach(r -> resourceTypesByName.put(r.getName(), r));
        }
        final ResourceTypes resourceTypesFromContainer = m_extContainer.getObject();
        if (resourceTypesFromContainer != null) {
            resourceTypesFromContainer.getResourceTypes().stream().forEach(r -> resourceTypesByName.put(r.getName(), r));
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

    private void initExtensions() {
        m_extContainer = new ConfigReloadContainer.Builder<>(ResourceTypes.class)
                .withFolder((accumulator, next) -> accumulator.getResourceTypes().addAll(next.getResourceTypes()))
                .build();
    }
}
