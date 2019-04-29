/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.application;

import java.util.Map;
import java.util.Optional;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.graph.provider.application.ApplicationVertex;
import org.opennms.netmgt.graph.simple.SimpleEdge;

public class DomainToGuiConverter {

    public static AbstractEdge convert(SimpleEdge domainEdge, Map<org.opennms.netmgt.graph.api.VertexRef, ApplicationVertex> allKnownVertices) {
        final String id = String.format("connection:%s:%s", domainEdge.getSource().getId(), domainEdge.getTarget().getId());
        VertexRef guiSourceVertexRef = convert(domainEdge.getSource(), allKnownVertices);
        VertexRef guiTargetVertexRef = convert(domainEdge.getSource(), allKnownVertices);
        return new AbstractEdge(domainEdge.getNamespace(), id, guiSourceVertexRef, guiTargetVertexRef);
    }

    public static VertexRef convert(org.opennms.netmgt.graph.api.VertexRef vertexRef, Map<org.opennms.netmgt.graph.api.VertexRef, ApplicationVertex> allKnownVertices) {
        String label = Optional.ofNullable(allKnownVertices.get(vertexRef))
                .map(ApplicationVertex::getName)
                .orElse(vertexRef.getNamespace() + ":" + vertexRef.getId());
        return new DefaultVertexRef(vertexRef.getNamespace(), vertexRef.getId(), label);
    }
}
