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
package org.opennms.features.topology.plugins.topo.asset.layers.decorator;

import org.opennms.features.graphml.model.GraphMLNode;
import org.opennms.features.topology.plugins.topo.asset.layers.NodeDecorator;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLProperties;
import org.opennms.netmgt.model.OnmsNode;

/**
 * Decorator to decorate {@link OnmsNode}s.
 *
 * @author mvrueden
 */
public class NodeItemNodeDecorator implements NodeDecorator<OnmsNode> {

    @Override
    public void decorate(GraphMLNode graphMLNode, OnmsNode onmsNode) {
        if (onmsNode.getNodeId() != null) {
            graphMLNode.setProperty(GraphMLProperties.NODE_ID, onmsNode.getId());
        }
        if (onmsNode.getLabel() != null) {
            graphMLNode.setProperty(GraphMLProperties.LABEL, onmsNode.getLabel());
        }
        if (onmsNode.getForeignId() != null) {
            graphMLNode.setProperty(GraphMLProperties.FOREIGN_ID, onmsNode.getForeignId());
        }
        if (onmsNode.getForeignSource() != null) {
            graphMLNode.setProperty(GraphMLProperties.FOREIGN_SOURCE, onmsNode.getForeignSource());
        }
    }

    @Override
    public String getId(OnmsNode node) {
        return "nodes:" + node.getNodeId() + ":" + node.getLabel();
    }
}
