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
