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
package org.opennms.netmgt.dao.support;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.core.collections.LazyList;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

import com.google.common.base.Preconditions;

public class LazyChildResourceLoader implements LazyList.Loader<OnmsResource> {
    private final ResourceDao m_resourceDao;
    private OnmsResource m_parent;

    public LazyChildResourceLoader(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    public void setParent(OnmsResource parent) {
        m_parent = parent;
    }

    @Override
    public List<OnmsResource> load() {
        Preconditions.checkNotNull(m_parent, "parent attribute");
        // Gather the lists of children from all the available resource types and merge them
        // into a single list
        List<OnmsResource> children = getAvailableResourceTypes().stream()
                .map(t -> t.getResourcesForParent(m_parent))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // Set the parent field on all of the resources
        children.stream().forEach(c -> c.setParent(m_parent));
        return children;
    }

    private Collection<OnmsResourceType> getAvailableResourceTypes() {
        return m_resourceDao.getResourceTypes().stream()
                .filter(t -> t.isResourceTypeOnParent(m_parent))
                .collect(Collectors.toList());
    }
}
