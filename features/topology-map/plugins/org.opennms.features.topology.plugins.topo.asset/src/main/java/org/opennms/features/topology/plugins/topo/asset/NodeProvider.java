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
