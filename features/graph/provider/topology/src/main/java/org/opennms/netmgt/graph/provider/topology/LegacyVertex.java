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
package org.opennms.netmgt.graph.provider.topology;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.LevelAware;
import org.opennms.integration.api.v1.graph.Properties;
import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.NodeInfo;

public class LegacyVertex extends AbstractVertex implements LevelAware {
    private final int level;
    private final Map<String, Object> properties;

    protected LegacyVertex(GenericVertex genericVertex) {
        super(Objects.requireNonNull(genericVertex).getNamespace(), genericVertex.getId(), genericVertex.getProperty(GenericProperties.LABEL, genericVertex.getId()));
        setIconKey(genericVertex.getProperty("iconKey"));
        setIpAddress(genericVertex.getProperty("ipAddr"));
        setLocked(Boolean.valueOf(genericVertex.getProperty("locked")));
        setSelected(Boolean.valueOf(genericVertex.getProperty("selected")));
        setStyleName(genericVertex.getProperty("styleName"));
        String tooltip = genericVertex.getProperty(Properties.Vertex.TOOLTIP_TEXT, genericVertex.getLabel());
        setTooltipText(tooltip);
        if (genericVertex.getProperty("edge-path-offset") != null) {
            setEdgePathOffset(genericVertex.getProperty("edge-path-offset"));
        }
        // We have 3 ways to determine the nodeId, lets try all - last one wins.
        // nodeInfo is produced by the NodeEnrichment
        Optional.ofNullable(genericVertex.getProperty(GenericProperties.NODE_INFO))
                .map(o -> (NodeInfo)o)
                .map(NodeInfo::getId)
                .ifPresent(this::setNodeID);
        if (genericVertex.getProperty(GenericProperties.NODE_ID) != null) {
            setNodeID(genericVertex.getProperty(GenericProperties.NODE_ID));
        }
        Optional.ofNullable(genericVertex.getNodeRef())
                .map(NodeRef::getNodeId)
                .ifPresent(this::setNodeID);
        level = genericVertex.getProperty("level", 0);
        properties = genericVertex.getProperties();
    }

    @Override
    public int getLevel() {
        return level;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
