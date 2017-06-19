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

package org.opennms.features.topology.plugins.topo.asset.layers;

import org.opennms.netmgt.model.OnmsNode;

/**
 * The ItemProvider is used to extract an element for the given {@link OnmsNode} in order to create {@link org.opennms.features.graphml.model.GraphMLNode}s afterwards.
 * Each {@link Layer} must define an {@link ItemProvider}.
 *
 * Examples:
 * Very simple provider:
 *  <code>
 *      new ItemProvider<OnmsNode>() {
 *          public OnmsNode getItem(OnmsNode node) {
 *              return node;
 *          }
 *      }
 *  </code>
 *
 * Provider to extract asset information:
 *  <code>
 *      new ItemProvider<String>() {
 *          public String getItem(OnmsNode node) {
 *              return node.getAssetRecord().getBuilding();
 *          }
 *      }
 *
 *  </code>
 *
 * @param <T> The type of the value which is extracted from the given {@link OnmsNode}
 * @author mvrueden
 */
public interface ItemProvider<T> {

    /**
     * Returns the item for the given <code>node</code> which is used to create {@link org.opennms.features.graphml.model.GraphMLNode}s afterwards.
     *
     * If {@link Restriction} annotations were set correctly to each {@link Layers}
     * it is not necessary to check for null values and it should be impossible that this method returns null.
     *
     * @param node The node to extract the value from.
     * @return The value extracted from the node. It is very unlikely to be null, but may be null (see above).
     */
    T getItem(OnmsNode node);
}
