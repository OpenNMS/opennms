/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.asset;

import java.util.List;

import org.opennms.features.topology.plugins.topo.asset.layers.LayerDefinition;
import org.opennms.netmgt.model.OnmsNode;

/**
 * Provides {@link OnmsNode}s which the Asset Topology is build from.
 *
 * @author mvrueden
 */
public interface NodeProvider {
    /**
     * Returns all nodes for which the given mapping applies.
     *
     * The returned list SHOULD NOT contain any nodes, where any value from {@link org.opennms.features.topology.plugins.topo.asset.layers.ItemProvider#getItem(OnmsNode)} would return null.
     * If the returned list contains those nodes anyways, they will be filtered out later.
     * This is considered a BAD PRACTISE and should only be used if absolutely necessary (e.g. tests)
     *
     * @param definitions
     * @return all nodes for which the given mapping applies.
     */
    List<OnmsNode> getNodes(List<LayerDefinition> definitions);
}
