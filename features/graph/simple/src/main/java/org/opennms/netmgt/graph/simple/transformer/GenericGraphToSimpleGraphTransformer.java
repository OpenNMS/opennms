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

package org.opennms.netmgt.graph.simple.transformer;

import java.util.function.Function;

import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.info.NodeInfo;
import org.opennms.netmgt.graph.simple.SimpleEdge;
import org.opennms.netmgt.graph.simple.SimpleGraph;
import org.opennms.netmgt.graph.simple.SimpleVertex;

public class GenericGraphToSimpleGraphTransformer implements Function<GenericGraph, SimpleGraph> {

    @Override
    public SimpleGraph apply(GenericGraph genericGraph) {
            final SimpleGraph simpleGraph = new SimpleGraph(genericGraph.getNamespace(), SimpleVertex.class);
            simpleGraph.setLabel(genericGraph.getLabel());
            simpleGraph.setDescription(genericGraph.getDescription());

            genericGraph.getVertices().forEach(genericVertex -> {
                try {
                    // TODO MVR we should use the factory instead
//                  final SimpleVertex eachSimpleVertex = vertexFactory.createVertex(SimpleVertex.class, simpleGraph.getNamespace(), genericVertex.getId());
                    final SimpleVertex eachSimpleVertex = new SimpleVertex(simpleGraph.getNamespace(), genericVertex.getId());
                    eachSimpleVertex.setLabel(genericVertex.getProperty(GenericProperties.LABEL));
                    eachSimpleVertex.setNodeRefString(genericVertex.getProperty(GenericProperties.NODE_REF));
                    eachSimpleVertex.setNodeInfo((NodeInfo) genericVertex.getComputedProperties().get("node"));
                    simpleGraph.addVertex(eachSimpleVertex);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

            genericGraph.getEdges().forEach(genericEdge -> {
                final SimpleVertex sourceVertex = simpleGraph.getVertex(genericEdge.getSource().getId());
                final SimpleVertex targetVertex = simpleGraph.getVertex(genericEdge.getTarget().getId());
                final SimpleEdge eachSimpleEdge = new SimpleEdge(sourceVertex, targetVertex);
                simpleGraph.addEdge(eachSimpleEdge);
            });
            return simpleGraph;
        }
}
