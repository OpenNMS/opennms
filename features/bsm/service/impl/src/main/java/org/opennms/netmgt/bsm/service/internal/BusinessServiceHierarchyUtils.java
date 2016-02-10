/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.service.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.service.model.BusinessService;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class BusinessServiceHierarchyUtils {

    /**
     * Sets the level information on the Business Service Hierarchy.
     * @param rootServices All known root elements
     */
    static void updateHierarchyLevel(Collection<BusinessService> rootServices) {
        updateHierarchyLevel(0, rootServices);
    }

    private static void updateHierarchyLevel(int level, Collection<BusinessService> elements) {
        elements.forEach(bs -> {
            // elements can be children of multiple parents, we use the maximum level
            bs.setLevel(Math.max(bs.getLevel(), level));
            // Afterwards move to next level
            updateHierarchyLevel(level + 1, bs.getChildServices());
        });
    }

    public static Set<BusinessService> getRoots(Collection<BusinessService> businessServices) {
        final Map<BusinessService, HashSet<BusinessService>> childParentMapping = getChildParentMapping(businessServices);
        return businessServices
                .stream()
                // no child -> parent mapping defined, means this is a root candidate
                .filter(eachService -> childParentMapping.get(eachService) == null || childParentMapping.get(eachService).isEmpty())
                .collect(Collectors.toSet());
    }

    private static Map<BusinessService, HashSet<BusinessService>> getChildParentMapping(Collection<BusinessService> businessServices) {
        final Map<BusinessService, HashSet<BusinessService>> childParentMapping = Maps.newHashMap();
        businessServices.forEach(bs -> bs.getChildEdges()
                .forEach(edge -> {
                    if (childParentMapping.get(edge.getChild()) == null) {
                        childParentMapping.put(edge.getChild(), Sets.newHashSet());
                    }
                    childParentMapping.get(edge.getChild()).add(edge.getSource());
                })
        );
        return childParentMapping;
    }
}
