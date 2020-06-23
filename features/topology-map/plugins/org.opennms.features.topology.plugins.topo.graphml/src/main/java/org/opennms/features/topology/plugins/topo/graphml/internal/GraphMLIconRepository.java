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

package org.opennms.features.topology.plugins.topo.graphml.internal;

import java.util.Set;

import org.opennms.features.topology.api.IconRepository;

import com.google.common.collect.Sets;

/**
 * Simple {@link IconRepository} for the {@link org.opennms.features.topology.plugins.topo.graphml.GraphMLMetaTopologyProvider}.
 * It should contain the Set of iconIds defined by all {@link org.opennms.features.topology.plugins.topo.graphml.GraphMLTopologyProvider}s.
 * This enables users to define custom icons (or using already existing icons) by simply defining them in the GraphML file itself.
 *
 * Please note that multiple {@link org.opennms.features.topology.plugins.topo.graphml.GraphMLMetaTopologyProvider}
 * could define identical {@link GraphMLIconRepository}s. For now this is not relevant. However if the icons
 * should be configurable from the Icon Selection Dialog in the UI, this issue must be addressed.
 *
 * @author mvrueden
 */
public class GraphMLIconRepository implements IconRepository {

    private Set<String> knownIconKeys = Sets.newHashSet();

    public GraphMLIconRepository(Set<String> knownIconKeys) {
        this.knownIconKeys = knownIconKeys;
    }

    @Override
    public boolean contains(String iconKey) {
        return knownIconKeys.contains(iconKey);
    }

    @Override
    public String getSVGIconId(String iconKey) {
        return iconKey;
    }
}
