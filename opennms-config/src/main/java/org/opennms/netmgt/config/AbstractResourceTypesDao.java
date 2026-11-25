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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.config.api.ConfigReloadContainer;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.ResourceTypes;

/**
 * Merges the resource types of all sources into one view. When a name is defined by more than one source, the later
 * source wins:
 * <ol>
 *     <li>the resource types of the SNMP data collection configuration</li>
 *     <li>the stored resource types, see {@link #getStoredResourceTypes()}</li>
 *     <li>the resource types provided by OIA extensions</li>
 * </ol>
 */
public abstract class AbstractResourceTypesDao implements ResourceTypesDao {

    /**
     * Key under which core publishes the stored resource types to the JSON store for Sentinel, in context
     * {@code ConfigDaoConstants.JSON_KEY_STORE_CONTEXT}.
     */
    public static final String JSON_STORE_KEY = "resource-types";

    private final ConfigReloadContainer<ResourceTypes> extContainer = new ConfigReloadContainer.Builder<>(ResourceTypes.class)
            .withFolder((accumulator, next) -> accumulator.getResourceTypes().addAll(next.getResourceTypes()))
            .build();

    /**
     * @return the resource types that used to be read from {@code etc/resource-types.d/}, in merge order
     */
    protected abstract List<ResourceType> getStoredResourceTypes();

    /**
     * @return when the stored resource types changed last, in milliseconds
     */
    protected abstract long getStoredLastUpdate();

    @Override
    public Map<String, ResourceType> getResourceTypes() {
        final Map<String, ResourceType> resourceTypesByName = new HashMap<>(DataCollectionConfigFactory.getInstance().getConfiguredResourceTypes());
        getStoredResourceTypes().forEach(r -> resourceTypesByName.put(r.getName(), r));
        final ResourceTypes resourceTypesFromExtensions = extContainer.getObject();
        if (resourceTypesFromExtensions != null) {
            resourceTypesFromExtensions.getResourceTypes().forEach(r -> resourceTypesByName.put(r.getName(), r));
        }
        return resourceTypesByName;
    }

    @Override
    public ResourceType getResourceTypeByName(final String name) {
        return getResourceTypes().get(name);
    }

    /**
     * @return the newer of the last update of the data collection configuration and of the stored resource types
     */
    @Override
    public Date getLastUpdate() {
        final Date lastUpdateOfDcConfig = DataCollectionConfigFactory.getInstance().getLastUpdate();
        return new Date(Math.max(lastUpdateOfDcConfig.getTime(), getStoredLastUpdate()));
    }
}
