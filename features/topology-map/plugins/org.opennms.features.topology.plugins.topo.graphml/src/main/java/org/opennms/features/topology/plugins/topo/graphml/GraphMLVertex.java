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

import java.util.HashMap;

import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.plugins.topo.graphml.model.GraphMLNode;

public class GraphMLVertex extends AbstractVertex {
    private HashMap<String, Object> properties = new HashMap<>();

    protected GraphMLVertex(GraphMLNode graphMLNode) {
        super(graphMLNode.getProperty(GraphMLProperties.NAMESPACE),
                graphMLNode.getProperty(GraphMLProperties.ID),
                graphMLNode.getProperty(GraphMLProperties.LABEL));

        setIconKey(graphMLNode.getProperty(GraphMLProperties.ICON_KEY));
        setIpAddress(graphMLNode.getProperty(GraphMLProperties.IP_ADDRESS));
        setLocked(Boolean.valueOf(graphMLNode.getProperty(GraphMLProperties.LOCKED)));
        setSelected(Boolean.valueOf(graphMLNode.getProperty(GraphMLProperties.SELECTED)));
        setStyleName(graphMLNode.getProperty(GraphMLProperties.STYLE_NAME));
        setTooltipText(graphMLNode.getProperty(GraphMLProperties.TOOLTIP_TEXT));
        if (graphMLNode.getProperty(GraphMLProperties.NODE_ID) != null) {
            setNodeID(graphMLNode.getProperty(GraphMLProperties.NODE_ID));
        }

        setProperties(graphMLNode.getProperties());
    }

    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    public HashMap<String, Object> getProperties() {
        return properties;
    }
}
