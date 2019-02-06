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

package org.opennms.netmgt.graph.persistence.hibernate;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.graph.api.GraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.DefaultGraphContainerInfo;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.persistence.api.GraphRepository;
import org.opennms.netmgt.graph.persistence.hibernate.mapper.EntityToGenericMapper;
import org.opennms.netmgt.graph.persistence.hibernate.mapper.GenericToEntityMapper;
import org.opennms.netmgt.graph.simple.SimpleVertex;
import org.opennms.netmgt.graph.updates.change.ContainerChangeSet;
import org.opennms.netmgt.topology.EdgeEntity;
import org.opennms.netmgt.topology.GraphContainerEntity;
import org.opennms.netmgt.topology.GraphEntity;
import org.opennms.netmgt.topology.PropertyEntity;
import org.opennms.netmgt.topology.VertexEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DefaultGraphRepository implements GraphRepository {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultGraphRepository.class);

    private final EntityToGenericMapper entityToGenericMapper = new EntityToGenericMapper();

    private final GenericToEntityMapper genericToEntityMapper = new GenericToEntityMapper();

    private final GenericPersistenceAccessor accessor;

    public DefaultGraphRepository(GenericPersistenceAccessor genericPersistenceAccessor) {
        this.accessor = Objects.requireNonNull(genericPersistenceAccessor);
    }

    @Override
    public <C extends GraphContainer> C findContainerById(String containerId, Function<GenericGraphContainer, C> transformer) {
        final GraphContainerEntity entity = findContainerEntity(containerId);
        if (entity != null) {
            final GenericGraphContainer genericGraphContainer = entityToGenericMapper.fromEntity(entity);
            final C convertedGraphContainer = transformer.apply(genericGraphContainer);
            return convertedGraphContainer;
        }
        return null;
    }

    @Override
    public GenericGraphContainer findContainerById(String containerId) {
        return findContainerById(containerId, Function.identity());
    }

    @Override
    public GraphContainerInfo findContainerInfoById(String containerId) {
        // Fetch all meta data of the container and graphs (no vertices and edges) with one select
        // We load the container and all its graph entities as well as the related properties.
        // This may load unnecessary properties, but is probably neglectable at the moment.
        // Vertices and Edges are not loaded, as they are lazy loaded.
        final List<GraphContainerEntity> graphContainerEntities = accessor.find("select distinct ge from GraphContainerEntity ge join ge.properties join ge.graphs as graphs join graphs.properties where ge.namespace = ?", containerId);
        if (graphContainerEntities.isEmpty()) {
            return null;
        }
        // Now convert
        final GraphContainerEntity containerEntity =  graphContainerEntities.get(0);
        final DefaultGraphContainerInfo containerInfo = new DefaultGraphContainerInfo(containerEntity.getNamespace());
        containerInfo.setLabel(containerEntity.getLabel());
        containerInfo.setDescription(containerEntity.getDescription());
        containerEntity.getGraphs().forEach(graphEntity -> {
            final DefaultGraphInfo graphInfo = new DefaultGraphInfo(graphEntity.getNamespace(), SimpleVertex.class /* TODO MVR this is not correct */);
            graphInfo.setLabel(graphEntity.getLabel());
            graphInfo.setDescription(graphEntity.getDescription());
            containerInfo.getGraphInfos().add(graphInfo);
        });
        return containerInfo;
    }

    @Override
    public void deleteContainer(String containerId) {
        final GraphContainerEntity containerEntity = findContainerEntity(containerId);
        if (containerEntity == null) {
            throw new NoSuchElementException("No container with id " + containerId + " found.");
        }
        accessor.delete(containerEntity);
    }

    @Override
    public void save(final GraphContainer graphContainer) {
        final GenericGraphContainer persistedGraphContainer = findContainerById(graphContainer.getId());
        final GenericGraphContainer genericGraphContainer = graphContainer.asGenericGraphContainer();
        if (persistedGraphContainer == null) {
            LOG.debug("Graph Container (id: {}) is new. Persisting...", graphContainer.getId());
            final GraphContainerEntity graphContainerEntity = genericToEntityMapper.toEntity(genericGraphContainer);
            accessor.save(graphContainerEntity);
            LOG.debug("Graph Container (id: {}) persisted.", graphContainer.getId());
        } else {
            LOG.debug("Graph Container (id: {}) exists. Calculating change set...", graphContainer.getId());

            // The Changes are calculated on the Generic conversion of the input and persisted graph
            // In order to apply the changes here, they must again be converted to the actual implementation of the persisted graph (entity).
            ContainerChangeSet containerChangeSet = new ContainerChangeSet(persistedGraphContainer, genericGraphContainer);
            if (containerChangeSet.hasChanges()) {
                final GraphContainerEntity graphContainerEntity = findContainerEntity(graphContainer.getId());

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
                        graphEntity.apply(changeSet.getGraphInfo());
                    }

                    // Update Edges
                    changeSet.getEdgesRemoved().forEach(edge -> {
                        final EdgeEntity edgeEntity = graphEntity.getEdgeByProperty("id", edge.getId());
                        graphEntity.removeEdge(edgeEntity);
                    });
                    changeSet.getEdgesAdded().forEach(edge -> {
                        final EdgeEntity edgeEntity = genericToEntityMapper.toEntity((GenericEdge) edge, graphEntity);
                        graphEntity.addEdge(edgeEntity);
                    });
                    changeSet.getEdgesUpdated().forEach(edge -> {
                        final EdgeEntity edgeEntity = graphEntity.getEdgeByProperty("id", edge.getId());
                        final List<PropertyEntity> propertyEntities = genericToEntityMapper.convertToPropertyEntities(((GenericEdge) edge).getProperties());
                        edgeEntity.mergeProperties(propertyEntities);
                    });

                    // Update Vertices
                    changeSet.getVerticesRemoved().forEach(vertex -> {
                        final VertexEntity vertexEntity = graphEntity.getVertexByProperty("id", vertex.getId());
                        graphEntity.removeVertex(vertexEntity);
                    });
                    changeSet.getVerticesAdded().forEach(vertex -> {
                        final VertexEntity vertexEntity = genericToEntityMapper.toEntity((GenericVertex) vertex);
                        graphEntity.addVertex(vertexEntity);
                    });
                    changeSet.getVerticesUpdated().forEach(vertex -> {
                        final VertexEntity vertexEntity = graphEntity.getVertexByProperty("id", vertex.getId());
                        final List<PropertyEntity> propertyEntities = genericToEntityMapper.convertToPropertyEntities(((GenericVertex) vertex).getProperties());
                        vertexEntity.mergeProperties(propertyEntities);
                    });
                });
                accessor.update(graphContainerEntity);
            }
            LOG.debug("Graph Container (id: {}) updated.", graphContainer.getId());
        }
    }

    @Override
    public void save(GraphContainerInfo containerInfo) {
        // We simply convert to a container and persist it
        final GenericGraphContainer genericGraphContainer = new GenericGraphContainer();
        genericGraphContainer.setDescription(containerInfo.getDescription());
        genericGraphContainer.setLabel(containerInfo.getLabel());
        genericGraphContainer.setId(containerInfo.getId());
        containerInfo.getGraphInfos().forEach(graphInfo -> {
            final GenericGraph genericGraph = new GenericGraph();
            genericGraph.setProperty(GenericProperties.NAMESPACE, graphInfo.getNamespace());
            genericGraph.setProperty(GenericProperties.LABEL, graphInfo.getLabel());
            genericGraph.setProperty(GenericProperties.DESCRIPTION, graphInfo.getDescription());
            genericGraphContainer.addGraph(genericGraph);
        });
        save(genericGraphContainer);
    }

    private GraphContainerEntity findContainerEntity(String containerId) {
        final List<GraphContainerEntity> containers = accessor.find("Select g from GraphContainerEntity g where g.namespace = ?", containerId);
        if (containers.isEmpty()) {
            return null;
        }
        return containers.get(0);
    }
}
