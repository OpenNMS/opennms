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
package org.opennms.netmgt.graph.persistence.mapper;

import org.opennms.netmgt.graph.FocusEntity;
import org.opennms.netmgt.graph.GraphContainerEntity;
import org.opennms.netmgt.graph.GraphEntity;
import org.opennms.netmgt.graph.PropertyEntity;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.focus.FocusStrategy;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericEdge.GenericEdgeBuilder;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraph.GenericGraphBuilder;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer.GenericGraphContainerBuilder;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.generic.GenericVertex.GenericVertexBuilder;
import org.opennms.netmgt.graph.persistence.converter.ConverterService;

public class EntityToGenericMapper {

    private ConverterService converterService = new ConverterService();

    public GenericGraphContainer fromEntity(GraphContainerEntity entity) {
        final GenericGraphContainerBuilder genericGraphContainerBuilder = GenericGraphContainer.builder();
        entity.getProperties().forEach(property -> { // will set id and namespace
            final Object value = convert(property);
            genericGraphContainerBuilder.property(property.getName(), value);
        });
        entity.getGraphs().forEach(graphEntity -> {
            GenericGraph genericGraph = fromEntity(graphEntity);
            genericGraphContainerBuilder.addGraph(genericGraph);
        });
        return genericGraphContainerBuilder.build();
    }

    public GenericGraph fromEntity(final GraphEntity graphEntity) {
        final GenericGraphBuilder genericGraphBuilder = GenericGraph.builder().namespace(graphEntity.getNamespace());
        graphEntity.getProperties().forEach(property -> { // will set id and namespace
            final Object value = convert(property);
            genericGraphBuilder.property(property.getName(), value);
        });
        
        graphEntity.getVertices().stream().forEach(vertexEntity -> {
            final GenericVertexBuilder genericVertex = GenericVertex.builder()
            		.namespace(graphEntity.getNamespace())
                    .id(vertexEntity.getProperty(GenericProperties.ID).getValue());
            vertexEntity.getProperties().forEach(property -> {  // will set id and namespace
                final Object value = convert(property);
                genericVertex.property(property.getName(), value);
            });
            genericGraphBuilder.addVertex(genericVertex.build());
        });

        graphEntity.getEdges().stream().forEach(edgeEntity -> {
            final GenericEdgeBuilder genericEdge = GenericEdge.builder()
                    .namespace(edgeEntity.getNamespace())
                    .source(new VertexRef(edgeEntity.getSource().getNamespace(), edgeEntity.getSource().getId()))
                    .target(new VertexRef(edgeEntity.getTarget().getNamespace(), edgeEntity.getTarget().getId()));
            edgeEntity.getProperties().stream()
                    .forEach(property -> {
                final Object value = convert(property);
                genericEdge.property(property.getName(), value);
            });
            genericGraphBuilder.addEdge(genericEdge.build());
        });

        final FocusEntity focusEntity = graphEntity.getDefaultFocus();
        if (focusEntity == null || focusEntity.getType().equalsIgnoreCase(FocusStrategy.EMPTY)) {
            genericGraphBuilder.focus().empty().apply();
        } else if(focusEntity.getType().equalsIgnoreCase(FocusStrategy.FIRST)) {
            genericGraphBuilder.focus().first().apply();
        } else if(focusEntity.getType().equalsIgnoreCase(FocusStrategy.ALL)) {
            genericGraphBuilder.focus().all().apply();
        } else if (focusEntity.getType().equalsIgnoreCase(FocusStrategy.SELECTION)) {
            genericGraphBuilder.focus().selection(genericGraphBuilder.getNamespace(), focusEntity.getVertexIds()).apply();
        } else {
            throw new IllegalStateException("The focus strategy '" + focusEntity.getType() + "' read from persistence is not known.");
        }
        return genericGraphBuilder.build();
    }

    public Object convert(final PropertyEntity propertyEntity) {
        final Object value = converterService.toValue(propertyEntity.getType(), propertyEntity.getValue());
        return value;
    }
}
