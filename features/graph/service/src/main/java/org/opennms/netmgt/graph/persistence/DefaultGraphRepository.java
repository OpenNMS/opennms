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

package org.opennms.netmgt.graph.persistence;

import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.graph.EdgeEntity;
import org.opennms.netmgt.graph.FocusEntity;
import org.opennms.netmgt.graph.GraphContainerEntity;
import org.opennms.netmgt.graph.GraphEntity;
import org.opennms.netmgt.graph.PropertyEntity;
import org.opennms.netmgt.graph.VertexEntity;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer.GenericGraphContainerBuilder;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.DefaultGraphContainerInfo;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.persistence.GraphRepository;
import org.opennms.netmgt.graph.dao.api.GraphContainerDao;
import org.opennms.netmgt.graph.persistence.mapper.EntityToGenericMapper;
import org.opennms.netmgt.graph.persistence.mapper.GenericToEntityMapper;
import org.opennms.netmgt.graph.api.updates.ContainerChangeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultGraphRepository implements GraphRepository {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultGraphRepository.class);

    private final EntityToGenericMapper entityToGenericMapper = new EntityToGenericMapper();

    private final GenericToEntityMapper genericToEntityMapper = new GenericToEntityMapper();

    private final GraphContainerDao graphContainerDao;
    private final SessionUtils sessionUtils;

    public DefaultGraphRepository(SessionUtils sessionUtils, GraphContainerDao graphContainerDao) {
        this.graphContainerDao = Objects.requireNonNull(graphContainerDao);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
    }

    @Override
    public GenericGraphContainer findContainerById(String containerId) {
        return sessionUtils.withTransaction(() -> {
            final GraphContainerEntity entity = graphContainerDao.findContainerById(containerId);
            if (entity != null) {
                return entityToGenericMapper.fromEntity(entity);
            }
            return null;
        });
    }

    @Override
    public GraphContainerInfo findContainerInfoById(String containerId) {
        return sessionUtils.withTransaction(() -> {
            // Fetch the whole container but only the relevant properties
            // Should avoid getVertices to not make it too slow
            final GraphContainerEntity containerEntity =  graphContainerDao.findContainerInfoById(containerId);
            if (containerEntity != null) {
                // Now convert
                final DefaultGraphContainerInfo containerInfo = new DefaultGraphContainerInfo(containerEntity.getNamespace());
                containerInfo.setLabel(containerEntity.getLabel());
                containerInfo.setDescription(containerEntity.getDescription());
                containerEntity.getGraphs().forEach(graphEntity -> {
                    // We don't know the vertex type anymore. When loading the container, the type of the vertex will be GenericVertex
                    // If another type is required, the loading instance should wrap the info accordingly
                    final DefaultGraphInfo graphInfo = new DefaultGraphInfo(graphEntity.getNamespace());
                    graphInfo.setLabel(graphEntity.getLabel());
                    graphInfo.setDescription(graphEntity.getDescription());
                    containerInfo.getGraphInfos().add(graphInfo);
                });
                return containerInfo;
            }
            return null;
        });
    }

    @Override
    public void deleteContainer(String containerId) {
        graphContainerDao.delete(containerId);
    }

    @Override
    public void save(final ImmutableGraphContainer graphContainer) {
        sessionUtils.withTransaction(() -> {
            final GenericGraphContainer persistedGraphContainer = findContainerById(graphContainer.getId());
            final GenericGraphContainer genericGraphContainer = graphContainer.asGenericGraphContainer();
            if (persistedGraphContainer == null) {
                LOG.debug("Graph Container (id: {}) is new. Persisting...", graphContainer.getId());
                final GraphContainerEntity graphContainerEntity = genericToEntityMapper.toEntity(genericGraphContainer);
                graphContainerDao.save(graphContainerEntity);
                LOG.debug("Graph Container (id: {}) persisted.", graphContainer.getId());
            } else {
                LOG.debug("Graph Container (id: {}) exists. Calculating change set...", graphContainer.getId());

                // The Changes are calculated on the Generic conversion of the input and persisted graph
                // In order to apply the changes here, they must again be converted to the actual implementation of the persisted graph (entity).
                ContainerChangeSet containerChangeSet = ContainerChangeSet.builder(persistedGraphContainer, genericGraphContainer).build();
                if (containerChangeSet.hasChanges()) {
                    final GraphContainerEntity graphContainerEntity = graphContainerDao.findContainerById(graphContainer.getId());

                    // Graph removal and addition is easy, simply remove or delete
                    containerChangeSet.getGraphsRemoved().forEach(genericGraph -> {
                        GraphEntity entity = graphContainerEntity.getGraph(genericGraph.getNamespace());
                        graphContainerEntity.getGraphs().remove(entity);
                    });
                    containerChangeSet.getGraphsAdded().forEach(genericGraph -> {
                        final GraphEntity newGraphEntity = genericToEntityMapper.toEntity((GenericGraph) genericGraph);
                        graphContainerEntity.getGraphs().add(newGraphEntity);
                    });

                    // Graph updates are more complex, as the changes were calculated on the generic version and now must be
                    // applied to the persistedEntity
                    containerChangeSet.getGraphsUpdated().forEach(changeSet -> {
                        final GraphEntity graphEntity = graphContainerEntity.getGraph(changeSet.getNamespace());

                        // Update Graph details
                        if (changeSet.getGraphInfo() != null) {
                            final GraphInfo graphInfo = changeSet.getGraphInfo();
                            graphEntity.setProperty(GenericProperties.NAMESPACE, String.class, graphInfo.getNamespace());
                            graphEntity.setProperty(GenericProperties.LABEL, String.class, graphInfo.getLabel());
                            graphEntity.setProperty(GenericProperties.DESCRIPTION, String.class, graphInfo.getDescription());
                        }

                        // Update Focus
                        if (changeSet.hasFocusChanged()) {
                            final Focus focus = changeSet.getFocus();
                            final FocusEntity focusEntity = graphEntity.getDefaultFocus();
                            focusEntity.setType(focus.getId());
                            focusEntity.setSelection(focus.getVertexIds());
                        }

                        // Update Edges
                        changeSet.getEdgesRemoved().forEach(edge -> {
                            final EdgeEntity edgeEntity = graphEntity.getEdgeByProperty(GenericProperties.ID, edge.getId());
                            graphEntity.removeEdge(edgeEntity);
                        });
                        changeSet.getEdgesAdded().forEach(edge -> {
                            final EdgeEntity edgeEntity = genericToEntityMapper.toEntity((GenericEdge) edge, graphEntity);
                            graphEntity.addEdge(edgeEntity);
                        });
                        changeSet.getEdgesUpdated().forEach(edge -> {
                            final EdgeEntity edgeEntity = graphEntity.getEdgeByProperty(GenericProperties.ID, edge.getId());
                            final List<PropertyEntity> propertyEntities = genericToEntityMapper.convertToPropertyEntities(((GenericEdge) edge).getProperties());
                            edgeEntity.mergeProperties(propertyEntities);
                        });

                        // Update Vertices
                        changeSet.getVerticesRemoved().forEach(vertex -> {
                            final VertexEntity vertexEntity = graphEntity.getVertexByProperty(GenericProperties.ID, vertex.getId());
                            graphEntity.removeVertex(vertexEntity);
                        });
                        changeSet.getVerticesAdded().forEach(vertex -> {
                            final VertexEntity vertexEntity = genericToEntityMapper.toEntity((GenericVertex) vertex);
                            graphEntity.addVertex(vertexEntity);
                        });
                        changeSet.getVerticesUpdated().forEach(vertex -> {
                            final VertexEntity vertexEntity = graphEntity.getVertexByProperty(GenericProperties.ID, vertex.getId());
                            final List<PropertyEntity> propertyEntities = genericToEntityMapper.convertToPropertyEntities(((GenericVertex) vertex).getProperties());
                            vertexEntity.mergeProperties(propertyEntities);
                        });
                    });
                    graphContainerDao.update(graphContainerEntity);
                }
                LOG.debug("Graph Container (id: {}) updated.", graphContainer.getId());
            }
            return null;
        });
    }

    @Override
    public void save(GraphContainerInfo containerInfo) {
        // We simply convert to a container and persist it
        final GenericGraphContainerBuilder genericGraphContainerBuilder = GenericGraphContainer.builder()
            .description(containerInfo.getDescription())
            .label(containerInfo.getLabel())
            .id(containerInfo.getId());
        containerInfo.getGraphInfos().forEach(graphInfo -> {
            final GenericGraph genericGraph = GenericGraph.builder()
                    .namespace(graphInfo.getNamespace())
                    .property(GenericProperties.LABEL, graphInfo.getLabel())
                    .property(GenericProperties.DESCRIPTION, graphInfo.getDescription())
                    .build();
            genericGraphContainerBuilder.addGraph(genericGraph);
        });
        save(genericGraphContainerBuilder.build());
    }
}
