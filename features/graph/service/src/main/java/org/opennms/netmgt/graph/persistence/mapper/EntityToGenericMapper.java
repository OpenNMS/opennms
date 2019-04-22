/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.persistence.mapper;

import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.generic.GenericVertexRef;
import org.opennms.netmgt.graph.persistence.converter.ConverterService;
import org.opennms.netmgt.topology.GraphContainerEntity;
import org.opennms.netmgt.topology.GraphEntity;
import org.opennms.netmgt.topology.PropertyEntity;

public class EntityToGenericMapper {

    private ConverterService converterService = new ConverterService();

    public GenericGraphContainer fromEntity(GraphContainerEntity entity) {
        final GenericGraphContainer genericGraphContainer = new GenericGraphContainer();
        entity.getProperties().forEach(property -> { // will set id and namespace
            final Object value = convert(property);
            genericGraphContainer.setProperty(property.getName(), value);
        });
        entity.getGraphs().forEach(graphEntity -> {
            GenericGraph genericGraph = fromEntity(graphEntity);
            genericGraphContainer.addGraph(genericGraph);
        });
        return genericGraphContainer;
    }

    public GenericGraph fromEntity(final GraphEntity graphEntity) {
        final GenericGraph genericGraph = new GenericGraph(graphEntity.getNamespace());
        graphEntity.getProperties().forEach(property -> { // will set id and namespace
            final Object value = convert(property);
            genericGraph.setProperty(property.getName(), value);
        });

        graphEntity.getVertices().stream().forEach(vertexEntity -> {
            final GenericVertex genericVertex = new GenericVertex(graphEntity.getNamespace(), graphEntity.getProperty(GenericProperties.ID).getValue());
            vertexEntity.getProperties().forEach(property -> {  // will set id and namespace
                final Object value = convert(property);
                genericVertex.setProperty(property.getName(), value);
            });
            genericGraph.addVertex(genericVertex);
        });

        graphEntity.getEdges().stream().forEach(edgeEntity -> {
            final GenericEdge genericEdge = new GenericEdge(
                    edgeEntity.getNamespace(),
                    new GenericVertexRef(edgeEntity.getSource().getNamespace(), edgeEntity.getSource().getId()),
                    new GenericVertexRef(edgeEntity.getTarget().getNamespace(), edgeEntity.getTarget().getId()));
            edgeEntity.getProperties().forEach(property -> {
                final Object value = convert(property);
                genericEdge.setProperty(property.getName(), value);
            });
            genericGraph.addEdge(genericEdge);
        });

        return genericGraph;
    }

    private Object convert(final PropertyEntity propertyEntity) {
        final Object value = converterService.toValue(propertyEntity.getType(), propertyEntity.getValue());
        return value;
    }
}
