/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
