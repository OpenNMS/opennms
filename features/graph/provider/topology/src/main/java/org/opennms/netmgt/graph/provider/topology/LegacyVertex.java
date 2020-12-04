/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
