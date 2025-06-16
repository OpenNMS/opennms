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
package org.opennms.netmgt.collection.support;

import java.util.List;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.Parameter;
import org.opennms.netmgt.collection.api.StorageStrategy;
import org.opennms.netmgt.collection.api.StorageStrategyService;
import org.opennms.netmgt.model.ResourcePath;

public class IndexStorageStrategy implements StorageStrategy {

    private String m_resourceTypeName;
    protected StorageStrategyService m_storageStrategyService;

    /** {@inheritDoc} */
    @Override
    public final ResourcePath getRelativePathForAttribute(ResourcePath resourceParent, String instance) {
        return ResourcePath.get(resourceParent, m_resourceTypeName, instance);
    }

    /** {@inheritDoc} */
    @Override
    public final void setResourceTypeName(String name) {
        m_resourceTypeName = name;
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public final String getResourceTypeName() {
        return m_resourceTypeName;
    }

    /** {@inheritDoc} */
    @Override
    public String getResourceNameFromIndex(CollectionResource resource) {
        // Use the instance value as the name of the resource
        return resource.getInstance();
    }

    /** {@inheritDoc} */
    @Override
    public final void setStorageStrategyService(StorageStrategyService agent) {
        m_storageStrategyService = agent;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(List<Parameter> parameterCollection) throws IllegalArgumentException {
        // Empty method, this strategy takes no parameters
    }
}
