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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.EdgeEntity;
import org.opennms.netmgt.graph.FocusEntity;
import org.opennms.netmgt.graph.GraphContainerEntity;
import org.opennms.netmgt.graph.GraphEntity;
import org.opennms.netmgt.graph.PropertyEntity;
import org.opennms.netmgt.graph.VertexEntity;
import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.focus.FocusStrategy;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.persistence.converter.ConverterService;
import org.slf4j.LoggerFactory;

public class GenericToEntityMapper {

    private ConverterService converterService = new ConverterService();

    public GraphContainerEntity toEntity(GenericGraphContainer genericGraphContainer) {
        final GraphContainerEntity graphContainerEntity = new GraphContainerEntity();
        graphContainerEntity.setNamespace(genericGraphContainer.getId());
        graphContainerEntity.setProperties(convertToPropertyEntities(genericGraphContainer.getProperties()));
        final List<GraphEntity> graphEntities = genericGraphContainer.getGraphs().stream().map(genericGraph -> toEntity(genericGraph)).collect(Collectors.toList());
        graphContainerEntity.setGraphs(graphEntities);
        return graphContainerEntity;
    }

    public GraphEntity toEntity(GenericGraph genericGraph) {
        final GraphEntity graphEntity = new GraphEntity();
        graphEntity.setNamespace(genericGraph.getNamespace());
        graphEntity.setProperties(convertToPropertyEntities(genericGraph.getProperties()));

        // Map Vertices
        final List<VertexEntity> vertexEntities = genericGraph.getVertices().stream().map(genericVertex -> toEntity(genericVertex)).collect(Collectors.toList());
        graphEntity.addRelations(vertexEntities);

        // Map Edges
        final List<EdgeEntity> edgeEntities = genericGraph.getEdges().stream().map(genericEdge -> toEntity(genericEdge, graphEntity)).collect(Collectors.toList());
        graphEntity.addRelations(edgeEntities);

        // Map Focus
        final Focus focus = genericGraph.getDefaultFocus();
        graphEntity.setDefaultFocus(toEntity(focus));

        return graphEntity;
    }

    public List<PropertyEntity> convertToPropertyEntities(Map<String, Object> properties) {
        Objects.requireNonNull(properties);
        final List<PropertyEntity> propertyEntities = new ArrayList<>();
        for(Map.Entry<String, Object> property : properties.entrySet()) {
            final Object value = property.getValue();
            if (value != null) {
                final String propertyName = property.getKey();
                final Class<?> propertyType = value.getClass();
                try {
                    final String stringRepresentation = converterService.toStringRepresentation(propertyType, value);
                    final PropertyEntity propertyEntity = new PropertyEntity();
                    propertyEntity.setType(propertyType);
                    propertyEntity.setName(propertyName);
                    propertyEntity.setValue(stringRepresentation);
                    propertyEntities.add(propertyEntity);
                } catch (IllegalStateException ex) {
                    LoggerFactory.getLogger(getClass()).warn("Property '{}' of type '{}' cannot be converted. Skipping property '{}'.", propertyName, propertyType, propertyName);
                }
            }
        }
        return propertyEntities;
    }

    public EdgeEntity toEntity(GenericEdge genericEdge, GraphEntity graphEntity) {
        final EdgeEntity edgeEntity = new EdgeEntity();
        final List<PropertyEntity> edgeProperties = convertToPropertyEntities(genericEdge.getProperties());
        edgeEntity.setProperties(edgeProperties);
        edgeEntity.setSource(genericEdge.getSource().getNamespace(), genericEdge.getSource().getId());
        edgeEntity.setTarget(genericEdge.getTarget().getNamespace(), genericEdge.getTarget().getId());
        edgeEntity.setNamespace(genericEdge.getNamespace());
        return edgeEntity;
    }

    public VertexEntity toEntity(GenericVertex genericVertex) {
        final VertexEntity vertexEntity = new VertexEntity();
        final List<PropertyEntity> vertexProperties = convertToPropertyEntities(genericVertex.getProperties());
        vertexEntity.setNamespace(genericVertex.getNamespace());
        vertexEntity.setProperties(vertexProperties);
        return vertexEntity;
    }

    public FocusEntity toEntity(Focus focus) {
        if (focus != null) {
            final FocusEntity focusEntity = new FocusEntity();
            focusEntity.setType(focus.getId());
            if (focus.getId().toLowerCase().equalsIgnoreCase(FocusStrategy.SELECTION)) {
               focusEntity.setSelection(focus.getVertexIds());
            }
            return focusEntity;
        }
        return null;
    }
}
