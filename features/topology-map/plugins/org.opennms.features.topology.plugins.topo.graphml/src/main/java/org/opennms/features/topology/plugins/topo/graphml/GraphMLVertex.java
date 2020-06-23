/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
