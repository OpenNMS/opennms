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
package org.opennms.features.topology.plugins.topo.graphml;

import java.util.Map;

import org.opennms.features.graphml.model.GraphMLNode;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.LevelAware;

public class GraphMLVertex extends AbstractVertex implements LevelAware {
    private final int level;
    private final Map<String, Object> properties;

    protected GraphMLVertex(String namespace, GraphMLNode graphMLNode) {
        super(namespace, graphMLNode.getId(), graphMLNode.getProperty(GraphMLProperties.LABEL, graphMLNode.getId()));

        setIconKey(graphMLNode.getProperty(GraphMLProperties.ICON_KEY));
        setIpAddress(graphMLNode.getProperty(GraphMLProperties.IP_ADDRESS));
        setLocked(Boolean.valueOf(graphMLNode.getProperty(GraphMLProperties.LOCKED)));
        setSelected(Boolean.valueOf(graphMLNode.getProperty(GraphMLProperties.SELECTED)));
        setStyleName(graphMLNode.getProperty(GraphMLProperties.STYLE_NAME));
        setTooltipText(graphMLNode.getProperty(GraphMLProperties.TOOLTIP_TEXT));
        if (graphMLNode.getProperty(GraphMLProperties.NODE_ID) != null) {
            setNodeID(graphMLNode.getProperty(GraphMLProperties.NODE_ID));
        }
        if (graphMLNode.getProperty(GraphMLProperties.EDGE_PATH_OFFSET) != null) {
            setEdgePathOffset(graphMLNode.getProperty(GraphMLProperties.EDGE_PATH_OFFSET));
        }

        level = graphMLNode.getProperty(GraphMLProperties.LEVEL, 0);
        properties = graphMLNode.getProperties();
    }

    @Override
    public int getLevel() {
        return level;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
