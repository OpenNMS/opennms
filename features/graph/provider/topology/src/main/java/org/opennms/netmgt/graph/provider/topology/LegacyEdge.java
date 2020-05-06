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

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.integration.api.v1.graph.Properties;
import org.opennms.netmgt.graph.api.generic.GenericEdge;

public class LegacyEdge extends AbstractEdge {

    private final Map<String, Object> properties;

    public LegacyEdge(GenericEdge edge) {
        super(Objects.requireNonNull(edge).getNamespace(), edge.getId(), createVertexRef(edge.getSource()), createVertexRef(edge.getTarget()));
        this.properties = edge.getProperties();
        String tooltip = edge.getProperty(Properties.Edge.TOOLTIP_TEXT, edge.getLabel());
        setTooltipText(tooltip);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    private static VertexRef createVertexRef(org.opennms.netmgt.graph.api.VertexRef input) {
        Objects.requireNonNull(input);
        final VertexRef output = new DefaultVertexRef(input.getNamespace(), input.getId());
        return output;
    }
}
