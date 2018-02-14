/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
import java.util.Map;

import org.opennms.features.topology.api.topo.AbstractEdge;

import com.google.common.collect.Maps;

public class GraphMLEdge extends AbstractEdge {
    private Map<String, Object> properties = Maps.newHashMap();

    public GraphMLEdge(String namespace, org.opennms.features.graphml.model.GraphMLEdge graphMLEdge, GraphMLVertex source, GraphMLVertex target) {
        super(namespace, graphMLEdge.getId(), source, target);

        setTooltipText(graphMLEdge.getProperty(GraphMLProperties.TOOLTIP_TEXT));
        setProperties(graphMLEdge.getProperties());
    }

    /**
     * Clone constructor.
     * It is required because each edge (whatever type) is cloned in the UI.
     * The resulting object is of type AbstractEdge.
     * This may be okay for edges which have the same fields. However, if a certain implementation needs Edge
     * specific properties (e.g. a VertexStatusProviderType) there is no way to retrieve those.
     * In order to make them accessible (without knowing the actual implementation), the clone constructor is used.
     *
     * @param edgeToClone The edge to clone
     */
    private GraphMLEdge(GraphMLEdge edgeToClone) {
        super(edgeToClone);
        properties = new HashMap<>(edgeToClone.getProperties());
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public AbstractEdge clone() {
        return new GraphMLEdge(this);
    }
}
