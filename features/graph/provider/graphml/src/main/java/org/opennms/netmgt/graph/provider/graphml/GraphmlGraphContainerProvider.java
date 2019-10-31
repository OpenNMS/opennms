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

package org.opennms.netmgt.graph.provider.graphml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLGraph;
import org.opennms.features.graphml.model.GraphMLReader;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraph.GenericGraphBuilder;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer.GenericGraphContainerBuilder;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;

import com.google.common.collect.Lists;

public class GraphmlGraphContainerProvider implements GraphContainerProvider {

    private static final String FOCUS_STRATEGY = "focus-strategy";
    private static final String FOCUS_IDS = "focus-ids";

    private final GraphML graphML;
    private GenericGraphContainer graphContainer;
    private HashMap<String, GraphMLGraph> vertexIdToGraphMapping;

    public GraphmlGraphContainerProvider(InputStream inputStream) throws InvalidGraphException {
        this(GraphMLReader.read(inputStream));
    }

    public GraphmlGraphContainerProvider(GraphML graphML) {
        this.graphML = Objects.requireNonNull(graphML);
        // This should not be invoked at this point, however it is static anyways and in order
        // to know the graph infos we must read the data.
        // Maybe we can just read it partially at some point, however this is how it is implemented for now
        loadGraphContainer();
    }

    public GraphmlGraphContainerProvider(String location) throws IOException, InvalidGraphException {
        if (!new File(location).exists()) {
            throw new FileNotFoundException(location);
        }
        try (InputStream input = new FileInputStream(location)) {
            this.graphML = GraphMLReader.read(input);
        }
        // TODO MVR duplicated comment
        // This should not be invoked at this point, however it is static anyways and in order
        // to know the graph infos we must read the data.
        // Maybe we can just read it partially at some point, however this is how it is implemented for now
        loadGraphContainer();
    }

//    @Override
//    public void setNotificationService(GraphNotificationService notificationService) {
//
//    }

    @Override
    public ImmutableGraphContainer loadGraphContainer() {
        if (graphContainer == null) {
            vertexIdToGraphMapping = new HashMap<>();
            // Index vertex id to graph mapping
            graphML.getGraphs().stream().forEach(
                    g -> g.getNodes().stream().forEach(n -> {
                        if (vertexIdToGraphMapping.containsKey(n.getId())) {
                            throw new IllegalStateException("GraphML graph contains vertices with same id. Bailing");
                        }
                        vertexIdToGraphMapping.put(n.getId(), g);
                    })
            );

            // Convert graph
            final String graphContainerId = determineGraphContainerId(graphML);
            final GenericGraphContainerBuilder graphContainerBuilder = GenericGraphContainer.builder()
                .id(graphContainerId)
                .label(graphML.getProperty(GenericProperties.LABEL))
                .description(graphML.getProperty(GenericProperties.DESCRIPTION));
            for (GraphMLGraph eachGraph : graphML.getGraphs()) {
                final GenericGraph convertedGraph = convert(eachGraph);
                graphContainerBuilder.addGraph(convertedGraph);
            }
            this.graphContainer = graphContainerBuilder.build();
        }
        return this.graphContainer;
    }

    @Override
    public GraphContainerInfo getContainerInfo() {
        // AS this is static content, the container info is already part of the graph, no extra setup required
        // TODO MVR maybe we should partially read this while instantiating and then implement the full loading,
        // But as this is already al lin memory anyways we can just do the conversion when instantiating. At least for now
        return graphContainer;
    }

    private final GenericGraph convert(GraphMLGraph graphMLGraph) {
        final GenericGraphBuilder graphBuilder = GenericGraph.builder().properties(graphMLGraph.getProperties());
        final List<GenericVertex> vertices = graphMLGraph.getNodes()
                .stream().map(n -> {
                    // In case of GraphML each vertex does not have a namespace, but it is inherited from the graph
                    // Therefore here we have to manually set it
                    return GenericVertex.builder()
                            .namespace(graphBuilder.getNamespace())
                            .id(n.getId())
                            .properties(n.getProperties()).build();
                })
                .collect(Collectors.toList());
        graphBuilder.addVertices(vertices);

        final List<GenericEdge> edges = graphMLGraph.getEdges().stream().map(e -> {
            final String sourceNamespace = vertexIdToGraphMapping.get(e.getSource().getId()).getProperty(GenericProperties.NAMESPACE);
            final String targetNamespace = vertexIdToGraphMapping.get(e.getTarget().getId()).getProperty(GenericProperties.NAMESPACE);
            final GenericVertex source = GenericVertex.builder().namespace(sourceNamespace).id(e.getSource().getId()).build();
            final GenericVertex target = GenericVertex.builder().namespace(targetNamespace).id(e.getTarget().getId()).build();
            // In case of GraphML each edge does not have a namespace, but it is inherited from the graph
            // Therefore here we have to manually set it
            final GenericEdge edge = GenericEdge.builder()
                    .namespace(graphBuilder.getNamespace())
                    .source(source.getVertexRef())
                    .target(target.getVertexRef())
                    .properties(e.getProperties()).build();
            return edge;
        }).collect(Collectors.toList());
        graphBuilder.addEdges(edges);

        applyFocus(graphMLGraph, graphBuilder);
        return graphBuilder.build();
    }

    private static void applyFocus(final GraphMLGraph graphMLGraph, final GenericGraphBuilder graphBuilder) {
        final String strategy = graphMLGraph.getProperty(FOCUS_STRATEGY);
        if (strategy == null || "empty".equalsIgnoreCase(strategy)) {
            graphBuilder.focus().empty().apply();
        } else if ("all".equalsIgnoreCase(strategy)) {
            graphBuilder.focus().all().apply();
        } else if ("first".equalsIgnoreCase(strategy)) {
            graphBuilder.focus().first().apply();
            graphBuilder.focus().empty().apply();
        } else if ("specific".equalsIgnoreCase(strategy) || "selection".equalsIgnoreCase(strategy)) {
            final List<String> focusIds = getFocusIds(graphMLGraph);
            graphBuilder.focus().selection(graphBuilder.getNamespace(), focusIds).apply();
        } else {
            final String[] supportedStrategies = new String[]{"empty", "all", "first", "specific"};
            throw new IllegalStateException("Provided focus strategy '" + strategy + "' is not supported. Supported values are: " + Arrays.toString(supportedStrategies));
        }
    }

    private static List<String> getFocusIds(GraphMLGraph inputGraph) {
        final String property = inputGraph.getProperty(FOCUS_IDS);
        if (property != null) {
            String[] split = property.split(",");
            return Lists.newArrayList(split);
        }
        return Lists.newArrayList();
    }

    // The graphML specification does not allow for an id on the graphML object itself
    // As we always need a unique Id we check if a property called `containerId` is provided.
    // If so we use that, otherwise we concatenate the ids of the graphs
    // TODO MVR when sending the topology/graphml/test-topology.xml the cntainer id is test instead of graphml :(
    protected static String determineGraphContainerId(GraphML graphML) {
        if (graphML.getProperty("containerId") != null) {
            return graphML.getProperty("containerId");
        }
        final String calculatedId = graphML.getGraphs().stream().map(g -> g.getId()).collect(Collectors.joining("."));
        return calculatedId;
    }

}
