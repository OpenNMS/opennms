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

import org.opennms.features.topology.api.support.FocusStrategy;

public interface Layer<T> {
    // The id of the layer
    String getId();

    // the namespace of the layer
    String getNamespace();

    // The label of the layer
    String getLabel();

    // The description of the layer
    String getDescription();

    // Enable/disable VertexStatusProvider
    boolean hasVertexStatusProvider();

    // The SZL
    int getSemanticZoomLevel();

    // The focus strategy
    FocusStrategy getFocusStrategy();

    // Decorator to build the node for this layer
    NodeDecorator<T> getNodeDecorator();

    // The item provider to build nodes from
    ItemProvider<T> getItemProvider();

    // The generator fo
    IdGenerator getIdGenerator();
}
