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

package org.opennms.features.graph.persistence.hibernate;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.opennms.features.graph.api.Graph;
import org.opennms.features.graph.api.GraphContainer;
import org.opennms.features.graph.api.generic.GenericEdge;
import org.opennms.features.graph.api.generic.GenericGraph;
import org.opennms.features.graph.api.generic.GenericGraphContainer;
import org.opennms.features.graph.api.generic.GenericVertex;
import org.opennms.features.graph.persistence.api.GraphRepository;
import org.opennms.netmgt.topology.GraphContainerEntity;
import org.opennms.netmgt.topology.GraphEntity;
import org.opennms.features.graph.persistence.hibernate.mapper.EntityToGenericMapper;
import org.opennms.features.graph.persistence.hibernate.mapper.GenericToEntityMapper;
import org.opennms.features.graph.updates.change.ChangeSet;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DefaultGraphRepository implements GraphRepository {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultGraphRepository.class);

    @Autowired
    private GenericPersistenceAccessor accessor;

    private EntityToGenericMapper entityToGenericMapper = new EntityToGenericMapper();
    private GenericToEntityMapper genericToEntityMapper = new GenericToEntityMapper();

    @Override
    public <C extends GraphContainer> C findContainerById(String containerId, Function<GenericGraphContainer, C> transformer) {
        final List<GraphContainerEntity> graphContainers = accessor.find("Select g from GraphContainerEntity g where g.namespace = ?", containerId);
        if (graphContainers.isEmpty()) {
            return null;
        }
        final GraphContainerEntity entity = graphContainers.get(0);
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
    public void deleteContainer(String containerId) {

    }

    @Override
    public void save(GraphContainer graphContainer) {
        GraphContainer container = findContainerById(graphContainer.getId());
        if (container == null) {
            LOG.debug("Graph Container (id: {}) is new. Persisting...", graphContainer.getId());
            final GenericGraphContainer genericGraphContainer = graphContainer.asGenericGraphContainer();
            final GraphContainerEntity graphContainerEntity = genericToEntityMapper.toEntity(genericGraphContainer);
            accessor.save(graphContainerEntity);
            LOG.debug("Graph Container (id: {}) persisted.", graphContainer.getId());
        } else {
            LOG.debug("Graph Container (id: {}) exists. Calculating change set...", graphContainer.getId());
            // TODO MVR implement
        }
    }

    private GenericGraph findGraphByNamespace(String namespace) {
        final List<GraphEntity> graphs = accessor.find("Select g from GraphEntity g where g.namespace = ?", namespace);
        if (graphs.isEmpty()) {
            return null;
        }
        final GraphEntity graphEntity = graphs.get(0);
        final GenericGraph genericGraph = entityToGenericMapper.fromEntity(graphEntity);
        return genericGraph;
    }

    @Override
    public void save(Graph graph) {
        Objects.requireNonNull(graph);
        long start = System.currentTimeMillis();
        System.out.println("Converting graph to generic graph");
        final GenericGraph genericGraph = graph.asGenericGraph();
        System.out.println("DONE. Took " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        System.out.println("Converting to graph entity");
        final GraphEntity graphEntity = genericToEntityMapper.toEntity(genericGraph);
        System.out.println("DONE. Took " + (System.currentTimeMillis() - start) + "ms");

        // Here we detect if a graph must be updated or persisted
        // This way we always detect changes even if the entity persisted was not received from the persistence context
        // in the first place.
        final GenericGraph persistedGraph = findGraphByNamespace(graph.getNamespace());
        if (persistedGraph == null) {
            System.out.println("ACTUALLY START PERSISTING NOW... WIU WIU WIU");
            start = System.currentTimeMillis();
            accessor.save(graphEntity);
            System.out.println("DONE. Took " + (System.currentTimeMillis() - start) + "ms");
        } else {
            final ChangeSet<GenericGraph, GenericVertex, GenericEdge> changeSet = new ChangeSet<>(persistedGraph, genericGraph);
            if (changeSet.hasChanges()) {
                changeSet.getEdgesRemoved().forEach(edge -> persistedGraph.removeEdge(edge));
                changeSet.getEdgesAdded().forEach(edge -> persistedGraph.addEdge(edge));
                changeSet.getEdgesUpdated().forEach(edge -> {
                    final GenericEdge persistedEdge = persistedGraph.getEdge(edge.getId());
                    persistedEdge.setProperties(edge.getProperties());
                });
                changeSet.getVerticesRemoved().forEach(vertex -> persistedGraph.removeVertex(vertex));
                changeSet.getVerticesAdded().forEach(vertex -> persistedGraph.addVertex(vertex));
                changeSet.getVerticesUpdated().forEach(vertex -> {
                    final GenericVertex persistedVertex = persistedGraph.getVertex(vertex.getId());
                    persistedVertex.setProperties(vertex.getProperties());
                });
                accessor.save(persistedGraph);
            }
        }
    }

//    @Override
//    public GenericGraph findByNamespace(String namespace) {
//        final List<GraphEntity> graphs = accessor.find("Select g from GraphEntity g where g.namespace = ?", namespace);
//        if (graphs.isEmpty()) {
//            return null;
//        }
//        final GraphEntity graphEntity = graphs.get(0);
//        final GenericGraph genericGraph = fromEntity(graphEntity);
//        return genericGraph;
//    }
//
//    @Override
//    public GraphInfo findGraphInfo(String namespace) {
//        List<GraphEntity> graphEntities = accessor.find("select ge from GraphEntity ge where ge.namespace = ?", namespace);
//        if (graphEntities == null || graphEntities.isEmpty()) {
//            return null;
//        }
//        return convert(graphEntities.get(0));
//    }
//
//    @Override
//    public <G extends Graph<V, E>, V extends Vertex, E extends Edge<V>> G findByNamespace(String namespace, Function<GenericGraph, G> transformer) {
//        Objects.requireNonNull(namespace);
//        Objects.requireNonNull(transformer);
//        final GenericGraph genericGraph = findByNamespace(namespace);
//        if (genericGraph != null) {
//            final G convertedGraph = transformer.apply(genericGraph);
//            return convertedGraph;
//        }
//        return null;
//    }
//
//    @Override
//    public List<GraphInfo> findAll() {
//        final List<GraphEntity> graphs = accessor.find("Select g from GraphEntity g");
//        final List<GraphInfo> graphInfos = graphs.stream().map(g -> convert(g)).collect(Collectors.toList());
//        return graphInfos;
//    }
//
//    @Override
//    public void deleteByNamespace(String namespace) {
//        // TODO MVR implement delete cascade automatically
//        final GenericGraph graph = findByNamespace(namespace);
//        if (graph != null) {
//            // TODO MVR implement me
////            accessor.delete(graph);
//        }
//    }





}
